package zechs.zplex.ui.login

import androidx.annotation.StringRes

sealed class LoginEvent {
    data class ShowError(
        @param:StringRes val messageRes: Int,
        val args: List<Any> = emptyList()
    ) : LoginEvent()

    object LoginSuccessButNoCapability : LoginEvent()
    object LoginSuccess : LoginEvent()
}