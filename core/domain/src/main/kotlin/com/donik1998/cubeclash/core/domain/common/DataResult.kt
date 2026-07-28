package com.donik1998.cubeclash.core.domain.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * A two-armed result. Kotlin's own `Result` is deliberately not used: it carries a
 * `Throwable`, and by the time a value reaches the domain the throwable has already been
 * translated into an [AppError] the UI can render.
 */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failure(val error: AppError) : DataResult<Nothing>

    val isSuccess: Boolean get() = this is Success

    fun getOrNull(): T? = (this as? Success)?.data
    fun errorOrNull(): AppError? = (this as? Failure)?.error
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Failure -> this
}

inline fun <T> DataResult<T>.onSuccess(action: (T) -> Unit): DataResult<T> = apply {
    if (this is DataResult.Success) action(data)
}

inline fun <T> DataResult<T>.onFailure(action: (AppError) -> Unit): DataResult<T> = apply {
    if (this is DataResult.Failure) action(error)
}

/** Wraps a cold flow so a thrown exception becomes a [DataResult.Failure] instead of a crash. */
fun <T> Flow<T>.asDataResult(map: (Throwable) -> AppError = { AppError.Unexpected(cause = it) }): Flow<DataResult<T>> =
    map<T, DataResult<T>> { DataResult.Success(it) }.catch { emit(DataResult.Failure(map(it))) }
