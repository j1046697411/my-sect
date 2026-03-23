# MVI 基础设施层修复计划

## TL;DR

> **快速总结**: 修复 Issue #16，创建缺失的 MVI 基础架构文件，更新 AGENTS.md 技术债务状态，并关闭相关 issues。
>
> **交付物**:
> - `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/base/MviContract.kt`
> - `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/extensions/FlowMviExt.kt`
> - AGENTS.md 技术债务表格更新
> - 3 个 GitHub Issues 关闭
>
> **估计工作量**: 短 (Short)
> **并行执行**: YES - 3 waves
> **关键路径**: Task 1 → Task 2 → Task 4 → Task 5

---

## Context

### 原始请求
修复 GitHub 上标记为 bug 的 issues：
1. Issue #16: MVI 基础设施层不完整（缺少 MviContract.kt 和 FlowMviExt.kt）
2. Issue #17: desktopMain 依赖重复（代码已正确，AGENTS.md 未同步）
3. Issue #18: Android MainActivity 未连接（代码已正确，AGENTS.md 未同步）

### 研究发现
- **现有代码审查**: Issue #17 和 #18 的代码实际上已经正确实现
- **FlowMVI 框架**: 项目使用 FlowMVI 3.2.0，接口包括 `MVIState`、`MVIIntent`、`MVIAction`
- **现有结构**: `GameErrorHandler.kt` 已在 `business/mvi/` 模块
- **GameContract.kt**: 展示了 State/Intent/Action 如何实现 FlowMVI 接口
- **GameContainer.kt**: 展示了 Container 接口的使用

### Metis 审查
**识别的缺口**（已解决）:
- MviContract.kt 应包含项目级 marker interfaces（扩展 FlowMVI 接口）
- FlowMviExt.kt 应仅包含实际使用的扩展函数
- AGENTS.md 技术债务表格需要同步更新

---

## Work Objectives

### Core Objective
完成 MVI 基础设施层建设，关闭相关 issues。

### Concrete Deliverables
- [ ] `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/base/MviContract.kt`
- [ ] `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/extensions/FlowMviExt.kt`
- [ ] AGENTS.md 技术债务表格更新

### Definition of Done
- [ ] `./gradlew :business:mvi:check` → BUILD SUCCESS
- [ ] 3 个 GitHub issues 状态变为 CLOSED

### Must Have
- MviContract.kt 包含项目级 MVI 契约基础接口
- FlowMviExt.kt 包含实用扩展函数
- AGENTS.md 技术债务表格准确反映当前状态

### Must NOT Have (Guardrails)
- 不修改已正常工作的文件（GameErrorHandler.kt、GameContract.kt、GameContainer.kt、GameScreen.kt）
- 不创建空的或未使用的代码
- 不移动 GameErrorHandler.kt 中的 `toUserMessage()`（错误相关，非 FlowMVI 扩展）

---

## Verification Strategy (MANDATORY)

> **ZERO HUMAN INTERVENTION** — 所有验证由 agent 执行。
> 不允许需要"用户手动测试/确认"的验收标准。

### Test Decision
- **基础设施存在**: YES (kotlin-test)
- **自动化测试**: None (本次任务为代码生成，非 TDD)
- **框架**: N/A

### QA Policy
每个任务包含 agent 执行的 QA 场景。证据保存到 `.sisyphus/evidence/`。

---

## Execution Strategy

### 并行执行 Waves

```
Wave 1 (立即启动 — 文件创建):
├── Task 1: 创建 MviContract.kt
├── Task 2: 创建 FlowMviExt.kt
└── Task 3: Gradle 编译验证

Wave 2 (Wave 1 完成后 — AGENTS.md):
├── Task 4: 更新 AGENTS.md 技术债务表格

Wave 3 (Wave 2 完成后 — GitHub):
└── Task 5: 关闭 GitHub issues

Critical Path: Task 1 → Task 2 → Task 3 → Task 4 → Task 5
Parallel Speedup: ~40% faster than sequential
Max Concurrent: 2 (Tasks 1 & 2)
```

### Dependency Matrix

- **Task 1**: — — Task 3, 4
- **Task 2**: — — Task 3, 4
- **Task 3**: 1, 2 — Task 4, 5
- **Task 4**: 3 — Task 5
- **Task 5**: 4 — —

### Agent Dispatch Summary

- **Wave 1**: **2** — T1 → `quick`, T2 → `quick`
- **Wave 2**: **1** — T4 → `quick`
- **Wave 3**: **1** — T5 → `quick`

---

## TODOs

- [x] 1. 创建 MviContract.kt 基础接口文件

  **What to do**:
  - 在 `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/base/` 目录创建 `MviContract.kt`
  - 文件包含项目级 MVI 契约基础接口：
    - `MviContractState` - 继承 `MVIState` 的标记接口
    - `MviContractIntent` - 继承 `MVIIntent` 的标记接口
    - `MviContractAction` - 继承 `MVIAction` 的标记接口
    - `MviContract<S, I, A>` - 组合 State/Intent/Action 的契约接口
  - 使用与 `GameErrorHandler.kt` 相同的 KDoc 风格
  - 包名：`com.sect.game.mvi.base`

  **Must NOT do**:
  - 不添加业务逻辑（仅为标记接口）
  - 不复制 FlowMVI 已有的功能
  - 不创建测试文件（标记接口无测试意义）

  **Recommended Agent Profile**:
  > - **Category**: `quick`
    - Reason: 简单文件创建，遵循现有模式
  > - **Skills**: []
    - 不需要特殊技能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 2)
  - **Blocks**: Task 3, 4
  - **Blocked By**: None

  **References**:
  - `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/GameErrorHandler.kt:1-20` - KDoc 风格和包结构参考
  - `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/contract/GameContract.kt` - State/Intent/Action 如何实现 FlowMVI 接口

  **Acceptance Criteria**:
  - [ ] 文件存在：`business/mvi/src/commonMain/kotlin/com/sect/game/mvi/base/MviContract.kt`
  - [ ] 包含 `MviContractState`、`MviContractIntent`、`MviContractAction` 接口
  - [ ] `./gradlew :business:mvi:compileKotlinJvm` → BUILD SUCCESS

  **QA Scenarios**:

  ```
  Scenario: MviContract.kt 编译通过
    Tool: Bash
    Preconditions: 文件已创建
    Steps:
      1. Run: ./gradlew :business:mvi:compileKotlinJvm
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-1-compile.log

  Scenario: 文件格式正确
    Tool: Bash
    Preconditions: 文件已创建
    Steps:
      1. Run: grep -c "interface MviContract" business/mvi/src/commonMain/kotlin/com/sect/game/mvi/base/MviContract.kt
    Expected Result: 输出为 4 (3个接口 + 1个泛型接口)
    Evidence: .sisyphus/evidence/task-1-interfaces.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-1-compile.log`
  - [ ] `.sisyphus/evidence/task-1-interfaces.log`

  **Commit**: YES
  - Message: `feat(mvi): 添加 MviContract.kt 基础接口`
  - Files: `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/base/MviContract.kt`

---

- [x] 2. 创建 FlowMviExt.kt 扩展函数文件

  **What to do**:
  - 在 `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/extensions/` 目录创建 `FlowMviExt.kt`
  - 文件包含实用扩展函数：
    - `updateStateOf<T>()` - 类型安全的 state 更新
    - `intents()` - 发送多个 Intent
    - `Container.intents()` - 从 Container 发送多个 Intent
    - `subscribeTo()` - 观察状态变化
    - `DefaultRetryConfig` - 重试配置默认实现
  - 扩展函数应与 GameContainer/GameScreen 的使用模式匹配
  - 使用与 `GameErrorHandler.kt` 相同的 KDoc 风格
  - 包名：`com.sect.game.mvi.extensions`

  **Must NOT do**:
  - 不复制 FlowMVI 库已有的扩展（仅添加项目需要的）
  - 不移动 `GameErrorHandler.kt` 中的 `toUserMessage()`（错误相关，非 FlowMVI）
  - 不创建未使用的代码

  **Recommended Agent Profile**:
  > - **Category**: `quick`
    - Reason: 简单文件创建，遵循现有模式
  > - **Skills**: []
    - 不需要特殊技能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 1)
  - **Blocks**: Task 3, 4
  - **Blocked By**: None

  **References**:
  - `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/GameErrorHandler.kt:1-20` - KDoc 风格和包结构参考
  - `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/container/GameContainer.kt` - 查看 Container 如何使用

  **Acceptance Criteria**:
  - [ ] 文件存在：`business/mvi/src/commonMain/kotlin/com/sect/game/mvi/extensions/FlowMviExt.kt`
  - [ ] 包含至少 3 个扩展函数
  - [ ] `./gradlew :business:mvi:compileKotlinJvm` → BUILD SUCCESS

  **QA Scenarios**:

  ```
  Scenario: FlowMviExt.kt 编译通过
    Tool: Bash
    Preconditions: 文件已创建
    Steps:
      1. Run: ./gradlew :business:mvi:compileKotlinJvm
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-2-compile.log

  Scenario: 扩展函数数量正确
    Tool: Bash
    Preconditions: 文件已创建
    Steps:
      1. Run: grep -c "^public " business/mvi/src/commonMain/kotlin/com/sect/game/mvi/extensions/FlowMviExt.kt
    Expected Result: >= 3
    Evidence: .sisyphus/evidence/task-2-functions.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-2-compile.log`
  - [ ] `.sisyphus/evidence/task-2-functions.log`

  **Commit**: YES
  - Message: `feat(mvi): 添加 FlowMviExt.kt 扩展函数`
  - Files: `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/extensions/FlowMviExt.kt`

---

- [x] 3. Gradle 编译验证

  **What to do**:
  - 运行 `./gradlew :business:mvi:check` 验证 MviContract.kt 和 FlowMviExt.kt 编译通过
  - 验证无 detekt 或 ktlint 违规
  - 保存构建输出到证据目录

  **Must NOT do**:
  - 不修改任何源文件
  - 不跳过检查

  **Recommended Agent Profile**:
  > - **Category**: `quick`
    - Reason: 验证命令执行
  > - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocks**: Task 4, 5
  - **Blocked By**: Task 1, 2

  **References**:
  - `business/mvi/build.gradle.kts` - 查看 mvi 模块的检查配置

  **Acceptance Criteria**:
  - [ ] `./gradlew :business:mvi:check` → BUILD SUCCESS
  - [ ] 无 detekt 违规
  - [ ] 无 ktlint 违规

  **QA Scenarios**:

  ```
  Scenario: Gradle check 通过
    Tool: Bash
    Preconditions: Task 1 和 Task 2 完成
    Steps:
      1. Run: ./gradlew :business:mvi:check 2>&1 | tee .sisyphus/evidence/task-3-check.log
    Expected Result: BUILD SUCCESS
    Failure Indicators: BUILD FAILED, detekt violations, ktlint violations
    Evidence: .sisyphus/evidence/task-3-check.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-3-check.log`

  **Commit**: NO

---

- [x] 4. 更新 AGENTS.md 技术债务表格

  **What to do**:
  - 更新根目录 `AGENTS.md` 技术债务表格：
    - "MVI 基础设施层" 行从 ⚠️ 改为 ✅
    - 或者拆分为多行：
      - "MviContract.kt" → ✅
      - "FlowMviExt.kt" → ✅
      - 删除原来的 "MVI 基础设施层" 行
  - 同步 Issue #17 和 #18 的描述（代码已正确，仅表格需更新）

  **Must NOT do**:
  - 不修改其他 AGENTS.md 内容
  - 不修改构建命令或项目结构描述

  **Recommended Agent Profile**:
  > - **Category**: `quick`
    - Reason: 简单文本编辑
  > - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocks**: Task 5
  - **Blocked By**: Task 3

  **References**:
  - `AGENTS.md:253-262` - 技术债务表格位置

  **Acceptance Criteria**:
  - [ ] "MVI 基础设施层" 行标记为 ✅
  - [ ] Issue #17 和 #18 相关描述已同步

  **QA Scenarios**:

  ```
  Scenario: AGENTS.md 技术债务表格已更新
    Tool: Bash
    Preconditions: 文件已编辑
    Steps:
      1. Run: grep -A1 "MVI 基础设施层" AGENTS.md
    Expected Result: 包含 ✅ 标记
    Evidence: .sisyphus/evidence/task-4-grep.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-4-grep.log`

  **Commit**: YES
  - Message: `docs: 更新 AGENTS.md 技术债务表`
  - Files: `AGENTS.md`

---

- [x] 5. 关闭 GitHub Issues

  **What to do**:
  - 使用 `gh issue close` 关闭以下 issues：
    - `#16` - MVI 基础设施层不完整（已创建 MviContract.kt 和 FlowMviExt.kt）
    - `#17` - desktopMain 依赖重复（代码已正确，AGENTS.md 已更新）
    - `#18` - Android MainActivity 未连接（代码已正确，AGENTS.md 已更新）
  - 每个 issue 添加关闭原因说明

  **Must NOT do**:
  - 不删除或修改 issue 内容
  - 不关闭其他 issues

  **Recommended Agent Profile**:
  > - **Category**: `quick`
    - Reason: GitHub CLI 命令
  > - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Blocks**: None
  - **Blocked By**: Task 4

  **References**:
  - `gh issue close --help` - 查看关闭命令选项

  **Acceptance Criteria**:
  - [ ] `gh issue list --label bug --state open` 不包含 #16, #17, #18
  - [ ] `gh issue list --label bug --state closed` 包含 #16, #17, #18

  **QA Scenarios**:

  ```
  Scenario: Issue #16 已关闭
    Tool: Bash
    Preconditions: Task 3 完成
    Steps:
      1. Run: gh issue view 16 --json state --jq '.state'
    Expected Result: "CLOSED"
    Evidence: .sisyphus/evidence/task-5-16-closed.json

  Scenario: Issue #17 已关闭
    Tool: Bash
    Steps:
      1. Run: gh issue view 17 --json state --jq '.state'
    Expected Result: "CLOSED"
    Evidence: .sisyphus/evidence/task-5-17-closed.json

  Scenario: Issue #18 已关闭
    Tool: Bash
    Steps:
      1. Run: gh issue view 18 --json state --jq '.state'
    Expected Result: "CLOSED"
    Evidence: .sisyphus/evidence/task-5-18-closed.json
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-5-16-closed.json`
  - [ ] `.sisyphus/evidence/task-5-17-closed.json`
  - [ ] `.sisyphus/evidence/task-5-18-closed.json`

  **Commit**: YES
  - Message: `fix: 关闭 Issue #16 #17 #18`
  - Files: N/A (仅 GitHub 操作)

---

## Final Verification Wave (MANDATORY)

- [x] F1. **Plan Compliance Audit** — `oracle`
  读取计划 end-to-end。对于每个 "Must Have"：验证实现存在。对于每个 "Must NOT Have"：搜索禁止模式。检查证据文件存在。
  Output: `Must Have [N/N] | Must NOT Have [N/N] | VERDICT: APPROVE/REJECT`

- [x] F2. **Build Verification** — `unspecified-high`
  运行 `./gradlew :business:mvi:check`。验证无编译错误、detekt 违规、ktlint 错误。
  Output: `Build [PASS/FAIL] | Detekt [PASS/FAIL] | ktlint [PASS/FAIL] | VERDICT`

- [x] F3. **GitHub Issues Status** — `unspecified-high`
  验证 `#16`、`#17`、`#18` 状态为 CLOSED。
  Output: `Issues [#16, #17, #18] → CLOSED | VERDICT`

---

## Commit Strategy

- **1**: `feat(mvi): 添加 MviContract.kt 基础接口` — base/MviContract.kt
- **2**: `feat(mvi): 添加 FlowMviExt.kt 扩展函数` — extensions/FlowMviExt.kt
- **3**: `docs: 更新 AGENTS.md 技术债务表` — AGENTS.md
- **4**: `fix: 关闭 Issue #16 #17 #18` — (gh issue close)

---

## Success Criteria

### Verification Commands
```bash
./gradlew :business:mvi:check  # Expected: BUILD SUCCESS
gh issue list --label bug --state open  # Expected: 0 open issues
```

### Final Checklist
- [ ] MviContract.kt 存在且编译通过
- [ ] FlowMviExt.kt 存在且编译通过
- [ ] AGENTS.md 技术债务表格反映正确状态
- [ ] GitHub issues #16, #17, #18 已关闭
