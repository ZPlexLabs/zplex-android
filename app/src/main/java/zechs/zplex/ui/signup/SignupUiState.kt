package zechs.zplex.ui.signup

data class SignupUiState(
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)