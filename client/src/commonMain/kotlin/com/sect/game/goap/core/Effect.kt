package com.sect.game.goap.core

interface Effect {
    fun apply(state: WorldState): WorldState
}
