package com.sect.game.goap.goals

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.WorldState

object BreakthroughGoal {
    const val ID = "breakthrough"
    const val PRIORITY = 70

    val targetConditions: Set<Condition> = setOf(
        Condition.greaterThanOrEqual("cultivationProgress", 100),
        Condition.greaterThan("readiness", 80)
    )

    fun isSatisfied(state: WorldState): Boolean {
        val progress = state.getValue("cultivationProgress") ?: 0
        val readiness = state.getValue("readiness") ?: 0
        return progress >= 100 && readiness > 80
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
