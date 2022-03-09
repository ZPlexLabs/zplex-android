package zechs.zplex.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.databinding.ItemCollectionBinding
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp

class CollectionAdapter(
    val setOnClickListener: (Media) -> Unit
) : RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {

    class CollectionViewHolder(
        private val itemBinding: ItemCollectionBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(media: Media) {
            itemBinding.apply {
                val seasonPosterUrl = if (media.poster_path == null) {
                    R.drawable.no_poster
                } else {
                    "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${media.poster_path}"
                }

                GlideApp.with(ivPoster)
                    .load(seasonPosterUrl)
                    .placeholder(R.drawable.no_poster)
                    .into(ivPoster)

                tvTitle.text = media.name ?: media.title
                tvYear.text = media.release_date?.take(4)
                tvRating.text = String.format("%.1f", media.vote_average?.div(2))
                tvPlot.text = media.overview
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val itemBinding = ItemCollectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CollectionViewHolder(itemBinding)
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val media = differ.currentList[position]
        holder.bind(media)
        holder.itemView.setOnClickListener {
            setOnClickListener.invoke(media)
        }
    }
}
