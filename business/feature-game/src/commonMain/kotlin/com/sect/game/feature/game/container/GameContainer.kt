package com.sect.game.feature.game.container

import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.entity.Sect
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.engine.GameEngine
import com.sect.game.feature.game.contract.GameAction
import com.sect.game.feature.game.contract.GameIntent
import com.sect.game.feature.game.contract.GameState
import com.sect.game.mvi.GameErrorHandler
import com.sect.game.mvi.toUserMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.Store

class GameContainer(private val kodein: DI) : Container<GameState, GameIntent, GameAction> {

    private val sect: Sect = kodein.direct.instance()
    private val gameEngine: GameEngine = kodein.direct.instance()

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _effects = Channel<GameAction>(Channel.BUFFERED)
    val effects: Flow<GameAction> = _effects.receiveAsFlow()

    override val store: Store<GameState, GameIntent, GameAction>
        get() = throw UnsupportedOperationException("Store not available - use state and effects directly")

    fun processIntent(intent: GameIntent) {
        try {
            when (intent) {
                is GameIntent.LoadGame -> loadGame()
                is GameIntent.CreateDisciple ->
                    createDisciple(
                        intent.name,
                        intent.attributes,
                    )
                is GameIntent.RemoveDisciple -> removeDisciple(intent.discipleId)
                is GameIntent.SelectDisciple -> selectDisciple(intent.discipleId)
                is GameIntent.PauseGame -> pauseGame()
                is GameIntent.ResumeGame -> resumeGame()
                is GameIntent.StopGame -> stopGame()
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
        gameEngine.onTick = { tick ->
            _state.value =
                _state.value.copy(
                    tickCount = tick,
                    disciples = sect.disciples.values.toList(),
                    isPaused = false,
                )
        }
        gameEngine.start()
        _state.value =
            _state.value.copy(
                sectName = sect.name,
                resources = sect.resources,
                disciples = sect.disciples.values.toList(),
                isLoading = false,
            )
        sendEffect(GameAction.ShowSuccess("宗门创建成功"))
    }

    private fun createDisciple(
        name: String,
        attributes: Attributes,
    ) {
        val discipleResult =
            Disciple.create(
                id = DiscipleId("disciple-${System.currentTimeMillis()}"),
                name = name,
                attributes = attributes,
            )
        discipleResult
            .onSuccess { newDisciple ->
                val addResult = sect.addDisciple(newDisciple)
                addResult
                    .onSuccess {
                        _state.value =
                            _state.value.copy(
                                sectName = sect.name,
                                resources = sect.resources,
                                disciples = sect.disciples.values.toList(),
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
        val id = DiscipleId(discipleId)
        val result = sect.removeDisciple(id)
        result.onSuccess { removed ->
            _state.value =
                _state.value.copy(
                    disciples = sect.disciples.values.toList(),
                )
            sendEffect(GameAction.ShowSuccess("已删除弟子：${removed.name}"))
        }.onFailure { error ->
            sendEffect(GameAction.ShowError(error.toUserMessage()))
        }
    }

    private fun selectDisciple(discipleId: String) {
        sendEffect(GameAction.NavigateToDiscipleDetail)
    }

    private fun pauseGame() {
        gameEngine.pause()
        _state.value = _state.value.copy(isPaused = true)
    }

    private fun resumeGame() {
        gameEngine.resume()
        _state.value = _state.value.copy(isPaused = false)
    }

    private fun stopGame() {
        gameEngine.stop()
        _state.value =
            _state.value.copy(
                tickCount = 0,
                isPaused = false,
            )
    }

    private fun sendEffect(effect: GameAction) {
        _effects.trySend(effect)
    }
}
