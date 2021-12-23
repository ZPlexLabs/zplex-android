package zechs.zplex.models

import androidx.annotation.Keep

@Keep
data class ShowEpisode(
    val tmdbId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int
)
