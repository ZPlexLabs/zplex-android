package zechs.zplex.ui.home.adapter.watched

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import zechs.zplex.R
import zechs.zplex.data.model.PosterSize
import zechs.zplex.databinding.ItemWatchedBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.util.ColorManager.Companion.darkenColor
import zechs.zplex.utils.util.ColorManager.Companion.isDark
import zechs.zplex.utils.util.ColorManager.Companion.lightUpColor

class WatchedViewHolder(
    private val itemBinding: ItemWatchedBinding,
    val watchedDataAdapter: WatchedDataAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {

    private var hasLoadedResource = false
    private var isPoster = false

    fun calcDominantColor(drawable: Drawable, onFinish: (Int?) -> Unit) {
        val bmp = (drawable as BitmapDrawable).bitmap.copy(
            Bitmap.Config.ARGB_8888, true
        )
        Palette.from(bmp).generate { p ->
            try {
                onFinish(p!!.dominantSwatch?.rgb ?: p.vibrantSwatch?.rgb)
            } catch (npe: NullPointerException) {
                onFinish(null)
            }
        }
    }

    fun calcMutedColor(drawable: Drawable, onFinish: (Int?) -> Unit) {
        val bmp = (drawable as BitmapDrawable).bitmap.copy(
            Bitmap.Config.ARGB_8888, true
        )
        Palette.from(bmp).generate { p ->
            try {
                onFinish(p!!.darkMutedSwatch?.rgb)
            } catch (npe: NullPointerException) {
                onFinish(null)
            }
        }
    }

    fun bind(watchedDataModel: WatchedDataModel) {
        when (watchedDataModel) {
            is WatchedDataModel.Show -> {
                val show = watchedDataModel.show
                val seasonEpisode = "S%02dE%02d".format(show.seasonNumber, show.episodeNumber)
                val name = "${seasonEpisode}\n${show.name}"

                val mediaPosterUrl = if (show.posterPath == null) {
                    R.drawable.no_poster
                } else {
                    isPoster = true
                    "$TMDB_IMAGE_PREFIX/${PosterSize.w342}${show.posterPath}"
                }

                itemBinding.apply {
                    tvName.text = name
                    watchedProgress.setProgressCompat(show.watchProgress(), true)

                    Glide.with(ivPoster)
                        .load(mediaPosterUrl)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable>,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                if (!hasLoadedResource && isPoster) {
                                    hasLoadedResource = true
                                    calcDominantColor(resource, onFinish = { c ->
                                        c?.let {
                                            val color = if (isDark(c)) lightUpColor(c) else c
                                            watchedProgress.setIndicatorColor(color)
                                        }
                                    })
                                    calcMutedColor(resource, onFinish = { c ->
                                        c?.let {
                                            watchedProgress.trackColor = darkenColor(c)
                                        }
                                    })
                                }
                                return false
                            }

                        })
                        .placeholder(R.drawable.no_poster)
                        .into(ivPoster)
                }
            }

            is WatchedDataModel.Movie -> {
                val movie = watchedDataModel.movie

                val mediaPosterUrl = if (movie.posterPath == null) {
                    R.drawable.no_poster
                } else {
                    isPoster = true
                    "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${movie.posterPath}"
                }

                itemBinding.apply {
                    tvName.text = movie.name
                    watchedProgress.setProgressCompat(movie.watchProgress(), true)
                    Glide.with(ivPoster)
                        .load(mediaPosterUrl)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable>,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                if (!hasLoadedResource && isPoster) {
                                    hasLoadedResource = true
                                    calcDominantColor(resource, onFinish = { c ->
                                        c?.let {
                                            val color = if (isDark(c)) lightUpColor(c) else c
                                            watchedProgress.setIndicatorColor(color)
                                        }
                                    })
                                    calcMutedColor(resource, onFinish = { c ->
                                        c?.let {
                                            watchedProgress.trackColor = darkenColor(c)
                                        }
                                    })
                                }
                                return false
                            }
                        })
                        .placeholder(R.drawable.no_poster)
                        .into(ivPoster)
                }
            }
        }

        itemBinding.root.apply {
            setOnClickListener {
                watchedDataAdapter.watchedOnClick.invoke(watchedDataModel)

            }
            setOnLongClickListener {
                watchedDataAdapter.watchedOnLongClick.invoke(watchedDataModel)
                true
            }
        }
    }
}