package com.sect.game.goap.actions

interface ActionProvider {
    val actions: List<Action>
}

object CultivationActionPackage : ActionProvider {
    override val actions: List<Action> = listOf(
        CultivateAction,
        RestAction,
        BreakthroughAction,
        GatherAction,
        AlchemyAction
    )
}
