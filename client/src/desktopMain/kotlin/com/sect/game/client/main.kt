package com.sect.game.client

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sect.game.feature.game.container.GameContainer
import com.sect.game.feature.game.presentation.GameScreen
import com.sect.game.presentation.theme.SectTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun main() = application {
    val container = GameContainer()
    val mainScope = MainScope()

    Window(onCloseRequest = ::exitApplication) {
        SectTheme {
            GameScreen(container = container)
        }
    }

    mainScope.launch {
        container.effects.collectLatest { effect ->
            when (effect) {
                is com.sect.game.feature.game.contract.GameAction.ShowError -> {
                    println("错误: ${effect.message}")
                }
                is com.sect.game.feature.game.contract.GameAction.ShowSuccess -> {
                    println("提示: ${effect.message}")
                }
                is com.sect.game.feature.game.contract.GameAction.NavigateToDiscipleDetail -> {
                    println("导航到弟子详情")
                }
            }
        }
    }
}
