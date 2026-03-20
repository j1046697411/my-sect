package com.sect.game.goap.actions

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.ModifyEffect

val BreakthroughAction: Action =
    action(
        id = "breakthrough",
        name = "突破",
        cost = 5,
    ) {
        withPrecondition(Condition.greaterThanOrEqual("cultivationProgress", 100))
        withEffect(ModifyEffect("realm", 1))
        withEffect(ModifyEffect("cultivationProgress", -100))
    }
