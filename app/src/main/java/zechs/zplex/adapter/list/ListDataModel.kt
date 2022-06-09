package zechs.zplex.adapter.list

import androidx.annotation.Keep
import zechs.zplex.models.tmdb.entities.Cast
import zechs.zplex.models.tmdb.entities.Season
import zechs.zplex.models.tmdb.entities.Video
import zechs.zplex.ui.tmdbMedia

sealed class ListDataModel {

    @Keep
    data class Seasons(
        val tmdbId: Int,
        val showName: String,
        val showPoster: String?,
        val seasons: List<Season>
    ) : ListDataModel()

    @Keep
    data class Media(
        val heading: String,
        val media: List<tmdbMedia>
    ) : ListDataModel()

    @Keep
    data class Casts(
        val casts: List<Cast>
    ) : ListDataModel()

    @Keep
    data class Videos(
        val videos: List<Video>
    ) : ListDataModel()

}