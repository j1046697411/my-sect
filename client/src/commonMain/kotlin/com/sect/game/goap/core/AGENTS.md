# GOAP 核心模块

**类型**: GOAP 核心抽象
**范围**: `client/src/commonMain/kotlin/com/sect/game/goap/core/`

---

## OVERVIEW

GOAP 系统的基础类型：WorldState、Condition、Effect。

## 文件列表

| 文件 | 用途 |
|------|------|
| `WorldState.kt` | 状态表示（Map<String, Int>） |
| `Condition.kt` | 条件谓词（AND/OR/NOT） |
| `Effect.kt` | 状态变更效果 |

## WorldState

使用 `Map<String, Int>` 存储动态属性，**不可变**：

```kotlin
val state = WorldState()
    .withValue("health", 80)
    .withValue("fatigue", 30)
val newState = state.withValue("health", 100)  // 返回新实例
```

## Condition

可组合的谓词逻辑：

```kotlin
Condition.greaterThan("fatigue", 80)
Condition.lessThan("health", 50)
Condition.equals("realm", 2)

// 组合
Condition.and(c1, c2)
Condition.or(c1, c2)
c1.invert()  // NOT

// Always/Never（设计模式）
Condition.Always
Condition.Never
```

## Effect

链式效果处理器：

```kotlin
Effect("health", 10)       // 增加
Effect("fatigue", -15)     // 减少
Effect("cultivationProgress", 20)
```

## 关键设计

1. **不可变 WorldState**: 所有修改返回新实例
2. **Condition 可组合**: 支持复杂布尔逻辑
3. **无副作用**: Effect 仅描述变更，执行由 Action 负责
