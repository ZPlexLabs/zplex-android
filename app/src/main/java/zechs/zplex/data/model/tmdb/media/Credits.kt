package zechs.zplex.data.model.tmdb.media

import androidx.annotation.Keep
import zechs.zplex.data.model.tmdb.entities.Cast

@Keep
data class Credits(
    val cast: List<Cast>?,
)