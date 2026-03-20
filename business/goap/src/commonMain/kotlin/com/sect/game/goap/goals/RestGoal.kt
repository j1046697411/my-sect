package com.sect.game.goap.goals

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.WorldState

object RestGoal {
    const val ID = "rest"
    const val PRIORITY = 60

    val targetConditions: Set<Condition> =
        setOf(
            Condition.lessThan("fatigue", 20),
        )

    private val template = GoalTemplate(
        id = ID,
        name = "休息",
        priority = PRIORITY,
        conditions = targetConditions,
        targetState = WorldState().withValue("fatigue", 20),
    )

    fun isSatisfied(state: WorldState): Boolean {
        return (state.getValue("fatigue") ?: 100) < 20
    }

    fun create(): SimpleGoal = template.toGoal(::isSatisfied)
}
