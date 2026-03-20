package com.sect.game.domain.attribute.provider

/**
 * 属性提供者类型枚举
 * 用于区分不同来源的属性提供者
 */
enum class ProviderType {
    /** 装备提供属性 */
    Equipment,

    /** 宝石提供属性 */
    Gem,

    /** 附魔提供属性 */
    Enchantment,

    /** 套装效果提供属性 */
    SetBonus,

    /** 技能提供属性 */
    Skill,

    /** Buff提供属性 */
    Buff,

    /** 建筑提供属性 */
    Building,

    /** 阵法提供属性 */
    Formation,

    /** 称号提供属性 */
    Title,

    /** 宠物提供属性 */
    Pet,
}
