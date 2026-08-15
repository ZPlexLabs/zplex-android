package zechs.zplex.common.ui.mvi

import zechs.zplex.common.utils.Result

/** UI-facing error carrying a headline message and optional detail. */
data class UiError(
    val message: String,
    val details: String? = null
)

fun Result.Error.toUiError() = UiError(message = message, details = details)

inline fun <T> Result<T>.fold(
    onSuccess: (T) -> Unit,
    onError: (UiError) -> Unit
) {
    when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Error -> onError(toUiError())
    }
}
