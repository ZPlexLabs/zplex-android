package zechs.zplex.models.dataclass

import androidx.annotation.Keep

@Keep
data class MediaArgs(
    val tmdbId: Int,
    val mediaType: String
)