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
            itemBinding.tvText.apply {
                text = item.title
                setOnClickListener {
                    streamsDataAdapter.setOnStreamClickListener.invoke(item)
                }
            }
            itemBinding.btnDownload.setOnClickListener {
                streamsDataAdapter.setOnDownloadClickListener.invoke(item)
            }
        }
    }

    class StreamViewHolder(
        private val itemBinding: ItemTextBinding,
        streamsDataAdapter: StreamsDataAdapter
    ) : StreamsDataViewHolder(itemBinding, streamsDataAdapter) {
        fun bind(item: StreamsDataModel.Stream) {
            itemBinding.tvText.apply {
                text = item.name
                setOnClickListener {
                    streamsDataAdapter.setOnStreamClickListener.invoke(item)
                }
            }

            itemBinding.btnDownload.setOnClickListener {
                streamsDataAdapter.setOnDownloadClickListener.invoke(item)
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