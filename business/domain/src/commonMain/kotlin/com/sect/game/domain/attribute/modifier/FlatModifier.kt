package com.sect.game.domain.attribute.modifier

import com.sect.game.domain.attribute.value.AttributeValue
import com.sect.game.domain.attribute.value.IntValue

data class FlatModifier(
    override val id: String,
    override val targetKey: String,
    val value: Int,
    override val condition: ModifierCondition? = null,
    override val source: ModifierSource,
) : Modifier {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(targetKey.isNotBlank()) { "targetKey must not be blank" }
    }

    override fun apply(baseValue: AttributeValue): AttributeValue {
        val baseInt = baseValue.toInt()
        return IntValue(baseInt + value)
    }
}
