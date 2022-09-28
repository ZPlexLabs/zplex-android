package zechs.zplex.ui.list.adapter

import androidx.annotation.Keep
import zechs.zplex.data.model.tmdb.entities.Cast
import zechs.zplex.data.model.tmdb.entities.Season
import zechs.zplex.data.model.tmdb.entities.Video
import zechs.zplex.ui.home.adapter.tmdbMedia

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