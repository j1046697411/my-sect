package com.sect.game.client

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sect.game.presentation.GameScreen
import com.sect.game.mvi.GameContainer

fun main() {
    val container = GameContainer()
    
    application {
        Window(onCloseRequest = ::exitApplication) {
            GameScreen(container = container)
        }
    }
}
