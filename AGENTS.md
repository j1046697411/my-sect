# AGENTS.md - AI 智能体开发指南

这是一个使用 Gradle 构建的 Kotlin Multiplatform 项目，采用 Compose 作为 UI 框架。

## 构建命令

### Gradle Wrapper
```bash
./gradlew <任务>
```

### 构建任务
| 命令 | 描述 |
|------|------|
| `./gradlew assemble` | 构建所有目标 |
| `./gradlew assembleDebug` | 构建调试版 APK/AAR |
| `./gradlew build` | 完整构建（含检查）|
| `./gradlew compileKotlin` | 编译 Kotlin 源码 |
| `./gradlew compileKotlinJvm` | 编译 JVM 目标 |
| `./gradlew compileKotlinAndroid` | 编译 Android 目标 |

### 运行应用
| 命令 | 描述 |
|------|------|
| `./gradlew run` | 运行桌面应用 |
| `./gradlew installDebug` | 安装 Android 调试版 APK |

### 测试
| 命令 | 描述 |
|------|------|
| `./gradlew test` | 运行所有测试 |
| `./gradlew test --tests "com.sect.game.*"` | 运行匹配模式的测试 |
| `./gradlew test --tests "com.sect.game.client.MyTest"` | 运行单个测试类 |
| `./gradlew test --tests "com.sect.game.client.MyTest.testMethod"` | 运行单个测试方法 |
| `./gradlew jvmTest` | 仅运行 JVM 测试 |
| `./gradlew check` | 运行所有检查（测试、lint 等）|

### 代码质量
| 命令 | 描述 |
|------|------|
| `./gradlew detekt` | 静态代码分析（已配置自定义规则）|
| `./gradlew koverReport` | 生成覆盖率报告 |

### 清理和重新构建
| 命令 | 描述 |
|------|------|
| `./gradlew clean` | 清理构建输出 |
| `./gradlew clean build` | 清理并重新构建 |

---

## 代码风格规范

### Kotlin 约定

**命名规范**
- 类/对象：`PascalCase`（如 `GameEngine`、`PlayerController`）
- 函数/属性：`camelCase`（如 `updatePosition`、`isGameOver`）
- 常量：`UPPER_SNAKE_CASE`（如 `MAX_PLAYERS`、`DEFAULT_SPEED`）
- 包名：小写（如 `com.sect.game.client`）

**导入**
- 按字母顺序排序，分组：stdlib → 外部库 → 内部代码
- 使用显式导入（禁止通配符 `.*`）

**格式化**
- 4 空格缩进（禁止 tab）
- 最大行长度：120 字符
- 顶层声明之间单空行
- 适当使用表达式体函数

```kotlin
// Good
fun isValid() = state.isActive && !state.isPaused

// Good
fun calculateScore(): Int {
    return baseScore * multiplier
}
```

### 类型系统
- 优先使用不可变数据：`val` 优于 `var`
- 使用 `data class` 表示值对象
- 避免可空类型（`?`），除非必要
- 使用 `sealed class` 实现穷尽的 when 表达式

### 函数
- 保持函数短小（< 30 行）
- 单一职责原则
- 使用默认参数代替重载

### 集合
- 使用 `listOf()`、`setOf()`、`mapOf()` 表示不可变集合
- 仅在需要修改时使用 `mutableListOf()`
- 优先使用函数式操作：`map`、`filter`、`reduce`

### 错误处理
- 绝不静默忽略异常
- 使用 `Result<T>` 处理可恢复错误
- 记录详细的错误上下文
- 在 UI 层提供用户友好的错误消息

### 依赖注入
- 使用 Kodein 进行 DI（项目已配置）
- 遵循构造函数注入模式

### Compose 规范
- 使用 `remember` 管理 UI 状态
- 使用 `rememberSaveable` 持久化状态
- 避免在状态中存储可变对象
- 将超过 30 行的 composable 提取为独立组件

### 架构
- Compose 应用遵循 MVVM 模式
- 分离 UI、业务逻辑和数据层
- 使用仓储模式抽象数据访问
- 将平台特定代码放在对应的源集中

### FlowMVI 架构

本项目使用 **FlowMVI** 框架实现 MVI（Model-View-Intent）架构。

#### 核心概念

| 概念 | 说明 | 实现 |
|------|------|------|
| **State** | UI 显示的状态 | 实现 `MVIState` 接口 |
| **Intent** | 用户操作或系统事件 | 实现 `MVIIntent` 接口 |
| **Action** | 副作用（如弹窗、导航） | 实现 `MVIAction` 接口 |
| **Store** | 处理 Intent 并更新 State | 使用 `store()` 构建器 |
| **Container** | Store 的包装器，提供依赖注入 | 实现 `Container` 接口 |

#### 项目模块结构

```
client/src/commonMain/kotlin/com/sect/game/
├── mvi/                              # MVI 基础设施层
│   ├── base/                         # 基础抽象
│   │   └── MviContract.kt
│   └── extensions/                   # FlowMVI 集成扩展
│       └── FlowMviExt.kt
│
├── feature/                          # 功能模块（可独立演进）
│   ├── game/                         # 游戏主模块
│   │   ├── contract/                # 契约（State/Intent/Action）
│   │   │   └── GameContract.kt
│   │   ├── container/               # 容器
│   │   │   ├── GameContainer.kt
│   │   │   └── GameReducer.kt
│   │   └── presentation/            # 界面
│   │       └── GameScreen.kt
│   │
│   ├── disciple/                     # 弟子模块（后期扩展）
│   │   └── ...
│   │
│   └── sect/                        # 宗门模块（后期扩展）
│       └── ...
│
└── presentation/                     # 共享 UI 组件
    └── common/
        └── CreateDiscipleDialog.kt
```

### 测试
- 最低 80% 测试覆盖率
- 单元测试使用 `kotlin-test` 框架
- 测试命名：`类名_方法名_场景_预期结果`
- 遵循 TDD：Red → Green → Refactor

---

## 项目结构

```
client/
├── src/
│   ├── androidMain/       # Android 特定代码
│   │   └── kotlin/
│   ├── desktopMain/      # 桌面端特定代码
│   │   └── kotlin/
│   ├── commonMain/       # 共享代码
│   │   └── kotlin/
│   └── commonTest/       # 共享测试
│       └── kotlin/
```

---

## 依赖管理

**强制规则：所有依赖必须写入 `@gradle/libs.versions.toml`**
- 禁止在 `build.gradle.kts` 中使用内联版本号（如 `implementation("org.example:lib:1.0")`）
- 必须通过版本目录引用：`implementation(libs.library.name)`
- 插件同理：使用 `alias(libs.plugins.plugin.name)` 而非内联版本

**版本目录结构：**
```toml
[versions]    # 版本号
[libraries]   # 库依赖
[plugins]     # 插件
```

---

## 依赖版本

- Kotlin 2.2.0
- Kotlin Coroutines 1.10.2
- Kotlin Serialization 1.8.0
- Jetpack Compose 1.11.0-alpha04
- Kodein DI 7.26.1
- FlowMVI 3.2.0

---

## 禁止模式

**类型安全**
- 禁止 `as Any`、强制解包 `!!`
- 禁止 `@Suppress` 抑制警告

**错误处理**
- 禁止空 catch 块 `catch { }`
- 禁止使用 `println` 输出错误（应用日志框架）

**导入**
- 禁止通配符导入 `.*`

**测试**
- 禁止删除测试使 CI 通过

**文档和注释**
- 所有文档（README、*.md）和代码注释必须使用中文
- 禁止在代码或文档中出现英文注释

---

## Git 规范

**提交规则**
- 所有任务完成后必须通过 `git commit` 提交到本地仓库
- 提交前确保代码通过 `./gradlew check`
- 提交信息应清晰描述本次修改的内容和目的

---

## 技术债务

| 问题 | 状态 | 说明 |
|------|------|------|
| UI 连接 GOAP/MVI | ✅ | GameContainer 集成 GameEngine，UI 显示暂停/恢复/停止控制 |
| ktlint | ✅ | 已配置，ktlintCheck 通过 |

---

## 注意事项

- 这是一个面向 JVM 和 Android 的 Kotlin Multiplatform 项目
- 使用阿里云镜像加速国内依赖下载
- Android minSdk: 24，targetSdk: 36
- 构建需要 Java 17
