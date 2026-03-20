package com.sect.game.goap.goals

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.WorldState

data class GoalTemplate(
    val id: String,
    val name: String,
    val priority: Int,
    val conditions: Set<Condition>,
    val targetState: WorldState
) {
    fun toGoal(satisfied: (WorldState) -> Boolean): SimpleGoal {
        return SimpleGoal(
            id = id,
            priority = priority,
            targetConditions = conditions,
            satisfied = satisfied
        )
    }
}
