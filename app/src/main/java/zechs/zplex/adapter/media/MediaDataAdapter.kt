package zechs.zplex.adapter.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.databinding.ItemDetailBinding
import zechs.zplex.databinding.ItemMediaMetaBinding

class MediaDataAdapter : RecyclerView.Adapter<MediaDataViewHolder>() {

    @JvmField
    var onItemClickListener: ((AboutDataModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (AboutDataModel) -> Unit) {
        onItemClickListener = listener
    }

    private val differCallback = object : DiffUtil.ItemCallback<MediaDataModel>() {

        override fun areItemsTheSame(
            oldItem: MediaDataModel,
            newItem: MediaDataModel
        ): Boolean = when {
            oldItem is MediaDataModel.Meta && newItem
                    is MediaDataModel.Meta && oldItem.tmdbId == newItem.tmdbId
            -> true
            oldItem is MediaDataModel.Details && newItem
                    is MediaDataModel.Details && oldItem.items == newItem.items
            -> true
            else -> false
        }

        override fun areContentsTheSame(
            oldItem: MediaDataModel, newItem: MediaDataModel
        ) = oldItem == newItem

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): MediaDataViewHolder {
        return when (viewType) {
            R.layout.item_media_meta ->
                MediaDataViewHolder.MetaViewHolder(
                    ItemMediaMetaBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    ), this
                )
            R.layout.item_detail ->
                MediaDataViewHolder.DetailsViewHolder(
                    ItemDetailBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    ), this
                )
            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(
        holder: MediaDataViewHolder, position: Int
    ) {
        when (holder) {
            is MediaDataViewHolder.MetaViewHolder ->
                holder.bind(differ.currentList[position] as MediaDataModel.Meta)
            is MediaDataViewHolder.DetailsViewHolder ->
                holder.bind(differ.currentList[position] as MediaDataModel.Details)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is MediaDataModel.Meta -> R.layout.item_media_meta
            is MediaDataModel.Details -> R.layout.item_detail
        }
    }
}