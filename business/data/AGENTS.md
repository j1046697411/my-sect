# AGENTS.md - business/data 模块

游戏数据持久化模块，负责存档存储和 DTO 映射。

## OVERVIEW

数据层模块，提供游戏存档的跨平台存储能力和领域模型与 DTO 之间的映射转换。

## STRUCTURE

```
business/data/src/
├── commonMain/kotlin/com/sect/game/data/
│   ├── storage/          # 存储接口和 JSON 实现
│   ├── dto/              # 数据传输对象
│   └── mapper/           # DTO 与领域模型映射器
├── androidMain/kotlin/  # Android 平台存储实现
├── desktopMain/kotlin/  # Desktop 平台存储实现
└── commonTest/kotlin/   # 共享测试
```

## WHERE TO LOOK

**公共 API：**
- `storage/GameStorage.kt` — 存档存储接口，定义 save/load/delete/gameExists 操作
- `mapper/DiscipleMapper.kt` — 弟子 DTO 映射器（含 AttributesMapper）
- `mapper/SectMapper.kt` — 宗门 DTO 映射器（含 ResourcesMapper）

**DTO：**
- `dto/SectDto.kt` — GameSaveDto、SectDto、DiscipleDto、ResourcesDto、AttributesDto

**平台实现：**
- `storage/JsonGameStorage.kt` — 通用 JSON 序列化存储，使用 expect/actual 模式
- `androidMain/.../AndroidGameStorage.kt` — Android 文件系统存储（Context.filesDir）
- `desktopMain/.../DesktopGameStorage.kt` — Desktop 存储（~/.sect-game/）

## CONVENTIONS

除遵循父文档规范外，额外约定：

- 使用 `expect fun createPlatformStorage()` 声明平台工厂函数
- 平台实现类实现 `PlatformGameStorage` 接口
- `JsonGameStorage` 组合 `PlatformGameStorage` 而非继承
- 存储异常使用 `GameStorageException` 密封类
- DTO 使用 `@Serializable` 注解，支持 Kotlin Serialization

---

参考父文档：`../AGENTS.md`
