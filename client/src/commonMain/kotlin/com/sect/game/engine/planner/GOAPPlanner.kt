package com.sect.game.engine.planner

import com.sect.game.goap.actions.Action
import com.sect.game.goap.core.WorldState
import com.sect.game.goap.goals.Goal

interface GOAPPlanner {
    fun plan(
        currentState: WorldState,
        goal: Goal,
        availableActions: List<Action>
    ): List<Action>
}

class AStarPlanner : GOAPPlanner {
    override fun plan(
        currentState: WorldState,
        goal: Goal,
        availableActions: List<Action>
    ): List<Action> {
        if (goal.isGoalSatisfied(currentState)) {
            return emptyList()
        }

        val validActions = availableActions.filter { it.isValid(currentState) }
        if (validActions.isEmpty()) {
            return emptyList()
        }

        val actionCosts = validActions.associateWith { it.cost.toFloat() }
        val sortedActions = validActions.sortedBy { actionCosts[it] ?: Float.MAX_VALUE }

        for (action in sortedActions) {
            val newState = action.applyEffects(currentState)
            if (goal.isGoalSatisfied(newState)) {
                return listOf(action)
            }
        }

        return sortedActions.take(1).map { it }
    }
}
