# 领域层知识库

**类型**: DDD 领域模型
**范围**: `client/src/commonMain/kotlin/com/sect/game/domain/`

---

## OVERVIEW

游戏核心业务逻辑，包括弟子、宗门实体和修仙相关值对象。

## 模块结构

```
domain/
├── entity/              # 实体
│   ├── Disciple.kt      # 弟子（核心实体）
│   └── Sect.kt          # 宗门（聚合根）
├── valueobject/         # 值对象
│   └── Resources.kt     # 宗门资源
├── exception/          # 领域异常
│   ├── CultivationException.kt
│   └── SectException.kt
├── Attributes.kt        # 资质属性（灵根、资质、气运）
├── Realm.kt            # 境界枚举
├── Identifiers.kt      # ID 值对象
└── SpiritualRoot.kt    # 灵根类型
```

## 核心实体

### Disciple（弟子）
```kotlin
data class Disciple(
    val id: DiscipleId,
    val name: String,
    val realm: Realm,           // 当前境界
    val attributes: Attributes, // 资质属性
    val cultivationProgress: Int = 0,
    val fatigue: Int = 0,
    val health: Int = 100,
    val lifespan: Int = 100,
    val age: Int = 18
)
```

**业务方法**: `cultivate()`, `rest()`, `attemptBreakthrough()`, `ageOneYear()`

### Sect（宗门）
- 聚合根，管理弟子集合
- 资源管理（灵石、药材等）
- 人数上限验证

## 领域异常

| 异常 | 触发条件 |
|------|---------|
| `DeadDiscipleException` | 弟子已死亡时执行操作 |
| `ExhaustedException` | 弟子过度疲劳时修炼 |
| `BreakthroughFailedException` | 突破失败 |

## 关键约束

- 所有业务方法返回 `Result<T>` 处理错误
- 使用 `require()` 在构造函数验证不变量
- 不直接访问 Repository（纯领域逻辑）
