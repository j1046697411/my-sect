package com.sect.game.domain.attribute.value

/**
 * 浮点属性值
 *
 * @param value 浮点数值
 */
data class FloatValue(val value: Float) : AttributeValue {

    init {
        require(value.isNaN() || value in MIN_VALUE..MAX_VALUE) {
            "FloatValue must be between $MIN_VALUE and $MAX_VALUE, but was $value"
        }
    }

    override fun toInt(): Int = value.toInt()

    override fun toFloat(): Float = value

    override fun toBoolean(): Boolean = value != 0f

    companion object {
        const val MIN_VALUE = -Float.MAX_VALUE
        const val MAX_VALUE = Float.MAX_VALUE
    }
}
