# 属性系统实现计划

## TL;DR

> **快速摘要**：为修仙游戏实现高扩展性属性系统，支持弟子、装备、技能、Buff 等多种实体。
> 
> **核心交付**：
> - `AttributeKey<T>` / `AttributeValue` 属性类型系统
> - `Modifier` 修饰器系统（固定值、百分比、临时）
> - `Condition` 条件触发系统
> - `AttributeProvider` 提供者接口 + `ProviderRegistry`
> - `Equipment`、`Skill`、`Buff` 领域模型
> - 与现有 `Disciple` 的渐进式集成
> 
> **Estimated Effort**: Medium
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: Wave 1 (Foundation) → Wave 2 (Provider) → Wave 3 (Integration)

---

## Context

### 项目背景
- Kotlin Multiplatform 项目，使用 Gradle 构建
- `business/domain` 模块定义领域模型
- 遵循现有模式：`data class`、`Result<T>`、`companion object.create()`
- 现有 `Attributes` 值对象（spiritRoot, talent, luck）需要兼容

### 用户需求
- 战斗属性、生活属性、状态值、资质属性
- 装备提供属性加成和技能插槽
- 技能提供被动/主动效果
- Buff/Debuff 支持持续时间和叠加
- 高扩展性，支持未来扩展（灵兽、建筑、阵法等）

---

## Work Objectives

### Core Objective
在 `business/domain/src/commonMain/kotlin/com/sect/game/domain/attribute/` 下实现完整的属性系统框架。

### Concrete Deliverables
- `attribute/value/`: `AttributeValue` 接口 + `IntValue`/`FloatValue`/`BoolValue`/`PercentValue`
- `attribute/key/`: `AttributeKey<T>` 内联值类 + `AttributeMeta` 元信息
- `attribute/set/`: `AttributeSet` 属性集合
- `attribute/modifier/`: `Modifier` 接口 + `FlatModifier`/`PercentModifier`/`TempModifier`
- `attribute/condition/`: `Condition` 接口 + `Always`/`AttributeThreshold`
- `attribute/provider/`: `AttributeProvider` 接口 + `ProviderType` 枚举 + `ProviderRegistry`
- `domain/Equipment.kt` - 装备领域模型
- `domain/Skill.kt` - 技能领域模型
- `domain/Buff.kt` - Buff 领域模型
- 与现有 `Disciple` 的集成扩展

### Definition of Done
- [ ] `./gradlew :business:domain:jvmTest` 全部通过
- [ ] `./gradlew detekt` 无警告
- [ ] 现有 `DiscipleTest` 仍然通过（向后兼容）

### Must Have
- 属性类型安全（`AttributeKey<T>`）
- 属性值校验（范围约束）
- 修饰器计算（固定值、百分比）
- 条件触发（`Always`、`AttributeThreshold`）
- 提供者注册表（`ProviderRegistry`）

### Must NOT Have (Guardrails)
- **禁止**运行时动态创建 AttributeKey（必须编译时声明）
- **禁止**修饰器中有副作用（Condition.evaluate() 必须是纯函数）
- **禁止**修改现有 `Attributes.kt`、`Disciple.kt` 的签名
- **禁止**在 domain 模块引入 UI 框架（Compose 等）
- **禁止**添加新依赖
- **延期**：SkillSlot 宝石镶嵌系统
- **延期**：SetBonus 套装效果系统
- **延期**：复杂 Condition（And/Or/Not 组合）

---

## Verification Strategy

### Test Infrastructure
- **Framework**: kotlin-test（现有项目使用）
- **Test Location**: `business/domain/src/commonTest/kotlin/com/sect/game/domain/attribute/`
- **Naming**: `{Component}Test.kt`（如 `AttributeKeyTest.kt`）

### QA Policy
Every task includes agent-executed QA scenarios. Evidence saved to `.sisyphus/evidence/task-{N}-{scenario}.{ext}`.

- **Unit Tests**: 运行 `./gradlew :business:domain:jvmTest --tests "*.TestName*"`
- **Integration**: 运行 `./gradlew :business:domain:jvmTest`
- **Full Check**: `./gradlew check`

---

## Execution Strategy

### Wave 1: Foundation（并行，无依赖）
```
Task 1: AttributeValue + AttributeKey 基础
Task 2: AttributeSet 属性集合
Task 3: Modifier 修饰器系统
Task 4: Condition 条件系统
```

### Wave 2: Provider 架构（依赖 Wave 1）
```
Task 5: Provider 系统
Task 6: Domain Models (Equipment, Skill, Buff)
```

### Wave 3: Integration（依赖 Wave 2）
```
Task 7: Disciple 集成
Task 8: 全面测试
```

### Dependency Matrix

| Task | Blocks | Blocked By |
|------|--------|------------|
| 1 | 2, 3, 4 | - |
| 2 | 5, 7 | 1 |
| 3 | 5, 7 | 1 |
| 4 | 5, 7 | 1 |
| 5 | 6, 7 | 2, 3, 4 |
| 6 | 7, 8 | 5 |
| 7 | 8 | 5, 6 |
| 8 | - | 7 |

---

## TODOs

- [x] **1. AttributeValue + AttributeKey 基础**

  **What to do**:
  创建 `attribute/value/` 和 `attribute/key/` 包：
  - `AttributeValue.kt`: sealed 接口 + `IntValue`、`FloatValue`、`BoolValue`、`PercentValue` 实现
  - `AttributeKey.kt`: `@JvmInline value class AttributeKey<T : AttributeValue>(val name: String)`
  - `AttributeMeta.kt`: 属性元信息（默认值、范围、标签）
  - `AttributeTag.kt`: 标签枚举（Combat、Life、Status、Talent）
  - 预定义属性键常量（ATTACK、DEFENSE、SPIRIT_ROOT 等）

  **Must NOT do**:
  - 不实现序列化逻辑（延期到数据层）
  - 不实现 Boolean 属性的复杂逻辑（仅用于标志位）

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 需要设计类型层级和接口契约
  - **Skills**: []
    - Reason: 纯 Kotlin 类型系统设计

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3, 4)
  - **Blocks**: 2, 3, 4
  - **Blocked By**: None

  **References**:
  - `business/domain/src/commonMain/kotlin/com/sect/game/domain/valueobject/Attributes.kt` - 值对象模式参考
  - `business/domain/src/commonMain/kotlin/com/sect/game/domain/valueobject/Realm.kt` - sealed class 枚举模式

  **Acceptance Criteria**:
  - [ ] `AttributeKey<IntValue>("test")` 可以创建
  - [ ] `IntValue(100)` 可以创建并通过范围校验
  - [ ] `PercentValue(1.5f)` 创建时抛出异常（范围 0-1）
  - [ ] `AttributeMeta(defaultValue = IntValue(10), range = 0..100)` 可以创建

  **QA Scenarios**:
  ```
  Scenario: AttributeKey 类型安全
    Tool: Bash
    Preconditions: 无
    Steps:
      1. 创建两个不同类型的 AttributeKey
      2. 验证它们的 name 相同但类型不同
    Expected Result: 两个 key 可以创建，equals 比较为 false
    Evidence: .sisyphus/evidence/task-1-type-safety.txt

  Scenario: PercentValue 范围校验
    Tool: Bash
    Preconditions: 无
    Steps:
      1. 创建 PercentValue(0.5f) - 应成功
      2. 创建 PercentValue(1.5f) - 应抛出异常
    Expected Result: 第一个成功，第二个抛出 IllegalArgumentException
    Evidence: .sisyphus/evidence/task-1-percent-validation.txt
  ```

- [x] **2. AttributeSet 属性集合**

  **What to do**:
  创建 `attribute/set/` 包：
  - `AttributeSet.kt`: 基于 `Map<AttributeKey<*>, Attribute<*>>` 的集合
    - `operator fun <T : AttributeValue> get(key: AttributeKey<T>): T?`
    - `fun contains(key: AttributeKey<*>): Boolean`
    - `fun with(key: AttributeKey<*>, value: AttributeValue): AttributeSet`
    - `fun compute(key: AttributeKey<*>, modifiers: List<Modifier>, context: ComputationContext): AttributeValue?`
  - `ComputationContext.kt`: 计算上下文（source、target、currentHp、currentMp）

  **Must NOT do**:
  - 不实现可变操作（所有操作返回新副本，保持不可变性）
  - 不实现复杂的派生属性计算

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 集合设计和计算逻辑
  - **Skills**: []
    - Reason: 纯 Kotlin

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3, 4)
  - **Blocks**: 5, 7
  - **Blocked By**: 1

  **References**:
  - `business/domain/src/commonMain/kotlin/com/sect/game/domain/entity/Disciple.kt` - 现有计算模式参考

  **Acceptance Criteria**:
  - [ ] `AttributeSet.EMPTY` 可用
  - [ ] `attributeSet.with(ATTACK, IntValue(100))` 返回新集合
  - [ ] `attributeSet[ATTACK]` 返回 `IntValue(100)`
  - [ ] `attributeSet.compute(ATTACK, modifiers, context)` 正确应用修饰器

  **QA Scenarios**:
  ```
  Scenario: AttributeSet 不可变性
    Tool: Bash
    Preconditions: 创建包含 ATTACK 的 AttributeSet
    Steps:
      1. 调用 with() 添加新属性
      2. 验证原集合未改变
    Expected Result: 原集合保持不变，新集合包含新属性
    Evidence: .sisyphus/evidence/task-2-immutability.txt

  Scenario: compute 应用固定值修饰器
    Tool: Bash
    Preconditions: AttributeSet 包含 ATTACK = 50
    Steps:
      1. 应用 FlatModifier(value = +10)
      2. 调用 compute()
    Expected Result: 结果为 60
    Evidence: .sisyphus/evidence/task-2-flat-modifier.txt

  Scenario: compute 应用百分比修饰器
    Tool: Bash
    Preconditions: AttributeSet 包含 ATTACK = 50
    Steps:
      1. 应用 PercentModifier(percent = 0.2f)
      2. 调用 compute()
    Expected Result: 结果为 60 (50 * 1.2)
    Evidence: .sisyphus/evidence/task-2-percent-modifier.txt
  ```

- [x] **3. Modifier 修饰器系统**

  **What to do**:
  创建 `attribute/modifier/` 包：
  - `Modifier.kt`: sealed 接口，定义 `id`、`targetKey`、`condition`、`source`
  - `FlatModifier.kt`: 固定值修饰器（`value: Int`）
  - `PercentModifier.kt`: 百分比修饰器（`percent: Float`）
  - `TempModifier.kt`: 临时修饰器（带持续时间）
  - `ModifierSource.kt`: 来源追踪（`EquipmentSource`、`SkillSource`、`BuffSource`）
  - `SourceType.kt`: 枚举（Equipment、Skill、Buff）

  **叠加规则**: 先计算所有固定值加成，再计算百分比加成

  **Must NOT do**:
  - 不实现复杂的时间相关修饰逻辑（TempModifier 仅记录 duration，触发由使用方处理）
  - 不实现修饰器优先级系统

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 设计修饰器接口和叠加规则
  - **Skills**: []
    - Reason: 纯 Kotlin

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 4)
  - **Blocks**: 5, 7
  - **Blocked By**: 1

  **References**:
  - 现有 `Disciple.kt` 中的 `calculateCultivationGain()` 方法 - 现有属性计算模式

  **Acceptance Criteria**:
  - [ ] `FlatModifier` 可以创建并应用
  - [ ] `PercentModifier` 可以创建并应用
  - [ ] `TempModifier` 包含 `duration` 字段
  - [ ] `ModifierSource` 正确追踪来源类型

  **QA Scenarios**:
  ```
  Scenario: FlatModifier 应用
    Tool: Bash
    Preconditions: 基础值 50
    Steps:
      1. 创建 FlatModifier(value = 10)
      2. 调用 apply(IntValue(50))
    Expected Result: 返回 IntValue(60)
    Evidence: .sisyphus/evidence/task-3-flat-apply.txt

  Scenario: PercentModifier 叠加
    Tool: Bash
    Preconditions: 基础值 50
    Steps:
      1. 应用 FlatModifier(+10) → 60
      2. 应用 PercentModifier(20%) → 60 * 1.2 = 72
    Expected Result: 最终值 72
    Evidence: .sisyphus/evidence/task-3-stacking.txt

  Scenario: 条件不满足时修饰器无效
    Tool: Bash
    Preconditions: 属性值 10，条件为 value > 20
    Steps:
      1. 创建带条件的 FlatModifier
      2. 调用 compute() 时条件不满足
    Expected Result: 修饰器被忽略，返回基础值
    Evidence: .sisyphus/evidence/task-3-condition-fail.txt
  ```

- [x] **4. Condition 条件系统**

  **What to do**:
  创建 `attribute/condition/` 包：
  - `Condition.kt`: sealed 接口
    - `Always`: 无条件生效
    - `AttributeThreshold`: 属性阈值条件（`key`、`operator`、`threshold`）
  - `ComparisonOperator.kt`: 比较操作符枚举

  **延期**: And、Or、Not 组合条件（Phase 2）

  **Must NOT do**:
  - 不在 `evaluate()` 中产生副作用
  - 不实现复杂的时间条件（Phase 2）

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 条件接口设计
  - **Skills**: []
    - Reason: 纯 Kotlin

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 3)
  - **Blocks**: 5, 7
  - **Blocked By**: 1

  **References**:
  - `business/domain/src/commonMain/kotlin/com/sect/game/domain/entity/Disciple.kt` - 现有条件判断模式（如 `isDead()`、`isExhausted()`）

  **Acceptance Criteria**:
  - [ ] `Condition.Always.evaluate()` 始终返回 true
  - [ ] `AttributeThreshold(MAX_HP, GREATER_THAN, 30)` 在 HP > 30 时返回 true
  - [ ] 阈值条件在上下文中正确获取属性值

  **QA Scenarios**:
  ```
  Scenario: Always 条件始终生效
    Tool: Bash
    Preconditions: 空上下文
    Steps:
      1. 调用 Condition.Always.evaluate(context)
    Expected Result: 返回 true
    Evidence: .sisyphus/evidence/task-4-always.txt

  Scenario: AttributeThreshold 正确判断
    Tool: Bash
    Preconditions: context 中 MAX_HP = 50
    Steps:
      1. 创建条件 MAX_HP > 30
      2. 调用 evaluate()
    Expected Result: 返回 true
    Evidence: .sisyphus/evidence/task-4-threshold.txt

  Scenario: 阈值条件不满足
    Tool: Bash
    Preconditions: context 中 MAX_HP = 20
    Steps:
      1. 创建条件 MAX_HP > 30
      2. 调用 evaluate()
    Expected Result: 返回 false
    Evidence: .sisyphus/evidence/task-4-threshold-fail.txt
  ```

- [x] **5. Provider 系统**

  **What to do**:
  创建 `attribute/provider/` 包：
  - `AttributeProvider.kt`: 接口，定义 `id`、`name`、`providerType`、`getModifiers()`、`getSkillSlots()`、`getMetaAttributes()`
  - `ProviderType.kt`: 枚举（Equipment、Gem、Enchantment、SetBonus、Skill、Buff、Building、Formation、Title、Pet）
  - `ProviderRegistry.kt`: 全局注册表（`register()`、`get()`、`getByType()`）
  - `UniversalProvider.kt`: 通用提供者实现

  **ProviderRegistry 设计**: 使用单例模式，但提供测试钩子

  **Must NOT do**:
  - 不实现 DSL（Phase 2）
  - 不实现复杂的提供者优先级

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 架构设计
  - **Skills**: []
    - Reason: 纯 Kotlin

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 2
  - **Blocks**: 6, 7
  - **Blocked By**: 1, 2, 3, 4

  **References**:
  - `business/domain/src/commonMain/kotlin/com/sect/game/domain/entity/Sect.kt` - 现有管理器模式

  **Acceptance Criteria**:
  - [ ] `AttributeProvider` 接口可被实现
  - [ ] `ProviderRegistry.register()` 可注册提供者
  - [ ] `ProviderRegistry.getByType()` 可按类型查询
  - [ ] `UniversalProvider` 可快速创建提供者

  **QA Scenarios**:
  ```
  Scenario: Provider 注册和查询
    Tool: Bash
    Preconditions: 无
    Steps:
      1. 创建 UniversalProvider
      2. 注册到 ProviderRegistry
      3. 通过 getByType(Equipment) 查询
    Expected Result: 返回注册的提供者
    Evidence: .sisyphus/evidence/task-5-registry.txt

  Scenario: getModifiers 收集所有修饰器
    Tool: Bash
    Preconditions: Provider 包含多个 Modifier
    Steps:
      1. 调用 getModifiers()
    Expected Result: 返回所有修饰器列表
    Evidence: .sisyphus/evidence/task-5-get-modifiers.txt
  ```

- [x] **6. Domain Models（Equipment、Skill、Buff）**

  **What to do**:
  在 `business/domain/src/commonMain/kotlin/com/sect/game/domain/` 创建：
  - `Equipment.kt`: 装备领域模型
    - 属性：`id`、`name`、`slot`、`rarity`、`modifiers`
    - 方法：`companion object.create()`
  - `Skill.kt`: 技能领域模型
    - 属性：`id`、`name`、`type`、`modifiers`、`cooldown`
    - 方法：`isPassive()`
  - `Buff.kt`: Buff 领域模型
    - 属性：`id`、`name`、`modifiers`、`duration`、`stackable`
    - 方法：`tick()`、`isExpired()`

  **延期**: SkillSlot 宝石插槽、SetBonus 套装效果

  **Must NOT do**:
  - 不实现装备强化逻辑
  - 不实现宝石镶嵌兼容性检查
  - 不实现套装效果计算

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 领域模型设计
  - **Skills**: []
    - Reason: 纯 Kotlin

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Task 5, but actually after 5)
  - **Blocks**: 7, 8
  - **Blocked By**: 5

  **References**:
  - `business/domain/src/commonMain/kotlin/com/sect/game/domain/entity/Disciple.kt` - 现有实体模式
  - `business/domain/src/commonMain/kotlin/com/sect/game/domain/valueobject/Attributes.kt` - 值对象模式

  **Acceptance Criteria**:
  - [ ] `Equipment.create()` 返回 `Result<Equipment>`
  - [ ] `Skill.isPassive()` 正确判断被动技能
  - [ ] `Buff.tick()` 减少持续时间
  - [ ] `Buff.isExpired()` 正确判断过期

  **QA Scenarios**:
  ```
  Scenario: Equipment 创建成功
    Tool: Bash
    Preconditions: 无
    Steps:
      1. 调用 Equipment.create() 提供有效参数
    Expected Result: 返回 Result.success(Equipment)
    Evidence: .sisyphus/evidence/task-6-equipment-create.txt

  Scenario: Equipment 创建失败
    Tool: Bash
    Preconditions: 无
    Steps:
      1. 调用 Equipment.create() 提供空名称
    Expected Result: 返回 Result.failure
    Evidence: .sisyphus/evidence/task-6-equipment-fail.txt

  Scenario: Buff 持续时间递减
    Tool: Bash
    Preconditions: Buff(duration = 5)
    Steps:
      1. 调用 tick()
    Expected Result: remainingTurns = 4
    Evidence: .sisyphus/evidence/task-6-buff-tick.txt

  Scenario: Buff 过期判断
    Tool: Bash
    Preconditions: Buff(remainingTurns = 0, duration != -1)
    Steps:
      1. 调用 isExpired()
    Expected Result: 返回 true
    Evidence: .sisyphus/evidence/task-6-buff-expired.txt
  ```

- [x] **7. Disciple 集成**

  **What to do**:
  创建集成层：
  - `DiscipleAttributes.kt`: 属性键定义对象
    - `SPIRIT_ROOT`、`TALENT`、`LUCK` 键定义
    - 映射到现有 `Attributes` 字段
  - `Extensions.kt`: 扩展函数
    - `Attributes.toAttributeSet()`: 旧 → 新
    - `AttributeSet.toAttributes()`: 新 → 旧
  - 在 `Disciple` 中添加计算属性：
    - `effectiveAttributes: AttributeSet`（延迟计算）
    - `computeAttribute(key: AttributeKey<IntValue>): Int`

  **迁移策略**: 渐进式 - Disciple 保留现有 `attributes: Attributes`，新增 `effectiveAttributes` 计算

  **Must NOT do**:
  - 不修改现有 `Disciple` 的 `attributes: Attributes` 字段
  - 不修改现有 `Disciple` 的 `calculateCultivationGain()` 等方法
  - 不破坏现有 `DiscipleTest`

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 集成设计
  - **Skills**: []
    - Reason: 纯 Kotlin

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3
  - **Blocks**: 8
  - **Blocked By**: 5, 6

  **References**:
  - `business/domain/src/commonMain/kotlin/com/sect/game/domain/entity/Disciple.kt` - 现有实体
  - `business/domain/src/commonMain/kotlin/com/sect/game/domain/valueobject/Attributes.kt` - 现有属性

  **Acceptance Criteria**:
  - [ ] `Attributes.toAttributeSet()` 返回包含 SPIRIT_ROOT、TALENT、LUCK 的 AttributeSet
  - [ ] `AttributeSet.toAttributes()` 返回正确的 Attributes
  - [ ] `Disciple.computeAttribute(SPIRIT_ROOT)` 返回计算后的值
  - [ ] 现有 `DiscipleTest` 仍然通过

  **QA Scenarios**:
  ```
  Scenario: toAttributeSet 转换
    Tool: Bash
    Preconditions: Attributes(spiritRoot=60, talent=70, luck=80)
    Steps:
      1. 调用 toAttributeSet()
      2. 查询 SPIRIT_ROOT 值
    Expected Result: SPIRIT_ROOT = 60
    Evidence: .sisyphus/evidence/task-7-conversion.txt

  Scenario: Disciple 计算属性
    Tool: Bash
    Preconditions: Disciple with base SPIRIT_ROOT=60, no modifiers
    Steps:
      1. 调用 computeAttribute(SPIRIT_ROOT)
    Expected Result: 返回 60
    Evidence: .sisyphus/evidence/task-7-compute.txt

  Scenario: 现有 DiscipleTest 通过
    Tool: Bash
    Preconditions: 无
    Steps:
      1. 运行 ./gradlew :business:domain:jvmTest --tests "DiscipleTest"
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/task-7-disciple-test.txt
  ```

- [x] **8. 全面测试**

  **What to do**:
  创建完整测试覆盖：
  - `AttributeValueTest.kt`: Int/Float/Bool/Percent 边界测试
  - `AttributeKeyTest.kt`: 类型安全测试
  - `AttributeSetTest.kt`: 集合操作测试
  - `ModifierTest.kt`: 修饰器叠加测试
  - `ConditionTest.kt`: 条件判断测试
  - `ProviderTest.kt`: 提供者接口测试
  - `EquipmentTest.kt`: 装备创建测试
  - `SkillTest.kt`: 技能判断测试
  - `BuffTest.kt`: Buff 持续测试
  - `DiscipleIntegrationTest.kt`: 集成测试

  **Must NOT do**:
  - 不写需要人工验证的测试
  - 不跳过任何失败测试

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 测试设计
  - **Skills**: []
    - Reason: 纯 Kotlin

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3
  - **Blocks**: Final Verification
  - **Blocked By**: 7

  **References**:
  - `business/domain/src/commonTest/kotlin/com/sect/game/domain/valueobject/AttributesTest.kt` - 现有测试模式

  **Acceptance Criteria**:
  - [ ] 所有 AttributeValue 类型测试覆盖
  - [ ] Modifier 叠加规则测试覆盖
  - [ ] Condition 所有场景测试覆盖
  - [ ] Disciple 集成测试通过

  **QA Scenarios**:
  ```
  Scenario: 修饰器叠加 - 固定值先于百分比
    Tool: Bash
    Preconditions: 基础值 100，多个修饰器
    Steps:
      1. 应用 FlatModifier(+50) → 150
      2. 应用 PercentModifier(20%) → 180
      3. 验证最终值
    Expected Result: 180
    Evidence: .sisyphus/evidence/task-8-stacking-order.txt

  Scenario: 多个条件修饰器
    Tool: Bash
    Preconditions: 属性值 50，context HP=20
    Steps:
      1. 应用条件修饰器（HP<30 时 +10）
      2. 计算最终值
    Expected Result: 60（基础50 + 条件10）
    Evidence: .sisyphus/evidence/task-8-conditional-modifier.txt
  ```

---

## Final Verification Wave

- [x] **F1. Plan Compliance Audit** — `oracle`
  读取计划 end-to-end，验证：
  - 每个 Must Have 存在实现
  - 每个 Must NOT Have 未违反
  - 延期项目未在 Phase 1 实现
  Output: `Must Have [8/8] | Must NOT Have [6/6] | Deferred [3] | VERDICT: APPROVE`

- [x] **F2. Code Quality Review** — `unspecified-high`
  运行 `./gradlew :business:domain:detekt` 和 `./gradlew :business:domain:jvmTest`
  Output: `Detekt [N/A - no detekt task] | Tests [ALL PASS] | VERDICT: APPROVE`

- [x] **F3. Backward Compatibility Check** — `unspecified-high`
  运行现有测试：
  - `./gradlew :business:domain:jvmTest --tests "AttributesTest"`
  - `./gradlew :business:domain:jvmTest --tests "DiscipleTest"`
  Output: `AttributesTest [PASS] | DiscipleTest [PASS] | VERDICT: APPROVE`

- [x] **F4. Scope Fidelity Check** — `deep`
  对比计划中定义的 Scope 和实际实现：
  - 检查是否漏掉任何 Must Have
  - 检查是否有计划外的额外实现
  Output: `Scope [8/8 compliant] | Extra [CLEAN] | VERDICT: APPROVE`

---

## Commit Strategy

- **C1**: `feat(attribute): 添加 AttributeValue 和 AttributeKey 基础类型` — value/, key/
- **C2**: `feat(attribute): 添加 AttributeSet 属性集合` — set/
- **C3**: `feat(attribute): 添加 Modifier 修饰器系统` — modifier/
- **C4**: `feat(attribute): 添加 Condition 条件系统` — condition/
- **C5**: `feat(attribute): 添加 Provider 系统` — provider/
- **C6**: `feat(attribute): 添加 Equipment、Skill、Buff 领域模型` — domain/Equipment.kt 等
- **C7**: `feat(attribute): 添加 Disciple 集成层` — Extensions.kt, DiscipleAttributes.kt
- **C8**: `test(attribute): 添加完整测试覆盖` — commonTest/attribute/

---

## Success Criteria

### Verification Commands
```bash
./gradlew :business:domain:jvmTest                  # 所有测试通过
./gradlew :business:domain:detekt                    # 代码风格通过
./gradlew :business:domain:jvmTest --tests "DiscipleTest"  # 现有测试通过
```

### Final Checklist
- [ ] 所有 8 个 Must Have 存在
- [ ] 所有 6 个 Must NOT Have 未违反
- [ ] 延期项目（SkillSlot、SetBonus、复杂 Condition）未实现
- [ ] 现有 DiscipleTest 通过
- [ ] 现有 AttributesTest 通过
- [ ] 新增测试覆盖所有组件