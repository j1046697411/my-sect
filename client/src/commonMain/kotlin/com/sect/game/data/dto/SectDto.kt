package com.sect.game.data.dto

import com.sect.game.domain.valueobject.Realm
import kotlinx.serialization.Serializable

@Serializable
data class AttributesDto(
    val spiritRoot: Int,
    val talent: Int,
    val luck: Int
)

@Serializable
data class DiscipleDto(
    val id: String,
    val name: String,
    val realm: Int,
    val attributes: AttributesDto,
    val cultivationProgress: Int,
    val fatigue: Int,
    val health: Int,
    val lifespan: Int
)

@Serializable
data class ResourcesDto(
    val spiritStones: Int,
    val herbs: Int,
    val pills: Int
)

@Serializable
data class SectDto(
    val id: String,
    val name: String,
    val disciples: List<DiscipleDto>,
    val resources: ResourcesDto,
    val maxDisciples: Int
)

@Serializable
data class GameSaveDto(
    val version: Int,
    val sect: SectDto,
    val savedAt: Long = System.currentTimeMillis()
)
