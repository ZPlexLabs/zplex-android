package zechs.zplex.models.tmdb.media

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.*

@Keep
data class MediaResponse(
    val id: Int,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val related_media: List<Media>,
    val misc: List<Pair>,
    val seasons: List<Season>,
    val cast: List<Cast>,
    val recommendations: List<Media>,
    val similar: List<Media>,
    val videos: List<Video>,
    val vote_average: String
)