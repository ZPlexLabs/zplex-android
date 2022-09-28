package zechs.zplex.ui.shared_adapters.media

import androidx.recyclerview.widget.DiffUtil
import zechs.zplex.data.model.tmdb.entities.Media

open class MediaItemDiffCallback : DiffUtil.ItemCallback<Media>() {

    override fun areItemsTheSame(
        oldItem: Media, newItem: Media
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: Media, newItem: Media
    ) = oldItem == newItem

}