# FlowMVI 模块化架构迁移计划

## TL;DR

> **目标**: 将自定义 MVI 架构迁移到 FlowMVI 3.2.0 框架，并建立模块化 Feature-First 架构
>
> **修改文件**: 
> - `gradle/libs.versions.toml` - 添加 flowmvi 3.2.0 依赖
> - `client/build.gradle.kts` - 添加 flowmvi-core 和 flowmvi-compose
> - `stability_definitions.txt` - Compose 稳定性配置
> - `AGENTS.md` - 更新架构规范文档
> - 重构 `mvi/` 目录为 `feature/game/` 模块化结构
> - `GameContract.kt` - 实现 MVIState/MVIIntent/MVIAction 接口
> - `GameContainer.kt` - 使用 store() 构建器
> - `GameReducer.kt` - Reducer 逻辑分离
> - `GameScreen.kt` - 使用 subscribe/intent API
> - `main.kt` - 适配新的 Container 接口

---

## Context

### 原请求
用户指出当前项目的 MVI 架构是自定义实现的，不符合需求。要求：
1. 使用 FlowMVI 框架
2. 考虑后期扩展，按模块实现

### FlowMVI 模块化架构（基于官方示例）

参考 [FlowMVI 官方示例项目](https://github.com/respawn-app/FlowMVI/tree/master/sample) 的最佳实践：

```
src/commonMain/kotlin/com/sect/game/
├── mvi/                              # MVI 基础设施层
│   ├── base/                         # 基础抽象
│   │   └── MviContract.kt
│   └── extensions/                  # FlowMVI 集成扩展
│       └── FlowMviExt.kt
│
├── feature/                          # 功能模块（可独立演进）
│   ├── game/                         # 游戏主模块
│   │   ├── contract/                # 契约
│   │   │   └── GameContract.kt
│   │   ├── container/               # 容器
│   │   │   ├── GameContainer.kt
│   │   │   └── GameReducer.kt
│   │   └── presentation/            # 界面
│   │       └── GameScreen.kt
│   │
│   ├── disciple/                     # 弟子模块（后期扩展）
│   │   └── ...
│   │
│   └── sect/                        # 宗门模块（后期扩展）
│       └── ...
│
└── presentation/                    # 共享 UI 组件
    └── common/
        └── CreateDiscipleDialog.kt
```

---

## Work Objectives

### 依赖配置

**文件**: `gradle/libs.versions.toml`

在 `[versions]` 添加:
```toml
flowmvi = "3.2.0"
```

在 `[libraries]` 添加:
```toml
flowmvi-core = { module = "pro.respawn.flowmvi:core", version.ref = "flowmvi" }
flowmvi-compose = { module = "pro.respawn.flowmvi:compose", version.ref = "flowmvi" }
```

**文件**: `client/build.gradle.kts`

`commonMain` dependencies 添加:
```kotlin
implementation(libs.flowmvi.core)
implementation(libs.flowmvi.compose)
```

`desktopMain` dependencies 添加:
```kotlin
implementation(libs.flowmvi.compose)
```

**文件**: `stability_definitions.txt` (项目根目录)

```
pro.respawn.flowmvi.api.MVIIntent
pro.respawn.flowmvi.api.MVIState
pro.respawn.flowmvi.api.MVIAction
pro.respawn.flowmvi.api.Store
pro.respawn.flowmvi.api.Container
pro.respawn.flowmvi.api.ImmutableStore
pro.respawn.flowmvi.dsl.LambdaIntent
pro.respawn.flowmvi.api.SubscriberLifecycle
pro.respawn.flowmvi.api.IntentReceiver
```

`client/build.gradle.kts` 添加 compose compiler 配置:
```kotlin
compose.compiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("stability_definitions.txt"))
}
```

---

### 目录重构

1. 创建 `client/src/commonMain/kotlin/com/sect/game/feature/game/contract/` 目录
2. 创建 `client/src/commonMain/kotlin/com/sect/game/feature/game/container/` 目录
3. 创建 `client/src/commonMain/kotlin/com/sect/game/feature/game/presentation/` 目录
4. 移动 `GameScreen.kt` 到 `feature/game/presentation/`
5. 删除旧 `mvi/GameContract.kt`、`GameContainer.kt`

---

### 1. 创建 GameContract.kt

**文件**: `client/src/commonMain/kotlin/com/sect/game/feature/game/contract/GameContract.kt`

```kotlin
package com.sect.game.feature.game.contract

import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.entity.Resources
import com.sect.game.domain.valueobject.Attributes
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

/**
 * 游戏模块契约定义
 * 
 * State - UI 显示的状态
 * Intent - 用户操作或系统事件
 * Action - 副作用（如弹窗、导航）
 */
data class GameState(
    val sectName: String = "青云宗",
    val resources: Resources = Resources.EMPTY,
    val disciples: List<Disciple> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) : MVIState

sealed interface GameIntent : MVIIntent {
    data object LoadGame : GameIntent
    data class CreateDisciple(val name: String, val attributes: Attributes) : GameIntent
    data class RemoveDisciple(val discipleId: String) : GameIntent
    data class SelectDisciple(val discipleId: String) : GameIntent
}

sealed interface GameAction : MVIAction {
    data class ShowError(val message: String) : GameAction
    data class ShowSuccess(val message: String) : GameAction
    data object NavigateToDiscipleDetail : GameAction
}
```

---

### 2. 创建 GameReducer.kt

**文件**: `client/src/commonMain/kotlin/com/sect/game/feature/game/container/GameReducer.kt`

```kotlin
package com.sect.game.feature.game.container

import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.entity.Sect
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.SectId
import com.sect.game.feature.game.contract.GameAction
import com.sect.game.feature.game.contract.GameIntent
import com.sect.game.feature.game.contract.GameState
import com.sect.game.mvi.GameErrorHandler
import com.sect.game.mvi.toUserMessage
import pro.respawn.flowmvi.dsl.PipelineContext

/**
 * 游戏模块 Reducer - 处理 Intent 并更新 State
 * 
 * 业务逻辑分离，便于测试和复用
 */
object GameReducer {

    context(PipelineContext<GameState, GameIntent, GameAction>)
    suspend fun reduce(intent: GameIntent, sect: Sect?) {
        when (intent) {
            is GameIntent.LoadGame -> loadGame()
            is GameIntent.CreateDisciple -> createDisciple(intent.name, intent.attributes, sect)
            is GameIntent.RemoveDisciple -> removeDisciple(intent.discipleId, sect)
            is GameIntent.SelectDisciple -> selectDisciple()
        }
    }

    context(PipelineContext<GameState, GameIntent, GameAction>)
    private fun loadGame() {
        updateState { copy(isLoading = true, error = null) }
        val result = Sect.create(SectId("sect-1"), "青云宗")
        result.onSuccess { newSect ->
            updateState {
                copy(
                    sectName = newSect.name,
                    resources = newSect.resources,
                    disciples = newSect.disciples.values.toList(),
                    isLoading = false
                )
            }
            action(GameAction.ShowSuccess("宗门创建成功"))
        }.onFailure { error ->
            updateState { copy(isLoading = false, error = error.toUserMessage()) }
            action(GameAction.ShowError(error.toUserMessage()))
        }
    }

    context(PipelineContext<GameState, GameIntent, GameAction>)
    private fun createDisciple(name: String, attributes: Attributes, sect: Sect?) {
        if (sect == null) {
            action(GameAction.ShowError("宗门未初始化"))
            return
        }
        val discipleResult = Disciple.create(
            id = DiscipleId("disciple-${System.currentTimeMillis()}"),
            name = name,
            attributes = attributes
        )
        discipleResult.onSuccess { newDisciple ->
            val addResult = sect.addDisciple(newDisciple)
            addResult.onSuccess {
                updateState {
                    copy(
                        sectName = sect.name,
                        resources = sect.resources,
                        disciples = sect.disciples.values.toList()
                    )
                }
                action(GameAction.ShowSuccess("成功招募弟子：${newDisciple.name}"))
            }.onFailure { error ->
                action(GameAction.ShowError(error.toUserMessage()))
            }
        }.onFailure { error ->
            action(GameAction.ShowError(error.toUserMessage()))
        }
    }

    context(PipelineContext<GameState, GameIntent, GameAction>)
    private fun removeDisciple(discipleId: String, sect: Sect?) {
        if (sect == null) {
            action(GameAction.ShowError("宗门未初始化"))
            return
        }
        val id = DiscipleId(discipleId)
        val result = sect.removeDisciple(id)
        result.onSuccess { removed ->
            updateState { copy(disciples = sect.disciples.values.toList()) }
            action(GameAction.ShowSuccess("已删除弟子：${removed.name}"))
        }.onFailure { error ->
            action(GameAction.ShowError(error.toUserMessage()))
        }
    }

    context(PipelineContext<GameState, GameIntent, GameAction>)
    private fun selectDisciple() {
        action(GameAction.NavigateToDiscipleDetail)
    }
}
```

---

### 3. 创建 GameContainer.kt

**文件**: `client/src/commonMain/kotlin/com/sect/game/feature/game/container/GameContainer.kt`

```kotlin
package com.sect.game.feature.game.container

import com.sect.game.domain.entity.Sect
import com.sect.game.feature.game.contract.GameAction
import com.sect.game.feature.game.contract.GameIntent
import com.sect.game.feature.game.contract.GameState
import com.sect.game.mvi.GameErrorHandler
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store

/**
 * 游戏模块 Container
 * 
 * 封装 Store，提供依赖注入点
 * Container 是 FlowMVI 与 UI 层的桥梁
 */
class GameContainer : Container<GameState, GameIntent, GameAction> {

    private var sect: Sect? = null

    override val store = store(initial = GameState()) {
        // 错误恢复插件
        recover { e: Throwable ->
            updateState { copy(isLoading = false, error = e.toUserMessage()) }
            action(GameAction.ShowError(e.toUserMessage()))
            GameErrorHandler.logError(e)
            null
        }

        // 初始化
        init {
            updateState { copy(isLoading = true, error = null) }
            val result = Sect.create(
                com.sect.game.domain.valueobject.SectId("sect-1"),
                "青云宗"
            )
            result.onSuccess { newSect ->
                sect = newSect
                updateState {
                    copy(
                        sectName = newSect.name,
                        resources = newSect.resources,
                        disciples = newSect.disciples.values.toList(),
                        isLoading = false
                    )
                }
                action(GameAction.ShowSuccess("宗门创建成功"))
            }.onFailure { error ->
                updateState { copy(isLoading = false, error = error.toUserMessage()) }
                action(GameAction.ShowError(error.toUserMessage()))
            }
        }

        // 状态更新
        reduce { intent ->
            GameReducer.reduce(intent, sect)
        }
    }
}
```

---

### 4. 创建 GameScreen.kt

**文件**: `client/src/commonMain/kotlin/com/sect/game/feature/game/presentation/GameScreen.kt`

```kotlin
package com.sect.game.feature.game.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.feature.game.contract.GameAction
import com.sect.game.feature.game.contract.GameContainer
import com.sect.game.feature.game.contract.GameIntent
import com.sect.game.feature.game.contract.GameState
import com.sect.game.presentation.DiscipleCard
import com.sect.game.presentation.EmptyContent
import com.sect.game.presentation.ErrorContent
import com.sect.game.presentation.ResourcesBar
import pro.respawn.flowmvi.compose.subscribe
import pro.respawn.flowmvi.api.IntentReceiver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    container: GameContainer,
    onDiscipleClick: (Disciple) -> Unit = {}
) = with(container.store) {
    // 订阅状态和副作用
    val state by subscribe { action ->
        when (action) {
            is GameAction.ShowError -> {
                // 错误处理（可扩展为 Snackbar 等）
            }
            is GameAction.ShowSuccess -> {
                // 成功提示
            }
            is GameAction.NavigateToDiscipleDetail -> {
                // 导航处理
            }
        }
    }

    // 发送初始 Intent
    LaunchedEffect(Unit) {
        intent(GameIntent.LoadGame)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    GameTitle(state)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val newDiscipleName = "弟子${state.disciples.size + 1}"
                    intent(GameIntent.CreateDisciple(newDiscipleName, Attributes.DEFAULT))
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GameContent(state, onDiscipleClick)
        }
    }
}

@Composable
private fun GameTitle(state: GameState) {
    androidx.compose.foundation.layout.Column {
        Text(
            text = state.sectName,
            style = MaterialTheme.typography.titleLarge
        )
        ResourcesBar(resources = state.resources)
    }
}

/**
 * 游戏内容区 - 提取为纯函数，支持预览和测试
 */
@Composable
private fun IntentReceiver<GameIntent>.GameContent(
    state: GameState,
    onDiscipleClick: (Disciple) -> Unit
) {
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        state.error != null -> {
            ErrorContent(
                message = state.error ?: "未知错误",
                onRetry = { intent(GameIntent.LoadGame) }
            )
        }
        state.disciples.isEmpty() -> {
            EmptyContent()
        }
        else -> {
            DiscipleList(
                disciples = state.disciples,
                onDiscipleClick = onDiscipleClick
            )
        }
    }
}

@Composable
private fun DiscipleList(
    disciples: List<Disciple>,
    onDiscipleClick: (Disciple) -> Unit
) {
    androidx.compose.foundation.lazy.LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.foundation.lazy.items(
            items = disciples,
            key = { it.id.value }
        ) { disciple ->
            DiscipleCard(
                disciple = disciple,
                onClick = { onDiscipleClick(disciple) }
            )
        }
    }
}
```

---

### 5. 更新 CreateDiscipleDialog.kt

**文件**: `client/src/commonMain/kotlin/com/sect/game/presentation/common/CreateDiscipleDialog.kt`

移动到 `presentation/common/` 目录，并更新导入:

```kotlin
import com.sect.game.feature.game.contract.GameContainer
import com.sect.game.feature.game.contract.GameIntent
```

更新调用:
```kotlin
// container.processIntent(GameIntent.CreateDisciple(name.trim(), attributes))
// 改为
// intent(GameIntent.CreateDisciple(name.trim(), attributes))
```

---

### 6. 更新 main.kt

**文件**: `client/src/desktopMain/kotlin/com/sect/game/client/main.kt`

```kotlin
package com.sect.game.client

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sect.game.feature.game.contract.GameAction
import com.sect.game.feature.game.contract.GameContainer
import com.sect.game.feature.game.presentation.GameScreen
import com.sect.game.presentation.theme.SectTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun main() = application {
    val container = GameContainer()
    val mainScope = MainScope()

    Window(onCloseRequest = ::exitApplication) {
        SectTheme {
            GameScreen(container = container)
        }
    }

    mainScope.launch {
        container.store.subscribe { action ->
            when (action) {
                is GameAction.ShowError -> {
                    println("错误: ${action.message}")
                }
                is GameAction.ShowSuccess -> {
                    println("提示: ${action.message}")
                }
                is GameAction.NavigateToDiscipleDetail -> {
                    println("导航到弟子详情")
                }
            }
        }.collectLatest { }
    }
}
```

---

### 7. 更新 AGENTS.md

**文件**: `AGENTS.md`

在"架构规范"部分添加 FlowMVI 架构说明：

```markdown
### FlowMVI 架构

本项目使用 **FlowMVI** 框架实现 MVI（Model-View-Intent）架构。

#### 核心概念

| 概念 | 说明 | 实现 |
|------|------|------|
| **State** | UI 显示的状态 | 实现 `MVIState` 接口 |
| **Intent** | 用户操作或系统事件 | 实现 `MVIIntent` 接口 |
| **Action** | 副作用（如弹窗、导航） | 实现 `MVIAction` 接口 |
| **Store** | 处理 Intent 并更新 State | 使用 `store()` 构建器 |
| **Container** | Store 的包装器，提供依赖注入 | 实现 `Container` 接口 |

#### 项目模块结构

```
client/src/commonMain/kotlin/com/sect/game/
├── mvi/                              # MVI 基础设施层
│   ├── base/                         # 基础抽象
│   │   └── MviContract.kt
│   └── extensions/                   # FlowMVI 集成扩展
│       └── FlowMviExt.kt
│
├── feature/                          # 功能模块（可独立演进）
│   ├── game/                         # 游戏主模块
│   │   ├── contract/                # 契约（State/Intent/Action）
│   │   │   └── GameContract.kt
│   │   ├── container/               # 容器
│   │   │   ├── GameContainer.kt
│   │   │   └── GameReducer.kt
│   │   └── presentation/            # 界面
│   │       └── GameScreen.kt
│   │
│   ├── disciple/                     # 弟子模块（后期扩展）
│   │   └── ...
│   │
│   └── sect/                        # 宗门模块（后期扩展）
│       └── ...
│
└── presentation/                     # 共享 UI 组件
    └── common/
        └── CreateDiscipleDialog.kt
```

...（完整内容见 AGENTS.md 文件）
```

---

### 8. 删除旧文件

- `client/src/commonMain/kotlin/com/sect/game/mvi/GameContract.kt`
- `client/src/commonMain/kotlin/com/sect/game/mvi/GameContainer.kt`
- `client/src/commonMain/kotlin/com/sect/game/mvi/GameErrorHandler.kt` (保留，用于错误处理)

---

## Verification Strategy

### QA Scenarios

**Scenario 1: 编译验证**
- Tool: Bash
- Command: `./gradlew :client:compileKotlinDesktop --no-configuration-cache`
- Expected: 编译成功，无错误

**Scenario 2: 桌面端运行验证**
- Tool: Bash
- Command: `./gradlew :client:desktopRun`
- Expected: 窗口显示宗门名称"青云宗"和资源

**Scenario 3: 创建弟子功能验证**
- 在桌面端点击 FAB 按钮
- Expected: 成功创建弟子并显示在列表中

---

## Execution Strategy

### Wave 1 (基础配置)
1. 添加 FlowMVI 依赖到 `libs.versions.toml`
2. 更新 `client/build.gradle.kts` 添加依赖和 compose compiler 配置
3. 创建 `stability_definitions.txt`
4. 更新 `AGENTS.md` 架构规范

### Wave 2 (目录重构)
5. 创建 `feature/game/` 目录结构
6. 创建 `GameContract.kt`
7. 创建 `GameReducer.kt`
8. 创建 `GameContainer.kt`

### Wave 3 (UI 层)
9. 移动并更新 `GameScreen.kt` 到 `feature/game/presentation/`
10. 移动 `CreateDiscipleDialog.kt` 到 `presentation/common/`
11. 更新 `main.kt`

### Wave 4 (清理)
12. 删除旧的 `mvi/GameContract.kt` 和 `GameContainer.kt`
13. 编译验证

---

## Success Criteria

- [x] FlowMVI 3.2.0 依赖正确添加 — ✅ `client/build.gradle.kts:49-50`
- [x] `stability_definitions.txt` 配置完成 — ✅ `client/build.gradle.kts:96-106`
- [x] `AGENTS.md` 架构规范已更新 — ✅
- [x] `GameContract` 实现 MVIState/MVIIntent/MVIAction 接口 — ✅ `GameContract.kt:6-8,26,42,49`
- [ ] `GameContainer` 使用 store() 构建器 — ❌ **未实现**（使用 MutableStateFlow + Channel）
- [ ] `GameReducer` 逻辑分离 — ❌ **不存在**
- [ ] `GameScreen` 使用 subscribe() 和 intent() — ❌ **未实现**（使用 `container.state.collectAsState()` 和 `container.processIntent()`）
- [x] 编译通过 — ✅
- [ ] 桌面端运行正常 — ⏳ 待验证

### 实际待办
1. **GameReducer**: 需要创建独立的 Reducer 文件，分离 intent 处理逻辑
2. **GameContainer 重构**: 需要使用 `store()` 构建器实现 `Container<GameState, GameIntent, GameAction>` 接口
3. **GameScreen 更新**: 需要使用 FlowMVI 的 `subscribe()` API 和 `intent()` 发送意图
4. **桌面端验证**: 需要运行 `./gradlew :client:desktopRun` 验证
