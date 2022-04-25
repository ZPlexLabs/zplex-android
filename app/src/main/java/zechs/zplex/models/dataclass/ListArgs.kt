package zechs.zplex.models.dataclass

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Cast
import zechs.zplex.models.tmdb.entities.Season

@Keep
data class ListArgs(
    val tmdbId: Int,
    val showName: String,
    val showPoster: String?,
    val castList: List<Cast>?,
    val seasonList: List<Season>?
)