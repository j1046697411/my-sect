package com.sect.game.goap.goals

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.WorldState

data class SimpleGoal(
    override val id: String,
    override val priority: Int,
    override val targetConditions: Set<Condition>,
    private val satisfied: (WorldState) -> Boolean,
) : Goal {
    override fun isGoalSatisfied(state: WorldState): Boolean = satisfied(state)
}
