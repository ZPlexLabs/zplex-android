package zechs.zplex.models.tmdb.discover.tv

import androidx.annotation.Keep

@Keep
data class Result(
    val id: Int?,
    val name: String?,
    val poster_path: String?,
    val vote_average: Double?
)