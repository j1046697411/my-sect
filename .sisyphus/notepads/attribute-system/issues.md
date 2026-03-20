# 问题与注意事项

## 需要注意的 Gotchas

### AttributeKey 类型擦除
- Kotlin 泛型在运行时会被擦除
- `AttributeKey<IntValue>` 和 `AttributeKey<FloatValue>` 在运行时可能相同
- 需要通过 equals() 实现区分

### PercentValue 校验
- 构造函数必须校验 0.0f <= value <= 1.0f
- 超出范围抛出 IllegalArgumentException

### ProviderRegistry 线程安全
- 多线程环境下需要考虑并发访问
- 延迟初始化可能需要同步

## 潜在问题

### Disciple 集成兼容性
- 现有 Disciple 有 `attributes: Attributes`
- 新系统有 `effectiveAttributes: AttributeSet`
- 需要确保两者同步

### 循环依赖风险
- Modifier 可能引用 AttributeKey
- AttributeSet.compute() 需要 Modifier
- 设计时注意层次
