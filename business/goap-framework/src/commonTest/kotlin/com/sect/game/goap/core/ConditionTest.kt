package com.sect.game.goap.core

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConditionTest {
    @Test
    fun greaterThan_satisfiedWhenValueAboveThreshold() {
        val condition = Condition.greaterThan("health", 50)
        val state = WorldState().withInt("health", 80)
        assertTrue(condition.isSatisfiedBy(state))
    }

    @Test
    fun greaterThan_notSatisfiedWhenValueEqualsThreshold() {
        val condition = Condition.greaterThan("health", 50)
        val state = WorldState().withInt("health", 50)
        assertFalse(condition.isSatisfiedBy(state))
    }

    @Test
    fun greaterThan_notSatisfiedWhenValueBelowThreshold() {
        val condition = Condition.greaterThan("health", 50)
        val state = WorldState().withInt("health", 30)
        assertFalse(condition.isSatisfiedBy(state))
    }

    @Test
    fun lessThan_satisfiedWhenValueBelowThreshold() {
        val condition = Condition.lessThan("fatigue", 30)
        val state = WorldState().withInt("fatigue", 15)
        assertTrue(condition.isSatisfiedBy(state))
    }

    @Test
    fun lessThan_notSatisfiedWhenValueEqualsThreshold() {
        val condition = Condition.lessThan("fatigue", 30)
        val state = WorldState().withInt("fatigue", 30)
        assertFalse(condition.isSatisfiedBy(state))
    }

    @Test
    fun lessThan_notSatisfiedWhenValueAboveThreshold() {
        val condition = Condition.lessThan("fatigue", 30)
        val state = WorldState().withInt("fatigue", 50)
        assertFalse(condition.isSatisfiedBy(state))
    }

    @Test
    fun equals_satisfiedWhenValueMatches() {
        val condition = Condition.equals("realm", 2)
        val state = WorldState().withInt("realm", 2)
        assertTrue(condition.isSatisfiedBy(state))
    }

    @Test
    fun equals_notSatisfiedWhenValueDiffers() {
        val condition = Condition.equals("realm", 2)
        val state = WorldState().withInt("realm", 3)
        assertFalse(condition.isSatisfiedBy(state))
    }

    @Test
    fun greaterThanOrEqual_satisfiedWhenValueAbove() {
        val condition = Condition.greaterThanOrEqual("progress", 100)
        val state = WorldState().withInt("progress", 120)
        assertTrue(condition.isSatisfiedBy(state))
    }

    @Test
    fun greaterThanOrEqual_satisfiedWhenValueEquals() {
        val condition = Condition.greaterThanOrEqual("progress", 100)
        val state = WorldState().withInt("progress", 100)
        assertTrue(condition.isSatisfiedBy(state))
    }

    @Test
    fun greaterThanOrEqual_notSatisfiedWhenValueBelow() {
        val condition = Condition.greaterThanOrEqual("progress", 100)
        val state = WorldState().withInt("progress", 80)
        assertFalse(condition.isSatisfiedBy(state))
    }

    @Test
    fun lessThanOrEqual_satisfiedWhenValueBelow() {
        val condition = Condition.lessThanOrEqual("fatigue", 20)
        val state = WorldState().withInt("fatigue", 15)
        assertTrue(condition.isSatisfiedBy(state))
    }

    @Test
    fun lessThanOrEqual_satisfiedWhenValueEquals() {
        val condition = Condition.lessThanOrEqual("fatigue", 20)
        val state = WorldState().withInt("fatigue", 20)
        assertTrue(condition.isSatisfiedBy(state))
    }

    @Test
    fun lessThanOrEqual_notSatisfiedWhenValueAbove() {
        val condition = Condition.lessThanOrEqual("fatigue", 20)
        val state = WorldState().withInt("fatigue", 30)
        assertFalse(condition.isSatisfiedBy(state))
    }

    @Test
    fun not_invertsCondition() {
        val inner = Condition.greaterThan("health", 50)
        val condition = Condition.Not(inner)
        val state = WorldState().withInt("health", 30)
        assertTrue(condition.isSatisfiedBy(state))
    }

    @Test
    fun not_whenInnerSatisfied_becomesUnsatisfied() {
        val inner = Condition.greaterThan("health", 50)
        val condition = Condition.Not(inner)
        val state = WorldState().withInt("health", 80)
        assertFalse(condition.isSatisfiedBy(state))
    }

    @Test
    fun and_bothSatisfied_returnsTrue() {
        val left = Condition.greaterThan("health", 50)
        val right = Condition.lessThan("fatigue", 80)
        val condition = Condition.and(left, right)
        val state =
            WorldState()
                .withInt("health", 80)
                .withInt("fatigue", 30)
        assertTrue(condition.isSatisfiedBy(state))
    }

    @Test
    fun and_leftUnsatisfied_returnsFalse() {
        val left = Condition.greaterThan("health", 50)
        val right = Condition.lessThan("fatigue", 80)
        val condition = Condition.and(left, right)
        val state =
            WorldState()
                .withInt("health", 30)
                .withInt("fatigue", 30)
        assertFalse(condition.isSatisfiedBy(state))
    }

    @Test
    fun and_rightUnsatisfied_returnsFalse() {
        val left = Condition.greaterThan("health", 50)
        val right = Condition.lessThan("fatigue", 80)
        val condition = Condition.and(left, right)
        val state =
            WorldState()
                .withInt("health", 80)
                .withInt("fatigue", 90)
        assertFalse(condition.isSatisfiedBy(state))
    }

    @Test
    fun or_bothSatisfied_returnsTrue() {
        val left = Condition.greaterThan("health", 50)
        val right = Condition.lessThan("fatigue", 80)
        val condition = Condition.or(left, right)
        val state =
            WorldState()
                .withInt("health", 60)
                .withInt("fatigue", 90)
        assertTrue(condition.isSatisfiedBy(state))
    }

    @Test
    fun or_leftSatisfied_returnsTrue() {
        val left = Condition.greaterThan("health", 50)
        val right = Condition.lessThan("fatigue", 80)
        val condition = Condition.or(left, right)
        val state =
            WorldState()
                .withInt("health", 80)
                .withInt("fatigue", 90)
        assertTrue(condition.isSatisfiedBy(state))
    }

    @Test
    fun or_rightSatisfied_returnsTrue() {
        val left = Condition.greaterThan("health", 50)
        val right = Condition.lessThan("fatigue", 80)
        val condition = Condition.or(left, right)
        val state =
            WorldState()
                .withInt("health", 30)
                .withInt("fatigue", 30)
        assertTrue(condition.isSatisfiedBy(state))
    }

    @Test
    fun or_bothUnsatisfied_returnsFalse() {
        val left = Condition.greaterThan("health", 50)
        val right = Condition.lessThan("fatigue", 80)
        val condition = Condition.or(left, right)
        val state =
            WorldState()
                .withInt("health", 30)
                .withInt("fatigue", 90)
        assertFalse(condition.isSatisfiedBy(state))
    }

    @Test
    fun always_alwaysReturnsTrue() {
        val condition = Condition.Always
        val state = WorldState()
        assertTrue(condition.isSatisfiedBy(state))
    }

    @Test
    fun never_alwaysReturnsFalse() {
        val condition = Condition.Never
        val state = WorldState()
        assertFalse(condition.isSatisfiedBy(state))
    }

    @Test
    fun nestedConditions_complexLogic_worksCorrectly() {
        val healthOk = Condition.greaterThan("health", 30)
        val fatigueLow = Condition.lessThan("fatigue", 80)
        val notResting = Condition.Not(Condition.equals("isResting", 1))

        val combined =
            Condition.and(
                healthOk,
                Condition.and(fatigueLow, notResting),
            )

        val validState =
            WorldState()
                .withInt("health", 50)
                .withInt("fatigue", 30)
                .withInt("isResting", 0)

        val invalidState =
            WorldState()
                .withInt("health", 20)
                .withInt("fatigue", 30)
                .withInt("isResting", 0)

        assertTrue(combined.isSatisfiedBy(validState))
        assertFalse(combined.isSatisfiedBy(invalidState))
    }

    @Test
    fun factoryMethods_createCorrectConditions() {
        val gt = Condition.greaterThan("key", 10)
        val lt = Condition.lessThan("key", 10)
        val gte = Condition.greaterThanOrEqual("key", 10)
        val lte = Condition.lessThanOrEqual("key", 10)
        val eq = Condition.equals("key", 10)

        val state = WorldState().withInt("key", 10)

        assertFalse(gt.isSatisfiedBy(state)) // 10 > 10 = false
        assertFalse(lt.isSatisfiedBy(state)) // 10 < 10 = false
        assertTrue(gte.isSatisfiedBy(state)) // 10 >= 10 = true
        assertTrue(lte.isSatisfiedBy(state)) // 10 <= 10 = true
        assertTrue(eq.isSatisfiedBy(state)) // 10 == 10 = true
    }
}
