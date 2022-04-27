package zechs.zplex.adapter.shared_adapters.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.databinding.ItemMediaBinding
import zechs.zplex.models.tmdb.entities.Media

class MediaAdapter(
    val mediaOnClick: (Media) -> Unit
) : ListAdapter<Media, MediaViewHolder>(MediaItemDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = MediaViewHolder(
        itemBinding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ),
        mediaAdapter = this
    )

    override fun onBindViewHolder(
        holder: MediaViewHolder, position: Int
    ) {
        holder.bind(getItem(position))
    }

}