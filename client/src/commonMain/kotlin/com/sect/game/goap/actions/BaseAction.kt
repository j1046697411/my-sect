package com.sect.game.goap.actions

import com.sect.game.goap.core.Condition
import com.sect.game.goap.core.Effect

abstract class BaseAction(
    override val id: String,
    override val name: String,
    override val preconditions: List<Condition>,
    override val effects: List<Effect>,
    override val cost: Int
) : Action

class ActionBuilder(
    private val id: String,
    private val name: String,
    private var cost: Int = 1
) {
    private var preconditions: MutableList<Condition> = mutableListOf()
    private var effects: MutableList<Effect> = mutableListOf()
    
    fun withPrecondition(condition: Condition): ActionBuilder {
        preconditions.add(condition)
        return this
    }
    
    fun withEffect(effect: Effect): ActionBuilder {
        effects.add(effect)
        return this
    }
    
    fun withCost(newCost: Int): ActionBuilder {
        cost = newCost
        return this
    }
    
    fun build(): Action = object : Action {
        override val id: String = this@ActionBuilder.id
        override val name: String = this@ActionBuilder.name
        override val preconditions: List<Condition> = this@ActionBuilder.preconditions.toList()
        override val effects: List<Effect> = this@ActionBuilder.effects.toList()
        override val cost: Int = this@ActionBuilder.cost
    }
}

fun action(id: String, name: String, cost: Int = 1, block: ActionBuilder.() -> Unit): Action {
    return ActionBuilder(id, name, cost).apply(block).build()
}
