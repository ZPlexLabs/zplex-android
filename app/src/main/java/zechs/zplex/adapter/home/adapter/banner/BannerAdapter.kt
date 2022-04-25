package zechs.zplex.adapter.home.adapter.banner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.databinding.ItemWideBannerBinding
import zechs.zplex.models.tmdb.entities.Media

class BannerAdapter(
    val bannerOnClick: (Media) -> Unit
) : RecyclerView.Adapter<BannerViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

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
        val media = differ.currentList[position]
        holder.bind(media)
    }

    override fun getItemCount() = differ.currentList.size
}