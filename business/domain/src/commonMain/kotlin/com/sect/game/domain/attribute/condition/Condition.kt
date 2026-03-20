package com.sect.game.domain.attribute.condition

import com.sect.game.domain.attribute.key.AttributeKey
import com.sect.game.domain.attribute.set.ComputationContext
import com.sect.game.domain.attribute.value.AttributeValue

sealed interface Condition {
    fun evaluate(context: ComputationContext): Boolean
}

data object Always : Condition {
    override fun evaluate(context: ComputationContext): Boolean = true
}

data class AttributeThreshold<T : AttributeValue>(
    val key: AttributeKey<T>,
    val operator: ComparisonOperator,
    val threshold: Int,
) : Condition {
    override fun evaluate(context: ComputationContext): Boolean {
        val currentValue = resolveValue(context)
        return compare(currentValue, threshold, operator)
    }

    private fun resolveValue(context: ComputationContext): Int {
        return when (key.name) {
            "max_hp" -> context.currentHp
            else -> 0
        }
    }

    private fun compare(value: Int, threshold: Int, op: ComparisonOperator): Boolean {
        return when (op) {
            ComparisonOperator.GREATER_THAN -> value > threshold
            ComparisonOperator.LESS_THAN -> value < threshold
            ComparisonOperator.EQUAL -> value == threshold
            ComparisonOperator.GREATER_THAN_OR_EQUAL -> value >= threshold
            ComparisonOperator.LESS_THAN_OR_EQUAL -> value <= threshold
        }
    }
}
