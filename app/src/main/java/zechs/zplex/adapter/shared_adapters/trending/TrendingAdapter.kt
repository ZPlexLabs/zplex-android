package zechs.zplex.adapter.shared_adapters.trending

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.databinding.ItemMediaBinding
import zechs.zplex.models.tmdb.entities.Media

class TrendingAdapter(
    val mediaOnClick: (Media) -> Unit
) : RecyclerView.Adapter<TrendingViewHolder>() {

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
    ) = TrendingViewHolder(
        itemBinding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ),
        trendingAdapter = this
    )

    override fun onBindViewHolder(
        holder: TrendingViewHolder, position: Int
    ) {
        val media = differ.currentList[position]
        holder.bind(media)
    }

    override fun getItemCount() = differ.currentList.size
}