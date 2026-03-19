# 文档管理系统 - 保持文档干净整洁且最新

## TL;DR

> **Quick Summary**: 为 Kotlin Multiplatform 项目建立完整的自动化文档管理系统，包含两个 GitHub Actions 工作流：(1) PR 审查时自动文档检查，(2) 代码合并到 main 后自动更新文档。通过 Dokka 自动生成 API 文档，AI Agent 自动更新规则文档，ADR 索引自动扫描。
> 
> **Deliverables**:
> - `docs/` 目录结构（架构文档、ADR、开发指南）
> - Dokka API 文档生成配置
> - `.github/workflows/doc-review.yml` - PR 审查工作流
> - `.github/workflows/doc-auto-update.yml` - 合并后自动更新工作流
> - `.opencode/rules/` 自动更新 agent
> - 文档维护规范（`docs/CONTRIBUTING.md`）
> - Bot Token 配置（避免循环触发）
> 
> **Estimated Effort**: Medium-Large (10-14 小时)
> **Parallel Execution**: YES - 4 waves
> **Critical Path**: Task 1 → Task 2 → Task 5 → Task 6 → Task 10 → Task 14 → Task 19

---

## Context

### Original Request
用户希望在 AI 辅助开发过程中保持文档干净整洁且处于最新状态，主要痛点：
- 文档过时：代码变了但文档没更新
- 文档冗余：同一信息在多处重复
- 缺乏规范：不知道什么该写文档、写到什么程度

### Interview Summary
**Key Discussions**:
- **文档范围**: 架构设计、API 文档、开发指南、代码注释、决策记录（ADR）全部需要
- **更新触发机制**: PR 审查时自动运行文档审查 agent
- **AI 文档质量**: 最小可用（保证基本信息准确，不追求完美）
- **存储位置**: `docs/` 目录下 + agent 自动更新 `.opencode/rules/` 下的规则文档

**Research Findings**:
- 项目使用 Kotlin Multiplatform + Compose + Gradle
- 需要配置 Dokka 生成 Kotlin API 文档
- GitHub Actions 作为 CI/CD 平台
- ADR 采用标准版格式（背景、选项、决策、后果）

### Metis Review
**Identified Gaps** (addressed):
- **假设验证**: 需要检查 Dokka 是否已配置、GitHub Actions 是否存在、当前 KDoc 覆盖率
- **Guardrails**: 明确 agent 不能做什么（不创建新文件、不修改手写文档等）
- **范围锁定**: 分阶段实施，Phase 1 基础、Phase 2 自动化、Phase 3 质量（可选）
- **边缘情况**: 处理文档冲突、部分 PR、破坏性 API 变更等

---

## Work Objectives

### Core Objective
建立一个轻量级但自动化的文档管理系统，确保文档与代码同步更新，减少人工维护成本。

### Concrete Deliverables
- `docs/architecture/adr/` 目录和 ADR 模板
- `docs/CONTRIBUTING.md` 文档维护规范
- `build.gradle.kts` Dokka 配置
- `.github/workflows/doc-review.yml` - PR 审查工作流
- `.github/workflows/doc-auto-update.yml` - 合并后自动更新工作流
- `.github/actions/run-doc-agent/` - 自定义复合 Action
- `.github/scripts/` - 自动化脚本（ADR 索引、质量检查）
- `.opencode/agents/doc-updater.md` 文档更新 agent 配置
- `.gitignore` 排除生成的文档
- `.markdownlint.json` Markdown 质量检查配置
- `.github/BOT_SETUP.md` Bot Token 配置说明

### Definition of Done
- [ ] `./gradlew dokkaHtml` 成功生成 API 文档
- [ ] PR 创建时自动触发文档审查
- [ ] 至少创建 1 个 ADR 示例
- [ ] 文档审查 agent 能正确识别代码变更并生成更新建议

### Must Have
- 生成的 API 文档被 gitignore 排除
- ADR 必须有人工审查才能合并
- 文档更新与代码变更分离提交
- Agent 自动更新仅限于 `.opencode/rules/`

### Must NOT Have (Guardrails)
- **Agent 禁止**: 创建新的文档文件（需人工批准）
- **禁止**: 为琐碎变更（拼写错误、无行为变化的重构）更新文档
- **禁止**: 自动修改手写架构文档
- **禁止**: 每次提交都生成文档（仅 PR 时）
- **禁止**: 一个 PR 触发多次文档更新（最多 1 次）

---

## Verification Strategy (MANDATORY)

> **ZERO HUMAN INTERVENTION** — ALL verification is agent-executed. No exceptions.

### Test Decision
- **Infrastructure exists**: YES (Gradle + GitHub Actions)
- **Automated tests**: YES (Tests-after)
- **Framework**: Kotlin test + GitHub Actions workflow validation
- **If TDD**: N/A - 这是基础设施工作，测试在实现后验证

### QA Policy
每个任务必须包含 agent 执行的 QA 场景：
- **Gradle 任务**: 使用 Bash 运行 `./gradlew` 命令验证
- **GitHub Actions**: 使用 Bash 检查 YAML 语法，模拟触发
- **Agent 配置**: 使用 Bash 运行测试场景验证
- **文档结构**: 使用 Bash 验证文件存在性和格式

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately — 基础结构):
├── Task 1: 审计现有文档状态 [quick]
├── Task 2: 创建 docs/ 目录结构 [quick]
├── Task 3: 配置 Dokka 生成 API 文档 [quick]
├── Task 4: 创建 ADR 模板和索引 [quick]
└── Task 5: 编写 docs/CONTRIBUTING.md [quick]

Wave 2 (After Wave 1 — PR 审查工作流):
├── Task 6: 创建 doc-review.yml 工作流 [quick]
├── Task 7: 编写文档审查 agent 配置 [quick]
├── Task 8: 配置 PR 模板包含文档检查清单 [quick]
└── Task 9: 设置 gitignore 排除生成文件 [quick]

Wave 3 (After Wave 2 — 规则文档自动化):
├── Task 10: 编写 rules 自动更新 agent [deep]
├── Task 11: 定义触发模式和更新策略 [quick]
├── Task 12: 创建 agent 测试场景 [quick]
└── Task 13: 集成测试和端到端验证 [unspecified-high]

Wave 4 (After Wave 2 — 合并后自动更新工作流):
├── Task 14: 创建 Bot Token 和 Secrets 配置 [quick]
├── Task 15: 创建 doc-auto-update.yml 工作流 [unspecified-high]
├── Task 16: 编写自定义 run-doc-agent Action [deep]
├── Task 17: 配置 ADR 索引自动扫描脚本 [quick]
└── Task 18: 配置文档质量检查工具 [quick]

Wave FINAL (After ALL tasks — 4 并行审查):
├── Task F1: 工作流验证（oracle）
├── Task F2: 文档质量审查（unspecified-high）
├── Task F3: PR 审查端到端测试（unspecified-high）
└── Task F4: 合并后自动更新测试（deep）
-> 呈现结果 -> 获取用户明确确认

Critical Path: Task 1 → Task 2 → Task 5 → Task 6 → Task 10 → Task 14 → Task 15 → Task 19 → F1-F4 → 用户确认
Parallel Speedup: ~65% faster than sequential
Max Concurrent: 5 (Waves 1, 2 & 4)
```

### Dependency Matrix

- **1-5**: — — 6-9, 10, 14-18
- **6**: 2, 5 — 10, 13, 15
- **9**: 3 — 13, 15
- **10**: 6, 8 — 12, 13, 16
- **13**: 9, 11, 12 — F1-F4
- **14**: — — 15, 16
- **15**: 6, 14 — 18, 19
- **16**: 10, 14 — 18
- **17**: 4 — 15, 18
- **18**: 15, 16, 17 — 19, F1-F4
- **19**: 15, 18 — F1-F4

### Agent Dispatch Summary

- **1**: **5** — T1-T5 → `quick`
- **2**: **4** — T6-T9 → `quick`
- **3**: **4** — T10 → `deep`, T11 → `quick`, T12 → `quick`, T13 → `unspecified-high`
- **4**: **5** — T14 → `quick`, T15 → `unspecified-high`, T16 → `deep`, T17 → `quick`, T18 → `quick`
- **FINAL**: **4** — F1 → `oracle`, F2 → `unspecified-high`, F3 → `unspecified-high`, F4 → `deep`

---

## TODOs

> Implementation + Test = ONE Task. Never separate.
> EVERY task MUST have: Recommended Agent Profile + Parallelization info + QA Scenarios.

- [ ] 1. 审计现有文档状态

  **What to do**:
  - 检查现有 `docs/` 目录（如果存在）的内容和结构
  - 检查 `.opencode/rules/` 下的规则文档
  - 检查 `AGENTS.md` 和 `README.md` 的当前状态
  - 检查 `build.gradle.kts` 中是否有 Dokka 配置
  - 检查 `.github/workflows/` 中是否有现有工作流
  - 抽样检查 KDoc 注释覆盖率（`src/commonMain/kotlin/` 中 `/**` 的数量）
  - 创建审计报告（Markdown 格式）

  **Must NOT do**:
  - 不修改任何现有文件
  - 不删除任何文档
  - 不创建新文件（除了审计报告）

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: 这是信息收集任务，不需要专业技能

  **Parallelization**:
  - **Can Run In Parallel**: NO (这是第一个任务)
  - **Parallel Group**: Sequential (Wave 1 起点)
  - **Blocks**: Tasks 2-5, 6-9, 10-13
  - **Blocked By**: None

  **References**:
  - `AGENTS.md` - 检查现有项目级 AI 开发指南
  - `README.md` - 检查快速入门文档
  - `build.gradle.kts` - 检查 Dokka 配置状态
  - `.github/workflows/` - 检查现有 CI/CD 工作流

  **Acceptance Criteria**:
  - [ ] 审计报告文件创建：`.sisyphus/evidence/task-1-audit.md`
  - [ ] 报告包含：docs/ 状态、rules/ 状态、Dokka 状态、GitHub Actions 状态、KDoc 覆盖率
  - [ ] 识别至少 3 个现有文档文件

  **QA Scenarios**:

  ```
  Scenario: 验证审计报告包含所有必需部分
    Tool: Bash
    Preconditions: 任务完成，审计报告已创建
    Steps:
      1. 读取 .sisyphus/evidence/task-1-audit.md
      2. 检查是否包含 "docs/ 状态" 或类似章节
      3. 检查是否包含 "rules/ 状态" 或类似章节
      4. 检查是否包含 "Dokka 状态" 或类似章节
      5. 检查是否包含 "KDoc 覆盖率" 或类似章节
    Expected Result: 所有 5 个部分都存在
    Failure Indicators: 缺少任何必需部分
    Evidence: .sisyphus/evidence/task-1-audit-complete.md
  ```

  **Commit**: NO (审计报告不提交)

- [ ] 2. 创建 docs/ 目录结构

  **What to do**:
  - 创建 `docs/architecture/` 目录
  - 创建 `docs/architecture/adr/` 目录
  - 创建 `docs/api/` 目录
  - 创建 `docs/guides/` 目录
  - 创建 `docs/README.md` 索引文件（包含目录导航）

  **Must NOT do**:
  - 不在 `docs/` 外创建文件
  - 不创建过深的嵌套（最多 2 层）
  - 不创建空文件（每个文件至少有标题）

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: 简单的目录和文件创建

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖 Task 1 的审计结果)
  - **Parallel Group**: Wave 1
  - **Blocks**: Tasks 6, 10
  - **Blocked By**: Task 1

  **References**:
  - Task 1 审计报告 - 了解现有结构

  **Acceptance Criteria**:
  - [ ] `docs/architecture/` 目录存在
  - [ ] `docs/architecture/adr/` 目录存在
  - [ ] `docs/api/` 目录存在
  - [ ] `docs/guides/` 目录存在
  - [ ] `docs/README.md` 存在且包含导航链接

  **QA Scenarios**:

  ```
  Scenario: 验证目录结构
    Tool: Bash
    Preconditions: 任务完成
    Steps:
      1. 运行 ls -la docs/
      2. 验证输出包含 architecture/, api/, guides/
      3. 运行 ls -la docs/architecture/
      4. 验证输出包含 adr/
      5. 运行 test -f docs/README.md && echo "exists"
      6. 验证输出为 "exists"
    Expected Result: 所有目录和文件都存在
    Failure Indicators: 任何目录或文件缺失
    Evidence: .sisyphus/evidence/task-2-structure-verified.txt
  ```

  **Commit**: YES (groups with 3, 4, 5)
  - Message: `docs: 创建文档目录结构`
  - Files: `docs/**`
  - Pre-commit: `test -d docs/architecture/adr && test -d docs/api && test -d docs/guides`

- [ ] 3. 配置 Dokka 生成 API 文档

  **What to do**:
  - 在 `build.gradle.kts` 中添加 Dokka 插件（如果未存在）
  - 配置 `dokkaHtml` 任务输出到 `build/dokka`
  - 启用 `reportUndocumented` 选项
  - 配置 `skipDeprecated` 为 true
  - 在 `.gitignore` 中添加 `build/dokka/`（在 Task 9 中完成）
  - 验证 `./gradlew dokkaHtml` 成功运行

  **Must NOT do**:
  - 不修改 Dokka 版本（使用项目已有版本或最新稳定版）
  - 不配置复杂的多模块文档（保持简单）
  - 不生成除 HTML 外的其他格式

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: Gradle 配置是标准操作

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 Task 4, 5 并行)
  - **Parallel Group**: Wave 1 (with Tasks 4, 5)
  - **Blocks**: Task 9, 13
  - **Blocked By**: Task 1

  **References**:
  - `build.gradle.kts` - 现有 Gradle 配置
  - 官方文档：https://kotlinlang.org/docs/dokka-introduction.html

  **Acceptance Criteria**:
  - [ ] `build.gradle.kts` 包含 Dokka 插件配置
  - [ ] `./gradlew dokkaHtml` 成功执行（退出码 0）
  - [ ] `build/dokka/` 目录生成且包含 HTML 文件
  - [ ] 配置包含 `reportUndocumented.set(true)`

  **QA Scenarios**:

  ```
  Scenario: 验证 Dokka 生成成功
    Tool: Bash
    Preconditions: build.gradle.kts 已配置 Dokka
    Steps:
      1. 运行 ./gradlew dokkaHtml --console=plain
      2. 验证退出码为 0
      3. 运行 test -d build/dokka && echo "generated"
      4. 验证输出为 "generated"
      5. 运行 find build/dokka -name "*.html" | wc -l
      6. 验证输出 > 0
    Expected Result: Dokka 成功生成 HTML 文档
    Failure Indicators: 非零退出码、目录不存在、没有 HTML 文件
    Evidence: .sisyphus/evidence/task-3-dokka-build.log
  ```

  **Commit**: YES (groups with 2, 4, 5)
  - Message: `build: 配置 Dokka API 文档生成`
  - Files: `build.gradle.kts`
  - Pre-commit: `./gradlew dokkaHtml --console=plain`

- [ ] 4. 创建 ADR 模板和索引

  **What to do**:
  - 创建 `docs/architecture/adr/000-template.md` ADR 模板
  - 模板包含标准四要素：背景、选项、决策、后果
  - 创建 `docs/architecture/adr/README.md` ADR 索引
  - 创建第一个示例 ADR（记录文档管理系统决策）

  **Must NOT do**:
  - 不创建超过 1 个示例 ADR（其他按需创建）
  - 不修改模板格式（保持标准）
  - 不添加额外的元数据字段

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: 模板创建是标准化工作

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 Task 3, 5 并行)
  - **Parallel Group**: Wave 1 (with Tasks 3, 5)
  - **Blocks**: Task 10
  - **Blocked By**: Task 1

  **References**:
  - 标准 ADR 格式：https://github.com/joelparkerhenderson/architecture-decision-record

  **Acceptance Criteria**:
  - [ ] `docs/architecture/adr/000-template.md` 存在
  - [ ] 模板包含：背景、选项、决策、后果四个部分
  - [ ] `docs/architecture/adr/README.md` 存在且包含 ADR 列表
  - [ ] 创建至少 1 个示例 ADR（编号 001）

  **QA Scenarios**:

  ```
  Scenario: 验证 ADR 模板结构
    Tool: Bash
    Preconditions: ADR 模板已创建
    Steps:
      1. 读取 docs/architecture/adr/000-template.md
      2. 检查包含 "背景" 或 "Context" 部分
      3. 检查包含 "选项" 或 "Options" 部分
      4. 检查包含 "决策" 或 "Decision" 部分
      5. 检查包含 "后果" 或 "Consequences" 部分
    Expected Result: 四个必需部分都存在
    Failure Indicators: 缺少任何部分
    Evidence: .sisyphus/evidence/task-4-adr-template-verified.md
  ```

  **Commit**: YES (groups with 2, 3, 5)
  - Message: `docs: 添加 ADR 模板和索引`
  - Files: `docs/architecture/adr/**`
  - Pre-commit: `test -f docs/architecture/adr/000-template.md`

- [ ] 5. 编写 docs/CONTRIBUTING.md

  **What to do**:
  - 创建 `docs/CONTRIBUTING.md` 文档维护规范
  - 包含：什么需要文档、文档标准、更新流程
  - 明确 guardrails（agent 能做什么、不能做什么）
  - 包含文档质量检查清单
  - 链接到 ADR 模板和 API 文档

  **Must NOT do**:
  - 不超过 800 行（保持简洁）
  - 不包含项目特定的代码示例（保持通用）
  - 不重复 AGENTS.md 的内容

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: 文档编写任务

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 Task 3, 4 并行)
  - **Parallel Group**: Wave 1 (with Tasks 3, 4)
  - **Blocks**: Tasks 6, 8, 10
  - **Blocked By**: Task 1

  **References**:
  - Task 1 审计报告
  - Metis 审查结果中的 guardrails

  **Acceptance Criteria**:
  - [ ] `docs/CONTRIBUTING.md` 存在
  - [ ] 包含 "什么需要文档" 章节
  - [ ] 包含 "文档标准" 章节
  - [ ] 包含 "更新流程" 章节
  - [ ] 包含 "Guardrails" 章节（agent 行为限制）
  - [ ] 包含文档质量检查清单

  **QA Scenarios**:

  ```
  Scenario: 验证 CONTRIBUTING.md 包含必需章节
    Tool: Bash
    Preconditions: CONTRIBUTING.md 已创建
    Steps:
      1. 读取 docs/CONTRIBUTING.md
      2. 检查包含 "什么需要文档" 或类似章节标题
      3. 检查包含 "文档标准" 或类似章节标题
      4. 检查包含 "更新流程" 或类似章节标题
      5. 检查包含 "Guardrails" 或 "限制" 或类似章节标题
      6. 检查包含检查清单（`- [ ]` 格式）
    Expected Result: 所有必需章节和检查清单都存在
    Failure Indicators: 缺少任何必需章节或检查清单
    Evidence: .sisyphus/evidence/task-5-contributing-verified.md
  ```

  **Commit**: YES (groups with 2, 3, 4)
  - Message: `docs: 添加文档维护规范`
  - Files: `docs/CONTRIBUTING.md`
  - Pre-commit: `test -f docs/CONTRIBUTING.md`

---

- [ ] 6. 创建 doc-review.yml 工作流

  **What to do**:
  - 创建 `.github/workflows/doc-review.yml`
  - 触发条件：`pull_request` (opened, synchronize)
  - 运行 Dokka 生成
  - 运行文档审查 agent
  - 输出审查报告作为 PR 注释
  - 配置超时（5 分钟）

  **Must NOT do**:
  - 不在 `push` 事件触发（只在 PR 时）
  - 不部署生成的文档（仅审查）
  - 不运行在每次 commit（只在 PR open/update）

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: GitHub Actions 配置是标准化的

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖 Wave 1 完成)
  - **Parallel Group**: Wave 2 (起点)
  - **Blocks**: Tasks 10, 13
  - **Blocked By**: Tasks 2, 5

  **References**:
  - GitHub Actions 文档：https://docs.github.com/en/actions
  - `docs/CONTRIBUTING.md` - 审查标准

  **Acceptance Criteria**:
  - [ ] `.github/workflows/doc-review.yml` 存在
  - [ ] YAML 语法有效（使用 `yamllint` 验证）
  - [ ] 触发条件包含 `pull_request`
  - [ ] 包含 Dokka 生成步骤
  - [ ] 包含文档审查步骤
  - [ ] 配置超时为 5 分钟

  **QA Scenarios**:

  ```
  Scenario: 验证 GitHub Actions YAML 语法
    Tool: Bash
    Preconditions: doc-review.yml 已创建
    Steps:
      1. 运行 yamllint .github/workflows/doc-review.yml
      2. 验证退出码为 0（无错误）
      3. 或者使用 python -c "import yaml; yaml.safe_load(open('.github/workflows/doc-review.yml'))"
      4. 验证无异常抛出
    Expected Result: YAML 语法有效
    Failure Indicators: yamllint 报错或 YAML 解析失败
    Evidence: .sisyphus/evidence/task-6-yaml-valid.txt
  ```

  **Commit**: YES (groups with 7, 8, 9)
  - Message: `ci: 添加文档审查 GitHub Actions 工作流`
  - Files: `.github/workflows/doc-review.yml`
  - Pre-commit: `python3 -c "import yaml; yaml.safe_load(open('.github/workflows/doc-review.yml'))"`

- [ ] 7. 编写文档审查 agent 配置

  **What to do**:
  - 创建 `.opencode/agents/doc-reviewer.md` agent 配置
  - 定义审查范围：API 变更、新功能文档、ADR 需求
  - 定义审查严格度：适中（检查 API 变更和主要功能）
  - 定义输出格式：PR 评论格式
  - 包含审查检查清单（最多 10 项）

  **Must NOT do**:
  - 不检查琐碎变更（拼写、格式）
  - 不自动创建文档文件
  - 不修改现有文档（仅建议）

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: Agent 配置编写

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 Task 6, 8, 9 并行)
  - **Parallel Group**: Wave 2
  - **Blocks**: Task 10, 13
  - **Blocked By**: Task 5

  **References**:
  - `.opencode/agents/` 下现有 agent 配置
  - `docs/CONTRIBUTING.md` - 审查标准

  **Acceptance Criteria**:
  - [ ] `.opencode/agents/doc-reviewer.md` 存在
  - [ ] 包含明确的审查范围定义
  - [ ] 包含审查检查清单（5-10 项）
  - [ ] 包含输出格式示例
  - [ ] 明确说明 "不做什么"

  **QA Scenarios**:

  ```
  Scenario: 验证 agent 配置包含必需元素
    Tool: Bash
    Preconditions: doc-reviewer.md 已创建
    Steps:
      1. 读取 .opencode/agents/doc-reviewer.md
      2. 检查包含 "审查范围" 或类似章节
      3. 检查包含检查清单（`- [ ]` 格式）
      4. 检查包含 "不做什么" 或 "禁止" 或类似章节
      5. 检查包含输出格式示例
    Expected Result: 所有必需元素都存在
    Failure Indicators: 缺少任何必需元素
    Evidence: .sisyphus/evidence/task-7-agent-config-verified.md
  ```

  **Commit**: YES (groups with 6, 8, 9)
  - Message: `agents: 添加文档审查 agent 配置`
  - Files: `.opencode/agents/doc-reviewer.md`
  - Pre-commit: `test -f .opencode/agents/doc-reviewer.md`

- [ ] 8. 配置 PR 模板包含文档检查清单

  **What to do**:
  - 创建 `.github/pull_request_template.md`
  - 包含文档影响评估部分
  - 包含检查清单：是否需要更新 API 文档、是否需要 ADR、是否更新 rules
  - 链接到 `docs/CONTRIBUTING.md`

  **Must NOT do**:
  - 检查清单不超过 10 项（保持简洁）
  - 不包含与文档无关的内容

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: PR 模板是标准 GitHub 功能

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 Task 6, 7, 9 并行)
  - **Parallel Group**: Wave 2
  - **Blocks**: Task 13
  - **Blocked By**: Task 5

  **References**:
  - GitHub PR 模板文档
  - `docs/CONTRIBUTING.md`

  **Acceptance Criteria**:
  - [ ] `.github/pull_request_template.md` 存在
  - [ ] 包含 "文档影响" 或类似章节
  - [ ] 包含文档检查清单（3-5 项）
  - [ ] 链接到 `docs/CONTRIBUTING.md`

  **QA Scenarios**:

  ```
  Scenario: 验证 PR 模板结构
    Tool: Bash
    Preconditions: pull_request_template.md 已创建
    Steps:
      1. 读取 .github/pull_request_template.md
      2. 检查包含 "文档" 相关章节标题
      3. 检查包含检查清单（`- [ ]` 格式）
      4. 检查包含 docs/CONTRIBUTING.md 链接
    Expected Result: 所有必需元素都存在
    Failure Indicators: 缺少文档章节或检查清单
    Evidence: .sisyphus/evidence/task-8-pr-template-verified.md
  ```

  **Commit**: YES (groups with 6, 7, 9)
  - Message: `ci: 添加 PR 模板包含文档检查`
  - Files: `.github/pull_request_template.md`
  - Pre-commit: `test -f .github/pull_request_template.md`

- [ ] 9. 设置 gitignore 排除生成文件

  **What to do**:
  - 读取现有 `.gitignore`
  - 添加 `build/dokka/` 排除规则
  - 添加 `*.log` 排除规则（构建日志）
  - 验证规则生效

  **Must NOT do**:
  - 不删除现有 gitignore 规则
  - 不添加与文档无关的排除规则

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: .gitignore 编辑是简单操作

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 Task 6, 7, 8 并行)
  - **Parallel Group**: Wave 2
  - **Blocks**: Task 13
  - **Blocked By**: Task 3

  **References**:
  - 现有 `.gitignore`
  - Task 3 Dokka 配置

  **Acceptance Criteria**:
  - [ ] `.gitignore` 包含 `build/dokka/`
  - [ ] `.gitignore` 包含 `*.log`
  - [ ] 运行 `git check-ignore build/dokka/` 返回该路径（验证生效）

  **QA Scenarios**:

  ```
  Scenario: 验证 gitignore 规则生效
    Tool: Bash
    Preconditions: .gitignore 已更新
    Steps:
      1. 运行 grep "build/dokka" .gitignore
      2. 验证输出包含 "build/dokka/"
      3. 运行 git check-ignore build/dokka/
      4. 验证输出包含 "build/dokka/"（表示被忽略）
    Expected Result: gitignore 规则存在且生效
    Failure Indicators: grep 无输出或 git check-ignore 无输出
    Evidence: .sisyphus/evidence/task-9-gitignore-verified.txt
  ```

  **Commit**: YES (groups with 6, 7, 8)
  - Message: `git: 排除生成的文档文件`
  - Files: `.gitignore`
  - Pre-commit: `git check-ignore build/dokka/`

---

- [ ] 10. 编写 rules 自动更新 agent

  **What to do**:
  - 创建 `.opencode/agents/doc-updater.md` agent 配置
  - 定义触发条件：新增 agent 调用模式、修改工作流、发现新最佳实践
  - 定义更新范围：仅限 `.opencode/rules/` 目录
  - 定义更新流程：检测变更 → 对比现有 → 生成建议 → 创建 PR
  - 包含冲突处理策略（人工编辑优先）

  **Must NOT do**:
  - 不创建新的 rules 文件（仅更新现有）
  - 不修改 `docs/` 下的人工维护文档
  - 不自动合并 PR（仅创建建议）

  **Recommended Agent Profile**:
  - **Category**: `deep`
  - **Skills**: []
  - **Reason**: 需要理解代码模式和文档更新的复杂逻辑

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖 Wave 2 完成)
  - **Parallel Group**: Wave 3 (起点)
  - **Blocks**: Tasks 12, 13
  - **Blocked By**: Tasks 6, 7

  **References**:
  - `.opencode/rules/` 下现有规则文档
  - `.opencode/agents/doc-reviewer.md` - 审查 agent 配置
  - `docs/CONTRIBUTING.md` - guardrails

  **Acceptance Criteria**:
  - [ ] `.opencode/agents/doc-updater.md` 存在
  - [ ] 包含明确的触发条件（至少 3 种）
  - [ ] 包含更新流程步骤
  - [ ] 包含冲突处理策略
  - [ ] 明确说明 "不做什么"

  **QA Scenarios**:

  ```
  Scenario: 验证 doc-updater agent 配置完整性
    Tool: Bash
    Preconditions: doc-updater.md 已创建
    Steps:
      1. 读取 .opencode/agents/doc-updater.md
      2. 检查包含 "触发条件" 或类似章节
      3. 检查列出至少 3 种触发场景
      4. 检查包含 "更新流程" 或类似章节
      5. 检查包含 "冲突处理" 或类似章节
      6. 检查包含 "禁止" 或 "不做什么" 章节
    Expected Result: 所有必需章节都存在
    Failure Indicators: 缺少任何必需章节
    Evidence: .sisyphus/evidence/task-10-doc-updater-verified.md
  ```

  **Commit**: YES (groups with 11, 12)
  - Message: `agents: 添加 rules 自动更新 agent`
  - Files: `.opencode/agents/doc-updater.md`
  - Pre-commit: `test -f .opencode/agents/doc-updater.md`

- [ ] 11. 定义触发模式和更新策略

  **What to do**:
  - 在 `doc-updater.md` 中明确定义触发模式
  - 模式 1: 新增 agent 调用（检测 `task(` 调用）
  - 模式 2: 修改工作流文件（`.github/workflows/` 变更）
  - 模式 3: 新增最佳实践（代码审查 agent 识别）
  - 定义更新阈值：变更超过 3 处才触发
  - 定义静默规则：注释、格式变更不触发

  **Must NOT do**:
  - 不定义过于敏感的触发（避免频繁触发）
  - 不检测琐碎变更（空格、注释）

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: 策略定义是文档工作

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 Task 10 并行，但需在同一文件中)
  - **Parallel Group**: Wave 3
  - **Blocks**: Task 13
  - **Blocked By**: Task 10

  **References**:
  - `.opencode/agents/doc-updater.md` - 主配置文件

  **Acceptance Criteria**:
  - [ ] 定义至少 3 种触发模式
  - [ ] 定义更新阈值
  - [ ] 定义静默规则
  - [ ] 包含触发模式示例代码

  **QA Scenarios**:

  ```
  Scenario: 验证触发模式定义
    Tool: Bash
    Preconditions: 触发模式已定义
    Steps:
      1. 读取 .opencode/agents/doc-updater.md
      2. 搜索 "触发" 或 "trigger" 相关章节
      3. 验证列出至少 3 种不同的触发场景
      4. 验证包含代码模式示例（如 task( 调用）
    Expected Result: 触发模式清晰定义
    Failure Indicators: 触发模式少于 3 种或无示例
    Evidence: .sisyphus/evidence/task-11-triggers-verified.md
  ```

  **Commit**: YES (groups with 10, 12)
  - Message: `agents: 定义 rules 更新触发模式`
  - Files: `.opencode/agents/doc-updater.md`
  - Pre-commit: `grep -q "触发" .opencode/agents/doc-updater.md`

- [ ] 12. 创建 agent 测试场景

  **What to do**:
  - 创建测试场景验证 doc-updater agent 行为
  - 场景 1: 新增 agent 调用 → 触发更新
  - 场景 2: 修改注释 → 不触发
  - 场景 3: 修改工作流 → 触发更新
  - 场景 4: 冲突检测 → 人工编辑优先
  - 记录预期输出和实际输出对比

  **Must NOT do**:
  - 不修改实际规则文件（仅测试）
  - 不创建复杂的测试框架

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: 测试场景是文档性质的

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 Task 10, 11 并行)
  - **Parallel Group**: Wave 3
  - **Blocks**: Task 13
  - **Blocked By**: Task 10

  **References**:
  - `.opencode/agents/doc-updater.md`
  - `docs/CONTRIBUTING.md`

  **Acceptance Criteria**:
  - [ ] 创建至少 4 个测试场景
  - [ ] 每个场景包含：输入、预期输出、实际输出
  - [ ] 测试场景保存在 `.sisyphus/evidence/task-12-test-scenarios.md`

  **QA Scenarios**:

  ```
  Scenario: 验证测试场景完整性
    Tool: Bash
    Preconditions: 测试场景已创建
    Steps:
      1. 读取 .sisyphus/evidence/task-12-test-scenarios.md
      2. 检查包含至少 4 个不同的测试场景
      3. 验证每个场景包含 "输入" 或 "Input" 描述
      4. 验证每个场景包含 "预期" 或 "Expected" 描述
      5. 验证包含 "冲突检测" 场景
    Expected Result: 测试场景完整且格式正确
    Failure Indicators: 场景少于 4 个或缺少必需元素
    Evidence: .sisyphus/evidence/task-12-scenarios-verified.md
  ```

  **Commit**: YES (groups with 10, 11)
  - Message: `test: 添加 doc-updater 测试场景`
  - Files: `.sisyphus/evidence/task-12-test-scenarios.md`
  - Pre-commit: `test -f .sisyphus/evidence/task-12-test-scenarios.md`

- [ ] 13. 集成测试和端到端验证

  **What to do**:
  - 创建测试 PR 验证整个工作流
  - 步骤 1: 创建测试分支和 PR
  - 步骤 2: 验证 GitHub Actions 触发
  - 步骤 3: 验证 Dokka 生成成功
  - 步骤 4: 验证文档审查 agent 输出
  - 步骤 5: 验证 PR 模板显示正确
  - 步骤 6: 清理测试分支
  - 创建端到端测试报告

  **Must NOT do**:
  - 不合并测试 PR（仅验证）
  - 不修改生产配置（使用测试分支）

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: []
  - **Reason**: 端到端测试需要协调多个组件

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖所有之前任务)
  - **Parallel Group**: Wave 3 (终点)
  - **Blocks**: Final Verification Wave
  - **Blocked By**: Tasks 6, 9, 10, 11, 12

  **References**:
  - 所有之前任务创建的文件
  - GitHub Actions 日志
  - PR 审查输出

  **Acceptance Criteria**:
  - [ ] 成功创建测试 PR
  - [ ] GitHub Actions 工作流触发并成功完成
  - [ ] Dokka 文档生成成功
  - [ ] 文档审查 agent 输出审查报告
  - [ ] PR 模板正确显示
  - [ ] 创建端到端测试报告：`.sisyphus/evidence/task-13-e2e-report.md`

  **QA Scenarios**:

  ```
  Scenario: 验证端到端工作流
    Tool: Bash + GitHub API
    Preconditions: 所有配置已完成
    Steps:
      1. 创建测试分支 test/doc-workflow
      2. 创建微小变更（如 README 添加一行）
      3. 推送并创建测试 PR
      4. 等待 2 分钟让 GitHub Actions 运行
      5. 检查 Actions 状态（应为 success）
      6. 检查 PR 评论（应有文档审查报告）
      7. 清理测试分支
    Expected Result: 整个工作流成功执行
    Failure Indicators: Actions 失败、无审查报告
    Evidence: .sisyphus/evidence/task-13-e2e-screenshot.png, .sisyphus/evidence/task-13-e2e-report.md
  ```

  **Commit**: NO (测试验证不提交)

---

- [ ] 14. 创建 Bot Token 和 Secrets 配置

  **What to do**:
  - 创建 GitHub Bot 用户（或使用现有）
  - 生成 Personal Access Token (PAT)
  - 权限范围：`repo` (full control), `workflow`
  - 在仓库 Settings → Secrets and variables → Actions 中添加：
    - `DOC_BOT_TOKEN` - Bot 的 PAT
    - `OPENAI_API_KEY` - AI Agent 使用（如果需要）
  - 创建配置说明文档 `.github/BOT_SETUP.md`

  **Must NOT do**:
  - 不使用个人 token（使用专用 bot）
  - 不授予不必要的权限
  - 不将 token 提交到代码库

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: GitHub 配置是标准操作

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖 Wave 2 完成，但可独立于 Wave 3)
  - **Parallel Group**: Wave 4 (起点)
  - **Blocks**: Tasks 15, 16
  - **Blocked By**: Task 6

  **References**:
  - GitHub PAT 文档：https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens
  - `docs/CONTRIBUTING.md` - 安全规范

  **Acceptance Criteria**:
  - [ ] Bot 用户创建成功
  - [ ] PAT 生成成功（权限：repo, workflow）
  - [ ] `DOC_BOT_TOKEN` 添加到仓库 Secrets
  - [ ] `.github/BOT_SETUP.md` 配置说明文档创建
  - [ ] 验证 token 可用（使用 API 测试）

  **QA Scenarios**:

  ```
  Scenario: 验证 Bot Token 配置
    Tool: Bash (curl)
    Preconditions: DOC_BOT_TOKEN 已添加到 Secrets
    Steps:
      1. 使用 GitHub CLI 验证 token：gh auth status
      2. 或使用 curl 测试 API：curl -H "Authorization: token $DOC_BOT_TOKEN" https://api.github.com/user
      3. 验证返回包含 bot 用户信息
    Expected Result: Token 有效，能访问 GitHub API
    Failure Indicators: 401 Unauthorized 或权限不足
    Evidence: .sisyphus/evidence/task-14-bot-token-verified.txt
  ```

  **Commit**: YES (groups with 15, 16, 17, 18)
  - Message: `ci: 配置 Bot Token 和 Secrets`
  - Files: `.github/BOT_SETUP.md`
  - Pre-commit: `test -f .github/BOT_SETUP.md`

- [ ] 15. 创建 doc-auto-update.yml 工作流

  **What to do**:
  - 创建 `.github/workflows/doc-auto-update.yml`
  - 触发条件：`push` to `main` (包含路径过滤)
  - 支持 `workflow_dispatch` 手动触发
  - 包含 4 个 Jobs：
    - Job 1: 生成 API 文档 (Dokka)
    - Job 2: 更新 ADR 索引
    - Job 3: 更新 Rules 文档 (AI Agent)
    - Job 4: 质量检查与自动提交
  - 使用 Bot Token 推送（避免循环触发）
  - 提交信息包含 `[skip ci]` 标记

  **Must NOT do**:
  - 不使用 GITHUB_TOKEN 推送（会循环触发）
  - 不在每次 commit 触发（只在 main 分支）
  - 不提交无变更的文档

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: []
  - **Reason**: 复杂的工作流配置，需要协调多个 Jobs

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖 Task 6, 14)
  - **Parallel Group**: Wave 4
  - **Blocks**: Tasks 18, 19
  - **Blocked By**: Tasks 6, 14

  **References**:
  - `.github/workflows/doc-review.yml` - PR 审查工作流参考
  - `docs/CONTRIBUTING.md` - 文档规范
  - Task 14 Bot Token 配置

  **Acceptance Criteria**:
  - [ ] `.github/workflows/doc-auto-update.yml` 存在
  - [ ] YAML 语法有效
  - [ ] 触发条件包含 `push: branches: [main]`
  - [ ] 包含 `workflow_dispatch` 手动触发
  - [ ] 包含 4 个 Jobs (API 生成、ADR 索引、Rules 更新、提交)
  - [ ] 使用 `DOC_BOT_TOKEN` 推送
  - [ ] 提交信息包含 `[skip ci]`

  **QA Scenarios**:

  ```
  Scenario: 验证工作流 YAML 语法和结构
    Tool: Bash
    Preconditions: doc-auto-update.yml 已创建
    Steps:
      1. 运行 python3 -c "import yaml; yaml.safe_load(open('.github/workflows/doc-auto-update.yml'))"
      2. 验证无异常抛出
      3. 使用 yq 检查 jobs 数量：yq '.jobs | keys | length' .github/workflows/doc-auto-update.yml
      4. 验证输出为 4
      5. 检查触发条件：grep -A5 "^on:" .github/workflows/doc-auto-update.yml
      6. 验证包含 push 和 workflow_dispatch
    Expected Result: YAML 有效，包含 4 个 Jobs，触发条件正确
    Failure Indicators: YAML 解析失败、Jobs 数量不对、缺少触发条件
    Evidence: .sisyphus/evidence/task-15-workflow-verified.txt
  ```

  **Commit**: YES (groups with 14, 16, 17, 18)
  - Message: `ci: 添加合并后自动更新工作流`
  - Files: `.github/workflows/doc-auto-update.yml`
  - Pre-commit: `python3 -c "import yaml; yaml.safe_load(open('.github/workflows/doc-auto-update.yml'))"`

- [ ] 16. 编写自定义 run-doc-agent Action

  **What to do**:
  - 创建 `.github/actions/run-doc-agent/action.yml` 复合 Action
  - 输入参数：
    - `agent-name`: agent 名称 (doc-updater)
    - `openai-api-key`: OpenAI API Key
    - `review-scope`: 审查范围
  - 输出：
    - `has-updates`: 是否有更新
    - `suggestion-file`: 建议文件路径
  - 步骤：
    - 安装 Node.js 运行时
    - 运行 AI Agent 审查脚本
    - 解析输出并设置 GitHub Outputs

  **Must NOT do**:
  - 不硬编码 API Key（使用 secrets）
  - 不直接修改文件（仅生成建议）
  - 不暴露敏感信息到日志

  **Recommended Agent Profile**:
  - **Category**: `deep`
  - **Skills**: []
  - **Reason**: 需要编写自定义 GitHub Action，涉及 API 调用和输出处理

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖 Task 10, 14)
  - **Parallel Group**: Wave 4
  - **Blocks**: Task 18
  - **Blocked By**: Tasks 10, 14

  **References**:
  - `.opencode/agents/doc-updater.md` - Agent 配置
  - GitHub Actions 复合 Action 文档

  **Acceptance Criteria**:
  - [ ] `.github/actions/run-doc-agent/action.yml` 存在
  - [ ] 定义输入参数 (agent-name, openai-api-key, review-scope)
  - [ ] 定义输出 (has-updates, suggestion-file)
  - [ ] 包含运行 Agent 的步骤
  - [ ] 正确处理 API Key（不暴露到日志）

  **QA Scenarios**:

  ```
  Scenario: 验证复合 Action 配置
    Tool: Bash
    Preconditions: run-doc-agent action.yml 已创建
    Steps:
      1. 读取 .github/actions/run-doc-agent/action.yml
      2. 检查包含 inputs: 部分
      3. 验证包含 agent-name, openai-api-key, review-scope
      4. 检查包含 outputs: 部分
      5. 验证包含 has-updates, suggestion-file
      6. 检查包含 runs: 部分（步骤定义）
    Expected Result: Action 配置完整，输入输出定义正确
    Failure Indicators: 缺少 inputs/outputs/runs 部分
    Evidence: .sisyphus/evidence/task-16-action-verified.md
  ```

  **Commit**: YES (groups with 14, 15, 17, 18)
  - Message: `ci: 添加 run-doc-agent 复合 Action`
  - Files: `.github/actions/run-doc-agent/**`
  - Pre-commit: `test -f .github/actions/run-doc-agent/action.yml`

- [ ] 17. 配置 ADR 索引自动扫描脚本

  **What to do**:
  - 创建 `.github/scripts/update-adr-index.sh` 脚本
  - 扫描 `docs/architecture/adr/[0-9]*-*.md` 文件
  - 提取元数据：编号、标题、日期、状态
  - 生成 `docs/architecture/adr/README.md` 索引表
  - 支持增量更新（只更新新增 ADR）

  **Must NOT do**:
  - 不修改现有 ADR 文件
  - 不删除手动添加的索引条目
  - 不生成过深的嵌套结构

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: Shell 脚本编写

  **Parallelization**:
  - **Can Run In Parallel**: YES (与 Task 15, 16 并行)
  - **Parallel Group**: Wave 4
  - **Blocks**: Task 15 (Job 2 使用)
  - **Blocked By**: Task 4

  **References**:
  - `docs/architecture/adr/000-template.md` - ADR 模板
  - Task 4 ADR 模板和索引

  **Acceptance Criteria**:
  - [ ] `.github/scripts/update-adr-index.sh` 存在且可执行
  - [ ] 脚本能扫描 ADR 文件
  - [ ] 正确提取元数据（编号、标题、日期、状态）
  - [ ] 生成 Markdown 表格格式的索引
  - [ ] 支持增量更新模式

  **QA Scenarios**:

  ```
  Scenario: 验证 ADR 索引脚本
    Tool: Bash
    Preconditions: update-adr-index.sh 已创建，存在至少 1 个 ADR
    Steps:
      1. 运行 .github/scripts/update-adr-index.sh
      2. 验证退出码为 0
      3. 检查 docs/architecture/adr/README.md 已生成
      4. 验证包含 Markdown 表格（| 编号 | 标题 | 日期 | 状态 |）
      5. 验证包含至少 1 个 ADR 条目
    Expected Result: 脚本成功生成索引文件
    Failure Indicators: 非零退出码、索引文件未生成、格式错误
    Evidence: .sisyphus/evidence/task-17-adr-index-generated.md
  ```

  **Commit**: YES (groups with 14, 15, 16, 18)
  - Message: `ci: 添加 ADR 索引自动扫描脚本`
  - Files: `.github/scripts/update-adr-index.sh`, `docs/architecture/adr/README.md`
  - Pre-commit: `test -x .github/scripts/update-adr-index.sh`

- [ ] 18. 配置文档质量检查工具

  **What to do**:
  - 创建 `.markdownlint.json` 配置文件
  - 配置 markdownlint 规则（标题、列表、链接等）
  - 安装 lychee 链接检查工具
  - 创建质量检查脚本 `.github/scripts/doc-quality-check.sh`
  - 集成到 doc-auto-update.yml 工作流

  **Must NOT do**:
  - 不使用过于严格的规则（避免频繁失败）
  - 不检查外部链接（只检查内部链接）
  - 不阻塞非关键问题

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []
  - **Reason**: 工具配置和脚本编写

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖 Task 15, 16, 17)
  - **Parallel Group**: Wave 4 (终点)
  - **Blocks**: Task 19, F1-F4
  - **Blocked By**: Tasks 15, 16, 17

  **References**:
  - markdownlint 规则文档
  - lychee 链接检查工具文档

  **Acceptance Criteria**:
  - [ ] `.markdownlint.json` 存在且配置合理
  - [ ] `.github/scripts/doc-quality-check.sh` 存在且可执行
  - [ ] 脚本运行 markdownlint 检查
  - [ ] 脚本运行 lychee 链接检查
  - [ ] 质量检查集成到 doc-auto-update.yml

  **QA Scenarios**:

  ```
  Scenario: 验证文档质量检查脚本
    Tool: Bash
    Preconditions: doc-quality-check.sh 已创建，存在 Markdown 文件
    Steps:
      1. 运行 .github/scripts/doc-quality-check.sh docs/
      2. 验证退出码为 0（无严重问题）
      3. 检查输出包含 markdownlint 结果
      4. 检查输出包含 lychee 链接检查结果
    Expected Result: 质量检查成功执行
    Failure Indicators: 非零退出码、缺少检查输出
    Evidence: .sisyphus/evidence/task-18-quality-check-output.txt
  ```

  **Commit**: YES (groups with 14, 15, 16, 17)
  - Message: `ci: 配置文档质量检查工具`
  - Files: `.markdownlint.json`, `.github/scripts/doc-quality-check.sh`
  - Pre-commit: `.github/scripts/doc-quality-check.sh docs/README.md`

- [ ] 19. 合并后自动更新端到端测试

  **What to do**:
  - 创建测试分支 test/auto-update-workflow
  - 创建微小变更并提交
  - 合并到 main 分支（触发工作流）
  - 验证 GitHub Actions 触发并成功完成
  - 验证文档自动更新提交已创建
  - 验证提交信息包含 `[skip ci]`
  - 清理测试分支
  - 创建测试报告

  **Must NOT do**:
  - 不频繁触发测试（避免浪费 CI 资源）
  - 不测试破坏性变更
  - 不忽略失败的测试

  **Recommended Agent Profile**:
  - **Category**: `deep`
  - **Skills**: []
  - **Reason**: 端到端测试需要协调多个步骤和验证

  **Parallelization**:
  - **Can Run In Parallel**: NO (依赖所有 Wave 4 任务)
  - **Parallel Group**: Wave 4 (最终验证前)
  - **Blocks**: F1-F4
  - **Blocked By**: Tasks 15, 18

  **References**:
  - `.github/workflows/doc-auto-update.yml`
  - Task 15 工作流配置
  - GitHub Actions 日志

  **Acceptance Criteria**:
  - [ ] 成功创建测试分支和变更
  - [ ] 合并到 main 后工作流触发
  - [ ] 4 个 Jobs 全部成功完成
  - [ ] 文档更新提交已创建
  - [ ] 提交信息包含 `[skip ci]`
  - [ ] 使用 Bot Token 推送（无循环触发）
  - [ ] 创建测试报告：`.sisyphus/evidence/task-19-e2e-report.md`

  **QA Scenarios**:

  ```
  Scenario: 验证合并后自动更新工作流
    Tool: Bash + GitHub API
    Preconditions: 所有配置已完成
    Steps:
      1. 创建测试分支 test/auto-update
      2. 创建微小变更（如添加注释）
      3. 提交并推送到远程
      4. 使用 GitHub API 创建 PR 并合并到 main
      5. 等待 3-5 分钟让工作流运行
      6. 检查 Actions 状态（应为 success）
      7. 检查新的提交（应为文档更新）
      8. 验证提交信息包含 "[skip ci]"
      9. 清理测试分支
    Expected Result: 工作流成功触发，文档自动更新提交创建
    Failure Indicators: 工作流失败、无更新提交、循环触发
    Evidence: .sisyphus/evidence/task-19-e2e-screenshot.png, .sisyphus/evidence/task-19-e2e-report.md
  ```

  **Commit**: NO (测试验证不提交)

---

## Final Verification Wave (MANDATORY — after ALL implementation tasks)

> 4 review agents run in PARALLEL. ALL must APPROVE. Present consolidated results to user and get explicit "okay" before completing.

- [ ] F1. **工作流验证** — `oracle`
  读取整个工作流配置：doc-review.yml, doc-auto-update.yml, doc-updater.md, doc-reviewer.md。验证：触发条件正确、审查范围清晰、guardrails 明确、无冲突配置。检查 Dokka 配置与 GitHub Actions 集成。验证 PR 模板包含所有必需检查项。验证 Bot Token 配置正确。
  Output: `工作流配置 [N/N] | 触发条件 [PASS/FAIL] | Guardrails [PASS/FAIL] | Bot Token [PASS/FAIL] | VERDICT: APPROVE/REJECT`

- [ ] F2. **文档质量审查** — `unspecified-high`
  检查所有创建的文档文件：docs/ 结构、ADR 模板、CONTRIBUTING.md、agent 配置。验证：格式一致、链接有效、无拼写错误、符合项目规范。运行 markdownlint 检查。
  Output: `文档文件 [N/N] | 格式检查 [PASS/FAIL] | 链接检查 [PASS/FAIL] | VERDICT`

- [ ] F3. **PR 审查端到端测试** — `unspecified-high`
  实际创建测试 PR 并执行 PR 审查工作流：触发 GitHub Actions、验证 Dokka 生成、检查 agent 输出、验证 PR 模板。保存所有证据到 `.sisyphus/evidence/final-qa/`。
  Output: `PR 审查 [PASS/FAIL] | GitHub Actions [PASS/FAIL] | Dokka [PASS/FAIL] | Agent 输出 [PASS/FAIL] | VERDICT`

- [ ] F4. **合并后自动更新测试** — `deep`
  创建测试分支并合并到 main，验证 doc-auto-update.yml 工作流触发：4 个 Jobs 成功完成、文档自动更新提交创建、提交信息包含 [skip ci]、无循环触发。保存所有证据。
  Output: `自动更新 [PASS/FAIL] | Jobs [N/N pass] | 提交创建 [PASS/FAIL] | 无循环 [PASS/FAIL] | VERDICT`

---

## Commit Strategy

- **Wave 1 (Tasks 2-5)**: `docs: 创建文档基础设施` — docs/**, build.gradle.kts
- **Wave 2 (Tasks 6-9)**: `ci: 添加文档审查工作流` — .github/**, .opencode/agents/**, .gitignore
- **Wave 3 (Tasks 10-12)**: `agents: 添加 rules 自动更新` — .opencode/agents/doc-updater.md
- **Wave 4 (Tasks 14-18)**: `ci: 添加合并后自动更新工作流` — .github/workflows/doc-auto-update.yml, .github/actions/**, .github/scripts/**, .markdownlint.json

每个 wave 完成后运行验证：
```bash
# Wave 1
./gradlew dokkaHtml --console=plain

# Wave 2
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/doc-review.yml'))"

# Wave 3
test -f .opencode/agents/doc-updater.md && grep -q "触发" .opencode/agents/doc-updater.md

# Wave 4
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/doc-auto-update.yml'))" && \
test -f .github/actions/run-doc-agent/action.yml && \
.github/scripts/doc-quality-check.sh docs/README.md
```

---

## Success Criteria

### Verification Commands
```bash
# Dokka 生成成功
./gradlew dokkaHtml --console=plain && test -d build/dokka

# PR 审查工作流语法有效
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/doc-review.yml'))"

# 合并后自动更新工作流语法有效
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/doc-auto-update.yml'))"

# 目录结构正确
test -d docs/architecture/adr && test -d docs/api && test -d docs/guides

# ADR 模板存在
test -f docs/architecture/adr/000-template.md && grep -q "背景\|Context" docs/architecture/adr/000-template.md

# gitignore 生效
git check-ignore build/dokka/ | grep -q "build/dokka/"

# 自定义 Action 存在
test -f .github/actions/run-doc-agent/action.yml

# 质量检查脚本可执行
test -x .github/scripts/doc-quality-check.sh && .github/scripts/doc-quality-check.sh docs/README.md
```

### Final Checklist
- [ ] 所有 "Must Have" 现（Dokka 配置、docs/ 结构、GitHub Actions、agent 配置）
- [ ] 所有 "Must NOT Have"  absent（手动验证 guardrails 在文档中）
- [ ] Dokka 生成成功且输出被 gitignore 排除
- [ ] PR 审查工作流语法有效（doc-review.yml）
- [ ] 合并后自动更新工作流语法有效（doc-auto-update.yml）
- [ ] ADR 模板包含标准四要素
- [ ] CONTRIBUTING.md 包含 guardrails 和检查清单
- [ ] doc-reviewer 和 doc-updater agent 配置完整
- [ ] Bot Token 配置正确（DOC_BOT_TOKEN 在 Secrets 中）
- [ ] 自定义 run-doc-agent Action 可用
- [ ] 文档质量检查工具配置完成（markdownlint、lychee）
- [ ] PR 审查端到端测试通过
- [ ] 合并后自动更新端到端测试通过
- [ ] 无循环触发问题（使用 Bot Token 推送）
- [ ] 所有 QA 场景执行并保存证据

### Success Metrics
- **Doc update latency**: 
  - PR 审查：< 5 分钟（PR 创建后）
  - 合并后更新：< 10 分钟（代码合并后）
- **ADR completeness**: 100% of architectural decisions documented (通过 PR 模板和 agent 审查)
- **API doc coverage**: ≥ 60% of public functions have KDoc (通过 Dokka reportUndocumented)
- **False positive rate**: < 10% of agent doc suggestions rejected (通过测试验证)
- **Auto-update success rate**: ≥ 95% of merges trigger successful doc updates (通过工作流历史验证)
- **Loop prevention**: 0 循环触发事件（使用 Bot Token + [skip ci] 标记）
