package zechs.zplex.ui.media.adapter

import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.RatingBar
import com.google.android.material.button.MaterialButton
import zechs.zplex.data.model.entities.Movie
import zechs.zplex.data.model.entities.Show
import zechs.zplex.data.model.tmdb.entities.Cast
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.data.model.tmdb.entities.Season
import zechs.zplex.data.model.tmdb.entities.Video
import zechs.zplex.ui.list.adapter.ListDataModel

interface MediaClickListener {

    fun onClickViewAll(listDataModel: ListDataModel)

    fun onClickMedia(media: Media)
    fun onClickVideo(video: Video)
    fun onClickCast(cast: Cast)

    fun setImageResource(image: Drawable)
    fun setRatingBarView(ratingBar: RatingBar)
    fun setButtonView(button: MaterialButton)

    fun lastSeasonClick(lastSeason: MediaDataModel.LatestSeason)
    fun collectionClick(collectionId: Int)

    fun movieWatchNow(tmdbId: Int, year: Int?)
    fun movieWatchlist(view: MaterialButton, movie: Movie)
    fun movieShare(movie: Movie)

    fun showWatchNow(seasons: List<Season>)
    fun showWatchlist(view: MaterialButton, show: Show)
    fun showShare(show: Show)

    fun openImageInBig(imagePath: String?, imageView: ImageView)

}