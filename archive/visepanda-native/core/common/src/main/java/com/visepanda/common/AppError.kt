package com.visepanda.common

sealed class AppError {
    abstract val displayMessage: String

    data class Network(val cause: Throwable) : AppError() {
        override val displayMessage: String get() = "Network error: ${cause.localizedMessage ?: "Unknown"}"
    }
    data class Server(val code: Int, val detail: String) : AppError() {
        override val displayMessage: String get() = detail
    }
    data class Parse(val cause: Throwable) : AppError() {
        override val displayMessage: String get() = "Data parse error"
    }
    data object EmptyData : AppError() {
        override val displayMessage: String get() = "No data available"
    }
    data class Unknown(val detail: String) : AppError() {
        override val displayMessage: String get() = detail
    }
}
