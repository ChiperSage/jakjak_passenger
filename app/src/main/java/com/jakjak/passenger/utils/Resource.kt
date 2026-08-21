package com.jakjak.passenger.utils

/**
 * Generic wrapper untuk state UI (Loading / Success / Error).
 */
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()

    val isLoading get() = this is Loading
    val isSuccess get() = this is Success
    val isError   get() = this is Error
}
