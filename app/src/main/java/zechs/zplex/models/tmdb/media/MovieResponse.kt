package zechs.zplex.models.tmdb.media

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Genre
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.models.tmdb.entities.Network

@Keep
data class MovieResponse(
    val title: String?,
    val runtime: Int?,
    val status: String?,
    val release_date: String?,
    val genres: List<Genre>?,
    val id: Int,
    val production_companies: List<Network>?,
    val belongs_to_collection: Media?,
    val overview: String?,
    val poster_path: String?,
    val credits: Credits,
    val recommendations: Recommendations?,
    val similar: Similar?,
    val videos: Videos?,
    val vote_average: Double?
)