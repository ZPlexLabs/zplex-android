package zechs.zplex.models.tmdb.tv

import androidx.annotation.Keep

@Keep
data class TvResponse(
    val episode_run_time: List<Int>?,
    val first_air_date: String?,
    val genres: List<Genre>?,
    val id: Int,
    val name: String?,
    val networks: List<Network>?,
    val overview: String?,
    val poster_path: String?,
    val seasons: MutableList<Season>?,
    val credits: Credits,
    val recommendations: Recommendations?,
    val similar: Similar?,
    val videos: Videos?,
    val vote_average: Double?
)