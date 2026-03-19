package com.sect.game.domain.valueobject

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class RealmTest {
    @Test
    fun next_whenLianQi_returns筑基() {
        assertEquals(Realm.筑基, Realm.炼气.next())
    }

    @Test
    fun next_when筑基_returns金丹() {
        assertEquals(Realm.金丹, Realm.筑基.next())
    }

    @Test
    fun next_when金丹_returns元婴() {
        assertEquals(Realm.元婴, Realm.金丹.next())
    }

    @Test
    fun next_when元婴_returns化神() {
        assertEquals(Realm.化神, Realm.元婴.next())
    }

    @Test
    fun next_when化神_wrapsTo炼气() {
        assertEquals(Realm.炼气, Realm.化神.next())
    }

    @Test
    fun order_returnsCorrectOrder() {
        assertEquals(1, Realm.炼气.order)
        assertEquals(2, Realm.筑基.order)
        assertEquals(3, Realm.金丹.order)
        assertEquals(4, Realm.元婴.order)
        assertEquals(5, Realm.化神.order)
    }

    @Test
    fun fromOrder_withValidOrder_returnsRealm() {
        assertSame(Realm.炼气, Realm.fromOrder(1))
        assertSame(Realm.筑基, Realm.fromOrder(2))
        assertSame(Realm.金丹, Realm.fromOrder(3))
        assertSame(Realm.元婴, Realm.fromOrder(4))
        assertSame(Realm.化神, Realm.fromOrder(5))
    }

    @Test
    fun fromOrder_withInvalidOrder_returnsNull() {
        assertEquals(null, Realm.fromOrder(0))
        assertEquals(null, Realm.fromOrder(6))
        assertEquals(null, Realm.fromOrder(-1))
    }
}
