package zechs.zplex.adapter.watched

import androidx.annotation.Keep
import zechs.zplex.models.dataclass.WatchedMovie
import zechs.zplex.models.dataclass.WatchedShow

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