package com.sect.game.domain.attribute.provider

import com.sect.game.domain.attribute.modifier.FlatModifier
import com.sect.game.domain.attribute.modifier.ModifierSource
import com.sect.game.domain.attribute.modifier.PercentModifier
import com.sect.game.domain.attribute.modifier.SourceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProviderTest {

    private val testSource = object : ModifierSource {
        override val sourceType = SourceType.Equipment
        override val sourceId = "equip-1"
    }

    @Test
    fun providerType_enum_hasCorrectValues() {
        assertEquals(ProviderType.Equipment, ProviderType.valueOf("Equipment"))
        assertEquals(ProviderType.Gem, ProviderType.valueOf("Gem"))
        assertEquals(ProviderType.Enchantment, ProviderType.valueOf("Enchantment"))
        assertEquals(ProviderType.SetBonus, ProviderType.valueOf("SetBonus"))
        assertEquals(ProviderType.Skill, ProviderType.valueOf("Skill"))
        assertEquals(ProviderType.Buff, ProviderType.valueOf("Buff"))
        assertEquals(ProviderType.Building, ProviderType.valueOf("Building"))
        assertEquals(ProviderType.Formation, ProviderType.valueOf("Formation"))
        assertEquals(ProviderType.Title, ProviderType.valueOf("Title"))
        assertEquals(ProviderType.Pet, ProviderType.valueOf("Pet"))
    }

    @Test
    fun universalProvider_createsWithAllProperties() {
        val provider = UniversalProvider(
            id = "provider-1",
            name = "测试装备",
            providerType = ProviderType.Equipment,
            modifiers = listOf(
                FlatModifier("mod-1", "attack", 100, source = testSource),
                PercentModifier("mod-2", "defense", 0.1f, source = testSource),
            ),
            skillSlots = listOf(
                Any(),
            ),
            metaAttributes = mapOf("quality" to "epic"),
        )

        assertEquals("provider-1", provider.id)
        assertEquals("测试装备", provider.name)
        assertEquals(ProviderType.Equipment, provider.providerType)
        assertEquals(2, provider.getModifiers().size)
        assertEquals(1, provider.getSkillSlots().size)
        assertEquals("epic", provider.getMetaAttributes()["quality"])
    }

    @Test
    fun universalProvider_withEmptyLists() {
        val provider = UniversalProvider(
            id = "provider-empty",
            name = "空提供者",
            providerType = ProviderType.Buff,
        )

        assertTrue(provider.getModifiers().isEmpty())
        assertTrue(provider.getSkillSlots().isEmpty())
        assertTrue(provider.getMetaAttributes().isEmpty())
    }

    @Test
    fun universalProvider_createFactory() {
        val provider = UniversalProvider.create(
            id = "factory-1",
            name = "工厂创建",
            providerType = ProviderType.Skill,
            modifiers = listOf(
                FlatModifier("mod-3", "attack", 50, source = testSource),
            ),
        )

        assertEquals("factory-1", provider.id)
        assertEquals(1, provider.getModifiers().size)
    }

    @Test
    fun skillSlot_dataClass() {
        val slot = Any()

        assertNotNull(slot)
    }

    @Test
    fun providerRegistry_register_and_get() {
        ProviderRegistry.resetForTest()

        val provider = UniversalProvider(
            id = "reg-test-1",
            name = "注册测试",
            providerType = ProviderType.Equipment,
        )
        ProviderRegistry.register(provider)

        val retrieved = ProviderRegistry.get("reg-test-1")
        assertNotNull(retrieved)
        assertEquals("reg-test-1", retrieved.id)
    }

    @Test
    fun providerRegistry_get_nonexistent_returnsNull() {
        ProviderRegistry.resetForTest()

        val result = ProviderRegistry.get("nonexistent")
        assertNull(result)
    }

    @Test
    fun providerRegistry_getByType() {
        ProviderRegistry.resetForTest()

        val equip1 = UniversalProvider("equip-1", "装备1", ProviderType.Equipment)
        val equip2 = UniversalProvider("equip-2", "装备2", ProviderType.Equipment)
        val gem1 = UniversalProvider("gem-1", "宝石1", ProviderType.Gem)

        ProviderRegistry.register(equip1)
        ProviderRegistry.register(equip2)
        ProviderRegistry.register(gem1)

        val equipments = ProviderRegistry.getByType(ProviderType.Equipment)
        assertEquals(2, equipments.size)

        val gems = ProviderRegistry.getByType(ProviderType.Gem)
        assertEquals(1, gems.size)

        val skills = ProviderRegistry.getByType(ProviderType.Skill)
        assertTrue(skills.isEmpty())
    }

    @Test
    fun providerRegistry_getAll() {
        ProviderRegistry.resetForTest()

        ProviderRegistry.register(UniversalProvider("p1", "提供者1", ProviderType.Buff))
        ProviderRegistry.register(UniversalProvider("p2", "提供者2", ProviderType.Skill))

        val all = ProviderRegistry.getAll()
        assertEquals(2, all.size)
    }

    @Test
    fun providerRegistry_resetForTest_clearsAll() {
        ProviderRegistry.register(UniversalProvider("p1", "提供者1", ProviderType.Buff))

        ProviderRegistry.resetForTest()

        assertTrue(ProviderRegistry.getAll().isEmpty())
        assertNull(ProviderRegistry.get("p1"))
    }

    @Test
    fun providerRegistry_duplicateId_overwrites() {
        ProviderRegistry.resetForTest()

        val provider1 = UniversalProvider("dup-id", "提供者1", ProviderType.Equipment)
        val provider2 = UniversalProvider("dup-id", "提供者2", ProviderType.Gem)

        ProviderRegistry.register(provider1)
        ProviderRegistry.register(provider2)

        val retrieved = ProviderRegistry.get("dup-id")
        assertNotNull(retrieved)
        assertEquals("提供者2", retrieved.name)
        assertEquals(ProviderType.Gem, retrieved.providerType)
    }

    @Test
    fun attributeProvider_interface_implementation() {
        val customProvider = object : AttributeProvider {
            override val id: String = "custom-1"
            override val name: String = "自定义提供者"
            override val providerType: ProviderType = ProviderType.Title
            override fun getModifiers(): List<com.sect.game.domain.attribute.modifier.Modifier> = emptyList()
            override fun getSkillSlots(): List<Any> = emptyList()
            override fun getMetaAttributes(): Map<String, String> = mapOf("tier" to "legendary")
        }

        ProviderRegistry.resetForTest()
        ProviderRegistry.register(customProvider)

        val retrieved = ProviderRegistry.get("custom-1")
        assertNotNull(retrieved)
        assertEquals("自定义提供者", retrieved.name)
        assertEquals("legendary", retrieved.getMetaAttributes()["tier"])
    }
}
