package zechs.zplex.zplex_api.data.remote.api.token.model

data class RefreshResponse(
    val accessToken: String,
    val tokenType: String
)