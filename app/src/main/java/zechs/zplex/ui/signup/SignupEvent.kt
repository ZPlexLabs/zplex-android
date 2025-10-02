package zechs.zplex.ui.signup

import androidx.annotation.StringRes

sealed class SignupEvent {
    data class ShowError(
        @param:StringRes val messageRes: Int,
        val args: List<Any> = emptyList()
    ) : SignupEvent()

    object SignupSuccessful : SignupEvent()
}