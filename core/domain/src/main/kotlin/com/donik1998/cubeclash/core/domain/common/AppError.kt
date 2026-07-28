package com.donik1998.cubeclash.core.domain.common

/**
 * Every failure the app can show a human.
 *
 * The data layer maps HTTP status codes and the API's `{ error: { code, message } }` envelope
 * onto these; nothing above the data layer ever sees an `IOException` or a status code.
 */
sealed class AppError(open val message: String?, open val cause: Throwable? = null) {

    /** No usable connection, or the request never got an answer. */
    data class Network(override val cause: Throwable? = null) :
        AppError("Can't reach CubeClash right now.", cause)

    /** 401 after a refresh attempt already failed — the session is genuinely over. */
    data object Unauthorized : AppError("Your session expired. Sign in again.")

    data class NotFound(override val message: String? = "Not found.") : AppError(message)

    /** 400/422 — the server rejected the body. [fields] carries the API's `details`. */
    data class Validation(
        override val message: String?,
        val fields: Map<String, String> = emptyMap(),
    ) : AppError(message)

    data class Conflict(override val message: String?) : AppError(message)

    data class RateLimited(val retryAfterSeconds: Int?) :
        AppError("Too many attempts. Try again shortly.")

    /** 5xx, or anything the client did not anticipate. */
    data class Unexpected(
        override val message: String? = "Something went wrong.",
        override val cause: Throwable? = null,
    ) : AppError(message, cause)
}
