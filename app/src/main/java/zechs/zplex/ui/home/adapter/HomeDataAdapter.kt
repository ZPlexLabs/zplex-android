package zechs.zplex.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.R
import zechs.zplex.databinding.ItemHeadingBinding
import zechs.zplex.databinding.ItemListBinding

class HomeDataAdapter(
    val homeClickListener: HomeClickListener
) : ListAdapter<HomeDataModel, HomeViewHolder>(
    HomeDataModelDiffCallback()
) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): HomeViewHolder {

        val headerViewHolder = HomeViewHolder.HeadingViewHolder(
            itemBinding = ItemHeadingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )

        val listViewHolder = HomeViewHolder.ListViewHolder(
            itemBinding = ItemListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            homeDataAdapter = this
        )

        return when (viewType) {
            R.layout.item_heading -> headerViewHolder
            R.layout.item_list -> listViewHolder
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(
        holder: HomeViewHolder,
        position: Int
    ) {
        val item = getItem(position)

        when (holder) {
            is HomeViewHolder.HeadingViewHolder ->
                holder.bind(item as HomeDataModel.Header)

            is HomeViewHolder.ListViewHolder -> {
                when (item) {
                    is HomeDataModel.Media -> holder.bindMedia(item)
                    is HomeDataModel.Banner -> holder.bindBanner(item)
                    is HomeDataModel.Watched -> holder.bindWatched(item)
                    else -> {}
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HomeDataModel.Header -> R.layout.item_heading
            is HomeDataModel.Media -> R.layout.item_list
            is HomeDataModel.Banner -> R.layout.item_list
            is HomeDataModel.Watched -> R.layout.item_list
        }
    }

}