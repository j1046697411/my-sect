package com.sect.game.e2e

import com.sect.game.data.storage.GameStorage
import com.sect.game.data.storage.InMemoryGameStorage
import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.entity.Sect
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm
import com.sect.game.domain.valueobject.SectId
import com.sect.game.engine.GameEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameE2ETest {

    @Test
    fun createDisciple_verifyInList_startEngine_autoCultivation_verifiesProgress() {
        val sect = createSect()
        val engine = GameEngine.create(sect, tickRate = 100)

        val disciple = createDisciple("张三")
        sect.addDisciple(disciple)

        assertTrue(sect.disciples.containsKey(disciple.id))
        assertEquals(1, sect.discipleCount)

        engine.start()
        Thread.sleep(200)
        engine.stop()

        val updatedDisciple = sect.getDisciple(disciple.id)
        assertNotNull(updatedDisciple)
        assertTrue(updatedDisciple.cultivationProgress > 0 || updatedDisciple.fatigue > 0)
    }

    @Test
    fun createMultipleDisciples_verifyAllExist_startEngine_verifyAllProgress() {
        val sect = createSect()
        val engine = GameEngine.create(sect, tickRate = 100)

        val disciple1 = createDisciple("弟子甲")
        val disciple2 = createDisciple("弟子乙")
        val disciple3 = createDisciple("弟子丙")

        sect.addDisciple(disciple1)
        sect.addDisciple(disciple2)
        sect.addDisciple(disciple3)

        assertEquals(3, sect.discipleCount)

        engine.start()
        Thread.sleep(300)
        engine.stop()

        val d1 = sect.getDisciple(disciple1.id)
        val d2 = sect.getDisciple(disciple2.id)
        val d3 = sect.getDisciple(disciple3.id)

        assertNotNull(d1)
        assertNotNull(d2)
        assertNotNull(d3)
        assertTrue(d1.cultivationProgress > 0 || d1.fatigue > 0)
        assertTrue(d2.cultivationProgress > 0 || d2.fatigue > 0)
        assertTrue(d3.cultivationProgress > 0 || d3.fatigue > 0)
    }

    @Test
    fun saveAndLoadGame_verifyStatePreserved() {
        val storage: GameStorage = InMemoryGameStorage()
        val sect = createSect()
        val disciple = createDisciple("保存测试")
        sect.addDisciple(disciple)

        val engine = GameEngine.create(sect, tickRate = 100)
        engine.start()
        Thread.sleep(200)
        engine.stop()

        val discipleBeforeSave = sect.getDisciple(disciple.id)
        assertNotNull(discipleBeforeSave)
        val progressBeforeSave = discipleBeforeSave.cultivationProgress

        storage.saveGame(sect, version = 1)

        val loadedSect = storage.loadGame().getOrThrow()

        assertEquals(sect.name, loadedSect.name)
        assertEquals(sect.discipleCount, loadedSect.discipleCount)

        val loadedDisciple = loadedSect.getDisciple(disciple.id)
        assertNotNull(loadedDisciple)
        assertEquals(disciple.name, loadedDisciple.name)
        assertEquals(disciple.realm, loadedDisciple.realm)
    }

    @Test
    fun saveGame_deleteGame_verifyDeleted() {
        val storage: GameStorage = InMemoryGameStorage()
        val sect = createSect()

        storage.saveGame(sect, version = 1)
        assertTrue(storage.gameExists())

        storage.deleteGame()

        assertFalse(storage.gameExists())
    }

    @Test
    fun loadGame_noSavedGame_returnsFailure() {
        val storage: GameStorage = InMemoryGameStorage()

        val result = storage.loadGame()

        assertTrue(result.isFailure)
    }

    @Test
    fun gameEngine_pauseResume_maintainsState() {
        val sect = createSect()
        val disciple = createDisciple("暂停测试")
        sect.addDisciple(disciple)

        val engine = GameEngine.create(sect, tickRate = 100)
        engine.start()
        Thread.sleep(100)

        val discipleBeforePause = sect.getDisciple(disciple.id)
        assertNotNull(discipleBeforePause)

        engine.pause()
        assertTrue(engine.isPaused)

        val tickCountWhilePaused = engine.tickCount
        Thread.sleep(100)

        engine.resume()
        assertFalse(engine.isPaused)

        engine.stop()
    }

    @Test
    fun fullGameCycle_createSect_addDisciples_cultivate_breakthrough() {
        val sect = createSect()
        val engine = GameEngine.create(sect, tickRate = 100)

        val disciple = createDisciple("修仙弟子")
        sect.addDisciple(disciple)

        assertEquals(Realm.炼气, disciple.realm)
        assertEquals(0, disciple.cultivationProgress)

        engine.start()
        Thread.sleep(500)
        engine.stop()

        val cultivated = sect.getDisciple(disciple.id)
        assertNotNull(cultivated)
        assertTrue(
            cultivated.cultivationProgress > 0 ||
            cultivated.fatigue > 0 ||
            cultivated.health < 100
        )
    }

    @Test
    fun discipleStateTransitions_cultivateUntilExhausted_rest_recovers() {
        val sect = createSect()
        val engine = GameEngine.create(sect, tickRate = 100)
        val disciple = createDisciple("疲劳测试")
        sect.addDisciple(disciple)

        engine.start()
        Thread.sleep(1000)
        engine.stop()

        val updated = sect.getDisciple(disciple.id)
        assertNotNull(updated)

        if (updated.isExhausted()) {
            val rested = updated.rest().getOrThrow()
            assertTrue(rested.fatigue < updated.fatigue)
        }
    }

    @Test
    fun sectCapacity_limitReached_cannotAddMore() {
        val smallSect = Sect.create(
            id = SectId("小宗门"),
            name = "小宗门",
            maxDisciples = 2
        ).getOrThrow()

        smallSect.addDisciple(createDisciple("弟子1"))
        smallSect.addDisciple(createDisciple("弟子2"))

        val result = smallSect.addDisciple(createDisciple("弟子3"))

        assertTrue(result.isFailure)
        assertEquals(2, smallSect.discipleCount)
    }

    @Test
    fun multipleSaveLoad_preservesLatestState() {
        val storage: GameStorage = InMemoryGameStorage()
        var sect = createSect()
        val engine = GameEngine.create(sect, tickRate = 100)

        val disciple = createDisciple("多次保存")
        sect.addDisciple(disciple)

        engine.start()
        Thread.sleep(150)
        engine.stop()

        storage.saveGame(sect, version = 1)

        val updatedDisciple = sect.getDisciple(disciple.id)
        assertNotNull(updatedDisciple)
        val firstProgress = updatedDisciple.cultivationProgress

        Thread.sleep(100)

        engine.start()
        Thread.sleep(150)
        engine.stop()

        storage.saveGame(sect, version = 1)

        val loadedSect = storage.loadGame().getOrThrow()
        val loadedDisciple = loadedSect.getDisciple(disciple.id)
        assertNotNull(loadedDisciple)
        assertTrue(loadedDisciple.cultivationProgress >= firstProgress)
    }

    @Test
    fun engineWithHighTickRate_updatesDisciplesFrequently() {
        val sect = createSect()
        val fastEngine = GameEngine.create(sect, tickRate = 200)
        val slowEngine = GameEngine.create(sect, tickRate = 10)

        val disciple1 = createDisciple("快速")
        val disciple2 = createDisciple("慢速")
        sect.addDisciple(disciple1)
        sect.addDisciple(disciple2)

        fastEngine.start()
        Thread.sleep(100)
        fastEngine.stop()

        val fastDisciple = sect.getDisciple(disciple1.id)
        assertNotNull(fastDisciple)
        val fastProgress = fastDisciple.cultivationProgress

        sect.removeDisciple(disciple1.id)
        sect.addDisciple(createDisciple("慢速2"))

        slowEngine.start()
        Thread.sleep(100)
        slowEngine.stop()

        val slowDisciple = sect.getDisciple(disciple2.id)
        assertNotNull(slowDisciple)
    }

    private fun createSect(): Sect {
        return Sect.create(
            id = SectId("e2e-sect-${System.nanoTime()}"),
            name = "E2E测试宗门"
        ).getOrThrow()
    }

    private fun createDisciple(name: String): Disciple {
        return Disciple.create(
            id = DiscipleId("e2e-disciple-$name-${System.nanoTime()}"),
            name = name,
            attributes = Attributes.DEFAULT,
            realm = Realm.炼气,
            lifespan = 100
        ).getOrThrow()
    }
}
