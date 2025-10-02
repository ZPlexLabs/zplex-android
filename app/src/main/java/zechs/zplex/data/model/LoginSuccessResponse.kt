package zechs.zplex.data.model

data class LoginSuccessResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)