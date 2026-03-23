package com.sect.game.mvi.base

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

/**
 * MVI 契约基础接口
 *
 * 提供项目级别的 MVI 标记接口，简化各模块的契约定义。
 *
 * @see MviContractState 状态标记接口
 * @see MviContractIntent 意图标记接口
 * @see MviContractAction 动作标记接口
 * @see MviContract 组合契约标记接口
 */

/**
 * MVI 状态标记接口
 *
 * 用于标记实现 MVIState 的状态类，确保类型安全。
 * 所有游戏状态类都应实现此接口。
 *
 * @see MVIState
 */
public interface MviContractState : MVIState

/**
 * MVI 意图标记接口
 *
 * 用于标记实现 MVIIntent 的意图接口。
 * 所有用户操作或系统事件都应定义在此接口中。
 *
 * @see MVIIntent
 */
public interface MviContractIntent : MVIIntent

/**
 * MVI 动作标记接口
 *
 * 用于标记实现 MVIAction 的动作接口。
 * 所有副作用（如弹窗、导航）都应定义在此接口中。
 *
 * @see MVIAction
 */
public interface MviContractAction : MVIAction

/**
 * MVI 组合契约标记接口
 *
 * 组合三个 MVI 核心接口，方便统一管理。
 * 用于容器和 DSL 构建器中。
 *
 * @param S 状态类型
 * @param I 意图类型
 * @param A 动作类型
 *
 * @see MviContractState
 * @see MviContractIntent
 * @see MviContractAction
 */
public interface MviContract<S : MviContractState, I : MviContractIntent, A : MviContractAction>
