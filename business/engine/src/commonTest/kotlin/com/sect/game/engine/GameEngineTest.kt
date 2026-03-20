package com.sect.game.engine

import com.sect.game.domain.entity.Disciple
import com.sect.game.domain.entity.Sect
import com.sect.game.domain.valueobject.Attributes
import com.sect.game.domain.valueobject.DiscipleId
import com.sect.game.domain.valueobject.Realm
import com.sect.game.domain.valueobject.SectId
import com.sect.game.engine.executor.ActionExecutor
import com.sect.game.engine.executor.DefaultActionExecutor
import com.sect.game.engine.planner.GOAPPlanner
import com.sect.game.engine.planner.AStarPlanner
import com.sect.game.engine.registry.ActionRegistry
import com.sect.game.engine.registry.DefaultActionRegistry
import com.sect.game.goap.actions.Action
import com.sect.game.goap.actions.CultivationActionPackage
import com.sect.game.goap.core.WorldState
import com.sect.game.goap.goals.Goal
import com.sect.game.goap.goals.GoalFactoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GameEngineTest {
    
    @Test
    fun create_withValidSect_returnsEngine() {
        val sect = createTestSect()
        val engine = GameEngine.create(sect)
        
        assertEquals(60, engine.tickRate)
        assertFalse(engine.isRunning)
        assertFalse(engine.isPaused)
    }
    
    @Test
    fun start_setsIsRunningToTrue() {
        val sect = createTestSect()
        val engine = GameEngine.create(sect, tickRate = 60)
        
        engine.start()
        
        assertTrue(engine.isRunning)
        assertFalse(engine.isPaused)
        
        engine.stop()
    }
    
    @Test
    fun pause_setsIsPausedToTrue() {
        val sect = createTestSect()
        val engine = GameEngine.create(sect, tickRate = 60)
        
        engine.start()
        engine.pause()
        
        assertTrue(engine.isRunning)
        assertTrue(engine.isPaused)
        
        engine.stop()
    }
    
    @Test
    fun resume_setsIsPausedToFalse() {
        val sect = createTestSect()
        val engine = GameEngine.create(sect, tickRate = 60)
        
        engine.start()
        engine.pause()
        engine.resume()
        
        assertTrue(engine.isRunning)
        assertFalse(engine.isPaused)
        
        engine.stop()
    }
    
    @Test
    fun stop_setsIsRunningToFalse() {
        val sect = createTestSect()
        val engine = GameEngine.create(sect, tickRate = 60)
        
        engine.start()
        engine.stop()
        
        assertFalse(engine.isRunning)
        assertFalse(engine.isPaused)
    }
    
    @Test
    fun tick_increasesTickCount() {
        val sect = createTestSect()
        val engine = GameEngine.create(sect, tickRate = 1000)
        var tickInvoked = false
        
        engine.onTick = { tickInvoked = true }
        engine.start()
        
        Thread.sleep(50)
        
        engine.stop()
        
        assertTrue(tickInvoked)
    }
    
    @Test
    fun updateDisciples_affectsDiscipleState() {
        val sect = createTestSect()
        val engine = GameEngine.create(sect, tickRate = 60)
        
        val discipleBefore = sect.disciples.values.first()
        
        engine.start()
        Thread.sleep(100)
        engine.stop()
        
        val discipleAfter = sect.getDisciple(discipleBefore.id)
        
        assertNotEquals(discipleBefore, discipleAfter)
    }
    
    @Test
    fun create_withCustomTickRate_usesProvidedTickRate() {
        val sect = createTestSect()
        val customTickRate = 30
        val engine = GameEngine.create(sect, tickRate = customTickRate)
        
        assertEquals(customTickRate, engine.tickRate)
    }
    
    @Test
    fun engine_withCustomPlanner_usesProvidedPlanner() {
        val sect = createTestSect()
        val customPlanner = TestGOAPPlanner()
        val engine = GameEngine.create(
            sect = sect,
            planner = customPlanner
        )
        
        engine.start()
        Thread.sleep(50)
        engine.stop()
        
        assertTrue(customPlanner.planCalled)
    }
    
    @Test
    fun engine_withCustomExecutor_usesProvidedExecutor() {
        val sect = createTestSect()
        val customExecutor = TestActionExecutor()
        val engine = GameEngine.create(
            sect = sect,
            executor = customExecutor
        )
        
        engine.start()
        Thread.sleep(50)
        engine.stop()
        
        assertTrue(customExecutor.executeCalled)
    }
    
    private fun createTestSect(): Sect {
        val sect = Sect.create(
            id = SectId("test-sect"),
            name = "测试宗门"
        ).getOrThrow()
        
        val disciple = Disciple.create(
            id = DiscipleId("test-disciple"),
            name = "测试弟子",
            attributes = Attributes.DEFAULT,
            realm = Realm.炼气
        ).getOrThrow()
        
        sect.addDisciple(disciple)
        
        return sect
    }
    
    private class TestGOAPPlanner : GOAPPlanner {
        var planCalled = false
        
        override fun plan(
            currentState: WorldState,
            goal: Goal,
            availableActions: List<Action>
        ): List<Action> {
            planCalled = true
            return emptyList()
        }
    }
    
    private class TestActionExecutor : ActionExecutor {
        var executeCalled = false
        
        override fun execute(disciple: Disciple, action: Action): Disciple {
            executeCalled = true
            return disciple
        }
    }
}
