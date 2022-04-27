package zechs.zplex.adapter.media

import androidx.recyclerview.widget.DiffUtil

class MediaDataModelDiffCallback : DiffUtil.ItemCallback<MediaDataModel>() {

    override fun areItemsTheSame(
        oldItem: MediaDataModel,
        newItem: MediaDataModel
    ): Boolean = when {
        oldItem is MediaDataModel.Heading && newItem
                is MediaDataModel.Heading && oldItem.heading == newItem.heading
        -> true

        oldItem is MediaDataModel.Header && newItem
                is MediaDataModel.Header && oldItem.backdropPath == newItem.backdropPath
        -> true

        oldItem is MediaDataModel.Title && newItem
                is MediaDataModel.Title && oldItem.title == newItem.title
        -> true

        oldItem is MediaDataModel.LatestSeason && newItem
                is MediaDataModel.LatestSeason && oldItem.showTmdbId == newItem.showTmdbId
        -> true

        oldItem is MediaDataModel.PartOfCollection && newItem
                is MediaDataModel.PartOfCollection && oldItem.collectionId == newItem.collectionId
        -> true

        oldItem is MediaDataModel.MovieButton && newItem
                is MediaDataModel.MovieButton && oldItem.movie.id == newItem.movie.id
        -> true

        oldItem is MediaDataModel.ShowButton && newItem
                is MediaDataModel.ShowButton && oldItem.show.id == newItem.show.id
        -> true

        oldItem is MediaDataModel.Casts && newItem
                is MediaDataModel.Casts && oldItem.casts == newItem.casts
        -> true

        oldItem is MediaDataModel.Recommendations && newItem
                is MediaDataModel.Recommendations && oldItem.recommendations == newItem.recommendations
        -> true

        oldItem is MediaDataModel.MoreFromCompany && newItem
                is MediaDataModel.MoreFromCompany && oldItem.more == newItem.more
        -> true

        oldItem is MediaDataModel.Videos && newItem
                is MediaDataModel.Videos && oldItem.videos == newItem.videos
        -> true
        else -> false
    }

    override fun areContentsTheSame(
        oldItem: MediaDataModel, newItem: MediaDataModel
    ) = oldItem == newItem

}