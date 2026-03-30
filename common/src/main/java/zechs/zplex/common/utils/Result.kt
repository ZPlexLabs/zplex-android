package zechs.zplex.common.utils

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val details: String? = null) : Result<Nothing>()
}