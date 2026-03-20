package com.sect.game.engine.planner

import com.sect.game.engine.DiscipleWorldStateConverter
import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm
import com.sect.game.goap.actions.CultivationActionPackage
import com.sect.game.goap.goals.BreakthroughGoal
import com.sect.game.goap.goals.CultivationGoal
import com.sect.game.goap.goals.SurvivalGoal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GOAPPlannerIntegrationTest {
    private val planner = AStarPlanner()
    private val actions = CultivationActionPackage.actions

    @Test
    fun survivalGoal_triggersWhenHealthBelow80() {
        val disciple = createDisciple(health = 50)
        val worldState = DiscipleWorldStateConverter.toWorldState(disciple)
        val goal = SurvivalGoal.create()

        assertTrue(!goal.isGoalSatisfied(worldState))
    }

    @Test
    fun survivalGoal_satisfiedWhenHealth80OrAbove() {
        val disciple = createDisciple(health = 80)
        val worldState = DiscipleWorldStateConverter.toWorldState(disciple)
        val goal = SurvivalGoal.create()

        assertTrue(goal.isGoalSatisfied(worldState))
    }

    @Test
    fun plannerSelectsAction_forLowHealthDisciple() {
        val disciple = createDisciple(health = 50, fatigue = 30)
        val worldState = DiscipleWorldStateConverter.toWorldState(disciple)
        val survivalGoal = SurvivalGoal.create()

        val plan = planner.plan(worldState, survivalGoal, actions)

        assertNotNull(plan)
        assertTrue(plan.isNotEmpty())
    }

    @Test
    fun plannerSelectsCultivateAction_forHealthyDisciple() {
        val disciple = createDisciple(health = 90, fatigue = 30, cultivationProgress = 0)
        val worldState = DiscipleWorldStateConverter.toWorldState(disciple)
        val cultivationGoal = CultivationGoal.create()

        val plan = planner.plan(worldState, cultivationGoal, actions)

        assertNotNull(plan)
        assertTrue(plan.isNotEmpty())
        assertEquals("cultivate", plan.first().id)
    }

    @Test
    fun prioritySurvivalOverCultivation_whenHealthLow() {
        val survivalGoal = SurvivalGoal.create()
        val cultivationGoal = CultivationGoal.create()

        assertTrue(
            survivalGoal.priority > cultivationGoal.priority,
            "SurvivalGoal priority=${survivalGoal.priority} should be higher than CultivationGoal priority=${cultivationGoal.priority}",
        )
    }

    @Test
    fun breakthroughGoal_triggersWhenProgressFullAndReadinessHigh() {
        val disciple = createDisciple(health = 100, fatigue = 10, cultivationProgress = 100)
        val worldState = DiscipleWorldStateConverter.toWorldState(disciple)
        val breakthroughGoal = BreakthroughGoal.create()

        assertTrue(breakthroughGoal.isGoalSatisfied(worldState))
    }

    @Test
    fun breakthroughGoal_notSatisfiedWhenReadinessLow() {
        val disciple = createDisciple(health = 100, fatigue = 90, cultivationProgress = 100)
        val worldState = DiscipleWorldStateConverter.toWorldState(disciple)
        val breakthroughGoal = BreakthroughGoal.create()

        assertTrue(!breakthroughGoal.isGoalSatisfied(worldState))
    }

    @Test
    fun readnessCalculation_formulaMatches() {
        val disciple = createDisciple(health = 100, fatigue = 10)
        val worldState = DiscipleWorldStateConverter.toWorldState(disciple)

        val readiness = worldState.getValue("readiness")
        val expected = ((100 - 10) * 0.6 + 100 * 0.4).toInt().coerceIn(0, 100)

        assertEquals(expected, readiness)
    }

    private fun createDisciple(
        health: Int = 100,
        fatigue: Int = 0,
        cultivationProgress: Int = 0,
        realm: Realm = Realm.LianQi,
        lifespan: Int = 100,
    ): Disciple {
        return Disciple(
            id = DiscipleId("test"),
            name = "测试弟子",
            realm = realm,
            attributes = Attributes.DEFAULT,
            cultivationProgress = cultivationProgress,
            fatigue = fatigue,
            health = health,
            lifespan = lifespan,
        )
    }
}
