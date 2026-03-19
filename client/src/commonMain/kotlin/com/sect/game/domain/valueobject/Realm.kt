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
     * Wraps around to the first realm after the last.
     */
    fun next(): Realm {
        val nextOrder = (order % 5) + 1
        return entries.first { it.order == nextOrder }
    }

    companion object {
        fun fromOrder(order: Int): Realm? {
            return entries.find { it.order == order }
        }
    }
}
