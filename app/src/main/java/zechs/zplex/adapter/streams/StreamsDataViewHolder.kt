package zechs.zplex.adapter.streams

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import zechs.zplex.databinding.ItemLoadingBinding
import zechs.zplex.databinding.ItemTextBinding
import zechs.zplex.databinding.ItemTextBoldBinding

sealed class StreamsDataViewHolder(
    binding: ViewBinding,
    val streamsDataAdapter: StreamsDataAdapter
) : RecyclerView.ViewHolder(binding.root) {

    class OriginalViewHolder(
        private val itemBinding: ItemTextBoldBinding,
        streamsDataAdapter: StreamsDataAdapter
    ) : StreamsDataViewHolder(itemBinding, streamsDataAdapter) {
        fun bind(item: StreamsDataModel.Original) {
            itemBinding.tvText.text = item.title
            itemBinding.root.setOnClickListener {
                streamsDataAdapter.onItemClickListener?.let { it(item) }
            }
        }
    }

    class StreamViewHolder(
        private val itemBinding: ItemTextBinding,
        streamsDataAdapter: StreamsDataAdapter
    ) : StreamsDataViewHolder(itemBinding, streamsDataAdapter) {
        fun bind(item: StreamsDataModel.Stream) {
            itemBinding.tvText.text = item.name
            itemBinding.root.setOnClickListener {
                streamsDataAdapter.onItemClickListener?.let { it(item) }
            }
        }
    }

    class LoadingViewHolder(
        itemBinding: ItemLoadingBinding,
        streamsDataAdapter: StreamsDataAdapter
    ) : StreamsDataViewHolder(itemBinding, streamsDataAdapter) {
        fun bind(item: StreamsDataModel.Loading) {}
    }

}