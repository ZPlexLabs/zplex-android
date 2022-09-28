package zechs.zplex.data.model.tmdb.person

import androidx.annotation.Keep
import zechs.zplex.data.model.tmdb.entities.Media

@Keep
data class CombinedCredits(
    val cast: MutableList<Media>?
)