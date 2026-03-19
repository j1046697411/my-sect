# GOAP 行动模块

**类型**: GOAP 行动定义
**范围**: `client/src/commonMain/kotlin/com/sect/game/goap/actions/`

---

## OVERVIEW

定义弟子可执行的所有行动（Action），包括前置条件、效果和成本。

## 文件列表

| 文件 | 用途 |
|------|------|
| `Action.kt` | Action 接口定义 |
| `BaseAction.kt` | Action 基类（builder 模式） |
| `CultivateAction.kt` | 修炼行动 |
| `RestAction.kt` | 休息行动 |
| `BreakthroughAction.kt` | 突破行动 |
| `GatherAction.kt` | 采集行动 |
| `RefinePillAction.kt` | 炼丹行动 |

## Action 接口

```kotlin
interface Action {
    val id: String
    val name: String
    val cost: Int
    val preconditions: Set<Condition>
    val effects: Set<Effect>
    fun execute(state: WorldState): WorldState
}
```

## BaseAction builder 模式

```kotlin
val cultivateAction = BaseAction.builder("cultivate", "修炼", cost = 1)
    .withPreconditions(setOf(
        Condition.greaterThan("fatigue", 80).invert(),
        Condition.greaterThan("health", 20)
    ))
    .withEffects(setOf(
        Effect("cultivationProgress", 15),
        Effect("fatigue", 8)
    ))
    .build()
```

## 行动注册

通过 `CultivationActionPackage` 批量注册：
```kotlin
val package = CultivationActionPackage()
registry.register(package)
```

## 关键约束

- `execute()` **不应直接修改输入状态**（返回新实例）
- Action id 和 name **必须非空**
