package zechs.zplex.ui.home.adapter.watched

import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.data.model.PosterSize
import zechs.zplex.databinding.ItemWatchedBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp

class WatchedViewHolder(
    private val itemBinding: ItemWatchedBinding,
    val watchedDataAdapter: WatchedDataAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {
    fun bind(watchedDataModel: WatchedDataModel) {
        when (watchedDataModel) {
            is WatchedDataModel.Show -> {
                val show = watchedDataModel.show
                val name = "S${show.seasonNumber}E${show.episodeNumber}\n${show.name}"

                val mediaPosterUrl = if (show.posterPath == null) {
                    R.drawable.no_poster
                } else {
                    "$TMDB_IMAGE_PREFIX/${PosterSize.w342}${show.posterPath}"
                }

                itemBinding.apply {
                    tvName.text = name
                    watchedProgress.setProgressCompat(show.watchProgress(), true)

                    GlideApp.with(ivPoster)
                        .load(mediaPosterUrl)
                        .placeholder(R.drawable.no_poster)
                        .into(ivPoster)
                }
            }

            is WatchedDataModel.Movie -> {
                val movie = watchedDataModel.movie

                val mediaPosterUrl = if (movie.posterPath == null) {
                    R.drawable.no_poster
                } else {
                    "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${movie.posterPath}"
                }

                itemBinding.apply {
                    tvName.text = movie.name
                    watchedProgress.setProgressCompat(movie.watchProgress(), true)
                    GlideApp.with(ivPoster)
                        .load(mediaPosterUrl)
                        .placeholder(R.drawable.no_poster)
                        .into(ivPoster)
                }
            }
        }

        itemBinding.root.setOnClickListener {
            watchedDataAdapter.watchedOnClick.invoke(watchedDataModel)
        }
    }
}