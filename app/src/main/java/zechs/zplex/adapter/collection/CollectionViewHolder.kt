package zechs.zplex.adapter.collection

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import zechs.zplex.R
import zechs.zplex.adapter.shared_adapters.detailed_media.DetailedMediaAdapter
import zechs.zplex.databinding.ItemCollectionHeaderBinding
import zechs.zplex.databinding.ItemListBinding
import zechs.zplex.models.tmdb.BackdropSize
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp


sealed class CollectionViewHolder(
    val context: Context,
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    class HeaderViewHolder(
        context: Context,
        private val itemBinding: ItemCollectionHeaderBinding
    ) : CollectionViewHolder(context, itemBinding) {
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
                GlideApp.with(ivBackdrop)
                    .load(backdropUrl)
                    .placeholder(R.drawable.no_thumb)
                    .into(ivBackdrop)
            }
        }
    }

    class ListViewHolder(
        context: Context,
        private val itemBinding: ItemListBinding,
        collectionDataAdapter: CollectionDataAdapter
    ) : CollectionViewHolder(context, itemBinding) {

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