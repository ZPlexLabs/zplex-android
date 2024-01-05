package zechs.zplex.ui.collection.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import zechs.zplex.R
import zechs.zplex.data.model.BackdropSize
import zechs.zplex.data.model.PosterSize
import zechs.zplex.databinding.ItemCollectionHeaderBinding
import zechs.zplex.databinding.ItemListBinding
import zechs.zplex.ui.shared_adapters.detailed_media.DetailedMediaAdapter
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX


sealed class CollectionViewHolder(
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    class HeaderViewHolder(
        private val itemBinding: ItemCollectionHeaderBinding
    ) : CollectionViewHolder(itemBinding) {
        fun bind(item: CollectionDataModel.Header) {
            val backdropUrl = if (item.backdropPath == null) {
                if (item.posterPath == null) {
                    "${TMDB_IMAGE_PREFIX}/${PosterSize.w780}${item.posterPath}"
                } else R.drawable.no_thumb
            } else {
                "${TMDB_IMAGE_PREFIX}/${BackdropSize.w780}${item.backdropPath}"
            }

            itemBinding.apply {
                tvName.text = item.title
                Glide.with(ivBackdrop)
                    .load(backdropUrl)
                    .placeholder(R.drawable.no_thumb)
                    .into(ivBackdrop)
            }
        }
    }

    class ListViewHolder(
        private val itemBinding: ItemListBinding,
        collectionDataAdapter: CollectionDataAdapter
    ) : CollectionViewHolder(itemBinding) {

        private val detailedMediaBinding by lazy {
            DetailedMediaAdapter {
                collectionDataAdapter.setOnClickListener.invoke(it)
            }
        }

        fun bindParts(item: CollectionDataModel.Parts) {
            itemBinding.rvList.apply {
                adapter = detailedMediaBinding
                layoutManager = LinearLayoutManager(
                    context, LinearLayoutManager.VERTICAL, false
                )
            }
            detailedMediaBinding.submitList(item.parts)
        }
    }
}