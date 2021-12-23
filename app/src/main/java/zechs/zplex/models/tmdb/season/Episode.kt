package zechs.zplex.models.tmdb.season

import androidx.annotation.Keep

@Keep
data class Episode(
    val id: Int?,
    val name: String?,
    val overview: String?,
    val episode_number: Int,
    val season_number: Int?,
    val still_path: String?,
)