package zechs.zplex.data.model.tmdb.media

import androidx.annotation.Keep
import zechs.zplex.data.model.tmdb.entities.Genre
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.data.model.tmdb.entities.Network

@Keep
data class MovieResponse(
    val title: String?,
    val runtime: Int?,
    val release_date: String?,
    val genres: List<Genre>?,
    val id: Int,
    val imdb_id: String?,
    val production_companies: List<Network>?,
    val belongs_to_collection: Media?,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val credits: Credits,
    val recommendations: Recommendations?,
    val videos: Videos?,
    val vote_average: Double?
)