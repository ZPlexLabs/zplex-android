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

        oldItem is HomeDataModel.MediaMenu && newItem
                is HomeDataModel.MediaMenu && oldItem.title == newItem.title
            -> true

        oldItem is HomeDataModel.LatestTvShows && newItem
                is HomeDataModel.LatestTvShows && oldItem.show == newItem.show
            -> true

        oldItem is HomeDataModel.LatestMovies && newItem
                is HomeDataModel.LatestMovies && oldItem.movies == newItem.movies
            -> true

        oldItem is HomeDataModel.Suggestions && newItem
                is HomeDataModel.Suggestions && oldItem.suggestions == newItem.suggestions
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