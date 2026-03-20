package com.sect.game.domain.attribute

import com.sect.game.domain.attribute.modifier.FlatModifier
import com.sect.game.domain.attribute.modifier.ModifierSource
import com.sect.game.domain.attribute.modifier.PercentModifier
import com.sect.game.domain.attribute.modifier.SourceType
import com.sect.game.domain.attribute.set.AttributeSet
import com.sect.game.domain.attribute.set.ComputationContext
import com.sect.game.domain.attribute.value.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AttributeSetTest {

    private val testSource = object : ModifierSource {
        override val sourceType = SourceType.Equipment
        override val sourceId = "equip-test"
    }

    @Test
    fun empty_setIsEmpty() {
        val set = AttributeSet.EMPTY
        assertTrue(set.isEmpty())
    }

    @Test
    fun empty_containsReturnsFalse() {
        val set = AttributeSet.EMPTY
        assertFalse(set.contains(PredefinedAttributes.ATTACK))
    }

    @Test
    fun empty_getReturnsNull() {
        val set = AttributeSet.EMPTY
        assertNull(set.get(PredefinedAttributes.ATTACK))
    }

    @Test
    fun with_singleAttribute_containsAttribute() {
        val set = AttributeSet.EMPTY.with(PredefinedAttributes.ATTACK, IntValue(100))
        assertTrue(set.contains(PredefinedAttributes.ATTACK))
    }

    @Test
    fun with_singleAttribute_getReturnsValue() {
        val set = AttributeSet.EMPTY.with(PredefinedAttributes.ATTACK, IntValue(100))
        assertEquals(100, set.get(PredefinedAttributes.ATTACK)?.toInt())
    }

    @Test
    fun with_multipleAttributes_containsAll() {
        var set = AttributeSet.EMPTY
        set = set.with(PredefinedAttributes.ATTACK, IntValue(100))
        set = set.with(PredefinedAttributes.DEFENSE, IntValue(50))
        set = set.with(PredefinedAttributes.SPEED, IntValue(75))

        assertTrue(set.contains(PredefinedAttributes.ATTACK))
        assertTrue(set.contains(PredefinedAttributes.DEFENSE))
        assertTrue(set.contains(PredefinedAttributes.SPEED))
        assertEquals(100, set.get(PredefinedAttributes.ATTACK)?.toInt())
        assertEquals(50, set.get(PredefinedAttributes.DEFENSE)?.toInt())
        assertEquals(75, set.get(PredefinedAttributes.SPEED)?.toInt())
    }

    @Test
    fun with_overwritesExistingValue() {
        var set = AttributeSet.EMPTY
        set = set.with(PredefinedAttributes.ATTACK, IntValue(100))
        set = set.with(PredefinedAttributes.ATTACK, IntValue(200))

        assertEquals(200, set.get(PredefinedAttributes.ATTACK)?.toInt())
    }

    @Test
    fun with_returnsNewInstance() {
        val original = AttributeSet.EMPTY
        val modified = original.with(PredefinedAttributes.ATTACK, IntValue(100))

        assertNotSame(original, modified)
        assertTrue(original.isEmpty())
        assertFalse(modified.isEmpty())
    }

    @Test
    fun get_withMissingKey_returnsNull() {
        val set = AttributeSet.EMPTY.with(PredefinedAttributes.ATTACK, IntValue(100))
        assertNull(set.get(PredefinedAttributes.DEFENSE))
    }

    @Test
    fun compute_withNoModifiers_returnsBaseValue() {
        val set = AttributeSet.EMPTY.with(PredefinedAttributes.ATTACK, IntValue(100))
        val result = set.compute(PredefinedAttributes.ATTACK, emptyList(), ComputationContext())

        assertEquals(100, result?.toInt())
    }

    @Test
    fun compute_withFlatModifier_addsValue() {
        val set = AttributeSet.EMPTY.with(PredefinedAttributes.ATTACK, IntValue(100))
        val modifiers = listOf(
            FlatModifier(
                id = "flat-1",
                targetKey = "attack",
                value = 50,
                source = testSource,
            ),
        )
        val result = set.compute(PredefinedAttributes.ATTACK, modifiers, ComputationContext())

        assertEquals(150, result?.toInt())
    }

    @Test
    fun compute_withPercentModifier_multipliesValue() {
        val set = AttributeSet.EMPTY.with(PredefinedAttributes.ATTACK, IntValue(100))
        val modifiers = listOf(
            PercentModifier(
                id = "percent-1",
                targetKey = "attack",
                percent = 0.5f,
                source = testSource,
            ),
        )
        val result = set.compute(PredefinedAttributes.ATTACK, modifiers, ComputationContext())

        assertEquals(150, result?.toInt())
    }

    @Test
    fun compute_flatModifiersApplyFirst() {
        val set = AttributeSet.EMPTY.with(PredefinedAttributes.ATTACK, IntValue(100))
        val modifiers = listOf(
            FlatModifier(
                id = "flat-1",
                targetKey = "attack",
                value = 50,
                source = testSource,
            ),
            PercentModifier(
                id = "percent-1",
                targetKey = "attack",
                percent = 0.5f,
                source = testSource,
            ),
        )
        val result = set.compute(PredefinedAttributes.ATTACK, modifiers, ComputationContext())

        assertEquals(225, result?.toInt())
    }

    @Test
    fun compute_multipleFlatModifiers_sumTogether() {
        val set = AttributeSet.EMPTY.with(PredefinedAttributes.ATTACK, IntValue(100))
        val modifiers = listOf(
            FlatModifier(id = "flat-1", targetKey = "attack", value = 50, source = testSource),
            FlatModifier(id = "flat-2", targetKey = "attack", value = 30, source = testSource),
        )
        val result = set.compute(PredefinedAttributes.ATTACK, modifiers, ComputationContext())

        assertEquals(180, result?.toInt())
    }

    @Test
    fun compute_multiplePercentModifiers_multiplyTogether() {
        val set = AttributeSet.EMPTY.with(PredefinedAttributes.ATTACK, IntValue(100))
        val modifiers = listOf(
            PercentModifier(id = "percent-1", targetKey = "attack", percent = 0.2f, source = testSource),
            PercentModifier(id = "percent-2", targetKey = "attack", percent = 0.3f, source = testSource),
        )
        val result = set.compute(PredefinedAttributes.ATTACK, modifiers, ComputationContext())

        assertEquals(156, result?.toInt())
    }

    @Test
    fun compute_withCondition_satisfiedCondition_appliesModifier() {
        val set = AttributeSet.EMPTY.with(PredefinedAttributes.MAX_HP, IntValue(100))
        val modifiers = listOf(
            FlatModifier(
                id = "flat-1",
                targetKey = "max_hp",
                value = 50,
                condition = null,
                source = testSource,
            ),
        )
        val result = set.compute(PredefinedAttributes.MAX_HP, modifiers, ComputationContext(currentHp = 50))

        assertEquals(150, result?.toInt())
    }

    @Test
    fun compute_withNonMatchingTargetKey_ignoresModifier() {
        val set = AttributeSet.EMPTY.with(PredefinedAttributes.ATTACK, IntValue(100))
        val modifiers = listOf(
            FlatModifier(
                id = "flat-1",
                targetKey = "defense",
                value = 50,
                source = testSource,
            ),
        )
        val result = set.compute(PredefinedAttributes.ATTACK, modifiers, ComputationContext())

        assertEquals(100, result?.toInt())
    }

    @Test
    fun compute_withMissingBaseValue_returnsNull() {
        val set = AttributeSet.EMPTY
        val modifiers = listOf(
            FlatModifier(
                id = "flat-1",
                targetKey = "attack",
                value = 50,
                source = testSource,
            ),
        )
        val result = set.compute(PredefinedAttributes.ATTACK, modifiers, ComputationContext())

        assertNull(result)
    }

    @Test
    fun isEmpty_withEmptySet_returnsTrue() {
        assertTrue(AttributeSet.EMPTY.isEmpty())
    }

    @Test
    fun isEmpty_withNonEmptySet_returnsFalse() {
        val set = AttributeSet.EMPTY.with(PredefinedAttributes.ATTACK, IntValue(100))
        assertFalse(set.isEmpty())
    }

    @Test
    fun get_returnsCorrectType() {
        val set = AttributeSet.EMPTY.with(PredefinedAttributes.ATTACK, IntValue(100))
        val result = set.get(PredefinedAttributes.ATTACK)

        assertTrue(result is IntValue)
        assertEquals(100, result?.toInt())
    }
}
