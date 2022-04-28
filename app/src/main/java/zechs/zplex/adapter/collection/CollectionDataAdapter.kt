package zechs.zplex.adapter.collection

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.R
import zechs.zplex.databinding.ItemCollectionHeaderBinding
import zechs.zplex.databinding.ItemListBinding
import zechs.zplex.models.tmdb.entities.Media

class CollectionDataAdapter(
    val context: Context,
    val setOnClickListener: (Media) -> Unit,
) : ListAdapter<CollectionDataModel, CollectionViewHolder>(CollectionDataModelDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): CollectionViewHolder {

        val headerViewHolder = CollectionViewHolder.HeaderViewHolder(
            context = context,
            itemBinding = ItemCollectionHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )

        val listViewHolder = CollectionViewHolder.ListViewHolder(
            context = context,
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