package zechs.zplex.adapter.shared_adapters.banner

import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.databinding.ItemWideBannerBinding
import zechs.zplex.models.tmdb.BackdropSize
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp

class BannerViewHolder(
    private val itemBinding: ItemWideBannerBinding,
    val bannerAdapter: BannerAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(media: Media) {
        val mediaBannerUrl = if (media.backdrop_path == null) {
            R.drawable.no_thumb
        } else {
            "${TMDB_IMAGE_PREFIX}/${BackdropSize.w780}${media.backdrop_path}"
        }

        itemBinding.apply {
            tvTitle.text = media.title
            ivBanner.apply {
                GlideApp.with(this)
                    .load(mediaBannerUrl)
                    .placeholder(R.drawable.no_thumb)
                    .into(this)

                setOnClickListener {
                    bannerAdapter.bannerOnClick.invoke(media)
                }
            }
        }
    }
}