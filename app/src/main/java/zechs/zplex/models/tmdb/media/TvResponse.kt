package zechs.zplex.models.tmdb.media

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Episode
import zechs.zplex.models.tmdb.entities.Genre
import zechs.zplex.models.tmdb.entities.Network
import zechs.zplex.models.tmdb.entities.Season

@Keep
data class TvResponse(
    val episode_run_time: List<Int>?,
    val first_air_date: String?,
    val genres: List<Genre>?,
    val id: Int,
    val imdb_id: String?,
    val name: String?,
    val networks: List<Network>?,
    val production_companies: List<Network>?,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val seasons: MutableList<Season>?,
    val credits: Credits,
    val recommendations: Recommendations?,
    val videos: Videos?,
    val vote_average: Double?,
    val last_episode_to_air: Episode?
)