package zechs.zplex.ui.server

import androidx.annotation.StringRes

sealed class ServerEvent {
    data class ShowError(
        @param:StringRes val messageRes: Int,
        val args: List<Any> = emptyList()
    ) : ServerEvent()

    object ConnectionSuccessful : ServerEvent()
}