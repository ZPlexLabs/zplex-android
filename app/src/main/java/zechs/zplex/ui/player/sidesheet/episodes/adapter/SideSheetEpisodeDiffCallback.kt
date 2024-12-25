package zechs.zplex.ui.player.sidesheet.episodes.adapter

import androidx.recyclerview.widget.DiffUtil
import zechs.zplex.data.model.tmdb.entities.Episode

class SideSheetEpisodeDiffCallback : DiffUtil.ItemCallback<Pair<Episode, Boolean>>() {

    override fun areItemsTheSame(
        oldItem: Pair<Episode, Boolean>,
        newItem: Pair<Episode, Boolean>
    ): Boolean = oldItem.first.id == newItem.first.id


    override fun areContentsTheSame(
        oldItem: Pair<Episode, Boolean>,
        newItem: Pair<Episode, Boolean>
    ) = oldItem == newItem


}