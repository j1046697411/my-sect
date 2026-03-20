# 数据层知识库

**类型**: 数据访问层
**范围**: `client/src/commonMain/kotlin/com/sect/game/data/`

---

## OVERVIEW

数据持久化和 DTO 映射，包括游戏存储和跨平台数据访问。

## 模块结构

```
data/
├── mapper/          # DTO 映射器
├── storage/         # 平台存储实现
│   ├── GameStorage.kt
│   ├── DesktopGameStorage.kt
│   └── AndroidGameStorage.kt
└── (DTO classes)
```

## 存储接口

`GameStorage` 是跨平台存储接口：

```kotlin
interface GameStorage {
    suspend fun saveSect(sect: Sect): Result<Unit>
    suspend fun loadSect(id: SectId): Result<Sect>
    suspend fun saveDisciple(disciple: Disciple): Result<Unit>
    suspend fun loadDisciples(sectId: SectId): Result<List<Disciple>>
}
```

## 平台实现

| 平台 | 实现 |
|------|------|
| Desktop | `DesktopGameStorage` |
| Android | `AndroidGameStorage` |

## Mapper 模式

DTO 与领域对象之间的双向转换：

```kotlin
fun Disciple.toDto(): DiscipleDto
fun DiscipleDto.toDomain(): Disciple
```
