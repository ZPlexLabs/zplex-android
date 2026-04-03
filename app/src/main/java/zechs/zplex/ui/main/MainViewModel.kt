package zechs.zplex.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import zechs.zplex.zplex_api.data.local.auth.AuthState
import zechs.zplex.zplex_api.data.local.session.SessionStorage
import zechs.zplex.zplex_api.data.local.user.UserStorage
import zechs.zplex.zplex_api.utils.TokenValidator
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userStorage: UserStorage,
    sessionStorage: SessionStorage,
    private val tokenValidator: TokenValidator
) : ViewModel() {

    companion object {
        private const val TAG = "ZPlexViewModel"
    }

    val authState: StateFlow<AuthState> =
        combine(
            userStorage.userFlow(),
            sessionStorage.accessTokenFlow(),
            sessionStorage.refreshTokenFlow()
        ) { user, accessToken, refreshToken ->

            Log.d(TAG, "Auth combine triggered")

            when {
                user == null -> {
                    Log.d(TAG, "AuthState -> LoggedOut (user null)")
                    AuthState.LoggedOut
                }

                accessToken.isNullOrEmpty() -> {
                    Log.d(TAG, "AuthState -> LoggedOut (accessToken missing)")
                    AuthState.LoggedOut
                }

                refreshToken.isNullOrEmpty() -> {
                    Log.d(TAG, "AuthState -> LoggedOut (refreshToken missing)")
                    AuthState.LoggedOut
                }

                !tokenValidator.isValid(accessToken) -> {
                    Log.d(TAG, "AuthState -> LoggedOut (accessToken expired)")
                    AuthState.LoggedOut
                }

                else -> {
                    Log.d(TAG, "AuthState -> LoggedIn")
                    AuthState.LoggedIn
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Loading
        )
}