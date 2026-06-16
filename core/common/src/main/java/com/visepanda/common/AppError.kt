package com.visepanda.common

sealed class AppError {
    data class Network(val cause: Throwable) : AppError() {
        override val message: String get() = "Network error: ${cause.localizedMessage ?: "Unknown"}"
    }
    data class Server(val code: Int, val message: String) : AppError()
    data class Parse(val cause: Throwable) : AppError() {
        override val message: String get() = "Data parse error"
    }
    data object EmptyData : AppError() {
        override val message: String get() = "No data available"
    }
    data class Unknown(val msg: String) : AppError() {
        override val message: String get() = msg
    }

    open val message: String = "Unknown error"
}
