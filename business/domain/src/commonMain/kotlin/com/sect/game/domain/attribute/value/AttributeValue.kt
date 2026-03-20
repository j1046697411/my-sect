package com.sect.game.domain.attribute.value

/**
 * 属性值 sealed 接口
 * 所有具体属性值类型需实现此接口
 */
sealed interface AttributeValue {
    /**
     * 将属性值转换为整型
     */
    fun toInt(): Int

    /**
     * 将属性值转换为浮点型
     */
    fun toFloat(): Float

    /**
     * 将属性值转换为布尔型
     */
    fun toBoolean(): Boolean
}
