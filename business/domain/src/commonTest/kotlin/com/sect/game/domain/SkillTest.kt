package com.sect.game.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SkillTest {
    @Test
    fun create_withValidInput_returnsSuccess() {
        val result = Skill.create(
            id = "skill-001",
            name = "火球术",
            type = SkillType.Active,
            cooldown = 10,
        )

        assertTrue(result.isSuccess)
        val skill = result.getOrThrow()
        assertEquals("skill-001", skill.id)
        assertEquals("火球术", skill.name)
        assertEquals(SkillType.Active, skill.type)
        assertEquals(10, skill.cooldown)
        assertTrue(skill.modifiers.isEmpty())
    }

    @Test
    fun create_withBlankId_returnsFailure() {
        val result = Skill.create(
            id = "",
            name = "火球术",
            type = SkillType.Active,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun create_withBlankName_returnsFailure() {
        val result = Skill.create(
            id = "skill-001",
            name = "   ",
            type = SkillType.Active,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun create_withNegativeCooldown_returnsFailure() {
        val result = Skill.create(
            id = "skill-001",
            name = "火球术",
            type = SkillType.Active,
            cooldown = -1,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun create_withZeroCooldown_succeeds() {
        val result = Skill.create(
            id = "skill-001",
            name = "被动技能",
            type = SkillType.Passive,
            cooldown = 0,
        )

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.cooldown)
    }

    @Test
    fun isPassive_withPassiveSkill_returnsTrue() {
        val skill = Skill.create(
            id = "skill-001",
            name = "被动技能",
            type = SkillType.Passive,
        ).getOrThrow()

        assertTrue(skill.isPassive())
    }

    @Test
    fun isPassive_withActiveSkill_returnsFalse() {
        val skill = Skill.create(
            id = "skill-001",
            name = "主动技能",
            type = SkillType.Active,
        ).getOrThrow()

        assertFalse(skill.isPassive())
    }

    @Test
    fun isPassive_withUltimateSkill_returnsFalse() {
        val skill = Skill.create(
            id = "skill-001",
            name = "终极技能",
            type = SkillType.Ultimate,
        ).getOrThrow()

        assertFalse(skill.isPassive())
    }

    @Test
    fun isPassive_withSupportSkill_returnsFalse() {
        val skill = Skill.create(
            id = "skill-001",
            name = "辅助技能",
            type = SkillType.Support,
        ).getOrThrow()

        assertFalse(skill.isPassive())
    }

    @Test
    fun create_withAllSkillTypes_succeeds() {
        SkillType.entries.forEach { type ->
            val result = Skill.create(
                id = "skill-${type.name}",
                name = "测试技能",
                type = type,
            )
            assertTrue(result.isSuccess, "Failed for type: ${type.name}")
        }
    }

    @Test
    fun dataClass_equals_withSameValues_returnsTrue() {
        val skill1 = Skill.create(
            id = "skill-001",
            name = "火球术",
            type = SkillType.Active,
            cooldown = 10,
        ).getOrThrow()

        val skill2 = Skill.create(
            id = "skill-001",
            name = "火球术",
            type = SkillType.Active,
            cooldown = 10,
        ).getOrThrow()

        assertEquals(skill1, skill2)
    }

    @Test
    fun dataClass_equals_withDifferentValues_returnsFalse() {
        val skill1 = Skill.create(
            id = "skill-001",
            name = "火球术",
            type = SkillType.Active,
            cooldown = 10,
        ).getOrThrow()

        val skill2 = Skill.create(
            id = "skill-002",
            name = "火球术",
            type = SkillType.Active,
            cooldown = 10,
        ).getOrThrow()

        assertFalse(skill1 == skill2)
    }
}
