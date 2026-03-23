package com.sect.game.visual

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import app.cash.paparazzi.Paparazzi
import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm
import com.sect.game.feature.game.container.GameContainer
import com.sect.game.feature.game.contract.GameState
import com.sect.game.feature.game.presentation.GameScreen
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class GameScreenPaparazziTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun paparazzi_gameScreen_loadingState() {
        paparazzi.snapshot {
            MaterialTheme {
                Surface {
                    GameScreen(
                        container = createMockContainer(isLoading = true)
                    )
                }
            }
        }
    }

    @Test
    fun paparazzi_gameScreen_errorState() {
        paparazzi.snapshot {
            MaterialTheme {
                Surface {
                    GameScreen(
                        container = createMockContainer(error = "网络连接失败")
                    )
                }
            }
        }
    }

    @Test
    fun paparazzi_gameScreen_emptyState() {
        paparazzi.snapshot {
            MaterialTheme {
                Surface {
                    GameScreen(
                        container = createMockContainer(disciples = emptyList())
                    )
                }
            }
        }
    }

    @Test
    fun paparazzi_gameScreen_defaultState() {
        paparazzi.snapshot {
            MaterialTheme {
                Surface {
                    GameScreen(
                        container = createMockContainer(disciples = listOf(
                            createTestDisciple("弟子甲", Realm.LianQi),
                            createTestDisciple("弟子乙", Realm.ZhuJi),
                            createTestDisciple("弟子丙", Realm.JinDan),
                        ))
                    )
                }
            }
        }
    }

    private fun createMockContainer(
        isLoading: Boolean = false,
        error: String? = null,
        disciples: List<Disciple> = emptyList(),
    ): GameContainer {
        return TestGameContainer(
            GameState(
                sectName = "测试宗门",
                disciples = disciples,
                isLoading = isLoading,
                error = error,
            )
        )
    }

    private fun createTestDisciple(
        name: String,
        realm: Realm,
    ): Disciple {
        return Disciple(
            id = DiscipleId("test-$name"),
            name = name,
            realm = realm,
            attributes = Attributes.DEFAULT,
            cultivationProgress = 50,
            fatigue = 20,
            health = 100,
            lifespan = 100,
        )
    }
}

private class TestGameContainer(
    initialState: GameState,
) : GameContainer() {
    private val _state = MutableStateFlow(initialState)
    override val state: MutableStateFlow<GameState> = _state
    
    fun updateState(state: GameState) {
        _state.value = state
    }
}
