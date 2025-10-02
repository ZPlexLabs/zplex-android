package zechs.zplex.data.model

data class SignupRequest(
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String
)