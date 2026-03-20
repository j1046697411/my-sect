package com.sect.game.goap.goals

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.WorldState

object SurvivalGoal {
    const val ID = "survival"
    const val PRIORITY = 100

    val targetConditions: Set<Condition> = setOf(
        Condition.greaterThanOrEqual("health", 80)
    )

    fun isSatisfied(state: WorldState): Boolean {
        return (state.getValue("health") ?: 0) >= 80
    }

    fun create(): SimpleGoal {
        return SimpleGoal(
            id = ID,
            priority = PRIORITY,
            targetConditions = targetConditions,
            satisfied = ::isSatisfied
        )
    }
}
