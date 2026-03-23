package com.sect.game.domain

import com.sect.game.domain.attribute.key.AttributeKey
import com.sect.game.domain.attribute.set.AttributeSet
import com.sect.game.domain.attribute.value.IntValue
import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.valueobject.Attributes

/**
 * 将旧属性系统转换为新属性系统
 */
fun Attributes.toAttributeSet(): AttributeSet {
    return AttributeSet(
        mapOf(
            DiscipleAttributes.SPIRIT_ROOT to IntValue(spiritRoot),
            DiscipleAttributes.TALENT to IntValue(talent),
            DiscipleAttributes.LUCK to IntValue(luck),
        ),
    )
}

/**
 * 将新属性系统转换为旧属性系统
 */
fun AttributeSet.toAttributes(): Attributes {
    val spiritRoot = this[DiscipleAttributes.SPIRIT_ROOT]?.toInt() ?: 50
    val talent = this[DiscipleAttributes.TALENT]?.toInt() ?: 50
    val luck = this[DiscipleAttributes.LUCK]?.toInt() ?: 50
    return Attributes(
        spiritRoot = spiritRoot,
        talent = talent,
        luck = luck,
    )
}

/**
 * 计算弟子指定属性的最终值
 * 使用弟子的 attributes 字段进行计算
 */
fun Disciple.computeAttribute(key: AttributeKey<IntValue>): Int {
    val attributeSet = attributes.toAttributeSet()
    return attributeSet[key]?.toInt() ?: 0
}
