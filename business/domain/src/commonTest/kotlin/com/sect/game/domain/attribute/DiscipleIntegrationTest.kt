package com.sect.game.domain.attribute

import com.sect.game.domain.DiscipleAttributes
import com.sect.game.domain.attribute.modifier.FlatModifier
import com.sect.game.domain.attribute.modifier.ModifierSource
import com.sect.game.domain.attribute.modifier.PercentModifier
import com.sect.game.domain.attribute.modifier.SourceType
import com.sect.game.domain.attribute.set.AttributeSet
import com.sect.game.domain.attribute.set.ComputationContext
import com.sect.game.domain.attribute.value.IntValue
import com.sect.game.domain.attribute.value.PercentValue
import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiscipleIntegrationTest {

    private val testSource = object : ModifierSource {
        override val sourceType = SourceType.Equipment
        override val sourceId = "equip-test"
    }

    @Test
    fun discipleAttributes_spiritRoot_matchesPredefined() {
        assertEquals(PredefinedAttributes.SPIRIT_ROOT.name, DiscipleAttributes.SPIRIT_ROOT.name)
    }

    @Test
    fun discipleAttributes_talent_matchesPredefined() {
        assertEquals(PredefinedAttributes.TALENT.name, DiscipleAttributes.TALENT.name)
    }

    @Test
    fun discipleAttributes_luck_matchesPredefined() {
        assertEquals(PredefinedAttributes.LUCK.name, DiscipleAttributes.LUCK.name)
    }

    @Test
    fun attributeSet_withDiscipleAttributes_computesCorrectly() {
        val attributes = Attributes(spiritRoot = 80, talent = 60, luck = 70)
        val disciple = createTestDisciple(attributes = attributes)

        val attributeSet = AttributeSet.EMPTY
            .with(DiscipleAttributes.SPIRIT_ROOT, IntValue(disciple.attributes.spiritRoot))
            .with(DiscipleAttributes.TALENT, IntValue(disciple.attributes.talent))
            .with(DiscipleAttributes.LUCK, IntValue(disciple.attributes.luck))

        assertEquals(80, attributeSet.get(DiscipleAttributes.SPIRIT_ROOT)?.toInt())
        assertEquals(60, attributeSet.get(DiscipleAttributes.TALENT)?.toInt())
        assertEquals(70, attributeSet.get(DiscipleAttributes.LUCK)?.toInt())
    }

    @Test
    fun attributeSet_cultivationSpeed_withSpiritRootBonus() {
        val attributes = Attributes(spiritRoot = 100, talent = 50, luck = 50)
        val attributeSet = AttributeSet.EMPTY
            .with(PredefinedAttributes.SPIRIT_ROOT, IntValue(attributes.spiritRoot))
            .with(PredefinedAttributes.CULTIVATION_SPEED, IntValue(100))

        val flatModifier = FlatModifier(
            id = "cult-bonus",
            targetKey = "cultivation_speed",
            value = attributes.spiritRoot / 10,
            source = testSource,
        )

        val result = attributeSet.compute(
            PredefinedAttributes.CULTIVATION_SPEED,
            listOf(flatModifier),
            ComputationContext(),
        )

        assertEquals(110, result?.toInt())
    }

    @Test
    fun attributeSet_attackWithEquipmentModifier() {
        val baseAttack = 50
        val attributeSet = AttributeSet.EMPTY
            .with(PredefinedAttributes.ATTACK, IntValue(baseAttack))

        val equipmentModifier = FlatModifier(
            id = "weapon-bonus",
            targetKey = "attack",
            value = 100,
            source = testSource,
        )

        val result = attributeSet.compute(
            PredefinedAttributes.ATTACK,
            listOf(equipmentModifier),
            ComputationContext(),
        )

        assertEquals(150, result?.toInt())
    }

    @Test
    fun attributeSet_defenseWithPercentBonus() {
        val baseDefense = 100
        val attributeSet = AttributeSet.EMPTY
            .with(PredefinedAttributes.DEFENSE, IntValue(baseDefense))

        val buff = PercentModifier(
            id = "defense-buff",
            targetKey = "defense",
            percent = 0.5f,
            source = testSource,
        )

        val result = attributeSet.compute(
            PredefinedAttributes.DEFENSE,
            listOf(buff),
            ComputationContext(),
        )

        assertEquals(150, result?.toInt())
    }

    @Test
    fun attributeSet_critRateCalculation() {
        val baseCritRate = 100
        val attributeSet = AttributeSet.EMPTY
            .with(PredefinedAttributes.CRIT_RATE, IntValue(baseCritRate))

        val critBonus = PercentModifier(
            id = "crit-bonus",
            targetKey = "crit_rate",
            percent = 0.1f,
            source = testSource,
        )

        val result = attributeSet.compute(
            PredefinedAttributes.CRIT_RATE,
            listOf(critBonus),
            ComputationContext(),
        )

        assertEquals(110, result?.toInt())
    }

    @Test
    fun attributeSet_maxHpWithConditionalModifier() {
        val baseMaxHp = 1000
        val attributeSet = AttributeSet.EMPTY
            .with(PredefinedAttributes.MAX_HP, IntValue(baseMaxHp))

        val healthyBonus = FlatModifier(
            id = "healthy bonus",
            targetKey = "max_hp",
            value = 200,
            condition = null,
            source = testSource,
        )

        val result = attributeSet.compute(
            PredefinedAttributes.MAX_HP,
            listOf(healthyBonus),
            ComputationContext(currentHp = 800),
        )

        assertEquals(1200, result?.toInt())
    }

    @Test
    fun disciple_cultivate_updatesAttributeSet() {
        val attributes = Attributes(spiritRoot = 50, talent = 50, luck = 50)
        val disciple = createTestDisciple(attributes = attributes)

        val initialAttributeSet = createAttributeSetForDisciple(disciple)
        assertEquals(50, initialAttributeSet.get(DiscipleAttributes.SPIRIT_ROOT)?.toInt())

        val cultivated = disciple.cultivate().getOrNull()!!
        val afterCultivateAttributeSet = createAttributeSetForDisciple(cultivated)

        assertEquals(50, afterCultivateAttributeSet.get(DiscipleAttributes.SPIRIT_ROOT)?.toInt())
    }

    @Test
    fun disciple_highSpiritRoot_cultivationSpeedBonus() {
        val lowSpiritAttributes = Attributes(spiritRoot = 20, talent = 50, luck = 50)
        val highSpiritAttributes = Attributes(spiritRoot = 100, talent = 50, luck = 50)

        val lowSpiritDisciple = createTestDisciple(attributes = lowSpiritAttributes)
        val highSpiritDisciple = createTestDisciple(attributes = highSpiritAttributes)

        val lowSpiritSet = createAttributeSetForDisciple(lowSpiritDisciple)
        val highSpiritSet = createAttributeSetForDisciple(highSpiritDisciple)

        val lowCultivationSpeed = lowSpiritSet.compute(
            PredefinedAttributes.CULTIVATION_SPEED,
            listOf(
                FlatModifier(
                    id = "spirit-bonus-low",
                    targetKey = "cultivation_speed",
                    value = lowSpiritDisciple.attributes.spiritRoot / 10,
                    source = testSource,
                ),
            ),
            ComputationContext(),
        )

        val highCultivationSpeed = highSpiritSet.compute(
            PredefinedAttributes.CULTIVATION_SPEED,
            listOf(
                FlatModifier(
                    id = "spirit-bonus-high",
                    targetKey = "cultivation_speed",
                    value = highSpiritDisciple.attributes.spiritRoot / 10,
                    source = testSource,
                ),
            ),
            ComputationContext(),
        )

        assertTrue(
            highCultivationSpeed?.toInt()!! > lowCultivationSpeed?.toInt()!!,
        )
    }

    @Test
    fun disciple_rest_reducesFatigueAndRecovers() {
        val attributes = Attributes(spiritRoot = 50, talent = 50, luck = 50)
        val fatiguedDisciple = createTestDisciple(attributes = attributes, fatigue = 80, health = 30)

        val rested = fatiguedDisciple.rest().getOrNull()!!

        assertTrue(rested.fatigue < fatiguedDisciple.fatigue)
        assertTrue(rested.health >= fatiguedDisciple.health)
    }

    @Test
    fun disciple_breakthrough_withFullProgress() {
        val attributes = Attributes(spiritRoot = 80, talent = 80, luck = 80)
        val disciple = createTestDisciple(
            attributes = attributes,
            cultivationProgress = 100,
            realm = Realm.LianQi,
        )

        val nextRealm = disciple.attemptBreakthrough().getOrNull()

        assertEquals(Realm.ZhuJi, nextRealm)
    }

    @Test
    fun disciple_breakthrough_atMaxRealm_fails() {
        val attributes = Attributes(spiritRoot = 100, talent = 100, luck = 100)
        val disciple = createTestDisciple(
            attributes = attributes,
            cultivationProgress = 100,
            realm = Realm.HuaShen,
        )

        val result = disciple.attemptBreakthrough()

        assertTrue(result.isFailure)
    }

    @Test
    fun attributeSet_multipleModifiers_stackingCorrectly() {
        val baseAttack = 100
        val attributeSet = AttributeSet.EMPTY
            .with(PredefinedAttributes.ATTACK, IntValue(baseAttack))

        val weaponBonus = FlatModifier(id = "weapon", targetKey = "attack", value = 50, source = testSource)
        val enchantBonus = FlatModifier(id = "enchant", targetKey = "attack", value = 30, source = testSource)
        val percentBonus = PercentModifier(id = "percent", targetKey = "attack", percent = 0.2f, source = testSource)

        val result = attributeSet.compute(
            PredefinedAttributes.ATTACK,
            listOf(weaponBonus, enchantBonus, percentBonus),
            ComputationContext(),
        )

        assertEquals(216, result?.toInt())
    }

    @Test
    fun disciple_lifespan_decreasesOverTime() {
        val attributes = Attributes(spiritRoot = 50, talent = 50, luck = 50)
        val disciple = createTestDisciple(attributes = attributes, lifespan = 100)

        assertEquals(100, disciple.lifespan)
        assertFalse(disciple.isDead())
    }

    @Test
    fun disciple_health_affectedByLuck() {
        val lowLuckAttributes = Attributes(spiritRoot = 50, talent = 50, luck = 10)
        val highLuckAttributes = Attributes(spiritRoot = 50, talent = 50, luck = 100)

        val lowLuckDisciple = createTestDisciple(attributes = lowLuckAttributes, health = 100)
        val highLuckDisciple = createTestDisciple(attributes = highLuckAttributes, health = 100)

        lowLuckDisciple.cultivate()
        highLuckDisciple.cultivate()

        val lowHealth = lowLuckDisciple.cultivate().getOrNull()!!.health
        val highHealth = highLuckDisciple.cultivate().getOrNull()!!.health

        assertTrue(highHealth >= lowHealth)
    }

    private fun createTestDisciple(
        id: DiscipleId = DiscipleId("test-id"),
        name: String = "测试弟子",
        realm: Realm = Realm.LianQi,
        attributes: Attributes = Attributes.DEFAULT,
        cultivationProgress: Int = 0,
        fatigue: Int = 0,
        health: Int = 100,
        lifespan: Int = 100,
    ): Disciple {
        return Disciple(
            id = id,
            name = name,
            realm = realm,
            attributes = attributes,
            cultivationProgress = cultivationProgress,
            fatigue = fatigue,
            health = health,
            lifespan = lifespan,
        )
    }

    private fun createAttributeSetForDisciple(disciple: Disciple): AttributeSet {
        return AttributeSet.EMPTY
            .with(DiscipleAttributes.SPIRIT_ROOT, IntValue(disciple.attributes.spiritRoot))
            .with(DiscipleAttributes.TALENT, IntValue(disciple.attributes.talent))
            .with(DiscipleAttributes.LUCK, IntValue(disciple.attributes.luck))
            .with(PredefinedAttributes.CULTIVATION_SPEED, IntValue(100))
    }
}
