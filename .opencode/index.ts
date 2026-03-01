/**
 * Everything Claude Code (ECC) OpenCode 插件
 *
 * 此包提供了一个完整的 OpenCode 插件，包括：
 * - 13 个专业 agents（planner、architect、code-reviewer 等）
 * - 31 个命令（/plan、/tdd、/code-review 等）
 * - 插件钩子（自动格式化、TypeScript 检查、console.log 警告、环境变量注入等）
 * - 自定义工具（run-tests、check-coverage、security-audit、format-code、lint-check、git-summary）
 * - 37 个技能（coding-standards、security-review、tdd-workflow 等）
 *
 * 使用方法：
 *
 * 选项 1：通过 npm 安装
 * ```bash
 * npm install ecc-universal
 * ```
 *
 * 然后添加到 opencode.json：
 * ```json
 * {
 *   "plugin": ["ecc-universal"]
 * }
 * ```
 *
 * 选项 2：克隆并直接使用
 * ```bash
 * git clone https://github.com/affaan-m/everything-claude-code
 * cd everything-claude-code
 * opencode
 * ```
 *
 * @packageDocumentation
 */

// 导出主插件
export { ECCHooksPlugin, default } from "./plugins/index.js"

// 导出各个组件以便选择性使用
export * from "./plugins/index.js"

// 版本导出
export const VERSION = "1.6.0"

// 插件元数据
export const metadata = {
  name: "ecc-universal",
  version: VERSION,
  description: "Everything Claude Code OpenCode 插件",
  author: "affaan-m",
  features: {
    agents: 13,
    commands: 31,
    skills: 37,
    hookEvents: [
      "file.edited",
      "tool.execute.before",
      "tool.execute.after",
      "session.created",
      "session.idle",
      "session.deleted",
      "file.watcher.updated",
      "permission.ask",
      "todo.updated",
      "shell.env",
      "experimental.session.compacting",
    ],
    customTools: [
      "run-tests",
      "check-coverage",
      "security-audit",
      "format-code",
      "lint-check",
      "git-summary",
    ],
  },
}
