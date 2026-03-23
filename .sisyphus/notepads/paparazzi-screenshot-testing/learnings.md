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

## 问题3 (2026-03-23): Paparazzi 1.3.4/1.3.5 与 compileSdk 36 不兼容

**现象**:
- 使用 Paparazzi 1.3.4 时，运行 screenshot 测试失败
- 错误信息: `java.util.NoSuchElementException: Array contains no element matching the predicate at app.cash.paparazzi.internal.Renderer.configureBuildProperties(Renderer.kt:211)`
- 后续测试失败: `kotlin.UninitializedPropertyAccessException: lateinit property sessionParamsBuilder has not been initialized`

**根本原因**:
- Paparazzi 1.3.4/1.3.5 存在已知 bug，与 compileSdk 36 (Android 16) 不兼容
- 详见 GitHub Issue: https://github.com/cashapp/paparazzi/issues/1877
- 问题出在 Paparazzi 尝试通过反射访问 `android.os._Original_Build.VERSION_CODES_FULL` 类，但 Android 16 中该类不存在

**解决方案**:
- 升级到 Paparazzi 2.0.0-alpha02 或更高版本
- 修改 `gradle/libs.versions.toml` 中的 `paparazzi` 版本: `paparazzi = "2.0.0-alpha02"`
- Paparazzi 2.x 可以正常工作，但需要 Java 21 环境（当前环境为 Java 17，但 Gradle 守护进程使用 Java 21）

**验证结果**:
- `./gradlew :client:recordPaparazziDebug --tests "*PaparazziTest*"` ✅ 成功
- `./gradlew :client:verifyPaparazziDebug --tests "*PaparazziTest*"` ✅ 成功
- 截图已生成在 `client/build/reports/paparazzi/debug/images/` 目录

**相关文件变更**:
- `gradle/libs.versions.toml`: `paparazzi = "1.3.4"` → `paparazzi = "2.0.0-alpha02"`
- `client/build.gradle.kts`: 添加 `alias(libs.plugins.paparazzi)`
- `gradle.properties`: 添加 `paparazzi.sdk.dir=/Users/yoca-676/Library/Android/sdk`（用于备用配置）

**注意**:
- Paparazzi 2.0.0-alpha02 官方声明需要 Java 21，但实际在当前环境（Java 17）可正常运行
- 后续应关注 Paparazzi 稳定版发布，适时升级
