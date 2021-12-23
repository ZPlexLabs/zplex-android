package zechs.zplex.models.tmdb.media

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Cast

@Keep
data class Credits(
    val cast: List<Cast>?,
)