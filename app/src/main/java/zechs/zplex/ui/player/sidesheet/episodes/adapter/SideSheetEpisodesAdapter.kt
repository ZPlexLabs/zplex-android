package zechs.zplex.ui.player.sidesheet.episodes.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.R
import zechs.zplex.data.model.tmdb.entities.Episode
import zechs.zplex.databinding.ItemSidesheetEpisodeBinding

class SideSheetEpisodesAdapter(
    val episodeOnClick: (Episode) -> Unit
) : ListAdapter<Pair<Episode, Boolean>, SideSheetEpisodeViewHolder>(SideSheetEpisodeDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = SideSheetEpisodeViewHolder(
        itemBinding = ItemSidesheetEpisodeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ),
        episodesAdapter = this
    )

    override fun onBindViewHolder(
        holder: SideSheetEpisodeViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.bind(item.first)
    }

    override fun getItemViewType(position: Int) = R.layout.item_sidesheet_episode
}