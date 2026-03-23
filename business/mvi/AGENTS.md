# AGENTS.md - MVI 基础设施模块

继承自父模块：`../AGENTS.md`

## OVERVIEW

FlowMVI 框架的薄封装层，提供错误处理和 Flow 扩展函数。

## WHERE TO LOOK

```
business/mvi/src/commonMain/kotlin/com/sect/game/mvi/
├── base/
│   └── MviContract.kt          # MVI 核心接口（MVIState/MVIIntent/MVIAction）
├── extensions/
│   └── FlowMviExt.kt           # StateFlow + Result 扩展函数
└── GameErrorHandler.kt         # 统一错误处理（重试 + 用户消息映射）
```

## 核心接口

### MVIState / MVIIntent / MVIAction

| 接口 | 用途 | 标记方式 |
|------|------|----------|
| MVIState | UI 状态 | `data class X : MVIState` |
| MVIIntent | 用户意图 | `sealed interface X : MVIIntent` |
| MVIAction | 副作用 | `sealed interface X : MVIAction` |

## GameErrorHandler

错误处理单例，提供：
- `executeWithRetry()` - 带重试的 suspend 操作（默认3次，100ms间隔）
- `mapToUserMessage()` - 异常 → 用户友好消息
- `logError()` - 结构化日志记录

**已处理异常类型**：DomainException、CultivationException、SectException、StorageException、GoapException

## FlowMviExt.kt 扩展

| 函数 | 说明 |
|------|------|
| `StateFlow.updateStateOf { }` | 安全更新状态 |
| `StateFlow.subscribeTo { }` | 状态流转换 |
| `Result.toUserMessage()` | 结果转用户消息 |

## 使用示例

```kotlin
// 定义状态
data class GameState(val count: Int = 0) : MVIState

// 定义意图
sealed interface GameIntent : MVIIntent {
    data object Increment : GameIntent
}

// 定义副作用
sealed interface GameAction : MVIAction {
    data class ShowError(val message: String) : GameAction
}

// 使用错误处理
suspend fun loadData(): Result<Data> = 
    GameErrorHandler.executeWithRetry { repository.fetch() }
```

详见父模块文档：`../AGENTS.md`
