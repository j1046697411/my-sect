package com.sect.game.goap.goals

import com.sect.game.goap.core.WorldState

object GoalFactoryImpl {
    private val goalTemplates: Map<String, GoalTemplate> =
        mapOf(
            SurvivalGoal.ID to
                GoalTemplate(
                    id = SurvivalGoal.ID,
                    name = "生存",
                    priority = SurvivalGoal.PRIORITY,
                    conditions = SurvivalGoal.targetConditions,
                    targetState = WorldState().withValue("health", 80),
                ),
            CultivationGoal.ID to
                GoalTemplate(
                    id = CultivationGoal.ID,
                    name = "修炼",
                    priority = CultivationGoal.PRIORITY,
                    conditions = CultivationGoal.targetConditions,
                    targetState = WorldState().withValue("cultivationProgress", 100),
                ),
            BreakthroughGoal.ID to
                GoalTemplate(
                    id = BreakthroughGoal.ID,
                    name = "突破",
                    priority = BreakthroughGoal.PRIORITY,
                    conditions = BreakthroughGoal.targetConditions,
                    targetState = WorldState().withValue("realm", 1),
                ),
            RestGoal.ID to
                GoalTemplate(
                    id = RestGoal.ID,
                    name = "休息",
                    priority = RestGoal.PRIORITY,
                    conditions = RestGoal.targetConditions,
                    targetState = WorldState().withValue("fatigue", 20),
                ),
        )

    fun getGoal(id: String): Goal? {
        return when (id) {
            SurvivalGoal.ID -> SurvivalGoal.create()
            CultivationGoal.ID -> CultivationGoal.create()
            BreakthroughGoal.ID -> BreakthroughGoal.create()
            RestGoal.ID -> RestGoal.create()
            else -> null
        }
    }

    fun getAllGoals(): List<Goal> {
        return listOf(
            SurvivalGoal.create(),
            CultivationGoal.create(),
            BreakthroughGoal.create(),
            RestGoal.create(),
        )
    }

    fun createGoalsForState(state: WorldState): List<Goal> {
        return getAllGoals()
    }
}
