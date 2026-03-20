package com.sect.game.domain.entity

/**
 * 宗门资源值对象
 * 包含灵石、药材、丹药等修炼资源
 */
data class Resources(
    val spiritStones: Int = 0,
    val herbs: Int = 0,
    val pills: Int = 0,
) {
    init {
        require(spiritStones >= 0) { "spiritStones must be non-negative, but was $spiritStones" }
        require(herbs >= 0) { "herbs must be non-negative, but was $herbs" }
        require(pills >= 0) { "pills must be non-negative, but was $pills" }
    }

    operator fun minus(other: Resources): Resources {
        return Resources(
            spiritStones = this.spiritStones - other.spiritStones,
            herbs = this.herbs - other.herbs,
            pills = this.pills - other.pills,
        )
    }

    operator fun plus(other: Resources): Resources {
        return Resources(
            spiritStones = this.spiritStones + other.spiritStones,
            herbs = this.herbs + other.herbs,
            pills = this.pills + other.pills,
        )
    }

    fun isAffordable(other: Resources): Boolean {
        return this.spiritStones >= other.spiritStones &&
            this.herbs >= other.herbs &&
            this.pills >= other.pills
    }

    companion object {
        val EMPTY = Resources(0, 0, 0)
    }
}
