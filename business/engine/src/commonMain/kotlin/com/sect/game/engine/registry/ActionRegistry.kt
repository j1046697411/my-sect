package com.sect.game.engine.registry

import com.sect.game.goap.actions.Action
import com.sect.game.goap.goals.Goal

/**
 * 注册表接口，用于存储和管理可用的行动和目标
 */
interface ActionRegistry {
    /**
     * 获取所有可用的行动
     */
    fun getAllActions(): List<Action>
    
    /**
     * 获取所有可用的目标
     */
    fun getAllGoals(): List<Goal>
    
    /**
     * 注册行动
     */
    fun registerAction(action: Action)
    
    /**
     * 注册目标
     */
    fun registerGoal(goal: Goal)
}

/**
 * 默认行动注册表实现
 */
class DefaultActionRegistry : ActionRegistry {
    private val _actions = mutableListOf<Action>()
    private val _goals = mutableListOf<Goal>()
    
    override fun getAllActions(): List<Action> = _actions.toList()
    
    override fun getAllGoals(): List<Goal> = _goals.toList()
    
    override fun registerAction(action: Action) {
        _actions.add(action)
    }
    
    override fun registerGoal(goal: Goal) {
        _goals.add(goal)
    }
}
