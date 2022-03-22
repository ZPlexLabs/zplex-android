package zechs.zplex.adapter.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.databinding.ItemGenreListBinding
import zechs.zplex.databinding.ItemSearchMetaBinding
import zechs.zplex.models.tmdb.entities.Genre

class BrowseDataAdapter : RecyclerView.Adapter<BrowseDataViewHolder>() {

    @JvmField
    var onItemClickListener: ((Genre) -> Unit)? = null

    fun setOnItemClickListener(listener: (Genre) -> Unit) {
        onItemClickListener = listener
    }

    private val differCallback = object : DiffUtil.ItemCallback<BrowseDataModel>() {

        override fun areItemsTheSame(
            oldItem: BrowseDataModel,
            newItem: BrowseDataModel
        ): Boolean = when {
            oldItem is BrowseDataModel.Meta && newItem
                    is BrowseDataModel.Meta && oldItem.title == newItem.title
            -> true
            oldItem is BrowseDataModel.Browse && newItem
                    is BrowseDataModel.Browse && oldItem.genres == newItem.genres
            -> true
            else -> false
        }

        override fun areContentsTheSame(
            oldItem: BrowseDataModel, newItem: BrowseDataModel
        ) = oldItem == newItem

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BrowseDataViewHolder {
        return when (viewType) {
            R.layout.item_search_meta -> BrowseDataViewHolder.MetaViewHolder(
                ItemSearchMetaBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                ), this
            )
            R.layout.item_genre_list -> BrowseDataViewHolder.BrowseViewHolder(
                ItemGenreListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                ), this
            )
            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(holder: BrowseDataViewHolder, position: Int) {
        when (holder) {
            is BrowseDataViewHolder.MetaViewHolder ->
                holder.bind(differ.currentList[position] as BrowseDataModel.Meta)
            is BrowseDataViewHolder.BrowseViewHolder ->
                holder.bind(differ.currentList[position] as BrowseDataModel.Browse)
        }
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is BrowseDataModel.Meta -> R.layout.item_search_meta
            is BrowseDataModel.Browse -> R.layout.item_genre_list
        }
    }

}