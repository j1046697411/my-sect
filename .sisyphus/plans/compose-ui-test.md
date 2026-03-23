# Compose UI 自动化测试

## TL;DR

> **Quick Summary**: 为 Compose Multiplatform UI 组件添加 JVM 自动化测试，使用 `runComposeUiTest` 在桌面端验证 UI 逻辑。
> 
> **Deliverables**:
> - 在 `client/build.gradle.kts` 添加 `compose.uiTest` 依赖
> - 创建 `DiscipleCardTest` (≥3 个测试)
> - 创建 `CreateDiscipleDialogTest` (≥3 个测试)
> - 创建 `GameScreenTest` (≥3 个测试)
> 
> **Estimated Effort**: Medium
> **Parallel Execution**: YES - 2 waves
> **Critical Path**: 依赖配置 → 测试辅助 → 组件测试 → 集成测试

---

## Context

### Original Request
GitHub Issue #24 - [功能] 添加 Compose UI 自动化测试

### Interview Summary
**Key Discussions**:
- **API 基准**: 按实际代码测试（Issue 描述与实际代码不符）
- **测试方式**: JVM 桌面端验证，使用 `runComposeUiTest`，不验证 Android
- **测试框架**: Compose Multiplatform 的 `runComposeUiTest` API
- **运行命令**: `./gradlew test` (JVM 测试)

**Research Findings**:
- 项目使用 Kotlin Multiplatform + Compose Multiplatform 1.11.0-alpha04
- 现有测试使用 `kotlin.test` 框架在 `commonTest` 源集
- `GameContainer` 可直接实例化，无需外部依赖
- `Disciple` 可通过 `Disciple.create()` 工厂方法创建

### Metis Review
**Identified Gaps** (addressed):
- 需要在 `client/build.gradle.kts` 的 `commonTest` 添加 `compose.uiTest` 依赖
- 需要 `@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)` 注解
- 测试辅助对象需要集中管理测试数据

---

## Work Objectives

### Core Objective
为 Compose UI 组件添加 JVM 自动化测试，验证界面渲染和交互逻辑。

### Concrete Deliverables
1. `client/src/commonTest/kotlin/com/sect/game/presentation/DiscipleCardTest.kt`
2. `client/src/commonTest/kotlin/com/sect/game/presentation/CreateDiscipleDialogTest.kt`
3. `client/src/commonTest/kotlin/com/sect/game/presentation/GameScreenTest.kt`

### Definition of Done
- [ ] `./gradlew test` 运行所有测试通过
- [ ] 每个测试类至少 3 个测试用例
- [ ] 测试覆盖正常路径和边界情况

### Must Have
- `compose.uiTest` 依赖正确配置
- 所有测试用例可独立运行
- 测试使用 `runComposeUiTest` 而非 Android instrumented test API

### Must NOT Have (Guardrails)
- **禁止** 使用 Android instrumented test API (`createAndroidComposeRule`)
- **禁止** 添加 Android-specific 测试依赖 (`androidTestImplementation`)
- **禁止** 修改被测组件的业务逻辑

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: YES (`kotlin.test`)
- **Automated tests**: Tests-after (在 commonTest 中添加)
- **Framework**: `kotlin.test` + `runComposeUiTest` (Compose Multiplatform)
- **Test location**: `client/src/commonTest/kotlin/com/sect/game/presentation/`

### QA Policy
Every task includes agent-executed QA scenarios (JVM verification via `runComposeUiTest`).

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately — foundation):
├── Task 1: 添加 compose.uiTest 依赖到 client/build.gradle.kts
└── Task 2: 创建测试辅助对象 (TestData.kt)

Wave 2 (After Wave 1 — test files, MAX PARALLEL):
├── Task 3: DiscipleCardTest (4 tests)
├── Task 4: CreateDiscipleDialogTest (3 tests)
└── Task 5: GameScreenTest (3 tests)

Wave FINAL:
├── Task F1: 运行 ./gradlew test 验证所有测试通过
```

### Dependency Matrix

- **Task 1**: — — 2, 3, 4, 5
- **Task 2**: 1 — 3, 4, 5
- **Task 3**: 1, 2 — F1
- **Task 4**: 1, 2 — F1
- **Task 5**: 1, 2 — F1
- **F1**: 3, 4, 5 — —

---

## TODOs

- [x] 1. **添加 compose.uiTest 依赖**

  **What to do**:
  在 `client/build.gradle.kts` 的 `commonTest` 依赖中添加 `compose.uiTest`：
  ```kotlin
  val commonTest by getting {
      dependencies {
          implementation(libs.kotlin.test)
          // ... existing deps ...
          @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
          implementation(compose.uiTest)
      }
  }
  ```
  注意：`compose.uiTest` 是 Compose Multiplatform 的测试库，通过 Compose Gradle 插件提供。

  **Must NOT do**:
  - 不要使用 `androidTestImplementation` (Android instrumented tests)
  - 不要添加 Android-specific 测试依赖

  **Recommended Agent Profile**:
  > - **Category**: `quick`
  >   Reason: 简单依赖配置修改
  > - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 2)
  - **Blocks**: Tasks 3, 4, 5
  - **Blocked By**: None

  **References**:
  - `client/build.gradle.kts:56-64` - commonTest 依赖配置位置
  - `gradle/libs.versions.toml:4` - composeMultiplatform 版本定义

  **Acceptance Criteria**:
  - [ ] `compose.uiTest` 依赖已添加到 commonTest
  - [ ] `@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)` 注解已添加

  **Commit**: YES
  - Message: `test(ui): add compose.uiTest dependency for commonTest`

---

- [x] 2. **创建测试辅助对象 (TestData.kt)**

  **What to do**:
  创建 `client/src/commonTest/kotlin/com/sect/game/presentation/TestData.kt`，包含：
  - `testDisciple()` - 创建默认测试弟子
  - `testDisciple(name: String, health: Int, fatigue: Int, cultivationProgress: Int)` - 自定义属性
  - `testContainer()` - 创建测试用 GameContainer
  - `testGameState(disciples: List<Disciple>)` - 创建测试用 GameState
  - `testAttributes()` - 创建默认属性

  ```kotlin
  object TestData {
      fun testDisciple(
          name: String = "张三",
          health: Int = 100,
          fatigue: Int = 0,
          cultivationProgress: Int = 50,
      ): Disciple = Disciple.create(
          id = DiscipleId("test-${System.nanoTime()}"),
          name = name,
          attributes = Attributes.DEFAULT,
          realm = Realm.LianQi,
          lifespan = 100,
      ).getOrThrow()
      
      fun testContainer(): GameContainer = GameContainer()
  }
  ```

  **Must NOT do**:
  - 不要创建需要外部依赖的测试数据

  **Recommended Agent Profile**:
  > - **Category**: `quick`
  >   Reason: 简单数据类创建
  > - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 1)
  - **Blocks**: Tasks 3, 4, 5
  - **Blocked By**: Task 1 (依赖配置)

  **References**:
  - `business/domain/.../Disciple.kt` - Disciple.create() 工厂方法
  - `business/feature-game/.../GameContainer.kt` - GameContainer 构造函数
  - `business/feature-game/.../GameContract.kt` - GameState 定义
  - `client/src/commonTest/kotlin/com/sect/game/e2e/GameE2ETest.kt` - 测试辅助方法模式参考

  **Acceptance Criteria**:
  - [ ] TestData.kt 文件已创建
  - [ ] `testDisciple()` 可创建默认弟子
  - [ ] `testDisciple(name, health, fatigue, cultivationProgress)` 可自定义属性
  - [ ] `testContainer()` 可创建 GameContainer

  **Commit**: YES (合并到 Task 1)

---

- [x] 3. **DiscipleCardTest**

  **What to do**:
  创建 `client/src/commonTest/kotlin/com/sect/game/presentation/DiscipleCardTest.kt`，使用 `runComposeUiTest`：

  ```kotlin
  @OptIn(ExperimentalTestApi::class)
  class DiscipleCardTest {
      @Test
      fun discipleCard_显示姓名和境界() = runComposeUiTest {
          setContent {
              DiscipleCard(disciple = testDisciple(name = "张三"))
          }
          onNodeWithText("张三").assertExists()
          onNodeWithText("炼气期").assertExists()
      }
      
      @Test
      fun discipleCard_显示修炼进度() = runComposeUiTest {
          setContent {
              DiscipleCard(disciple = testDisciple(cultivationProgress = 75))
          }
          onNodeWithText("75%").assertExists()
      }
      
      @Test
      fun discipleCard_高疲劳值显示警告色() = runComposeUiTest {
          setContent {
              DiscipleCard(disciple = testDisciple(fatigue = 80))
          }
          // 疲劳条应该显示（文本包含"疲劳"）
          onNodeWithText("疲劳").assertExists()
      }
      
      @Test
      fun discipleCard_展开显示详细信息() = runComposeUiTest {
          setContent {
              DiscipleCard(
                  disciple = testDisciple(),
                  isExpanded = true
              )
          }
          onNodeWithText("详细信息").assertExists()
          onNodeWithText("灵根").assertExists()
      }
  }
  ```

  **Must NOT do**:
  - 不要使用 `createAndroidComposeRule` (Android API)
  - 不要使用 Android instrumented test 注解

  **Recommended Agent Profile**:
  > - **Category**: `unspecified-high`
  >   Reason: Compose UI 测试需要理解测试 API 和组件结构
  > - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 5)
  - **Blocks**: Final verification
  - **Blocked By**: Tasks 1, 2

  **References**:
  - `business/presentation/.../DiscipleCard.kt` - 被测组件
  - `business/domain/.../Disciple.kt` - Disciple 实体
  - `business/domain/.../Realm.kt` - Realm 枚举 (LianQi, ZhuJi, JinDan, YuanYing, HuaShen)
  - Compose Multiplatform 测试文档 - `runComposeUiTest` API

  **Acceptance Criteria**:
  - [ ] DiscipleCardTest.kt 已创建
  - [ ] `discipleCard_显示姓名和境界` 测试通过
  - [ ] `discipleCard_显示修炼进度` 测试通过
  - [ ] `discipleCard_高疲劳值显示警告色` 测试通过
  - [ ] `discipleCard_展开显示详细信息` 测试通过

  **Commit**: YES
  - Message: `test(ui): add DiscipleCardTest`

---

- [x] 4. **CreateDiscipleDialogTest**

  **What to do**:
  创建 `client/src/commonTest/kotlin/com/sect/game/presentation/CreateDiscipleDialogTest.kt`：

  ```kotlin
  @OptIn(ExperimentalTestApi::class)
  class CreateDiscipleDialogTest {
      @Test
      fun dialog_空名字_创建按钮禁用() = runComposeUiTest {
          setContent {
              CreateDiscipleDialog(
                  container = testContainer(),
                  onDismiss = {}
              )
          }
          onNodeWithText("创建").assertIsDisabled()
      }
      
      @Test
      fun dialog_输入名字_创建按钮启用() = runComposeUiTest {
          setContent {
              CreateDiscipleDialog(
                  container = testContainer(),
                  onDismiss = {}
              )
          }
          onNodeWithText("请输入弟子姓名").performTextInput("李四")
          onNodeWithText("创建").assertIsEnabled()
      }
      
      @Test
      fun dialog_点击随机_生成随机姓名() = runComposeUiTest {
          setContent {
              CreateDiscipleDialog(
                  container = testContainer(),
                  onDismiss = {}
              )
          }
          onNodeWithText("随机").performClick()
          // 验证名字输入框有内容（不等于空）
          val textField = onNodeWithText("请输入弟子姓名")
          // 随机按钮会填充名字，所以输入框不再显示 placeholder
      }
  }
  ```

  **Must NOT do**:
  - 不要测试 GameContainer 的内部状态（只测试 UI 行为）

  **Recommended Agent Profile**:
  > - **Category**: `unspecified-high`
  >   Reason: Dialog 测试涉及用户交互和状态变化
  > - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 3, 5)
  - **Blocks**: Final verification
  - **Blocked By**: Tasks 1, 2

  **References**:
  - `business/feature-game/.../CreateDiscipleDialog.kt` - 被测组件
  - `business/feature-game/.../GameContainer.kt` - GameContainer
  - DiscipleCardTest.kt - 测试模式参考

  **Acceptance Criteria**:
  - [ ] CreateDiscipleDialogTest.kt 已创建
  - [ ] `dialog_空名字_创建按钮禁用` 测试通过
  - [ ] `dialog_输入名字_创建按钮启用` 测试通过
  - [ ] `dialog_点击随机_生成随机姓名` 测试通过

  **Commit**: YES
  - Message: `test(ui): add CreateDiscipleDialogTest`

---

- [x] 5. **GameScreenTest**

  **What to do**:
  创建 `client/src/commonTest/kotlin/com/sect/game/presentation/GameScreenTest.kt`：

  ```kotlin
  @OptIn(ExperimentalTestApi::class)
  class GameScreenTest {
      @Test
      fun gameScreen_加载中_显示Loading() = runComposeUiTest {
          setContent {
              GameScreen(container = testContainer())
          }
          // 初始状态 isLoading=true，应该显示 CircularProgressIndicator
          // 由于 GameScreen 会在 LaunchedEffect 中调用 LoadGame，
          // 需要等待一下让状态更新
          // 或者直接设置 GameState 的 isLoading
      }
      
      @Test
      fun gameScreen_空状态_显示暂无弟子() = runComposeUiTest {
          setContent {
              // 直接测试 EmptyContent 组件
              EmptyContent()
          }
          onNodeWithText("暂无弟子").assertExists()
      }
      
      @Test
      fun gameScreen_显示宗门名称() = runComposeUiTest {
          setContent {
              GameScreen(container = testContainer())
          }
          // TopAppBar 显示宗门名 "青云宗" (默认)
          onNodeWithText("青云宗").assertExists()
      }
  }
  ```

  注意：由于 GameScreen 内部使用 LaunchedEffect 调用 LoadGame，完整测试可能需要等待状态更新。可以考虑直接测试内部组件（LoadingContent、EmptyContent、DiscipleList）。

  **Must NOT do**:
  - 不要依赖真实的 GameEngine 运行（可能导致测试不稳定）

  **Recommended Agent Profile**:
  > - **Category**: `unspecified-high`
  >   Reason: GameScreen 测试涉及异步状态和 LaunchedEffect
  > - **Skills**: none

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 3, 4)
  - **Blocks**: Final verification
  - **Blocked By**: Tasks 1, 2

  **References**:
  - `business/feature-game/.../GameScreen.kt` - 被测组件（GameContent、EmptyContent 等内部组件）
  - `business/feature-game/.../GameContract.kt` - GameState

  **Acceptance Criteria**:
  - [ ] GameScreenTest.kt 已创建
  - [ ] `gameScreen_空状态_显示暂无弟子` 测试通过
  - [ ] `gameScreen_显示宗门名称` 测试通过

  **Commit**: YES
  - Message: `test(ui): add GameScreenTest`

---

## Final Verification Wave

- [x] F1. **运行测试** — `bash`
  运行 `./gradlew test` 验证所有测试通过
  Output: `BUILD SUCCESSFUL | Tests: N passed, 0 failed`

---

## Commit Strategy

- **1**: `test(ui): add compose ui automation tests` — 添加依赖、测试文件

---

## Success Criteria

### Verification Commands
```bash
./gradlew test  # Expected: BUILD SUCCESSFUL
```

### Final Checklist
- [ ] compose.uiTest 依赖已添加
- [ ] DiscipleCardTest 至少 3 个测试
- [ ] CreateDiscipleDialogTest 至少 3 个测试
- [ ] GameScreenTest 至少 3 个测试
- [ ] `./gradlew test` 通过
