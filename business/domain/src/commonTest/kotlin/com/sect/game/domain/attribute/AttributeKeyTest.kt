package com.sect.game.domain.attribute

import com.sect.game.domain.attribute.key.AttributeKey
import com.sect.game.domain.attribute.value.BoolValue
import com.sect.game.domain.attribute.value.IntValue
import com.sect.game.domain.attribute.value.PercentValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AttributeKeyTest {

    @Test
    fun attributeKey_withString_createsKey() {
        val key = AttributeKey<IntValue>("attack")
        assertEquals("attack", key.name)
    }

    @Test
    fun attributeKey_toString_returnsName() {
        val key = AttributeKey<IntValue>("defense")
        assertEquals("defense", key.toString())
    }

    @Test
    fun attributeKey_equals_withSameName_returnsTrue() {
        val key1 = AttributeKey<IntValue>("attack")
        val key2 = AttributeKey<IntValue>("attack")
        assertEquals(key1, key2)
    }

    @Test
    fun attributeKey_equals_withDifferentName_returnsFalse() {
        val key1 = AttributeKey<IntValue>("attack")
        val key2 = AttributeKey<IntValue>("defense")
        assertNotEquals(key1, key2)
    }

    @Test
    fun attributeKey_sameNameDifferentType_areEqual() {
        val key1 = AttributeKey<IntValue>("attack")
        val key2 = AttributeKey<PercentValue>("attack")
        assertEquals(key1.name, key2.name)
    }

    @Test
    fun attributeKey_hashCode_withSameName_areEqual() {
        val key1 = AttributeKey<IntValue>("attack")
        val key2 = AttributeKey<IntValue>("attack")
        assertEquals(key1.hashCode(), key2.hashCode())
    }

    @Test
    fun attributeKey_valueClass_equality() {
        val key1 = AttributeKey<IntValue>("speed")
        val key2 = AttributeKey<IntValue>("speed")
        assertTrue(key1 == key2)
    }

    @Test
    fun attributeKey_typeParameter_preserved() {
        val intKey = AttributeKey<IntValue>("int_key")
        val boolKey = AttributeKey<BoolValue>("bool_key")
        val percentKey = AttributeKey<PercentValue>("percent_key")
        assertTrue(intKey is AttributeKey<IntValue>)
        assertTrue(boolKey is AttributeKey<BoolValue>)
        assertTrue(percentKey is AttributeKey<PercentValue>)
    }

    @Test
    fun predefinedAttributes_attack_isCorrectType() {
        assertTrue(PredefinedAttributes.ATTACK is AttributeKey<IntValue>)
    }

    @Test
    fun predefinedAttributes_critRate_isCorrectType() {
        assertTrue(PredefinedAttributes.CRIT_RATE is AttributeKey<PercentValue>)
    }

    @Test
    fun predefinedAttributes_dodgeRate_isCorrectType() {
        assertTrue(PredefinedAttributes.DODGE_RATE is AttributeKey<PercentValue>)
    }

    @Test
    fun attributeKey_generated_fromPredefinedAttributes_hasCorrectNames() {
        assertEquals("attack", PredefinedAttributes.ATTACK.name)
        assertEquals("defense", PredefinedAttributes.DEFENSE.name)
        assertEquals("speed", PredefinedAttributes.SPEED.name)
        assertEquals("crit_rate", PredefinedAttributes.CRIT_RATE.name)
        assertEquals("crit_damage", PredefinedAttributes.CRIT_DAMAGE.name)
        assertEquals("max_hp", PredefinedAttributes.MAX_HP.name)
    }
}
