package zechs.zplex.zplex_api.data.remote.api.login.model

data class LoginSuccessResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)