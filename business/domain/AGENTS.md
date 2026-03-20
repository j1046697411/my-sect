# AGENTS.md - business/domain 模块

继承自父模块：[父模块文档](../AGENTS.md)

## OVERVIEW

领域模型层，定义宗门经营游戏的核心业务实体、值对象和异常体系。

## WHERE TO LOOK

```
business/domain/src/commonMain/kotlin/com/sect/game/domain/
├── entity/                    # 实体
│   ├── Disciple.kt           # 弟子实体（修炼、休息、突破）
│   ├── Sect.kt               # 宗门实体（弟子管理、资源消耗）
│   └── Resources.kt          # 资源值对象（灵石、药材、丹药）
├── valueobject/              # 值对象
│   ├── Attributes.kt         # 弟子属性（灵根、资质、气运）
│   ├── Realm.kt              # 境界枚举（炼气→化神）
│   └── Identifiers.kt        # ID值类（DiscipleId, SectId）
└── exception/                # 异常体系
    ├── DomainException.kt    # 领域异常根接口
    ├── CultivationException.kt  # 修炼相关异常
    └── SectException.kt      # 宗门相关异常
```

## CONVENTIONS

### 实体与值对象
- 实体使用 `data class`，包含业务约束校验（`init` 块中的 `require`）
- 值对象使用 `@JvmInline value class` 包装ID类型
- 所有实体构造通过 `companion object.create()` 返回 `Result<T>`

### 异常处理
- 实现 `DomainException` sealed 接口
- 每个异常包含 `message`（技术日志）和 `userMessage`（用户提示）
- 使用 `Result.runCatching` 包装可能失败的操作

### 命名规范
- 实体：`PascalCase`（如 `Disciple`、`Sect`）
- 值对象属性：`camelCase`
- 枚举成员：`PascalCase`

### 返回值规范
- 业务操作返回 `Result<T>`，调用方负责处理失败情况
- 查询方法直接返回具体类型或可空类型

## 核心实体

| 实体 | 职责 | 关键方法 |
|------|------|----------|
| Disciple | 弟子修炼 | `cultivate()`、`rest()`、`attemptBreakthrough()` |
| Sect | 宗门管理 | `addDisciple()`、`removeDisciple()`、`spendResources()` |
| Resources | 资源管理 | `isAffordable()`、`+`、`-` |

## 值对象

| 值对象 | 说明 |
|--------|------|
| Attributes | 灵根(1-100)、资质(1-100)、气运(1-100) |
| Realm | 炼气→筑基→金丹→元婴→化神 |
| DiscipleId/SectId | 内联值类，封装String类型 |
