package com.joma.mediagrab.core.result

sealed interface AppResult<out T> {
    data class Ok<T>(val value: T) : AppResult<T>

    data class Err(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    data object InvalidLink : AppError

    data object UnsupportedPlatform : AppError

    data object UnsupportedContent : AppError

    data object NoStream : AppError

    data class Network(val cause: Throwable? = null) : AppError

    data class Parse(val cause: Throwable? = null) : AppError

    data class Download(val cause: Throwable? = null) : AppError

    data object StorageFull : AppError
}
