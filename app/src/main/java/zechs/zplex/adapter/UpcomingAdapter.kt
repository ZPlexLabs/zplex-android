package zechs.zplex.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.databinding.ItemCollectionBinding
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.utils.Constants
import zechs.zplex.utils.ConverterUtils
import zechs.zplex.utils.GlideApp

class UpcomingAdapter(
    val setOnClickListener: (Media) -> Unit
) : RecyclerView.Adapter<UpcomingAdapter.MediaViewHolder>() {

    class MediaViewHolder(
        private val itemBinding: ItemCollectionBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(media: Media) {
            itemBinding.apply {
                val seasonPosterUrl = if (media.poster_path == null) {
                    R.drawable.no_poster
                } else {
                    "${Constants.TMDB_IMAGE_PREFIX}/${PosterSize.w342}${media.poster_path}"
                }

                GlideApp.with(ivPoster)
                    .load(seasonPosterUrl)
                    .placeholder(R.drawable.no_poster)
                    .into(ivPoster)

                var releasing = "Release date not available"
                media.release_date?.let {
                    releasing = ConverterUtils.parseDate(it, "MMM dd, yyyy")
                }

                tvYear.text = releasing
                tvTitle.text = media.name ?: media.title
                tvPlot.text = media.overview
                tvRating.isInvisible = true
                imageView2.isInvisible = true
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val itemBinding = ItemCollectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MediaViewHolder(itemBinding)
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = differ.currentList[position]
        holder.bind(media)
        holder.itemView.setOnClickListener {
            setOnClickListener.invoke(media)
        }
    }
}
