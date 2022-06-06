package zechs.zplex.adapter.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.databinding.ItemHeadingBinding
import zechs.zplex.databinding.ItemListBinding

class HomeDataAdapter(
    val context: Context,
    val homeClickListener: HomeClickListener
) : RecyclerView.Adapter<HomeViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<HomeDataModel>() {

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

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): HomeViewHolder {

        val headerViewHolder = HomeViewHolder.HeadingViewHolder(
            context = context,
            itemBinding = ItemHeadingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )

        val listViewHolder = HomeViewHolder.ListViewHolder(
            context = context,
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

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val item = differ.currentList[position]

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
        return when (differ.currentList[position]) {
            is HomeDataModel.Header -> R.layout.item_heading
            is HomeDataModel.Media -> R.layout.item_list
            is HomeDataModel.Banner -> R.layout.item_list
            is HomeDataModel.Watched -> R.layout.item_list
        }
    }

    override fun getItemCount() = differ.currentList.size

}