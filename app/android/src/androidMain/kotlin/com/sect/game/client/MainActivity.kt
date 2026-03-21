package com.sect.game.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sect.game.data.storage.initializeAndroidContext
import com.sect.game.feature.game.container.GameContainer
import com.sect.game.feature.game.contract.GameAction
import com.sect.game.feature.game.presentation.GameScreen
import com.sect.game.presentation.theme.SectTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val container = GameContainer()
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeAndroidContext(this)
        setContent {
            SectTheme {
                GameScreen(container = container)
            }
        }

        mainScope.launch {
            container.effects.collectLatest { effect ->
                when (effect) {
                    is GameAction.ShowError -> { }
                    is GameAction.ShowSuccess -> { }
                    is GameAction.NavigateToDiscipleDetail -> { }
                }
            }
        }
    }
}
