package zechs.zplex.ui.home.adapter

import androidx.annotation.Keep
import zechs.zplex.ui.home.adapter.watched.WatchedDataModel

typealias tmdbMedia = zechs.zplex.data.model.tmdb.entities.Media

sealed class HomeDataModel {

    @Keep
    data class Header(
        val heading: String
    ) : HomeDataModel()

    @Keep
    data class Media(
        val media: List<tmdbMedia>
    ) : HomeDataModel()

    @Keep
    data class Banner(
        val media: List<tmdbMedia>
    ) : HomeDataModel()

    @Keep
    data class Watched(
        val watched: List<WatchedDataModel>
    ) : HomeDataModel()

}