package com.sect.game.mvi

import com.sect.game.domain.exception.CultivationException
import com.sect.game.domain.exception.DomainException
import com.sect.game.domain.exception.GoapException
import com.sect.game.domain.exception.SectException
import com.sect.game.domain.exception.StorageException
import com.sect.game.domain.exception.toUserMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

object GameErrorHandler {

    private const val MAX_RETRY_ATTEMPTS = 3
    private const val RETRY_DELAY_MS = 100L

    interface RetryConfig {
        val maxAttempts: Int
        val delayMs: Long
    }

    data class DefaultRetryConfig(
        override val maxAttempts: Int = MAX_RETRY_ATTEMPTS,
        override val delayMs: Long = RETRY_DELAY_MS
    ) : RetryConfig

    suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        onError: (Throwable) -> Unit = {},
        retryConfig: RetryConfig = DefaultRetryConfig()
    ): Result<T> = withContext(Dispatchers.Default) {
        var lastError: Throwable? = null
        var attempt = 0

        while (attempt < retryConfig.maxAttempts) {
            attempt++
            try {
                val result = operation()
                return@withContext Result.success(result)
            } catch (e: Throwable) {
                lastError = e
                logError(e, attempt)
                onError(e)
                if (attempt < retryConfig.maxAttempts) {
                    kotlinx.coroutines.delay(retryConfig.delayMs * attempt)
                }
            }
        }
        Result.failure(lastError ?: IllegalStateException("Unknown error after $attempt attempts"))
    }

    fun mapToUserMessage(error: Throwable): String {
        return when (error) {
            is DomainException -> error.toUserMessage()
            is CultivationException -> error.userMessage
            is SectException -> error.userMessage
            is StorageException -> error.userMessage
            is GoapException -> error.userMessage
            else -> "操作失败，请稍后重试"
        }
    }

    fun logError(error: Throwable, attempt: Int = 1) {
        val prefix = if (attempt > 1) "[Retry $attempt] " else ""
        System.err.println("${prefix}Error: ${error.message}")
        error.printStackTrace()
    }

    fun shouldRetry(error: Throwable): Boolean {
        return when (error) {
            is StorageException.SaveFailedException -> true
            is StorageException.LoadFailedException -> true
            is GoapException.PlanningFailedException -> false
            is CultivationException.MaxRealmReachedException -> false
            is SectException.AtCapacityException -> false
            else -> true
        }
    }

    fun getRetryStrategy(error: Throwable): RetryConfig {
        return when (error) {
            is StorageException.SaveFailedException -> DefaultRetryConfig(maxAttempts = 5, delayMs = 200L)
            is StorageException.LoadFailedException -> DefaultRetryConfig(maxAttempts = 3, delayMs = 500L)
            is GoapException.PlanningFailedException -> DefaultRetryConfig(maxAttempts = 1, delayMs = 0L)
            else -> DefaultRetryConfig()
        }
    }
}

fun <T> Result<T>.toUserMessage(): String {
    return fold(
        onSuccess = { "操作成功" },
        onFailure = { GameErrorHandler.mapToUserMessage(it) }
    )
}

fun Throwable.toUserMessage(): String {
    return GameErrorHandler.mapToUserMessage(this)
}
