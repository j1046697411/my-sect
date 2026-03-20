package com.sect.game.goap.actions

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.ModifyEffect

val CultivateAction: Action =
    action(
        id = "cultivate",
        name = "修炼",
        cost = 1,
    ) {
        withPrecondition(Condition.lessThan("fatigue", 80))
        withEffect(ModifyEffect("cultivationProgress", 10))
        withEffect(ModifyEffect("fatigue", 15))
        withEffect(ModifyEffect("health", -5))
    }
