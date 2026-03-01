/**
 * ECC 自定义工具：Git 摘要
 *
 * 提供全面的 git 状态信息，包括分支信息、状态、近期日志和与基础分支的差异。
 */

// @ts-ignore
import { tool } from "@opencode-ai/plugin"
import { z } from "zod"

export default tool({
  name: "git-summary",
  description: "获取全面的 git 摘要：分支、状态、近期日志和与基础分支的差异。",
  parameters: z.object({
    depth: z.number().optional().describe("显示的近期提交数量（默认：5）"),
    includeDiff: z.boolean().optional().describe("包含与基础分支的差异（默认：true）"),
    baseBranch: z.string().optional().describe("用于比较的基础分支（默认：main）"),
  }),
  execute: async ({ depth = 5, includeDiff = true, baseBranch = "main" }, { $ }) => {
    const results: Record<string, string> = {}

    try {
      results.branch = (await $`git branch --show-current`.text()).trim()
    } catch {
      results.branch = "unknown"
    }

    try {
      results.status = (await $`git status --short`.text()).trim()
    } catch {
      results.status = "无法获取状态"
    }

    try {
      results.log = (await $`git log --oneline -${depth}`.text()).trim()
    } catch {
      results.log = "无法获取日志"
    }

    if (includeDiff) {
      try {
        results.stagedDiff = (await $`git diff --cached --stat`.text()).trim()
      } catch {
        results.stagedDiff = ""
      }

      try {
        results.branchDiff = (await $`git diff ${baseBranch}...HEAD --stat`.text()).trim()
      } catch {
        results.branchDiff = `无法与 ${baseBranch} 比较`
      }
    }

    return results
  },
})
