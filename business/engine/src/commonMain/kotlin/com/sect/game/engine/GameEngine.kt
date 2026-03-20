package com.sect.game.engine

import com.sect.game.domain.entity.Sect
import com.sect.game.engine.executor.ActionExecutor
import com.sect.game.engine.executor.DefaultActionExecutor
import com.sect.game.engine.planner.AStarPlanner
import com.sect.game.engine.planner.GOAPPlanner
import com.sect.game.engine.registry.ActionRegistry
import com.sect.game.engine.registry.DefaultActionRegistry
import com.sect.game.goap.actions.Action
import com.sect.game.goap.actions.CultivationActionPackage
import com.sect.game.goap.goals.Goal
import com.sect.game.goap.goals.GoalFactoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameEngine private constructor(
    private val sect: Sect,
    private val planner: GOAPPlanner,
    private val executor: ActionExecutor,
    private val registry: ActionRegistry,
    val tickRate: Int = 60
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var gameLoopJob: Job? = null
    
    var isRunning: Boolean = false
        private set
    
    var isPaused: Boolean = false
        private set
    
    var tickCount: Long = 0
        private set
    
    var onTick: ((Long) -> Unit)? = null
    
    fun start() {
        if (isRunning) return
        isRunning = true
        isPaused = false
        
        gameLoopJob = scope.launch {
            val tickIntervalMs = 1000L / tickRate
            
            while (isActive && isRunning) {
                if (!isPaused) {
                    tick()
                    tickCount++
                    onTick?.invoke(tickCount)
                }
                delay(tickIntervalMs)
            }
        }
    }
    
    fun pause() {
        if (!isRunning) return
        isPaused = true
    }
    
    fun resume() {
        if (!isRunning) return
        isPaused = false
    }
    
    fun stop() {
        isRunning = false
        isPaused = false
        gameLoopJob?.cancel()
        gameLoopJob = null
    }
    
    private fun tick() {
        updateDisciples()
    }
    
    private fun updateDisciples() {
        val disciples = sect.disciples.values.toList()
        val actions = registry.getAllActions()
        val goals = registry.getAllGoals()
        
        for (disciple in disciples) {
            if (disciple.isDead()) continue
            
            var currentState = DiscipleWorldStateConverter.toWorldState(disciple)
            val selectedGoal = selectBestGoal(currentState, goals)
            
            if (selectedGoal != null) {
                val plan = planner.plan(currentState, selectedGoal, actions)
                
                for (action in plan) {
                    if (!action.isValid(currentState)) continue
                    
                    val updatedDisciple = executor.execute(disciple, action)
                    sect.updateDisciple(updatedDisciple)
                    currentState = DiscipleWorldStateConverter.toWorldState(updatedDisciple)
                }
            }
        }
    }
    
    private fun selectBestGoal(state: com.sect.game.goap.core.WorldState, goals: List<Goal>): Goal? {
        return goals
            .filter { !it.isGoalSatisfied(state) }
            .maxByOrNull { it.priority }
    }
    
    companion object {
        fun create(
            sect: Sect,
            tickRate: Int = 60,
            planner: GOAPPlanner = AStarPlanner(),
            executor: ActionExecutor = DefaultActionExecutor(),
            registry: ActionRegistry = createDefaultRegistry()
        ): GameEngine {
            return GameEngine(sect, planner, executor, registry, tickRate)
        }
        
        private fun createDefaultRegistry(): ActionRegistry {
            val registry = DefaultActionRegistry()
            CultivationActionPackage.actions.forEach { registry.registerAction(it) }
            GoalFactoryImpl.getAllGoals().forEach { registry.registerGoal(it) }
            return registry
        }
    }
}
