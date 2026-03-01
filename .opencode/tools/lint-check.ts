/**
 * ECC 自定义工具：代码检查
 *
 * 多语言 linter，自动检测项目的 linting 工具。
 * 支持：ESLint/Biome (JS/TS)、Pylint/Ruff (Python)、golangci-lint (Go)
 */

import { tool } from "@opencode-ai/plugin"
import { z } from "zod"

export default tool({
  name: "lint-check",
  description: "在文件或目录上运行 linter。自动检测 ESLint、Biome、Ruff、Pylint 或 golangci-lint。",
  parameters: z.object({
    target: z.string().optional().describe("要检查的文件或目录（默认：当前目录）"),
    fix: z.boolean().optional().describe("如果支持则自动修复问题（默认：false）"),
    linter: z.string().optional().describe("覆盖 linter：eslint、biome、ruff、pylint、golangci-lint（默认：自动检测）"),
  }),
  execute: async ({ target = ".", fix = false, linter }, { $ }) => {
    // 自动检测 linter
    let detected = linter
    if (!detected) {
      try {
        await $`test -f biome.json || test -f biome.jsonc`
        detected = "biome"
      } catch {
        try {
          await $`test -f .eslintrc.json || test -f .eslintrc.js || test -f .eslintrc.cjs || test -f eslint.config.js || test -f eslint.config.mjs`
          detected = "eslint"
        } catch {
          try {
            await $`test -f pyproject.toml && grep -q "ruff" pyproject.toml`
            detected = "ruff"
          } catch {
            try {
              await $`test -f .golangci.yml || test -f .golangci.yaml`
              detected = "golangci-lint"
            } catch {
              // 根据目标中的文件扩展名回退
              detected = "eslint"
            }
          }
        }
      }
    }

    const fixFlag = fix ? " --fix" : ""
    const commands: Record<string, string> = {
      biome: `npx @biomejs/biome lint${fix ? " --write" : ""} ${target}`,
      eslint: `npx eslint${fixFlag} ${target}`,
      ruff: `ruff check${fixFlag} ${target}`,
      pylint: `pylint ${target}`,
      "golangci-lint": `golangci-lint run${fixFlag} ${target}`,
    }

    const cmd = commands[detected]
    if (!cmd) {
      return { success: false, message: `未知的 linter：${detected}` }
    }

    try {
      const result = await $`${cmd}`.text()
      return { success: true, linter: detected, output: result, issues: 0 }
    } catch (error: unknown) {
      const err = error as { stdout?: string; stderr?: string }
      return {
        success: false,
        linter: detected,
        output: err.stdout || "",
        errors: err.stderr || "",
      }
    }
  },
})
