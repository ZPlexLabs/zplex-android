package zechs.zplex.ui.home.adapter

import androidx.recyclerview.widget.DiffUtil

class HomeDataModelDiffCallback : DiffUtil.ItemCallback<HomeDataModel>() {

    override fun areItemsTheSame(
        oldItem: HomeDataModel,
        newItem: HomeDataModel
    ): Boolean = when {
        oldItem is HomeDataModel.Header && newItem
                is HomeDataModel.Header && oldItem.heading == newItem.heading
            -> true

        oldItem is HomeDataModel.Media && newItem
                is HomeDataModel.Media && oldItem.media == newItem.media
            -> true

        oldItem is HomeDataModel.Banner && newItem
                is HomeDataModel.Banner && oldItem.media == newItem.media
            -> true

        oldItem is HomeDataModel.Watched && newItem
                is HomeDataModel.Watched && oldItem.watched == newItem.watched
            -> true

        else -> false
    }

    override fun areContentsTheSame(
        oldItem: HomeDataModel, newItem: HomeDataModel
    ) = oldItem == newItem

}