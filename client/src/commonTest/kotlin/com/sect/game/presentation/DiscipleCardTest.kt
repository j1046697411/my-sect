package com.sect.game.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.sect.game.presentation.theme.SectTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DiscipleCardTest {
    @Test
    fun discipleCard_displaysNameAndRealm() =
        runComposeUiTest {
            setContent {
                SectTheme {
                    DiscipleCard(disciple = TestData.testDisciple(name = "张三"))
                }
            }
            onNodeWithText("张三").assertExists()
            onNodeWithText("LianQi").assertExists()
        }

    @Test
    fun discipleCard_displaysCultivationProgress() =
        runComposeUiTest {
            setContent {
                SectTheme {
                    DiscipleCard(disciple = TestData.testDisciple(cultivationProgress = 75))
                }
            }
            onNodeWithText("75%").assertExists()
        }

    @Test
    fun discipleCard_displaysHighFatigueWarning() =
        runComposeUiTest {
            setContent {
                SectTheme {
                    DiscipleCard(disciple = TestData.testDisciple(fatigue = 80))
                }
            }
            onNodeWithText("疲劳").assertExists()
        }

    @Test
    fun discipleCard_expandedShowsDetails() =
        runComposeUiTest {
            setContent {
                SectTheme {
                    DiscipleCard(
                        disciple = TestData.testDisciple(),
                        isExpanded = true,
                    )
                }
            }
            onNodeWithText("详细信息").assertExists()
            onNodeWithText("灵根").assertExists()
        }
}
