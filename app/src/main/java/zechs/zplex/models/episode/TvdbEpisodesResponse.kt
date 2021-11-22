package zechs.zplex.models.episode

import androidx.annotation.Keep

@Keep
data class TvdbEpisodesResponse(
    val data: MutableList<Data>?,
)