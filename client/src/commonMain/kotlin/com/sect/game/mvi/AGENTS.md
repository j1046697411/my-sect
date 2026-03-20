# MVI 架构知识库

**类型**: MVI (Model-View-Intent) 架构
**范围**: `client/src/commonMain/kotlin/com/sect/game/mvi/`

---

## OVERVIEW

使用 FlowMVI 模式管理游戏状态容器。

## 文件列表

| 文件 | 用途 |
|------|------|
| `GameErrorHandler.kt` | 游戏错误处理器 |

**实际的 MVI 组件位于 `feature/game/` 目录下：**

| 文件 | 位置 |
|------|------|
| `GameContract.kt` | `feature/game/contract/` |
| `GameContainer.kt` | `feature/game/container/` |

## MVI 契约

实际的 MVI 契约定义在 `feature/game/contract/GameContract.kt`，使用 FlowMVI 框架：

```kotlin
// State - 游戏状态
sealed class GameState : MVIState { ... }

// Intent - 用户意图
sealed class GameIntent : MVIIntent { ... }

// Action - 副作用
sealed class GameAction : MVIAction { ... }
```

**注意**: 项目使用 FlowMVI 框架（`fr.nico招拨.FlowMVI`），MVI 基础设施层仅包含 `GameErrorHandler.kt`，未实现自定义的 `MviContract.kt` 基类。

## Container 使用

```kotlin
val container = GameContainer()

// 发送 Intent
container.accept(GameIntent.StartGame("测试宗门"))

// 观察状态
container.state.collect { state ->
    // UI 更新
}

// 发送副作用 Action
container.sendAction(GameAction.ShowSnackbar("成功"))
```

## 数据流

```
用户操作 → Intent → GameContainer
                    ↓
              领域层业务逻辑
                    ↓
              更新 State
                    ↓
              Compose UI 响应
                    ↓
              发送 Action（副作用）
                    ↓
              UI 响应（Snackbar/导航）
```

## 关键约束

- Intent 处理返回 `Result<T>`
- State 不可变
- Container 需要 dispose 释放资源
