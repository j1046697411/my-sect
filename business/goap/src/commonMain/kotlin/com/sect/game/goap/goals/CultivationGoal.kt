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

    private val template = GoalTemplate(
        id = ID,
        name = "修炼",
        priority = PRIORITY,
        conditions = targetConditions,
        targetState = WorldState().withValue("cultivationProgress", 100),
    )

    fun isSatisfied(state: WorldState): Boolean {
        return (state.getValue("cultivationProgress") ?: 0) >= 100
    }

    fun create(): SimpleGoal = template.toGoal(::isSatisfied)
}
