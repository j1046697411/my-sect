# AGENTS.md - business/engine 模块

游戏引擎核心模块，负责游戏循环驱动和 GOAP 规划。

## OVERVIEW

驱动游戏主循环，通过 GOAP 规划为弟子选择行动并执行。

## WHERE TO LOOK

核心文件：
- `GameEngine.kt` - 游戏引擎，135 行
- `planner/GOAPPlanner.kt` - A* 规划器，125 行
- `registry/ActionRegistry.kt` - 行动注册表
- `executor/ActionExecutor.kt` - 行动执行器

## 核心组件

### GameEngine
游戏主循环控制器。基于 tickRate（默认 60 TPS）驱动游戏更新：
- `start()` / `pause()` / `resume()` / `stop()` 控制生命周期
- `tick()` 每帧调用 `updateDisciples()` 更新所有弟子
- 为每个弟子选择最高优先级未满足的目标，通过 GOAPPlanner 规划行动序列
- `onTick` 回调用于 UI 层监听游戏进度

### GOAPPlanner (AStarPlanner)
A* 路径规划算法实现，用于为弟子规划行动序列：
- `maxSearchDepth = 10` 限制规划深度
- `maxSearchNodes = 500` 限制搜索节点数
- 使用 `PriorityQueue` 按预估总成本优先扩展节点
- `calculateHeuristic()` 计算启发值（未满足条件数量）
- 返回满足目标的最优行动序列

### ActionRegistry
行动和目标的注册表：
- 管理所有可用的 `Action` 和 `Goal`
- `DefaultActionRegistry` 使用可变列表存储
- `createDefaultRegistry()` 自动注册 `CultivationActionPackage` 和所有目标

### ActionExecutor
行动效果执行器：
- `execute()` 遍历行动的所有效果并应用
- `applyEffect()` 根据 effect.key 应用属性修改（health/fatigue/cultivationProgress/realm/lifespan）
- `realm` 突破时重置修炼进度为 0

## CONVENTIONS

- 模块内共享代码放在 `commonMain`
- GameEngine 通过工厂方法 `create()` 构建，默认注册修仙行动包
- GOAPPlanner 接口允许替换不同的规划算法实现
- ActionExecutor 处理属性边界（coerceIn/coerceAtLeast）

## 依赖关系

```
GameEngine
├── GOAPPlanner (规划)
├── ActionRegistry (行动/目标存储)
├── ActionExecutor (执行效果)
└── Sect (领域实体)
```

详见父模块文档：`../AGENTS.md`
