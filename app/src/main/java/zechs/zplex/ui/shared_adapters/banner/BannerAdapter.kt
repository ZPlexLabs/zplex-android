package zechs.zplex.ui.shared_adapters.banner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.ItemWideBannerBinding

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