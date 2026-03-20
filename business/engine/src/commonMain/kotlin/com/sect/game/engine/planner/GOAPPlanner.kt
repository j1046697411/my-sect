package com.sect.game.engine.planner

import com.sect.game.goap.actions.Action
import com.sect.game.goap.core.WorldState
import com.sect.game.goap.goals.Goal
import java.util.PriorityQueue

interface GOAPPlanner {
    fun plan(
        currentState: WorldState,
        goal: Goal,
        availableActions: List<Action>
    ): List<Action>
}

class AStarPlanner(
    private val maxSearchDepth: Int = 10,
    private val maxSearchNodes: Int = 500
) : GOAPPlanner {
    
    data class PlanningNode(
        val state: WorldState,
        val actionSequence: List<Action>,
        val costSoFar: Float,
        val estimatedTotalCost: Float
    ) {
        val priority: Float get() = estimatedTotalCost
    }
    
    override fun plan(
        currentState: WorldState,
        goal: Goal,
        availableActions: List<Action>
    ): List<Action> {
        if (goal.isGoalSatisfied(currentState)) {
            return emptyList()
        }
        
        val openSet = PriorityQueue<PlanningNode>(compareBy { it.priority })
        val visitedStates = mutableSetOf<WorldState>()
        
        val initialHeuristic = calculateHeuristic(currentState, goal)
        openSet.add(PlanningNode(
            state = currentState,
            actionSequence = emptyList(),
            costSoFar = 0f,
            estimatedTotalCost = initialHeuristic
        ))
        
        var nodesExpanded = 0
        var bestActionSequence: List<Action> = emptyList()
        var bestHeuristic = Float.MAX_VALUE
        
        while (openSet.isNotEmpty() && nodesExpanded < maxSearchNodes) {
            val current = openSet.poll()
            
            val currentHeuristic = calculateHeuristic(current.state, goal)
            if (currentHeuristic < bestHeuristic && current.actionSequence.isNotEmpty()) {
                bestHeuristic = currentHeuristic
                bestActionSequence = current.actionSequence
            }
            
            if (goal.isGoalSatisfied(current.state)) {
                if (current.actionSequence.isNotEmpty()) {
                    return current.actionSequence
                }
                continue
            }
            
            if (current.actionSequence.size >= maxSearchDepth) {
                continue
            }
            
            if (current.state in visitedStates) {
                continue
            }
            visitedStates.add(current.state)
            nodesExpanded++
            
            val validActions = availableActions.filter { it.isValid(current.state) }
            
            for (action in validActions) {
                val newState = action.applyEffects(current.state)
                val newCostSoFar = current.costSoFar + action.cost
                val newHeuristic = calculateHeuristic(newState, goal)
                
                if (goal.isGoalSatisfied(newState)) {
                    return current.actionSequence + action
                }
                
                if (newState !in visitedStates) {
                    openSet.add(PlanningNode(
                        state = newState,
                        actionSequence = current.actionSequence + action,
                        costSoFar = newCostSoFar,
                        estimatedTotalCost = newCostSoFar + newHeuristic
                    ))
                }
            }
        }
        
        return bestActionSequence
    }
    
    private fun calculateHeuristic(state: WorldState, goal: Goal): Float {
        val targetConditions = goal.targetConditions
        if (targetConditions.isEmpty()) {
            return 0f
        }
        
        var unsatisfiedCount = 0
        for (condition in targetConditions) {
            if (!condition.isSatisfiedBy(state)) {
                unsatisfiedCount++
            }
        }
        return unsatisfiedCount.toFloat()
    }
}
