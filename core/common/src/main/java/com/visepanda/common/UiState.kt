package com.visepanda.common

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data object Empty : UiState<Nothing>()
    data class Error(val message: String, val retry: (() -> Unit)? = null) : UiState<Nothing>()
}
