package zechs.zplex.adapter.browse

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import zechs.zplex.ThisApp.Companion.context
import zechs.zplex.adapter.GenreAdapter
import zechs.zplex.databinding.ItemGenreListBinding
import zechs.zplex.databinding.ItemSearchMetaBinding
import zechs.zplex.models.tmdb.genre.Genre


sealed class BrowseDataViewHolder(
    binding: ViewBinding,
    val browseDataAdapter: BrowseDataAdapter
) : RecyclerView.ViewHolder(binding.root) {

    class MetaViewHolder(
        private val itemBinding: ItemSearchMetaBinding,
        browseDataAdapter: BrowseDataAdapter
    ) : BrowseDataViewHolder(itemBinding, browseDataAdapter) {
        fun bind(item: BrowseDataModel.Meta) {
            itemBinding.apply {
                textView.text = item.title
                btnGoSearch.setOnClickListener {
                    browseDataAdapter.onItemClickListener?.let {
                        it(Genre(0, "null", "none"))
                    }
                }
            }
        }
    }


    class BrowseViewHolder(
        private val itemBinding: ItemGenreListBinding,
        browseDataAdapter: BrowseDataAdapter
    ) : BrowseDataViewHolder(itemBinding, browseDataAdapter) {

        private val genreAdapter by lazy { GenreAdapter() }

        fun bind(item: BrowseDataModel.Browse) {
            itemBinding.tvHeader.text = item.header
            context?.let { c ->
                itemBinding.rvList.apply {
                    adapter = genreAdapter
                    layoutManager = GridLayoutManager(c, 2)
                    itemAnimator = null
                }
            }
            genreAdapter.differ.submitList(item.genres)
            genreAdapter.setOnItemClickListener { genre ->
                browseDataAdapter.onItemClickListener?.let {
                    it(Genre(genre.id, genre.name, genre.mediaType))
                }
            }
        }
    }

}