package zechs.zplex.ui.episodes.adapter

import androidx.recyclerview.widget.DiffUtil

class EpisodesDataModelDiffCallback : DiffUtil.ItemCallback<EpisodesDataModel>() {

    override fun areItemsTheSame(
        oldItem: EpisodesDataModel,
        newItem: EpisodesDataModel
    ): Boolean = when {
        oldItem is EpisodesDataModel.Header && newItem
                is EpisodesDataModel.Header && oldItem.seasonPosterPath == newItem.seasonPosterPath
        -> true

        oldItem is EpisodesDataModel.Episode && newItem
                is EpisodesDataModel.Episode && oldItem.id == newItem.id
        -> true

        else -> false
    }

    override fun areContentsTheSame(
        oldItem: EpisodesDataModel, newItem: EpisodesDataModel
    ) = oldItem == newItem

}