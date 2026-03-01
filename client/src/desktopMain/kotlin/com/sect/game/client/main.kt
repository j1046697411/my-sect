package com.sect.game.client

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.material3.Text

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        Text("Hello Sect Game - Desktop")
    }
}
