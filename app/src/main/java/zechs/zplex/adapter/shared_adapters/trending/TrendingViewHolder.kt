package zechs.zplex.adapter.shared_adapters.trending

import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.databinding.ItemMediaBinding
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp

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
            GlideApp.with(this)
                .load(mediaPosterUrl)
                .placeholder(R.drawable.no_poster)
                .into(this)
            setOnClickListener {
                trendingAdapter.mediaOnClick.invoke(media)
            }
        }

    }
}