package zechs.zplex.ui.episodes.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.R
import zechs.zplex.data.model.tmdb.entities.Episode
import zechs.zplex.databinding.ItemEpisodeBinding
import zechs.zplex.databinding.ItemEpisodeHeaderBinding

class EpisodesDataAdapter(
    val episodeOnClick: (Episode) -> Unit
) : ListAdapter<EpisodesDataModel, EpisodesViewHolder>(EpisodesDataModelDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): EpisodesViewHolder {

        val headerViewHolder = EpisodesViewHolder.HeaderViewHolder(
            itemBinding = ItemEpisodeHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )

        val episodeViewHolder = EpisodesViewHolder.EpisodeViewHolder(
            itemBinding = ItemEpisodeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            episodesDataAdapter = this
        )

        return when (viewType) {
            R.layout.item_episode_header -> headerViewHolder
            R.layout.item_episode -> episodeViewHolder
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: EpisodesViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is EpisodesViewHolder.HeaderViewHolder -> holder.bind(item as EpisodesDataModel.Header)
            is EpisodesViewHolder.EpisodeViewHolder -> holder.bind(item as EpisodesDataModel.Episode)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is EpisodesDataModel.Header -> R.layout.item_episode_header
            is EpisodesDataModel.Episode -> R.layout.item_episode
        }
    }
}