/**
 * 检查覆盖率工具
 *
 * 自定义 OpenCode 工具，用于分析测试覆盖率并报告缺口。
 * 支持常见的覆盖率报告格式。
 * 支持：JavaScript/TypeScript (Istanbul/nyc) 和 Kotlin (JaCoCo/Kover)
 */

import { tool } from "@opencode-ai/plugin/tool"
import * as path from "path"
import * as fs from "fs"

export default tool({
  description: "根据阈值检查测试覆盖率，并识别覆盖率较低的文件。从常见位置读取覆盖率报告。支持 JS/TS (Istanbul/nyc) 和 Kotlin (JaCoCo/Kover) 项目。",
  args: {
    threshold: tool.schema
      .number()
      .optional()
      .describe("所需的最低覆盖率百分比（默认：80）"),
    showUncovered: tool.schema
      .boolean()
      .optional()
      .describe("显示未覆盖文件的列表（默认：true）"),
    format: tool.schema
      .enum(["summary", "detailed", "json"])
      .optional()
      .describe("输出格式（默认：summary）"),
    platform: tool.schema
      .string()
      .optional()
      .describe("指定平台：js 或 kotlin（默认：自动检测）"),
  },
  async execute(args, context) {
    const threshold = args.threshold ?? 80
    const showUncovered = args.showUncovered ?? true
    const format = args.format ?? "summary"
    const platform = args.platform
    const cwd = context.worktree || context.directory

    const detectedPlatform = platform || await detectPlatform(cwd)

    if (detectedPlatform === "kotlin") {
      return await checkKotlinCoverage(cwd, { threshold, showUncovered, format })
    }

    return await checkJSCoverage(cwd, { threshold, showUncovered, format })
  },
})

async function checkJSCoverage(
  cwd: string,
  options: { threshold: number; showUncovered: boolean; format: string }
) {
  const { threshold, showUncovered, format } = options

  const coveragePaths = [
    "coverage/coverage-summary.json",
    "coverage/lcov-report/index.html",
    "coverage/coverage-final.json",
    ".nyc_output/coverage.json",
  ]

  let coverageData: CoverageSummary | null = null
  let coverageFile: string | null = null

  for (const coveragePath of coveragePaths) {
    const fullPath = path.join(cwd, coveragePath)
    if (fs.existsSync(fullPath) && coveragePath.endsWith(".json")) {
      try {
        const content = JSON.parse(fs.readFileSync(fullPath, "utf-8"))
        coverageData = parseJSCoverageData(content)
        coverageFile = coveragePath
        break
      } catch {
        // 继续检查下一个文件
      }
    }
  }

  if (!coverageData) {
    return JSON.stringify({
      success: false,
      error: "未找到覆盖率报告",
      suggestion: "先运行带覆盖率的测试：npm test -- --coverage",
      searchedPaths: coveragePaths,
    })
  }

  const passed = coverageData.total.percentage >= threshold
  const uncoveredFiles = coverageData.files.filter(
    (f) => f.percentage < threshold
  )

  const result: CoverageResult = {
    success: passed,
    threshold,
    coverageFile,
    total: coverageData.total,
    passed,
    platform: "javascript/typescript",
  }

  if (format === "detailed" || (showUncovered && uncoveredFiles.length > 0)) {
    result.uncoveredFiles = uncoveredFiles.slice(0, 20)
    result.uncoveredCount = uncoveredFiles.length
  }

  if (format === "json") {
    result.rawData = coverageData
  }

  if (!passed) {
    result.suggestion = `覆盖率为 ${coverageData.total.percentage.toFixed(1)}%，低于 ${threshold}% 的阈值。专注于这些文件：\n${uncoveredFiles
      .slice(0, 5)
      .map((f) => `- ${f.file}: ${f.percentage.toFixed(1)}%`)
      .join("\n")}`
  }

  return JSON.stringify(result)
}

async function checkKotlinCoverage(
  cwd: string,
  options: { threshold: number; showUncovered: boolean; format: string }
) {
  const { threshold, showUncovered, format } = options

  const koverPaths = [
    "build/reports/kover/report.json",
    "build/kover/kover-report.json",
    "kover-report.json",
    "build/reports/kover/merged/report.json",
  ]

  const jacocoPaths = [
    "build/reports/jacoco/test/jacocoTestReport.xml",
    "build/jacoco/test-jacoco.xml",
    "build/reports/jacoco/jacoco.xml",
    "jacoco.xml",
  ]

  let coverageData: CoverageSummary | null = null
  let coverageFile: string | null = null
  let coverageType: string = ""

  for (const koverPath of koverPaths) {
    const fullPath = path.join(cwd, koverPath)
    if (fs.existsSync(fullPath)) {
      try {
        const content = JSON.parse(fs.readFileSync(fullPath, "utf-8"))
        coverageData = parseKoverCoverageData(content)
        coverageFile = koverPath
        coverageType = "kover"
        break
      } catch {
        // 继续检查下一个文件
      }
    }
  }

  if (!coverageData) {
    for (const jacocoPath of jacocoPaths) {
      const fullPath = path.join(cwd, jacocoPath)
      if (fs.existsSync(fullPath)) {
        try {
          const content = fs.readFileSync(fullPath, "utf-8")
          coverageData = parseJacocoCoverageData(content)
          coverageFile = jacocoPath
          coverageType = "jacoco"
          break
        } catch {
          // 继续检查下一个文件
        }
      }
    }
  }

  if (!coverageData) {
    return JSON.stringify({
      success: false,
      error: "未找到 Kotlin 覆盖率报告（JaCoCo 或 Kover）",
      suggestion: "运行带覆盖率的测试：\n- JaCoCo: ./gradlew test jacocoTestReport\n- Kover: ./gradlew koverReport 或 ./gradlew test koverReport",
      searchedPaths: [...koverPaths, ...jacocoPaths],
    })
  }

  const passed = coverageData.total.percentage >= threshold
  const uncoveredFiles = coverageData.files.filter(
    (f) => f.percentage < threshold
  )

  const result: CoverageResult = {
    success: passed,
    threshold,
    coverageFile,
    total: coverageData.total,
    passed,
    platform: "kotlin",
    coverageType,
  }

  if (format === "detailed" || (showUncovered && uncoveredFiles.length > 0)) {
    result.uncoveredFiles = uncoveredFiles.slice(0, 20)
    result.uncoveredCount = uncoveredFiles.length
  }

  if (format === "json") {
    result.rawData = coverageData
  }

  if (!passed) {
    result.suggestion = `覆盖率为 ${coverageData.total.percentage.toFixed(1)}%，低于 ${threshold}% 的阈值。专注于这些文件：\n${uncoveredFiles
      .slice(0, 5)
      .map((f) => `- ${f.file}: ${f.percentage.toFixed(1)}%`)
      .join("\n")}`
  }

  return JSON.stringify(result)
}

async function detectPlatform(cwd: string): Promise<"js" | "kotlin"> {
  const kotlinIndicators = [
    "build.gradle.kts",
    "build.gradle",
    "settings.gradle.kts",
    "settings.gradle",
    "pom.xml",
    "gradlew",
  ]

  for (const indicator of kotlinIndicators) {
    if (fs.existsSync(path.join(cwd, indicator))) {
      return "kotlin"
    }
  }

  const jsIndicators = [
    "package.json",
    "pnpm-lock.yaml",
    "yarn.lock",
    "package-lock.json",
  ]

  for (const indicator of jsIndicators) {
    if (fs.existsSync(path.join(cwd, indicator))) {
      return "js"
    }
  }

  return "js"
}

interface CoverageSummary {
  total: {
    lines: number
    covered: number
    percentage: number
  }
  files: Array<{
    file: string
    lines: number
    covered: number
    percentage: number
  }>
}

interface CoverageResult {
  success: boolean
  threshold: number
  coverageFile: string | null
  total: CoverageSummary["total"]
  passed: boolean
  platform?: string
  coverageType?: string
  uncoveredFiles?: CoverageSummary["files"]
  uncoveredCount?: number
  rawData?: CoverageSummary
  suggestion?: string
}

function parseJSCoverageData(data: unknown): CoverageSummary {
  if (typeof data === "object" && data !== null && "total" in data) {
    const istanbulData = data as Record<string, unknown>
    const total = istanbulData.total as Record<string, { total: number; covered: number }>

    const files: CoverageSummary["files"] = []

    for (const [key, value] of Object.entries(istanbulData)) {
      if (key !== "total" && typeof value === "object" && value !== null) {
        const fileData = value as Record<string, { total: number; covered: number }>
        if (fileData.lines) {
          files.push({
            file: key,
            lines: fileData.lines.total,
            covered: fileData.lines.covered,
            percentage: fileData.lines.total > 0
              ? (fileData.lines.covered / fileData.lines.total) * 100
              : 100,
          })
        }
      }
    }

    return {
      total: {
        lines: total.lines?.total || 0,
        covered: total.lines?.covered || 0,
        percentage: total.lines?.total
          ? (total.lines.covered / total.lines.total) * 100
          : 0,
      },
      files,
    }
  }

  return {
    total: { lines: 0, covered: 0, percentage: 0 },
    files: [],
  }
}

interface KoverReportData {
  total: {
    covered: number
    missed: number
    total: number
    coveredPercent: number
  }
  groups: Array<{
    name: string
    covered: number
    missed: number
    total: number
    coveredPercent: number
    classes: Array<{
      name: string
      fileName?: string
      sourceFileName?: string
      covered: number
      missed: number
      total: number
      coveredPercent: number
    }>
  }>
}

function parseKoverCoverageData(data: unknown): CoverageSummary {
  const koverData = data as KoverReportData
  const files: CoverageSummary["files"] = []

  let totalCovered = 0
  let totalMissed = 0

  if (koverData.groups) {
    for (const group of koverData.groups) {
      if (group.classes) {
        for (const cls of group.classes) {
          const fileName = cls.sourceFileName || cls.fileName || cls.name
          const covered = cls.covered || 0
          const missed = cls.missed || 0
          const total = covered + missed

          if (total > 0) {
            files.push({
              file: fileName,
              lines: total,
              covered: covered,
              percentage: (covered / total) * 100,
            })
          }

          totalCovered += covered
          totalMissed += missed
        }
      }
    }
  }

  const totalLines = totalCovered + totalMissed

  const total = koverData.total
    ? {
        lines: totalLines,
        covered: totalCovered,
        percentage: totalLines > 0 ? (totalCovered / totalLines) * 100 : 0,
      }
    : {
        lines: totalLines,
        covered: totalCovered,
        percentage: total > 0 ? total.coveredPercent : 0,
      }

  return {
    total,
    files,
  }
}

function parseJacocoCoverageData(xmlContent: string): CoverageSummary {
  const files: CoverageSummary["files"] = []

  const counterRegex = /<counter type="LINE" missed="(\d+)" covered="(\d+)"/g
  const packageRegex = /<package name="([^"]+)"/g
  const classRegex = /<class name="([^"]+)" filename="([^"]+)"/g

  let totalMissed = 0
  let totalCovered = 0

  const packageMap = new Map<string, { missed: number; covered: number; files: string[] }>()

  let packageMatch
  while ((packageMatch = packageRegex.exec(xmlContent)) !== null) {
    const packageName = packageMatch[1]
    packageMap.set(packageName, { missed: 0, covered: 0, files: [] })
  }

  let counterMatch
  const countersInPackage: Map<string, { missed: number; covered: number }> = new Map()

  const packageStartRegex = /<package name="([^"]+)"[^>]*>[\s\S]*?<counter type="LINE"/g
  let packageStartMatch
  while ((packageStartMatch = packageStartRegex.exec(xmlContent)) !== null) {
    const pkgName = packageStartMatch[1]
    const pkgStartIndex = packageStartMatch.index

    const nextPackageStart = xmlContent.indexOf('<package name="', pkgStartIndex + 1)
    const pkgContent = nextPackageStart === -1
      ? xmlContent.slice(pkgStartIndex)
      : xmlContent.slice(pkgStartIndex, nextPackageStart)

    const pkgCounterMatch = /<counter type="LINE" missed="(\d+)" covered="(\d+)"/.exec(pkgContent)
    if (pkgCounterMatch) {
      const missed = parseInt(pkgCounterMatch[1], 10)
      const covered = parseInt(pkgCounterMatch[2], 10)
      totalMissed += missed
      totalCovered += covered
    }
  }

  let classMatch
  while ((classMatch = classRegex.exec(xmlContent)) !== null) {
    const className = classMatch[1]
    const filename = classMatch[2]

    const classStartIndex = classMatch.index
    const nextClassStart = xmlContent.indexOf('<class name="', classStartIndex + 1)
    const classContent = nextClassStart === -1
      ? xmlContent.slice(classStartIndex)
      : xmlContent.slice(classStartIndex, nextClassStart)

    const counterMatch = /<counter type="LINE" missed="(\d+)" covered="(\d+)"/.exec(classContent)
    if (counterMatch) {
      const missed = parseInt(counterMatch[1], 10)
      const covered = parseInt(counterMatch[2], 10)
      const total = missed + covered

      files.push({
        file: filename,
        lines: total,
        covered: covered,
        percentage: total > 0 ? (covered / total) * 100 : 100,
      })
    }
  }

  const totalLines = totalMissed + totalCovered

  return {
    total: {
      lines: totalLines,
      covered: totalCovered,
      percentage: totalLines > 0 ? (totalCovered / totalLines) * 100 : 0,
    },
    files,
  }
}
