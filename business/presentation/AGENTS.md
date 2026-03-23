# AGENTS.md - 共享 UI 组件模块

继承自父模块：`../AGENTS.md`

## OVERVIEW

跨模块复用的 Compose UI 组件和主题系统。

## WHERE TO LOOK

```
business/presentation/src/commonMain/kotlin/com/sect/game/presentation/
├── theme/
│   ├── Theme.kt           # SectTheme（Material3 主题包装）
│   ├── SectColors.kt      # 宗门游戏配色方案
│   └── SectTypography.kt  # 字体排版规范
└── DiscipleCard.kt        # 弟子信息卡片组件
```

## 主题系统

### SectColors
 修仙风格配色

| 颜色 | Light | Dark | 用途 |
|------|-------|------|------|
| JadeGreen | 翡翠绿 | 浅翡翠 | 主色 |
| Gold | 金色 | 浅金 | 次色 |
| NascentSoul | 元婴紫 | 浅紫 | 强调色 |
| SpiritBackground | 灵墟白 | 幽冥灰 | 背景/表面 |
| Health | 健康绿 | - | 状态条 |
| Warning | 警告橙 | - | 状态条 |
| Error | 错误红 | - | 错误状态 |

### SectTypography

使用 Material3 默认字体，通过 `FontWeight` 控制粗细。

### SectTheme

```kotlin
@Composable
fun SectTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
)
```
自动应用配色方案，支持 Light/Dark 主题切换。

## DiscipleCard

弟子信息卡片 Composable：
- 显示：头像、姓名、境界、修炼进度
- 状态条：健康（绿/橙/红）、疲劳（反向颜色）
- 可展开：显示当前动作、详细信息（寿命、灵根、资质、气运）
- 动画：`animateContentSize` 平滑展开/收起

### 使用方式

```kotlin
DiscipleCard(
    disciple = disciple,
    currentAction = "修炼中",
    isExpanded = false,
    onExpandClick = { /* 切换展开状态 */ }
)
```

## 组件规范

- 组件文件用 `PascalCase`（如 `DiscipleCard.kt`）
- 私有辅助函数用 `camelCase`
- 使用 `MaterialTheme.colorScheme` 获取颜色
- 间距使用 4.dp / 8.dp / 12.dp / 16.dp 规范
- 圆角使用 `CircleShape` 或 `MaterialTheme.shapes`

详见父模块文档：`../AGENTS.md`
