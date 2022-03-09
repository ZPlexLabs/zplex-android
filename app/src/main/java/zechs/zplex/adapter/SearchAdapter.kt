package zechs.zplex.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_season.view.*
import zechs.zplex.R
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp


class SearchAdapter : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_season, parent, false
            )
        )
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val media = differ.currentList[position]

        val mediaPosterUrl = if (media.poster_path == null) {
            R.drawable.no_poster
        } else {
            "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${media.poster_path}"
        }

        val rating = "${media.vote_average?.div(2) ?: 0.0}"

        holder.itemView.apply {
            season_number.text = media.name ?: media.title
            episode_count.text = rating.take(3)

            GlideApp.with(this)
                .load(mediaPosterUrl)
                .placeholder(R.drawable.no_poster)
                .into(item_poster)

            image_card.transitionName = "SHARED_${media.id}"

            setOnClickListener { view ->
                onItemClickListener?.let {
                    it(media, view, position)
                }
            }
        }
    }

    private var onItemClickListener: ((Media, View, Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Media, View, Int) -> Unit) {
        onItemClickListener = listener
    }
}