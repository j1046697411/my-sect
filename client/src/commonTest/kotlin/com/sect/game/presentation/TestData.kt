package com.sect.game.presentation

import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm
import com.sect.game.feature.game.container.GameContainer

object TestData {
    fun testDisciple(
        name: String = "张三",
        health: Int = 100,
        fatigue: Int = 0,
        cultivationProgress: Int = 50,
    ): Disciple =
        Disciple.create(
            id = DiscipleId("test-${System.nanoTime()}"),
            name = name,
            attributes = Attributes.DEFAULT,
            realm = Realm.LianQi,
            lifespan = 100,
        ).getOrThrow().copy(
            health = health,
            fatigue = fatigue,
            cultivationProgress = cultivationProgress,
        )

    fun testContainer(): GameContainer = GameContainer()
}
