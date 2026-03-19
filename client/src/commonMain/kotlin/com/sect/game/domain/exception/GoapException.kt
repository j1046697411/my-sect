package com.sect.game.domain.exception

sealed class GoapException(
    message: String,
    userMessage: String
) : DomainExceptionBase(message, userMessage) {

    class PlanningFailedException(goal: String, reason: String = "") :
        GoapException(
            message = "GOAP planning failed for goal: $goal. Reason: $reason",
            userMessage = "无法制定行动计划：$goal${if (reason.isNotEmpty()) "（$reason）" else ""}"
        )

    class NoValidActionsException(goal: String) :
        GoapException(
            message = "No valid actions available for goal: $goal",
            userMessage = "没有可行的行动方案来达成目标：$goal"
        )

    class ActionExecutionFailedException(action: String, cause: String = "") :
        GoapException(
            message = "Action execution failed: $action. Cause: $cause",
            userMessage = "行动执行失败：$action${if (cause.isNotEmpty()) "（$cause）" else ""}"
        )
}
