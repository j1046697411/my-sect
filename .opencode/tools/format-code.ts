/**
 * ECC 自定义工具：格式化代码
 *
 * 支持语言识别的代码格式化工具，自动检测项目的格式化工具。
 * 支持：Biome/Prettier (JS/TS)、Black (Python)、gofmt (Go)、rustfmt (Rust)
 */

import { tool } from "@opencode-ai/plugin"
import { z } from "zod"

export default tool({
  name: "format-code",
  description: "使用项目配置的格式化工具格式化文件。自动检测 Biome、Prettier、Black、gofmt 或 rustfmt。",
  parameters: z.object({
    filePath: z.string().describe("要格式化的文件路径"),
    formatter: z.string().optional().describe("覆盖格式化工具：biome、prettier、black、gofmt、rustfmt（默认：自动检测）"),
  }),
  execute: async ({ filePath, formatter }, { $ }) => {
    const ext = filePath.split(".").pop()?.toLowerCase() || ""

    // 根据文件扩展名和配置文件自动检测格式化工具
    let detected = formatter
    if (!detected) {
      if (["ts", "tsx", "js", "jsx", "json", "css", "scss"].includes(ext)) {
        // 先检查 Biome，再检查 Prettier
        try {
          await $`test -f biome.json || test -f biome.jsonc`
          detected = "biome"
        } catch {
          detected = "prettier"
        }
      } else if (["py", "pyi"].includes(ext)) {
        detected = "black"
      } else if (ext === "go") {
        detected = "gofmt"
      } else if (ext === "rs") {
        detected = "rustfmt"
      }
    }

    if (!detected) {
      return { formatted: false, message: `未检测到 .${ext} 文件的格式化工具` }
    }

    const commands: Record<string, string> = {
      biome: `npx @biomejs/biome format --write ${filePath}`,
      prettier: `npx prettier --write ${filePath}`,
      black: `black ${filePath}`,
      gofmt: `gofmt -w ${filePath}`,
      rustfmt: `rustfmt ${filePath}`,
    }

    const cmd = commands[detected]
    if (!cmd) {
      return { formatted: false, message: `未知的格式化工具：${detected}` }
    }

    try {
      const result = await $`${cmd}`.text()
      return { formatted: true, formatter: detected, output: result }
    } catch (error: unknown) {
      const err = error as { stderr?: string }
      return { formatted: false, formatter: detected, error: err.stderr || "格式化失败" }
    }
  },
})
