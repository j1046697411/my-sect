package com.sect.game.goap.actions

import com.sect.game.goap.core.ModifyEffect
import com.sect.game.goap.core.WorldState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActionTest {
    
    @Test
    fun cultivateAction_validWhenFatigueBelow80() {
        val state = WorldState.fromMap(mapOf("fatigue" to 50, "health" to 50))
        assertTrue(CultivateAction.isValid(state))
    }
    
    @Test
    fun cultivateAction_invalidWhenFatigue80OrHigher() {
        val state = WorldState.fromMap(mapOf("fatigue" to 80, "health" to 50))
        assertFalse(CultivateAction.isValid(state))
    }
    
    @Test
    fun cultivateAction_effectsCultivationProgressAndStats() {
        val state = WorldState.fromMap(mapOf(
            "fatigue" to 50,
            "health" to 50,
            "cultivationProgress" to 0
        ))
        val newState = CultivateAction.applyEffects(state)
        assertEquals(10, newState.getValue("cultivationProgress"))
        assertEquals(65, newState.getValue("fatigue"))
        assertEquals(45, newState.getValue("health"))
    }
    
    @Test
    fun restAction_validWhenHealthBelow100() {
        val state = WorldState.fromMap(mapOf("health" to 80, "fatigue" to 10))
        assertTrue(RestAction.isValid(state))
    }
    
    @Test
    fun restAction_validWhenFatigueAbove20() {
        val state = WorldState.fromMap(mapOf("health" to 100, "fatigue" to 30))
        assertTrue(RestAction.isValid(state))
    }
    
    @Test
    fun restAction_invalidWhenHealthyAndRested() {
        val state = WorldState.fromMap(mapOf("health" to 100, "fatigue" to 10))
        assertFalse(RestAction.isValid(state))
    }
    
    @Test
    fun restAction_effectsReduceFatigueIncreaseHealth() {
        val state = WorldState.fromMap(mapOf(
            "health" to 80,
            "fatigue" to 50
        ))
        val newState = RestAction.applyEffects(state)
        assertEquals(90, newState.getValue("health"))
        assertEquals(20, newState.getValue("fatigue"))
    }
    
    @Test
    fun breakthroughAction_validWhenCultivationProgressAt100() {
        val state = WorldState.fromMap(mapOf("cultivationProgress" to 100))
        assertTrue(BreakthroughAction.isValid(state))
    }
    
    @Test
    fun breakthroughAction_invalidWhenCultivationProgressBelow100() {
        val state = WorldState.fromMap(mapOf("cultivationProgress" to 99))
        assertFalse(BreakthroughAction.isValid(state))
    }
    
    @Test
    fun breakthroughAction_effectsRealmAdvancement() {
        val state = WorldState.fromMap(mapOf(
            "cultivationProgress" to 100,
            "realm" to 1
        ))
        val newState = BreakthroughAction.applyEffects(state)
        assertEquals(2, newState.getValue("realm"))
        assertEquals(0, newState.getValue("cultivationProgress"))
    }
    
    @Test
    fun gatherAction_validWhenHealthAbove30() {
        val state = WorldState.fromMap(mapOf("health" to 50))
        assertTrue(GatherAction.isValid(state))
    }
    
    @Test
    fun gatherAction_invalidWhenHealth30OrLower() {
        val state = WorldState.fromMap(mapOf("health" to 30))
        assertFalse(GatherAction.isValid(state))
    }
    
    @Test
    fun gatherAction_effectsResourcesAndFatigue() {
        val state = WorldState.fromMap(mapOf(
            "resources" to 0,
            "fatigue" to 0,
            "health" to 50
        ))
        val newState = GatherAction.applyEffects(state)
        assertEquals(5, newState.getValue("resources"))
        assertEquals(10, newState.getValue("fatigue"))
    }
    
    @Test
    fun alchemyAction_validWhenResourcesAtLeast10AndHealthAbove50() {
        val state = WorldState.fromMap(mapOf("resources" to 15, "health" to 60))
        assertTrue(AlchemyAction.isValid(state))
    }
    
    @Test
    fun alchemyAction_invalidWhenResourcesBelow10() {
        val state = WorldState.fromMap(mapOf("resources" to 9, "health" to 60))
        assertFalse(AlchemyAction.isValid(state))
    }
    
    @Test
    fun alchemyAction_invalidWhenHealth50OrLower() {
        val state = WorldState.fromMap(mapOf("resources" to 15, "health" to 50))
        assertFalse(AlchemyAction.isValid(state))
    }
    
    @Test
    fun alchemyAction_effectsResourcesAndCultivationProgress() {
        val state = WorldState.fromMap(mapOf(
            "resources" to 20,
            "cultivationProgress" to 0,
            "health" to 60
        ))
        val newState = AlchemyAction.applyEffects(state)
        assertEquals(10, newState.getValue("resources"))
        assertEquals(20, newState.getValue("cultivationProgress"))
    }
    
    @Test
    fun actionPackage_containsAllFiveActions() {
        val actions = CultivationActionPackage.actions
        assertEquals(5, actions.size)
        assertTrue(actions.any { it.id == "cultivate" })
        assertTrue(actions.any { it.id == "rest" })
        assertTrue(actions.any { it.id == "breakthrough" })
        assertTrue(actions.any { it.id == "gather" })
        assertTrue(actions.any { it.id == "alchemy" })
    }
    
    @Test
    fun action_costsAreCorrect() {
        assertEquals(1, CultivateAction.cost)
        assertEquals(1, RestAction.cost)
        assertEquals(5, BreakthroughAction.cost)
        assertEquals(2, GatherAction.cost)
        assertEquals(3, AlchemyAction.cost)
    }
}
