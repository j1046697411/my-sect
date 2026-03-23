package com.sect.game.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.sect.game.presentation.theme.SectTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CreateDiscipleDialogTest {
    @Test
    fun dialog_createButtonExistsWhenNameEmpty() =
        runComposeUiTest {
            setContent {
                SectTheme {
                    CreateDiscipleDialog(
                        container = TestData.testContainer(),
                        onDismiss = {},
                    )
                }
            }
            onNodeWithText("创建").assertExists()
        }

    @Test
    fun dialog_createButtonExistsAfterNameInput() =
        runComposeUiTest {
            setContent {
                SectTheme {
                    CreateDiscipleDialog(
                        container = TestData.testContainer(),
                        onDismiss = {},
                    )
                }
            }
            onNodeWithText("弟子姓名").performTextInput("李四")
            onNodeWithText("创建").assertExists()
        }

    @Test
    fun dialog_createButtonExistsAfterRandomClick() =
        runComposeUiTest {
            setContent {
                SectTheme {
                    CreateDiscipleDialog(
                        container = TestData.testContainer(),
                        onDismiss = {},
                    )
                }
            }
            onNodeWithText("随机").performClick()
            onNodeWithText("创建").assertExists()
        }
}
