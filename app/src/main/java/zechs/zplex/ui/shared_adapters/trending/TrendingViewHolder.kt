package zechs.zplex.ui.shared_adapters.trending

import androidx.recyclerview.widget.RecyclerView
import coil.load
import zechs.zplex.R
import zechs.zplex.data.model.PosterSize
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.ItemMediaBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX

class TrendingViewHolder(
    private val itemBinding: ItemMediaBinding,
    val trendingAdapter: TrendingAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(media: Media) {
        val mediaPosterUrl = if (media.poster_path == null) {
            R.drawable.no_poster
        } else {
            "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${media.poster_path}"
        }
        itemBinding.itemPoster.apply {
            load(mediaPosterUrl) {
                placeholder(R.drawable.no_poster)
            }
            setOnClickListener {
                trendingAdapter.mediaOnClick.invoke(media)
            }
        }

    }
}