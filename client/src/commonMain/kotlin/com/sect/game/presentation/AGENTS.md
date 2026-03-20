# 展示层知识库

**类型**: UI 组件层
**范围**: `client/src/commonMain/kotlin/com/sect/game/presentation/`

---

## OVERVIEW

共享 Compose UI 组件，包括对话框、卡片、主题等。

## 模块结构

```
presentation/
├── theme/           # Compose 主题配置
│   ├── Theme.kt
│   ├── Color.kt
│   └── Type.kt
└── common/          # 共享组件
    └── CreateDiscipleDialog.kt
```

## 主题配置

使用 Compose Material3 主题系统：

```kotlin
SectGameTheme {
    // 使用 contentColorFor 获取主题色
    // 支持深色模式自动适配
}
```

## 组件

### CreateDiscipleDialog

宗门创建对话框组件，用于创建新弟子。

## 技术约束

- 使用 `remember` 管理 UI 状态
- 使用 `rememberSaveable` 持久化状态
- 避免在状态中存储可变对象
- 超过 30 行的 Composable 应提取为独立组件
