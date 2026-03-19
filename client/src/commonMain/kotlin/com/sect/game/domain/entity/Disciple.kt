package com.sect.game.domain.entity

import com.sect.game.domain.exception.CultivationException
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm

data class Disciple(
    val id: DiscipleId,
    val name: String,
    val realm: Realm,
    val attributes: Attributes,
    val cultivationProgress: Int,
    val fatigue: Int,
    val health: Int,
    val lifespan: Int
) {
    init {
        require(cultivationProgress in 0..100) {
            "cultivationProgress must be in 0..100, but was $cultivationProgress"
        }
        require(fatigue in 0..100) {
            "fatigue must be in 0..100, but was $fatigue"
        }
        require(health in 0..100) {
            "health must be in 0..100, but was $health"
        }
        require(lifespan >= 0) {
            "lifespan must be non-negative, but was $lifespan"
        }
    }

    fun isExhausted(): Boolean = fatigue >= 100

    fun isDead(): Boolean = health <= 0 || lifespan <= 0

    fun isHealthy(): Boolean = health >= 50

    fun cultivate(): Result<Disciple> {
        return Result.runCatching {
            when {
                isDead() -> throw CultivationException.DeadDiscipleException(id.value)
                isExhausted() -> throw CultivationException.ExhaustedException(id.value, fatigue)
            }
            
            val progressGain = calculateCultivationGain()
            val fatigueGain = calculateFatigueGain()
            val healthLoss = calculateHealthLoss()
            
            copy(
                cultivationProgress = (cultivationProgress + progressGain).coerceAtMost(100),
                fatigue = (fatigue + fatigueGain).coerceAtMost(100),
                health = (health - healthLoss).coerceAtLeast(0)
            )
        }
    }

    fun rest(): Result<Disciple> {
        return Result.runCatching {
            if (isDead()) {
                throw CultivationException.DeadDiscipleException(id.value)
            }
            
            val fatigueReduction = 30
            val healthGain = 20
            
            copy(
                fatigue = (fatigue - fatigueReduction).coerceAtLeast(0),
                health = (health + healthGain).coerceAtMost(100)
            )
        }
    }

    fun attemptBreakthrough(): Result<Realm> {
        return Result.runCatching {
            when {
                isDead() -> throw CultivationException.DeadDiscipleException(id.value)
                cultivationProgress < 100 -> throw CultivationException.InsufficientProgressException(
                    id.value, cultivationProgress
                )
                realm == Realm.化神 -> throw CultivationException.MaxRealmReachedException(id.value, realm.name)
            }
            
            realm.next()
        }
    }

    private fun calculateCultivationGain(): Int {
        val baseGain = 10
        val spiritBonus = attributes.spiritRoot / 10
        val talentBonus = attributes.talent / 20
        return baseGain + spiritBonus + talentBonus
    }

    private fun calculateFatigueGain(): Int {
        val baseFatigue = 15
        val talentPenalty = attributes.talent / 25
        return (baseFatigue - talentPenalty).coerceAtLeast(5)
    }

    private fun calculateHealthLoss(): Int {
        val baseLoss = 5
        val luckMitigation = attributes.luck / 50
        return (baseLoss - luckMitigation).coerceAtLeast(1)
    }

    companion object {
        fun create(
            id: DiscipleId,
            name: String,
            attributes: Attributes,
            realm: Realm = Realm.炼气,
            lifespan: Int = 100
        ): Result<Disciple> {
            return Result.runCatching {
                require(name.isNotBlank()) { "name must not be blank" }
                require(lifespan > 0) { "lifespan must be positive, but was $lifespan" }
                
                Disciple(
                    id = id,
                    name = name,
                    realm = realm,
                    attributes = attributes,
                    cultivationProgress = 0,
                    fatigue = 0,
                    health = 100,
                    lifespan = lifespan
                )
            }
        }
    }
}
