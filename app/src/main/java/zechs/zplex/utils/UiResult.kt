package zechs.zplex.utils

sealed class UiResult<T> {

    class Idle<T> : UiResult<T>()

    data class Loading<T>(
        val data: List<T> = emptyList()
    ) : UiResult<T>()

    data class Success<T>(
        val data: List<T>,
        val endReached: Boolean
    ) : UiResult<T>()

    data class Error<T>(
        val message: String,
        val data: List<T> = emptyList()
    ) : UiResult<T>()
}