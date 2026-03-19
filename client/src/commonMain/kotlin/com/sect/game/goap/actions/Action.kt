package com.sect.game.goap.actions

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.Effect
import com.sect.game.goap.core.WorldState

interface Action {
    val id: String
    val name: String
    val preconditions: List<Condition>
    val effects: List<Effect>
    val cost: Int
    
    fun isValid(worldState: WorldState): Boolean {
        return preconditions.all { it.isSatisfiedBy(worldState) }
    }
    
    fun applyEffects(worldState: WorldState): WorldState {
        return effects.fold(worldState) { state, effect -> effect.apply(state) }
    }
}
