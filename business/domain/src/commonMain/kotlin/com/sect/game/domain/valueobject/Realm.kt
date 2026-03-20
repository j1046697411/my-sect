package com.sect.game.domain.valueobject

enum class Realm(val order: Int) {
    LianQi(1),
    ZhuJi(2),
    JinDan(3),
    YuanYing(4),
    HuaShen(5),
    ;

    fun next(): Realm? {
        if (this == HuaShen) return null
        val nextOrder = order + 1
        return entries.find { it.order == nextOrder }
    }

    companion object {
        fun fromOrder(order: Int): Realm? {
            return entries.find { it.order == order }
        }
    }
}
