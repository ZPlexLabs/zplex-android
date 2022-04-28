package zechs.zplex.adapter.shared_adapters.detailed_media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.adapter.shared_adapters.media.MediaItemDiffCallback
import zechs.zplex.databinding.ItemDetailedMediaBinding
import zechs.zplex.models.tmdb.entities.Media

class DetailedMediaAdapter(
    val mediaOnClick: (Media) -> Unit
) : ListAdapter<Media, DetailedViewHolder>(MediaItemDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = DetailedViewHolder(
        itemBinding = ItemDetailedMediaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ),
        detailedMediaAdapter = this
    )

    override fun onBindViewHolder(
        holder: DetailedViewHolder, position: Int
    ) {
        holder.bind(getItem(position))
    }

}