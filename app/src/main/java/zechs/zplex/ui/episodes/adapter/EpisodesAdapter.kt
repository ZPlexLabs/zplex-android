package zechs.zplex.ui.episodes.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.R
import zechs.zplex.data.model.tmdb.entities.Episode
import zechs.zplex.databinding.ItemEpisodeBinding

class EpisodesAdapter(
    val episodeOnClick: (Episode) -> Unit,
    val episodeOnLongPress: (Episode) -> Unit
) : ListAdapter<Episode, EpisodeViewHolder>(EpisodeDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = EpisodeViewHolder(
        itemBinding = ItemEpisodeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ),
        episodesAdapter = this
    )

    override fun onBindViewHolder(
        holder: EpisodeViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun getItemViewType(position: Int) = R.layout.item_episode
}