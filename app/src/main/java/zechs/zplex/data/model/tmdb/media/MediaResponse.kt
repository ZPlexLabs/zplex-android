package zechs.zplex.data.model.tmdb.media

import androidx.annotation.Keep
import zechs.zplex.data.model.tmdb.entities.*

@Keep
data class MediaResponse(
    val id: Int,
    val imdb_id: String?,
    val backdrop_path: String?,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val related_media: List<Media>,
    val seasons: List<Season>,
    val cast: List<Cast>,
    val recommendations: List<Media>,
    val videos: List<Video>,
    val vote_average: Double?,
    val genres: List<Genre>?,
    val runtime: Int?,
    val year: Int?,
    val belongs_to_collection: Media?,
    val last_episode_to_air: Episode?
)