package com.sect.game.domain.attribute.condition

import com.sect.game.domain.attribute.PredefinedAttributes.MAX_HP
import com.sect.game.domain.attribute.set.ComputationContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConditionTest {

    @Test
    fun always_evaluate_alwaysReturnsTrue() {
        val condition = Always
        val context = ComputationContext()
        assertTrue(condition.evaluate(context))
    }

    @Test
    fun always_evaluate_withDifferentContexts_alwaysReturnsTrue() {
        val condition = Always
        val context1 = ComputationContext(currentHp = 0)
        val context2 = ComputationContext(currentHp = 100)
        val context3 = ComputationContext(currentHp = 50, currentMp = 30)
        assertTrue(condition.evaluate(context1))
        assertTrue(condition.evaluate(context2))
        assertTrue(condition.evaluate(context3))
    }

    @Test
    fun attributeThreshold_maxHpGreaterThan_satisfied() {
        val condition = AttributeThreshold(
            key = MAX_HP,
            operator = ComparisonOperator.GREATER_THAN,
            threshold = 30,
        )
        val context = ComputationContext(currentHp = 50)
        assertTrue(condition.evaluate(context))
    }

    @Test
    fun attributeThreshold_maxHpGreaterThan_notSatisfied() {
        val condition = AttributeThreshold(
            key = MAX_HP,
            operator = ComparisonOperator.GREATER_THAN,
            threshold = 30,
        )
        val context = ComputationContext(currentHp = 20)
        assertFalse(condition.evaluate(context))
    }

    @Test
    fun attributeThreshold_maxHpLessThan_satisfied() {
        val condition = AttributeThreshold(
            key = MAX_HP,
            operator = ComparisonOperator.LESS_THAN,
            threshold = 50,
        )
        val context = ComputationContext(currentHp = 30)
        assertTrue(condition.evaluate(context))
    }

    @Test
    fun attributeThreshold_maxHpLessThan_notSatisfied() {
        val condition = AttributeThreshold(
            key = MAX_HP,
            operator = ComparisonOperator.LESS_THAN,
            threshold = 50,
        )
        val context = ComputationContext(currentHp = 60)
        assertFalse(condition.evaluate(context))
    }

    @Test
    fun attributeThreshold_maxHpEqual_satisfied() {
        val condition = AttributeThreshold(
            key = MAX_HP,
            operator = ComparisonOperator.EQUAL,
            threshold = 50,
        )
        val context = ComputationContext(currentHp = 50)
        assertTrue(condition.evaluate(context))
    }

    @Test
    fun attributeThreshold_maxHpEqual_notSatisfied() {
        val condition = AttributeThreshold(
            key = MAX_HP,
            operator = ComparisonOperator.EQUAL,
            threshold = 50,
        )
        val context = ComputationContext(currentHp = 51)
        assertFalse(condition.evaluate(context))
    }

    @Test
    fun attributeThreshold_maxHpGreaterThanOrEqual_satisfied() {
        val condition = AttributeThreshold(
            key = MAX_HP,
            operator = ComparisonOperator.GREATER_THAN_OR_EQUAL,
            threshold = 50,
        )
        val context = ComputationContext(currentHp = 50)
        assertTrue(condition.evaluate(context))
    }

    @Test
    fun attributeThreshold_maxHpGreaterThanOrEqual_notSatisfied() {
        val condition = AttributeThreshold(
            key = MAX_HP,
            operator = ComparisonOperator.GREATER_THAN_OR_EQUAL,
            threshold = 50,
        )
        val context = ComputationContext(currentHp = 49)
        assertFalse(condition.evaluate(context))
    }

    @Test
    fun attributeThreshold_maxHpLessThanOrEqual_satisfied() {
        val condition = AttributeThreshold(
            key = MAX_HP,
            operator = ComparisonOperator.LESS_THAN_OR_EQUAL,
            threshold = 50,
        )
        val context = ComputationContext(currentHp = 50)
        assertTrue(condition.evaluate(context))
    }

    @Test
    fun attributeThreshold_maxHpLessThanOrEqual_notSatisfied() {
        val condition = AttributeThreshold(
            key = MAX_HP,
            operator = ComparisonOperator.LESS_THAN_OR_EQUAL,
            threshold = 50,
        )
        val context = ComputationContext(currentHp = 51)
        assertFalse(condition.evaluate(context))
    }

    @Test
    fun comparisonOperator_hasCorrectValues() {
        assertEquals(5, ComparisonOperator.entries.size)
        assertEquals(ComparisonOperator.GREATER_THAN, ComparisonOperator.valueOf("GREATER_THAN"))
        assertEquals(ComparisonOperator.LESS_THAN, ComparisonOperator.valueOf("LESS_THAN"))
        assertEquals(ComparisonOperator.EQUAL, ComparisonOperator.valueOf("EQUAL"))
        assertEquals(ComparisonOperator.GREATER_THAN_OR_EQUAL, ComparisonOperator.valueOf("GREATER_THAN_OR_EQUAL"))
        assertEquals(ComparisonOperator.LESS_THAN_OR_EQUAL, ComparisonOperator.valueOf("LESS_THAN_OR_EQUAL"))
    }
}
