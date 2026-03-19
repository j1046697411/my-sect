package com.sect.game.goap.actions

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.ModifyEffect

val AlchemyAction: Action = action(
    id = "alchemy",
    name = "炼丹",
    cost = 3
) {
    withPrecondition(
        Condition.and(
            Condition.greaterThanOrEqual("resources", 10),
            Condition.greaterThan("health", 50)
        )
    )
    withEffect(ModifyEffect("resources", -10))
    withEffect(ModifyEffect("cultivationProgress", 20))
}
