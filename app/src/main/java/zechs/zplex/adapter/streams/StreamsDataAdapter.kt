package zechs.zplex.adapter.streams

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.databinding.ItemLoadingBinding
import zechs.zplex.databinding.ItemTextBinding
import zechs.zplex.databinding.ItemTextBoldBinding

class StreamsDataAdapter(
    val setOnStreamClickListener: (StreamsDataModel) -> Unit,
    val setOnDownloadClickListener: (StreamsDataModel) -> Unit
) : RecyclerView.Adapter<StreamsDataViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<StreamsDataModel>() {

        override fun areItemsTheSame(
            oldItem: StreamsDataModel,
            newItem: StreamsDataModel
        ): Boolean = when {
            oldItem is StreamsDataModel.Original && newItem
                    is StreamsDataModel.Original && oldItem.id == newItem.id
            -> true
            oldItem is StreamsDataModel.Stream && newItem
                    is StreamsDataModel.Stream && oldItem.url == newItem.url
            -> true
            oldItem is StreamsDataModel.Loading && newItem
                    is StreamsDataModel.Loading && oldItem == newItem
            -> true
            else -> false
        }

        override fun areContentsTheSame(
            oldItem: StreamsDataModel,
            newItem: StreamsDataModel
        ) = oldItem == newItem

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): StreamsDataViewHolder {
        return when (viewType) {
            R.layout.item_text_bold ->
                StreamsDataViewHolder.OriginalViewHolder(
                    ItemTextBoldBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    ), this
                )
            R.layout.item_text ->
                StreamsDataViewHolder.StreamViewHolder(
                    ItemTextBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    ), this
                )
            R.layout.item_loading ->
                StreamsDataViewHolder.LoadingViewHolder(
                    ItemLoadingBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    ), this
                )
            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(
        holder: StreamsDataViewHolder, position: Int
    ) {
        when (holder) {
            is StreamsDataViewHolder.OriginalViewHolder ->
                holder.bind(differ.currentList[position] as StreamsDataModel.Original)
            is StreamsDataViewHolder.StreamViewHolder ->
                holder.bind(differ.currentList[position] as StreamsDataModel.Stream)
            is StreamsDataViewHolder.LoadingViewHolder ->
                holder.bind()
        }
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is StreamsDataModel.Original -> R.layout.item_text_bold
            is StreamsDataModel.Stream -> R.layout.item_text
            is StreamsDataModel.Loading -> R.layout.item_loading
        }
    }

    override fun onViewRecycled(holder: StreamsDataViewHolder) {
        super.onViewRecycled(holder)
        Log.d(
            "onViewRecycled",
            "position=${holder.adapterPosition}, viewType=${holder.itemViewType} "
        )
    }
}