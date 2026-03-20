package com.sect.game.goap.core

data class ModifyEffect(
    val key: String,
    val delta: Int
) : Effect {
    override fun apply(state: WorldState): WorldState {
        val currentValue = state.getValue(key) ?: 0
        return state.withValue(key, currentValue + delta)
    }
    
    companion object {
        operator fun invoke(key: String, delta: Int): ModifyEffect = ModifyEffect(key, delta)
    }
}
