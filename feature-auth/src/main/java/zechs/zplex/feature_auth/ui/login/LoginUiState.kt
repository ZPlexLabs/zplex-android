package zechs.zplex.feature_auth.ui.login

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)