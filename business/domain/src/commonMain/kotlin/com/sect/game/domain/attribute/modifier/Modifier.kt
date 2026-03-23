package com.sect.game.domain.attribute.modifier

import com.sect.game.domain.attribute.value.AttributeValue

/**
 * 修饰器 sealed 接口
 * 所有修饰器类型需实现此接口
 */
sealed interface Modifier {
    /** 修饰器唯一标识 */
    val id: String

    /** 目标属性键 */
    val targetKey: String

    /** 生效条件（可选） */
    val condition: ModifierCondition?

    /** 来源追踪 */
    val source: ModifierSource

    /**
     * 应用修饰器到基础值
     * @param baseValue 基础属性值
     * @return 修饰后的属性值
     */
    fun apply(baseValue: AttributeValue): AttributeValue
}

/**
 * 修饰器生效条件
 * TODO: 实现更复杂的条件表达式（And/Or/Not）
 */
sealed interface ModifierCondition {
    /** 条件是否满足 */
    fun evaluate(context: ModifierContext): Boolean
}

interface ModifierContext {
    val targetDiscipleId: String?
    val currentRealm: String?
    val environmentFactors: Map<String, Boolean>
}
