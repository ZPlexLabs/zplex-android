package zechs.zplex.models.tmdb.entities

import androidx.annotation.Keep

@Keep
data class Genre(
    val id: Int?,
    val name: String,
    val mediaType: String?,
    val keyword: Int? = null
)