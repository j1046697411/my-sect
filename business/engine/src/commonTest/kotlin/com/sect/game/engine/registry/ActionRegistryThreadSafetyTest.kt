package com.sect.game.engine.registry

import com.sect.game.goap.actions.Action
import com.sect.game.goap.goals.Goal
import com.sect.game.goap.core.WorldState
import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.Effect
import kotlin.test.Test
import kotlin.test.assertTrue

class ActionRegistryThreadSafetyTest {
    private fun createTestAction(id: String): Action = object : Action {
        override val id: String = id
        override val name: String = id
        override val cost: Int = 1
        override val preconditions: List<Condition> = emptyList()
        override val effects: List<Effect> = emptyList()
        override fun isValid(state: WorldState): Boolean = true
        override fun applyEffects(state: WorldState): WorldState = state
    }

    private fun createTestGoal(id: String, priority: Int): Goal {
        return object : Goal {
            override val id: String = id
            override val priority: Int = priority
            override val targetConditions: Set<Condition> = emptySet()
            override fun isGoalSatisfied(state: WorldState): Boolean = true
        }
    }

    @Test
    fun concurrentRegistration_doesNotCrash() {
        val registry = DefaultActionRegistry()
        val actionCount = 100
        val threadCount = 10

        val threads = (1..threadCount).map { threadIndex ->
            Thread {
                repeat(actionCount / threadCount) { i ->
                    registry.registerAction(createTestAction("action-${threadIndex}-$i"))
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        val allActions = registry.getAllActions()
        assertTrue(allActions.isNotEmpty(), "并发注册后应有行动被注册")
    }

    @Test
    fun concurrentGoalRegistration_doesNotCrash() {
        val registry = DefaultActionRegistry()
        val goalCount = 50
        val threadCount = 10

        val threads = (1..threadCount).map { threadIndex ->
            Thread {
                repeat(goalCount / threadCount) { i ->
                    registry.registerGoal(createTestGoal("goal-${threadIndex}-$i", i))
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        val allGoals = registry.getAllGoals()
        assertTrue(allGoals.isNotEmpty(), "并发注册后应有目标被注册")
    }

    @Test
    fun concurrentReadAndWrite_doesNotCrash() {
        val registry = DefaultActionRegistry()

        repeat(5) { i ->
            registry.registerAction(createTestAction("initial-action-$i"))
        }

        val readerThread = Thread {
            repeat(100) {
                registry.getAllActions()
                registry.getAllGoals()
            }
        }

        val writerThread = Thread {
            repeat(100) { i ->
                registry.registerAction(createTestAction("new-action-$i"))
            }
        }

        readerThread.start()
        writerThread.start()

        readerThread.join()
        writerThread.join()

        assertTrue(registry.getAllActions().isNotEmpty())
    }
}
