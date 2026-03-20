package com.sect.game.domain.entity

import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiscipleLifespanTest {
    @Test
    fun disciple_withZeroLifespan_isDead() {
        val disciple = createDisciple(lifespan = 0)
        assertTrue(disciple.isDead())
    }

    @Test
    fun disciple_withZeroHealth_isDead() {
        val disciple = createDisciple(health = 0)
        assertTrue(disciple.isDead())
    }

    @Test
    fun disciple_cultivate_doesNotChangeLifespan() {
        val initialLifespan = 100
        val disciple = createDisciple(lifespan = initialLifespan, health = 100, fatigue = 0)

        val result = disciple.cultivate()

        assertTrue(result.isSuccess)
        val cultivated = result.getOrThrow()
        assertEquals(initialLifespan, cultivated.lifespan, "当前实现中修炼不改变寿元")
    }

    @Test
    fun disciple_withLifespan1_canCultivate() {
        val disciple = createDisciple(lifespan = 1, health = 100, fatigue = 0)

        val result = disciple.cultivate()

        assertTrue(result.isSuccess, "当前实现中寿元>0即可修炼")
    }

    @Test
    fun disciple_rest_doesNotRestoreLifespan() {
        val initialLifespan = 80
        val disciple = createDisciple(lifespan = initialLifespan, health = 50, fatigue = 80)

        val result = disciple.rest()

        assertTrue(result.isSuccess)
        val rested = result.getOrThrow()
        assertEquals(initialLifespan, rested.lifespan, "休息不应恢复寿元")
    }

    @Test
    fun disciple_withHighLifespan_canCultivateMultipleTimes() {
        var disciple = createDisciple(lifespan = 200, health = 100, fatigue = 0)

        repeat(5) {
            val result = disciple.cultivate()
            assertTrue(result.isSuccess, "高寿元弟子应能多次修炼")
            disciple = result.getOrThrow()
        }

        assertTrue(disciple.lifespan > 0, "高寿元弟子经过5次修炼后仍应存活")
        assertEquals(200, disciple.lifespan, "当前实现中寿元不随修炼减少")
    }

    @Test
    fun disciple_lifespan_boundary_1_isAlive() {
        val disciple = createDisciple(lifespan = 1, health = 100)
        assertFalse(disciple.isDead())
    }

    @Test
    fun disciple_lifespan_boundary_0_isDead() {
        val disciple = createDisciple(lifespan = 0, health = 100)
        assertTrue(disciple.isDead())
    }

    @Test
    fun disciple_maxRealm_cultivate_doesNotChangeLifespan() {
        val initialLifespan = 100
        val disciple = createDisciple(
            realm = Realm.HuaShen,
            lifespan = initialLifespan,
            health = 100,
            fatigue = 0,
        )

        val result = disciple.cultivate()

        assertTrue(result.isSuccess)
        val cultivated = result.getOrThrow()
        assertEquals(initialLifespan, cultivated.lifespan, "化神境界修炼不改变寿元")
    }

    private fun createDisciple(
        id: DiscipleId = DiscipleId("test"),
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
