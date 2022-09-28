package zechs.zplex.ui.list.adapter

import androidx.recyclerview.widget.DiffUtil

class ListDataModelDiffCallback : DiffUtil.ItemCallback<ListDataModel>() {

    override fun areItemsTheSame(
        oldItem: ListDataModel,
        newItem: ListDataModel
    ): Boolean = when {

        oldItem is ListDataModel.Seasons && newItem
                is ListDataModel.Seasons && oldItem.tmdbId == newItem.tmdbId
        -> true
        oldItem is ListDataModel.Casts && newItem
                is ListDataModel.Casts && oldItem.casts == newItem.casts
        -> true

        oldItem is ListDataModel.Media && newItem
                is ListDataModel.Media && oldItem.media == newItem.media
        -> true

        oldItem is ListDataModel.Videos && newItem
                is ListDataModel.Videos && oldItem.videos == newItem.videos
        -> true

        else -> false
    }

    override fun areContentsTheSame(
        oldItem: ListDataModel, newItem: ListDataModel
    ) = oldItem == newItem

}