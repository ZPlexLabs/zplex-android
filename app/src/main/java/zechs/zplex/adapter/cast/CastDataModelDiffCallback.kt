package zechs.zplex.adapter.cast

import androidx.recyclerview.widget.DiffUtil

class CastDataModelDiffCallback : DiffUtil.ItemCallback<CastDataModel>() {

    override fun areItemsTheSame(
        oldItem: CastDataModel,
        newItem: CastDataModel
    ): Boolean = when {
        oldItem is CastDataModel.Heading && newItem
                is CastDataModel.Heading && oldItem.heading == newItem.heading
        -> true

        oldItem is CastDataModel.Header && newItem
                is CastDataModel.Header && oldItem.profilePath == newItem.profilePath
        -> true

        oldItem is CastDataModel.Meta && newItem
                is CastDataModel.Meta && oldItem.id == newItem.id
        -> true

        oldItem is CastDataModel.AppearsIn && newItem
                is CastDataModel.AppearsIn && oldItem.appearsIn == newItem.appearsIn
        -> true

        else -> false
    }

    override fun areContentsTheSame(
        oldItem: CastDataModel, newItem: CastDataModel
    ) = oldItem == newItem

}