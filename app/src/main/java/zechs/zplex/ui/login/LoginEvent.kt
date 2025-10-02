package zechs.zplex.ui.login

import androidx.annotation.StringRes
import zechs.zplex.data.model.LoginSuccessResponse

sealed class LoginEvent {
    data class ShowError(
        @param:StringRes val messageRes: Int,
        val args: List<Any> = emptyList()
    ) : LoginEvent()

    data class LoginSuccess(val data: LoginSuccessResponse) : LoginEvent()
}