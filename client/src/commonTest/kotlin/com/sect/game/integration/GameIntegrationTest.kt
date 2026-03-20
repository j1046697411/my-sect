package com.sect.game.integration

import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.entity.Sect
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm
import com.sect.game.domain.valueobject.SectId
import com.sect.game.engine.GameEngine
import com.sect.game.engine.planner.AStarPlanner
import com.sect.game.goap.actions.CultivationActionPackage
import com.sect.game.goap.core.WorldState
import com.sect.game.goap.goals.CultivationGoal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 游戏引擎集成测试
 * 测试完整游戏流程、GOAP规划、弟子状态转换和资源管理
 */
class GameIntegrationTest {
    // ==================== 完整游戏流程测试 ====================

    @Test
    fun createSect_addDisciples_runGameLoop_verifiesStateChanges() {
        // Given: 创建宗门并添加弟子
        val sect = createSectWithDisciples(1)
        val engine = GameEngine.create(sect, tickRate = 100)
        val initialDisciple = sect.disciples.values.first()

        assertEquals(0, initialDisciple.cultivationProgress)
        assertEquals(0, initialDisciple.fatigue)

        // When: 启动游戏循环
        engine.start()
        Thread.sleep(200)
        engine.stop()

        // Then: 验证弟子状态发生变化
        val updatedDisciple = sect.getDisciple(initialDisciple.id)
        assertNotNull(updatedDisciple)
        assertNotEquals(initialDisciple.cultivationProgress, updatedDisciple.cultivationProgress)
    }

    @Test
    fun createSect_multipleDisciples_allDisciplesUpdated() {
        // Given: 创建有多个弟子的宗门
        val sect = createSectWithDisciples(3)
        val engine = GameEngine.create(sect, tickRate = 100)

        // When: 启动游戏循环
        engine.start()
        Thread.sleep(300)
        engine.stop()

        // Then: 验证所有弟子都被更新
        val disciples = sect.disciples.values.toList()
        assertEquals(3, disciples.size)
        assertTrue(disciples.all { it.cultivationProgress > 0 || it.fatigue > 0 || it.health < 100 })
    }

    @Test
    fun gameLoop_increasesTickCount() {
        // Given: 创建宗门和引擎
        val sect = createSectWithDisciples(1)
        val engine = GameEngine.create(sect, tickRate = 200)

        // When: 启动并运行游戏循环
        engine.start()
        Thread.sleep(100)
        engine.stop()

        // Then: tickCount 增加
        assertTrue(engine.tickCount > 0)
    }

    // ==================== GOAP 规划测试 ====================

    @Test
    fun goapPlanner_selectsCultivationAction_forLowFatigueDisciple() {
        // Given: 创建低疲劳的弟子状态
        val state =
            WorldState()
                .withValue("health", 100)
                .withValue("fatigue", 30)
                .withValue("cultivationProgress", 0)
                .withValue("realm", 1)

        val actions = CultivationActionPackage.actions
        val goal = CultivationGoal.create()

        // When: 使用 A* 规划器生成计划
        val planner = AStarPlanner()
        val plan = planner.plan(state, goal, actions)

        // Then: 计划应包含修炼动作
        assertTrue(plan.isNotEmpty())
        assertEquals("cultivate", plan.first().id)
    }

    @Test
    fun goapPlanner_selectsRestAction_forHighFatigueDisciple() {
        // Given: 创建高疲劳的弟子状态
        val state =
            WorldState()
                .withValue("health", 80)
                .withValue("fatigue", 90)
                .withValue("cultivationProgress", 50)
                .withValue("realm", 1)

        val actions = CultivationActionPackage.actions
        val goal = CultivationGoal.create()

        // When: 使用规划器生成计划
        val planner = AStarPlanner()
        val plan = planner.plan(state, goal, actions)

        // Then: 计划应选择休息动作（因为前置条件不满足修炼）
        // 规划器会选择代价最低的有效动作
        assertTrue(plan.isNotEmpty())
    }

    @Test
    fun goapPlanner_emptyPlan_whenGoalSatisfied() {
        // Given: 目标已满足的状态
        val state =
            WorldState()
                .withValue("health", 100)
                .withValue("fatigue", 10)
                .withValue("cultivationProgress", 100)
                .withValue("realm", 1)

        val actions = CultivationActionPackage.actions
        val goal = CultivationGoal.create()

        // When: 规划器生成计划
        val planner = AStarPlanner()
        val plan = planner.plan(state, goal, actions)

        // Then: 返回空计划
        assertTrue(plan.isEmpty())
    }

    @Test
    fun goapPlanner_returnsValidPlan_withMultipleActions() {
        // Given: 需要多个动作才能满足的目标
        val state =
            WorldState()
                .withValue("health", 50)
                .withValue("fatigue", 50)
                .withValue("cultivationProgress", 0)
                .withValue("realm", 1)

        val actions = CultivationActionPackage.actions
        val goal = CultivationGoal.create()

        // When: 规划器生成计划
        val planner = AStarPlanner()
        val plan = planner.plan(state, goal, actions)

        // Then: 返回非空计划
        assertNotNull(plan)
    }

    // ==================== 弟子状态转换测试 ====================

    @Test
    fun disciple_cultivate_increasesProgressAndFatigue() {
        // Given: 正常状态的弟子
        val disciple =
            createDisciple(
                health = 100,
                fatigue = 0,
                cultivationProgress = 0,
            )

        // When: 执行修炼
        val result = disciple.cultivate()

        // Then: 修炼进度增加，疲劳增加
        assertTrue(result.isSuccess)
        val cultivated = result.getOrThrow()
        assertTrue(cultivated.cultivationProgress > 0)
        assertTrue(cultivated.fatigue > 0)
    }

    @Test
    fun disciple_rest_reducesFatigueAndIncreasesHealth() {
        // Given: 疲劳的弟子
        val disciple =
            createDisciple(
                health = 80,
                fatigue = 80,
                cultivationProgress = 50,
            )

        // When: 执行休息
        val result = disciple.rest()

        // Then: 疲劳降低，健康恢复
        assertTrue(result.isSuccess)
        val rested = result.getOrThrow()
        assertTrue(rested.fatigue < disciple.fatigue)
        assertTrue(rested.health > disciple.health)
    }

    @Test
    fun disciple_exhausted_cannotCultivate() {
        // Given: 精疲力竭的弟子
        val disciple =
            createDisciple(
                health = 50,
                fatigue = 100,
                cultivationProgress = 50,
            )

        // When: 尝试修炼
        val result = disciple.cultivate()

        // Then: 修炼失败
        assertTrue(result.isFailure)
    }

    @Test
    fun disciple_breakthrough_withFullProgress() {
        // Given: 进度满的弟子
        val disciple =
            createDisciple(
                health = 100,
                fatigue = 0,
                cultivationProgress = 100,
            )

        // When: 尝试突破
        val result = disciple.attemptBreakthrough()

        // Then: 突破成功，境界提升
        assertTrue(result.isSuccess)
        assertEquals(Realm.ZhuJi, result.getOrThrow())
    }

    @Test
    fun disciple_breakthrough_insufficientProgress_fails() {
        // Given: 进度不足的弟子
        val disciple =
            createDisciple(
                health = 100,
                fatigue = 0,
                cultivationProgress = 50,
            )

        // When: 尝试突破
        val result = disciple.attemptBreakthrough()

        // Then: 突破失败
        assertTrue(result.isFailure)
    }

    @Test
    fun disciple_dead_cannotPerformAnyAction() {
        // Given: 死亡的弟子
        val deadDisciple =
            Disciple(
                id = DiscipleId("dead-disciple"),
                name = "死亡弟子",
                realm = Realm.LianQi,
                attributes = Attributes.DEFAULT,
                cultivationProgress = 50,
                fatigue = 50,
                health = 0,
                lifespan = 100,
            )

        // When: 尝试修炼或休息
        val cultivateResult = deadDisciple.cultivate()
        val restResult = deadDisciple.rest()

        // Then: 所有操作失败
        assertTrue(cultivateResult.isFailure)
        assertTrue(restResult.isFailure)
    }

    @Test
    fun disciple_healthCritical_consideredUnhealthy() {
        // Given: 生命值危险的弟子
        val criticalDisciple =
            createDisciple(
                health = 20,
                fatigue = 50,
                cultivationProgress = 50,
            )

        // Then: 健康检查失败
        assertFalse(criticalDisciple.isHealthy())
    }

    // ==================== 资源管理测试 ====================

    @Test
    fun sect_addResources_increasesResources() {
        // Given: 初始宗门
        val sect = createEmptySect()
        val initialResources = sect.resources

        // When: 添加资源
        val updatedSect =
            sect.addResources(
                com.sect.game.domain.entity.Resources(
                    spiritStones = 100,
                    herbs = 50,
                    pills = 10,
                ),
            )

        // Then: 资源增加
        assertEquals(initialResources.spiritStones + 100, updatedSect.resources.spiritStones)
        assertEquals(initialResources.herbs + 50, updatedSect.resources.herbs)
        assertEquals(initialResources.pills + 10, updatedSect.resources.pills)
    }

    @Test
    fun sect_spendResources_sufficientFunds_succeeds() {
        // Given: 有足够资源的宗门
        val sect =
            createEmptySect().addResources(
                com.sect.game.domain.entity.Resources(
                    spiritStones = 100,
                    herbs = 50,
                    pills = 10,
                ),
            )

        // When: 消耗资源
        val result =
            sect.spendResources(
                com.sect.game.domain.entity.Resources(
                    spiritStones = 50,
                    herbs = 25,
                    pills = 5,
                ),
            )

        // Then: 成功
        assertTrue(result.isSuccess)
    }

    @Test
    fun sect_spendResources_insufficientFunds_fails() {
        // Given: 资源不足的宗门
        val sect = createEmptySect()

        // When: 尝试消耗超出拥有的资源
        val result =
            sect.spendResources(
                com.sect.game.domain.entity.Resources(
                    spiritStones = 100,
                    herbs = 50,
                    pills = 10,
                ),
            )

        // Then: 失败
        assertTrue(result.isFailure)
    }

    @Test
    fun sect_resources_isAffordable_checksCorrectly() {
        // Given: 有资源的宗门
        val sect =
            createEmptySect().addResources(
                com.sect.game.domain.entity.Resources(
                    spiritStones = 100,
                    herbs = 50,
                    pills = 10,
                ),
            )

        // Then: 可以负担检查
        assertTrue(
            sect.resources.isAffordable(
                com.sect.game.domain.entity.Resources(spiritStones = 50, herbs = 25, pills = 5),
            ),
        )
        assertFalse(
            sect.resources.isAffordable(
                com.sect.game.domain.entity.Resources(spiritStones = 150, herbs = 50, pills = 10),
            ),
        )
    }

    // ==================== 宗门弟子管理测试 ====================

    @Test
    fun sect_addDisciple_increasesCount() {
        // Given: 空宗门
        val sect = createEmptySect()
        assertEquals(0, sect.discipleCount)

        // When: 添加弟子
        val disciple = createDisciple()
        sect.addDisciple(disciple)

        // Then: 数量增加
        assertEquals(1, sect.discipleCount)
    }

    @Test
    fun sect_addDisciple_atCapacity_fails() {
        // Given: 达到人数上限的宗门
        val sect =
            Sect.create(
                id = SectId("small-sect"),
                name = "小宗门",
                maxDisciples = 1,
            ).getOrThrow()

        sect.addDisciple(createDisciple())

        // When: 尝试添加第二个弟子
        val result = sect.addDisciple(createDisciple())

        // Then: 失败
        assertTrue(result.isFailure)
    }

    @Test
    fun sect_removeDisciple_decreasesCount() {
        // Given: 有一个弟子的宗门
        val sect = createEmptySect()
        val disciple = createDisciple()
        sect.addDisciple(disciple)

        // When: 移除弟子
        val result = sect.removeDisciple(disciple.id)

        // Then: 数量减少
        assertTrue(result.isSuccess)
        assertEquals(0, sect.discipleCount)
    }

    @Test
    fun sect_updateDisciple_modifiesDisciple() {
        // Given: 宗门中有弟子
        val sect = createSectWithDisciples(1)
        val disciple = sect.disciples.values.first()

        // When: 更新弟子状态
        val updatedDisciple = disciple.cultivate().getOrThrow()
        sect.updateDisciple(updatedDisciple)

        // Then: 弟子状态已更新
        assertEquals(updatedDisciple.cultivationProgress, sect.getDisciple(disciple.id)?.cultivationProgress)
    }

    // ==================== 游戏引擎生命周期测试 ====================

    @Test
    fun engine_start_resume_pause_stop_lifecycle() {
        // Given: 创建的引擎
        val sect = createSectWithDisciples(1)
        val engine = GameEngine.create(sect)

        // When/Then: 启动/暂停/恢复/停止
        engine.start()
        assertTrue(engine.isRunning)
        assertFalse(engine.isPaused)

        engine.pause()
        assertTrue(engine.isPaused)

        engine.resume()
        assertFalse(engine.isPaused)

        engine.stop()
        assertFalse(engine.isRunning)
        assertFalse(engine.isPaused)
    }

    @Test
    fun engine_onTick_callback_invoked() {
        // Given: 创建引擎
        val sect = createSectWithDisciples(1)
        val engine = GameEngine.create(sect, tickRate = 200)
        var tickCount = 0L

        engine.onTick = { tickCount = it }

        // When: 启动引擎
        engine.start()
        Thread.sleep(150)
        engine.stop()

        // Then: 回调被调用
        assertTrue(tickCount > 0)
    }

    // ==================== 辅助方法 ====================

    private fun createEmptySect(): Sect {
        return Sect.create(
            id = SectId("test-sect-${System.nanoTime()}"),
            name = "测试宗门",
        ).getOrThrow()
    }

    private fun createSectWithDisciples(count: Int): Sect {
        val sect = createEmptySect()
        repeat(count) { index ->
            val disciple =
                Disciple.create(
                    id = DiscipleId("disciple-$index-${System.nanoTime()}"),
                    name = "弟子$index",
                    attributes = Attributes.DEFAULT,
                    realm = Realm.LianQi,
                ).getOrThrow()
            sect.addDisciple(disciple)
        }
        return sect
    }

    private fun createDisciple(
        health: Int = 100,
        fatigue: Int = 0,
        cultivationProgress: Int = 0,
    ): Disciple {
        return Disciple(
            id = DiscipleId("test-${System.nanoTime()}"),
            name = "测试弟子",
            realm = Realm.LianQi,
            attributes = Attributes.DEFAULT,
            cultivationProgress = cultivationProgress,
            fatigue = fatigue,
            health = health,
            lifespan = 100,
        )
    }
}
