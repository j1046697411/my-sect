package com.sect.game.goap.goals

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.WorldState

object CultivationGoal {
    const val ID = "cultivation"
    const val PRIORITY = 50

    val targetConditions: Set<Condition> =
        setOf(
            Condition.greaterThanOrEqual("cultivationProgress", 100),
        )

    fun isSatisfied(state: WorldState): Boolean {
        return (state.getValue("cultivationProgress") ?: 0) >= 100
    }

    fun create(): SimpleGoal {
        return SimpleGoal(
            id = ID,
            priority = PRIORITY,
            targetConditions = targetConditions,
            satisfied = ::isSatisfied,
        )
    }
}
