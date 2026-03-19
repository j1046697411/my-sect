package com.sect.game.data.storage

import com.sect.game.data.dto.GameSaveDto
import com.sect.game.data.mapper.SectMapper
import com.sect.game.domain.entity.Sect
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

expect fun createPlatformStorage(): PlatformGameStorage

interface PlatformGameStorage {
    fun writeToFile(content: String)
    fun readFromFile(): String?
    fun deleteFile()
    fun fileExists(): Boolean
}

class JsonGameStorage(
    private val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
    private val platformStorage: PlatformGameStorage = createPlatformStorage()
) : GameStorage {

    override fun saveGame(sect: Sect, version: Int): Result<Unit> {
        return Result.runCatching {
            val sectDto = SectMapper.toDto(sect)
            val saveDto = GameSaveDto(version = version, sect = sectDto)
            val jsonString = json.encodeToString(saveDto)
            platformStorage.writeToFile(jsonString)
        }
    }

    override fun loadGame(): Result<Sect> {
        return Result.runCatching {
            val jsonString = platformStorage.readFromFile()
                ?: throw GameStorageException.GameNotFoundException()
            val saveDto = json.decodeFromString<GameSaveDto>(jsonString)
            SectMapper.toDomain(saveDto.sect)
        }
    }

    override fun deleteGame(): Result<Unit> {
        return Result.runCatching {
            platformStorage.deleteFile()
        }
    }

    override fun gameExists(): Boolean {
        return platformStorage.fileExists()
    }
}

sealed class GameStorageException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class GameNotFoundException : GameStorageException("Game save not found")
    class CorruptedSaveException(cause: Throwable) : GameStorageException("Save file is corrupted", cause)
    class SaveWriteException(cause: Throwable) : GameStorageException("Failed to write save file", cause)
    class SaveReadException(cause: Throwable) : GameStorageException("Failed to read save file", cause)
    class SaveDeleteException(cause: Throwable) : GameStorageException("Failed to delete save file", cause)
}
