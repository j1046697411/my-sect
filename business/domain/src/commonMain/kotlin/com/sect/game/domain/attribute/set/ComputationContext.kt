package com.sect.game.domain.attribute.set

/**
 * 属性计算上下文
 * 用于在计算属性时提供额外的上下文信息
 *
 * @property source 属性来源（如装备、技能名称）
 * @property target 属性目标（如弟子ID）
 * @property currentHp 当前生命值
 * @property currentMp 当前灵力值
 */
data class ComputationContext(
    val source: String? = null,
    val target: String? = null,
    val currentHp: Int = 0,
    val currentMp: Int = 0,
)
