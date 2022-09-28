package zechs.zplex.data.model.tmdb.season

import androidx.annotation.Keep
import zechs.zplex.data.model.tmdb.entities.Episode

@Keep
data class SeasonResponse(
    val episodes: List<Episode>?,
    val id: Int?,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val season_number: Int?
)