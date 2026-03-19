package com.sect.game.domain.exception

/**
 * 领域异常根接口
 * 所有领域异常都必须实现此接口
 */
sealed interface DomainException {
    val message: String
    val userMessage: String
}

/**
 * 将异常转换为用户友好的中文消息
 */
fun DomainException.toUserMessage(): String = userMessage

/**
 * 领域异常基类
 */
abstract class DomainExceptionBase(
    final override val message: String,
    final override val userMessage: String
) : DomainException, Exception(message)
