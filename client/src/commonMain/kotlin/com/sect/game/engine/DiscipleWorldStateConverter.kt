package com.sect.game.engine

import com.sect.game.domain.entity.Disciple
import com.sect.game.goap.core.WorldState

object DiscipleWorldStateConverter {
    /**
     * 将弟子状态转换为 GOAP 世界状态
     * readiness 表示突破准备度，由疲劳度和健康度计算得出（0-100）
     * 疲劳度越低、健康度越高，则准备度越高
     */
    fun toWorldState(disciple: Disciple): WorldState {
        val readiness = calculateReadiness(disciple.fatigue, disciple.health)
        return WorldState()
            .withValue("health", disciple.health)
            .withValue("fatigue", disciple.fatigue)
            .withValue("cultivationProgress", disciple.cultivationProgress)
            .withValue("realm", disciple.realm.order)
            .withValue("lifespan", disciple.lifespan)
            .withValue("readiness", readiness)
    }

    /**
     * 计算突破准备度
     * 公式: (100 - fatigue) * 0.6 + health * 0.4，限制在 0-100 范围
     */
    private fun calculateReadiness(
        fatigue: Int,
        health: Int,
    ): Int {
        val baseReadiness = (100 - fatigue) * 0.6 + health * 0.4
        return baseReadiness.toInt().coerceIn(0, 100)
    }
}
