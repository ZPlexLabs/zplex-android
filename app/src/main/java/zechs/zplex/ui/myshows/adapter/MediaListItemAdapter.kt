package zechs.zplex.ui.myshows.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.zplex_api.data.remote.api.MediaListItem
import zechs.zplex.databinding.ItemMediaItemBinding

class MediaListItemAdapter(
    val onClick: (MediaListItem) -> Unit
) : ListAdapter<MediaListItem, MediaListItemViewHolder>(MediaListItemDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = MediaListItemViewHolder(
        binding = ItemMediaItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ),
        mediaAdapter = this
    )

    override fun onBindViewHolder(
        holder: MediaListItemViewHolder, position: Int
    ) {
        holder.bind(getItem(position))
    }

}