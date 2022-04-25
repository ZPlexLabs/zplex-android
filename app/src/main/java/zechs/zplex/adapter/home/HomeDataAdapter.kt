package zechs.zplex.adapter.home

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.adapter.watched.WatchedDataModel
import zechs.zplex.databinding.ItemHeadingBinding
import zechs.zplex.databinding.ItemList2Binding
import zechs.zplex.databinding.ItemList3Binding
import zechs.zplex.databinding.ItemListBinding
import zechs.zplex.models.tmdb.entities.Media

class HomeDataAdapter(
    val context: Context,
    val homeOnClick: (Media) -> Unit,
    val watchedOnClick: (WatchedDataModel) -> Unit
) : RecyclerView.Adapter<HomeViewHolder>() {

    private val scrollStates = hashMapOf<String, Parcelable>()

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

        val mediaListViewHolder = HomeViewHolder.MediaViewHolder(
            context = context,
            itemBinding = ItemListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            homeDataAdapter = this
        )

        val bannerViewHolder = HomeViewHolder.BannerViewHolder(
            context = context,
            itemBinding = ItemList2Binding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            homeDataAdapter = this
        )


        val watchedViewHolder = HomeViewHolder.WatchedViewHolder(
            context = context,
            itemBinding = ItemList3Binding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            homeDataAdapter = this
        )

        return when (viewType) {
            R.layout.item_heading -> headerViewHolder
            R.layout.item_list -> mediaListViewHolder
            R.layout.item_list2 -> bannerViewHolder
            R.layout.item_list3 -> watchedViewHolder
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        when (holder) {
            is HomeViewHolder.HeadingViewHolder ->
                holder.bind(differ.currentList[position] as HomeDataModel.Header)
            is HomeViewHolder.MediaViewHolder ->
                holder.bind(differ.currentList[position] as HomeDataModel.Media)
            is HomeViewHolder.BannerViewHolder ->
                holder.bind(differ.currentList[position] as HomeDataModel.Banner)
            is HomeViewHolder.WatchedViewHolder ->
                holder.bind(differ.currentList[position] as HomeDataModel.Watched)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is HomeDataModel.Header -> R.layout.item_heading
            is HomeDataModel.Media -> R.layout.item_list
            is HomeDataModel.Banner -> R.layout.item_list2
            is HomeDataModel.Watched -> R.layout.item_list3
        }
    }

    override fun getItemCount() = differ.currentList.size

}