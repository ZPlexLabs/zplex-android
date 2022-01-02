package zechs.zplex.models.dataclass

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Media
import java.io.Serializable

@Keep
data class MediaArgs(
    val tmdbId: Int,
    val mediaType: String,
    val media: Media?
) : Serializable