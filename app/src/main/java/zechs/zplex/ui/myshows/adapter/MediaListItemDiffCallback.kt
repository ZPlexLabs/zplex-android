package zechs.zplex.ui.myshows.adapter

import androidx.recyclerview.widget.DiffUtil
import zechs.zplex.zplex_api.data.remote.api.MediaListItem

open class MediaListItemDiffCallback : DiffUtil.ItemCallback<MediaListItem>() {

    override fun areItemsTheSame(
        oldItem: MediaListItem,
        newItem: MediaListItem
    ) = oldItem.tmdbId == newItem.tmdbId

    override fun areContentsTheSame(
        oldItem: MediaListItem,
        newItem: MediaListItem
    ) = oldItem == newItem

}