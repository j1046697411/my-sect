package com.sect.game.feature.game.contract

import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.entity.Resources
import com.sect.game.domain.valueobject.Attributes
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

/**
 * 游戏模块契约定义
 *
 * State - UI 显示的状态
 * Intent - 用户操作或系统事件
 * Action - 副作用（如弹窗、导航）
 */
data class GameState(
    val sectName: String = "青云宗",
    val resources: Resources = Resources.EMPTY,
    val disciples: List<Disciple> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val tickCount: Long = 0,
    val isPaused: Boolean = false,
    val selectedDiscipleId: String? = null,
) : MVIState

sealed interface GameIntent : MVIIntent {
    data object LoadGame : GameIntent

    data class CreateDisciple(val name: String, val attributes: Attributes) : GameIntent

    data class RemoveDisciple(val discipleId: String) : GameIntent

    data class SelectDisciple(val discipleId: String) : GameIntent

    data object PauseGame : GameIntent

    data object ResumeGame : GameIntent

    data object StopGame : GameIntent
}

sealed interface GameAction : MVIAction {
    data class ShowError(val message: String) : GameAction

    data class ShowSuccess(val message: String) : GameAction

    data object NavigateToDiscipleDetail : GameAction
}
