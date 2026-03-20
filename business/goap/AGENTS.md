# AGENTS.md - GOAP 业务实现模块

继承自父模块：`../AGENTS.md`
依赖框架：`../goap-framework/AGENTS.md`

## OVERVIEW

基于 goap-framework 框架的游戏 AI 实现，提供弟子自动决策的 Actions 和 Goals。

## WHERE TO LOOK

```
business/goap/src/commonMain/kotlin/com/sect/game/
├── goap/
│   ├── actions/
│   │   ├── Action.kt         # Action 接口定义
│   │   ├── BaseAction.kt     # 基础实现 + action DSL 构建器
│   │   ├── GatherAction.kt   # 采集（资源+，疲劳+）
│   │   ├── RestAction.kt     # 休息（疲劳-，健康+）
│   │   ├── AlchemyAction.kt  # 炼丹（资源-，修为+）
│   │   ├── CultivationAction.kt  # 修炼（修为+，疲劳+，健康-）
│   │   └── BreakthroughAction.kt # 突破（境界+，修为-）
│   └── goals/
│       ├── SimpleGoal.kt      # Goal 接口简单实现
│       ├── SurvivalGoal.kt    # 生存目标（健康>=80，优先级100）
│       ├── CultivationGoal.kt # 修炼目标（修为>=100，优先级50）
│       ├── BreakthroughGoal.kt # 突破目标（修为>=100且准备度>80，优先级70）
│       └── RestGoal.kt        # 休息目标（疲劳<20，优先级60）
└── engine/
    └── DiscipleWorldStateConverter.kt  # 领域模型转 GOAP 世界状态
```

## Actions

| Action | ID | Cost | 前置条件 | 效果 |
|--------|-----|------|----------|------|
| GatherAction | gather | 2 | health > 30 | resources +5, fatigue +10 |
| RestAction | rest | 1 | health < 100 OR fatigue > 20 | fatigue -30, health +10 |
| AlchemyAction | alchemy | 3 | resources >= 10 AND health > 50 | resources -10, cultivationProgress +20 |
| CultivateAction | cultivate | 1 | fatigue < 80 | cultivationProgress +10, fatigue +15, health -5 |
| BreakthroughAction | breakthrough | 5 | cultivationProgress >= 100 | realm +1, cultivationProgress -100 |

## Goals

| Goal | ID | Priority | 完成条件 |
|------|-----|----------|----------|
| SurvivalGoal | survival | 100 | health >= 80 |
| RestGoal | rest | 60 | fatigue < 20 |
| CultivationGoal | cultivation | 50 | cultivationProgress >= 100 |
| BreakthroughGoal | breakthrough | 70 | cultivationProgress >= 100 AND readiness > 80 |

## DiscipleWorldStateConverter

将 `Disciple` 领域实体转换为 GOAP `WorldState`，桥接 domain 层与 GOAP AI 层。

转换字段：health, fatigue, cultivationProgress, realm.order, lifespan, readiness（突破准备度）

**readiness 计算公式**：(100 - fatigue) * 0.6 + health * 0.4，限制在 0-100

## CONVENTIONS

- 使用 `action()` DSL 函数构建 Action 实例
- Goal 实现类提供 `create()` 工厂方法和 `isSatisfied()` 静态方法
- 所有数值修改使用 `ModifyEffect`
- 优先级数值越高越优先执行
