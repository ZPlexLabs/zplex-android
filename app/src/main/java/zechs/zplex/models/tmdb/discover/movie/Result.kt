package zechs.zplex.models.tmdb.discover.movie

import androidx.annotation.Keep

@Keep
data class Result(
    val id: Int?,
    val poster_path: String?,
    val title: String?,
    val vote_average: Double?
)