# 战斗历练系统开发计划

## TL;DR

> **Quick Summary**: 为修仙游戏实现战斗历练系统，支持弟子外出历练、回合制战斗、多层地图递进
>
> **Deliverables**:
> - `CombatUnit` 战斗单元值对象（统一处理敌我双方）
> - `ExpeditionState` 远征状态管理
> - `CombatExecutor` 战斗执行器
> - `TurnBasedCombatPlanner` 回合制战斗规划器
> - `ExpeditionGoal` 新的GOAP目标类型
> - 3层递进地图（外围→深处→核心）
>
> **Estimated Effort**: Medium-Large
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: T1 → T5 → T9 → T13 → F1-F4

---

## Context

### Original Request
为修仙游戏项目开发战斗历练系统，支持：
1. 回合制战斗（速度决定行动顺序）
2. 多场景递进（固定3层地图）
3. GOAP自动判断历练vs修炼
4. 统一战斗对象设计（方便后期扩展）

### Interview Summary
**Key Discussions**:
- [x] 战斗机制：回合制，速度决定顺序
- [x] 敌人设计：统一战斗对象，与Disciple解耦
- [x] HP=0处理：撤退（可恢复，非死亡）
- [x] 队伍系统：2-3人小队
- [x] 与GOAP关系：混合模式，战斗时切换子系统

**Research Findings**:
- [Metis分析]: 当前Disciple无战斗属性，需通过Realm+Attributes映射
- [Metis分析]: 需要GameMode枚举区分GOAP和COMBAT模式
- [Metis分析]: 战斗系统需与GOAP WorldState隔离

### Metis Review
**Identified Gaps (addressed)**:
- [Gap]: 战斗属性缺失 → Resolved: 通过Realm映射计算CombatStats
- [Gap]: 无模式切换机制 → Resolved: GameEngine添加GameMode枚举
- [Gap]: 无敌人实体 → Resolved: 使用CombatUnit抽象，敌我都用

---

## Work Objectives

### Core Objective
实现完整的战斗历练系统闭环：**GOAP判断外出 → 遭遇敌人 → 回合制战斗 → 奖励结算 → 返回**

### Concrete Deliverables
- `CombatStats.kt` - 战斗属性值对象（attack, defense, speed, hp）
- `CombatUnit.kt` - 统一战斗单元（敌我双方都用这个）
- `ExpeditionState.kt` - 远征状态（当前层数、队伍HP、敌人列表）
- `CombatPhase.kt` - 回合制阶段机（PARTY_TURN, ENEMY_TURN, RESOLVE, END）
- `CombatExecutor.kt` - 战斗执行器（实现ActionExecutor接口）
- `TurnBasedCombatPlanner.kt` - 回合制规划器（速度排序+行动选择）
- `ExpeditionGoal.kt` - 历练目标（GOAP新目标类型）
- `ExpeditionAction.kt` - 历练行动（GOAP新行动类型）
- `MonsterFactory.kt` - 怪物工厂（根据层数生成敌人）
- `RewardCalculator.kt` - 奖励计算器
- `GameEngine` 改造 - 添加GameMode和模式切换

### Definition of Done
- [ ] 单元测试：伤害计算正确
- [ ] 单元测试：回合顺序按速度排序
- [ ] 单元测试：HP=0正确处理为撤退
- [ ] 集成测试：从GOAP模式切换到战斗，完成后返回GOAP
- [ ] 集成测试：3层地图递进正确

### Must Have
- 回合制战斗逻辑（攻击-防御=伤害）
- 速度决定行动顺序
- 3层固定地图
- 战斗超时保护（20回合限制）
- GOAP→战斗→GOAP模式切换

### Must NOT Have (Guardrails)
- 战斗UI界面（MVP阶段）
- 玩家操作控制
- 技能/功法系统
- 属性克制系统
- 永久死亡机制
- 资源消耗（丹药使用等）
- 多线程/并发战斗

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: YES (kotlin-test)
- **Automated tests**: YES (Tests-after)
- **Framework**: kotlin-test (JUnit风格)
- **Coverage target**: 核心逻辑80%+

### QA Policy
Every task includes agent-executed QA scenarios. Evidence saved to `.sisyphus/evidence/`.

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (foundation - no dependencies):
├── T1: CombatStats + CombatUnit 值对象
├── T2: CombatPhase 阶段机
├── T3: DamageCalculator 伤害计算
└── T4: TurnOrderCalculator 速度排序

Wave 2 (core logic - after Wave 1):
├── T5:  CombatExecutor 战斗执行器
├── T6:  TurnBasedCombatPlanner 规划器
├── T7:  MonsterFactory 怪物工厂
└── T8:  RewardCalculator 奖励计算

Wave 3 (integration - after Wave 2):
├── T9:  ExpeditionState 远征状态
├── T10: ExpeditionGoal 历练目标
├── T11: ExpeditionAction 历练行动
├── T12: GameEngine 模式切换
└── T13: End-to-End 集成测试

Wave FINAL (4 parallel reviews):
├── F1: Plan Compliance Audit (oracle)
├── F2: Code Quality Review (unspecified-high)
├── F3: Real Manual QA (unspecified-high)
└── F4: Scope Fidelity Check (deep)
```

### Dependency Matrix

| Task | Depends On | Blocks |
|------|------------|--------|
| T1 | - | T5, T6 |
| T2 | - | T5, T6 |
| T3 | - | T5, T6 |
| T4 | - | T6 |
| T5 | T1, T2, T3 | T9, T12 |
| T6 | T3, T4 | T9, T12 |
| T7 | T1 | T9, T12 |
| T8 | T1 | T9, T12 |
| T9 | T5, T6, T7, T8 | T13 |
| T10 | T9 | T13 |
| T11 | T9 | T13 |
| T12 | T9 | T13 |
| T13 | T10, T11, T12 | F1, F2, F3, F4 |

### Agent Dispatch Summary

- **Wave 1**: **4** — T1-T4 → `ultrabrain` (逻辑复杂，需深度理解)
- **Wave 2**: **4** — T5-T8 → `deep` (核心战斗逻辑)
- **Wave 3**: **5** — T9-T13 → `deep` (集成+测试)
- **FINAL**: **4** — F1 → `oracle`, F2 → `unspecified-high`, F3 → `unspecified-high`, F4 → `deep`

---

## TODOs

- [ ] 1. **CombatStats + CombatUnit 值对象**

  **What to do**:
  - 创建 `CombatStats.kt` 值对象，包含 `attack`, `defense`, `speed`, `maxHp`
  - 创建 `CombatUnit.kt` 统一战斗单元，`isPlayer: Boolean` 区分敌我
  - 实现 `fromDisciple(Disciple)` 工厂方法：从Disciple计算CombatStats
  - 战斗属性计算公式：
    - `attack = realm.ordinal * 10 + spiritRoot / 10`
    - `defense = realm.ordinal * 5 + talent / 10`
    - `speed = 50 + luck / 5`
    - `maxHp = 100 + realm.ordinal * 20`

  **Must NOT do**:
  - 不要修改 Disciple 实体（保持解耦）
  - 不要添加技能/Buff字段（MVP范围外）

  **Recommended Agent Profile**:
  > **ultrabrain** - 领域建模需要深度理解现有实体关系
  - **Category**: `ultrabrain`
    - Reason: 需要基于现有Disciple/Realm/Attributes构建新的值对象，涉及多个属性的组合计算
  - **Skills**: `[]`
  - **Skills Evaluated but Omitted**:
    - `playwright`: 不涉及UI

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with T2, T3, T4)
  - **Blocks**: T5, T6
  - **Blocked By**: None

  **References**:

  **Pattern References** (existing code to follow):
  - `client/src/commonMain/kotlin/com/sect/game/domain/valueobject/Realm.kt` - Realm枚举定义，ordinal用于计算
  - `client/src/commonMain/kotlin/com/sect/game/domain/valueobject/Attributes.kt` - Attributes值对象（spiritRoot, talent, luck）
  - `client/src/commonMain/kotlin/com/sect/game/domain/entity/Disciple.kt:88-105` - calculateCultivationGain等私有方法参考

  **API/Type References** (contracts to implement against):
  - `Disciple` - 工厂方法输入参数
  - `Realm` - ordinal用于属性映射
  - `Attributes` - spiritRoot/talent/luck用于属性映射

  **Test References** (testing patterns to follow):
  - `client/src/commonTest/kotlin/com/sect/game/domain/valueobject/RealmTest.kt` - 值对象测试风格

  **Acceptance Criteria**:
  - [ ] 测试：Disciple(Realm.LianQi) → CombatStats(attack=10, defense=5, speed=50, maxHp=120)
  - [ ] 测试：Disciple(Realm.JinDan) → CombatStats(attack=30, defense=15, speed=80, maxHp=160)
  - [ ] 测试：CombatUnit可正确区分isPlayer=true/false
  - [ ] `./gradlew jvmTest --tests "*CombatStats*" → PASS`

  **QA Scenarios**:
  ```
  Scenario: 境界映射计算正确
    Tool: Bash
    Preconditions: Gradle build succeeds
    Steps:
      1. Run: ./gradlew jvmTest --tests "com.sect.game.engine.combat.CombatStatsTest"
    Expected Result: 所有测试通过，输出包含 "2 tests passed"
    Evidence: .sisyphus/evidence/task-1-combat-stats-test.log
  ```

- [ ] 2. **CombatPhase 阶段机**

  **What to do**:
  - 创建 `CombatPhase.kt` sealed class：`INIT`, `PARTY_TURN`, `ENEMY_TURN`, `RESOLVE`, `END`
  - 实现 `nextPhase()` 方法定义阶段转换规则
  - 转换规则：
    - INIT → PARTY_TURN
    - PARTY_TURN → RESOLVE
    - ENEMY_TURN → RESOLVE
    - RESOLVE → (判断是否结束) PARTY_TURN / ENEMY_TURN / END
  - END阶段包含 `victory: Boolean` 和 `retreat: Boolean` 数据

  **Must NOT do**:
  - 不要实现具体的战斗逻辑（那是Executor的职责）
  - 不要包含UI状态

  **Recommended Agent Profile**:
  > **ultrabrain** - 状态机设计需要严谨的逻辑
  - **Category**: `ultrabrain`
    - Reason: 回合制阶段机是核心状态机，需要清晰的状态转换定义
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with T1, T3, T4)
  - **Blocks**: T5
  - **Blocked By**: None

  **References**:

  **Pattern References** (existing code to follow):
  - `client/src/commonMain/kotlin/com/sect/game/goap/goals/Goal.kt` - sealed interface模式

  **Acceptance Criteria**:
  - [ ] INIT.nextPhase() → PARTY_TURN
  - [ ] PARTY_TURN.nextPhase() → RESOLVE
  - [ ] ENEMY_TURN.nextPhase() → RESOLVE
  - [ ] RESOLVE.nextPhase() 当未结束时 → 轮换到另一方
  - [ ] ./gradlew jvmTest --tests "*CombatPhase*" → PASS

  **QA Scenarios**:
  ```
  Scenario: 阶段转换正确
    Tool: Bash
    Preconditions: Gradle build succeeds
    Steps:
      1. Run: ./gradlew jvmTest --tests "com.sect.game.engine.combat.CombatPhaseTest"
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-2-combat-phase-test.log
  ```

- [ ] 3. **DamageCalculator 伤害计算**

  **What to do**:
  - 创建 `DamageCalculator.kt` 对象
  - 实现 `calculateDamage(attacker: CombatStats, defender: CombatStats): Int`
  - 伤害公式：`damage = max(1, attacker.attack - defender.defense)`
  - 最低伤害保底为1点

  **Must NOT do**:
  - 不要添加暴击/随机性（MVP确定性）
  - 不要添加属性克制

  **Recommended Agent Profile**:
  > **ultrabrain** - 数值公式设计需要逻辑严谨
  - **Category**: `ultrabrain`
    - Reason: 伤害公式是战斗核心，需要精确计算
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with T1, T2, T4)
  - **Blocks**: T5
  - **Blocked By**: None

  **References**:

  **API/Type References**:
  - `CombatStats` - T1创建的类型

  **Acceptance Criteria**:
  - [ ] 测试：attack=20, defense=5 → damage=15
  - [ ] 测试：attack=10, defense=15 → damage=1 (保底)
  - [ ] 测试：attack=10, defense=10 → damage=1
  - [ ] ./gradlew jvmTest --tests "*DamageCalculator*" → PASS

  **QA Scenarios**:
  ```
  Scenario: 伤害计算正确
    Tool: Bash
    Preconditions: Gradle build succeeds
    Steps:
      1. Run: ./gradlew jvmTest --tests "com.sect.game.engine.combat.DamageCalculatorTest"
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-3-damage-calc-test.log
  ```

- [ ] 4. **TurnOrderCalculator 速度排序**

  **What to do**:
  - 创建 `TurnOrderCalculator.kt` 对象
  - 实现 `calculateTurnOrder(party: List<CombatUnit>, enemies: List<CombatUnit>): List<CombatUnit>`
  - 按speed降序排列，返回所有单位的行动顺序列表
  - 同速度时保持相对顺序

  **Must NOT do**:
  - 不要实现具体的行动选择逻辑
  - 不要修改CombatUnit列表

  **Recommended Agent Profile**:
  > **ultrabrain** - 排序算法需要逻辑清晰
  - **Category**: `ultrabrain`
    - Reason: 简单的速度排序+合并逻辑
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with T1, T2, T3)
  - **Blocks**: T6
  - **Blocked By**: None

  **References**:

  **API/Type References**:
  - `CombatUnit` - T1创建的类型

  **Acceptance Criteria**:
  - [ ] 测试：3个单位速度[30, 50, 10] → 顺序[50, 30, 10]
  - [ ] 测试：敌我混合排序正确
  - [ ] 测试：空列表返回空列表
  - [ ] ./gradlew jvmTest --tests "*TurnOrder*" → PASS

  **QA Scenarios**:
  ```
  Scenario: 速度排序正确
    Tool: Bash
    Preconditions: Gradle build succeeds
    Steps:
      1. Run: ./gradlew jvmTest --tests "com.sect.game.engine.combat.TurnOrderCalculatorTest"
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-4-turn-order-test.log
  ```

- [ ] 5. **CombatExecutor 战斗执行器**

  **What to do**:
  - 创建 `CombatExecutor.kt`，实现 `ActionExecutor` 接口
  - 实现 `execute(unit: CombatUnit, action: CombatAction, target: CombatUnit, state: CombatState): CombatState`
  - `CombatAction` sealed class：`Attack`, `Defend`, `UseItem`
  - 执行Attack时调用DamageCalculator计算伤害
  - 执行Defend时临时提升defense（持续1回合）
  - 返回更新后的CombatState

  **Must NOT do**:
  - 不要实现多目标攻击（MVP单目标）
  - 不要实现技能系统

  **Recommended Agent Profile**:
  > **deep** - 执行器是核心逻辑
  - **Category**: `deep`
    - Reason: 战斗执行器是核心组件，需要正确实现ActionExecutor接口模式
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with T6, T7, T8)
  - **Blocks**: T9, T12
  - **Blocked By**: T1, T2, T3

  **References**:

  **Pattern References** (existing code to follow):
  - `client/src/commonMain/kotlin/com/sect/game/engine/executor/ActionExecutor.kt` - 接口定义
  - `client/src/commonMain/kotlin/com/sect/game/engine/executor/DefaultActionExecutor.kt` - 实现参考

  **API/Type References** (contracts to implement against):
  - `ActionExecutor` - 必须实现的接口
  - `CombatPhase` - T2创建的类型
  - `CombatUnit` - T1创建的类型

  **Acceptance Criteria**:
  - [ ] 测试：Attack执行后目标HP减少
  - [ ] 测试：Defend执行后defense临时提升
  - [ ] 测试：HP降为0时标记为倒下
  - [ ] ./gradlew jvmTest --tests "*CombatExecutor*" → PASS

  **QA Scenarios**:
  ```
  Scenario: 攻击执行后HP减少
    Tool: Bash
    Preconditions: T1-T3完成
    Steps:
      1. Run: ./gradlew jvmTest --tests "com.sect.game.engine.combat.CombatExecutorTest"
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-5-executor-test.log
  ```

- [ ] 6. **TurnBasedCombatPlanner 回合制规划器**

  **What to do**:
  - 创建 `TurnBasedCombatPlanner.kt`
  - 实现 `plan(currentState: CombatState, turnOrder: List<CombatUnit>): List<CombatAction>`
  - 行动选择策略（AI自动）：
    - 如果自己HP < 30%且有道具，使用道具
    - 如果敌人defense > 自己attack * 1.5，使用破防技能（暂无，跳过）
    - 默认使用Attack
  - 生成每个单位的CombatAction列表

  **Must NOT do**:
  - 不要实现复杂的策略AI（MVP简单优先）
  - 不要让玩家控制

  **Recommended Agent Profile**:
  > **deep** - 规划器逻辑需要深度思考
  - **Category**: `deep`
    - Reason: AI行动选择逻辑，虽然MVP简单但需要正确集成
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with T5, T7, T8)
  - **Blocks**: T9, T12
  - **Blocked By**: T3, T4

  **References**:

  **Pattern References** (existing code to follow):
  - `client/src/commonMain/kotlin/com/sect/game/engine/planner/AStarPlanner.kt` - 规划器模式参考
  - `client/src/commonMain/kotlin/com/sect/game/goap/planner/GOAPPlanner.kt` - 接口参考

  **API/Type References**:
  - `CombatState` - 待创建
  - `TurnOrderCalculator` - T4

  **Acceptance Criteria**:
  - [ ] 测试：HP充足时选择Attack
  - [ ] 测试：HP低于30%时选择UseItem（如有）
  - [ ] ./gradlew jvmTest --tests "*TurnBasedCombatPlanner*" → PASS

  **QA Scenarios**:
  ```
  Scenario: 规划器选择正确行动
    Tool: Bash
    Preconditions: T3, T4完成
    Steps:
      1. Run: ./gradlew jvmTest --tests "com.sect.game.engine.combat.TurnBasedCombatPlannerTest"
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-6-planner-test.log
  ```

- [ ] 7. **MonsterFactory 怪物工厂**

  **What to do**:
  - 创建 `MonsterFactory.kt`
  - 实现 `createMonster(floor: Int, partyRealm: Realm): CombatUnit`
  - 根据层数生成对应难度的敌人：
    - 外围(1-3层)：LianQi~Zhuji级别
    - 深处(4-6层)：JinDan~YuanYing级别
    - 核心(7-9层)：HuaShen级别
  - 敌人属性 = 队伍平均realm映射 * (1 + floor * 0.1) 增幅

  **Must NOT do**:
  - 不要实现多波次敌人（MVP每层1组）
  - 不要实现BOSS机制

  **Recommended Agent Profile**:
  > **deep** - 数值平衡设计
  - **Category**: `deep`
    - Reason: 怪物属性计算需要平衡性考虑
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with T5, T6, T8)
  - **Blocks**: T9, T12
  - **Blocked By**: T1

  **References**:

  **Pattern References** (existing code to follow):
  - `client/src/commonMain/kotlin/com/sect/game/domain/valueobject/Realm.kt` - Realm枚举定义

  **API/Type References**:
  - `CombatUnit` - T1
  - `Realm` - 域模型

  **Acceptance Criteria**:
  - [ ] 测试：第1层生成LianQi级别怪物
  - [ ] 测试：第5层生成JinDan级别怪物
  - [ ] 测试：第8层生成HuaShen级别怪物
  - [ ] ./gradlew jvmTest --tests "*MonsterFactory*" → PASS

  **QA Scenarios**:
  ```
  Scenario: 怪物工厂生成正确层数敌人
    Tool: Bash
    Preconditions: T1完成
    Steps:
      1. Run: ./gradlew jvmTest --tests "com.sect.game.engine.combat.MonsterFactoryTest"
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-7-monster-factory-test.log
  ```

- [ ] 8. **RewardCalculator 奖励计算**

  **What to do**:
  - 创建 `RewardCalculator.kt`
  - 实现 `calculateRewards(floor: Int, victory: Boolean): ExpeditionReward`
  - `ExpeditionReward` data class: `spiritStones: Int, herbs: Int, experience: Int`
  - 胜利奖励公式：
    - `spiritStones = floor * 10`
    - `herbs = floor / 2 + 1`
    - `experience = floor * 20`
  - 失败/撤退奖励：spiritStones = floor * 2，其他为0

  **Must NOT do**:
  - 不要实现随机奖励范围（MVP确定性）
  - 不要实现特殊掉落

  **Recommended Agent Profile**:
  > **quick** - 简单的数值计算
  - **Category**: `quick`
    - Reason: 奖励公式简单，直接计算即可
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with T5, T6, T7)
  - **Blocks**: T9, T12
  - **Blocked By**: T1

  **References**:

  **API/Type References**:
  - `Resources` - 域模型的资源类型

  **Acceptance Criteria**:
  - [ ] 测试：第5层胜利 → spiritStones=50, herbs=3, exp=100
  - [ ] 测试：撤退 → spiritStones=10, herbs=0, exp=0
  - [ ] ./gradlew jvmTest --tests "*RewardCalculator*" → PASS

  **QA Scenarios**:
  ```
  Scenario: 奖励计算正确
    Tool: Bash
    Preconditions: T1完成
    Steps:
      1. Run: ./gradlew jvmTest --tests "com.sect.game.engine.combat.RewardCalculatorTest"
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-8-reward-test.log
  ```

- [ ] 9. **ExpeditionState 远征状态**

  **What to do**:
  - 创建 `ExpeditionState.kt` data class
  - 包含字段：`currentFloor: Int`, `maxFloors: Int = 9`, `partyUnits: List<CombatUnit>`, `enemyUnits: List<CombatUnit>`, `turnCount: Int`, `maxTurns: Int = 20`, `isEnded: Boolean`, `victory: Boolean`
  - 实现 `advanceFloor()`: currentFloor++并生成新敌人
  - 实现 `isFloorCleared()`: enemyUnits.all { it.isDefeated }
  - 实现 `canContinue()`: currentFloor < maxFloors && !isEnded

  **Must NOT do**:
  - 不要包含UI状态
  - 不要实现存档逻辑

  **Recommended Agent Profile**:
  > **deep** - 状态管理核心
  - **Category**: `deep`
    - Reason: ExpeditionState是远征流程的核心状态容器
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with T10, T11, T12)
  - **Blocks**: T13
  - **Blocked By**: T5, T6, T7, T8

  **References**:

  **Pattern References** (existing code to follow):
  - `client/src/commonMain/kotlin/com/sect/game/engine/GameEngine.kt` - 状态管理模式参考

  **API/Type References**:
  - `CombatUnit` - T1
  - `MonsterFactory` - T7

  **Acceptance Criteria**:
  - [ ] 测试：advanceFloor()后currentFloor增加
  - [ ] 测试：isFloorCleared()在敌人全倒下时返回true
  - [ ] 测试：canContinue()在第9层后返回false
  - [ ] ./gradlew jvmTest --tests "*ExpeditionState*" → PASS

  **QA Scenarios**:
  ```
  Scenario: 远征状态管理
    Tool: Bash
    Preconditions: T5-T8完成
    Steps:
      1. Run: ./gradlew jvmTest --tests "com.sect.game.engine.expedition.ExpeditionStateTest"
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-9-expedition-state-test.log
  ```

- [ ] 10. **ExpeditionGoal 历练目标**

  **What to do**:
  - 创建 `ExpeditionGoal.kt`，实现 `Goal` 接口
  - 目标条件：`isOnExpedition = true`
  - 目标状态：`currentFloor >= maxFloors`（到达最深层）或 `allEnemiesDefeated`
  - 优先级：60（介于休息80和修炼50之间）
  - 触发条件：fatigue < 50, health > 60, readiness > 70

  **Must NOT do**:
  - 不要修改现有Goal实现
  - 不要影响现有GOAP规划

  **Recommended Agent Profile**:
  > **deep** - GOAP集成需要理解现有系统
  - **Category**: `deep`
    - Reason: ExpeditionGoal需要正确实现Goal接口并与现有GOAP系统集成
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with T9, T11, T12)
  - **Blocks**: T13
  - **Blocked By**: T9

  **References**:

  **Pattern References** (existing code to follow):
  - `client/src/commonMain/kotlin/com/sect/game/goap/goals/SurvivalGoal.kt` - Goal实现参考
  - `client/src/commonMain/kotlin/com/sect/game/goap/goals/Goal.kt` - Goal接口

  **API/Type References**:
  - `Goal` - GOAP目标接口
  - `WorldState` - GOAP状态

  **Acceptance Criteria**:
  - [ ] 测试：ExpeditionGoal正确实现Goal接口
  - [ ] 测试：isGoalSatisfied()在到达最深层时返回true
  - [ ] 测试：优先级为60
  - [ ] ./gradlew jvmTest --tests "*ExpeditionGoal*" → PASS

  **QA Scenarios**:
  ```
  Scenario: 历练目标GOAP集成
    Tool: Bash
    Preconditions: T9完成
    Steps:
      1. Run: ./gradlew jvmTest --tests "com.sect.game.engine.expedition.ExpeditionGoalTest"
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-10-goal-test.log
  ```

- [ ] 11. **ExpeditionAction 历练行动**

  **What to do**:
  - 创建 `ExpeditionAction.kt`，实现 `Action` 接口
  - 行动ID：`"expedition"`
  - 前置条件：`fatigue < 70, health > 50`
  - 效果：`isOnExpedition = true`
  - 成本：10（高于修炼的1）

  **Must NOT do**:
  - 不要修改现有Action实现
  - 不要影响现有GOAP规划

  **Recommended Agent Profile**:
  > **deep** - GOAP行动集成
  - **Category**: `deep`
    - Reason: ExpeditionAction需要正确实现Action接口
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with T9, T10, T12)
  - **Blocks**: T13
  - **Blocked By**: T9

  **References**:

  **Pattern References** (existing code to follow):
  - `client/src/commonMain/kotlin/com/sect/game/goap/actions/CultivationAction.kt` - Action实现参考
  - `client/src/commonMain/kotlin/com/sect/game/goap/actions/Action.kt` - Action接口

  **API/Type References**:
  - `Action` - GOAP行动接口
  - `WorldState` - GOAP状态

  **Acceptance Criteria**:
  - [ ] 测试：ExpeditionAction正确实现Action接口
  - [ ] 测试：前置条件检查正确
  - [ ] 测试：效果设置isOnExpedition=true
  - [ ] ./gradlew jvmTest --tests "*ExpeditionAction*" → PASS

  **QA Scenarios**:
  ```
  Scenario: 历练行动GOAP集成
    Tool: Bash
    Preconditions: T9完成
    Steps:
      1. Run: ./gradlew jvmTest --tests "com.sect.game.engine.expedition.ExpeditionActionTest"
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-11-action-test.log
  ```

- [ ] 12. **GameEngine 模式切换**

  **What to do**:
  - 在 `GameEngine.kt` 中添加 `GameMode` enum：`GOAP`, `COMBAT`
  - 添加 `currentMode: GameMode` 属性
  - 添加 `setMode(mode: GameMode)` 方法
  - 修改 `tick()` 方法：
    - `GOAP` 模式：调用现有 `updateDisciples()`
    - `COMBAT` 模式：调用 `updateCombat()`
  - 创建内部方法 `updateCombat()` 处理战斗回合循环
  - 战斗结束后自动调用 `setMode(GOAP)` 返回GOAP模式

  **Must NOT do**:
  - 不要删除或破坏现有的GOAP逻辑
  - 不要修改现有的tickRate行为

  **Recommended Agent Profile**:
  > **deep** - 核心引擎修改
  - **Category**: `deep`
    - Reason: GameEngine是核心，需要谨慎修改模式切换逻辑
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with T9, T10, T11)
  - **Blocks**: T13
  - **Blocked By**: T9

  **References**:

  **Pattern References** (existing code to follow):
  - `client/src/commonMain/kotlin/com/sect/game/engine/GameEngine.kt:79-106` - tick()和updateDisciples()实现

  **API/Type References**:
  - `ExpeditionState` - T9
  - `CombatExecutor` - T5
  - `TurnBasedCombatPlanner` - T6

  **Acceptance Criteria**:
  - [ ] 测试：GameEngine正确切换GOAP/COMBAT模式
  - [ ] 测试：tick()在GOAP模式下调用updateDisciples()
  - [ ] 测试：战斗结束后自动返回GOAP模式
  - [ ] ./gradlew jvmTest --tests "*GameEngine*" → PASS

  **QA Scenarios**:
  ```
  Scenario: 游戏引擎模式切换
    Tool: Bash
    Preconditions: T9-T11完成
    Steps:
      1. Run: ./gradlew jvmTest --tests "com.sect.game.engine.GameEngineModeSwitchTest"
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-12-mode-switch-test.log
  ```

- [ ] 13. **End-to-End 集成测试**

  **What to do**:
  - 创建 `ExpeditionIntegrationTest.kt`
  - 测试完整流程：
    1. GOAP模式正常运行
    2. GOAP规划出ExpeditionAction
    3. 切换到COMBAT模式
    4. 执行完整战斗（多回合）
    5. 战斗结束（胜利或20回合限制）
    6. 返回GOAP模式
    7. 发放奖励
  - 测试3层地图递进：
    1. 第1层战斗胜利 → 进入第2层
    2. 第2层战斗胜利 → 进入第3层
    3. 第3层战斗胜利 → 返回GOAP

  **Must NOT do**:
  - 不要测试具体伤害数值（已由T3覆盖）
  - 不要测试UI（无UI）

  **Recommended Agent Profile**:
  > **deep** - 集成测试
  - **Category**: `deep`
    - Reason: 端到端测试需要理解完整流程
  - **Skills**: `[]`

  **Parallelization**:
  - **Can Run In Parallel**: NO (最终集成，必须在T9-T12后)
  - **Parallel Group**: Wave 3 (T9-T12之后)
  - **Blocks**: F1, F2, F3, F4
  - **Blocked By**: T10, T11, T12

  **References**:

  **Pattern References** (existing code to follow):
  - `client/src/commonTest/kotlin/com/sect/game/e2e/GameE2ETest.kt` - E2E测试风格参考

  **API/Type References**:
  - `GameEngine` - T12修改后的完整引擎
  - `ExpeditionState` - T9

  **Acceptance Criteria**:
  - [ ] 测试：GOAP→COMBAT→GOAP完整切换流程
  - [ ] 测试：3层地图递进正确
  - [ ] 测试：战斗超时（20回合）正确处理
  - [ ] ./gradlew jvmTest --tests "*ExpeditionIntegration*" → PASS

  **QA Scenarios**:
  ```
  Scenario: 端到端远征流程
    Tool: Bash
    Preconditions: T1-T12全部完成
    Steps:
      1. Run: ./gradlew jvmTest --tests "com.sect.game.engine.expedition.ExpeditionIntegrationTest"
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-13-e2e-test.log
  ```

---

## Final Verification Wave

- [ ] F1. **Plan Compliance Audit** — `oracle`

  Read the plan end-to-end. For each "Must Have": verify implementation exists (read file, run test). For each "Must NOT Have": search codebase for forbidden patterns. Check evidence files exist.

  **QA Scenarios**:
  ```
  Scenario: 计划合规审计
    Tool: oracle
    Preconditions: T1-T13完成
    Steps:
      1. 验证所有Must Have已实现
      2. 验证Must NOT Have未出现
      3. 检查所有evidence文件存在
    Expected Result: VERDICT: APPROVE
    Evidence: .sisyphus/evidence/final-f1-audit.log
  ```

- [ ] F2. **Code Quality Review** — `unspecified-high`

  Run `./gradlew jvmTest` and `./gradlew check`. Review all new files for: `as any`/`@ts-ignore`, empty catches, console.log in prod, commented-out code, unused imports.

  **QA Scenarios**:
  ```
  Scenario: 代码质量审查
    Tool: unspecified-high
    Preconditions: T1-T13完成
    Steps:
      1. Run: ./gradlew jvmTest
      2. Run: ./gradlew check
      3. Review new files for code quality issues
    Expected Result: Build [PASS] | Tests [ALL PASS] | Lint [PASS]
    Evidence: .sisyphus/evidence/final-f2-quality.log
  ```

- [ ] F3. **Real Manual QA** — `unspecified-high`

  Start from clean state. Execute integration test scenario from T13. Verify full flow works without human intervention.

  **QA Scenarios**:
  ```
  Scenario: 真实QA验证
    Tool: unspecified-high
    Preconditions: 全量编译通过
    Steps:
      1. Run: ./gradlew jvmTest --tests "*ExpeditionIntegration*"
      2. Verify all tests pass
      3. Verify evidence files created
    Expected Result: 所有测试通过，evidence文件存在
    Evidence: .sisyphus/evidence/final-f3-qa.log
  ```

- [ ] F4. **Scope Fidelity Check** — `deep`

  For each task: read "What to do", read actual diff. Verify 1:1 — everything in spec was built, nothing beyond spec was built. Check "Must NOT do" compliance.

  **QA Scenarios**:
  ```
  Scenario: 范围忠实度检查
    Tool: deep
    Preconditions: T1-T13完成
    Steps:
      1. Verify each task's "What to do" matches actual implementation
      2. Verify no "Must NOT do" violations
      3. Verify no scope creep
    Expected Result: Tasks [N/N compliant] | Contamination [CLEAN]
    Evidence: .sisyphus/evidence/final-f4-scope.log
  ```

---

## Commit Strategy

| # | Commit | Files |
|---|--------|-------|
| 1 | `feat(combat): 战斗属性和单元值对象` | CombatStats.kt, CombatUnit.kt |
| 2 | `feat(combat): 回合制阶段机和伤害计算` | CombatPhase.kt, DamageCalculator.kt |
| 3 | `feat(combat): 速度排序和行动顺序` | TurnOrderCalculator.kt |
| 4 | `feat(combat): 战斗执行器和规划器` | CombatExecutor.kt, TurnBasedCombatPlanner.kt |
| 5 | `feat(combat): 怪物工厂和奖励计算` | MonsterFactory.kt, RewardCalculator.kt |
| 6 | `feat(expedition): 远征状态和GOAP集成` | ExpeditionState.kt, ExpeditionGoal.kt |
| 7 | `feat(engine): 游戏引擎模式切换` | GameEngine.kt |
| 8 | `test(integration): 端到端集成测试` | ExpeditionIntegrationTest.kt |

---

## Success Criteria

### Verification Commands
```bash
./gradlew jvmTest --tests "com.sect.game.engine.combat.*"  # 所有战斗测试通过
./gradlew jvmTest --tests "com.sect.game.engine.expedition.*"  # 远征测试通过
./gradlew check  # 全量检查通过
```

### Final Checklist
- [ ] 所有战斗逻辑测试通过
- [ ] GOAP→战斗→GOAP模式切换正常
- [ ] 3层地图递进正确
- [ ] 无新引入的LSP错误
