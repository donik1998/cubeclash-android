package com.donik1998.cubeclash.core.network

import com.donik1998.cubeclash.core.domain.common.AppError
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.network.dto.ErrorEnvelope
import java.io.IOException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/**
 * The border where transport failures stop being transport failures.
 *
 * Above this line nothing knows what a status code is — the API's
 * `{ error: { code, message, details } }` envelope and every `IOException` land as an
 * [AppError] the UI can render directly.
 */
class ApiErrorMapper(private val json: Json) {

    fun map(throwable: Throwable): AppError = when (throwable) {
        is IOException -> AppError.Network(throwable)

        is HttpException -> {
            val body = runCatching { throwable.response()?.errorBody()?.string() }.getOrNull()
            val envelope = body?.let { runCatching { json.decodeFromString<ErrorEnvelope>(it) }.getOrNull() }
            val message = envelope?.error?.message

            when (throwable.code()) {
                401 -> AppError.Unauthorized
                403 -> AppError.Validation(message ?: "You can't do that.")
                404 -> AppError.NotFound(message)
                409 -> AppError.Conflict(message)
                400, 422 -> AppError.Validation(message, envelope?.error?.details.orEmpty())
                429 -> AppError.RateLimited(
                    throwable.response()?.headers()?.get("Retry-After")?.toIntOrNull(),
                )

                else -> AppError.Unexpected(message, throwable)
            }
        }

        else -> AppError.Unexpected(cause = throwable)
    }
}

/** Runs a network call and lands both arms of the outcome in a [DataResult]. */
suspend inline fun <T> ApiErrorMapper.call(block: () -> T): DataResult<T> = try {
    DataResult.Success(block())
} catch (throwable: Throwable) {
    if (throwable is kotlinx.coroutines.CancellationException) throw throwable
    DataResult.Failure(map(throwable))
}
