package zechs.zplex.zplex_api.data.local.auth

sealed class AuthState {
    object Loading : AuthState()
    object LoggedOut : AuthState()
    object LoggedIn : AuthState()
}