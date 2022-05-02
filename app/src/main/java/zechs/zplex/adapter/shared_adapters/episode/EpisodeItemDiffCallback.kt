package zechs.zplex.adapter.shared_adapters.episode

import androidx.recyclerview.widget.DiffUtil
import zechs.zplex.models.zplex.Episode

open class EpisodeItemDiffCallback : DiffUtil.ItemCallback<Episode>() {

    override fun areItemsTheSame(
        oldItem: Episode, newItem: Episode
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: Episode, newItem: Episode
    ) = oldItem == newItem

}