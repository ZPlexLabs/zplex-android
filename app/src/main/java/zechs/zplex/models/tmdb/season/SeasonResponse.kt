package zechs.zplex.models.tmdb.season

import androidx.annotation.Keep

@Keep
data class SeasonResponse(
    val _id: String?,
    val air_date: String?,
    val episodes: MutableList<Episode>?,
    val id: Int?,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val season_number: Int?
)