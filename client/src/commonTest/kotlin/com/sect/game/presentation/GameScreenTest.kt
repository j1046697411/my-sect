package com.sect.game.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.sect.game.feature.game.presentation.EmptyContent
import com.sect.game.feature.game.presentation.GameScreen
import com.sect.game.feature.game.presentation.LoadingContent
import com.sect.game.presentation.theme.SectTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class GameScreenTest {
    @Test
    fun emptyContent_displaysNoDisciplesMessage() =
        runComposeUiTest {
            setContent {
                SectTheme {
                    EmptyContent()
                }
            }
            onNodeWithText("暂无弟子").assertExists()
            onNodeWithText("点击右下角按钮招募新弟子").assertExists()
        }

    @Test
    fun loadingContent_doesNotDisplayEmptyMessage() =
        runComposeUiTest {
            setContent {
                SectTheme {
                    LoadingContent()
                }
            }
            onNodeWithText("暂无弟子").assertDoesNotExist()
        }

    @Test
    fun gameScreen_rendersWithTestContainer() =
        runComposeUiTest {
            setContent {
                SectTheme {
                    GameScreen(container = TestData.testContainer())
                }
            }
            onNodeWithText("青云宗").assertExists()
        }
}
