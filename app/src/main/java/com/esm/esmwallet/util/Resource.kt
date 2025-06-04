package com.esm.esmwallet.util

/**
 * A sealed class to represent the state of data, typically used for network requests or data loading.
 * Provides states for Loading, Success, Error, and Empty.
 */
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T?) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Empty<T> : Resource<T>() // Optional: For initial state or no data
}