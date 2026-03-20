package com.sect.game.domain.attribute.value

/**
 * 整型属性值
 *
 * @param value 整数值
 */
data class IntValue(val value: Int) : AttributeValue {

    init {
        require(value in MIN_VALUE..MAX_VALUE) {
            "IntValue must be between $MIN_VALUE and $MAX_VALUE, but was $value"
        }
    }

    override fun toInt(): Int = value

    override fun toFloat(): Float = value.toFloat()

    override fun toBoolean(): Boolean = value != 0

    companion object {
        const val MIN_VALUE = Int.MIN_VALUE
        const val MAX_VALUE = Int.MAX_VALUE
    }
}
