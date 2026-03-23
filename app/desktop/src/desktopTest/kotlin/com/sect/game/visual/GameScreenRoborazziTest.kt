package com.sect.game.visual

import androidx.compose.material3.Surface
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.entity.Resources
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm
import com.sect.game.feature.game.contract.GameState
import com.sect.game.feature.game.container.GameContainer
import com.sect.game.feature.game.presentation.GameScreen
import com.sect.game.presentation.theme.SectTheme
import io.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import androidx.compose.ui.test.runDesktopComposeUiTest

class GameScreenRoborazziTest {

    private fun createTestDisciple(
        id: String = "test-id",
        name: String = "测试弟子",
        realm: Realm = Realm.LianQi,
        cultivationProgress: Int = 0,
        fatigue: Int = 0,
    ): Disciple {
        return Disciple(
            id = DiscipleId(id),
            name = name,
            realm = realm,
            attributes = Attributes.DEFAULT,
            cultivationProgress = cultivationProgress,
            fatigue = fatigue,
            health = 100,
            lifespan = 100,
        )
    }

    private fun createMockGameContainer(initialState: GameState): GameContainer {
        return object : GameContainer() {
            override val state = MutableStateFlow(initialState)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun gameScreenLoadingState() = runDesktopComposeUiTest {
        setContent {
            SectTheme {
                Surface {
                    GameScreen(
                        container = createMockGameContainer(
                            GameState(isLoading = true)
                        )
                    )
                }
            }
        }
        onRoot().captureRoboImage()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun gameScreenErrorState() = runDesktopComposeUiTest {
        setContent {
            SectTheme {
                Surface {
                    GameScreen(
                        container = createMockGameContainer(
                            GameState(error = "加载失败：宗门数据损坏")
                        )
                    )
                }
            }
        }
        onRoot().captureRoboImage()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun gameScreenEmptyState() = runDesktopComposeUiTest {
        setContent {
            SectTheme {
                Surface {
                    GameScreen(
                        container = createMockGameContainer(
                            GameState(
                                isLoading = false,
                                sectName = "青云宗",
                                resources = Resources.EMPTY,
                                disciples = emptyList(),
                            )
                        )
                    )
                }
            }
        }
        onRoot().captureRoboImage()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun gameScreenDefaultState() = runDesktopComposeUiTest {
        setContent {
            SectTheme {
                Surface {
                    GameScreen(
                        container = createMockGameContainer(
                            GameState(
                                isLoading = false,
                                sectName = "青云宗",
                                resources = Resources(
                                    spiritStones = 1000,
                                    herbs = 50,
                                    pills = 20,
                                ),
                                disciples = listOf(
                                    createTestDisciple(
                                        id = "1",
                                        name = "张三",
                                        realm = Realm.LianQi,
                                    ),
                                    createTestDisciple(
                                        id = "2",
                                        name = "李四",
                                        realm = Realm.ZhuJi,
                                        cultivationProgress = 65,
                                    ),
                                    createTestDisciple(
                                        id = "3",
                                        name = "王五",
                                        realm = Realm.JinDan,
                                        fatigue = 100,
                                    ),
                                ),
                                tickCount = 12345,
                                isPaused = false,
                            )
                        )
                    )
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
