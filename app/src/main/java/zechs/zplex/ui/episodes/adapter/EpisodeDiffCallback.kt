package zechs.zplex.ui.episodes.adapter

import androidx.recyclerview.widget.DiffUtil
import zechs.zplex.data.model.tmdb.entities.Episode

class EpisodeDiffCallback : DiffUtil.ItemCallback<Episode>() {

    override fun areItemsTheSame(
        oldItem: Episode,
        newItem: Episode
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: Episode, newItem: Episode
    ) = oldItem == newItem

}