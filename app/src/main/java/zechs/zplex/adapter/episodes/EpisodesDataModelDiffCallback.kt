package zechs.zplex.adapter.episodes

import androidx.recyclerview.widget.DiffUtil

class EpisodesDataModelDiffCallback : DiffUtil.ItemCallback<EpisodesDataModel>() {

    override fun areItemsTheSame(
        oldItem: EpisodesDataModel,
        newItem: EpisodesDataModel
    ): Boolean = when {
        oldItem is EpisodesDataModel.Header && newItem
                is EpisodesDataModel.Header && oldItem.seasonPosterPath == newItem.seasonPosterPath
        -> true

        oldItem is EpisodesDataModel.Episodes && newItem
                is EpisodesDataModel.Episodes && oldItem.episodes == newItem.episodes
        -> true

        else -> false
    }

    override fun areContentsTheSame(
        oldItem: EpisodesDataModel, newItem: EpisodesDataModel
    ) = oldItem == newItem

}