/**
 * 安全审计工具
 *
 * 自定义 OpenCode 工具，用于对依赖项和代码运行安全审计。
 * 结合了 npm audit、密钥扫描和 OWASP 检查。
 *
 * 注意：此工具用于扫描安全反模式 - 它不会引入这些反模式。
 * 下面的正则表达式模式用于检测用户代码中的潜在问题。
 */

import { tool } from "@opencode-ai/plugin/tool"
import * as path from "path"
import * as fs from "fs"

export default tool({
  description: "运行全面的安全审计，包括依赖项漏洞、密钥扫描和常见安全问题。",
  args: {
    type: tool.schema
      .enum(["all", "dependencies", "secrets", "code"])
      .optional()
      .describe("要运行的审计类型（默认：all）"),
    fix: tool.schema
      .boolean()
      .optional()
      .describe("尝试自动修复依赖项漏洞（默认：false）"),
    severity: tool.schema
      .enum(["low", "moderate", "high", "critical"])
      .optional()
      .describe("要报告的最低严重级别（默认：moderate）"),
  },
  async execute(args, context) {
    const auditType = args.type ?? "all"
    const fix = args.fix ?? false
    const severity = args.severity ?? "moderate"
    const cwd = context.worktree || context.directory

    const results: AuditResults = {
      timestamp: new Date().toISOString(),
      directory: cwd,
      checks: [],
      summary: {
        passed: 0,
        failed: 0,
        warnings: 0,
      },
    }

    // 检查依赖项审计
    if (auditType === "all" || auditType === "dependencies") {
      results.checks.push({
        name: "依赖项漏洞",
        description: "检查依赖项中的已知漏洞",
        command: fix ? "npm audit fix" : "npm audit",
        severityFilter: severity,
        status: "pending",
      })
    }

    // 检查密钥
    if (auditType === "all" || auditType === "secrets") {
      const secretPatterns = await scanForSecrets(cwd)
      if (secretPatterns.length > 0) {
        results.checks.push({
          name: "密钥检测",
          description: "扫描硬编码的密钥和 API 密钥",
          status: "failed",
          findings: secretPatterns,
        })
        results.summary.failed++
      } else {
        results.checks.push({
          name: "密钥检测",
          description: "扫描硬编码的密钥和 API 密钥",
          status: "passed",
        })
        results.summary.passed++
      }
    }

    // 检查常见代码安全问题
    if (auditType === "all" || auditType === "code") {
      const codeIssues = await scanCodeSecurity(cwd)
      if (codeIssues.length > 0) {
        results.checks.push({
          name: "代码安全",
          description: "检查常见的安全反模式",
          status: "warning",
          findings: codeIssues,
        })
        results.summary.warnings++
      } else {
        results.checks.push({
          name: "代码安全",
          description: "检查常见的安全反模式",
          status: "passed",
        })
        results.summary.passed++
      }
    }

    // 生成建议
    results.recommendations = generateRecommendations(results)

    return JSON.stringify(results)
  },
})

interface AuditCheck {
  name: string
  description: string
  command?: string
  severityFilter?: string
  status: "pending" | "passed" | "failed" | "warning"
  findings?: Array<{ file: string; issue: string; line?: number }>
}

interface AuditResults {
  timestamp: string
  directory: string
  checks: AuditCheck[]
  summary: {
    passed: number
    failed: number
    warnings: number
  }
  recommendations?: string[]
}

async function scanForSecrets(
  cwd: string
): Promise<Array<{ file: string; issue: string; line?: number }>> {
  const findings: Array<{ file: string; issue: string; line?: number }> = []

  // 用于检测潜在密钥的模式（安全扫描）
  const secretPatterns = [
    { pattern: /api[_-]?key\s*[:=]\s*['"][^'"]{20,}['"]/gi, name: "API 密钥" },
    { pattern: /password\s*[:=]\s*['"][^'"]+['"]/gi, name: "密码" },
    { pattern: /secret\s*[:=]\s*['"][^'"]{10,}['"]/gi, name: "密钥" },
    { pattern: /Bearer\s+[A-Za-z0-9\-_]+\.[A-Za-z0-9\-_]+/g, name: "JWT 令牌" },
    { pattern: /sk-[a-zA-Z0-9]{32,}/g, name: "OpenAI API 密钥" },
    { pattern: /ghp_[a-zA-Z0-9]{36}/g, name: "GitHub 令牌" },
    { pattern: /aws[_-]?secret[_-]?access[_-]?key/gi, name: "AWS 密钥" },
  ]

  const ignorePatterns = [
    "node_modules",
    ".git",
    "dist",
    "build",
    ".env.example",
    ".env.template",
  ]

  const srcDir = path.join(cwd, "src")
  if (fs.existsSync(srcDir)) {
    await scanDirectory(srcDir, secretPatterns, ignorePatterns, findings)
  }

  // 还要检查根目录配置文件
  const configFiles = ["config.js", "config.ts", "settings.js", "settings.ts"]
  for (const configFile of configFiles) {
    const filePath = path.join(cwd, configFile)
    if (fs.existsSync(filePath)) {
      await scanFile(filePath, secretPatterns, findings)
    }
  }

  return findings
}

async function scanDirectory(
  dir: string,
  patterns: Array<{ pattern: RegExp; name: string }>,
  ignorePatterns: string[],
  findings: Array<{ file: string; issue: string; line?: number }>
): Promise<void> {
  if (!fs.existsSync(dir)) return

  const entries = fs.readdirSync(dir, { withFileTypes: true })

  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name)

    if (ignorePatterns.some((p) => fullPath.includes(p))) continue

    if (entry.isDirectory()) {
      await scanDirectory(fullPath, patterns, ignorePatterns, findings)
    } else if (entry.isFile() && entry.name.match(/\.(ts|tsx|js|jsx|json)$/)) {
      await scanFile(fullPath, patterns, findings)
    }
  }
}

async function scanFile(
  filePath: string,
  patterns: Array<{ pattern: RegExp; name: string }>,
  findings: Array<{ file: string; issue: string; line?: number }>
): Promise<void> {
  try {
    const content = fs.readFileSync(filePath, "utf-8")
    const lines = content.split("\n")

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i]
      for (const { pattern, name } of patterns) {
        // 重置正则表达式状态
        pattern.lastIndex = 0
        if (pattern.test(line)) {
          findings.push({
            file: filePath,
            issue: `发现潜在 ${name}`,
            line: i + 1,
          })
        }
      }
    }
  } catch {
    // 忽略读取错误
  }
}

async function scanCodeSecurity(
  cwd: string
): Promise<Array<{ file: string; issue: string; line?: number }>> {
  const findings: Array<{ file: string; issue: string; line?: number }> = []

  // 用于检测安全反模式的模式（此工具用于扫描问题）
  // 这些是检测模式，而不是使用这些反模式的代码
  const securityPatterns = [
    { pattern: /\beval\s*\(/g, name: "eval() 使用 - 潜在的代码注入" },
    { pattern: /innerHTML\s*=/g, name: "innerHTML 赋值 - 潜在的 XSS" },
    { pattern: /dangerouslySetInnerHTML/g, name: "dangerouslySetInnerHTML - 潜在的 XSS" },
    { pattern: /document\.write/g, name: "document.write - 潜在的 XSS" },
    { pattern: /\$\{.*\}.*sql/gi, name: "潜在的 SQL 注入" },
  ]

  const srcDir = path.join(cwd, "src")
  if (fs.existsSync(srcDir)) {
    await scanDirectory(srcDir, securityPatterns, ["node_modules", ".git", "dist"], findings)
  }

  return findings
}

function generateRecommendations(results: AuditResults): string[] {
  const recommendations: string[] = []

  for (const check of results.checks) {
    if (check.status === "failed" && check.name === "密钥检测") {
      recommendations.push(
        "严重：移除硬编码的密钥，改用环境变量"
      )
      recommendations.push("添加 .env 文件（加入 .gitignore）用于本地开发")
      recommendations.push("生产部署使用密钥管理器")
    }

    if (check.status === "warning" && check.name === "代码安全") {
      recommendations.push(
        "检查标记的代码模式，查找潜在的安全漏洞"
      )
      recommendations.push("考虑使用 DOMPurify 进行 HTML 净化")
      recommendations.push("数据库操作使用参数化查询")
    }

    if (check.status === "pending" && check.name === "依赖项漏洞") {
      recommendations.push("运行 'npm audit' 检查依赖项漏洞")
      recommendations.push("考虑使用 'npm audit fix' 自动修复问题")
    }
  }

  if (recommendations.length === 0) {
    recommendations.push("未发现严重安全问题。继续遵循安全最佳实践。")
  }

  return recommendations
}
