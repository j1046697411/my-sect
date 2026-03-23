package com.sect.game.domain.attribute

import com.sect.game.domain.attribute.value.AttributeValue
import com.sect.game.domain.attribute.value.BoolValue
import com.sect.game.domain.attribute.value.FloatValue
import com.sect.game.domain.attribute.value.IntValue
import com.sect.game.domain.attribute.value.PercentValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AttributeValueTest {

    // ===== IntValue Tests =====

    @Test
    fun intValue_withValidValue_createsInstance() {
        val intValue = IntValue(100)
        assertEquals(100, intValue.value)
    }

    @Test
    fun intValue_withBoundaryMinValue_succeeds() {
        val intValue = IntValue(Int.MIN_VALUE)
        assertEquals(Int.MIN_VALUE, intValue.value)
    }

    @Test
    fun intValue_withBoundaryMaxValue_succeeds() {
        val intValue = IntValue(Int.MAX_VALUE)
        assertEquals(Int.MAX_VALUE, intValue.value)
    }

    @Test
    fun intValue_toInt_returnsCorrectValue() {
        val intValue = IntValue(42)
        assertEquals(42, intValue.toInt())
    }

    @Test
    fun intValue_toFloat_returnsCorrectValue() {
        val intValue = IntValue(42)
        assertEquals(42.0f, intValue.toFloat())
    }

    @Test
    fun intValue_toBoolean_withNonZero_returnsTrue() {
        val intValue = IntValue(100)
        assertTrue(intValue.toBoolean())
    }

    @Test
    fun intValue_toBoolean_withZero_returnsFalse() {
        val intValue = IntValue(0)
        assertFalse(intValue.toBoolean())
    }

    @Test
    fun intValue_toBoolean_withNegative_returnsTrue() {
        val intValue = IntValue(-1)
        assertTrue(intValue.toBoolean())
    }

    @Test
    fun intValue_equals_withSameValue_returnsTrue() {
        val intValue1 = IntValue(100)
        val intValue2 = IntValue(100)
        assertEquals(intValue1, intValue2)
    }

    @Test
    fun intValue_equals_withDifferentValue_returnsFalse() {
        val intValue1 = IntValue(100)
        val intValue2 = IntValue(200)
        assertNotEquals(intValue1, intValue2)
    }

    @Test
    fun intValue_hashCode_withSameValue_areEqual() {
        val intValue1 = IntValue(100)
        val intValue2 = IntValue(100)
        assertEquals(intValue1.hashCode(), intValue2.hashCode())
    }

    @Test
    fun intValue_toString_returnsFormattedString() {
        val intValue = IntValue(42)
        assertEquals("IntValue(value=42)", intValue.toString())
    }

    // ===== FloatValue Tests =====

    @Test
    fun floatValue_withValidValue_createsInstance() {
        val floatValue = FloatValue(3.14f)
        assertEquals(3.14f, floatValue.value)
    }

    @Test
    fun floatValue_withBoundaryMinValue_succeeds() {
        val floatValue = FloatValue(-Float.MAX_VALUE)
        assertEquals(-Float.MAX_VALUE, floatValue.value)
    }

    @Test
    fun floatValue_withBoundaryMaxValue_succeeds() {
        val floatValue = FloatValue(Float.MAX_VALUE)
        assertEquals(Float.MAX_VALUE, floatValue.value)
    }

    @Test
    fun floatValue_withNaN_succeeds() {
        val floatValue = FloatValue(Float.NaN)
        assertTrue(floatValue.value.isNaN())
    }

    @Test
    fun floatValue_withInfinity_throws() {
        assertFailsWith<IllegalArgumentException> {
            FloatValue(Float.POSITIVE_INFINITY)
        }
    }

    @Test
    fun floatValue_withNegativeInfinity_throws() {
        assertFailsWith<IllegalArgumentException> {
            FloatValue(Float.NEGATIVE_INFINITY)
        }
    }

    @Test
    fun floatValue_toInt_convertsCorrectly() {
        val floatValue = FloatValue(3.7f)
        assertEquals(3, floatValue.toInt())
    }

    @Test
    fun floatValue_toFloat_returnsCorrectValue() {
        val floatValue = FloatValue(3.14f)
        assertEquals(3.14f, floatValue.toFloat())
    }

    @Test
    fun floatValue_toBoolean_withNonZero_returnsTrue() {
        val floatValue = FloatValue(1.0f)
        assertTrue(floatValue.toBoolean())
    }

    @Test
    fun floatValue_toBoolean_withZero_returnsFalse() {
        val floatValue = FloatValue(0f)
        assertFalse(floatValue.toBoolean())
    }

    @Test
    fun floatValue_equals_withSameValue_returnsTrue() {
        val floatValue1 = FloatValue(3.14f)
        val floatValue2 = FloatValue(3.14f)
        assertEquals(floatValue1, floatValue2)
    }

    @Test
    fun floatValue_toString_returnsFormattedString() {
        val floatValue = FloatValue(3.14f)
        assertEquals("FloatValue(value=3.14)", floatValue.toString())
    }

    // ===== BoolValue Tests =====

    @Test
    fun boolValue_withTrue_createsInstance() {
        val boolValue = BoolValue(true)
        assertTrue(boolValue.value)
    }

    @Test
    fun boolValue_withFalse_createsInstance() {
        val boolValue = BoolValue(false)
        assertFalse(boolValue.value)
    }

    @Test
    fun boolValue_toInt_withTrue_returnsOne() {
        val boolValue = BoolValue(true)
        assertEquals(1, boolValue.toInt())
    }

    @Test
    fun boolValue_toInt_withFalse_returnsZero() {
        val boolValue = BoolValue(false)
        assertEquals(0, boolValue.toInt())
    }

    @Test
    fun boolValue_toFloat_withTrue_returnsOne() {
        val boolValue = BoolValue(true)
        assertEquals(1f, boolValue.toFloat())
    }

    @Test
    fun boolValue_toFloat_withFalse_returnsZero() {
        val boolValue = BoolValue(false)
        assertEquals(0f, boolValue.toFloat())
    }

    @Test
    fun boolValue_toBoolean_returnsOriginalValue() {
        val trueValue = BoolValue(true)
        val falseValue = BoolValue(false)
        assertTrue(trueValue.toBoolean())
        assertFalse(falseValue.toBoolean())
    }

    @Test
    fun boolValue_equals_withSameValue_returnsTrue() {
        val boolValue1 = BoolValue(true)
        val boolValue2 = BoolValue(true)
        assertEquals(boolValue1, boolValue2)
    }

    @Test
    fun boolValue_equals_withDifferentValue_returnsFalse() {
        val boolValue1 = BoolValue(true)
        val boolValue2 = BoolValue(false)
        assertNotEquals(boolValue1, boolValue2)
    }

    @Test
    fun boolValue_toString_returnsFormattedString() {
        val boolValue = BoolValue(true)
        assertEquals("BoolValue(value=true)", boolValue.toString())
    }

    // ===== PercentValue Tests =====

    @Test
    fun percentValue_withValidValue_createsInstance() {
        val percentValue = PercentValue(0.5f)
        assertEquals(0.5f, percentValue.value)
    }

    @Test
    fun percentValue_withMinValue_succeeds() {
        val percentValue = PercentValue(0.0f)
        assertEquals(0.0f, percentValue.value)
    }

    @Test
    fun percentValue_withMaxValue_succeeds() {
        val percentValue = PercentValue(1.0f)
        assertEquals(1.0f, percentValue.value)
    }

    @Test
    fun percentValue_belowMin_throws() {
        assertFailsWith<IllegalArgumentException> {
            PercentValue(-0.1f)
        }
    }

    @Test
    fun percentValue_aboveMax_throws() {
        assertFailsWith<IllegalArgumentException> {
            PercentValue(1.1f)
        }
    }

    @Test
    fun percentValue_toInt_convertsToPercentage() {
        val percentValue = PercentValue(0.5f)
        assertEquals(50, percentValue.toInt())
    }

    @Test
    fun percentValue_toInt_withFullPercent_returns100() {
        val percentValue = PercentValue(1.0f)
        assertEquals(100, percentValue.toInt())
    }

    @Test
    fun percentValue_toInt_withZeroPercent_returns0() {
        val percentValue = PercentValue(0.0f)
        assertEquals(0, percentValue.toInt())
    }

    @Test
    fun percentValue_toFloat_returnsOriginalValue() {
        val percentValue = PercentValue(0.75f)
        assertEquals(0.75f, percentValue.toFloat())
    }

    @Test
    fun percentValue_toBoolean_withZero_returnsFalse() {
        val percentValue = PercentValue(0.0f)
        assertFalse(percentValue.toBoolean())
    }

    @Test
    fun percentValue_toBoolean_withPositive_returnsTrue() {
        val percentValue = PercentValue(0.01f)
        assertTrue(percentValue.toBoolean())
    }

    @Test
    fun percentValue_toDisplayString_with50Percent_returns50Percent() {
        val percentValue = PercentValue(0.5f)
        assertEquals("50%", percentValue.toDisplayString())
    }

    @Test
    fun percentValue_toDisplayString_with0Percent_returns0Percent() {
        val percentValue = PercentValue(0.0f)
        assertEquals("0%", percentValue.toDisplayString())
    }

    @Test
    fun percentValue_toDisplayString_with100Percent_returns100Percent() {
        val percentValue = PercentValue(1.0f)
        assertEquals("100%", percentValue.toDisplayString())
    }

    @Test
    fun percentValue_ZERO_returnsZeroPercent() {
        assertEquals(0.0f, PercentValue.ZERO.value)
    }

    @Test
    fun percentValue_HUNDRED_returnsFullPercent() {
        assertEquals(1.0f, PercentValue.HUNDRED.value)
    }

    @Test
    fun percentValue_FIFTY_returns50Percent() {
        assertEquals(0.5f, PercentValue.FIFTY.value)
    }

    @Test
    fun percentValue_equals_withSameValue_returnsTrue() {
        val percent1 = PercentValue(0.5f)
        val percent2 = PercentValue(0.5f)
        assertEquals(percent1, percent2)
    }

    @Test
    fun percentValue_equals_withDifferentValue_returnsFalse() {
        val percent1 = PercentValue(0.5f)
        val percent2 = PercentValue(0.75f)
        assertNotEquals(percent1, percent2)
    }

    @Test
    fun percentValue_toString_returnsFormattedString() {
        val percentValue = PercentValue(0.5f)
        assertEquals("PercentValue(value=0.5)", percentValue.toString())
    }

    // ===== AttributeValue Sealed Interface Tests =====

    @Test
    fun attributeValue_IntValue_implementsInterface() {
        val value: AttributeValue = IntValue(100)
        assertEquals(100, value.toInt())
        assertEquals(100f, value.toFloat())
        assertTrue(value.toBoolean())
    }

    @Test
    fun attributeValue_FloatValue_implementsInterface() {
        val value: AttributeValue = FloatValue(3.14f)
        assertEquals(3, value.toInt())
        assertEquals(3.14f, value.toFloat())
        assertTrue(value.toBoolean())
    }

    @Test
    fun attributeValue_BoolValue_implementsInterface() {
        val value: AttributeValue = BoolValue(false)
        assertEquals(0, value.toInt())
        assertEquals(0f, value.toFloat())
        assertFalse(value.toBoolean())
    }

    @Test
    fun attributeValue_PercentValue_implementsInterface() {
        val value: AttributeValue = PercentValue(0.5f)
        assertEquals(50, value.toInt())
        assertEquals(0.5f, value.toFloat())
        assertTrue(value.toBoolean())
    }
}
