package zechs.zplex.adapter.home

import androidx.annotation.Keep
import zechs.zplex.adapter.watched.WatchedDataModel
import zechs.zplex.ui.tmdbMedia

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