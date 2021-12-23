package zechs.zplex.models.season

import androidx.annotation.Keep

@Keep
data class Seasons(
    val episodes: List<Ep>,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val season_number: Int?
)