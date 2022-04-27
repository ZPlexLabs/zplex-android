package zechs.zplex.adapter.media

import android.graphics.drawable.Drawable
import android.widget.RatingBar
import com.google.android.material.button.MaterialButton
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show
import zechs.zplex.models.tmdb.entities.Cast
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.models.tmdb.entities.Season
import zechs.zplex.models.tmdb.entities.Video

interface MediaClickListener {

    fun onClickMedia(media: Media)
    fun onClickVideo(video: Video)
    fun onClickCast(cast: Cast)

    fun setImageResource(image: Drawable)
    fun setRatingBarView(ratingBar: RatingBar)
    fun setButtonView(button: MaterialButton)

    fun lastSeasonClick(lastSeason: MediaDataModel.LatestSeason)
    fun collectionClick(collectionId: Int)

    fun movieWatchNow(tmdbId: Int)
    fun movieWatchlist(view: MaterialButton, movie: Movie)
    fun movieShare(movie: Movie)

    fun showWatchNow(seasons: List<Season>)
    fun showWatchlist(view: MaterialButton, show: Show)
    fun showShare(show: Show)

}