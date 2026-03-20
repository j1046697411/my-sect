package com.sect.game.domain.entity

import com.sect.game.domain.exception.CultivationException
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DiscipleTest {
    @Test
    fun create_withValidInput_returnsSuccess() {
        val id = DiscipleId("test-id")
        val attributes = Attributes.DEFAULT

        val result = Disciple.create(id, "张三", attributes)

        assertTrue(result.isSuccess)
        val disciple = result.getOrThrow()
        assertEquals("张三", disciple.name)
        assertEquals(Realm.LianQi, disciple.realm)
        assertEquals(0, disciple.cultivationProgress)
        assertEquals(0, disciple.fatigue)
        assertEquals(100, disciple.health)
    }

    @Test
    fun create_withBlankName_returnsFailure() {
        val id = DiscipleId("test-id")
        val attributes = Attributes.DEFAULT

        val result = Disciple.create(id, "   ", attributes)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun isExhausted_whenFatigueBelow100_returnsFalse() {
        val disciple = createTestDisciple(fatigue = 50)
        assertFalse(disciple.isExhausted())
    }

    @Test
    fun isExhausted_whenFatigueAt100_returnsTrue() {
        val disciple = createTestDisciple(fatigue = 100)
        assertTrue(disciple.isExhausted())
    }

    @Test
    fun cultivate_withNormalDisciple_increasesProgressAndFatigue() {
        val disciple = createTestDisciple(cultivationProgress = 0, fatigue = 0, health = 100)

        val result = disciple.cultivate()

        assertTrue(result.isSuccess)
        val updated = result.getOrNull()
        assertEquals(17, updated?.cultivationProgress)
        assertEquals(13, updated?.fatigue)
        assertEquals(96, updated?.health)
    }

    @Test
    fun cultivate_withExhaustedDisciple_returnsExhaustedException() {
        val disciple = createTestDisciple(fatigue = 100)

        val result = disciple.cultivate()

        assertTrue(result.isFailure)
        assertIs<CultivationException.ExhaustedException>(result.exceptionOrNull())
    }

    @Test
    fun cultivate_withDeadDisciple_returnsDeadException() {
        val disciple = createTestDisciple(health = 0, lifespan = 0)

        val result = disciple.cultivate()

        assertTrue(result.isFailure)
        assertIs<CultivationException.DeadDiscipleException>(result.exceptionOrNull())
    }

    @Test
    fun cultivate_capsProgressAt100() {
        val disciple = createTestDisciple(cultivationProgress = 95)

        val result = disciple.cultivate()

        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()?.cultivationProgress)
    }

    @Test
    fun rest_withNormalDisciple_decreasesFatigueAndIncreasesHealth() {
        val disciple = createTestDisciple(fatigue = 50, health = 80)

        val result = disciple.rest()

        assertTrue(result.isSuccess)
        val updated = result.getOrNull()
        assertEquals(20, updated?.fatigue)
        assertEquals(100, updated?.health)
    }

    @Test
    fun rest_withDeadDisciple_returnsDeadException() {
        val disciple = createTestDisciple(health = 0)

        val result = disciple.rest()

        assertTrue(result.isFailure)
        assertIs<CultivationException.DeadDiscipleException>(result.exceptionOrNull())
    }

    @Test
    fun rest_doesNotGoBelowZeroFatigue() {
        val disciple = createTestDisciple(fatigue = 10)

        val result = disciple.rest()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.fatigue)
    }

    @Test
    fun rest_doesNotExceed100Health() {
        val disciple = createTestDisciple(health = 90)

        val result = disciple.rest()

        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()?.health)
    }

    @Test
    fun attemptBreakthrough_withFullProgress_returnsNextRealm() {
        val disciple = createTestDisciple(cultivationProgress = 100, realm = Realm.LianQi)

        val result = disciple.attemptBreakthrough()

        assertTrue(result.isSuccess)
        assertEquals(Realm.ZhuJi, result.getOrNull())
    }

    @Test
    fun attemptBreakthrough_withInsufficientProgress_returnsInsufficientException() {
        val disciple = createTestDisciple(cultivationProgress = 50)

        val result = disciple.attemptBreakthrough()

        assertTrue(result.isFailure)
        assertIs<CultivationException.InsufficientProgressException>(result.exceptionOrNull())
    }

    @Test
    fun attemptBreakthrough_atMaxRealm_returnsMaxRealmException() {
        val disciple = createTestDisciple(cultivationProgress = 100, realm = Realm.HuaShen)

        val result = disciple.attemptBreakthrough()

        assertTrue(result.isFailure)
        assertIs<CultivationException.MaxRealmReachedException>(result.exceptionOrNull())
    }

    @Test
    fun attemptBreakthrough_withDeadDisciple_returnsDeadException() {
        val disciple = createTestDisciple(health = 0, cultivationProgress = 100)

        val result = disciple.attemptBreakthrough()

        assertTrue(result.isFailure)
        assertIs<CultivationException.DeadDiscipleException>(result.exceptionOrNull())
    }

    @Test
    fun highSpiritRoot_increasesCultivationGain() {
        val highSpiritAttributes = Attributes(spiritRoot = 100, talent = 50, luck = 50)
        val lowSpiritAttributes = Attributes(spiritRoot = 10, talent = 50, luck = 50)

        val highSpiritDisciple = createTestDisciple(attributes = highSpiritAttributes, cultivationProgress = 0)
        val lowSpiritDisciple = createTestDisciple(attributes = lowSpiritAttributes, cultivationProgress = 0)

        highSpiritDisciple.cultivate()
        lowSpiritDisciple.cultivate()

        assertTrue(
            highSpiritDisciple.cultivate().getOrNull()!!.cultivationProgress >
                lowSpiritDisciple.cultivate().getOrNull()!!.cultivationProgress,
        )
    }

    @Test
    fun highTalent_reducesFatigueGain() {
        val highTalentAttributes = Attributes(spiritRoot = 50, talent = 100, luck = 50)
        val lowTalentAttributes = Attributes(spiritRoot = 50, talent = 1, luck = 50)

        val highTalentDisciple = createTestDisciple(attributes = highTalentAttributes, fatigue = 0)
        val lowTalentDisciple = createTestDisciple(attributes = lowTalentAttributes, fatigue = 0)

        highTalentDisciple.cultivate()
        lowTalentDisciple.cultivate()

        assertTrue(
            highTalentDisciple.cultivate().getOrNull()!!.fatigue <=
                lowTalentDisciple.cultivate().getOrNull()!!.fatigue,
        )
    }

    @Test
    fun highLuck_reducesHealthLoss() {
        val highLuckAttributes = Attributes(spiritRoot = 50, talent = 50, luck = 100)
        val lowLuckAttributes = Attributes(spiritRoot = 50, talent = 50, luck = 1)

        val highLuckDisciple = createTestDisciple(attributes = highLuckAttributes, health = 100)
        val lowLuckDisciple = createTestDisciple(attributes = lowLuckAttributes, health = 100)

        highLuckDisciple.cultivate()
        lowLuckDisciple.cultivate()

        assertTrue(
            highLuckDisciple.cultivate().getOrNull()!!.health >=
                lowLuckDisciple.cultivate().getOrNull()!!.health,
        )
    }

    @Test
    fun isDead_withZeroHealth_returnsTrue() {
        val disciple = createTestDisciple(health = 0)
        assertTrue(disciple.isDead())
    }

    @Test
    fun isDead_withZeroLifespan_returnsTrue() {
        val disciple = createTestDisciple(lifespan = 0)
        assertTrue(disciple.isDead())
    }

    @Test
    fun isDead_withPositiveHealthAndLifespan_returnsFalse() {
        val disciple = createTestDisciple(health = 50, lifespan = 50)
        assertFalse(disciple.isDead())
    }

    @Test
    fun isHealthy_withHealthAbove50_returnsTrue() {
        val disciple = createTestDisciple(health = 51)
        assertTrue(disciple.isHealthy())
    }

    @Test
    fun isHealthy_withHealthAt50_returnsFalse() {
        val disciple = createTestDisciple(health = 49)
        assertFalse(disciple.isHealthy())
    }

    @Test
    fun create_withCustomRealmAndLifespan() {
        val id = DiscipleId("custom-id")
        val attributes = Attributes.DEFAULT

        val result =
            Disciple.create(
                id = id,
                name = "李四",
                attributes = attributes,
                realm = Realm.JinDan,
                lifespan = 200,
            )

        assertTrue(result.isSuccess)
        val disciple = result.getOrThrow()
        assertEquals(Realm.JinDan, disciple.realm)
        assertEquals(200, disciple.lifespan)
    }

    @Test
    fun multipleCultivations_accumulateProgressAndFatigue() {
        var disciple = createTestDisciple(cultivationProgress = 0, fatigue = 0, health = 100)

        repeat(3) {
            disciple = disciple.cultivate().getOrNull()!!
        }

        assertTrue(disciple.cultivationProgress > 0)
        assertTrue(disciple.fatigue > 0)
    }

    private fun createTestDisciple(
        id: DiscipleId = DiscipleId("test-id"),
        name: String = "测试弟子",
        realm: Realm = Realm.LianQi,
        attributes: Attributes = Attributes.DEFAULT,
        cultivationProgress: Int = 0,
        fatigue: Int = 0,
        health: Int = 100,
        lifespan: Int = 100,
    ): Disciple {
        return Disciple(
            id = id,
            name = name,
            realm = realm,
            attributes = attributes,
            cultivationProgress = cultivationProgress,
            fatigue = fatigue,
            health = health,
            lifespan = lifespan,
        )
    }
}
