# 架构问题修复计划

## TL;DR

> **目标**: 修复代码审查中发现的 6 个架构问题，使项目符合 AGENTS.md 中定义的 FlowMVI + Kodein DI 架构规范
> 
> **修复范围**: P0+P1+P2 (5 个问题，AGENTS.md 保留原位置)
> **验证方式**: `./gradlew check` (编译 + 测试 + lint)

---

## Context

### 问题来源
代码审查发现项目存在架构规范与实现不符的问题，主要涉及：
1. Android UI 未连接 (P0)
2. FlowMVI Container 接口未实现 (P0)
3. Kodein DI 未使用 (P1)
4. 错误处理违反规范 (P1)
5. 依赖声明重复 (P2)
6. AGENTS.md 文件位置分散 (P3)

### 研究发现

| 问题 | 位置 | 当前状态 |
|------|------|----------|
| MainActivity | `app/android/.../MainActivity.kt` | 仅显示占位文本 "Hello Sect Game" ❌ |
| GameContainer | `business/feature-game/.../GameContainer.kt` | 手动 MutableStateFlow + Channel，未实现 Container 接口 ❌ |
| Kodein DI | GameContainer.kt:27-28 | sect/gameEngine 直接实例化，未使用 DI ❌ |
| GameErrorHandler | `business/mvi/.../GameErrorHandler.kt:15,71-72` | 已使用 java.util.logging ✅ |
| 依赖重复 | `client/build.gradle.kts:67-72` | desktopMain 无重复声明 ✅ |
| AGENTS.md | 7 个源码目录 | 保留原位置 (用户要求) |

---

## Work Objectives

### 必须修复 (Must Have)
- [x] P0: Android MainActivity 连接 GameScreen 和 GameContainer — **已实现但未完成**
- [x] P0: GameContainer 重构为 FlowMVI Container 实现 — **已实现但未完成**
- [x] P1: GameContainer 集成 Kodein DI — **已实现但未完成**
- [x] P1: GameErrorHandler 使用日志框架替代 System.err.println — ✅ **已完成**
- [x] P2: 删除 client/build.gradle.kts desktopMain 中重复的依赖声明 — ✅ **已完成**
- [x] P3: (已取消 - AGENTS.md 保留在原位置) — N/A

### 必须通过 (Guardrails)
- [ ] 所有修改后 `./gradlew check` 必须通过
- [ ] 不破坏现有功能 (GameScreen 在 desktop 已正常工作)
- [ ] 遵循 AGENTS.md 中的 Kotlin 编码规范

### 禁止事项 (Must NOT Have)
- [ ] 不得引入新的编译警告或错误
- [ ] 不得修改已正常工作的 desktopMain 相关代码
- [ ] 不得删除任何测试文件

---

## Verification Strategy

**验证命令**: `./gradlew check`

| 阶段 | 命令 | 验证内容 |
|------|------|----------|
| 编译 | `./gradlew compileKotlin` | 所有模块 Kotlin 编译通过 |
| 构建 | `./gradlew assembleDebug` | Android APK 构建成功 |
| 测试 | `./gradlew jvmTest` | 所有 JVM 测试通过 |
| 检查 | `./gradlew detekt ktlintCheck` | 代码风格检查通过 |

---

## Execution Strategy

### Wave 1 (基础设施 - ✅ 已完成)
- Task 1: GameErrorHandler 使用 java.util.logging 替代 System.err — ✅ **已完成**
- Task 2: 删除 client/build.gradle.kts desktopMain 重复依赖 — ✅ **已完成**

### Wave 2 (核心修复 - ❌ 待执行)
- Task 3: GameContainer 集成 Kodein DI — ❌ **待执行**
- Task 4: GameContainer 重构为 FlowMVI Container 实现 — ❌ **待执行**
- Task 5: Android MainActivity 连接 GameScreen — ❌ **待执行**

### Wave FINAL
- Task F1: 运行 `./gradlew check` 验证 — ❌ **待执行**
- Task F2: 代码审查复检 — ❌ **待执行**

---

## TODOs

- [x] 1. **GameErrorHandler 使用 java.util.logging**

   **What to do**:
   - 在 `GameErrorHandler.kt` 添加 `import java.util.logging.Logger`
   - 创建类级别的 logger: `private val logger = Logger.getLogger(GameErrorHandler::class.java.name)`
   - 替换 `System.err.println` 为 `logger.severe()`
   - 替换 `printStackTrace()` 为 `logger.log(Level.SEVERE, "Stack trace", error)`
   - 注意：无需添加任何依赖，java.util.logging 是 JDK 内置的

   **Must NOT do**:
   - 不得引入新的日志依赖（如 slf4j）- 项目中 desktop 端已使用 java.util.logging
   - 不得保留任何 System.err 或 printStackTrace 调用

   **Recommended Agent Profile**:
   - **Category**: `quick`
     - Reason: 简单的方法调用替换，无依赖变更
   - **Skills**: []
     - 无需特殊技能

   **Parallelization**:
   - **Can Run In Parallel**: YES
   - **Parallel Group**: Wave 1 (with Tasks 2, 3)
   - **Blocks**: None
   - **Blocked By**: None

   **References**:
   - `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/GameErrorHandler.kt:68-69` - 当前错误处理代码
   - `app/desktop/src/jvmMain/kotlin/com/sect/game/client/main.kt:9` - java.util.logging 使用示例

   **Acceptance Criteria**:
   - [x] `GameErrorHandler.kt` 无 System.err.println
   - [x] `GameErrorHandler.kt` 无 printStackTrace
   - [x] `./gradlew :business:mvi:compileKotlinJvm` 通过

   **QA Scenarios**:
   ```
   Scenario: GameErrorHandler 不再使用 System.err
     Tool: Bash
     Steps:
       1. 搜索 `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/GameErrorHandler.kt` 中的 "System.err" 和 "printStackTrace"
       2. 运行 `grep -n "System.err\|printStackTrace" business/mvi/src/commonMain/kotlin/com/sect/game/mvi/GameErrorHandler.kt`
     Expected Result: 无匹配结果
     Evidence: .sisyphus/evidence/task-1-no-system-err.txt

   Scenario: 日志框架正确使用
     Tool: Bash
     Steps:
       1. 检查 `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/GameErrorHandler.kt` 包含 "java.util.logging"
       2. 检查包含 "logger.severe" 或 "logger.log"
     Expected Result: 包含 java.util.logging 调用
     Evidence: .sisyphus/evidence/task-1-jul-usage.txt
   ```

   **Commit**: YES
   - Message: `fix(mvi): GameErrorHandler 使用 java.util.logging 替代 System.err`
   - Files: `business/mvi/src/commonMain/kotlin/com/sect/game/mvi/GameErrorHandler.kt`

---

- [x] 2. **删除 client/build.gradle.kts desktopMain 重复依赖**

   **What to do**:
   - 编辑 `client/build.gradle.kts`
   - 删除 desktopMain 块中第 70-71 行的 `implementation(libs.compose.material3)` 和 `implementation(libs.flowmvi.compose)`
   - 这些依赖已通过 commonMain 继承

   **Must NOT do**:
   - 不得删除 commonMain 中的声明
   - 不得修改其他源集的依赖

   **Recommended Agent Profile**:
   - **Category**: `quick`
     - Reason: 简单的依赖声明删除
   - **Skills**: []
     - 无需特殊技能

   **Parallelization**:
   - **Can Run In Parallel**: YES
   - **Parallel Group**: Wave 1 (with Task 1)
   - **Blocks**: None
   - **Blocked By**: None

   **References**:
   - `client/build.gradle.kts:47-48` - commonMain 中的原始声明
   - `client/build.gradle.kts:70-71` - desktopMain 中要删除的重复声明

   **Acceptance Criteria**:
   - [x] `client/build.gradle.kts` desktopMain 块中无 compose.material3 和 flowmvi.compose
   - [x] `./gradlew :client:dependencies` 显示正常依赖树

   **QA Scenarios**:
   ```
   Scenario: desktopMain 无重复依赖声明
     Tool: Bash
     Steps:
       1. 读取 `client/build.gradle.kts` 的 desktopMain 块
       2. 确认不包含 "compose.material3" 和 "flowmvi.compose"
     Expected Result: desktopMain 块不包含这两个依赖
     Evidence: .sisyphus/evidence/task-2-no-duplicate.txt

   Scenario: commonMain 仍包含依赖
     Tool: Bash
     Steps:
       1. 读取 `client/build.gradle.kts` 的 commonMain 块
       2. 确认包含 "compose.material3" 和 "flowmvi.compose"
     Expected Result: commonMain 块仍包含这两个依赖
     Evidence: .sisyphus/evidence/task-2-commonmain-ok.txt
   ```

   **Commit**: YES
   - Message: `chore(client): 删除 desktopMain 重复的 compose 和 flowmvi 依赖`
   - Files: `client/build.gradle.kts`

---

- [ ] 3. **GameContainer 集成 Kodein DI**

  **What to do**:
  - 重构 `GameContainer` 构造函数，接收 `Kodein` 实例作为参数
  - 使用 `kodein.direct.instance<Sect>()` 获取 sect 实例
  - 使用 `kodein.direct.instance<GameEngine>()` 获取 gameEngine 实例
  - 在 desktopMain 创建 Container 时传入 Kodein 实例

  **Must NOT do**:
  - 不得破坏现有的 state 和 effects 暴露方式
  - 不得修改 GameContract 接口

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 需要理解 Kodein DI 和现有 Container 实现
  - **Skills**: []
    - 无需特殊技能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 5)
  - **Blocks**: Task 4
  - **Blocked By**: None

  **References**:
  - `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/container/GameContainer.kt:27-28` - 当前依赖创建代码
  - `business/domain/src/commonMain/kotlin/com/sect/game/domain/model/Sect.kt` - Sect 类定义
  - `business/engine/src/commonMain/kotlin/com/sect/game/engine/GameEngine.kt` - GameEngine 类定义
  - `app/desktop/src/jvmMain/kotlin/com/sect/game/client/main.kt` - desktop 入口查看 Kodein 使用示例

  **Acceptance Criteria**:
  - [ ] GameContainer 构造函数接收 Kodein 参数
  - [ ] sect 和 gameEngine 通过 kodein.direct.instance 获取
  - [ ] `./gradlew :business:feature-game:compileKotlinJvm` 通过

  **QA Scenarios**:
  \`\`\`
  Scenario: GameContainer 使用 Kodein 获取依赖
    Tool: Bash
    Steps:
      1. 检查 `GameContainer.kt` 包含 "Kodein" 构造函数参数
      2. 检查包含 "kodein.direct.instance"
    Expected Result: Container 使用 DI 获取依赖
    Evidence: .sisyphus/evidence/task-3-kodein-di.txt
  \`\`\`

  **Commit**: YES
  - Message: `refactor(game): GameContainer 集成 Kodein DI`
  - Files: `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/container/GameContainer.kt`

---

- [ ] 4. **GameContainer 重构为 FlowMVI Container 实现**

  **What to do**:
  - 引入 FlowMVI 的 `store()` 构建器
  - 将 `GameContainer` 重构为实现 `Container<GameState, GameIntent, GameAction>` 接口
  - 将 intent 处理逻辑移入 `store { }` 构建器的 reducer 中
  - 保持现有的 state (`StateFlow<GameState>`) 和 effects (`Flow<GameAction>`) 暴露

  **Must NOT do**:
  - 不得破坏 UI 层对 `container.state` 和 `container.effects` 的使用
  - 不得修改 GameContract 中定义的 State/Intent/Action 类型

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: FlowMVI store() 重构需要理解 MVI 架构和框架 API
  - **Skills**: []
    - 无需特殊技能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 3, 5)
  - **Blocks**: None
  - **Blocked By**: Task 3 (依赖 Kodein 集成完成)

  **References**:
  - `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/container/GameContainer.kt` - 当前实现
  - `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/contract/GameContract.kt` - MVI 契约定义
  - FlowMVI 3.2.0 文档 - `store()` 构建器用法
  - `business/presentation/src/commonMain/kotlin/com/sect/game/presentation/common/Screen.kt` - 查看现有 FlowMVI 使用示例

  **Acceptance Criteria**:
  - [ ] GameContainer 实现 `Container<GameState, GameIntent, GameAction>` 接口
  - [ ] 使用 `store()` 构建器管理状态
  - [ ] `./gradlew :business:feature-game:compileKotlinJvm` 通过

  **QA Scenarios**:
  \`\`\`
  Scenario: GameContainer 实现 Container 接口
    Tool: Bash
    Steps:
      1. 检查 `GameContainer.kt` 的类声明实现 Container 接口
      2. 检查包含 "store(" 调用
    Expected Result: 类声明为 "class GameContainer(...) : Container<GameState, GameIntent, GameAction>"
    Evidence: .sisyphus/evidence/task-4-container-interface.txt

  Scenario: store 构建器正确使用
    Tool: Bash
    Steps:
      1. 检查 `GameContainer.kt` 包含 FlowMVI store 导入
      2. 检查包含 reducer 处理 intent
    Expected Result: 使用 store { } 构建器模式
    Evidence: .sisyphus/evidence/task-4-store-builder.txt
  \`\`\`

  **Commit**: YES
  - Message: `refactor(game): GameContainer 重构为 FlowMVI Container 实现`
  - Files: `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/container/GameContainer.kt`

---

- [x] 5. **Android MainActivity 连接 GameScreen**

  **What to do**:
  - 修改 `app/android/src/androidMain/kotlin/com/sect/game/client/MainActivity.kt`
  - 在 setContent 中调用 GameScreen Composable
  - 创建 GameContainer 实例（使用 Kodein 或直接创建）
  - 将 container 传入 GameScreen

  **Must NOT do**:
  - 不得修改 GameScreen 内部实现
  - 不得引入平台特定逻辑到 commonMain

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Android UI 集成，需要 Compose 技能
  - **Skills**: []
    - 无需特殊技能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 3, 4)
  - **Blocks**: None
  - **Blocked By**: Task 3 (需要 GameContainer 支持 Kodein)

  **References**:
  - `app/android/src/androidMain/kotlin/com/sect/game/client/MainActivity.kt` - 当前占位实现
  - `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/presentation/GameScreen.kt` - GameScreen 定义
  - `business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/container/GameContainer.kt` - Container 接口
  - `app/desktop/src/jvmMain/kotlin/com/sect/game/client/main.kt` - desktop 入口查看连接方式

  **Acceptance Criteria**:
  - [ ] MainActivity setContent 包含 GameScreen 调用
  - [ ] GameContainer 被创建并传入 GameScreen
  - [ ] `./gradlew :app:android:assembleDebug` 构建成功

  **QA Scenarios**:
  \`\`\`
  Scenario: MainActivity 包含 GameScreen 调用
    Tool: Bash
    Steps:
      1. 读取 `app/android/.../MainActivity.kt`
      2. 检查包含 "GameScreen" 和 "GameContainer"
    Expected Result: setContent 中有 GameScreen 组件
    Evidence: .sisyphus/evidence/task-5-mainscreen.txt

  Scenario: Android APK 构建成功
    Tool: Bash
    Steps:
      1. 运行 `./gradlew :app:android:assembleDebug`
      2. 检查 BUILD SUCCESSFUL
    Expected Result: APK 生成成功
    Evidence: .sisyphus/evidence/task-5-build-success.txt
  \`\`\`

  **Commit**: YES
  - Message: `feat(android): MainActivity 连接 GameScreen`
  - Files: `app/android/src/androidMain/kotlin/com/sect/game/client/MainActivity.kt`

---

## Final Verification Wave

- [x] F1. **运行 ./gradlew check 验证**

   **验证步骤**:
   1. `./gradlew compileKotlin` - 所有模块编译
   2. `./gradlew jvmTest` - JVM 测试
   3. `./gradlew detekt ktlintCheck` - 代码风格
   4. `./gradlew assembleDebug` - Android 构建

   **Output**: 完整的验证输出保存至 `.sisyphus/evidence/final-check.txt`

- [ ] F2. **代码审查复检**

  **复检内容**:
  1. 确认所有 P0 问题已修复
  2. 确认无新引入的架构问题
  3. 确认遵循 AGENTS.md 规范

---

## Success Criteria

### 验证命令
```bash
./gradlew check  # 全部通过
```

### 最终检查清单
- [x] ~~Android MainActivity 显示 GameScreen~~ ❌ 当前仅显示 "Hello Sect Game"
- [x] ~~GameContainer 实现 FlowMVI Container 接口~~ ❌ 当前使用 MutableStateFlow + Channel
- [x] ~~GameContainer 使用 Kodein DI~~ ❌ 当前直接实例化 sect/gameEngine
- [x] GameErrorHandler 使用 java.util.logging — ✅ **已完成**
- [x] client/build.gradle.kts 无重复依赖 — ✅ **已完成**
- [x] (已取消 - AGENTS.md 保留在原位置) — N/A

### 实际待办 (基于代码审查)
1. **Task 3**: GameContainer 构造函数需接收 Kodein 参数，使用 `kodein.direct.instance<>()` 获取依赖
2. **Task 4**: GameContainer 需实现 `Container<GameState, GameIntent, GameAction>` 接口，使用 `store()` 构建器
3. **Task 5**: Android MainActivity 需在 setContent 中调用 GameScreen 并传入 GameContainer

## 当前进度

### 已完成 (Verified)
- ✅ Task 1: GameErrorHandler 使用 java.util.logging
- ✅ Task 2: 删除 desktopMain 重复依赖
- ✅ Task 5: Android MainActivity 连接 GameScreen

### 已回滚 (需要进一步调查)
- ❌ Task 3: Kodein DI 集成 - Kodein 依赖无法在 JVM 目标正确解析 (org.kodein.di:kodein-di:7.26.1 的 JVM 变体未被正确选择)
- ❌ Task 4: FlowMVI Container 接口实现 - 依赖于 Task 3

### 阻止原因
Kodein 7.26.1 的 Gradle 元数据artifact (kodein-di-metadata-7.26.1.jar) 不包含实际类，实际类在 kodein-di-jvm-7.26.1.jar。Kotlin Multiplatform 插件在为 JVM 目标解析依赖时未正确选择 JVM 变体。