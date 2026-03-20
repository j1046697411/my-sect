# 工具项目分离计划

## TL;DR

> **快速总结**: 将 detekt 自定义规则、kover 配置、dokka 配置从 `client` 业务模块分离到独立的 `tools/` 子项目中，实现关注点分离。Client 仍然使用这些工具（插件 applied），但工具代码和配置在独立子项目中。
> 
> **交付物**:
> - `tools/detekt-rules/` - detekt 自定义规则子项目（通过 `detekt(project(":tools:detekt-rules"))` 被 client 使用）
> - `tools/kover-config/` - kover 配置子项目（占位符，未来可扩展）
> - `tools/dokka-config/` - dokka 配置子项目（占位符，未来可扩展）
> - `client/` - 业务模块，工具代码已移除，但保留插件使用配置
> 
> **预计工作量**: Medium
> **并行执行**: YES - 5 waves
> **关键路径**: Task 1 → 2 → 3 → 4 → 5 → 6 → 9 → 10

---

## Context

### 原始请求
用户明确要求：
1. 创建单独的子项目存放工具代码
2. 保留 detekt 自定义规则（NoChineseInTestMethodName）
3. 所有工具（detekt、kover、dokka 等）都应分离到工具项目中
4. 业务模块不应包含工具代码，不应被打包进项目
5. **主项目仍然需要使用这些工具进行检查**（新确认需求）

### 访谈总结
**关键讨论**:
- 项目组织方式: **多个独立子项目** (非单一 tools 目录)
- kover 和 dokka: **创建配置子项目** (非仅移除依赖)
- 工具项目结构: `tools/detekt-rules/`, `tools/kover-config/`, `tools/dokka-config/`
- **工具使用方式**: client 保留插件应用，通过 `detekt(project(...))` 依赖引用 tools 子项目

**研究结果**:
- 当前项目是单一模块 Kotlin Multiplatform 项目
- detekt 自定义规则混在 `client/src/commonMain/kotlin/com/sect/game/detekt/`
- kover 和 dokka 只是插件应用，无自定义规则代码

### Metis 审查
**识别到的缺口** (已解决):
- 服务文件 discovery 问题: 通过配置 build.gradle.kts 生成 META-INF/services
- 依赖方向: client 依赖 tools，不反向
- 构建顺序: Gradle 自动处理 tools 先于 client
- **工具使用问题**: 通过 `detekt { dependencies { detekt(project(...)) } }` 解决

---

## 工作目标

### 核心目标
将工具相关**代码和配置**从业务模块 (`client`) 分离到独立的 Gradle 子项目中，同时保持 client 仍然能够使用这些工具进行检查。

### 具体交付物
- `tools/detekt-rules/` - 独立的 detekt 规则子项目，包含 NoChineseInTestMethodName 规则和 detekt.yml 配置
- `tools/kover-config/` - kover 配置子项目（占位符）
- `tools/dokka-config/` - dokka 配置子项目（占位符）
- `client/` - 业务模块，工具代码已移除，但保留插件使用配置

### 完成定义
- [ ] `tools/detekt-rules/` 包含完整的 detekt 自定义规则
- [ ] `client/src/commonMain/kotlin/com/sect/game/detekt/` 目录已删除
- [ ] `client/src/jvmMain/resources/META-INF/services/` 中的 detekt 服务文件已删除
- [ ] `client/build.gradle.kts` 通过 `detekt { dependencies { detekt(project(...)) } }` 使用 tools/detekt-rules
- [ ] `tools/kover-config/` 和 `tools/dokka-config/` 子项目存在
- [ ] `./gradlew :client:detekt` 成功运行并使用自定义规则
- [ ] `./gradlew build` 成功执行

### 必须有
- NoChineseInTestMethodName 规则功能保持不变
- 工具子项目不被打包进 client 的最终产物
- Gradle 构建顺序正确 (tools 先于 client)
- **Client 仍然能够运行 detekt/kover/dokka 等工具**（通过插件和依赖配置）

### 禁止有 (Guardrails)
- 禁止在迁移过程中修改 detekt 规则逻辑
- 禁止在 client 模块中保留工具代码（自定义规则、服务文件等）
- **禁止在 client 中移除 detekt/kover/dokka 插件配置**（client 仍需使用这些工具）
- 禁止修改 client 业务代码
- 禁止修改 CI/CD 配置（除非构建中断）

---

## 验证策略

### 测试决策
- **基础设施存在**: YES (gradle 项目)
- **自动化测试**: 无 (重构任务，验证构建成功即可)
- **框架**: N/A
- **Agent-Executed QA**: 每次任务后验证构建成功

### QA 策略
每次任务完成后，通过运行 Gradle 命令验证：
- 子项目构建成功
- 依赖正确配置
- 原位置代码已清理

---

## 执行策略

### 并行执行 Waves

```
Wave 1 (立即开始 - 基础结构):
├── Task 1: 初始化 tools 目录结构
├── Task 7: 创建 kover-config 空白子项目
└── Task 8: 创建 dokka-config 空白子项目

Wave 2 (Task 1 完成后):
├── Task 2: 创建 detekt-rules 子项目 build 配置
├── Task 3: 迁移 detekt 规则文件
└── Task 4: 配置 META-INF/service 文件生成

Wave 3 (Wave 2 完成后):
├── Task 5: 更新 client 依赖 tools
└── Task 6: 移除 client 中的旧 detekt 代码

Wave 4 (Wave 3 + Wave 1 完成后):
└── Task 9: 验证完整构建

Wave 5 (Task 9 完成后):
└── Task 10: 执行所有验收标准
```

### 依赖矩阵

| Task | 依赖 | 阻塞 |
|------|------|------|
| 1 | - | 2, 7, 8 |
| 2 | 1 | 3 |
| 3 | 2 | 4 |
| 4 | 3 | 5 |
| 5 | 4 | 6 |
| 6 | 5 | 9 |
| 7 | 1 | 9 |
| 8 | 1 | 9 |
| 9 | 6, 7, 8 | 10 |
| 10 | 9 | - |

---

## TODOs

---

## TODOs

- [ ] 1. **初始化 tools 目录结构**

  **What to do**:
  - 创建 `tools/` 目录
  - 创建 `tools/settings.gradle.kts` 配置子项目
  - 创建 `tools/build.gradle.kts` 根配置
  - 更新根目录 `settings.gradle.kts` include 子项目

  **Must NOT do**:
  - 不要创建具体子项目内容（由后续任务处理）
  - 不要修改 client 模块

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 目录结构和配置文件创建，简单的文件操作
  - **Skills**: [`git-master`]
    - `git-master`: 用于正确的 git 操作和 commit

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 7, 8)
  - **Blocks**: Tasks 2, 7, 8
  - **Blocked By**: None (can start immediately)

  **References**:
  - `settings.gradle.kts:26-28` - 当前的 include 模式
  - `gradle/libs.versions.toml` - 版本管理配置

  **Acceptance Criteria**:
  - [ ] `tools/` 目录已创建
  - [ ] `tools/settings.gradle.kts` 包含 detekt-rules, kover-config, dokka-config
  - [ ] 根 `settings.gradle.kts` include 了 tools 子项目
  - [ ] `./gradlew projects` 显示新的子项目

  **QA Scenarios**:

  ```
  Scenario: 验证 tools 子项目已注册
    Tool: Bash
    Preconditions: settings.gradle.kts 已更新
    Steps:
      1. 运行 ./gradlew projects
      2. 检查输出包含 :tools:detekt-rules, :tools:kover-config, :tools:dokka-config
    Expected Result: 所有 tools 子项目出现在项目列表中
    Evidence: .sisyphus/evidence/task-1-projects-list.txt
  ```

  **Commit**: YES
  - Message: `chore: setup tools directory structure`
  - Files: `settings.gradle.kts`, `tools/settings.gradle.kts`, `tools/build.gradle.kts`
  - Pre-commit: `./gradlew projects`

---

- [ ] 7. **创建 kover-config 空白子项目**

  **What to do**:
  - 创建 `tools/kover-config/` 目录结构
  - 创建 `tools/kover-config/build.gradle.kts` - JVM-only 项目
  
  **kover-config 目录结构**:
  ```
  tools/kover-config/
  ├── build.gradle.kts
  └── src/main/
      ├── kotlin/           # 未来存放自定义 kover 规则（如有）
      └── resources/        # kover 配置文件
          └── kover/
              └── kover.xml  # 覆盖率规则配置（未来）
  ```
  
  **kover-config build.gradle.kts**:
  ```kotlin
  plugins {
      id("java-library")
      alias(libs.plugins.kover)
  }
  
  // 空的 kover 配置占位符
  // 未来可以在这里定义覆盖率规则、排除项等
  kover {
      // 覆盖率配置将放在这里
  }
  ```

  **client 如何使用**:
  - client 保留 `alias(libs.plugins.kover)` 插件
  - 运行 `./gradlew koverXmlReport` 时会自动使用 client 的 kover 配置
  - 如需共享配置，可以在 tools/kover-config 中定义后被 client 引用

  **Must NOT do**:
  - 不要添加复杂的 kover 配置（占位符即可）
  - 不要修改 client 模块

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 创建空白子项目结构和配置文件
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 8)
  - **Blocks**: Task 9
  - **Blocked By**: Task 1

  **References**:
  - `client/build.gradle.kts:12-13` - kover 插件配置参考
  - `gradle/libs.versions.toml:15` - kover 版本

  **Acceptance Criteria**:
  - [ ] `tools/kover-config/build.gradle.kts` 存在且有效
  - [ ] `./gradlew :tools/kover-config:build` 成功

  **QA Scenarios**:

  ```
  Scenario: kover-config 子项目构建成功
    Tool: Bash
    Preconditions: 子项目结构已创建
    Steps:
      1. 运行 ./gradlew :tools:kover-config:build
    Expected Result: 构建成功，输出 "BUILD SUCCESSFUL"
    Evidence: .sisyphus/evidence/task-7-kover-build.txt
  ```

  **Commit**: YES
  - Message: `chore(kover): add tools/kover-config placeholder`
  - Files: `tools/kover-config/`
  - Pre-commit: `./gradlew :tools:kover-config:build`

---

- [ ] 8. **创建 dokka-config 空白子项目**

  **What to do**:
  - 创建 `tools/dokka-config/` 目录结构
  - 创建 `tools/dokka-config/build.gradle.kts` - JVM-only 项目

  **dokka-config 目录结构**:
  ```
  tools/dokka-config/
  ├── build.gradle.kts
  └── src/main/
      ├── kotlin/           # 未来存放自定义 dokka 插件（如有）
      └── resources/        # dokka 配置文件
          └── dokka/
              └── dokka.conf  # 文档生成配置（未来）
  ```

  **dokka-config build.gradle.kts**:
  ```kotlin
  plugins {
      id("java-library")
      alias(libs.plugins.dokka)
  }
  
  // 空的 dokka 配置占位符
  // 未来可以在这里定义文档模板、插件等
  dokkaHtml {
      // 文档配置将放在这里
  }
  ```

  **client 如何使用**:
  - client 保留 `alias(libs.plugins.dokka)` 插件
  - 运行 `./gradlew dokkaHtml` 时会自动使用 client 的 dokka 配置
  - 如需共享配置，可以在 tools/dokka-config 中定义后被 client 引用

  **Must NOT do**:
  - 不要添加复杂的 dokka 配置（占位符即可）
  - 不要修改 client 模块

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 创建空白子项目结构和配置文件
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 7)
  - **Blocks**: Task 9
  - **Blocked By**: Task 1

  **References**:
  - `client/build.gradle.kts:11` - dokka 插件配置参考
  - `gradle/libs.versions.toml:16` - dokka 版本

  **Acceptance Criteria**:
  - [ ] `tools/dokka-config/build.gradle.kts` 存在且有效
  - [ ] `./gradlew :tools:dokka-config:build` 成功

  **QA Scenarios**:

  ```
  Scenario: dokka-config 子项目构建成功
    Tool: Bash
    Preconditions: 子项目结构已创建
    Steps:
      1. 运行 ./gradlew :tools:dokka-config:build
    Expected Result: 构建成功，输出 "BUILD SUCCESSFUL"
    Evidence: .sisyphus/evidence/task-8-dokka-build.txt
  ```

  **Commit**: YES
  - Message: `chore(dokka): add tools/dokka-config placeholder`
  - Files: `tools/dokka-config/`
  - Pre-commit: `./gradlew :tools:dokka-config:build`

---

---

- [x] 2. **创建 detekt-rules 子项目 build 配置**

  **What to do**:
  - 创建 `tools/detekt-rules/build.gradle.kts`
  - 配置 JVM-only 项目（detekt 是 JVM 工具）
  - 添加 detekt-api 依赖
  - 配置 `kotlin {}` jvm target
  - 配置 `java { }` source/target compatibility
  - 配置 META-INF/services 文件生成
  
  **创建 `tools/detekt-rules/detekt.yml`**（从 client/detekt.yml 复制）:
  ```yaml
  config:
    validation: true
    warningsAsErrors: false
    excludes: 'no-chinese-in-test-method.*'

  processors:
    active: true
    exclude:
      - '**/detekt/**/*.class'

  build:
    maxIssues: 200

  output-reports:
    active: true

  no-chinese-in-test-method:
    active: true
    NoChineseInTestMethodName:
      active: true
  ```

  **Must NOT do**:
  - 不要创建规则文件（由 Task 3 处理）
  - 不要修改 client 模块
  - 不要删除 client/detekt.yml（由 Task 6 处理）

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 创建 Gradle 构建配置文件
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 2
  - **Blocks**: Task 3
  - **Blocked By**: Task 1

  **References**:
  - `gradle/libs.versions.toml:2,18,22,23` - detekt 版本和依赖
  - `client/build.gradle.kts:13` - detekt 插件
  - `client/src/jvmMain/resources/META-INF/services/io.gitlab.arturbosch.detekt.api.RuleSetProvider` - 服务文件内容
  - `client/detekt.yml` - 配置内容参考

  **Acceptance Criteria**:
  - [ ] `tools/detekt-rules/build.gradle.kts` 配置了 JVM target
  - [ ] `libs.gitlab.detekt.api` 依赖已添加
  - [ ] META-INF/services 生成已配置
  - [ ] `tools/detekt-rules/detekt.yml` 已创建
  - [ ] `./gradlew :tools:detekt-rules:dependencies` 显示 detekt-api

  **QA Scenarios**:

  ```
  Scenario: detekt-rules 子项目依赖配置正确
    Tool: Bash
    Preconditions: build.gradle.kts 已创建
    Steps:
      1. 运行 ./gradlew :tools:detekt-rules:dependencies --configuration releaseRuntimeClasspath
      2. 检查输出包含 detekt-api
    Expected Result: detekt-api 依赖出现在运行时类路径中
    Evidence: .sisyphus/evidence/task-2-dependencies.txt

  Scenario: detekt.yml 配置文件存在
    Tool: Bash
    Preconditions: detekt.yml 已创建
    Steps:
      1. 检查 tools/detekt-rules/detekt.yml 存在
      2. 检查包含 no-chinese-in-test-method 配置
    Expected Result: 配置文件存在且内容正确
    Evidence: .sisyphus/evidence/task-2-detekt-config.txt
  ```

  **Commit**: NO (与 Task 3 一起 commit)

---

- [x] 3. **迁移 detekt 规则文件**

  **What to do**:
  - 创建 `tools/detekt-rules/src/main/kotlin/com/sect/game/tools/detekt/` 目录
  - 复制 `client/src/commonMain/kotlin/com/sect/game/detekt/NoChineseInTestMethodName.kt` 到新位置
  - 复制 `client/src/commonMain/kotlin/com/sect/game/detekt/NoChineseInTestMethodNameRuleSetProvider.kt` 到新位置
  - 更新 package 声明为 `com.sect.game.tools.detekt`
  - 更新 import 路径

  **Must NOT do**:
  - 不要修改规则逻辑，只更新 package 和 import
  - 不要删除 client 中的原文件（由 Task 6 处理）

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 文件复制和 package 更新
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 2
  - **Blocks**: Task 4
  - **Blocked By**: Task 2

  **References**:
  - `client/src/commonMain/kotlin/com/sect/game/detekt/NoChineseInTestMethodName.kt:1-49` - 源文件内容
  - `client/src/commonMain/kotlin/com/sect/game/detekt/NoChineseInTestMethodNameRuleSetProvider.kt:1-17` - 源文件内容

  **Acceptance Criteria**:
  - [ ] 文件已复制到 `tools/detekt-rules/src/main/kotlin/com/sect/game/tools/detekt/`
  - [ ] package 声明已更新
  - [ ] import 路径已更新
  - [ ] `./gradlew :tools:detekt-rules:compileKotlin` 成功

  **QA Scenarios**:

  ```
  Scenario: detekt-rules 编译成功
    Tool: Bash
    Preconditions: 规则文件已迁移
    Steps:
      1. 运行 ./gradlew :tools:detekt-rules:compileKotlin
    Expected Result: 编译成功，无错误
    Evidence: .sisyphus/evidence/task-3-compile.txt
  ```

  **Commit**: NO (与 Task 2 一起 commit)

---

- [x] 4. **配置 META-INF/service 文件生成**

  **What to do**:
  - 在 `tools/detekt-rules/src/main/resources/META-INF/services/` 创建服务文件
  - 文件名: `io.gitlab.arturbosch.detekt.api.RuleSetProvider`
  - 内容: `com.sect.game.tools.detekt.NoChineseInTestMethodNameRuleSetProvider`
  - 配置 `build.gradle.kts` 确保资源文件被打包进 JAR

  **Must NOT do**:
  - 不要修改规则逻辑
  - 不要在 client 中保留服务文件

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 创建服务配置文件
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 2
  - **Blocks**: Task 5
  - **Blocked By**: Task 3

  **References**:
  - `client/src/jvmMain/resources/META-INF/services/io.gitlab.arturbosch.detekt.api.RuleSetProvider` - 原服务文件

  **Acceptance Criteria**:
  - [ ] 服务文件存在于正确位置
  - [ ] JAR 文件包含 META-INF/services/
  - [ ] `jar tf :tools:detekt-rules:jar` 显示服务文件

  **QA Scenarios**:

  ```
  Scenario: 服务文件已打包进 JAR
    Tool: Bash
    Preconditions: 服务文件已创建
    Steps:
      1. 运行 ./gradlew :tools:detekt-rules:jar
      2. 运行 jar tf $(./gradlew :tools:detekt-rules:jar --dry-run | grep -o 'build/libs/[^ ]*\.jar' | head -1)
      3. 检查输出包含 META-INF/services/io.gitlab.arturbosch.detekt.api.RuleSetProvider
    Expected Result: JAR 中包含服务文件
    Evidence: .sisyphus/evidence/task-4-jar-contents.txt
  ```

  **Commit**: NO (与 Task 5 一起 commit)

---

---

- [x] 5. **更新 client 依赖 tools 子项目**

  **What to do**:
  
  **5.1 更新 `client/build.gradle.kts`**:
  
  ```kotlin
  plugins {
      alias(libs.plugins.detekt)    // 保留 detekt 插件
      alias(libs.plugins.kover)    // 保留 kover 插件
      alias(libs.plugins.dokka)    // 保留 dokka 插件
      // ... 其他插件
  }
  
  // detekt 配置 - 指向 tools 子项目的配置
  detekt {
      buildUponDefaultConfig = true
      config.setFrom(files("../tools/detekt-rules/detekt.yml"))  // 共享配置
      source.setFrom(files("src/commonMain/kotlin", "src/commonTest/kotlin"))
      
      // 关键：将规则子项目添加到 detekt classpath，这样自定义规则才能被发现
      dependencies {
          detekt(project(":tools:detekt-rules"))
      }
  }
  
  kotlin {
      sourceSets {
          val commonMain by getting {
              dependencies {
                  // 移除 libs.gitlab.detekt.api（规则已在 tools/detekt-rules 中）
                  // 保留其他业务依赖...
              }
          }
      }
  }
  ```
  
  **5.2 保留 kover 和 dokka 插件配置**:
  - kover 和 dokka 插件保留在 client 中（用于运行检查）
  - 如果有自定义配置需求，可以通过 `kover {}` 或 `dokka {}` 块指定配置来源
  
  **5.3 更新 `tools/detekt-rules/build.gradle.kts`**（在 Task 2 中创建）:
  ```kotlin
  plugins {
      id("java-library")
      alias(libs.plugins.detekt)
  }
  
  // 将 detekt.yml 复制或创建到 tools/detekt-rules/detekt.yml
  // 这样 client 可以通过 config.setFrom() 引用
  
  // META-INF/services 文件已配置（在 Task 4 中）
  ```
  
  **5.4 移除旧代码**:
  - 从 client 的 `commonMain` 移除 `libs.gitlab.detekt.api` 依赖
  - `client/detekt.yml` 可以删除（配置移到 tools/detekt-rules/）
  - 或保留在 client 中作为本地覆盖

  **Must NOT do**:
  - 不要删除 detekt/kover/dokka 插件（client 仍需使用这些工具）
  - 不要移除 `detekt {}` 配置块（需要配置 dependencies 指向 tools）
  - 不要修改 client 业务代码和业务依赖

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 修改 Gradle 构建文件
  - **Skills**: [`git-master`]

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3
  - **Blocks**: Task 6
  - **Blocked By**: Task 4

  **References**:
  - `client/build.gradle.kts:1-115` - 当前完整配置
  - `tools/detekt-rules/build.gradle.kts` - 新子项目配置
  - `client/detekt.yml` - 配置内容参考

  **Acceptance Criteria**:
  - [ ] `detekt {}` 块包含 `dependencies { detekt(project(":tools:detekt-rules")) }`
  - [ ] `detekt {}` 块 `config.setFrom()` 指向 tools/detekt-rules
  - [ ] `libs.gitlab.detekt.api` 依赖已从 client sourceSets 移除
  - [ ] kover 和 dokka 插件保留在 client
  - [ ] `./gradlew :client:detekt` 成功运行并使用自定义规则

  **QA Scenarios**:

  ```
  Scenario: client detekt 配置正确指向 tools 子项目
    Tool: Bash
    Preconditions: client/build.gradle.kts 已更新
    Steps:
      1. 运行 ./gradlew :client:detekt --dry-run
      2. 检查任务依赖包含 :tools:detekt-rules:jar
    Expected Result: detekt 任务依赖 tools/detekt-rules
    Evidence: .sisyphus/evidence/task-5-detekt-deps.txt

  Scenario: client 中 detekt 自定义规则被正确发现
    Tool: Bash
    Preconditions: 完整配置已完成
    Steps:
      1. 运行 ./gradlew :client:detekt
      2. 检查输出包含 "no-chinese-in-test-method" 规则
    Expected Result: 自定义规则被加载并生效
    Evidence: .sisyphus/evidence/task-5-detekt-run.txt
  ```

  **Commit**: NO (与 Task 6 一起 commit)

---

- [x] 6. **移除 client 中的旧 detekt 代码**

  **What to do**:
  - 删除 `client/src/commonMain/kotlin/com/sect/game/detekt/` 目录
  - 删除 `client/src/jvmMain/resources/META-INF/services/` 目录（如果只有 detekt 服务文件）
  - 删除或移动 `client/detekt.yml` 到 tools/detekt-rules/
  - 检查是否有其他 detekt 相关文件

  **Must NOT do**:
  - 不要删除其他业务代码
  - 不要删除其他 META-INF 服务文件（如果有）

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 删除文件，清理工作
  - **Skills**: [`git-master`]

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3
  - **Blocks**: Task 9
  - **Blocked By**: Task 5

  **References**:
  - `client/src/commonMain/kotlin/com/sect/game/detekt/` - 待删除目录
  - `client/src/jvmMain/resources/META-INF/services/` - 待检查目录

  **Acceptance Criteria**:
  - [ ] `client/src/commonMain/kotlin/com/sect/game/detekt/` 目录已删除
  - [ ] `client/src/jvmMain/resources/META-INF/services/io.gitlab.arturbosch.detekt.api.RuleSetProvider` 已删除
  - [ ] `client/detekt.yml` 已删除或移动
  - [ ] `git status` 显示文件已删除

  **QA Scenarios**:

  ```
  Scenario: client 中 detekt 代码已清理
    Tool: Bash
    Preconditions: 文件已删除
    Steps:
      1. 运行 ls client/src/commonMain/kotlin/com/sect/game/ | grep detekt
      2. 检查返回为空
    Expected Result: detekt 目录不存在于 client 中
    Evidence: .sisyphus/evidence/task-6-cleanup.txt
  ```

  **Commit**: YES (与 Task 5 合并为 refactor commit)
  - Message: `refactor(client): remove tool code, depend on tools subprojects`
  - Files: `client/build.gradle.kts`, 删除的文件
  - Pre-commit: `./gradlew :client:build`

---

---

- [x] 9. **验证完整构建**

  **What to do**:
  - 运行 `./gradlew build --no-daemon` 完整构建
  - 验证所有子项目构建成功
  - 验证 client 模块构建成功

  **Must NOT do**:
  - 不要修改任何代码
  - 只是验证构建

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 需要理解构建失败原因并修复
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 4
  - **Blocks**: Task 10
  - **Blocked By**: Tasks 6, 7, 8

  **References**:
  - `client/build.gradle.kts` - 验证配置正确

  **Acceptance Criteria**:
  - [ ] `./gradlew build` 成功
  - [ ] 无编译错误
  - [ ] 所有工具子项目构建成功

  **QA Scenarios**:

  ```
  Scenario: 完整项目构建成功
    Tool: Bash
    Preconditions: 所有任务完成
    Steps:
      1. 运行 ./gradlew build --no-daemon
      2. 检查输出包含 "BUILD SUCCESSFUL"
    Expected Result: 构建成功，无错误
    Evidence: .sisyphus/evidence/task-9-full-build.txt
  ```

  **Commit**: NO (验证 commit)

---

- [x] 10. **执行所有验收标准**

  **What to do**:
  - 按顺序执行所有验收命令
  - 捕获每个命令的输出
  - 验证所有标准满足

  **Must NOT do**:
  - 不要修改代码
  - 只是验证

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 需要综合验证
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 5
  - **Blocks**: None
  - **Blocked By**: Task 9

  **References**:
  - 计划中的 Success Criteria 部分

  **Acceptance Criteria**:
  - [ ] `./gradlew :tools:detekt-rules:build` 成功
  - [ ] `./gradlew :tools:kover-config:build` 成功
  - [ ] `./gradlew :tools:dokka-config:build` 成功
  - [ ] `./gradlew :client:detekt` 成功
  - [ ] `./gradlew :client:build` 成功
  - [ ] `./gradlew build` 成功

  **QA Scenarios**:

  ```
  Scenario: 所有工具子项目构建成功
    Tool: Bash
    Preconditions: 所有代码已完成
    Steps:
      1. 运行 ./gradlew :tools:detekt-rules:build
      2. 运行 ./gradlew :tools:kover-config:build
      3. 运行 ./gradlew :tools:dokka-config:build
    Expected Result: 所有构建成功
    Evidence: .sisyphus/evidence/task-10-tools-build.txt

  Scenario: client 模块 detekt 成功
    Tool: Bash
    Preconditions: 依赖已配置
    Steps:
      1. 运行 ./gradlew :client:detekt
    Expected Result: detekt 检查通过
    Evidence: .sisyphus/evidence/task-10-client-detekt.txt

  Scenario: client 模块构建成功
    Tool: Bash
    Preconditions: 工具已集成
    Steps:
      1. 运行 ./gradlew :client:build
    Expected Result: client 构建成功
    Evidence: .sisyphus/evidence/task-10-client-build.txt
  ```

  **Commit**: YES
  - Message: `ci: verify full project builds successfully`
  - Files: N/A (验证 commit)
  - Pre-commit: 所有验收标准通过

---

## Final Verification Wave

- [x] F1. **Plan Compliance Audit** — `oracle`
  读取计划 end-to-end。对于每个 "Must Have": 验证实现存在（读取文件，运行命令）。对于每个 "Must NOT Have": 搜索 codebase 确认禁止模式不存在。检查证据文件存在于 `.sisyphus/evidence/`。比较交付物与计划。
  Output: `Must Have [N/N] | Must NOT Have [N/N] | Tasks [N/N] | VERDICT: APPROVE/REJECT`

- [x] F2. **Code Quality Review** — `unspecified-high`
  运行 `detekt` 和 `build`。审查所有更改的文件: 是否有 `as any`/`@ts-ignore`，空 catch，console.log。检查 AI slop: 过度注释，过度抽象，通用名称。
  Output: `Build [PASS/FAIL] | Detekt [PASS/FAIL] | Files [N clean/N issues] | VERDICT`

- [x] F3. **Real Manual QA** — `unspecified-high`
  从干净状态开始。执行每个任务的所有 QA 场景 — 遵循精确步骤，捕获证据。测试跨任务集成。
  Output: `Scenarios [N/N pass] | Integration [N/N] | Edge Cases [N tested] | VERDICT`

- [x] F4. **Scope Fidelity Check** — `deep`
  对于每个任务: 读取 "What to do"，读取实际 diff (git log/diff)。验证 1:1 — spec 中的所有内容都已构建（无遗漏），spec 之外的内容未构建（无蔓延）。检查 "Must NOT do" 合规性。
  Output: `Tasks [N/N compliant] | Contamination [CLEAN/N issues] | Unaccounted [CLEAN/N files] | VERDICT`

---

## Commit Strategy

| Commit | Message | Files |
|--------|---------|-------|
| 1 | `chore: setup tools directory structure` | settings.gradle.kts, tools/ 目录 |
| 2 | `feat(detekt): move custom rules to tools/detekt-rules` | tools/detekt-rules/ |
| 3 | `chore(kover): add tools/kover-config placeholder` | tools/kover-config/ |
| 4 | `chore(dokka): add tools/dokka-config placeholder` | tools/dokka-config/ |
| 5 | `refactor(client): remove tool code, depend on tools subprojects` | client/build.gradle.kts, 删除的文件 |
| 6 | `ci: verify full project builds successfully` | - (验证 commit) |

---

## Success Criteria

### 验证命令
```bash
# 工具子项目构建
./gradlew :tools:detekt-rules:build    # 成功
./gradlew :tools:kover-config:build   # 成功
./gradlew :tools:dokka-config:build    # 成功

# Client 使用工具检查（关键验证）
./gradlew :client:detekt               # 成功 - 自定义规则生效
./gradlew :client:koverXmlReport      # 成功 - 覆盖率报告生成
./gradlew :client:dokkaHtml           # 成功 - 文档生成

# 完整项目构建
./gradlew :client:build               # 成功
./gradlew build                        # 成功
```

### 最终检查清单
- [ ] 所有 "Must Have" 存在
- [ ] 所有 "Must NOT Have" 不存在
- [ ] client 模块无工具代码（但保留插件配置）
- [ ] client 仍能运行 detekt/kover/dokka
- [ ] 工具子项目构建成功
- [ ] 完整项目构建成功
