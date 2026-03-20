package com.sect.game.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuffTest {
    @Test
    fun create_withValidInput_returnsSuccess() {
        val result = Buff.create(
            id = "buff-001",
            name = "力量祝福",
            duration = 60,
            stackable = false,
        )

        assertTrue(result.isSuccess)
        val buff = result.getOrThrow()
        assertEquals("buff-001", buff.id)
        assertEquals("力量祝福", buff.name)
        assertEquals(60, buff.duration)
        assertFalse(buff.stackable)
        assertTrue(buff.modifiers.isEmpty())
    }

    @Test
    fun create_withBlankId_returnsFailure() {
        val result = Buff.create(
            id = "",
            name = "力量祝福",
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun create_withBlankName_returnsFailure() {
        val result = Buff.create(
            id = "buff-001",
            name = "   ",
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun create_withNegativeDuration_returnsFailure() {
        val result = Buff.create(
            id = "buff-001",
            name = "力量祝福",
            duration = -2,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun create_withZeroDuration_succeeds() {
        val result = Buff.create(
            id = "buff-001",
            name = "力量祝福",
            duration = 0,
        )

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.duration)
    }

    @Test
    fun create_withNegativeOneDuration_meansPermanent() {
        val result = Buff.create(
            id = "buff-001",
            name = "永久buff",
            duration = -1,
        )

        assertTrue(result.isSuccess)
        assertEquals(-1, result.getOrNull()?.duration)
    }

    @Test
    fun isExpired_withPositiveDuration_returnsFalse() {
        val buff = Buff.create(
            id = "buff-001",
            name = "力量祝福",
            duration = 60,
        ).getOrThrow()

        assertFalse(buff.isExpired())
    }

    @Test
    fun isExpired_withZeroDuration_returnsTrue() {
        val buff = Buff.create(
            id = "buff-001",
            name = "力量祝福",
            duration = 0,
        ).getOrThrow()

        assertTrue(buff.isExpired())
    }

    @Test
    fun isExpired_withPermanent_returnsFalse() {
        val buff = Buff.create(
            id = "buff-001",
            name = "永久buff",
            duration = -1,
        ).getOrThrow()

        assertFalse(buff.isExpired())
    }

    @Test
    fun tick_withPositiveDuration_decreasesDuration() {
        val buff = Buff.create(
            id = "buff-001",
            name = "力量祝福",
            duration = 60,
        ).getOrThrow()

        val tickedBuff = buff.tick()

        assertEquals(59, tickedBuff.duration)
        assertEquals(buff.id, tickedBuff.id)
        assertEquals(buff.name, tickedBuff.name)
    }

    @Test
    fun tick_withZeroDuration_staysAtZero() {
        val buff = Buff.create(
            id = "buff-001",
            name = "力量祝福",
            duration = 0,
        ).getOrThrow()

        val tickedBuff = buff.tick()

        assertEquals(0, tickedBuff.duration)
    }

    @Test
    fun tick_withPermanent_returnsSameBuff() {
        val buff = Buff.create(
            id = "buff-001",
            name = "永久buff",
            duration = -1,
        ).getOrThrow()

        val tickedBuff = buff.tick()

        assertEquals(-1, tickedBuff.duration)
        assertEquals(buff, tickedBuff)
    }

    @Test
    fun tick_multipleTimes_decreasesDuration() {
        var buff = Buff.create(
            id = "buff-001",
            name = "力量祝福",
            duration = 10,
        ).getOrThrow()

        repeat(3) {
            buff = buff.tick()
        }

        assertEquals(7, buff.duration)
    }

    @Test
    fun create_stackableFlag_isPreserved() {
        val stackableBuff = Buff.create(
            id = "buff-001",
            name = "可叠加buff",
            stackable = true,
        ).getOrThrow()

        val nonStackableBuff = Buff.create(
            id = "buff-002",
            name = "不可叠加buff",
            stackable = false,
        ).getOrThrow()

        assertTrue(stackableBuff.stackable)
        assertFalse(nonStackableBuff.stackable)
    }

    @Test
    fun dataClass_equals_withSameValues_returnsTrue() {
        val buff1 = Buff.create(
            id = "buff-001",
            name = "力量祝福",
            duration = 60,
            stackable = false,
        ).getOrThrow()

        val buff2 = Buff.create(
            id = "buff-001",
            name = "力量祝福",
            duration = 60,
            stackable = false,
        ).getOrThrow()

        assertEquals(buff1, buff2)
    }

    @Test
    fun dataClass_equals_withDifferentValues_returnsFalse() {
        val buff1 = Buff.create(
            id = "buff-001",
            name = "力量祝福",
            duration = 60,
        ).getOrThrow()

        val buff2 = Buff.create(
            id = "buff-002",
            name = "力量祝福",
            duration = 60,
        ).getOrThrow()

        assertFalse(buff1 == buff2)
    }

    @Test
    fun tick_returnsNewInstance_notModifiesOriginal() {
        val originalBuff = Buff.create(
            id = "buff-001",
            name = "力量祝福",
            duration = 60,
        ).getOrThrow()

        val tickedBuff = originalBuff.tick()

        assertEquals(60, originalBuff.duration)
        assertEquals(59, tickedBuff.duration)
        assertFalse(originalBuff === tickedBuff)
    }
}
