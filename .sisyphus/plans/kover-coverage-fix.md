# Kover 覆盖率配置修复计划

**Generated**: 2026-03-23
**Status**: 新建
**Purpose**: 修复 Issue #21 覆盖率验证失败

## TL;DR

> **核心目标**: 在业务模块配置 kover，使覆盖率验证可以通过
>
> **问题**: kover 仅配置在 `tools/kover-config`（无源码），业务模块未配置
> **解决**: 在 `business/*/build.gradle.kts` 中应用 kover 插件并配置阈值
>
> **预计周期**: 快速修复（1-2 小时）
> **并行执行**: YES

---

## Context

### Issue #21 验证失败原因

```
koverHtmlReport 仅生成 tools:kover-config 模块报告（No sources）
业务模块（business/domain, business/goap, business/engine 等）未配置 kover
```

### 验证输出

```
Line Coverage: N/A (未配置)
Branch Coverage: N/A (未配置)
Tests: PASS (所有测试通过)
VERDICT: FAIL
```

### 验收标准

- [ ] Line coverage >= 80%
- [ ] Branch coverage >= 70%
- [ ] 所有单元测试通过

---

## Work Objectives

### Core Objective
在业务模块正确配置 kover，使覆盖率报告可以生成并满足 MVP 要求。

### Concrete Deliverables
- 在 `business/domain/build.gradle.kts` 配置 kover
- 在 `business/goap/build.gradle.kts` 配置 kover
- 在 `business/engine/build.gradle.kts` 配置 kover
- 在 `business/feature-game/build.gradle.kts` 配置 kover
- 配置 `koverVerify` 任务验证阈值
- 运行 `./gradlew koverVerify` 通过

### Must Have
- kover 插件应用于所有有源码的业务模块
- 配置 line coverage >= 80%, branch coverage >= 70%
- `koverVerify` 任务验证通过

### Must NOT Have
- 不修改 `tools/kover-config` 模块（无源码，无需覆盖）
- 不删除任何现有测试

---

## Execution Strategy

### 并行执行

```
Wave 1 (并行配置所有模块):
├── Task 1: domain 模块 kover 配置 [quick]
├── Task 2: goap 模块 kover 配置 [quick]
├── Task 3: engine 模块 kover 配置 [quick]
└── Task 4: feature-game 模块 kover 配置 [quick]

Wave 2 (验证):
└── Task 5: 运行 koverVerify 验证覆盖率 [quick]
```

### 模块对应关系

| 业务模块 | 源码路径 | Gradle 项目 |
|---------|---------|-------------|
| domain | `business/domain/src/commonMain/` | `:business:domain` |
| goap | `business/goap-framework/` + `business/goap/` | `:business:goap-framework` + `:business:goap` |
| engine | `business/engine/src/commonMain/` | `:business:engine` |
| feature-game | `business/feature-game/src/commonMain/` | `:business:feature-game` |

---

## TODOs

- [x] 1. domain 模块 kover 配置

  **What to do**:
  - 在 `business/domain/build.gradle.kts` 添加 kover 插件
  - 配置覆盖率阈值：line >= 80%, branch >= 70%
  - 添加 koverVerify 任务

  **Must NOT do**:
  - 不修改源码

  **References**:
  - `tools/kover-config/build.gradle.kts` - 现有 kover 配置参考
  - Kover 官方文档

  **Acceptance Criteria**:
  - [ ] `./gradlew :business:domain:koverVerify` 通过

  **QA Scenarios**:
  ```
  Scenario: domain 模块覆盖率验证
    Tool: Bash
    Steps:
      1. 执行 ./gradlew :business:domain:koverVerify
      2. 检查退出码为 0
    Expected Result: 验证通过
    Evidence: .sisyphus/evidence/kover-domain-verify.txt
  ```

- [x] 2. goap 模块 kover 配置

  **What to do**:
  - 在 `business/goap-framework/build.gradle.kts` 添加 kover 插件
  - 在 `business/goap/build.gradle.kts` 添加 kover 插件
  - 配置覆盖率阈值

  **References**:
  - `tools/kover-config/build.gradle.kts` - 现有 kover 配置参考

  **Acceptance Criteria**:
  - [ ] `./gradlew :business:goap-framework:koverVerify` 通过
  - [ ] `./gradlew :business:goap:koverVerify` 通过

  **QA Scenarios**:
  ```
  Scenario: goap 模块覆盖率验证
    Tool: Bash
    Steps:
      1. 执行 ./gradlew :business:goap-framework:koverVerify
      2. 执行 ./gradlew :business:goap:koverVerify
    Expected Result: 两个验证都通过
    Evidence: .sisyphus/evidence/kover-goap-verify.txt
  ```

- [x] 3. engine 模块 kover 配置

  **What to do**:
  - 在 `business/engine/build.gradle.kts` 添加 kover 插件
  - 配置覆盖率阈值

  **References**:
  - `tools/kover-config/build.gradle.kts` - 现有 kover 配置参考

  **Acceptance Criteria**:
  - [ ] `./gradlew :business:engine:koverVerify` 通过

  **QA Scenarios**:
  ```
  Scenario: engine 模块覆盖率验证
    Tool: Bash
    Steps:
      1. 执行 ./gradlew :business:engine:koverVerify
    Expected Result: 验证通过
    Evidence: .sisyphus/evidence/kover-engine-verify.txt
  ```

- [x] 4. feature-game 模块 kover 配置

  **What to do**:
  - 在 `business/feature-game/build.gradle.kts` 添加 kover 插件
  - 配置覆盖率阈值

  **References**:
  - `tools/kover-config/build.gradle.kts` - 现有 kover 配置参考

  **Acceptance Criteria**:
  - [ ] `./gradlew :business:feature-game:koverVerify` 通过

  **QA Scenarios**:
  ```
  Scenario: feature-game 模块覆盖率验证
    Tool: Bash
    Steps:
      1. 执行 ./gradlew :business:feature-game:koverVerify
    Expected Result: 验证通过
    Evidence: .sisyphus/evidence/kover-feature-game-verify.txt
  ```

- [x] 5. 全局 koverVerify 验证

  **What to do**:
  - 运行 `./gradlew koverVerify` 验证所有模块
  - 生成覆盖率报告 `./gradlew koverHtmlReport`

  **Acceptance Criteria**:
  - [ ] `./gradlew koverVerify` 通过
  - [ ] `./gradlew koverHtmlReport` 生成报告

  **QA Scenarios**:
  ```
  Scenario: 全局覆盖率验证
    Tool: Bash
    Steps:
      1. 执行 ./gradlew koverVerify
      2. 检查退出码为 0
      3. 执行 ./gradlew koverHtmlReport
    Expected Result: 所有验证通过，报告生成
    Evidence: .sisyphus/evidence/kover-global-verify.txt
  ```

---

## Commit Strategy

- Message: `chore: configure kover coverage for business modules`
- Files: `business/*/build.gradle.kts`

---

## Success Criteria

### Verification Commands
```bash
./gradlew koverVerify                    # Expected: BUILD SUCCESSFUL
./gradlew koverHtmlReport               # Expected: Coverage report generated
```

### Final Checklist
- [ ] business:domain kover 配置完成
- [ ] business:goap-framework kover 配置完成
- [ ] business:goap kover 配置完成
- [ ] business:engine kover 配置完成
- [ ] business:feature-game kover 配置完成
- [ ] 全局 koverVerify 通过
- [ ] 覆盖率报告生成

---

## 关联 Issue

- Issue #21: [验证] 测试覆盖率验证 - **需要此计划完成后重新验证**
