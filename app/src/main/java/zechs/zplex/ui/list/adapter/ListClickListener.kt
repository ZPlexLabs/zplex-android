package zechs.zplex.ui.list.adapter

import zechs.zplex.data.model.tmdb.entities.Cast
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.data.model.tmdb.entities.Season
import zechs.zplex.data.model.tmdb.entities.Video

interface ListClickListener {

    fun onClickSeason(season: Season)
    fun onClickMedia(media: Media)
    fun onClickCast(cast: Cast)
    fun onClickVideo(video: Video)

}