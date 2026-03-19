# GOAP AI 系统知识库

**类型**: GOAP (Goal-Oriented Action Planning) 核心实现
**范围**: `client/src/commonMain/kotlin/com/sect/game/goap/`

---

## OVERVIEW

自研 GOAP AI 系统，用于控制弟子的自主决策。每个弟子根据当前状态选择最优行动序列。

## 模块结构

```
goap/
├── core/           # 核心抽象（WorldState, Condition, Effect）
├── actions/        # 行动定义（5个基础行动）
├── goals/         # 目标定义
├── planner/       # A* 规划器
├── registry/      # 行动/目标注册表
└── executor/      # 行动执行器
```

## 核心类型

| 类型 | 文件 | 用途 |
|------|------|------|
| `WorldState` | core/WorldState.kt | Map 存储，不可变 |
| `Condition` | core/Condition.kt | AND/OR/NOT 组合 |
| `Effect` | core/Effect.kt | 链式效果 |
| `Action` | actions/Action.kt | 接口：前置条件+效果+成本 |
| `Goal` | goals/Goal.kt | sealed interface |
| `AStarPlanner` | planner/AStarPlanner.kt | A* 算法 |

## 行动包

| 行动 | ID | 前置条件 | 效果 |
|------|-----|---------|------|
| 修炼 | `cultivate` | fatigue < 80, health > 20 | progress +10~20, fatigue +8 |
| 休息 | `rest` | health > 0 | fatigue -15~25, health +5~10 |
| 突破 | `breakthrough` | progress >= 100 | 境界提升（概率） |
| 采集 | `gather` | health > 30 | 资源 +5 |
| 炼丹 | `refine_pill` | 拥有药材 | 丹药效果 |

## 规划流程

```kotlin
val actions = registry.getAllActions()
val goal = factory.selectGoal(state, goals)
val plan = planner.plan(state, goal.targetState, actions)
plan.forEach { executor.execute(disciple, it) }
```

## 关键设计

1. **不可变 WorldState**: 状态修改返回新实例
2. **A* 启发式**: `distanceTo()` 计算状态差异
3. **条件组合**: AND/OR/NOT 逻辑
