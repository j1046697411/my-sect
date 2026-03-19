package com.sect.game.engine

import com.sect.game.domain.entity.Disciple
import com.sect.game.goap.core.WorldState

object DiscipleWorldStateConverter {
    fun toWorldState(disciple: Disciple): WorldState {
        return WorldState()
            .withValue("health", disciple.health)
            .withValue("fatigue", disciple.fatigue)
            .withValue("cultivationProgress", disciple.cultivationProgress)
            .withValue("realm", disciple.realm.order)
            .withValue("lifespan", disciple.lifespan)
    }
}
