package zechs.zplex.common.utils

sealed class CacheResource<out T> {
    data class Loading<T>(val data: T?) : CacheResource<T>()
    data class Success<T>(val data: T) : CacheResource<T>()
    data class Error<T>(
        val message: String,
        val details: String?,
        val data: T?
    ) : CacheResource<T>()
}
