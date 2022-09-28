package zechs.zplex.ui.home.adapter

import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.ui.home.adapter.watched.WatchedDataModel

interface HomeClickListener {

    fun onClickMedia(media: Media)
    fun onClickWatched(watched: WatchedDataModel)

}