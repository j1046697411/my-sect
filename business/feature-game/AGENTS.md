# AGENTS.md - 宗门游戏模块

游戏主界面：UI + MVI + GameEngine 集成入口。

## OVERVIEW

宗门经营游戏的核心 UI 模块，基于 FlowMVI 架构连接 GameEngine 与 Compose UI。

## WHERE TO LOOK

```
business/feature-game/src/commonMain/kotlin/com/sect/game/feature/game/
├── contract/
│   └── GameContract.kt      # MVI 契约（State/Intent/Action）
├── container/
│   └── GameContainer.kt     # MVI 容器（174行）
└── presentation/
    ├── GameScreen.kt         # 主界面 Composable（329行）
    └── CreateDiscipleDialog.kt # 招募弟子弹窗
```

## CONVENTIONS

### MVI 模式
- State：GameState 数据类，实现 MVIState
- Intent：GameIntent 密封接口
- Action：GameAction 副作用接口
- Container：通过 processIntent(intent) 处理用户意图

### UI 层
- 使用 Compose Material3 组件
- 状态通过 `container.state.collectAsState()` 收集
- LaunchedEffect(Unit) 在 Composable 首次组合时触发 LoadGame
- 子组件：LoadingContent / ErrorContent / EmptyContent / DiscipleList

### 命名
- Composable 函数使用 PascalCase
- 私有组件以 Content/Screen/Bar 等后缀结尾
- 事件处理：`container.processIntent(GameIntent.XXX)`

## GameContract.kt

```kotlin
data class GameState(
    val sectName: String = "青云宗",
    val resources: Resources = Resources.EMPTY,
    val disciples: List<Disciple> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val tickCount: Long = 0,
    val isPaused: Boolean = false,
    val selectedDiscipleId: String? = null,
) : MVIState

sealed interface GameIntent : MVIIntent {
    data object LoadGame : GameIntent
    data class CreateDisciple(val name: String, val attributes: Attributes) : GameIntent
    data class RemoveDisciple(val discipleId: String) : GameIntent
    data class SelectDisciple(val discipleId: String) : GameIntent
    data object PauseGame / ResumeGame / StopGame : GameIntent
}

sealed interface GameAction : MVIAction {
    data class ShowError/ShowSuccess(val message: String) : GameAction
    data object NavigateToDiscipleDetail : GameAction
}
```

## GameContainer.kt

MVI 容器，整合 GameEngine：
- `processIntent(intent)` 分发用户意图
- `loadGame()` 创建 Sect 和 GameEngine
- `onTick` 回调更新 tickCount 和 disciples 列表
- `pauseGame/resumeGame/stopGame` 控制游戏循环
- 错误通过 `GameErrorHandler.logError()` 记录

## GameScreen.kt

主界面 Composable（329行）：
- `GameScreen(container, onDiscipleClick)` 顶层入口
- TopAppBar 显示宗门名、资源、Tick 计数、暂停/继续/停止按钮
- FloatingActionButton 招募新弟子
- GameContent 根据状态渲染：加载中/错误/空状态/弟子列表

详情见父级文档：../AGENTS.md
