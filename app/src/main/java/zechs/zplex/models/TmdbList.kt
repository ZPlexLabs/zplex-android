package zechs.zplex.models

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Cast
import zechs.zplex.models.tmdb.entities.Season
import zechs.zplex.models.tmdb.entities.Video
import zechs.zplex.ui.tmdbMedia

sealed class TmdbList {

    @Keep
    data class Seasons(
        val tmdbId: Int,
        val showName: String,
        val showPoster: String?,
        val seasons: List<Season>
    ) : TmdbList()

    @Keep
    data class Media(
        val heading: String,
        val media: List<tmdbMedia>
    ) : TmdbList()

    @Keep
    data class Casts(
        val heading: String,
        val casts: List<Cast>
    ) : TmdbList()

    @Keep
    data class Videos(
        val heading: String,
        val videos: List<Video>
    ) : TmdbList()

}