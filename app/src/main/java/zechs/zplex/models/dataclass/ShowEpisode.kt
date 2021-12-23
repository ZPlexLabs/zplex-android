package zechs.zplex.models.dataclass

import androidx.annotation.Keep

@Keep
data class ShowEpisode(
    val tmdbId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int
)
