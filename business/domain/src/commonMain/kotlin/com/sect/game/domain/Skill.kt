package com.sect.game.domain

import com.sect.game.domain.attribute.modifier.Modifier

/**
 * 技能类型枚举
 */
enum class SkillType {
    Active,   // 主动技能
    Passive,  // 被动技能
    Ultimate, // 终极技能
    Support,  // 辅助技能
    ;
}

/**
 * 技能领域模型
 * 代表游戏中的技能，可提供属性修饰器
 *
 * @param id 技能唯一标识
 * @param name 技能名称
 * @param type 技能类型
 * @param modifiers 属性修饰器列表
 * @param cooldown 冷却时间（秒），0表示无冷却
 */
data class Skill(
    val id: String,
    val name: String,
    val type: SkillType,
    val modifiers: List<Modifier>,
    val cooldown: Int,
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(name.isNotBlank()) { "name must not be blank" }
        require(cooldown >= 0) { "cooldown must be non-negative, but was $cooldown" }
    }

    /**
     * 判断是否为被动技能
     * @return true 如果是被动技能
     */
    fun isPassive(): Boolean = type == SkillType.Passive

    companion object {
        /**
         * 创建技能实例
         * @param id 技能唯一标识
         * @param name 技能名称
         * @param type 技能类型
         * @param modifiers 属性修饰器列表
         * @param cooldown 冷却时间（秒）
         * @return 技能创建结果
         */
        fun create(
            id: String,
            name: String,
            type: SkillType,
            modifiers: List<Modifier> = emptyList(),
            cooldown: Int = 0,
        ): Result<Skill> {
            return Result.runCatching {
                require(id.isNotBlank()) { "id must not be blank" }
                require(name.isNotBlank()) { "name must not be blank" }
                require(cooldown >= 0) { "cooldown must be non-negative, but was $cooldown" }

                Skill(
                    id = id,
                    name = name,
                    type = type,
                    modifiers = modifiers,
                    cooldown = cooldown,
                )
            }
        }
    }
}
