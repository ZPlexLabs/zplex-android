package zechs.zplex.ui.collection.adapter

import androidx.recyclerview.widget.DiffUtil

class CollectionDataModelDiffCallback : DiffUtil.ItemCallback<CollectionDataModel>() {

    override fun areItemsTheSame(
        oldItem: CollectionDataModel,
        newItem: CollectionDataModel
    ): Boolean = when {
        oldItem is CollectionDataModel.Header && newItem
                is CollectionDataModel.Header && oldItem.title == newItem.title
        -> true

        oldItem is CollectionDataModel.Parts && newItem
                is CollectionDataModel.Parts && oldItem.parts == newItem.parts
        -> true

        else -> false
    }

    override fun areContentsTheSame(
        oldItem: CollectionDataModel, newItem: CollectionDataModel
    ) = oldItem == newItem

}