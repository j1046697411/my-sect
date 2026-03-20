package com.sect.game.goap.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class WorldStateTest {

    @Test
    fun default_createsEmptyState() {
        val state = WorldState()
        assertEquals(0, state.getInt("anyKey"))
        assertEquals(false, state.getBoolean("anyKey"))
        assertEquals(null, state.getFloat("anyKey"))
    }

    @Test
    fun withInt_addsIntValue() {
        val state = WorldState().withInt("health", 80)
        assertEquals(80, state.getInt("health"))
    }

    @Test
    fun withInt_returnsNewInstance() {
        val original = WorldState()
        val modified = original.withInt("health", 80)
        assertNotSame(original, modified)
        assertEquals(0, original.getInt("health"))
        assertEquals(80, modified.getInt("health"))
    }

    @Test
    fun withBoolean_addsBooleanValue() {
        val state = WorldState().withBoolean("isResting", true)
        assertTrue(state.getBoolean("isResting"))
    }

    @Test
    fun withBoolean_returnsNewInstance() {
        val original = WorldState()
        val modified = original.withBoolean("isResting", true)
        assertNotSame(original, modified)
        assertFalse(original.getBoolean("isResting"))
        assertTrue(modified.getBoolean("isResting"))
    }

    @Test
    fun withFloat_addsFloatValue() {
        val state = WorldState().withFloat("progress", 0.75f)
        assertEquals(0.75f, state.getFloat("progress"))
    }

    @Test
    fun withFloat_returnsNewInstance() {
        val original = WorldState()
        val modified = original.withFloat("progress", 0.75f)
        assertNotSame(original, modified)
        assertEquals(null, original.getFloat("progress"))
        assertEquals(0.75f, modified.getFloat("progress"))
    }

    @Test
    fun withValue_forBackwardCompatibility() {
        val state = WorldState().withValue("health", 50)
        assertEquals(50, state.getValue("health"))
    }

    @Test
    fun getValue_returnsIntValue() {
        val state = WorldState().withInt("fatigue", 30)
        assertEquals(30, state.getValue("fatigue"))
    }

    @Test
    fun toMap_returnsIntMap() {
        val state = WorldState()
            .withInt("health", 80)
            .withInt("fatigue", 30)
        val map = state.toMap()
        assertEquals(80, map["health"])
        assertEquals(30, map["fatigue"])
    }

    @Test
    fun distanceTo_sameState_returnsZero() {
        val state = WorldState().withInt("health", 80)
        assertEquals(0f, state.distanceTo(state))
    }

    @Test
    fun distanceTo_identicalStates_returnsZero() {
        val state1 = WorldState().withInt("health", 80)
        val state2 = WorldState().withInt("health", 80)
        assertEquals(0f, state1.distanceTo(state2))
    }

    @Test
    fun distanceTo_differentIntValues_returnsOne() {
        val state1 = WorldState().withInt("health", 80)
        val state2 = WorldState().withInt("health", 50)
        assertEquals(1f, state1.distanceTo(state2))
    }

    @Test
    fun distanceTo_differentBooleanValues_returnsOne() {
        val state1 = WorldState().withBoolean("isResting", true)
        val state2 = WorldState().withBoolean("isResting", false)
        assertEquals(1f, state1.distanceTo(state2))
    }

    @Test
    fun distanceTo_differentFloatValues_returnsOne() {
        val state1 = WorldState().withFloat("progress", 0.5f)
        val state2 = WorldState().withFloat("progress", 0.8f)
        assertEquals(1f, state1.distanceTo(state2))
    }

    @Test
    fun distanceTo_multipleDifferences_returnsSum() {
        val state1 = WorldState()
            .withInt("health", 80)
            .withBoolean("isResting", true)
            .withFloat("progress", 0.5f)
        val state2 = WorldState()
            .withInt("health", 50)
            .withBoolean("isResting", false)
            .withFloat("progress", 0.8f)
        assertEquals(3f, state1.distanceTo(state2))
    }

    @Test
    fun distanceTo_extraKeyInOther_returnsOne() {
        val state1 = WorldState().withInt("health", 80)
        val state2 = WorldState()
            .withInt("health", 80)
            .withInt("fatigue", 30)
        assertEquals(1f, state1.distanceTo(state2))
    }

    @Test
    fun distanceTo_emptyStates_returnsZero() {
        val state1 = WorldState()
        val state2 = WorldState()
        assertEquals(0f, state1.distanceTo(state2))
    }

    @Test
    fun equals_sameValues_returnsTrue() {
        val state1 = WorldState().withInt("health", 80)
        val state2 = WorldState().withInt("health", 80)
        assertTrue(state1 == state2)
    }

    @Test
    fun equals_differentValues_returnsFalse() {
        val state1 = WorldState().withInt("health", 80)
        val state2 = WorldState().withInt("health", 50)
        assertFalse(state1 == state2)
    }

    @Test
    fun equals_differentTypes_returnsFalse() {
        val state1 = WorldState().withInt("health", 80)
        val state2 = WorldState().withBoolean("health", true)
        assertFalse(state1 == state2)
    }

    @Test
    fun hashCode_sameValues_sameHash() {
        val state1 = WorldState().withInt("health", 80)
        val state2 = WorldState().withInt("health", 80)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun fromMap_createsStateFromMap() {
        val map = mapOf("health" to 80, "fatigue" to 30)
        val state = WorldState.fromMap(map)
        assertEquals(80, state.getInt("health"))
        assertEquals(30, state.getInt("fatigue"))
    }
}
