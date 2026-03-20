package com.sect.game.goap.goals

object GoalFactoryImpl {
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
}
