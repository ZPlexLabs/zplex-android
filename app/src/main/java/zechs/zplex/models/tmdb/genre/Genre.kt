package zechs.zplex.models.tmdb.genre

import androidx.annotation.Keep

@Keep
data class Genre(
    val id: Int,
    val name: String,
    val mediaType: String
)