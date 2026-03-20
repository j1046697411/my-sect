# app 模块重构：Kotlin Multiplatform 转为纯 Android 和纯 JVM

## TL;DR

> **快速总结**：将 `app/android` 从 Kotlin Multiplatform 改为纯 Android 库，将 `app/desktop` 从 Kotlin Multiplatform 改为纯 JVM 桌面应用。
> 
> **交付物**：
> - 改写 `app/android/build.gradle.kts` 使用标准 Android 库配置
> - 改写 `app/desktop/build.gradle.kts` 使用纯 Kotlin JVM 配置
> - 调整源码目录结构以适配新的构建配置
> 
> **预计工作量**：小
> **并行执行**：是 - 两个模块可以并行修改
> **关键路径**：Task 1 → Task 2 → 验证

---

## Context

### 原始需求
用户要求将 `app` 目录下的模块改为"纯 Android 的子项目和纯 Java 的模块"，即：
- `app:android` - 纯 Android 库（不使用 Kotlin Multiplatform）
- `app:desktop` - 纯 JVM 应用（不使用 Kotlin Multiplatform）

### 访谈总结
**关键讨论**：
- 用户明确确认：移除 kotlinMultiplatform，使用标准 android-library 和 kotlin-jvm 插件

**研究结果**：
- 当前 `app:android` 使用 `kotlinMultiplatform` + `androidTarget()` + `androidLibrary` 插件
- 当前 `app:desktop` 使用 `kotlinMultiplatform` + `jvm("desktop")` + `composeMultiplatform`
- 两种配置都依赖其他 business 模块

---

## Work Objectives

### 核心目标
将 app 下的两个 Kotlin Multiplatform 模块重构为单一平台的纯 Android 和纯 JVM 模块。

### 具体交付物
- `app/android/build.gradle.kts` - 纯 Android 库配置
- `app/desktop/build.gradle.kts` - 纯 JVM 桌面应用配置
- 适配新的源码目录结构

### 定义完成
- [ ] `./gradlew :app:android:assemble` 成功
- [ ] `./gradlew :app:desktop:run` 成功

### 必须有
- Android 模块保留 Android 特定功能
- Desktop 模块保留 Compose 桌面应用入口
- 依赖的 business 模块保持不变

### 必须没有（Guardrails）
- 禁止在 app:android 中使用 multiplatform 插件
- 禁止在 app:desktop 中使用 multiplatform 插件
- 禁止修改 business 模块

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: YES
- **Automated tests**: Tests-after
- **Framework**: kotlin-test (via Gradle)
- **Agent-Executed QA**: 每项任务后通过 `./gradlew` 命令验证构建

### QA Policy
每项任务必须包含 agent-executed QA 场景。验证通过运行 Gradle 命令完成。

---

## Execution Strategy

### 并行执行 Waves

```
Wave 1 (立即开始 — 独立任务可并行):
├── Task 1: 修改 app:android build.gradle.kts [独立]
└── Task 2: 修改 app:desktop build.gradle.kts [独立]

Wave FINAL (所有任务后 — 验证):
└── Task F1: 构建验证
```

### Dependency Matrix
- **1, 2**: 互不依赖，可并行执行
- **F1**: 依赖 Task 1 和 Task 2

---

## TODOs

- [x] 1. 修改 app:android build.gradle.kts - 移除 Multiplatform，使用纯 Android 库配置

  **What to do**:
  - 移除 `kotlinMultiplatform` 插件
  - 添加 `kotlinAndroid` 插件（如果尚未存在）
  - 将 `kotlin { androidTarget() }` 块替换为标准的 Android sourceSets 配置
  - 保留 `androidLibrary` 插件和 Android 特定配置（namespace, compileSdk, minSdk）
  - 将 `androidMain` 源集重命名为 `main`（因为不再是 multiplatform）
  - 保持对 `business:data` 模块的依赖

  **Must NOT do**:
  - 不要移除 Android 特定配置
  - 不要修改 business 模块的依赖

  **Recommended Agent Profile**:
  > - **Category**: `quick`
    - Reason: 简单的配置文件修改，模式清晰
  > - **Skills**: []
    - 无需特殊技能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 2)
  - **Blocks**: Task F1
  - **Blocked By**: None (can start immediately)

  **References**:

  **Pattern References** (existing code to follow):
  - `app/android/build.gradle.kts` - 当前配置，需要简化

  **Test References**:
  - `./gradlew :app:android:assemble` - Android 模块构建命令

  **Acceptance Criteria**:
  - [ ] `app/android/build.gradle.kts` 不再包含 `kotlinMultiplatform`
  - [ ] 包含 `androidLibrary` 插件
  - [ ] sourceSets 使用 `main` 而非 `androidMain`
  - [ ] `./gradlew :app:android:assemble` 成功

  **QA Scenarios**:

  ```
  Scenario: Android 模块构建成功
    Tool: Bash
    Preconditions: 干净 Gradle 缓存
    Steps:
      1. cd /Users/yoca-676/.local/share/opencode/worktree/2d17054cedea0482513410afd9e40b9ed72b3fe7/quick-falcon
      2. ./gradlew :app:android:assemble --no-daemon
    Expected Result: BUILD SUCCESSFUL
    Failure Indicators: BUILD FAILED 或编译错误
    Evidence: .sisyphus/evidence/task-1-build-success.log
  ```

  **Commit**: YES
  - Message: `refactor(app): convert android module from kmp to pure android`
  - Files: `app/android/build.gradle.kts`
  - Pre-commit: `./gradlew :app:android:assemble`

---

- [x] 2. 修改 app:desktop build.gradle.kts - 移除 Multiplatform，使用纯 JVM 配置

  **What to do**:
  - 移除 `kotlinMultiplatform` 插件
  - 添加 `kotlinJvm` 插件
  - 添加 `application` 插件（桌面应用需要）
  - 将 `kotlin { jvm("desktop") }` 块替换为标准的 JVM sourceSets 配置
  - 将 `desktopMain` 源集重命名为 `main`（因为不再是 multiplatform）
  - 移除 `composeMultiplatform` 和 `composeCompiler` 插件，使用 `compose` 插件替代
  - 保留 `composeHotReload`（如果仍需要热重载）
  - 配置 `mainClass` 用于桌面应用
  - 保持对 `business:presentation` 和 `business:feature-game` 模块的依赖

  **Must NOT do**:
  - 不要移除 Compose 相关依赖
  - 不要修改 business 模块的依赖

  **Recommended Agent Profile**:
  > - **Category**: `quick`
    - Reason: 简单的配置文件修改，模式清晰
  > - **Skills**: []
    - 无需特殊技能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 1)
  - **Blocks**: Task F1
  - **Blocked By**: None (can start immediately)

  **References**:

  **Pattern References** (existing code to follow):
  - `app/desktop/build.gradle.kts` - 当前配置，需要简化

  **Test References**:
  - `./gradlew :app:desktop:run` - Desktop 模块运行命令

  **Acceptance Criteria**:
  - [ ] `app/desktop/build.gradle.kts` 不再包含 `kotlinMultiplatform`
  - [ ] 包含 `kotlinJvm` 和 `application` 插件
  - [ ] sourceSets 使用 `main` 而非 `desktopMain`
  - [ ] 配置了 `mainClass`
  - [ ] `./gradlew :app:desktop:run` 成功

  **QA Scenarios**:

  ```
  Scenario: Desktop 模块编译成功
    Tool: Bash
    Preconditions: 干净 Gradle 缓存
    Steps:
      1. cd /Users/yoca-676/.local/share/opencode/worktree/2d17054cedea0482513410afd9e40b9ed72b3fe7/quick-falcon
      2. ./gradlew :app:desktop:compileKotlinDesktop --no-daemon
    Expected Result: BUILD SUCCESSFUL
    Failure Indicators: BUILD FAILED 或编译错误
    Evidence: .sisyphus/evidence/task-2-compile-success.log

  Scenario: Desktop 模块运行成功（快速验证）
    Tool: Bash
    Preconditions: Task 2 编译成功
    Steps:
      1. cd /Users/yoca-676/.local/share/opencode/worktree/2d17054cedea0482513410afd9e40b9ed72b3fe7/quick-falcon
      2. timeout 30 ./gradlew :app:desktop:run --no-daemon || true
    Expected Result: 应用启动（timeout 后正常退出）
    Failure Indicators: 启动失败或崩溃
    Evidence: .sisyphus/evidence/task-2-run-success.log
  ```

  **Commit**: YES
  - Message: `refactor(app): convert desktop module from kmp to pure jvm`
  - Files: `app/desktop/build.gradle.kts`
  - Pre-commit: `./gradlew :app:desktop:compileKotlinDesktop`

---

## Final Verification Wave

- [x] F1. **构建验证** — `quick`
  验证两个模块都能成功构建。
  Output: `Android [PASS/FAIL] | Desktop [PASS/FAIL] | VERDICT`

---

## Commit Strategy

- **1**: `refactor(app): convert android module from kmp to pure android` — `app/android/build.gradle.kts`
- **2**: `refactor(app): convert desktop module from kmp to pure jvm` — `app/desktop/build.gradle.kts`

---

## Success Criteria

### Verification Commands
```bash
./gradlew :app:android:assemble  # Expected: BUILD SUCCESSFUL
./gradlew :app:desktop:run       # Expected: 应用启动成功
```

### Final Checklist
- [x] `app/android/build.gradle.kts` 不包含 `kotlinMultiplatform`
- [x] `app/desktop/build.gradle.kts` 不包含 `kotlinMultiplatform`
- [x] 两个模块构建成功
- [ ] Desktop 应用能正常启动

