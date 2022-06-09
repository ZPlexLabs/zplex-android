package zechs.zplex.adapter.list

import zechs.zplex.models.tmdb.entities.Cast
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.models.tmdb.entities.Season
import zechs.zplex.models.tmdb.entities.Video

interface ListClickListener {

    fun onClickSeason(season: Season)
    fun onClickMedia(media: Media)
    fun onClickCast(cast: Cast)
    fun onClickVideo(video: Video)

}