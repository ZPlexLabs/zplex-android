package zechs.zplex.ui.home.adapter.watched

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.databinding.ItemWatchedBinding

class WatchedDataAdapter(
    val watchedOnClick: (WatchedDataModel) -> Unit,
    val watchedOnLongClick: (WatchedDataModel) -> Unit
) : RecyclerView.Adapter<WatchedViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<WatchedDataModel>() {

        override fun areItemsTheSame(
            oldItem: WatchedDataModel,
            newItem: WatchedDataModel
        ): Boolean = when {
            oldItem is WatchedDataModel.Movie && newItem
                    is WatchedDataModel.Movie && oldItem.movie.tmdbId == newItem.movie.tmdbId
            -> true
            oldItem is WatchedDataModel.Show && newItem
                    is WatchedDataModel.Show && oldItem.show.tmdbId == newItem.show.tmdbId
            -> true
            else -> false
        }

        override fun areContentsTheSame(
            oldItem: WatchedDataModel, newItem: WatchedDataModel
        ) = oldItem == newItem

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = WatchedViewHolder(
        itemBinding = ItemWatchedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ),
        watchedDataAdapter = this
    )

    override fun onBindViewHolder(
        holder: WatchedViewHolder, position: Int
    ) {
        val media = differ.currentList[position]
        holder.bind(media)
    }

    override fun getItemCount() = differ.currentList.size
}