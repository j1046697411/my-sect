package com.sect.game.mvi.extensions

import com.sect.game.mvi.GameErrorHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

inline fun <reified S> StateFlow<S>.updateStateOf(noinline update: (S) -> S): S {
    val currentValue = value
    return update(currentValue)
}

inline fun <reified S, T> StateFlow<S>.subscribeTo(crossinline transform: (S) -> T): Flow<T> = map(transform)

fun <T> Result<T>.toUserMessage(): String {
    return fold(
        onSuccess = { "操作成功" },
        onFailure = { GameErrorHandler.mapToUserMessage(it) },
    )
}

typealias DefaultRetryConfig = GameErrorHandler.DefaultRetryConfig

typealias RetryConfigAlias = GameErrorHandler.RetryConfig
