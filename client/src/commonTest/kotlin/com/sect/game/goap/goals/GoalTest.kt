package com.sect.game.goap.goals

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.WorldState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoalTest {

    @Test
    fun survivalGoal_hasCorrectPriority() {
        val goal = SurvivalGoal.create()
        assertEquals("survival", goal.id)
        assertEquals(100, goal.priority)
    }

    @Test
    fun survivalGoal_satisfiedWhenHealth80OrMore() {
        val goal = SurvivalGoal.create()
        val satisfiedState = WorldState().withValue("health", 80)
        val unsatisfiedState = WorldState().withValue("health", 50)
        
        assertTrue(goal.isGoalSatisfied(satisfiedState))
        assertFalse(goal.isGoalSatisfied(unsatisfiedState))
    }

    @Test
    fun cultivationGoal_hasCorrectPriority() {
        val goal = CultivationGoal.create()
        assertEquals("cultivation", goal.id)
        assertEquals(50, goal.priority)
    }

    @Test
    fun cultivationGoal_satisfiedWhenProgress100OrMore() {
        val goal = CultivationGoal.create()
        val satisfiedState = WorldState().withValue("cultivationProgress", 100)
        val unsatisfiedState = WorldState().withValue("cultivationProgress", 50)
        
        assertTrue(goal.isGoalSatisfied(satisfiedState))
        assertFalse(goal.isGoalSatisfied(unsatisfiedState))
    }

    @Test
    fun breakthroughGoal_hasCorrectPriority() {
        val goal = BreakthroughGoal.create()
        assertEquals("breakthrough", goal.id)
        assertEquals(70, goal.priority)
    }

    @Test
    fun breakthroughGoal_satisfiedWhenProgress100AndReadinessAbove80() {
        val goal = BreakthroughGoal.create()
        val satisfiedState = WorldState()
            .withValue("cultivationProgress", 100)
            .withValue("readiness", 85)
        val unsatisfiedState = WorldState()
            .withValue("cultivationProgress", 100)
            .withValue("readiness", 80)
        
        assertTrue(goal.isGoalSatisfied(satisfiedState))
        assertFalse(goal.isGoalSatisfied(unsatisfiedState))
    }

    @Test
    fun restGoal_hasCorrectPriority() {
        val goal = RestGoal.create()
        assertEquals("rest", goal.id)
        assertEquals(60, goal.priority)
    }

    @Test
    fun restGoal_satisfiedWhenFatigueBelow20() {
        val goal = RestGoal.create()
        val satisfiedState = WorldState().withValue("fatigue", 15)
        val unsatisfiedState = WorldState().withValue("fatigue", 50)
        
        assertTrue(goal.isGoalSatisfied(satisfiedState))
        assertFalse(goal.isGoalSatisfied(unsatisfiedState))
    }

    @Test
    fun goalFactory_returnsCorrectGoal() {
        val survival = GoalFactoryImpl.getGoal("survival")
        val cultivation = GoalFactoryImpl.getGoal("cultivation")
        val breakthrough = GoalFactoryImpl.getGoal("breakthrough")
        val rest = GoalFactoryImpl.getGoal("rest")
        
        assertTrue(survival != null)
        assertTrue(cultivation != null)
        assertTrue(breakthrough != null)
        assertTrue(rest != null)
        
        assertEquals(100, survival.priority)
        assertEquals(50, cultivation.priority)
        assertEquals(70, breakthrough.priority)
        assertEquals(60, rest.priority)
    }

    @Test
    fun goalFactory_returnsAllGoals() {
        val goals = GoalFactoryImpl.getAllGoals()
        assertEquals(4, goals.size)
    }

    @Test
    fun simpleGoal_implementsGoalInterface() {
        val goal = SimpleGoal(
            id = "test",
            priority = 10,
            targetConditions = setOf(Condition.greaterThan("health", 50)),
            satisfied = { it.getValue("health") ?: 0 > 50 }
        )
        
        assertEquals("test", goal.id)
        assertEquals(10, goal.priority)
        assertEquals(1, goal.targetConditions.size)
    }

    @Test
    fun goalTemplate_createsSimpleGoal() {
        val template = GoalTemplate(
            id = "test",
            name = "Test",
            priority = 25,
            conditions = setOf(Condition.greaterThan("health", 50)),
            targetState = WorldState().withValue("health", 100)
        )
        
        val goal = template.toGoal { it.getValue("health") ?: 0 >= 100 }
        
        assertEquals("test", goal.id)
        assertEquals(25, goal.priority)
        assertTrue(goal.isGoalSatisfied(WorldState().withValue("health", 100)))
    }

    @Test
    fun goals_sortedByPriority() {
        val goals = GoalFactoryImpl.getAllGoals()
        val sorted = goals.sortedByDescending { it.priority }
        
        assertEquals("survival", sorted[0].id)
        assertEquals("breakthrough", sorted[1].id)
        assertEquals("rest", sorted[2].id)
        assertEquals("cultivation", sorted[3].id)
    }
}
