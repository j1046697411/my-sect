package com.sect.game.domain.attribute.value

/**
 * 百分比属性值
 * 取值范围为 0.0f 到 1.0f，表示 0% 到 100%
 *
 * @param value 百分比数值（0.0-1.0）
 * @throws IllegalArgumentException 当 value 不在 [0.0f, 1.0f] 范围内时
 */
data class PercentValue(val value: Float) : AttributeValue {

    init {
        require(value in MIN_VALUE..MAX_VALUE) {
            "PercentValue must be between $MIN_VALUE and $MAX_VALUE, but was $value"
        }
    }

    override fun toInt(): Int = (value * 100).toInt()

    override fun toFloat(): Float = value

    override fun toBoolean(): Boolean = value > 0f

    /**
     * 将百分比转换为字符串形式（如 "50%"）
     */
    fun toDisplayString(): String = "${(value * 100).toInt()}%"

    companion object {
        const val MIN_VALUE = 0.0f
        const val MAX_VALUE = 1.0f

        val ZERO = PercentValue(0.0f)
        val HUNDRED = PercentValue(1.0f)
        val FIFTY = PercentValue(0.5f)
    }
}
