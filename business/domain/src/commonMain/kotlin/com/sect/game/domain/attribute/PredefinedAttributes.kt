package com.sect.game.domain.attribute

import com.sect.game.domain.attribute.key.AttributeKey
import com.sect.game.domain.attribute.key.AttributeMeta
import com.sect.game.domain.attribute.key.AttributeTag
import com.sect.game.domain.attribute.value.AttributeValue
import com.sect.game.domain.attribute.value.IntValue
import com.sect.game.domain.attribute.value.PercentValue

/**
 * 预定义属性键常量
 * 所有游戏属性应在此对象中定义，避免使用字符串字面量
 */
object PredefinedAttributes {

    // ===== 战斗属性 =====

    /** 攻击力 */
    val ATTACK: AttributeKey<IntValue> = AttributeKey("attack")

    /** 防御力 */
    val DEFENSE: AttributeKey<IntValue> = AttributeKey("defense")

    /** 速度 */
    val SPEED: AttributeKey<IntValue> = AttributeKey("speed")

    /** 暴击率 */
    val CRIT_RATE: AttributeKey<PercentValue> = AttributeKey("crit_rate")

    /** 暴击伤害 */
    val CRIT_DAMAGE: AttributeKey<PercentValue> = AttributeKey("crit_damage")

    // ===== 生命属性 =====

    /** 生命值 */
    val MAX_HP: AttributeKey<IntValue> = AttributeKey("max_hp")

    /** 生命恢复 */
    val HP_REGEN: AttributeKey<IntValue> = AttributeKey("hp_regen")

    /** 生命汲取 */
    val LIFE_STEAL: AttributeKey<PercentValue> = AttributeKey("life_steal")

    // ===== 状态属性 =====

    /** 闪避率 */
    val DODGE_RATE: AttributeKey<PercentValue> = AttributeKey("dodge_rate")

    /** 韧性 */
    val TENACITY: AttributeKey<PercentValue> = AttributeKey("tenacity")

    /** 抗性 */
    val RESISTANCE: AttributeKey<PercentValue> = AttributeKey("resistance")

    // ===== 天赋属性 =====

    /** 灵根 */
    val SPIRIT_ROOT: AttributeKey<IntValue> = AttributeKey("spirit_root")

    /** 资质 */
    val TALENT: AttributeKey<IntValue> = AttributeKey("talent")

    /** 气运 */
    val LUCK: AttributeKey<IntValue> = AttributeKey("luck")

    /** 悟性 */
    val COMPREHENSION: AttributeKey<IntValue> = AttributeKey("comprehension")

    /** 修炼速度 */
    val CULTIVATION_SPEED: AttributeKey<PercentValue> = AttributeKey("cultivation_speed")

    // ===== 属性元信息映射 =====

    /**
     * 属性元信息映射表
     * 可通过 [getMeta] 方法获取属性的元信息
     */
    val METAS: Map<AttributeKey<*>, AttributeMeta<*>> = mapOf(
        ATTACK to AttributeMeta.int(
            defaultValue = 10,
            range = 0..9999,
            tag = AttributeTag.Combat,
            description = "攻击力",
        ),
        DEFENSE to AttributeMeta.int(
            defaultValue = 10,
            range = 0..9999,
            tag = AttributeTag.Combat,
            description = "防御力",
        ),
        SPEED to AttributeMeta.int(
            defaultValue = 10,
            range = 0..9999,
            tag = AttributeTag.Combat,
            description = "速度",
        ),
        CRIT_RATE to AttributeMeta.int(
            defaultValue = 5,
            range = 0..100,
            tag = AttributeTag.Combat,
            description = "暴击率",
        ),
        CRIT_DAMAGE to AttributeMeta.int(
            defaultValue = 150,
            range = 0..500,
            tag = AttributeTag.Combat,
            description = "暴击伤害",
        ),
        MAX_HP to AttributeMeta.int(
            defaultValue = 100,
            range = 1..999999,
            tag = AttributeTag.Life,
            description = "最大生命值",
        ),
        HP_REGEN to AttributeMeta.int(
            defaultValue = 0,
            range = 0..9999,
            tag = AttributeTag.Life,
            description = "生命恢复",
        ),
        LIFE_STEAL to AttributeMeta.int(
            defaultValue = 0,
            range = 0..100,
            tag = AttributeTag.Life,
            description = "生命汲取",
        ),
        DODGE_RATE to AttributeMeta.int(
            defaultValue = 0,
            range = 0..100,
            tag = AttributeTag.Status,
            description = "闪避率",
        ),
        TENACITY to AttributeMeta.int(
            defaultValue = 0,
            range = 0..100,
            tag = AttributeTag.Status,
            description = "韧性",
        ),
        RESISTANCE to AttributeMeta.int(
            defaultValue = 0,
            range = 0..100,
            tag = AttributeTag.Status,
            description = "抗性",
        ),
        SPIRIT_ROOT to AttributeMeta.int(
            defaultValue = 50,
            range = 1..100,
            tag = AttributeTag.Talent,
            description = "灵根",
        ),
        TALENT to AttributeMeta.int(
            defaultValue = 50,
            range = 1..100,
            tag = AttributeTag.Talent,
            description = "资质",
        ),
        LUCK to AttributeMeta.int(
            defaultValue = 50,
            range = 1..100,
            tag = AttributeTag.Talent,
            description = "气运",
        ),
        COMPREHENSION to AttributeMeta.int(
            defaultValue = 50,
            range = 1..100,
            tag = AttributeTag.Talent,
            description = "悟性",
        ),
        CULTIVATION_SPEED to AttributeMeta.int(
            defaultValue = 100,
            range = 1..500,
            tag = AttributeTag.Talent,
            description = "修炼速度",
        ),
    )

    /**
     * 获取属性的元信息
     *
     * @param key 属性键
     * @return 属性元信息，如果不存在则返回 null
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : AttributeValue> getMeta(key: AttributeKey<T>): AttributeMeta<T>? {
        return METAS[key] as? AttributeMeta<T>
    }
}
