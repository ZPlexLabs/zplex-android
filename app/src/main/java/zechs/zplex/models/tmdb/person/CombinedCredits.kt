package zechs.zplex.models.tmdb.person

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Media

@Keep
data class CombinedCredits(
    val cast: MutableList<Media>?
)