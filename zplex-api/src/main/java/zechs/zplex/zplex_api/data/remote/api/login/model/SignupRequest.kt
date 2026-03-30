package zechs.zplex.zplex_api.data.remote.api.login.model

data class SignupRequest(
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String
)