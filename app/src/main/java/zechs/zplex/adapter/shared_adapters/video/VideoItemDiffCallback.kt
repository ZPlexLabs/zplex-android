package zechs.zplex.adapter.shared_adapters.video

import androidx.recyclerview.widget.DiffUtil
import zechs.zplex.models.tmdb.entities.Video

open class VideoItemDiffCallback : DiffUtil.ItemCallback<Video>() {

    override fun areItemsTheSame(
        oldItem: Video, newItem: Video
    ) = oldItem.key == newItem.key

    override fun areContentsTheSame(
        oldItem: Video, newItem: Video
    ) = oldItem == newItem

}