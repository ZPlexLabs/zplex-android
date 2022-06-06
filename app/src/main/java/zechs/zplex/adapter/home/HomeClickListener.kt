package zechs.zplex.adapter.home

import android.graphics.drawable.Drawable
import android.widget.RatingBar
import zechs.zplex.adapter.watched.WatchedDataModel
import zechs.zplex.models.tmdb.entities.Media

interface HomeClickListener {

    fun onClickMedia(media: Media)
    fun onClickWatched(watched: WatchedDataModel)

    fun setImageResource(image: Drawable)
    fun setRatingBarView(ratingBar: RatingBar)


}