package zechs.zplex.adapter.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.R
import zechs.zplex.databinding.ItemListBinding

class ListDataAdapter(
    val listClickListener: ListClickListener
) : ListAdapter<ListDataModel, ListViewHolder>(ListDataModelDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ListViewHolder {
        return ListViewHolder(
            itemBinding = ItemListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            listDataAdapter = this
        )
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListDataModel.Seasons -> holder.bindSeason(item)
            is ListDataModel.Casts -> holder.bindCasts(item)
            is ListDataModel.Media -> holder.bindMedia(item)
            is ListDataModel.Videos -> holder.bindVideo(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_list
    }
}