# Sect Game 项目配置

## 项目概述

| 配置项 | 值 |
|--------|-----|
| **项目名称** | sect-game |
| **项目类型** | Kotlin Multiplatform |
| **目标平台** | JVM (桌面)、Android |
| **UI 框架** | Jetpack Compose |
| **构建系统** | Gradle 8.13.2 |
| **Java 版本** | 17 |

## 核心版本

| 依赖 | 版本 |
|------|------|
| Kotlin | 2.3.10 |
| kotlinx-coroutines | 1.10.2 |
| kotlinx-serialization | 1.10.0 |
| Jetpack Compose (BOM) | 1.10.1 |
| Kodein DI | 10.1.0 |
| Android Gradle Plugin | 8.13.2 |

## Android 配置

- **minSdk**: 24
- **targetSdk**: 36
- **namespace**: com.sect.game.client

## 项目结构

```
client/
├── src/
│   ├── commonMain/      # 共享代码 (Kotlin + Compose)
│   │   └── kotlin/com/sect/game/client/
│   ├── androidMain/     # Android 特定代码
│   │   └── kotlin/com/sect/game/client/
│   ├── desktopMain/     # 桌面端特定代码
│   │   └── kotlin/com/sect/game/client/
│   └── commonTest/      # 共享测试
│       └── kotlin/
```

## 构建命令

| 命令 | 描述 |
|------|------|
| `./gradlew run` | 运行桌面应用 |
| `./gradlew assembleDebug` | 构建 Android 调试版 APK |
| `./gradlew installDebug` | 安装 Android 调试版 APK |
| `./gradlew test` | 运行所有测试 |
| `./gradlew ktlintCheck` | 运行 Kotlin linter |
| `./gradlew ktlintFormat` | 自动修复 lint 问题 |
| `./gradlew clean build` | 清理并重新构建 |

## 依赖配置

**核心依赖**：
- `kotlinx-coroutines-core` - 协程支持
- `kotlinx-coroutines-android` - Android 协程
- `kotlinx-serialization-json` - JSON 序列化
- `compose.material3` - Material 3 UI 组件

**测试框架**：
- `kotlin-test` - Kotlin 内置测试框架

## 代码规范

**命名**：
- 类/对象：`PascalCase`
- 函数/属性：`camelCase`
- 常量：`UPPER_SNAKE_CASE`
- 包名：小写

**格式化**：
- 4 空格缩进（无 tab）
- 最大行长度：120 字符
- 函数保持 < 30 行
- 文件保持 < 800 行

**架构**：
- MVVM 模式
- 仓库模式数据访问
- 依赖注入使用 Kodein

## 测试要求

- 最低测试覆盖率：80%
- 使用 TDD 流程：Red → Green → Refactor
- 测试命名：`类名_方法名_场景_预期结果`

## 镜像配置

使用阿里云 Maven 镜像加速下载：
- https://maven.aliyun.com/repository/public
- https://maven.aliyun.com/repository/google
- https://maven.aliyun.com/repository/central
- https://maven.aliyun.com/repository/gradle-plugin

## 构建优化

**gradle.properties 配置**：
```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8 -XX:+UseParallelGC
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
```
