package com.sect.game.domain.valueobject

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class IdentifiersTest {
    @Test
    fun discipleId_valueProperty_returnsCorrectValue() {
        val id = DiscipleId("disciple-123")
        assertEquals("disciple-123", id.value)
    }

    @Test
    fun sectId_valueProperty_returnsCorrectValue() {
        val id = SectId("sect-456")
        assertEquals("sect-456", id.value)
    }

    @Test
    fun discipleId_equality_withSameValue_areEqual() {
        val id1 = DiscipleId("disciple-123")
        val id2 = DiscipleId("disciple-123")
        assertEquals(id1, id2)
    }

    @Test
    fun discipleId_equality_withDifferentValue_areNotEqual() {
        val id1 = DiscipleId("disciple-123")
        val id2 = DiscipleId("disciple-456")
        assertNotSame(id1, id2)
    }

    @Test
    fun sectId_equality_withSameValue_areEqual() {
        val id1 = SectId("sect-123")
        val id2 = SectId("sect-123")
        assertEquals(id1, id2)
    }

    @Test
    fun sectId_equality_withDifferentValue_areNotEqual() {
        val id1 = SectId("sect-123")
        val id2 = SectId("sect-456")
        assertNotSame(id1, id2)
    }

    @Test
    fun discipleId_hashCode_withSameValue_areEqual() {
        val id1 = DiscipleId("disciple-123")
        val id2 = DiscipleId("disciple-123")
        assertEquals(id1.hashCode(), id2.hashCode())
    }

    @Test
    fun sectId_hashCode_withSameValue_areEqual() {
        val id1 = SectId("sect-123")
        val id2 = SectId("sect-123")
        assertEquals(id1.hashCode(), id2.hashCode())
    }

    @Test
    fun discipleId_toString_returnsFormattedString() {
        val id = DiscipleId("disciple-123")
        assertEquals("DiscipleId(value=disciple-123)", id.toString())
    }

    @Test
    fun sectId_toString_returnsFormattedString() {
        val id = SectId("sect-123")
        assertEquals("SectId(value=sect-123)", id.toString())
    }

    @Test
    fun discipleId_canBeUsedAsMapKey() {
        val map = mapOf(DiscipleId("d1") to "disciple1", DiscipleId("d2") to "disciple2")
        assertEquals("disciple1", map[DiscipleId("d1")])
        assertEquals("disciple2", map[DiscipleId("d2")])
    }

    @Test
    fun sectId_canBeUsedAsMapKey() {
        val map = mapOf(SectId("s1") to "sect1", SectId("s2") to "sect2")
        assertEquals("sect1", map[SectId("s1")])
        assertEquals("sect2", map[SectId("s2")])
    }
}
