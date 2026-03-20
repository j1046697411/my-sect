package com.sect.game.domain.attribute.modifier

import com.sect.game.domain.attribute.value.AttributeValue
import com.sect.game.domain.attribute.value.IntValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ModifierTest {

    private val testSource = object : ModifierSource {
        override val sourceType = SourceType.Equipment
        override val sourceId = "equip-1"
    }

    @Test
    fun flatModifier_apply_addsValue() {
        val modifier = FlatModifier(
            id = "flat-1",
            targetKey = "attack",
            value = 50,
            source = testSource,
        )
        val baseValue: AttributeValue = IntValue(100)

        val result = modifier.apply(baseValue)

        assertEquals(150, result.toInt())
    }

    @Test
    fun flatModifier_apply_withNegativeValue() {
        val modifier = FlatModifier(
            id = "flat-2",
            targetKey = "defense",
            value = -20,
            source = testSource,
        )
        val baseValue: AttributeValue = IntValue(100)

        val result = modifier.apply(baseValue)

        assertEquals(80, result.toInt())
    }

    @Test
    fun flatModifier_withBlankId_throws() {
        assertFailsWith<IllegalArgumentException> {
            FlatModifier(
                id = "",
                targetKey = "attack",
                value = 10,
                source = testSource,
            )
        }
    }

    @Test
    fun percentModifier_apply_multipliesValue() {
        val modifier = PercentModifier(
            id = "percent-1",
            targetKey = "attack",
            percent = 0.2f,
            source = testSource,
        )
        val baseValue: AttributeValue = IntValue(100)

        val result = modifier.apply(baseValue)

        assertEquals(120, result.toInt())
    }

    @Test
    fun percentModifier_apply_withZeroPercent_returnsSame() {
        val modifier = PercentModifier(
            id = "percent-2",
            targetKey = "attack",
            percent = 0.0f,
            source = testSource,
        )
        val baseValue: AttributeValue = IntValue(100)

        val result = modifier.apply(baseValue)

        assertEquals(100, result.toInt())
    }

    @Test
    fun percentModifier_withPercentOutOfRange_throws() {
        assertFailsWith<IllegalArgumentException> {
            PercentModifier(
                id = "percent-3",
                targetKey = "attack",
                percent = 1.5f,
                source = testSource,
            )
        }
    }

    @Test
    fun percentModifier_withNegativePercent_throws() {
        assertFailsWith<IllegalArgumentException> {
            PercentModifier(
                id = "percent-4",
                targetKey = "attack",
                percent = -0.1f,
                source = testSource,
            )
        }
    }

    @Test
    fun tempModifier_containsDuration() {
        val inner = FlatModifier(
            id = "flat-1",
            targetKey = "attack",
            value = 10,
            source = testSource,
        )
        val modifier = TempModifier(
            id = "temp-1",
            targetKey = "attack",
            duration = 5,
            inner = inner,
            source = testSource,
        )

        assertEquals(5, modifier.duration)
    }

    @Test
    fun tempModifier_apply_delegatesToInner() {
        val inner = FlatModifier(
            id = "flat-1",
            targetKey = "attack",
            value = 25,
            source = testSource,
        )
        val modifier = TempModifier(
            id = "temp-1",
            targetKey = "attack",
            duration = 3,
            inner = inner,
            source = testSource,
        )
        val baseValue: AttributeValue = IntValue(100)

        val result = modifier.apply(baseValue)

        assertEquals(125, result.toInt())
    }

    @Test
    fun tempModifier_withZeroDuration_throws() {
        val inner = FlatModifier(
            id = "flat-1",
            targetKey = "attack",
            value = 10,
            source = testSource,
        )
        assertFailsWith<IllegalArgumentException> {
            TempModifier(
                id = "temp-2",
                targetKey = "attack",
                duration = 0,
                inner = inner,
                source = testSource,
            )
        }
    }

    @Test
    fun stackingRule_flatFirstThenPercent() {
        val flatModifier = FlatModifier(
            id = "flat-1",
            targetKey = "attack",
            value = 100,
            source = testSource,
        )
        val percentModifier = PercentModifier(
            id = "percent-1",
            targetKey = "attack",
            percent = 0.5f,
            source = testSource,
        )
        val baseValue: AttributeValue = IntValue(100)

        val afterFlat = flatModifier.apply(baseValue)
        val finalResult = percentModifier.apply(afterFlat)

        assertEquals(300, finalResult.toInt())
    }

    @Test
    fun multipleFlatModifiers_sumTogether() {
        val flat1 = FlatModifier(
            id = "flat-1",
            targetKey = "attack",
            value = 50,
            source = testSource,
        )
        val flat2 = FlatModifier(
            id = "flat-2",
            targetKey = "attack",
            value = 30,
            source = testSource,
        )
        val baseValue: AttributeValue = IntValue(100)

        val afterFlat1 = flat1.apply(baseValue)
        val afterFlat2 = flat2.apply(afterFlat1)

        assertEquals(180, afterFlat2.toInt())
    }

    @Test
    fun multiplePercentModifiers_multiplyTogether() {
        val percent1 = PercentModifier(
            id = "percent-1",
            targetKey = "attack",
            percent = 0.2f,
            source = testSource,
        )
        val percent2 = PercentModifier(
            id = "percent-2",
            targetKey = "attack",
            percent = 0.3f,
            source = testSource,
        )
        val baseValue: AttributeValue = IntValue(100)

        val afterPercent1 = percent1.apply(baseValue)
        val afterPercent2 = percent2.apply(afterPercent1)

        assertEquals(156, afterPercent2.toInt())
    }

    @Test
    fun sourceType_enum_hasCorrectValues() {
        assertEquals(SourceType.Equipment, SourceType.valueOf("Equipment"))
        assertEquals(SourceType.Skill, SourceType.valueOf("Skill"))
        assertEquals(SourceType.Buff, SourceType.valueOf("Buff"))
    }

    @Test
    fun modifierSource_implementation() {
        val source = object : ModifierSource {
            override val sourceType = SourceType.Skill
            override val sourceId = "skill-flame"
        }

        assertEquals(SourceType.Skill, source.sourceType)
        assertEquals("skill-flame", source.sourceId)
    }
}
