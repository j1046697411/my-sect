package com.sect.game.data.storage

import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.entity.Resources
import com.sect.game.domain.entity.Sect
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm
import com.sect.game.domain.valueobject.SectId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsonGameStorageTest {
    private class FakePlatformStorage : PlatformGameStorage {
        private val storage = mutableMapOf<String, String>()

        override fun writeToFile(content: String) {
            storage["game_save"] = content
        }

        override fun readFromFile(): String? {
            return storage["game_save"]
        }

        override fun deleteFile() {
            storage.remove("game_save")
        }

        override fun fileExists(): Boolean {
            return storage.containsKey("game_save")
        }
    }

    private fun createTestSect(): Sect {
        val sect =
            Sect.create(
                id = SectId("test-sect-1"),
                name = "测试宗门",
                maxDisciples = 50,
                resources = Resources(spiritStones = 1000, herbs = 500, pills = 100),
            ).getOrThrow()

        val disciple =
            Disciple.create(
                id = DiscipleId("disciple-1"),
                name = "张三",
                attributes = Attributes(spiritRoot = 80, talent = 70, luck = 60),
                realm = Realm.ZhuJi,
                lifespan = 150,
            ).getOrThrow()

        sect.addDisciple(disciple)
        return sect
    }

    @Test
    fun saveGame_shouldPersistSectData() {
        val storage = FakePlatformStorage()
        val jsonStorage = JsonGameStorage(platformStorage = storage)
        val sect = createTestSect()

        val result = jsonStorage.saveGame(sect, version = 1)

        assertTrue(result.isSuccess)
        assertTrue(storage.fileExists())
    }

    @Test
    fun loadGame_shouldReturnSavedSect() {
        val storage = FakePlatformStorage()
        val jsonStorage = JsonGameStorage(platformStorage = storage)
        val originalSect = createTestSect()

        jsonStorage.saveGame(originalSect, version = 1)
        val loadedSect = jsonStorage.loadGame().getOrThrow()

        assertEquals(originalSect.id.value, loadedSect.id.value)
        assertEquals(originalSect.name, loadedSect.name)
        assertEquals(originalSect.resources.spiritStones, loadedSect.resources.spiritStones)
        assertEquals(originalSect.resources.herbs, loadedSect.resources.herbs)
        assertEquals(originalSect.resources.pills, loadedSect.resources.pills)
        assertEquals(originalSect.maxDisciples, loadedSect.maxDisciples)
        assertEquals(originalSect.discipleCount, loadedSect.discipleCount)
    }

    @Test
    fun loadGame_shouldThrowWhenNoSaveExists() {
        val storage = FakePlatformStorage()
        val jsonStorage = JsonGameStorage(platformStorage = storage)

        val result = jsonStorage.loadGame()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GameStorageException.GameNotFoundException)
    }

    @Test
    fun deleteGame_shouldRemoveSaveFile() {
        val storage = FakePlatformStorage()
        val jsonStorage = JsonGameStorage(platformStorage = storage)
        val sect = createTestSect()

        jsonStorage.saveGame(sect, version = 1)
        assertTrue(storage.fileExists())

        val deleteResult = jsonStorage.deleteGame()
        assertTrue(deleteResult.isSuccess)
        assertFalse(storage.fileExists())
    }

    @Test
    fun gameExists_shouldReturnFalseWhenNoSave() {
        val storage = FakePlatformStorage()
        val jsonStorage = JsonGameStorage(platformStorage = storage)

        assertFalse(jsonStorage.gameExists())
    }

    @Test
    fun gameExists_shouldReturnTrueWhenSaveExists() {
        val storage = FakePlatformStorage()
        val jsonStorage = JsonGameStorage(platformStorage = storage)
        val sect = createTestSect()

        jsonStorage.saveGame(sect, version = 1)

        assertTrue(jsonStorage.gameExists())
    }

    @Test
    fun saveAndLoad_shouldPreserveDiscipleData() {
        val storage = FakePlatformStorage()
        val jsonStorage = JsonGameStorage(platformStorage = storage)
        val sect = createTestSect()

        jsonStorage.saveGame(sect, version = 1)
        val loadedSect = jsonStorage.loadGame().getOrThrow()

        val originalDisciple = sect.disciples.values.first()
        val loadedDisciple = loadedSect.disciples.values.first()

        assertEquals(originalDisciple.id.value, loadedDisciple.id.value)
        assertEquals(originalDisciple.name, loadedDisciple.name)
        assertEquals(originalDisciple.realm.order, loadedDisciple.realm.order)
        assertEquals(originalDisciple.attributes.spiritRoot, loadedDisciple.attributes.spiritRoot)
        assertEquals(originalDisciple.attributes.talent, loadedDisciple.attributes.talent)
        assertEquals(originalDisciple.attributes.luck, loadedDisciple.attributes.luck)
        assertEquals(originalDisciple.cultivationProgress, loadedDisciple.cultivationProgress)
        assertEquals(originalDisciple.fatigue, loadedDisciple.fatigue)
        assertEquals(originalDisciple.health, loadedDisciple.health)
        assertEquals(originalDisciple.lifespan, loadedDisciple.lifespan)
    }

    @Test
    fun saveGame_shouldOverwriteExistingSave() {
        val storage = FakePlatformStorage()
        val jsonStorage = JsonGameStorage(platformStorage = storage)

        val sect1 = createTestSect()
        jsonStorage.saveGame(sect1, version = 1)

        val sect2 =
            Sect.create(
                id = SectId("test-sect-2"),
                name = "新宗门",
                resources = Resources(spiritStones = 9999),
            ).getOrThrow()
        jsonStorage.saveGame(sect2, version = 1)

        val loadedSect = jsonStorage.loadGame().getOrThrow()
        assertEquals("test-sect-2", loadedSect.id.value)
        assertEquals("新宗门", loadedSect.name)
        assertEquals(9999, loadedSect.resources.spiritStones)
    }
}
