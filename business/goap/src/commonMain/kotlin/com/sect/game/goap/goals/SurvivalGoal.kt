package com.sect.game.goap.goals

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.WorldState

object SurvivalGoal {
    const val ID = "survival"
    const val PRIORITY = 100

    val targetConditions: Set<Condition> =
        setOf(
            Condition.greaterThanOrEqual("health", 80),
        )

    private val template = GoalTemplate(
        id = ID,
        name = "生存",
        priority = PRIORITY,
        conditions = targetConditions,
        targetState = WorldState().withValue("health", 80),
    )

    fun isSatisfied(state: WorldState): Boolean {
        return (state.getValue("health") ?: 0) >= 80
    }

    fun create(): SimpleGoal = template.toGoal(::isSatisfied)
}
