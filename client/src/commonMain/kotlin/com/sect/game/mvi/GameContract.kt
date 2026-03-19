package com.sect.game.mvi

import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.entity.Resources
import com.sect.game.domain.entity.Sect
import com.sect.game.domain.valueobject.Attributes

data class GameState(
    val sectName: String = "青云宗",
    val resources: Resources = Resources(),
    val disciples: List<Disciple> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface GameIntent {
    data object LoadGame : GameIntent
    data class CreateDisciple(val name: String, val attributes: Attributes) : GameIntent
    data class RemoveDisciple(val discipleId: String) : GameIntent
    data class SelectDisciple(val discipleId: String) : GameIntent
}

sealed interface GameEffect {
    data class ShowError(val message: String) : GameEffect
    data class ShowSuccess(val message: String) : GameEffect
    data object NavigateToDiscipleDetail : GameEffect
}
