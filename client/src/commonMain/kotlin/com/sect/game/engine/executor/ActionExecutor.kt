package com.sect.game.engine.executor

import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.valueobject.Realm
import com.sect.game.goap.actions.Action
import com.sect.game.goap.core.ModifyEffect

interface ActionExecutor {
    fun execute(
        disciple: Disciple,
        action: Action,
    ): Disciple
}

class DefaultActionExecutor : ActionExecutor {
    override fun execute(
        disciple: Disciple,
        action: Action,
    ): Disciple {
        var result = disciple

        for (effect in action.effects) {
            result = applyEffect(result, effect)
        }

        return result
    }

    private fun applyEffect(
        disciple: Disciple,
        effect: com.sect.game.goap.core.Effect,
    ): Disciple {
        if (effect !is ModifyEffect) {
            return disciple
        }

        return when (effect.key) {
            "health" -> disciple.copy(health = (disciple.health + effect.delta).coerceIn(0, 100))
            "fatigue" -> disciple.copy(fatigue = (disciple.fatigue + effect.delta).coerceIn(0, 100))
            "cultivationProgress" ->
                disciple.copy(
                    cultivationProgress = (disciple.cultivationProgress + effect.delta).coerceIn(0, 100),
                )
            "realm" -> {
                val newRealm = Realm.fromOrder(disciple.realm.order + effect.delta)
                if (newRealm != null && newRealm.order > disciple.realm.order) {
                    disciple.copy(realm = newRealm, cultivationProgress = 0)
                } else {
                    disciple
                }
            }
            "lifespan" -> disciple.copy(lifespan = (disciple.lifespan + effect.delta).coerceAtLeast(0))
            else -> disciple
        }
    }
}
