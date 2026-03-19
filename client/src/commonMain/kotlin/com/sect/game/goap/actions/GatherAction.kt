package com.sect.game.goap.actions

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.ModifyEffect

val GatherAction: Action = action(
    id = "gather",
    name = "采集",
    cost = 2
) {
    withPrecondition(Condition.greaterThan("health", 30))
    withEffect(ModifyEffect("resources", 5))
    withEffect(ModifyEffect("fatigue", 10))
}
