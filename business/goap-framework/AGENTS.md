# AGENTS.md - GOAP Framework 模块

继承自父模块：`../AGENTS.md`

## OVERVIEW

纯接口层，定义 GOAP AI 系统的核心抽象，不包含任何具体实现。

## WHERE TO LOOK

| 文件 | 说明 |
|------|------|
| `goals/Goal.kt` | 目标接口 |
| `core/WorldState.kt` | 世界状态（不可变 map） |
| `core/Condition.kt` | 条件断言（sealed class） |
| `core/Effect.kt` | 效果接口 |
| `core/ModifyEffect.kt` | 数值修改效果实现 |

## 核心接口

### Goal
```kotlin
interface Goal {
    val id: String
    val priority: Int
    val targetConditions: Set<Condition>
    fun isGoalSatisfied(state: WorldState): Boolean
}
```
目标由优先级和目标条件集合组成，`isGoalSatisfied` 判断当前世界状态是否已达成目标。

### WorldState
```kotlin
class WorldState(
    booleans: Map<String, Boolean> = mapOf(),
    ints: Map<String, Int> = mapOf(),
    floats: Map<String, Float> = mapOf(),
)
```
支持 Boolean/Int/Float 三种值类型，每次修改返回新实例。`distanceTo(other)` 用于 A* 规划启发式距离计算。

### Condition (sealed class)
| 子类 | 说明 |
|------|------|
| `GreaterThan/LessThan(key, threshold)` | Int 值比较 |
| `Equals(key, value)` | Int 值等于指定值 |
| `GreaterThanOrEqual/LessThanOrEqual` | 边界比较 |
| `Not(condition)` | 逻辑非 |
| `And(left, right)/Or(left, right)` | 组合条件 |
| `Always/Never` | 恒真/恒假 |

### Effect
```kotlin
interface Effect {
    fun apply(state: WorldState): WorldState
}
```

### ModifyEffect
```kotlin
data class ModifyEffect(val key: String, val delta: Int) : Effect
```
将指定 key 的 Int 值增加 delta（可以为负数）。

## CONVENTIONS

- 所有接口仅定义抽象契约，不包含业务逻辑
- WorldState 保持不可变，每次修改返回新实例
- Condition 使用 sealed class 实现穷尽 when 表达式
- Effect 实现类应放在 core 包下，Goal 实现类放在 goals 包下
