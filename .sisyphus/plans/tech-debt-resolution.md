# 技术债务解决工作计划

## TL;DR

> **目标**: 解决项目剩余技术债务
> 1. UI 未连接 GOAP/MVI - 入口点仅显示静态文本
> 2. ktlint 未配置（仅使用 detekt）
>
> **额外任务**: 从 AGENTS.md 移除已解决的技术债务（自定义 detekt 规则、CI/CD）
>
> **预计工作量**: Medium
> **并行执行**: YES
> **关键路径**: 调研 → 方案设计 → 实现 → 验证

---

## Context

### 原始请求
用户希望解决当前项目的技术债务，已解决的需要从 AGENTS.md 移除。

### 当前技术债务状态

| 问题 | 状态 | 说明 |
|------|------|------|
| UI 未连接 GOAP/MVI | ✅ | GameEngine 通过 onTick 回调集成到 GameContainer，GameScreen 显示暂停/继续/停止控制 |
| ktlint | ✅ | 已配置（client/build.gradle.kts:128-133），.editorconfig 已创建 |
| AGENTS.md 技术债务表格 | ✅ | 已更新 |

### 已解决（待移除）

| 问题 | 状态 | 说明 |
|------|------|------|
| 自定义 detekt 规则 | ✅ | NoChineseInTestMethodName 已实现（tools/detekt-rules/） |
| CI/CD | ✅ | .github/workflows/ci.yml 已配置 |

### 研究发现

**核心问题**: GameEngine 和 GameContainer 各自持有独立的 Sect 实例，GameEngine 从未被启动。

**关键连接点**:
1. `GameEngine.onTick` 回调（已存在，未被使用）
2. GameContainer 需要在 `loadGame()` 时创建并启动 GameEngine
3. GameContainer 需要在 `onTick` 回调中同步状态到 `_state.value`

**最小变更方案**:
1. GameContract.kt - 添加 `tickCount`, `isPaused` 到 GameState
2. GameContainer.kt - 在 `loadGame()` 中创建/启动 GameEngine，添加 tick 回调同步
3. GameIntent.kt - 添加 `PauseGame`, `ResumeGame`, `StopGame` intent
4. GameScreen.kt - 显示暂停状态指示器

**ktlint 配置**:
- 使用 ktlint-gradle 插件
- 配置 .editorconfig
- 集成到 check 任务

---

## Work Objectives

### 已完成的工作

1. ✅ **从 AGENTS.md 移除已解决的技术债务** - 已完成
2. ✅ **解决 UI 未连接 GOAP/MVI** - GameContainer 已集成 GameEngine，GameScreen 已显示暂停/继续/停止控制
3. ✅ **配置 ktlint** - ktlint 插件已配置，.editorconfig 已创建

### 待完成的工作

1. **T9: 运行 ktlintCheck 验证** - 需要执行验证命令
2. **Final Verification Wave** - 4 个最终验证任务

### 技术方案

#### 1. UI 连接 GOAP/MVI

**现状分析:**
- MVI 层: GameContainer + FlowMVI ✅
- GOAP 层: GameEngine ✅
- 缺失: GameEngine 与 GameContainer 的连接

**建议方案:**
```
GameEngine (tick循环)
    ↓ 每 tick 更新 disciple 状态
    ↓ 通知 GameContainer
GameContainer (MVI State)
    ↓ 状态更新
GameScreen (Compose UI)
```

**关键变更点:**
1. GameContainer 需要持有 GameEngine 引用
2. GameEngine 的 `onTick` 回调触发状态同步
3. MVI State 需要包含 GOAP 相关状态（如当前执行的动作）

#### 2. ktlint 配置

**建议方案:**
- 使用 ktlint 官方 Gradle 插件
- 集成到 check 任务
- 配置格式化规则

---

## Verification Strategy

### QA 策略
- 所有验证均为 agent-executed
- UI 连接: 启动应用，验证弟子状态随 GOAP tick 更新
- ktlint: 运行 `./gradlew check` 确保代码通过 ktlint 检查

### 测试策略
- 无需添加新测试（现有测试覆盖）
- ktlint 配置后验证现有代码通过检查

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (基础配置 - ✅ 已完成):
├── T1: 更新 AGENTS.md 技术债务表格 ✅
├── T6: 添加 ktlint 依赖到版本目录 ✅
├── T7: 应用 ktlint 插件到项目 ✅
└── T8: 创建 ktlint 配置文件 ✅

Wave 2 (GOAP/MVI 连接 - ✅ 已完成):
├── T2: GameContract 添加游戏状态 ✅ (tickCount, isPaused 已存在)
├── T3: GameIntent 添加游戏控制 Intent ✅ (PauseGame, ResumeGame, StopGame 已存在)
├── T4: GameContainer 集成 GameEngine ✅ (GameContainer 创建并管理 GameEngine，onTick 回调同步状态)
└── T5: GameScreen 添加暂停指示器 ✅ (GameScreen 显示暂停按钮和 Tick 计数)

Wave 3 (验证 - ⏳ 待执行):
├── T9: 运行 ktlintCheck 验证 ⏳
└── T10: 验证 GOAP/MVI 连接 ⏳

Wave FINAL (⏳ 待执行):
├── F1-F4: Final Verification ⏳
```

### 依赖关系
- T2, T3 依赖 T1 完成（AGENTS.md 更新）
- T4 依赖 T2, T3 完成（需要新的 State 和 Intent）
- T5 依赖 T4 完成（需要新的 State）
- T9, T10 依赖 T4, T5, T8 完成

---

## TODO List

---

### 任务 1: 更新 AGENTS.md 技术债务表格（AGENTS.md 修改）

- [x] ~~文件~~: ~~`AGENTS.md`~~
- [x] ~~保留~~: ~~`UI 未连接 GOAP/MVI`、`ktlint`~~
- [x] ~~删除~~: ~~`自定义 detekt 规则`、`CI/CD` 行~~
- [x] ~~验收标准~~: ~~AGENTS.md 中技术债务表格仅包含 2 行~~ ✅

---

### 任务 6: 添加 ktlint 依赖到版本目录（ktlint 配置）

- [x] ~~文件~~: ~~`gradle/libs.versions.toml`~~
- [x] ~~ktlint 版本~~: ~~12.1.0~~
- [x] ~~ktlint 插件~~: ~~org.jlleitschuh.gradle.ktlint~~ ✅

---

### 任务 7: 应用 ktlint 插件到项目（ktlint 配置）

- [x] ~~文件~~: ~~`client/build.gradle.kts`~~
- [x] ~~ktlint 插件~~: ~~已应用~~ ✅

---

### 任务 8: 创建 ktlint 配置（ktlint 配置）

- [x] ~~文件~~: ~~`.editorconfig`（项目根目录）~~
- [x] ~~标准 ktlint 配置~~: ~~已创建~~ ✅

---

### 任务 9: 运行 ktlintCheck 验证（验证任务）

**命令**: `./gradlew :client:ktlintCheck`

**验收标准**:
- 所有 Kotlin 文件通过 ktlint 检查（或有预期内的警告）

---

### 任务 10: 验证 GOAP/MVI 连接（验证任务）

**验收标准**:
- 启动应用后，弟子状态随 tick 更新
- isPaused 状态正确反映

---

## Final Verification Wave

- [ ] F1: Plan Compliance Audit — 验证所有 Must Have 已实现
- [ ] F2: Code Quality Review — 运行 `./gradlew check`
- [ ] F3: Real Manual QA — 验证 GOAP/MVI 连接正常工作
- [ ] F4: Scope Fidelity Check — 验证范围忠实度

---

## Success Criteria

### 验证命令
```bash
# 1. 检查 AGENTS.md 技术债务表格
grep -A 5 "## 技术债务" AGENTS.md

# 2. 运行 ktlint 检查
./gradlew :client:ktlintCheck

# 3. 编译检查
./gradlew :client:compileKotlinJvm

# 4. 运行测试
./gradlew :client:jvmTest
```

### 最终检查清单
- [x] ~~AGENTS.md 仅保留 2 行技术债务~~ ✅ 已完成
- [x] ~~GameState 包含 tickCount, isPaused~~ ✅ 已存在 (GameContract.kt:23-24)
- [x] ~~GameIntent 包含 PauseGame, ResumeGame, StopGame~~ ✅ 已存在 (GameContract.kt:37-41)
- [x] ~~GameContainer 持有 GameEngine 引用并同步状态~~ ✅ 已实现 (GameContainer.kt:59-71 onTick 回调)
- [x] ~~ktlint 配置完成并通过检查~~ ✅ 已配置
- [ ] 所有测试通过 ⏳ 待验证

### 实际验证状态
1. ✅ GameContract: tickCount (Long), isPaused (Boolean) 已添加
2. ✅ GameIntent: PauseGame, ResumeGame, StopGame 已添加
3. ✅ GameContainer: GameEngine 已集成，onTick 回调同步状态
4. ✅ GameScreen: TopAppBar 显示暂停/继续/停止按钮，Tick 计数
5. ⏳ ktlintCheck: 需运行 `./gradlew :client:ktlintCheck` 验证
6. ⏳ Final Verification: 需执行 F1-F4
