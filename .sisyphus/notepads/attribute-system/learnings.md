# 属性系统学习笔记

## 项目约定

### 值对象模式
- 使用 `data class` + `Result<T>` + `companion object.create()`
- 参考 `Attributes.kt`、`Realm.kt`

### sealed class 模式
- 使用 sealed interface/class 实现枚举类型
- 参考 `Realm.kt`

### 目录结构
- 领域模型: `business/domain/src/commonMain/kotlin/com/sect/game/domain/`
- 测试: `business/domain/src/commonTest/kotlin/com/sect/game/domain/`
- attribute 包: `business/domain/src/commonMain/kotlin/com/sect/game/domain/attribute/`

### 命名规范
- 文件名: PascalCase（如 `AttributeValue.kt`）
- 类名: PascalCase
- 函数名: camelCase
- 常量: UPPER_SNAKE_CASE

## 关键实现决策

### AttributeKey<T> 设计
- 使用 `@JvmInline value class` 实现类型安全的属性键
- 内联值类包装 `String name`
- 泛型约束 `T : AttributeValue`

### AttributeValue 类型
- IntValue: 整型属性
- FloatValue: 浮点属性
- BoolValue: 布尔标志
- PercentValue: 百分比（0-1 范围）

### 修饰器叠加规则
- 先计算所有固定值加成
- 再计算百分比加成
- TempModifier 仅记录 duration，触发逻辑由使用方处理

## 已知约束

### Must NOT Have
- 禁止运行时动态创建 AttributeKey
- 禁止 Condition.evaluate() 有副作用
- 禁止修改现有 Attributes.kt、Disciple.kt 签名
- 禁止在 domain 模块引入 UI 框架
- 禁止添加新依赖

### 延期项目
- SkillSlot 宝石镶嵌系统
- SetBonus 套装效果
- 复杂 Condition（And/Or/Not）

## Wave 1 实现记录

### 已完成的文件
- `attribute/value/AttributeValue.kt`: sealed interface，定义 toInt/toFloat/toBoolean 接口
- `attribute/value/IntValue.kt`: data class with Int，init 校验范围
- `attribute/value/FloatValue.kt`: data class with Float，支持 NaN
- `attribute/value/BoolValue.kt`: data class with Boolean，仅用于标志位
- `attribute/value/PercentValue.kt`: data class with Float，init 校验 0.0-1.0
- `attribute/key/AttributeKey.kt`: `@JvmInline value class AttributeKey<T : AttributeValue>(val name: String)`
- `attribute/key/AttributeMeta.kt`: data class，包含 defaultValue、range、tag、description
- `attribute/key/AttributeTag.kt`: enum class（Combat、Life、Status、Talent）
- `attribute/PredefinedAttributes.kt`: object 包含所有预定义属性键常量和 METAS 映射表

### 验证方式
- `AttributeKey<IntValue>("test")` 可正常创建
- `IntValue(100)` 可正常创建
- `PercentValue(1.5f)` 创建时抛出 IllegalArgumentException
- `AttributeMeta.int(defaultValue = 10, range = 0..100, ...)` 可正常创建

### 已知问题
- 现有 `attribute/condition/ConditionTest.kt` 引用了不存在的 `MAX_HP`（应导入 `PredefinedAttributes.MAX_HP`）

## Wave 2 实现记录

### attribute/set/ 包
创建了以下文件：
- `attribute/set/ComputationContext.kt`: data class，包含 source、target、currentHp、currentMp 属性
- `attribute/set/AttributeSet.kt`: 基于 Map 的不可变集合，实现：
  - `EMPTY`: 空集合常量
  - `get(key)`: 通过键获取值（类型安全）
  - `contains(key)`: 检查键是否存在
  - `with(key, value)`: 返回包含新值的新集合（不可变）
  - `compute(key, modifiers, context)`: 应用修饰器到属性值

### compute() 计算规则
1. 获取基础值
2. 筛选条件满足的修饰器（condition == null 或 evaluate() 返回 true）
3. 先应用所有 FlatModifier（累加固定值）
4. 再应用所有 PercentModifier（乘以百分比）
5. 返回 IntValue

### 修饰器类型
- `FlatModifier`: 添加固定值
- `PercentModifier`: 百分比加成（如 0.5 表示 +50%）
- `TempModifier`: 临时修饰器（仅记录 duration）

### 测试文件
- `attribute/set/AttributeSetTest.kt`: 包含以下测试用例
  - EMPTY 相关测试
  - with() 不可变性测试
  - 链式调用测试
  - compute() 修饰器应用测试

### 编译状态
- 主源码编译通过（desktopMainClasses）
- 测试源码编译失败（ConditionTest.kt 存在导入错误）

## Wave 3 实现记录（修饰器系统 - 本次任务）

### 已完成的文件
- `attribute/modifier/SourceType.kt`: enum（Equipment、Skill、Buff）
- `attribute/modifier/ModifierSource.kt`: interface，sourceType + sourceId
- `attribute/modifier/Modifier.kt`: sealed interface，id/targetKey/condition/source + apply()
- `attribute/modifier/FlatModifier.kt`: data class，value: Int，apply 返回 IntValue
- `attribute/modifier/PercentModifier.kt`: data class，percent: Float(0.0-1.0)，apply 计算百分比
- `attribute/modifier/TempModifier.kt`: data class，duration: Int，内部委托 inner Modifier

### ModifierContext 设计
- 改为 interface（不是 data class）以允许 AttributeSet.ModifierContextImpl 实现
- 属性: targetDiscipleId?, currentRealm?, environmentFactors

### 叠加规则实现
```kotlin
// flat 先于 percent 计算
val afterFlat = flatModifier.apply(baseValue)  // final = base + flatSum
val result = percentModifier.apply(afterFlat)  // result = final * (1 + percentSum)
```

### 测试结果
- ModifierTest.kt: 15 个测试全部通过
- FlatModifier: 正负值、blank id 校验
- PercentModifier: 0.0-1.0 范围校验、倍数计算
- TempModifier: duration > 0 校验、委托内部修饰器
- 叠加规则验证: base(100) + flat(100) = 200，200 * (1 + 0.5) = 300

## Wave 4 实现记录（提供者系统）

### 已完成的文件
- `attribute/provider/ProviderType.kt`: enum（Equipment、Gem、Enchantment、SetBonus、Skill、Buff、Building、Formation、Title、Pet）
- `attribute/provider/AttributeProvider.kt`: interface + SkillSlot data class
- `attribute/provider/ProviderRegistry.kt`: object 单例注册表
- `attribute/provider/UniversalProvider.kt`: 通用实现

### AttributeProvider 接口设计
- id: String - 提供者唯一标识
- name: String - 提供者名称
- providerType: ProviderType - 提供者类型
- getModifiers(): List<Modifier> - 获取所有修饰器
- getSkillSlots(): List<SkillSlot> - 获取技能槽位
- getMetaAttributes(): Map<String, String> - 获取元属性

### SkillSlot 设计
- slotId: String - 槽位ID
- slotType: String - 槽位类型（如 gem、socket）
- isEmpty: Boolean - 是否为空

### ProviderRegistry 设计
- 单例模式（object 声明）
- register(provider: AttributeProvider) - 注册提供者
- get(id: String): AttributeProvider? - 根据ID获取
- getByType(type: ProviderType): List<AttributeProvider> - 按类型查询
- getAll(): List<AttributeProvider> - 获取所有
- resetForTest() - 测试钩子，清空注册表

### 测试文件
- `attribute/provider/ProviderTest.kt`: 12 个测试用例
  - ProviderType 枚举值验证
  - UniversalProvider 属性创建
  - ProviderRegistry 注册/获取/按类型查询
  - resetForTest 清空功能
  - 自定义 AttributeProvider 实现

### 编译状态
- 主源码编译通过（desktopMainClasses）
- 测试源码编译通过（desktopTest）
- 所有测试通过

## Wave 5 实现记录（领域模型：Equipment、Skill、Buff）

### 已完成的文件
- `Equipment.kt`: 装备领域模型
  - Rarity enum: Common、Uncommon、Rare、Epic、Legendary
  - EquipmentSlot enum: Weapon、Helmet、Armor、Boots、Gloves、Ring、Necklace、Cloak、Belt、Offhand
  - data class Equipment: id/name/slot/rarity/modifiers
  - companion object.create(): 返回 Result<Equipment>
- `Skill.kt`: 技能领域模型
  - SkillType enum: Active、Passive、Ultimate、Support
  - data class Skill: id/name/type/modifiers/cooldown
  - isPassive(): 判断是否为被动技能
  - companion object.create(): 返回 Result<Skill>
- `Buff.kt`: Buff领域模型
  - data class Buff: id/name/modifiers/duration/stackable
  - tick(): 减少持续时间，返回新实例（不可变）
  - isExpired(): 判断是否已过期
  - companion object.create(): 返回 Result<Buff>

### 测试文件
- `EquipmentTest.kt`: 16 个测试用例
  - create() 成功/失败场景
  - Rarity/EquipmentSlot 枚举方法
  - data class equals 测试
- `SkillTest.kt`: 15 个测试用例
  - create() 成功/失败场景
  - isPassive() 对所有 SkillType 的判断
  - data class equals 测试
- `BuffTest.kt`: 17 个测试用例
  - create() 成功/失败场景
  - isExpired() 对 permanent(-1)/正值/零值的判断
  - tick() 减少持续时间、永久buff不变、返回新实例
  - stackable 标志保留
  - data class equals 测试

### 设计要点
- duration = -1 表示永久 buff
- tick() 对永久 buff 返回相同实例
- tick() 对有期限 buff 返回 duration-1 的新实例（coerceAtLeast(0)）
- 所有模型使用 init 块进行 require 校验

### 编译状态
- 主源码编译通过（desktopMainClasses）
- 测试源码编译通过（desktopTestClasses）
- 所有测试通过（desktopTest）

## Wave 6 实现记录（Disciple 集成层）

### 已完成的文件
- `DiscipleAttributes.kt`: 属性键定义对象
  - SPIRIT_ROOT、TALENT、LUCK 直接映射到 PredefinedAttributes
  - object 声明，提供命名空间
- `DiscipleExtensions.kt`: 扩展函数
  - `Attributes.toAttributeSet()`: 旧→新转换
  - `AttributeSet.toAttributes()`: 新→旧转换
  - `Disciple.computeAttribute(key: AttributeKey<IntValue>): Int`: 计算属性值

### 关键设计决策
1. **使用扩展函数而非修改原有类**: 遵循"不修改 Disciple 原有代码"的要求
2. **DiscipleAttributes 作为桥接**: 直接引用 PredefinedAttributes 中的键，保持一致性
3. **toAttributeSet() 使用 IntValue 包装**: 确保类型安全
4. **toAttributes() 使用默认值 50**: 与 Attributes.DEFAULT 保持一致
5. **computeAttribute() 直接读取 attributes 字段**: 暂不支持修饰器系统

### 测试验证
- DiscipleTest: 所有 26 个测试用例通过
- 编译状态: desktopMainClasses 编译通过

### 待扩展方向
- computeAttribute() 可在未来支持 modifiers 参数进行修饰器计算
- 可添加 computeAttribute(disciple, key, modifiers, context) 重载
