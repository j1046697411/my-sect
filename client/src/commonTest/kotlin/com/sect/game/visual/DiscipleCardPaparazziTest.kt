package com.sect.game.visual

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.cash.paparazzi.Paparazzi
import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm
import com.sect.game.presentation.DiscipleCard
import org.junit.Rule
import org.junit.Test

class DiscipleCardPaparazziTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun paparazzi_discipleCard_defaultState() {
        paparazzi.snapshot {
            MaterialTheme {
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
    }

    @Test
    fun paparazzi_discipleCard_cultivatingState() {
        paparazzi.snapshot {
            MaterialTheme {
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
    }

    @Test
    fun paparazzi_discipleCard_exhaustedState() {
        paparazzi.snapshot {
            MaterialTheme {
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
    }

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
}
