package com.sect.game.goap.goals

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.WorldState

interface Goal {
    val id: String
    val priority: Int
    val targetConditions: Set<Condition>
    fun isGoalSatisfied(state: WorldState): Boolean
}
