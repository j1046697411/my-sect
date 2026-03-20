package com.sect.game.goap.goals

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.WorldState

object BreakthroughGoal {
    const val ID = "breakthrough"
    const val PRIORITY = 70

    val targetConditions: Set<Condition> =
        setOf(
            Condition.greaterThanOrEqual("cultivationProgress", 100),
            Condition.greaterThan("readiness", 80),
        )

    private val template = GoalTemplate(
        id = ID,
        name = "突破",
        priority = PRIORITY,
        conditions = targetConditions,
        targetState = WorldState().withValue("cultivationProgress", 100),
    )

    fun isSatisfied(state: WorldState): Boolean {
        val progress = state.getValue("cultivationProgress") ?: 0
        val readiness = state.getValue("readiness") ?: 0
        return progress >= 100 && readiness > 80
    }

    fun create(): SimpleGoal = template.toGoal(::isSatisfied)
}
