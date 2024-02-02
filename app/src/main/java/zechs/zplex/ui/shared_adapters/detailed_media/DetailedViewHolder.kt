package zechs.zplex.ui.shared_adapters.detailed_media

import androidx.recyclerview.widget.RecyclerView
import coil.load
import zechs.zplex.R
import zechs.zplex.data.model.PosterSize
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.ItemDetailedMediaBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX

class DetailedViewHolder(
    private val itemBinding: ItemDetailedMediaBinding,
    val detailedMediaAdapter: DetailedMediaAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(media: Media) {
        val posterUrl = if (media.poster_path == null) {
            R.drawable.no_poster
        } else {
            "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${media.poster_path}"
        }

        itemBinding.apply {

            root.setOnClickListener {
                detailedMediaAdapter.mediaOnClick.invoke(media)
            }

            ivPoster.load(posterUrl) {
                placeholder(R.drawable.no_poster)
            }

            var releasing = "Release date not available"
            media.releasedDate()?.let { releasing = it }
            tvYear.text = releasing

            tvTitle.text = media.name ?: media.title
            tvPlot.text = media.overview
        }

    }
}