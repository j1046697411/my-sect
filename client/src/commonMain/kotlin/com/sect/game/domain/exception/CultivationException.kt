package com.sect.game.domain.exception

sealed class CultivationException(
    message: String,
    userMessage: String,
) : DomainExceptionBase(message, userMessage) {
    class DeadDiscipleException(discipleId: String) :
        CultivationException(
            message = "Disciple $discipleId is dead",
            userMessage = "该弟子已经死亡，无法执行操作",
        )

    class ExhaustedException(discipleId: String, fatigue: Int) :
        CultivationException(
            message = "Disciple $discipleId is exhausted with fatigue $fatigue",
            userMessage = "该弟子疲劳过度，无法继续修炼",
        )

    class BreakthroughFailedException(discipleId: String, currentRealm: String, progress: Int) :
        CultivationException(
            message = "Breakthrough failed for disciple $discipleId in realm $currentRealm with progress $progress",
            userMessage = "突破失败，当前境界：$currentRealm，修炼进度：$progress",
        )

    class InsufficientProgressException(discipleId: String, progress: Int, required: Int = 100) :
        CultivationException(
            message = "Insufficient progress for breakthrough: disciple $discipleId has $progress, needs $required",
            userMessage = "修为不足，无法突破。当前进度：$progress，需要：$required",
        )

    class HealthDepletedException(discipleId: String, health: Int) :
        CultivationException(
            message = "Disciple $discipleId health depleted: $health",
            userMessage = "生命值耗尽，无法继续修炼",
        )

    class MaxRealmReachedException(discipleId: String, realm: String) :
        CultivationException(
            message = "Disciple $discipleId already reached max realm: $realm",
            userMessage = "已达最大境界：$realm，无法继续突破",
        )
}
