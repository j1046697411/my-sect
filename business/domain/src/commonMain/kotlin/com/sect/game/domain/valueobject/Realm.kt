package com.sect.game.domain.valueobject

/**
 * 修仙境界枚举
 * Representing the cultivation realms in the game.
 */
enum class Realm(val order: Int) {
    炼气(1),
    筑基(2),
    金丹(3),
    元婴(4),
    化神(5);

    /**
     * Returns the next realm in the cultivation path.
     * Returns null if already at maximum realm (化神).
     */
    fun next(): Realm? {
        if (this == 化神) return null
        val nextOrder = order + 1
        return entries.find { it.order == nextOrder }
    }

    companion object {
        fun fromOrder(order: Int): Realm? {
            return entries.find { it.order == order }
        }
    }
}
