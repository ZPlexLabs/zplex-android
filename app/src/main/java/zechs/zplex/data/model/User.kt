package zechs.zplex.data.model

data class User (
    val firstName: String,
    val lastName: String,
    val username: String,
    val capabilities: List<Int>,
    val isAdult: Boolean,
    val tokenType: String
)