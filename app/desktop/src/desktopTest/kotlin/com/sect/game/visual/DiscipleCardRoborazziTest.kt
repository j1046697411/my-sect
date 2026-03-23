package com.sect.game.visual

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm
import com.sect.game.presentation.DiscipleCard
import com.sect.game.presentation.theme.SectTheme
import io.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import androidx.compose.ui.test.runDesktopComposeUiTest

class DiscipleCardRoborazziTest {

    private fun createTestDisciple(
        id: DiscipleId = DiscipleId("test-id"),
        name: String = "测试弟子",
        realm: Realm = Realm.LianQi,
        attributes: Attributes = Attributes.DEFAULT,
        cultivationProgress: Int = 0,
        fatigue: Int = 0,
        health: Int = 100,
        lifespan: Int = 100,
    ): Disciple {
        return Disciple(
            id = id,
            name = name,
            realm = realm,
            attributes = attributes,
            cultivationProgress = cultivationProgress,
            fatigue = fatigue,
            health = health,
            lifespan = lifespan,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun discipleCardDefaultState() = runDesktopComposeUiTest {
        setContent {
            SectTheme {
                Surface {
                    DiscipleCard(
                        disciple = createTestDisciple(
                            name = "张三",
                            realm = Realm.LianQi,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        onRoot().captureRoboImage()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun discipleCardCultivatingState() = runDesktopComposeUiTest {
        setContent {
            SectTheme {
                Surface {
                    DiscipleCard(
                        disciple = createTestDisciple(
                            name = "李四",
                            realm = Realm.ZhuJi,
                            cultivationProgress = 65,
                        ),
                        currentAction = "修炼中",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        onRoot().captureRoboImage()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun discipleCardExhaustedState() = runDesktopComposeUiTest {
        setContent {
            SectTheme {
                Surface {
                    DiscipleCard(
                        disciple = createTestDisciple(
                            name = "王五",
                            realm = Realm.JinDan,
                            fatigue = 100,
                        ),
                        currentAction = "疲劳过度",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
