# 游戏引擎知识库

**类型**: GOAP 执行引擎
**范围**: `client/src/commonMain/kotlin/com/sect/game/engine/`

---

## OVERVIEW

GOAP 系统的执行层，负责运行规划器生成的行动序列。

## 模块结构

```
engine/
├── executor/        # 行动执行器
├── planner/         # A* 规划器
│   └── AStarPlanner.kt
├── registry/        # 行动/目标注册表
│   └── ActionRegistry.kt
└── GameEngine.kt    # 游戏引擎主类
```

## 核心组件

| 组件 | 职责 |
|------|------|
| `AStarPlanner` | 使用 A* 算法规划行动序列 |
| `ActionRegistry` | 管理所有可用行动和目标 |
| `ActionExecutor` | 执行单个行动，更新弟子状态 |
| `GameEngine` | 协调规划器和执行器 |

## 规划流程

```kotlin
val actions = registry.getAllActions()
val goal = factory.selectGoal(state, goals)
val plan = planner.plan(state, goal.targetState, actions)
plan.forEach { executor.execute(disciple, it) }
```

## A* 规划器

启发式函数 `distanceTo()` 计算当前状态到目标状态的差异：

```kotlin
fun distanceTo(target: WorldState): Int {
    // 计算需要改变的属性数量
}
```

## 注册表模式

行动通过包（Package）批量注册：

```kotlin
val package = CultivationActionPackage()
registry.register(package)
```
