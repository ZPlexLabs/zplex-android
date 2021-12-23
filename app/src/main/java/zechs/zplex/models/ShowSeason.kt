package zechs.zplex.models

import androidx.annotation.Keep

@Keep
data class ShowSeason(
    val driveId: String,
    val tmdbId: Int,
    val seasonName: String,
    val seasonNumber: Int,
    val showName: String
)
