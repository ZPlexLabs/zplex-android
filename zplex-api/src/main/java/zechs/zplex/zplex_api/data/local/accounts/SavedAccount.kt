package zechs.zplex.zplex_api.data.local.accounts

data class SavedAccount(
    val username: String,
    val firstName: String,
    val lastName: String,
    val accessToken: String,
    val refreshToken: String,
    val capabilities: List<Int>,
    val isAdult: Boolean,
    val tokenType: String
)
