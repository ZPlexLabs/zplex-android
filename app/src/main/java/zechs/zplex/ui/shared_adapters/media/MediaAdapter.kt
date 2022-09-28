package zechs.zplex.ui.shared_adapters.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.ItemMediaBinding

class MediaAdapter(
    val rating: Boolean = false,
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