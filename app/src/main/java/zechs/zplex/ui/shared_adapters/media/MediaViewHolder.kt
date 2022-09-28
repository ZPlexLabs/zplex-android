package zechs.zplex.ui.shared_adapters.media

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.data.model.PosterSize
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.ItemMediaBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp

class MediaViewHolder(
    private val itemBinding: ItemMediaBinding,
    val mediaAdapter: MediaAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(media: Media) {

        val mediaPosterUrl = if (media.poster_path == null) {
            R.drawable.no_poster
        } else {
            "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${media.poster_path}"
        }

        val rating = "${media.vote_average?.div(2) ?: 0.0}"

        itemBinding.apply {
            seasonNumber.text = media.name ?: media.title
            episodeCount.text = rating.take(3)
            if (mediaAdapter.rating) {
                ratingView.isVisible = true
            }

            itemPoster.apply {
                GlideApp.with(this)
                    .load(mediaPosterUrl)
                    .placeholder(R.drawable.no_poster)
                    .into(this)

                setOnClickListener {
                    mediaAdapter.mediaOnClick.invoke(media)
                }
            }
        }

    }
}