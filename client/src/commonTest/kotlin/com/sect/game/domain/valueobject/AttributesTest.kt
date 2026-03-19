package com.sect.game.domain.valueobject

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame

class AttributesTest {
    @Test
    fun constructor_withValidValues_createsAttributes() {
        val attrs = Attributes(spiritRoot = 50, talent = 60, luck = 70)
        assertEquals(50, attrs.spiritRoot)
        assertEquals(60, attrs.talent)
        assertEquals(70, attrs.luck)
    }

    @Test
    fun constructor_withBoundaryValues_accepted() {
        Attributes(spiritRoot = 1, talent = 1, luck = 1)
        Attributes(spiritRoot = 100, talent = 100, luck = 100)
    }

    @Test
    fun constructor_withInvalidSpiritRoot_throws() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Attributes(spiritRoot = 0, talent = 50, luck = 50)
        }
        assertEquals("spiritRoot must be between 1 and 100, but was 0", exception.message)
    }

    @Test
    fun constructor_withInvalidSpiritRootTooHigh_throws() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Attributes(spiritRoot = 101, talent = 50, luck = 50)
        }
        assertEquals("spiritRoot must be between 1 and 100, but was 101", exception.message)
    }

    @Test
    fun constructor_withInvalidTalent_throws() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Attributes(spiritRoot = 50, talent = 0, luck = 50)
        }
        assertEquals("talent must be between 1 and 100, but was 0", exception.message)
    }

    @Test
    fun constructor_withInvalidTalentTooHigh_throws() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Attributes(spiritRoot = 50, talent = 101, luck = 50)
        }
        assertEquals("talent must be between 1 and 100, but was 101", exception.message)
    }

    @Test
    fun constructor_withInvalidLuck_throws() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Attributes(spiritRoot = 50, talent = 50, luck = 0)
        }
        assertEquals("luck must be between 1 and 100, but was 0", exception.message)
    }

    @Test
    fun constructor_withInvalidLuckTooHigh_throws() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Attributes(spiritRoot = 50, talent = 50, luck = 101)
        }
        assertEquals("luck must be between 1 and 100, but was 101", exception.message)
    }

    @Test
    fun default_returnsAttributesWith50Values() {
        val default = Attributes.DEFAULT
        assertEquals(50, default.spiritRoot)
        assertEquals(50, default.talent)
        assertEquals(50, default.luck)
    }

    @Test
    fun copy_preservesValues() {
        val original = Attributes(spiritRoot = 30, talent = 40, luck = 50)
        val copied = original.copy()
        assertEquals(original.spiritRoot, copied.spiritRoot)
        assertEquals(original.talent, copied.talent)
        assertEquals(original.luck, copied.luck)
        assertNotSame(original, copied)
    }

    @Test
    fun equals_withSameValues_returnsTrue() {
        val attrs1 = Attributes(spiritRoot = 30, talent = 40, luck = 50)
        val attrs2 = Attributes(spiritRoot = 30, talent = 40, luck = 50)
        assertEquals(attrs1, attrs2)
    }

    @Test
    fun equals_withDifferentValues_returnsFalse() {
        val attrs1 = Attributes(spiritRoot = 30, talent = 40, luck = 50)
        val attrs2 = Attributes(spiritRoot = 31, talent = 40, luck = 50)
        assertNotSame(attrs1, attrs2)
    }

    @Test
    fun hashCode_withSameValues_areEqual() {
        val attrs1 = Attributes(spiritRoot = 30, talent = 40, luck = 50)
        val attrs2 = Attributes(spiritRoot = 30, talent = 40, luck = 50)
        assertEquals(attrs1.hashCode(), attrs2.hashCode())
    }

    @Test
    fun toString_returnsFormattedString() {
        val attrs = Attributes(spiritRoot = 30, talent = 40, luck = 50)
        val str = attrs.toString()
        assertEquals("Attributes(spiritRoot=30, talent=40, luck=50)", str)
    }
}
