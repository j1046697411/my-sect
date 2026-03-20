package com.sect.game.data.storage

import com.sect.game.data.dto.GameSaveDto
import com.sect.game.data.mapper.SectMapper
import com.sect.game.domain.entity.Sect
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class InMemoryGameStorage(
    private val json: Json = Json { ignoreUnknownKeys = true },
) : GameStorage {
    private var savedContent: String? = null

    fun getSavedContent(): String? = savedContent

    override fun saveGame(
        sect: Sect,
        version: Int,
    ): Result<Unit> {
        return Result.runCatching {
            val sectDto = SectMapper.toDto(sect)
            val saveDto = GameSaveDto(version = version, sect = sectDto)
            savedContent = json.encodeToString(saveDto)
        }
    }

    override fun loadGame(): Result<Sect> {
        return Result.runCatching {
            val content =
                savedContent
                    ?: throw GameStorageException.GameNotFoundException()
            val saveDto = json.decodeFromString<GameSaveDto>(content)
            SectMapper.toDomain(saveDto.sect)
        }
    }

    override fun deleteGame(): Result<Unit> {
        return Result.runCatching {
            savedContent = null
        }
    }

    override fun gameExists(): Boolean {
        return savedContent != null
    }
}
