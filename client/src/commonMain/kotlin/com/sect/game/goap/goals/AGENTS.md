# GOAP 目标模块

**类型**: GOAP 目标定义与工厂
**范围**: `client/src/commonMain/kotlin/com/sect/game/goap/goals/`

---

## OVERVIEW

定义弟子追求的目标（Goal）及其生成工厂（GoalFactory）。

## 文件列表

| 文件 | 用途 |
|------|------|
| `Goal.kt` | sealed interface 定义 |
| `GoalTemplate.kt` | 目标模板实现 |
| `SimpleGoal.kt` | 简单目标 |
| `GoalFactory.kt` | 工厂接口 |
| `GoalFactoryImpl.kt` | 工厂实现 |
| `SurvivalGoal.kt` | 生存目标 |
| `RestGoal.kt` | 休息目标 |
| `CultivationGoal.kt` | 修炼目标 |
| `BreakthroughGoal.kt` | 突破目标 |

## 目标优先级

| 目标 | 优先级 | 触发条件 | 目标状态 |
|------|--------|---------|---------|
| 生存 | 100 | health < 50 | health >= 80 |
| 休息 | 80 | fatigue > 70 | fatigue <= 30 |
| 修炼 | 50 | fatigue < 60, health > 50 | progress += 20 |
| 突破 | 40 | progress >= 100 | realm 提升 |

## 使用模式

```kotlin
val goal = GoalTemplate(
    id = "survival",
    name = "生存",
    priority = 100,
    conditions = setOf(Condition.lessThan("health", 50)),
    targetState = WorldState().withValue("health", 80)
)
```
