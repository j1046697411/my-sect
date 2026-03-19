package com.sect.game.domain.valueobject

/**
 * 弟子属性值对象
 * 包含灵根、资质、气运三个属性，每个属性取值范围为1-100
 */
data class Attributes(
    val spiritRoot: Int,
    val talent: Int,
    val luck: Int
) {
    init {
        require(spiritRoot in 1..100) {
            "spiritRoot must be between 1 and 100, but was $spiritRoot"
        }
        require(talent in 1..100) {
            "talent must be between 1 and 100, but was $talent"
        }
        require(luck in 1..100) {
            "luck must be between 1 and 100, but was $luck"
        }
    }

    companion object {
        val DEFAULT = Attributes(spiritRoot = 50, talent = 50, luck = 50)
    }
}
