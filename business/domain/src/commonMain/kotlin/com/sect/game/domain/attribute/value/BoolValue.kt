package com.sect.game.domain.attribute.value

/**
 * 布尔属性值
 * 仅用于标志位，不参与数值运算
 *
 * @param value 布尔值
 */
data class BoolValue(val value: Boolean) : AttributeValue {

    override fun toInt(): Int = if (value) 1 else 0

    override fun toFloat(): Float = if (value) 1f else 0f

    override fun toBoolean(): Boolean = value
}
