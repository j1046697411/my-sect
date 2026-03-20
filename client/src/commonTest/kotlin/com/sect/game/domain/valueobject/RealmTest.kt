package com.sect.game.domain.valueobject

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class RealmTest {
    @Test
    fun next_whenLianQi_returnsZhuJi() {
        assertEquals(Realm.ZhuJi, Realm.LianQi.next())
    }

    @Test
    fun next_whenZhuJi_returnsJinDan() {
        assertEquals(Realm.JinDan, Realm.ZhuJi.next())
    }

    @Test
    fun next_whenJinDan_returnsYuanYing() {
        assertEquals(Realm.YuanYing, Realm.JinDan.next())
    }

    @Test
    fun next_whenYuanYing_returnsHuaShen() {
        assertEquals(Realm.HuaShen, Realm.YuanYing.next())
    }

    @Test
    fun next_whenHuaShen_returnsNull() {
        assertEquals(null, Realm.HuaShen.next())
    }

    @Test
    fun order_returnsCorrectOrder() {
        assertEquals(1, Realm.LianQi.order)
        assertEquals(2, Realm.ZhuJi.order)
        assertEquals(3, Realm.JinDan.order)
        assertEquals(4, Realm.YuanYing.order)
        assertEquals(5, Realm.HuaShen.order)
    }

    @Test
    fun fromOrder_withValidOrder_returnsRealm() {
        assertSame(Realm.LianQi, Realm.fromOrder(1))
        assertSame(Realm.ZhuJi, Realm.fromOrder(2))
        assertSame(Realm.JinDan, Realm.fromOrder(3))
        assertSame(Realm.YuanYing, Realm.fromOrder(4))
        assertSame(Realm.HuaShen, Realm.fromOrder(5))
    }

    @Test
    fun fromOrder_withInvalidOrder_returnsNull() {
        assertEquals(null, Realm.fromOrder(0))
        assertEquals(null, Realm.fromOrder(6))
        assertEquals(null, Realm.fromOrder(-1))
    }
}
