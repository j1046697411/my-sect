package com.sect.game.domain

import com.sect.game.domain.attribute.modifier.Modifier

/**
 * Buff领域模型
 * 代表游戏中的增益/减益状态
 *
 * @param id Buff唯一标识
 * @param name Buff名称
 * @param modifiers 属性修饰器列表
 * @param duration 持续时间（秒），-1表示永久
 * @param stackable 是否可叠加
 */
data class Buff(
    val id: String,
    val name: String,
    val modifiers: List<Modifier>,
    val duration: Int,
    val stackable: Boolean,
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(name.isNotBlank()) { "name must not be blank" }
        require(duration >= -1) { "duration must be non-negative or -1 for permanent, but was $duration" }
    }

    /**
     * 判断Buff是否已过期
     * @return true 如果已过期
     */
    fun isExpired(): Boolean = duration >= 0 && duration <= 0

    /**
     * 执行一次tick，减少持续时间
     * @return 新的Buff实例（不可变操作）
     */
    fun tick(): Buff {
        if (duration == -1) {
            return this
        }
        return copy(duration = (duration - 1).coerceAtLeast(0))
    }

    companion object {
        /**
         * 创建Buff实例
         * @param id Buff唯一标识
         * @param name Buff名称
         * @param modifiers 属性修饰器列表
         * @param duration 持续时间（秒），-1表示永久
         * @param stackable 是否可叠加
         * @return Buff创建结果
         */
        fun create(
            id: String,
            name: String,
            modifiers: List<Modifier> = emptyList(),
            duration: Int = -1,
            stackable: Boolean = false,
        ): Result<Buff> {
            return Result.runCatching {
                require(id.isNotBlank()) { "id must not be blank" }
                require(name.isNotBlank()) { "name must not be blank" }
                require(duration >= -1) { "duration must be non-negative or -1 for permanent, but was $duration" }

                Buff(
                    id = id,
                    name = name,
                    modifiers = modifiers,
                    duration = duration,
                    stackable = stackable,
                )
            }
        }
    }
}
