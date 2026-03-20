package com.sect.game.goap.actions

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.ModifyEffect

val RestAction: Action = action(
    id = "rest",
    name = "休息",
    cost = 1
) {
    withPrecondition(
        Condition.or(
            Condition.lessThan("health", 100),
            Condition.greaterThan("fatigue", 20)
        )
    )
    withEffect(ModifyEffect("fatigue", -30))
    withEffect(ModifyEffect("health", 10))
}
