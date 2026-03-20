package com.sect.game.feature.game.container

import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.entity.Sect
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.SectId
import com.sect.game.feature.game.contract.GameAction
import com.sect.game.feature.game.contract.GameIntent
import com.sect.game.feature.game.contract.GameState
import com.sect.game.mvi.GameErrorHandler
import com.sect.game.mvi.toUserMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class GameContainer {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _effects = Channel<GameAction>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var sect: Sect? = null

    fun processIntent(intent: GameIntent) {
        try {
            when (intent) {
                is GameIntent.LoadGame -> loadGame()
                is GameIntent.CreateDisciple -> createDisciple(intent.name, intent.attributes)
                is GameIntent.RemoveDisciple -> removeDisciple(intent.discipleId)
                is GameIntent.SelectDisciple -> selectDisciple(intent.discipleId)
            }
        } catch (e: Throwable) {
            val userMessage = e.toUserMessage()
            _state.value = _state.value.copy(error = userMessage)
            sendEffect(GameAction.ShowError(userMessage))
            GameErrorHandler.logError(e)
        }
    }

    private fun loadGame() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        val result = Sect.create(SectId("sect-1"), "青云宗")
        result.onSuccess { newSect ->
            sect = newSect
            _state.value = _state.value.copy(
                sectName = newSect.name,
                resources = newSect.resources,
                disciples = newSect.disciples.values.toList(),
                isLoading = false
            )
            sendEffect(GameAction.ShowSuccess("宗门创建成功"))
        }.onFailure { error ->
            _state.value = _state.value.copy(
                isLoading = false,
                error = error.toUserMessage()
            )
            sendEffect(GameAction.ShowError(error.toUserMessage()))
        }
    }

    private fun createDisciple(name: String, attributes: Attributes) {
        val currentSect = sect ?: run {
            sendEffect(GameAction.ShowError("宗门未初始化"))
            return
        }
        val discipleResult = Disciple.create(
            id = DiscipleId("disciple-${System.currentTimeMillis()}"),
            name = name,
            attributes = attributes
        )
        discipleResult.onSuccess { newDisciple ->
            val addResult = currentSect.addDisciple(newDisciple)
            addResult.onSuccess {
                _state.value = _state.value.copy(
                    sectName = currentSect.name,
                    resources = currentSect.resources,
                    disciples = currentSect.disciples.values.toList()
                )
                sendEffect(GameAction.ShowSuccess("成功招募弟子：${newDisciple.name}"))
            }.onFailure { error ->
                sendEffect(GameAction.ShowError(error.toUserMessage()))
            }
        }.onFailure { error ->
            sendEffect(GameAction.ShowError(error.toUserMessage()))
        }
    }

    private fun removeDisciple(discipleId: String) {
        val currentSect = sect ?: run {
            sendEffect(GameAction.ShowError("宗门未初始化"))
            return
        }
        val id = DiscipleId(discipleId)
        val result = currentSect.removeDisciple(id)
        result.onSuccess { removed ->
            _state.value = _state.value.copy(
                disciples = currentSect.disciples.values.toList()
            )
            sendEffect(GameAction.ShowSuccess("已删除弟子：${removed.name}"))
        }.onFailure { error ->
            sendEffect(GameAction.ShowError(error.toUserMessage()))
        }
    }

    private fun selectDisciple(discipleId: String) {
        sendEffect(GameAction.NavigateToDiscipleDetail)
    }

    private fun sendEffect(effect: GameAction) {
        _effects.trySend(effect)
    }
}
