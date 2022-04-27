package zechs.zplex.adapter.shared_adapters.banner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.databinding.ItemWideBannerBinding
import zechs.zplex.models.tmdb.entities.Media

class BannerAdapter(
    val bannerOnClick: (Media) -> Unit
) : ListAdapter<Media, BannerViewHolder>(BannerItemDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BannerViewHolder(
        itemBinding = ItemWideBannerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ),
        bannerAdapter = this
    )

    override fun onBindViewHolder(
        holder: BannerViewHolder, position: Int
    ) {
        holder.bind(getItem(position))
    }

}