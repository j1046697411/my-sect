package com.sect.game.domain.entity

import com.sect.game.domain.exception.SectException
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.SectId

data class Sect(
    val id: SectId,
    val name: String,
    private val _disciples: MutableMap<DiscipleId, Disciple> = mutableMapOf(),
    val resources: Resources = Resources.EMPTY,
    val maxDisciples: Int = 100,
) {
    val disciples: Map<DiscipleId, Disciple>
        get() = _disciples.toMap()

    val discipleCount: Int
        get() = _disciples.size

    fun isAtCapacity(): Boolean = _disciples.size >= maxDisciples

    fun addDisciple(disc: Disciple): Result<Unit> {
        return Result.runCatching {
            if (isAtCapacity()) {
                throw SectException.AtCapacityException(id.value, maxDisciples)
            }
            _disciples[disc.id] = disc
        }
    }

    fun removeDisciple(id: DiscipleId): Result<Disciple> {
        return Result.runCatching {
            val disciple =
                _disciples.remove(id)
                    ?: throw SectException.DiscipleNotFoundException(id.value)
            disciple
        }
    }

    fun getDisciple(id: DiscipleId): Disciple? = _disciples[id]

    fun updateDisciple(disciple: Disciple): Result<Unit> {
        return Result.runCatching {
            if (!_disciples.containsKey(disciple.id)) {
                throw SectException.DiscipleNotFoundException(disciple.id.value)
            }
            _disciples[disciple.id] = disciple
        }
    }

    fun spendResources(resourcesToSpend: Resources): Result<Sect> {
        return Result.runCatching {
            if (!this@Sect.resources.isAffordable(resourcesToSpend)) {
                throw SectException.InsufficientResourcesException(
                    resourcesToSpend.spiritStones,
                    resourcesToSpend.herbs,
                    resourcesToSpend.pills,
                    this@Sect.resources.spiritStones,
                    this@Sect.resources.herbs,
                    this@Sect.resources.pills,
                )
            }
            val newResources = this@Sect.resources - resourcesToSpend
            copy(resources = newResources)
        }
    }

    fun addResources(resourcesToAdd: Resources): Sect {
        return copy(resources = resources + resourcesToAdd)
    }

    companion object {
        fun create(
            id: SectId,
            name: String,
            maxDisciples: Int = 100,
            resources: Resources = Resources.EMPTY,
        ): Result<Sect> {
            return Result.runCatching {
                require(name.isNotBlank()) { "name must not be blank" }
                require(maxDisciples > 0) { "maxDisciples must be positive, but was $maxDisciples" }
                Sect(
                    id = id,
                    name = name,
                    maxDisciples = maxDisciples,
                    resources = resources,
                )
            }
        }
    }
}
