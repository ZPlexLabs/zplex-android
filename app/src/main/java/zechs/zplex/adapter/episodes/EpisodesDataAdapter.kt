package zechs.zplex.adapter.episodes

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.R
import zechs.zplex.databinding.ItemEpisodeHeaderBinding
import zechs.zplex.databinding.ItemListBinding
import zechs.zplex.models.zplex.Episode

class EpisodesDataAdapter(
    val context: Context,
    val setOnEpisodeClick: (Episode, String, Boolean) -> Unit,
) : ListAdapter<EpisodesDataModel, EpisodesViewHolder>(EpisodesDataModelDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): EpisodesViewHolder {

        val headerViewHolder = EpisodesViewHolder.HeaderViewHolder(
            context = context,
            itemBinding = ItemEpisodeHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )

        val listViewHolder = EpisodesViewHolder.ListViewHolder(
            context = context,
            itemBinding = ItemListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            episodesDataAdapter = this
        )

        return when (viewType) {
            R.layout.item_episode_header -> headerViewHolder
            R.layout.item_list -> listViewHolder
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: EpisodesViewHolder, position: Int) {
        val item = getItem(position)

        when (holder) {
            is EpisodesViewHolder.HeaderViewHolder -> holder.bind(item as EpisodesDataModel.Header)
            is EpisodesViewHolder.ListViewHolder -> {
                when (item) {
                    is EpisodesDataModel.Episodes -> holder.bindEpisodes(item)
                    else -> {}
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is EpisodesDataModel.Header -> R.layout.item_episode_header
            is EpisodesDataModel.Episodes -> R.layout.item_list
        }
    }
}