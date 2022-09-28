package zechs.zplex.ui.collection.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.R
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.ItemCollectionHeaderBinding
import zechs.zplex.databinding.ItemListBinding

class CollectionDataAdapter(
    val setOnClickListener: (Media) -> Unit
) : ListAdapter<CollectionDataModel, CollectionViewHolder>(CollectionDataModelDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): CollectionViewHolder {

        val headerViewHolder = CollectionViewHolder.HeaderViewHolder(
            itemBinding = ItemCollectionHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )

        val listViewHolder = CollectionViewHolder.ListViewHolder(
            itemBinding = ItemListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            collectionDataAdapter = this
        )

        return when (viewType) {
            R.layout.item_collection_header -> headerViewHolder
            R.layout.item_list -> listViewHolder
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val item = getItem(position)

        when (holder) {
            is CollectionViewHolder.HeaderViewHolder -> holder.bind(item as CollectionDataModel.Header)
            is CollectionViewHolder.ListViewHolder -> {
                when (item) {
                    is CollectionDataModel.Parts -> holder.bindParts(item)
                    else -> {}
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CollectionDataModel.Header -> R.layout.item_collection_header
            is CollectionDataModel.Parts -> R.layout.item_list
        }
    }
}