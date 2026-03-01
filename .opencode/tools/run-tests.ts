/**
 * 运行测试工具
 *
 * 自定义 OpenCode 工具，用于运行具有各种选项的测试套件。
 * 自动检测包管理器和测试框架。
 * 支持：JavaScript/TypeScript (npm/pnpm/yarn/bun) 和 Kotlin (Gradle/Maven)
 */

import { tool } from "@opencode-ai/plugin/tool"
import * as path from "path"
import * as fs from "fs"

export default tool({
  description: "运行测试套件，可选带覆盖率、监听模式或特定测试模式。自动检测包管理器（npm、pnpm、yarn、bun）和测试框架。也支持 Kotlin (Gradle/Maven) 项目。",
  args: {
    pattern: tool.schema
      .string()
      .optional()
      .describe("要运行的测试文件模式或特定测试名称"),
    coverage: tool.schema
      .boolean()
      .optional()
      .describe("运行并生成覆盖率报告（默认：false）"),
    watch: tool.schema
      .boolean()
      .optional()
      .describe("以监听模式运行，进行持续测试（默认：false）"),
    updateSnapshots: tool.schema
      .boolean()
      .optional()
      .describe("更新 Jest/Vitest 快照（默认：false）"),
    platform: tool.schema
      .string()
      .optional()
      .describe("指定平台：js 或 kotlin（默认：自动检测）"),
  },
  async execute(args, context) {
    const { pattern, coverage, watch, updateSnapshots, platform } = args
    const cwd = context.worktree || context.directory

    const detectedPlatform = platform || await detectPlatform(cwd)

    if (detectedPlatform === "kotlin") {
      return await executeKotlinTests(cwd, { pattern, coverage, watch })
    }

    return await executeJSTests(cwd, { pattern, coverage, watch, updateSnapshots })
  },
})

async function executeJSTests(
  cwd: string,
  options: { pattern?: string; coverage?: boolean; watch?: boolean; updateSnapshots?: boolean }
) {
  const { pattern, coverage, watch, updateSnapshots } = options

  const packageManager = await detectPackageManager(cwd)
  const testFramework = await detectTestFramework(cwd)

  let cmd: string[] = [packageManager]

  if (packageManager === "npm") {
    cmd.push("run", "test")
  } else {
    cmd.push("test")
  }

  const testArgs: string[] = []

  if (coverage) {
    testArgs.push("--coverage")
  }

  if (watch) {
    testArgs.push("--watch")
  }

  if (updateSnapshots) {
    testArgs.push("-u")
  }

  if (pattern) {
    if (testFramework === "jest" || testFramework === "vitest") {
      testArgs.push("--testPathPattern", pattern)
    } else {
      testArgs.push(pattern)
    }
  }

  if (testArgs.length > 0) {
    if (packageManager === "npm") {
      cmd.push("--")
    }
    cmd.push(...testArgs)
  }

  const command = cmd.join(" ")

  return JSON.stringify({
    command,
    packageManager,
    testFramework,
    platform: "javascript/typescript",
    options: {
      pattern: pattern || "all tests",
      coverage: coverage || false,
      watch: watch || false,
      updateSnapshots: updateSnapshots || false,
    },
    instructions: `运行此命令来执行测试：\n\n${command}`,
  })
}

async function executeKotlinTests(
  cwd: string,
  options: { pattern?: string; coverage?: boolean; watch?: boolean }
) {
  const { pattern, coverage, watch } = options

  const buildTool = await detectKotlinBuildTool(cwd)
  const testFramework = await detectKotlinTestFramework(cwd)

  let cmd: string[] = []

  if (buildTool === "gradle") {
    const hasWrapper = fs.existsSync(path.join(cwd, "gradlew"))
    cmd = hasWrapper ? ["./gradlew"] : ["gradle"]

    if (watch) {
      cmd.push("test", "--continuous")
    } else {
      cmd.push("test")
    }

    if (coverage) {
      cmd.push("--coverage")
    }

    if (pattern) {
      cmd.push("--tests", pattern)
    }
  } else if (buildTool === "maven") {
    const hasWrapper = fs.existsSync(path.join(cwd, "mvnw"))
    cmd = hasWrapper ? ["./mvnw"] : ["mvn"]

    cmd.push("test")

    if (pattern) {
      cmd.push("-Dtest", pattern)
    }

    if (coverage) {
      cmd.push("-Dcoverage=true")
    }
  } else {
    return JSON.stringify({
      success: false,
      error: "未找到 Gradle 或 Maven 构建工具",
      suggestion: "确保项目根目录有 gradlew/mvnw 或 build.gradle/pom.xml",
    })
  }

  const command = cmd.join(" ")

  return JSON.stringify({
    command,
    buildTool,
    testFramework,
    platform: "kotlin",
    options: {
      pattern: pattern || "all tests",
      coverage: coverage || false,
      watch: watch || false,
    },
    instructions: `运行此命令来执行测试：\n\n${command}`,
  })
}

async function detectPlatform(cwd: string): Promise<"js" | "kotlin"> {
  const isKotlin = await detectKotlinProject(cwd)
  if (isKotlin) return "kotlin"
  return "js"
}

async function detectKotlinProject(cwd: string): Promise<boolean> {
  const kotlinIndicators = [
    "build.gradle.kts",
    "build.gradle",
    "settings.gradle.kts",
    "settings.gradle",
    "pom.xml",
    "gradlew",
    ".gradle",
  ]

  for (const indicator of kotlinIndicators) {
    if (fs.existsSync(path.join(cwd, indicator))) {
      return true
    }
  }

  const srcDir = path.join(cwd, "src")
  if (fs.existsSync(srcDir)) {
    const entries = fs.readdirSync(srcDir)
    if (entries.some(e => e === "kotlin" || e === "commonMain")) {
      return true
    }
  }

  return false
}

async function detectKotlinBuildTool(cwd: string): Promise<string> {
  if (fs.existsSync(path.join(cwd, "gradlew")) || fs.existsSync(path.join(cwd, "gradlew.bat"))) {
    return "gradle"
  }
  if (fs.existsSync(path.join(cwd, "build.gradle.kts")) || fs.existsSync(path.join(cwd, "build.gradle"))) {
    return "gradle"
  }
  if (fs.existsSync(path.join(cwd, "mvnw")) || fs.existsSync(path.join(cwd, "mvnw.bat"))) {
    return "maven"
  }
  if (fs.existsSync(path.join(cwd, "pom.xml"))) {
    return "maven"
  }
  return "gradle"
}

async function detectKotlinTestFramework(cwd: string): Promise<string> {
  const buildFiles = [
    "build.gradle.kts",
    "build.gradle",
    "pom.xml",
  ]

  for (const buildFile of buildFiles) {
    const filePath = path.join(cwd, buildFile)
    if (fs.existsSync(filePath)) {
      try {
        const content = fs.readFileSync(filePath, "utf-8")

        if (content.includes("kotest") || content.includes("io.kotest")) {
          return "kotest"
        }
        if (content.includes("spek")) {
          return "spek"
        }
        if (content.includes("kotlin-test") || content.includes("kotlin.test")) {
          return "kotlin-test"
        }
        if (content.includes("junit") || content.includes("junit5") || content.includes("junit-jupiter")) {
          return "junit"
        }
        if (content.includes("spek")) {
          return "spek"
        }
      } catch {
        // 忽略读取错误
      }
    }
  }

  return "junit"
}

async function detectPackageManager(cwd: string): Promise<string> {
  const lockFiles: Record<string, string> = {
    "bun.lockb": "bun",
    "pnpm-lock.yaml": "pnpm",
    "yarn.lock": "yarn",
    "package-lock.json": "npm",
  }

  for (const [lockFile, pm] of Object.entries(lockFiles)) {
    if (fs.existsSync(path.join(cwd, lockFile))) {
      return pm
    }
  }

  return "npm"
}

async function detectTestFramework(cwd: string): Promise<string> {
  const packageJsonPath = path.join(cwd, "package.json")

  if (fs.existsSync(packageJsonPath)) {
    try {
      const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, "utf-8"))
      const deps = {
        ...packageJson.dependencies,
        ...packageJson.devDependencies,
      }

      if (deps.vitest) return "vitest"
      if (deps.jest) return "jest"
      if (deps.mocha) return "mocha"
      if (deps.ava) return "ava"
      if (deps.tap) return "tap"
    } catch {
      // 忽略解析错误
    }
  }

  return "unknown"
}
