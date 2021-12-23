package zechs.zplex.models.tmdb.tv

import androidx.annotation.Keep
import zechs.zplex.models.misc.Pairs
import zechs.zplex.models.tmdb.Media

@Keep
data class MediaResponse(
    val id: Int,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val related_media: List<Media>,
    val misc: List<Pairs>,
    val seasons: List<Season>,
    val cast: List<Cast>,
    val recommendations: List<Media>,
    val similar: List<Media>,
    val videos: List<Video>,
    val vote_average: String
)