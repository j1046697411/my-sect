package com.sect.game.domain.attribute.modifier

import com.sect.game.domain.attribute.value.AttributeValue

data class TempModifier(
    override val id: String,
    override val targetKey: String,
    val duration: Int,
    val inner: Modifier,
    override val condition: ModifierCondition? = null,
    override val source: ModifierSource,
) : Modifier {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(targetKey.isNotBlank()) { "targetKey must not be blank" }
        require(duration > 0) { "duration must be positive, but was $duration" }
    }

    override fun apply(baseValue: AttributeValue): AttributeValue {
        return inner.apply(baseValue)
    }
}
