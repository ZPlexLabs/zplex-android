package zechs.zplex.ui.home.adapter.watched

import androidx.annotation.Keep
import zechs.zplex.data.model.entities.WatchedMovie
import zechs.zplex.data.model.entities.WatchedShow

sealed class WatchedDataModel {

    @Keep
    data class Show(
        val show: WatchedShow
    ) : WatchedDataModel()

    @Keep
    data class Movie(
        val movie: WatchedMovie
    ) : WatchedDataModel()


}