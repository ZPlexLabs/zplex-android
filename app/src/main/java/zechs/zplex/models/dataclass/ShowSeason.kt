package zechs.zplex.models.dataclass

import androidx.annotation.Keep

@Keep
data class ShowSeason(
    val tmdbId: Int,
    val seasonName: String,
    val seasonNumber: Int,
    val showName: String,
    val posterPath: String?
)
