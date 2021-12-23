package zechs.zplex.models.tmdb.movies

import androidx.annotation.Keep

@Keep
data class Collection(
    val backdrop_path: String?,
    val id: Int,
    val name: String,
    val poster_path: String?
)