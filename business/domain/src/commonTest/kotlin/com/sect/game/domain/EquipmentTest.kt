package com.sect.game.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EquipmentTest {
    @Test
    fun create_withValidInput_returnsSuccess() {
        val result = Equipment.create(
            id = "equip-001",
            name = "青云剑",
            slot = EquipmentSlot.Weapon,
            rarity = Rarity.Rare,
        )

        assertTrue(result.isSuccess)
        val equipment = result.getOrThrow()
        assertEquals("equip-001", equipment.id)
        assertEquals("青云剑", equipment.name)
        assertEquals(EquipmentSlot.Weapon, equipment.slot)
        assertEquals(Rarity.Rare, equipment.rarity)
        assertTrue(equipment.modifiers.isEmpty())
    }

    @Test
    fun create_withBlankId_returnsFailure() {
        val result = Equipment.create(
            id = "",
            name = "青云剑",
            slot = EquipmentSlot.Weapon,
            rarity = Rarity.Rare,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun create_withBlankName_returnsFailure() {
        val result = Equipment.create(
            id = "equip-001",
            name = "   ",
            slot = EquipmentSlot.Weapon,
            rarity = Rarity.Rare,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun create_withAllRarities_succeeds() {
        Rarity.entries.forEach { rarity ->
            val result = Equipment.create(
                id = "equip-${rarity.name}",
                name = "测试装备",
                slot = EquipmentSlot.Armor,
                rarity = rarity,
            )
            assertTrue(result.isSuccess, "Failed for rarity: ${rarity.name}")
        }
    }

    @Test
    fun create_withAllSlots_succeeds() {
        EquipmentSlot.entries.forEach { slot ->
            val result = Equipment.create(
                id = "equip-${slot.name}",
                name = "测试装备",
                slot = slot,
                rarity = Rarity.Common,
            )
            assertTrue(result.isSuccess, "Failed for slot: ${slot.name}")
        }
    }

    @Test
    fun Rarity_fromOrder_returnsCorrectRarity() {
        assertEquals(Rarity.Common, Rarity.fromOrder(1))
        assertEquals(Rarity.Uncommon, Rarity.fromOrder(2))
        assertEquals(Rarity.Rare, Rarity.fromOrder(3))
        assertEquals(Rarity.Epic, Rarity.fromOrder(4))
        assertEquals(Rarity.Legendary, Rarity.fromOrder(5))
    }

    @Test
    fun Rarity_fromOrder_withInvalidOrder_returnsNull() {
        assertEquals(null, Rarity.fromOrder(0))
        assertEquals(null, Rarity.fromOrder(6))
        assertEquals(null, Rarity.fromOrder(-1))
    }

    @Test
    fun EquipmentSlot_fromName_returnsCorrectSlot() {
        assertEquals(EquipmentSlot.Weapon, EquipmentSlot.fromName("Weapon"))
        assertEquals(EquipmentSlot.Helmet, EquipmentSlot.fromName("Helmet"))
        assertEquals(EquipmentSlot.Armor, EquipmentSlot.fromName("Armor"))
    }

    @Test
    fun EquipmentSlot_fromName_withInvalidName_returnsNull() {
        assertEquals(null, EquipmentSlot.fromName("InvalidSlot"))
        assertEquals(null, EquipmentSlot.fromName(""))
    }

    @Test
    fun dataClass_equals_withSameValues_returnsTrue() {
        val equip1 = Equipment.create(
            id = "equip-001",
            name = "青云剑",
            slot = EquipmentSlot.Weapon,
            rarity = Rarity.Rare,
        ).getOrThrow()

        val equip2 = Equipment.create(
            id = "equip-001",
            name = "青云剑",
            slot = EquipmentSlot.Weapon,
            rarity = Rarity.Rare,
        ).getOrThrow()

        assertEquals(equip1, equip2)
    }

    @Test
    fun dataClass_equals_withDifferentValues_returnsFalse() {
        val equip1 = Equipment.create(
            id = "equip-001",
            name = "青云剑",
            slot = EquipmentSlot.Weapon,
            rarity = Rarity.Rare,
        ).getOrThrow()

        val equip2 = Equipment.create(
            id = "equip-002",
            name = "青云剑",
            slot = EquipmentSlot.Weapon,
            rarity = Rarity.Rare,
        ).getOrThrow()

        assertFalse(equip1 == equip2)
    }
}
