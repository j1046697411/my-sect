package com.sect.game.domain.attribute.set

import com.sect.game.domain.attribute.key.AttributeKey
import com.sect.game.domain.attribute.modifier.FlatModifier
import com.sect.game.domain.attribute.modifier.Modifier
import com.sect.game.domain.attribute.modifier.PercentModifier
import com.sect.game.domain.attribute.value.AttributeValue
import com.sect.game.domain.attribute.value.IntValue

data class AttributeSet(private val values: Map<AttributeKey<*>, AttributeValue>) {

    fun isEmpty(): Boolean = values.isEmpty()

    @Suppress("UNCHECKED_CAST")
    operator fun <T : AttributeValue> get(key: AttributeKey<T>): T? {
        return values[key] as? T
    }

    fun contains(key: AttributeKey<*>): Boolean {
        return values.containsKey(key)
    }

    fun with(key: AttributeKey<*>, value: AttributeValue): AttributeSet {
        return AttributeSet(values + (key to value))
    }

    fun compute(
        key: AttributeKey<*>,
        modifiers: List<Modifier>,
        context: ComputationContext,
    ): AttributeValue? {
        val baseValue = values[key] ?: return null

        val applicableModifiers = modifiers.filter { modifier ->
            modifier.targetKey == key.name &&
                run {
                    val cond = modifier.condition
                    cond == null || cond.evaluate(ModifierContextImpl(context))
                }
        }

        val flatModifiers = applicableModifiers.filterIsInstance<FlatModifier>()
        val percentModifiers = applicableModifiers.filterIsInstance<PercentModifier>()

        var result = baseValue.toInt()

        for (flat in flatModifiers) {
            result = flat.value + result
        }

        for (percent in percentModifiers) {
            result = (result * (1 + percent.percent)).toInt()
        }

        return IntValue(result)
    }

    private class ModifierContextImpl(ctx: ComputationContext) : com.sect.game.domain.attribute.modifier.ModifierContext {
        override val targetDiscipleId: String? = ctx.target
        override val currentRealm: String? = null
        override val environmentFactors: Map<String, Boolean> = emptyMap()
    }

    companion object {
        val EMPTY = AttributeSet(emptyMap())
    }
}
