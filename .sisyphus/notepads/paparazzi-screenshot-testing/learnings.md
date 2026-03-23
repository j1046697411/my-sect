# Paparazzi Screenshot Testing Learnings

## 配置问题

### 问题1: paparazzi { } 配置块在 KMP Android Library 项目中无法解析

**现象**:
- 使用 `alias(libs.plugins.paparazzi)` 应用插件成功
- Paparazzi 任务正常显示 (`verifyPaparazziDebug`, `recordPaparazziRelease` 等)
- 但 `paparazzi { sdkDir = ... }` 配置块报 `Unresolved reference: paparazzi`

**尝试的解决方案**:
1. 使用 `id("app.cash.paparazzi")` 直接引用插件 → 失败，插件未找到
2. 将 paparazzi 插件移到 androidLibrary 之后 → 同样失败
3. 使用 `extensions.configure<paparazzi.PaparazziExtension>` → 失败，类型未找到
4. 使用完全限定名 `extensions.configure<app.cash.paparazzi.PaparazziExtension>` → 失败

**根本原因**:
Paparazzi Gradle 插件在 KMP Android Library 项目中似乎无法正确注册 DSL 扩展。尽管插件本身被应用并且任务被正确创建，但 `paparazzi { }` 配置块语法不被识别。

**当前状态**:
- 插件已添加: `alias(libs.plugins.paparazzi)` (client/build.gradle.kts:11)
- 依赖已添加: `implementation(libs.paparazzi)` (client/build.gradle.kts:65)
- 任务可查看: `./gradlew :client:tasks --group verification | grep -i paparazzi`
- 配置块暂时省略，等待进一步调查

### 问题2: androidTestImplementation 在 commonTest source set 中不可用

**现象**:
- 尝试使用 `androidTestImplementation(libs.paparazzi)` → 失败
- `testImplementation(libs.paparazzi)` → 也不可用

**解决方案**:
- 使用 `implementation(libs.paparazzi)` (line 65)
- `implementation` 在所有 source set 中都可用

## 未来调查方向
1. 检查 Paparazzi 插件版本与 Kotlin 2.2.0 和 Android Gradle Plugin 8.11.2 的兼容性
2. 尝试在 root build.gradle.kts 中使用 `apply false` 声明 paparazzi
3. 研究是否需要在 android {} 块内配置 Paparazzi
