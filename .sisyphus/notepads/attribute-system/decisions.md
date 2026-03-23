# 架构决策记录

## AttributeValue 类型系统

### 决策 1: 使用 sealed interface 而非 sealed class
**原因**: 让 `IntValue`、`FloatValue` 等可以携带额外数据（如范围校验）
**日期**: 2026-03-20

### 决策 2: PercentValue 范围约束
**选择**: 0.0f <= value <= 1.0f
**原因**: 符合游戏开发中百分比的标准表示
**替代方案**: 0-100 范围 → 被否决，不够直观

## AttributeSet 设计

### 决策 3: 不可变性
**选择**: 所有修改操作返回新副本
**原因**: 保持线程安全，避免意外修改
**参考**: Kotlin immutable collection patterns

## Modifier 叠加规则

### 决策 4: 计算顺序
**选择**: 固定值先于百分比
**公式**: `final = base + flatSum; result = final * (1 + percentSum)`
**原因**: 游戏行业标准规则

## ProviderRegistry 设计

### 决策 5: 单例模式
**选择**: 使用单例 + 测试钩子
**原因**: 全局注册表适合单例模式
**测试替代**: 提供 `resetForTest()` 方法
