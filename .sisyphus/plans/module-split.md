# 主模块拆分工作计划

## TL;DR

> **目标**: 将 `client` 模块拆分为 10 个独立的 Gradle 子模块，实现层结构清晰、单向依赖
>
> **交付物**:
> - `business/domain` - 领域实体、值对象、异常
> - `business/goap-framework` - GOAP 框架层（WorldState, Condition, Effect）
> - `business/goap` - GOAP 实现层（Actions, Goals, DiscipleWorldStateConverter）
> - `business/mvi` - MVI 基础设施
> - `business/data` - 数据层（expect createPlatformStorage）
> - `business/engine` - 游戏引擎
> - `business/feature-game` - 游戏功能 MVI
> - `business/presentation` - UI 组件
> - `app/android` - Android 平台入口
> - `app/desktop` - Desktop 平台入口
>
> **预计工作量**: 大型（重构）
> **并行执行**: 部分可行（模块间有依赖顺序）
> **关键路径**: domain → goap-framework → goap → engine → feature-game → app

---

## Context

### 原始请求
将主模块拆分成多个模块，层级清晰，通用的功能提取为可复用模块。

### 访谈总结
**关键讨论**:
- 拆分维度：按层拆分
- 目录结构：`business/` 业务代码 + `app/` 平台入口
- 只有 GOAP 框架可跨项目复用，但放在 business 下
- Storage 保持 Kotlin Multiplatform expect/actual 模式
- GOAP 拆分为框架层和实现层
- 测试拆分到各模块

**用户决策**:
- Storage: 保持 expect/actual 模式
- DiscipleWorldStateConverter: 放在 GOAP 实现层
- mvi: 保持独立模块

### Metis Review
**识别的问题（已解决）**:
- Q1: Storage 策略 → 保持 expect/actual
- Q2: GOAP 结构 → 拆分为框架层 + 实现层
- Q3: DiscipleWorldStateConverter → 移到 GOAP 实现层
- Q4: mvi → 保持独立

**Guardrails 设置**:
- 不改变任何业务逻辑，仅移动文件
- 不创建超过定义的 10 个模块
- 不删除或禁用任何测试
- 不超过 120 字符行长度

---

## Work Objectives

### Core Objective
将 `client` 模块重构为多个 Gradle 子模块，保持功能不变，仅改变代码组织结构。

### Concrete Deliverables
- 10 个独立的 Gradle 子模块
- 新的 `settings.gradle.kts` 配置
- 新的 `build.gradle.kts` 配置
- 各模块正确的依赖声明

### Definition of Done
- [ ] `./gradlew assemble` 构建成功
- [ ] `./gradlew test` 所有测试通过
- [ ] `./gradlew detekt` 无违规
- [ ] 代码覆盖率不低于重构前

### Must Have
- 单向依赖架构（domain → goap-framework → goap → engine → ...）
- GOAP 框架层完全解耦（无 domain 依赖）
- Storage expect/actual 模式保持不变
- 所有测试文件移动到对应模块

### Must NOT Have (Guardrails)
- 业务逻辑变化
- 超过 10 个模块
- 删除或禁用测试
- expect/actual 模式改变
- 循环依赖

---

## Verification Strategy

### Pre-Refactor Baseline (必须先记录)
```bash
./gradlew assemble                    # 必须通过
./gradlew test                       # 必须通过
./gradlew koverReport                # 记录覆盖率基线

find client/src/commonMain -name "*.kt" | wc -l    # 预期: 59
find client/src/commonTest -name "*.kt" | wc -l    # 预期: 18
```

### Test Decision
- **Infrastructure exists**: YES
- **Automated tests**: Tests-after（重构后验证）
- **Framework**: kotlin-test (已有)
- **Agent-Executed QA**: 每步原子验证

---

## Execution Strategy

### 依赖关系图
```
domain (无依赖)
    │
    ├──► goap-framework (无依赖) ← GOAP 核心算法
    │
    ├──► goap (依赖 goap-framework, domain) ← GOAP 实现
    │
    ├──► engine (依赖 goap-framework, domain)
    │
    ├──► mvi (依赖 domain exception)
    │
    ├──► data (依赖 domain)
    │
    ├──► presentation (依赖 domain, feature-game)
    │
    ├──► feature-game (依赖 domain, mvi, presentation)
    │
    └──► app/android, app/desktop (依赖 business/*)
```

### 原子提交策略

| # | 提交信息 | 验证 |
|---|---------|------|
| 1 | `chore: 创建 Gradle 模块骨架` | `./gradlew projects` 显示 10 个模块 |
| 2 | `refactor: 移动 domain 层` | `:business:domain:compileKotlinJvm` 通过 |
| 3 | `refactor: 创建 goap-framework 模块` | `:business:goap-framework:compileKotlinJvm` 通过 |
| 4 | `refactor: 移动 goap 实现层` | `:business:goap:compileKotlinJvm` 通过 |
| 5 | `refactor: 移动 mvi 层` | `:business:mvi:compileKotlinJvm` 通过 |
| 6 | `refactor: 移动 data 层` | `:business:data:compileKotlinJvm` 通过 |
| 7 | `refactor: 移动 engine 层` | `:business:engine:compileKotlinJvm` 通过 |
| 8 | `refactor: 移动 feature-game` | `:business:feature-game:compileKotlinJvm` 通过 |
| 9 | `refactor: 移动 presentation` | `:business:presentation:compileKotlinJvm` 通过 |
| 10 | `refactor: 创建 app:android 模块` | `:app:android:compileKotlinAndroid` 通过 |
| 11 | `refactor: 创建 app:desktop 模块` | `:app:desktop:compileKotlinJvm` 通过 |
| 12 | `test: 拆分测试到各模块` | `./gradlew test` 全部通过 |
| 13 | `chore: 清理旧 client 结构` | 文件计数验证 |
| 14 | `chore: 最终验证` | `./gradlew build` 通过 |

---

## TODOs

- [x] 1. 创建 Gradle 模块骨架结构

  **What to do**:
  - 创建 `business/` 和 `app/` 目录结构
  - 创建各模块的 `build.gradle.kts` 文件
  - 创建模块骨架源码目录 (`src/commonMain/kotlin/...`)
  - 更新根目录 `settings.gradle.kts` 包含所有 10 个模块
  - 配置各模块的 `sourceSets`（commonMain, commonTest, androidMain, desktopMain 等）

  **Must NOT do**:
  - 不要在任何模块中添加源代码（仅骨架）
  - 不要修改根 `build.gradle.kts` 的 plugins 或 dependencies
  - 不要创建 libs.versions.toml 中不存在的依赖声明

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: 目录结构和 Gradle 配置是机械化操作
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO（顺序创建目录结构）
  - **Blocks**: 所有后续任务

  **References**:
  - `settings.gradle.kts` - 现有模块配置参考
  - `client/build.gradle.kts` - 现有 build 配置参考
  - `.gradle/` 结构 - Kotlin Multiplatform 标准布局

  **Acceptance Criteria**:
  - [ ] `find business/ app/ -type d -name "kotlin" | wc -l` 返回 20+（各模块的 kotlin 目录）
  - [ ] `grep -l "business:domain" settings.gradle.kts` 存在
  - [ ] `grep -l "business:goap-framework" settings.gradle.kts` 存在

  **QA Scenarios**:
  ```
  Scenario: 骨架结构验证
    Tool: Bash
    Steps:
      1. 运行 ./gradlew projects
      2. 验证输出包含 10 个新模块
    Expected Result: 列表包含 business:domain, business:goap-framework, business:goap, 
                     business:mvi, business:data, business:engine, business:feature-game,
                     business:presentation, app:android, app:desktop
    Failure Indicators: 缺少任何模块名称
    Evidence: .sisyphus/evidence/task-1-projects.txt
  ```

  **Commit**: YES
  - Message: `chore: 创建 Gradle 模块骨架结构`
  - Files: `settings.gradle.kts`, `business/*/build.gradle.kts`, `app/*/build.gradle.kts`

---

- [ ] 2. 移动 domain 层到 business:domain

  **What to do**:
  - 将 `client/src/commonMain/kotlin/com/sect/game/domain/` 移动到 `business/domain/src/commonMain/kotlin/com/sect/game/domain/`
  - 包含: entity/, valueobject/, exception/
  - 创建 `business/domain/src/commonTest/kotlin/com/sect/game/domain/` 并移动相关测试
  - 配置 `business/domain/build.gradle.kts` 的 dependencies（无外部依赖，仅 kotlin stdlib）

  **Must NOT do**:
  - 不要修改任何 Kotlin 文件内容
  - 不要移动 goap 相关文件（goap 在 domain 之后处理）
  - 不要修改 domain 内的 import 语句（路径不变）

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: 纯文件移动，无逻辑修改
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES（与任务 3, 4, 5, 6 可并行）
  - **Blocks**: 任务 7, 8, 9, 10, 11
  - **Blocked By**: 任务 1

  **References**:
  - `client/src/commonMain/kotlin/com/sect/game/domain/` - 待移动文件
  - `client/src/commonTest/kotlin/com/sect/game/domain/` - 待移动测试
  - `client/src/commonTest/kotlin/com/sect/game/e2e/` - E2E 测试保留在 app 层

  **Acceptance Criteria**:
  - [ ] `ls business/domain/src/commonMain/kotlin/com/sect/game/domain/` 显示 entity/, valueobject/, exception/
  - [ ] `ls business/domain/src/commonTest/kotlin/com/sect/game/domain/` 显示对应测试文件
  - [ ] `:business:domain:compileKotlinJvm` BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: Domain 模块编译验证
    Tool: Bash
    Steps:
      1. 运行 ./gradlew :business:domain:compileKotlinJvm
      2. 验证 BUILD SUCCESSFUL
    Expected Result: 无编译错误
    Failure Indicators: 缺少依赖、路径错误
    Evidence: .sisyphus/evidence/task-2-compile.txt
  ```

  **Commit**: YES
  - Message: `refactor: 移动 domain 层到 business:domain`
  - Files: `business/domain/src/commonMain/`, `business/domain/src/commonTest/`

---

- [x] 3. 创建 goap-framework 模块并移动 GOAP 核心

  **What to do**:
  - 创建 `business/goap-framework/src/commonMain/kotlin/com/sect/game/goap/`
  - 移动 GOAP 框架层代码（无 domain 依赖）:
    - `goap/core/` - WorldState, Condition, Effect, ModifyEffect
    - `goap/goals/Goal.kt` - Goal 接口（纯框架）
  - **不移动**: actions/, goals/的实现类（它们依赖 domain）
  - 创建测试目录并移动 `commonTest/kotlin/com/sect/game/goap/core/` 测试

  **Must NOT do**:
  - 不要移动 DiscipleWorldStateConverter（属于 goap 实现层）
  - 不要移动 actions/ 和 goals/ 实现类
  - 不要让 goap-framework 依赖 domain

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: 文件识别和移动，需区分框架与实现
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES（与任务 2, 4, 5, 6 可并行）
  - **Blocks**: 任务 4, 7
  - **Blocked By**: 任务 1

  **References**:
  - `client/src/commonMain/kotlin/com/sect/game/goap/core/` - 框架核心
  - `client/src/commonMain/kotlin/com/sect/game/goap/goals/Goal.kt` - Goal 接口
  - `client/src/commonTest/kotlin/com/sect/game/goap/core/` - 测试

  **Acceptance Criteria**:
  - [ ] `ls business/goap-framework/src/commonMain/kotlin/com/sect/game/goap/` 显示 core/, goals/Goal.kt
  - [ ] `:business:goap-framework:compileKotlinJvm` BUILD SUCCESSFUL
  - [ ] `grep -r "import com.sect.game.domain" business/goap-framework/src/` 返回空

  **QA Scenarios**:
  ```
  Scenario: goap-framework 编译验证
    Tool: Bash
    Steps:
      1. 运行 ./gradlew :business:goap-framework:compileKotlinJvm
      2. 验证 BUILD SUCCESSFUL
    Expected Result: 无编译错误
    Evidence: .sisyphus/evidence/task-3-compile.txt

  Scenario: 无 domain 依赖验证
    Tool: grep
    Steps:
      1. grep -r "import com.sect.game.domain" business/goap-framework/src/
    Expected Result: 无输出（框架层无 domain 依赖）
    Evidence: .sisyphus/evidence/task-3-no-domain-dep.txt
  ```

  **Commit**: YES
  - Message: `refactor: 创建 goap-framework 模块`
  - Files: `business/goap-framework/src/commonMain/`, `business/goap-framework/src/commonTest/`

---

- [x] 4. 创建 goap 实现层并移动 Actions 和 Goals 实现

  **What to do**:
  - 创建 `business/goap/src/commonMain/kotlin/com/sect/game/goap/`
  - 移动 GOAP 实现层:
    - `goap/actions/` - 所有 Action 实现类
    - `goap/goals/` - 除 Goal.kt 外的实现（RestGoal, CultivationGoal 等）
  - 移动 `engine/DiscipleWorldStateConverter.kt`（GOAP 实现使用 domain 数据）
  - 创建测试目录并移动 `commonTest/kotlin/com/sect/game/goap/actions/`, `goap/goals/` 测试

  **Must NOT do**:
  - 不要移动 goap/core/（属于 goap-framework）
  - 不要移动 goap/goals/Goal.kt（属于 goap-framework）
  - 不要移动 engine/ 的其他文件（GameEngine, ActionExecutor 等属于 engine）

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: 文件移动操作
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES（与任务 2, 3, 5, 6 可并行）
  - **Blocks**: 任务 7
  - **Blocked By**: 任务 1, 3

  **References**:
  - `client/src/commonMain/kotlin/com/sect/game/goap/actions/` - Action 实现
  - `client/src/commonMain/kotlin/com/sect/game/goap/goals/` - Goal 实现（除 Goal.kt）
  - `client/src/commonMain/kotlin/com/sect/game/engine/DiscipleWorldStateConverter.kt` - 桥接组件
  - `client/src/commonTest/kotlin/com/sect/game/goap/actions/`, `goap/goals/` - 测试

  **Acceptance Criteria**:
  - [ ] `ls business/goap/src/commonMain/kotlin/com/sect/game/goap/` 显示 actions/, goals/
  - [ ] `ls business/goap/src/commonMain/kotlin/com/sect/game/` 显示 engine/DiscipleWorldStateConverter.kt
  - [ ] `:business:goap:compileKotlinJvm` BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: goap 模块编译验证
    Tool: Bash
    Steps:
      1. 运行 ./gradlew :business:goap:compileKotlinJvm
      2. 验证 BUILD SUCCESSFUL
    Expected Result: 无编译错误
    Evidence: .sisyphus/evidence/task-4-compile.txt
  ```

  **Commit**: YES
  - Message: `refactor: 移动 goap 实现层`
  - Files: `business/goap/src/commonMain/`, `business/goap/src/commonTest/`

---

- [x] 5. 移动 mvi 层到 business:mvi

  **What to do**:
  - 将 `client/src/commonMain/kotlin/com/sect/game/mvi/` 移动到 `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/`
  - 包含: GameErrorHandler.kt
  - 创建测试目录（无 mvi 测试文件，保持 commonTest 结构一致）

  **Must NOT do**:
  - 不要修改 GameErrorHandler.kt 内容
  - 不要移动到 feature-game（保持独立模块）

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: 纯文件移动
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES（与任务 2, 3, 4, 6 可并行）
  - **Blocks**: 任务 7, 8, 9, 10, 11
  - **Blocked By**: 任务 1

  **References**:
  - `client/src/commonMain/kotlin/com/sect/game/mvi/` - 待移动文件

  **Acceptance Criteria**:
  - [ ] `ls business/mvi/src/commonMain/kotlin/com/sect/game/mvi/` 显示 GameErrorHandler.kt
  - [ ] `:business:mvi:compileKotlinJvm` BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: mvi 模块编译验证
    Tool: Bash
    Steps:
      1. 运行 ./gradlew :business:mvi:compileKotlinJvm
      2. 验证 BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-5-compile.txt
  ```

  **Commit**: YES
  - Message: `refactor: 移动 mvi 层到 business:mvi`
  - Files: `business/mvi/src/commonMain/`

---

- [x] 6. 移动 data 层到 business:data

  **What to do**:
  - 将 `client/src/commonMain/kotlin/com/sect/game/data/` 移动到 `business/data/src/commonMain/kotlin/com/sect/game/data/`
  - 包含: dto/, mapper/, storage/（含 expect 声明）
  - 创建 `business/data/src/commonTest/` 并移动 JsonGameStorageTest
  - **重要**: 保持 `expect fun createPlatformStorage()` 在 commonMain

  **Must NOT do**:
  - 不要移动 androidMain/desktopMain 的 storage 实现（属于 app 模块）
  - 不要修改 expect/actual 声明

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: 文件移动
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES（与任务 2, 3, 4, 5 可并行）
  - **Blocks**: 任务 10, 11
  - **Blocked By**: 任务 1

  **References**:
  - `client/src/commonMain/kotlin/com/sect/game/data/` - commonMain 数据层
  - `client/src/commonTest/kotlin/com/sect/game/data/storage/JsonGameStorageTest.kt` - 测试
  - `client/src/androidMain/kotlin/com/sect/game/data/storage/AndroidGameStorage.kt` - 后续移动
  - `client/src/desktopMain/kotlin/com/sect/game/data/storage/DesktopGameStorage.kt` - 后续移动

  **Acceptance Criteria**:
  - [ ] `ls business/data/src/commonMain/kotlin/com/sect/game/data/` 显示 dto/, mapper/, storage/
  - [ ] `grep "expect fun createPlatformStorage" business/data/src/commonMain/` 存在
  - [ ] `:business:data:compileKotlinJvm` BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: data 模块编译验证
    Tool: Bash
    Steps:
      1. 运行 ./gradlew :business:data:compileKotlinJvm
      2. 验证 BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-6-compile.txt
  ```

  **Commit**: YES
  - Message: `refactor: 移动 data 层到 business:data`
  - Files: `business/data/src/commonMain/`, `business/data/src/commonTest/`

---

- [x] 7. 移动 engine 层到 business:engine

  **What to do**:
  - 将 `client/src/commonMain/kotlin/com/sect/game/engine/` 移动到 `business/engine/src/commonMain/kotlin/com/sect/game/engine/`
  - **注意**: DiscipleWorldStateConverter.kt 已在上一步移到 goap 模块
  - 包含: GameEngine.kt, ActionExecutor.kt, ActionRegistry.kt, GOAPPlanner.kt
  - 创建测试目录并移动 `commonTest/kotlin/com/sect/game/engine/` 测试

  **Must NOT do**:
  - 不要移动 DiscipleWorldStateConverter.kt（已在 goap 模块）
  - 不要修改 engine 内文件的 import（可能需要更新 DiscipleWorldStateConverter 的 import）

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: 文件移动
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO（依赖任务 4 完成 - DiscipleWorldStateConverter 位置确定）
  - **Blocks**: 任务 8, 9, 10, 11
  - **Blocked By**: 任务 1, 4

  **References**:
  - `client/src/commonMain/kotlin/com/sect/game/engine/` - 待移动文件（不含 DiscipleWorldStateConverter）
  - `client/src/commonTest/kotlin/com/sect/game/engine/` - 测试

  **Acceptance Criteria**:
  - [ ] `ls business/engine/src/commonMain/kotlin/com/sect/game/engine/` 显示 GameEngine.kt, executor/, registry/, planner/
  - [ ] `:business:engine:compileKotlinJvm` BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: engine 模块编译验证
    Tool: Bash
    Steps:
      1. 运行 ./gradlew :business:engine:compileKotlinJvm
      2. 验证 BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-7-compile.txt
  ```

  **Commit**: YES
  - Message: `refactor: 移动 engine 层到 business:engine`
  - Files: `business/engine/src/commonMain/`, `business/engine/src/commonTest/`

---

- [x] 8. 移动 feature-game 到 business:feature-game
  **What to do**:
  - 将 `client/src/commonMain/kotlin/com/sect/game/feature/game/` 移动到 `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/`
  - 包含: contract/, container/, presentation/
  - 创建测试目录并移动 `commonTest/` 中相关测试

  **Must NOT do**:
  - 不要移动 presentation/ 下的共享组件（DiscipleCard, CreateDiscipleDialog 属于 presentation）
  - 不要修改任何 Kotlin 文件内容

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: 文件移动
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES（与任务 9 可并行，依赖任务 2, 5, 6, 7 完成）
  - **Blocks**: 任务 10, 11
  - **Blocked By**: 任务 1, 2, 5, 6, 7

  **References**:
  - `client/src/commonMain/kotlin/com/sect/game/feature/game/` - 待移动文件
  - `client/src/commonTest/kotlin/com/sect/game/feature/` - 测试

  **Acceptance Criteria**:
  - [ ] `ls business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/` 显示 contract/, container/, presentation/
  - [ ] `:business:feature-game:compileKotlinJvm` BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: feature-game 模块编译验证
    Tool: Bash
    Steps:
      1. 运行 ./gradlew :business:feature-game:compileKotlinJvm
      2. 验证 BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-8-compile.txt
  ```

  **Commit**: YES
  - Message: `refactor: 移动 feature-game 到 business:feature-game`
  - Files: `business/feature-game/src/commonMain/`, `business/feature-game/src/commonTest/`

---

- [x] 9. 移动 presentation 到 business:presentation
  **What to do**:
  - 将 `client/src/commonMain/kotlin/com/sect/game/presentation/` 移动到 `business/presentation/src/commonMain/kotlin/com/sect/game/presentation/`
  - 包含: theme/, DiscipleCard.kt, CreateDiscipleDialog.kt
  - 创建测试目录并移动 `commonTest/` 中相关测试

  **Must NOT do**:
  - 不要移动 feature/game/ 下的 presentation（属于 feature-game）
  - 不要修改任何 Kotlin 文件内容

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: 文件移动
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES（与任务 8 可并行，依赖任务 2, 6, 7 完成）
  - **Blocks**: 任务 10, 11
  - **Blocked By**: 任务 1, 2, 6, 7, 8

  **References**:
  - `client/src/commonMain/kotlin/com/sect/game/presentation/` - 待移动文件
  - `client/src/commonTest/kotlin/com/sect/game/presentation/` - 测试

  **Acceptance Criteria**:
  - [ ] `ls business/presentation/src/commonMain/kotlin/com/sect/game/presentation/` 显示 theme/, DiscipleCard.kt, CreateDiscipleDialog.kt
  - [ ] `:business:presentation:compileKotlinJvm` BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: presentation 模块编译验证
    Tool: Bash
    Steps:
      1. 运行 ./gradlew :business:presentation:compileKotlinJvm
      2. 验证 BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-9-compile.txt
  ```

  **Commit**: YES
  - Message: `refactor: 移动 presentation 到 business:presentation`
  - Files: `business/presentation/src/commonMain/`, `business/presentation/src/commonTest/`

---

- [x] 10. 创建 app:android 模块

  **What to do**:
  - 创建 `app/android/src/androidMain/kotlin/com/sect/game/` 并移动:
    - `client/src/androidMain/kotlin/com/sect/game/client/MainActivity.kt`
    - `client/src/androidMain/kotlin/com/sect/game/data/storage/AndroidGameStorage.kt`
  - 创建 `app/android/src/androidMain/` 的 actual storage 实现
  - 配置 Android 特定的 sourceSets 和 dependencies

  **Must NOT do**:
  - 不要移动 commonMain 代码到 app 模块
  - 不要修改 AndroidGameStorage 的 actual 实现

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: 结构重组
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES（与任务 11 可并行，依赖任务 6, 8, 9 完成）
  - **Blocks**: 任务 12, 13, 14
  - **Blocked By**: 任务 1, 6, 8, 9

  **References**:
  - `client/src/androidMain/kotlin/com/sect/game/client/MainActivity.kt`
  - `client/src/androidMain/kotlin/com/sect/game/data/storage/AndroidGameStorage.kt`
  - `business/data/src/commonMain/kotlin/com/sect/game/data/storage/` - expect 声明

  **Acceptance Criteria**:
  - [ ] `ls app/android/src/androidMain/kotlin/com/sect/game/` 显示 client/, data/storage/
  - [ ] `grep "actual fun createPlatformStorage" app/android/` 存在
  - [ ] `:app:android:compileKotlinAndroid` BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: Android 模块编译验证
    Tool: Bash
    Steps:
      1. 运行 ./gradlew :app:android:compileKotlinAndroid
      2. 验证 BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-10-compile.txt
  ```

  **Commit**: YES
  - Message: `refactor: 创建 app:android 模块`
  - Files: `app/android/src/androidMain/`

---

- [x] 11. 创建 app:desktop 模块

  **What to do**:
  - 创建 `app/desktop/src/desktopMain/kotlin/com/sect/game/` 并移动:
    - `client/src/desktopMain/kotlin/com/sect/game/client/main.kt`
    - `client/src/desktopMain/kotlin/com/sect/game/data/storage/DesktopGameStorage.kt`
  - 创建 `app/desktop/src/desktopMain/` 的 actual storage 实现
  - 配置 Desktop 特定的 sourceSets 和 dependencies

  **Must NOT do**:
  - 不要移动 commonMain 代码到 app 模块
  - 不要修改 DesktopGameStorage 的 actual 实现

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: 结构重组
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES（与任务 10 可并行，依赖任务 6, 8, 9 完成）
  - **Blocks**: 任务 12, 13, 14
  - **Blocked By**: 任务 1, 6, 8, 9

  **References**:
  - `client/src/desktopMain/kotlin/com/sect/game/client/main.kt`
  - `client/src/desktopMain/kotlin/com/sect/game/data/storage/DesktopGameStorage.kt`
  - `business/data/src/commonMain/kotlin/com/sect/game/data/storage/` - expect 声明

  **Acceptance Criteria**:
  - [ ] `ls app/desktop/src/desktopMain/kotlin/com/sect/game/` 显示 client/, data/storage/
  - [ ] `grep "actual fun createPlatformStorage" app/desktop/` 存在
  - [ ] `:app:desktop:compileKotlinJvm` BUILD SUCCESSFUL

  **QA Scenarios**:
  ```
  Scenario: Desktop 模块编译验证
    Tool: Bash
    Steps:
      1. 运行 ./gradlew :app:desktop:compileKotlinJvm
      2. 验证 BUILD SUCCESSFUL
    Evidence: .sisyphus/evidence/task-11-compile.txt
  ```

  **Commit**: YES
  - Message: `refactor: 创建 app:desktop 模块`
  - Files: `app/desktop/src/desktopMain/`

---

- [x] 12. 拆分测试到各模块

  **What to do**:
  - 将 `client/src/commonTest/` 下的测试文件移动到对应模块的 `commonTest/`:
    - `domain/*` → `business/domain/src/commonTest/`
    - `goap/*` → `business/goap/src/commonTest/`（framework 和 implementation 合并测试）
    - `engine/*` → `business/engine/src/commonTest/`
    - `data/storage/*` → `business/data/src/commonTest/`
    - `feature/*` → `business/feature-game/src/commonTest/`
    - `presentation/*` → `business/presentation/src/commonTest/`
  - **保留在 app 层**: `e2e/*`（端到端测试需要完整 stack）
  - 创建 `InMemoryGameStorage` 辅助类在 e2e 测试中

  **Must NOT do**:
  - 不要删除任何测试文件
  - 不要修改测试代码（只移动位置）
  - 不要移动 e2e 测试（它们依赖完整 stack）

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: 文件移动
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO（依赖所有业务模块创建完成）
  - **Blocks**: 任务 13, 14
  - **Blocked By**: 任务 2, 3, 4, 5, 6, 7, 8, 9

  **References**:
  - `client/src/commonTest/kotlin/com/sect/game/` - 所有测试
  - `business/*/src/commonTest/` - 各模块测试目标目录

  **Acceptance Criteria**:
  - [ ] 各模块 commonTest 目录存在且包含对应测试
  - [ ] `client/src/commonTest/` 仅保留 e2e/ 目录
  - [ ] `./gradlew test` 全部通过

  **QA Scenarios**:
  ```
  Scenario: 测试拆分验证
    Tool: Bash
    Steps:
      1. find business/*/src/commonTest -name "*.kt" | wc -l
      2. ls client/src/commonTest/kotlin/com/sect/game/
    Expected Result: 文件数合理（与原 18 个减去 e2e 数量一致），仅 e2e 保留在 client
    Evidence: .sisyphus/evidence/task-12-test-split.txt

  Scenario: 所有测试通过
    Tool: Bash
    Steps:
      1. ./gradlew test
      2. 验证所有测试 PASSED
    Expected Result: 无测试失败
    Evidence: .sisyphus/evidence/task-12-all-tests.txt
  ```

  **Commit**: YES
  - Message: `test: 拆分测试到各模块`
  - Files: 移动测试文件到各模块

---

- [x] 13. 清理旧的 client 模块结构

  **What to do**:
  - 删除 `client/src/commonMain/` 目录（所有代码已移出）
  - 删除 `client/src/commonTest/` 目录（测试已移出）
  - 删除 `client/src/androidMain/` 目录（已移出）
  - 删除 `client/src/desktopMain/` 目录（已移出）
  - 检查 `client/` 目录是否还有其他必要文件（build.gradle.kts 可能需要保留作为入口）
  - 验证新模块结构完整

  **Must NOT do**:
  - 不要删除 `client/build.gradle.kts`（需要保留或重建）
  - 不要删除 `.github/workflows/` 中的 CI 配置
  - 不要修改任何已移动文件的实际内容

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > **Reason**: 清理操作
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO（依赖任务 12）
  - **Blocks**: 任务 14
  - **Blocked By**: 任务 12

  **References**:
  - `client/src/` - 待检查和清理
  - `business/` - 新模块结构
  - `app/` - 新平台入口

  **Acceptance Criteria**:
  - [ ] `ls client/src/` 返回空或仅有 build.gradle.kts
  - [ ] `find business/ app/ -name "*.kt" | wc -l` 大于 59

  **QA Scenarios**:
  ```
  Scenario: 旧结构清理验证
    Tool: Bash
    Steps:
      1. ls client/src/
      2. find business/ app/ -name "*.kt" | wc -l
    Expected Result: client/src 为空或仅有配置，kotlin 文件总数大于原 59
    Evidence: .sisyphus/evidence/task-13-cleanup.txt
  ```

  **Commit**: YES
  - Message: `chore: 清理旧 client 模块结构`
  - Files: 删除 `client/src/commonMain/`, `commonTest/`, `androidMain/`, `desktopMain/`

---

- [x] 14. 执行最终验证

  **What to do**:
  - 执行完整构建验证：`./gradlew build`
  - 运行 Final Verification Wave (F1-F4)
  - 收集所有证据文件
  - 向用户展示验证结果

  **Must NOT do**:
  - 不要修改任何代码（仅验证）
  - 不要跳过任何验证步骤

  **Recommended Agent Profile**:
  > **Category**: `unspecified-high`
  > **Reason**: 需要运行多个验证命令，综合判断
  > **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES（F1-F4 可并行执行）
  - **Blocked By**: 任务 13

  **References**:
  - Final Verification Wave - 验证标准和命令

  **Acceptance Criteria**:
  - [ ] `./gradlew build` BUILD SUCCESSFUL
  - [ ] F1-F4 全部 APPROVE

  **QA Scenarios**:
  ```
  Scenario: 最终构建验证
    Tool: Bash
    Steps:
      1. ./gradlew build
      2. 验证 BUILD SUCCESSFUL
    Expected Result: build 通过
    Evidence: .sisyphus/evidence/task-14-build.txt
  ```

  **Commit**: YES
  - Message: `chore: 最终验证`
  - Files: 无文件变更

---

## Final Verification Wave

> 4 个验证并行执行，全部通过后向用户展示结果

- [ ] F1. **Plan Compliance Audit** — `oracle`

  读取计划 end-to-end，验证：
  - 每个 "Must Have" 存在（实现存在）
  - 每个 "Must NOT Have" 不存在（搜索禁止模式）
  - 证据文件存在于 `.sisyphus/evidence/`
  - 交付物与计划对比

  **QA Scenarios**:
  ```
  Scenario: Must Have 检查
    Tool: oracle
    Steps:
      1. 读取计划 "Must Have" 部分
      2. 验证每个 Must Have 存在实现
      3. 记录结果
    Expected Result: 所有 Must Have 验证通过
    Evidence: .sisyphus/evidence/f1-must-have.txt

  Scenario: Must NOT Have 检查
    Tool: oracle
    Steps:
      1. 读取计划 "Must NOT Have" 部分
      2. 搜索禁止模式
      3. 记录任何违规
    Expected Result: 无违规发现
    Evidence: .sisyphus/evidence/f1-must-not-have.txt
  ```

  Output: `Must Have [N/N] | Must NOT Have [N/N] | Tasks [N/N] | VERDICT: APPROVE/REJECT`

- [ ] F2. **Build Verification** — `unspecified-high`

  验证命令：
  ```bash
  ./gradlew assemble
  ./gradlew test
  ./gradlew detekt
  ./gradlew koverReport
  ```

  **QA Scenarios**:
  ```
  Scenario: 完整构建验证
    Tool: Bash
    Steps:
      1. ./gradlew assemble
      2. 验证 BUILD SUCCESSFUL
    Expected Result: assemble 通过
    Evidence: .sisyphus/evidence/f2-assemble.txt

  Scenario: 所有测试通过
    Tool: Bash
    Steps:
      1. ./gradlew test
      2. 验证所有测试 PASSED
    Expected Result: 无测试失败
    Evidence: .sisyphus/evidence/f2-test.txt

  Scenario: 代码质量检查
    Tool: Bash
    Steps:
      1. ./gradlew detekt
      2. 验证无违规
    Expected Result: detekt 通过
    Evidence: .sisyphus/evidence/f2-detekt.txt
  ```

  Output: `Assemble [PASS/FAIL] | Tests [N pass/N fail] | Detekt [PASS/FAIL] | Coverage [N%]`

- [ ] F3. **Module Structure Verification** — `unspecified-high`

  验证模块结构：
  ```bash
  ./gradlew projects
  find business/ -name "*.kt" | wc -l
  find app/ -name "*.kt" | wc -l
  ```

  验证依赖关系：
  ```bash
  ./gradlew :business:domain:dependencies --configuration compileClasspath
  ./gradlew :business:goap-framework:dependencies --configuration compileClasspath
  ```

  **QA Scenarios**:
  ```
  Scenario: 模块数量验证
    Tool: Bash
    Steps:
      1. ./gradlew projects
      2. 验证输出包含 10 个新模块
    Expected Result: 10 个模块存在
    Evidence: .sisyphus/evidence/f3-modules.txt

  Scenario: 文件数量验证
    Tool: Bash
    Steps:
      1. find business/ -name "*.kt" | wc -l
      2. find app/ -name "*.kt" | wc -l
    Expected Result: business 中约 59 个文件，app 中约 4 个文件
    Evidence: .sisyphus/evidence/f3-file-counts.txt

  Scenario: 依赖关系验证
    Tool: Bash
    Steps:
      1. ./gradlew :business:goap-framework:dependencies --configuration compileClasspath
      2. 验证 goap-framework 无 domain 依赖
    Expected Result: goap-framework 的 compileClasspath 中无 domain
    Evidence: .sisyphus/evidence/f3-goap-framework-deps.txt
  ```

  Output: `Modules [10/N] | Files [N in business, M in app] | Dependencies [Correct/Incorrect]`

- [ ] F4. **Scope Fidelity Check** — `deep`

  对每个任务：
  - 读取 "What to do"
  - 读取实际 diff
  - 验证 1:1

  检测跨任务污染：Task N 是否触碰 Task M 的文件

  **QA Scenarios**:
  ```
  Scenario: 任务 1:1 验证
    Tool: deep
    Steps:
      1. 对每个任务读取 "What to do"
      2. git diff --name-only
      3. 验证移动的文件与计划一致
    Expected Result: 所有文件移动与计划匹配
    Evidence: .sisyphus/evidence/f4-task fidelity.txt

  Scenario: 跨任务污染检测
    Tool: deep
    Steps:
      1. 检查是否有 Task N 触碰 Task M 文件
      2. 记录任何污染
    Expected Result: 无跨任务污染
    Evidence: .sisyphus/evidence/f4-contamination.txt
  ```

  Output: `Tasks [N/N compliant] | Contamination [CLEAN/N issues] | VERDICT`

---

## Commit Strategy

- **Task 1**: `chore: 创建 Gradle 模块骨架结构`
- **Task 2**: `refactor: 移动 domain 层到 business:domain`
- **Task 3**: `refactor: 创建 goap-framework 模块`
- **Task 4**: `refactor: 移动 goap 实现层`
- **Task 5**: `refactor: 移动 mvi 层到 business:mvi`
- **Task 6**: `refactor: 移动 data 层到 business:data`
- **Task 7**: `refactor: 移动 engine 层到 business:engine`
- **Task 8**: `refactor: 移动 feature-game 到 business:feature-game`
- **Task 9**: `refactor: 移动 presentation 到 business:presentation`
- **Task 10**: `refactor: 创建 app:android 模块`
- **Task 11**: `refactor: 创建 app:desktop 模块`
- **Task 12**: `test: 拆分测试到各模块`
- **Task 13**: `chore: 清理旧 client 模块结构`
- **Task 14**: `chore: 最终验证`

---

## Success Criteria

### 验证命令
```bash
./gradlew assemble                    # Expected: BUILD SUCCESSFUL
./gradlew test                       # Expected: All tests passed
./gradlew detekt                     # Expected: No violations
./gradlew :business:domain:compileKotlinJvm   # Expected: BUILD SUCCESSFUL
./gradlew :business:goap-framework:compileKotlinJvm  # Expected: BUILD SUCCESSFUL
./gradlew :business:goap:compileKotlinJvm    # Expected: BUILD SUCCESSFUL
./gradlew :business:engine:compileKotlinJvm   # Expected: BUILD SUCCESSFUL
./gradlew :app:android:compileKotlinAndroid   # Expected: BUILD SUCCESSFUL
./gradlew :app:desktop:compileKotlinJvm       # Expected: BUILD SUCCESSFUL
```

### 最终检查清单
- [ ] 所有 10 个模块存在且编译通过
- [ ] 依赖关系单向（无循环依赖）
- [ ] GOAP framework 层无 domain 依赖
- [ ] Storage expect/actual 模式保持
- [ ] 所有测试通过
- [ ] 代码覆盖率不低于重构前
- [ ] 旧 client/src 目录已清理
- [ ] Detekt 无违规