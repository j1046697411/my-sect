package com.sect.game.domain.attribute.modifier

import com.sect.game.domain.attribute.value.AttributeValue
import com.sect.game.domain.attribute.value.IntValue

data class PercentModifier(
    override val id: String,
    override val targetKey: String,
    val percent: Float,
    override val condition: ModifierCondition? = null,
    override val source: ModifierSource,
) : Modifier {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(targetKey.isNotBlank()) { "targetKey must not be blank" }
        require(percent in 0.0f..1.0f) { "percent must be in 0.0..1.0, but was $percent" }
    }

    override fun apply(baseValue: AttributeValue): AttributeValue {
        val baseInt = baseValue.toInt()
        val result = (baseInt * (1 + percent)).toInt()
        return IntValue(result)
    }
}
