package zechs.zplex.models

data class FilterArgs(
    val mediaType: String,
    val sortBy: String,
    val page: Int,
    val withKeyword: Int?,
    val withGenres: Int
)