package zechs.zplex.adapter.shared_adapters.banner

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
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
            val rating = media.vote_average?.div(2).toString()
            val ratingText = "$rating/5"
            rbRating.rating = rating.toFloat()
            tvRatingText.text = ratingText

            GlideApp.with(ivBanner)
                .load(mediaBannerUrl)
                .placeholder(R.drawable.no_thumb)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(100)))
                .into(ivBanner)

            GlideApp.with(ivMainBanner)
                .load(mediaBannerUrl)
                .placeholder(R.drawable.no_thumb)
                .into(ivMainBanner)

            root.setOnClickListener {
                bannerAdapter.bannerOnClick.invoke(media)
            }
        }
    }
}