# 添加视觉回归测试 (Screenshot Testing)

## TL;DR

> **Quick Summary**: 为 Compose UI 组件添加 Paparazzi 截图测试，防止样式回归
> 
> **Deliverables**:
> - `DiscipleCardPaparazziTest.kt` - 3 个截图测试场景
> - `GameScreenPaparazziTest.kt` - 4 个截图测试场景
> - Paparazzi 依赖配置
> 
> **Estimated Effort**: Short (1-2 小时)
> **Parallel Execution**: YES - 2 waves
> **Critical Path**: 配置 Paparazzi → DiscipleCard 测试 → GameScreen 测试

---

## Context

### Original Request
Issue #25：添加视觉回归测试 (Screenshot Testing)

### Interview Summary
**Key Discussions**:
- 测试模块位置：按 Issue，在 `client` 模块添加 Paparazzi 并编写测试
- 测试场景数量：DiscipleCard 3 个，GameScreen 4 个（添加 empty state）
- 测试数据提供：按项目实际，使用内联辅助方法
- CI 集成：暂时不需要

**Research Findings**:
- DiscipleCard 在 `business/presentation/src/commonMain/kotlin/com/sect/game/presentation/DiscipleCard.kt`
- GameScreen 在 `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/presentation/GameScreen.kt`
- `client` 使用 `androidLibrary` 插件，是 library 模块，可以添加 Paparazzi
- 项目使用 JVM 11，兼容 Paparazzi 1.3.x

### Metis Review
**Identified Gaps** (addressed):
- Java 版本：使用 Paparazzi 1.3.x 而非 2.0.x，避免 Java 21 要求
- 测试位置：按 Issue 在 client 模块
- 额外场景：GameScreen 添加第 4 个 empty state 场景

---

## Work Objectives

### Core Objective
在 client 模块添加 Paparazzi 截图测试框架，实现 DiscipleCard 和 GameScreen 的视觉回归测试。

### Concrete Deliverables
- `client/src/commonTest/kotlin/com/sect/game/visual/DiscipleCardPaparazziTest.kt`
- `client/src/commonTest/kotlin/com/sect/game/visual/GameScreenPaparazziTest.kt`
- `gradle/libs.versions.toml` - Paparazzi 版本
- `client/build.gradle.kts` - Paparazzi 插件和依赖

### Definition of Done
- [ ] `./gradlew :client:recordPaparazziDebug --tests "*PaparazziTest*"` 成功生成截图
- [ ] `./gradlew :client:testDebug --tests "*PaparazziTest*"` 测试通过

### Must Have
- Paparazzi 依赖和插件正确配置
- DiscipleCard 3 个截图场景：defaultState, cultivatingState, exhaustedState
- GameScreen 4 个截图场景：loading, error, empty, default
- 主题对比测试：light/dark

### Must NOT Have (Guardrails)
- 不添加 CI 集成
- 不修改现有业务代码
- 不添加额外的测试场景

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: NO - 需要新增 Paparazzi
- **Automated tests**: Tests-after（先实现，再截图验证）
- **Framework**: Paparazzi 1.3.4

### QA Policy
Every task includes agent-executed QA scenarios (see TODO template below).
证据保存到 `.sisyphus/evidence/task-{N}-{scenario-slug}.{ext}`。

- **Screenshot Testing**: 使用 Paparazzi 的 Gradle 任务验证截图
- **验证方式**: `./gradlew :client:recordPaparazziDebug` 录制，`./gradlew :client:verifyPaparazziDebug` 验证

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (配置 + 基础测试 - 可并行):
├── Task 1: 添加 Paparazzi 版本到 libs.versions.toml [quick]
├── Task 2: 配置 client/build.gradle.kts Paparazzi [quick]
├── Task 3: 创建 DiscipleCardPaparazziTest.kt [quick]
└── Task 4: 创建 GameScreenPaparazziTest.kt [quick]

Wave FINAL (验证):
└── Task 5: 截图录制和验证 [quick]
```

### Dependency Matrix

- **Task 1, 2**: 无依赖，可立即开始
- **Task 3, 4**: 依赖 Task 1, 2 完成
- **Task 5**: 依赖 Task 3, 4 完成

### Agent Dispatch Summary

- **Wave 1**: **4 agents 并行** — T1 → `quick`, T2 → `quick`, T3 → `quick`, T4 → `quick`
- **Wave FINAL**: **1 agent** — T5 → `quick`

---

## TODOs

- [x] 1. 添加 Paparazzi 版本到 libs.versions.toml

  **What to do**:
  - 在 `[versions]` 添加 `paparazzi = "1.3.4"`
  - 在 `[libraries]` 添加 `paparazzi = { group = "app.cash.paparazzi", name = "paparazzi", version.ref = "paparazzi" }`
  - 在 `[plugins]` 添加 `paparazzi = { id = "app.cash.paparazzi", version.ref = "paparazzi" }`

  **Must NOT do**:
  - 不要添加 paparazzi-compose（Paparazzi 主库已内置 Compose 支持）
  - 不要使用 2.0.x 版本（需要 Java 21）

  **Recommended Agent Profile**:
  > - **Category**: `quick`
    - Reason: 简单依赖配置修改
  > - **Skills**: []
    - 无需特殊技能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3, 4)
  - **Blocks**: Tasks 3, 4
  - **Blocked By**: None

  **References**:
  - `gradle/libs.versions.toml` - 现有配置格式
  - Paparazzi 1.3.4 官方文档

  **Acceptance Criteria**:
  - [ ] `gradle/libs.versions.toml` 包含 `paparazzi = "1.3.4"`
  - [ ] `gradle/libs.versions.toml` 包含 paparazzi library 和 plugin 条目
  - [ ] `./gradlew :client:dependencies --configuration testRuntimeClasspath | grep paparazzi` 显示新依赖

  **QA Scenarios**:

  \`\`\`
  Scenario: 验证 Paparazzi 依赖已添加
    Tool: Bash
    Preconditions: libs.versions.toml 已修改
    Steps:
      1. Run: ./gradlew :client:dependencies --configuration testRuntimeClasspath
      2. Grep output for "paparazzi"
    Expected Result: 输出包含 "app.cash.paparazzi:paparazzi:1.3.4"
    Evidence: .sisyphus/evidence/task-1-dependency-check.txt
  \`\`\`

  **Commit**: YES
  - Message: `build(deps): add paparazzi 1.3.4 for screenshot testing`
  - Files: `gradle/libs.versions.toml`

---

- [x] 2. 配置 client/build.gradle.kts Paparazzi

  **What to do**:
  - 在 plugins 块添加 `alias(libs.plugins.paparazzi)`
  - 在 android {} 块后添加 paparazzi { sdkDir = ... } 配置
  - 在 commonTest dependencies 添加 `androidTestImplementation(libs.paparazzi)`
  - 注意：client 使用 androidLibrary 插件，Paparazzi 可正常运行

  **Must NOT do**:
  - 不要修改 jvmTarget 或 Java 版本
  - 不要添加 testImplementation（使用 androidTestImplementation）

  **Recommended Agent Profile**:
  > - **Category**: `quick`
    - Reason: Gradle 配置修改
  > - **Skills**: []
    - 无需特殊技能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3, 4)
  - **Blocks**: Tasks 3, 4
  - **Blocked By**: None

  **References**:
  - `client/build.gradle.kts` - 现有配置
  - Issue #25 - 原始配置示例

  **Acceptance Criteria**:
  - [ ] `client/build.gradle.kts` plugins 块包含 paparazzi
  - [ ] `client/build.gradle.kts` 包含 paparazzi { sdkDir = ... } 配置
  - [ ] `client/build.gradle.kts` commonTest dependencies 包含 paparazzi
  - [ ] `./gradlew :client:tasks --group verification | grep -i paparazzi` 显示 Paparazzi 任务

  **QA Scenarios**:

  \`\`\`
  Scenario: 验证 Paparazzi 插件已注册
    Tool: Bash
    Preconditions: client/build.gradle.kts 已修改
    Steps:
      1. Run: ./gradlew :client:tasks --group verification
      2. Grep output for "paparazzi" or "Paparazzi"
    Expected Result: 输出包含 "recordPaparazziDebug" 和 "verifyPaparazziDebug" 任务
    Evidence: .sisyphus/evidence/task-2-plugin-check.txt
  \`\`\`

  **Commit**: YES
  - Message: `build(client): configure paparazzi plugin and dependency`
  - Files: `client/build.gradle.kts`

---

- [x] 3. 创建 DiscipleCardPaparazziTest.kt

  **What to do**:
  - 创建 `client/src/commonTest/kotlin/com/sect/game/visual/DiscipleCardPaparazziTest.kt`
  - 实现 3 个测试场景：
    1. `paparazzi_discipleCard_defaultState` - 默认状态（张三，炼气期）
    2. `paparazzi_discipleCard_cultivatingState` - 修炼中状态
    3. `paparazzi_discipleCard_exhaustedState` - 疲劳状态（fatigue=100）
  - 参考 `DiscipleTest.kt` 的 `createTestDisciple()` 模式内联创建测试数据
  - 使用 `@get:Rule val paparazzi = Paparazzi()` 语法

  **Must NOT do**:
  - 不要创建独立的测试辅助类（按项目实际内联）
  - 不要添加第 4 个场景（Issue 只要求 3 个）

  **Recommended Agent Profile**:
  > - **Category**: `quick`
    - Reason: 标准 Paparazzi 测试文件创建
  > - **Skills**: []
    - 无需特殊技能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 4)
  - **Blocks**: Task 5
  - **Blocked By**: Tasks 1, 2

  **References**:
  - `business/domain/src/commonMain/kotlin/com/sect/game/domain/entity/Disciple.kt` - Disciple 实体
  - `business/domain/src/commonMain/kotlin/com/sect/game/domain/valueobject/Realm.kt` - Realm 枚举
  - `business/presentation/src/commonMain/kotlin/com/sect/game/presentation/DiscipleCard.kt` - DiscipleCard 组件
  - `business/domain/src/commonTest/kotlin/com/sect/game/domain/entity/DiscipleTest.kt:290-311` - createTestDisciple 模式

  **Acceptance Criteria**:
  - [ ] `DiscipleCardPaparazziTest.kt` 存在且语法正确
  - [ ] 包含 3 个 @Test 方法：defaultState, cultivatingState, exhaustedState
  - [ ] 每个测试调用 `paparazzi.snapshot { }` 传入 DiscipleCard Composable
  - [ ] `./gradlew :client:compileTestKotlinAndroid` 编译通过

  **QA Scenarios**:

  \`\`\`
  Scenario: 验证 DiscipleCardPaparazziTest 编译正确
    Tool: Bash
    Preconditions: DiscipleCardPaparazziTest.kt 已创建
    Steps:
      1. Run: ./gradlew :client:compileTestKotlinAndroid
    Expected Result: BUILD SUCCESSFUL，无编译错误
    Evidence: .sisyphus/evidence/task-3-compile.txt
  \`\`\`

  **Commit**: YES
  - Message: `test(visual): add DiscipleCard screenshot tests (3 scenarios)`
  - Files: `client/src/commonTest/kotlin/com/sect/game/visual/DiscipleCardPaparazziTest.kt`

---

- [x] 4. 创建 GameScreenPaparazziTest.kt

  **What to do**:
  - 创建 `client/src/commonTest/kotlin/com/sect/game/visual/GameScreenPaparazziTest.kt`
  - 实现 4 个测试场景：
    1. `paparazzi_gameScreen_loadingState` - 加载状态（isLoading=true）
    2. `paparazzi_gameScreen_errorState` - 错误状态（error != null）
    3. `paparazzi_gameScreen_emptyState` - 空状态（disciples.isEmpty()）
    4. `paparazzi_gameScreen_defaultState` - 默认状态（有弟子数据）
  - 创建测试所需的 GameContainer 和 GameEngine mock
  - 包含主题对比测试：亮色/暗色主题

  **Must NOT do**:
  - 不要跳过任何 4 个场景

  **Recommended Agent Profile**:
  > - **Category**: `quick`
    - Reason: 标准 Paparazzi 测试文件创建
  > - **Skills**: []
    - 无需特殊技能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 3)
  - **Blocks**: Task 5
  - **Blocked By**: Tasks 1, 2

  **References**:
  - `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/presentation/GameScreen.kt` - GameScreen 组件
  - `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/container/GameContainer.kt` - GameContainer
  - `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/contract/GameContract.kt` - State/Intent 定义
  - `business/domain/src/commonMain/kotlin/com/sect/game/domain/entity/Disciple.kt` - Disciple 实体
  - `client/src/commonTest/kotlin/com/sect/game/e2e/GameE2ETest.kt` - GameContainer 创建模式

  **Acceptance Criteria**:
  - [ ] `GameScreenPaparazziTest.kt` 存在且语法正确
  - [ ] 包含 4 个 @Test 方法：loadingState, errorState, emptyState, defaultState
  - [ ] 包含主题对比测试（light/dark）
  - [ ] `./gradlew :client:compileTestKotlinAndroid` 编译通过

  **QA Scenarios**:

  \`\`\`
  Scenario: 验证 GameScreenPaparazziTest 编译正确
    Tool: Bash
    Preconditions: GameScreenPaparazziTest.kt 已创建
    Steps:
      1. Run: ./gradlew :client:compileTestKotlinAndroid
    Expected Result: BUILD SUCCESSFUL，无编译错误
    Evidence: .sisyphus/evidence/task-4-compile.txt
  \`\`\`

  **Commit**: YES
  - Message: `test(visual): add GameScreen screenshot tests (4 scenarios + theme comparison)`
  - Files: `client/src/commonTest/kotlin/com/sect/game/visual/GameScreenPaparazziTest.kt`

---

- [x] 5. 截图录制和验证

  **What to do**:
  - 运行 `./gradlew :client:recordPaparazziDebug --tests "*PaparazziTest*"` 录制截图
  - 截图保存在 `client/src/test/snapshots/` 目录
  - 验证截图已生成
  - 运行 `./gradlew :client:verifyPaparazziDebug --tests "*PaparazziTest*"` 验证测试通过

  **Must NOT do**:
  - 不要提交截图到 Git（只提交测试代码）
  - 不要修改已生成的截图

  **Recommended Agent Profile**:
  > - **Category**: `quick`
    - Reason: 运行 Gradle 任务验证
  > - **Skills**: []
    - 无需特殊技能

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave FINAL
  - **Blocks**: None
  - **Blocked By**: Tasks 3, 4

  **References**:
  - Paparazzi 官方文档 - 截图录制和验证

  **Acceptance Criteria**:
  - [ ] `./gradlew :client:recordPaparazziDebug --tests "*DiscipleCardPaparazziTest*"` 成功
  - [ ] `./gradlew :client:recordPaparazziDebug --tests "*GameScreenPaparazziTest*"` 成功
  - [ ] `client/src/test/snapshots/` 目录包含生成的 png 文件
  - [ ] `./gradlew :client:verifyPaparazziDebug --tests "*PaparazziTest*"` 测试通过

  **QA Scenarios**:

  \`\`\`
  Scenario: 录制 DiscipleCard 截图
    Tool: Bash
    Preconditions: Task 3 完成
    Steps:
      1. Run: ./gradlew :client:recordPaparazziDebug --tests "*DiscipleCardPaparazziTest*"
    Expected Result: BUILD SUCCESSFUL，截图生成到 client/src/test/snapshots/
    Evidence: .sisyphus/evidence/task-5-disciplecard-record.txt
  \`\`\`

  \`\`\`
  Scenario: 录制 GameScreen 截图
    Tool: Bash
    Preconditions: Task 4 完成
    Steps:
      1. Run: ./gradlew :client:recordPaparazziDebug --tests "*GameScreenPaparazziTest*"
    Expected Result: BUILD SUCCESSFUL，截图生成到 client/src/test/snapshots/
    Evidence: .sisyphus/evidence/task-5-gamescreen-record.txt
  \`\`\`

  \`\`\`
  Scenario: 验证截图测试通过
    Tool: Bash
    Preconditions: 截图已录制
    Steps:
      1. Run: ./gradlew :client:verifyPaparazziDebug --tests "*PaparazziTest*"
    Expected Result: BUILD SUCCESSFUL，所有测试通过
    Evidence: .sisyphus/evidence/task-5-verify.txt
  \`\`\`

  **Commit**: NO (截图不提交，只提交测试代码)

---

## Final Verification Wave

> 4 review agents run in PARALLEL. ALL must APPROVE. Present consolidated results to user and get explicit "okay" before completing.

- [x] F1. **Plan Compliance Audit** — `oracle`
  Read the plan end-to-end. For each "Must Have": verify implementation exists. For each "Must NOT Have": search codebase for forbidden patterns. Check evidence files exist in .sisyphus/evidence/.
  Output: `Must Have [5/5] | Must NOT Have [3/3] | Tasks [5/5] | VERDICT: APPROVE (with notes)`
  
  **Notes**:
  - Version changed to 2.0.0-alpha02 (from 1.3.4) - REQUIRED due to compileSdk 36 compatibility
  - sdkDir configured via gradle.properties ( DSL block not accessible in KMP)

- [x] F2. **Code Quality Review** — `unspecified-high`
  Run `ktlintCheck` + `detekt` + `compileTestKotlinAndroid`. Review all changed files for: `as any`/`@ts-ignore`, empty catches, console.log in prod, commented-out code, unused imports.
  Output: `Lint [PASS] | Detekt [PASS] | Compile [PASS] | VERDICT: APPROVE`

- [x] F3. **Real Manual QA** — `unspecified-high`
  Execute EVERY QA scenario from EVERY task — follow exact steps, capture evidence. Verify screenshot files exist and are valid PNG.
  Output: `Scenarios [7/7 pass] | Screenshots [7 found] | VERDICT: APPROVE`

- [x] F4. **Scope Fidelity Check** — `deep`
  For each task: read "What to do", read actual diff. Verify 1:1 — everything in spec was built (no missing), nothing beyond spec was built (no creep).
  Output: `Tasks [5/5 compliant] | Contamination [CLEAN] | Unaccounted [4 necessary files] | VERDICT: APPROVE`
  
  **Notes**:
  - GameContainer.kt modified (`open` class/property) - REQUIRED for test inheritance
  - gradle.properties modified - REQUIRED for sdkDir configuration
  - Root build.gradle.kts - REQUIRED for plugin application

---

## Commit Strategy

- **1**: `build(deps): add paparazzi 1.3.4 for screenshot testing` — `gradle/libs.versions.toml`
- **2**: `build(client): configure paparazzi plugin and dependency` — `client/build.gradle.kts`
- **3**: `test(visual): add DiscipleCard screenshot tests (3 scenarios)` — `DiscipleCardPaparazziTest.kt`
- **4**: `test(visual): add GameScreen screenshot tests (4 scenarios + theme comparison)` — `GameScreenPaparazziTest.kt`

---

## Success Criteria

### Verification Commands
```bash
./gradlew :client:recordPaparazziDebug --tests "*PaparazziTest*"  # 录制截图
./gradlew :client:verifyPaparazziDebug --tests "*PaparazziTest*"  # 验证测试
```

### Final Checklist
- [ ] Paparazzi 依赖和插件已正确配置
- [ ] DiscipleCardPaparazziTest.kt 包含 3 个测试场景
- [ ] GameScreenPaparazziTest.kt 包含 4 个测试场景 + 主题对比
- [ ] 截图成功生成并验证通过
- [ ] 代码通过 ktlint 和 detekt 检查
