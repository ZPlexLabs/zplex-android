package zechs.zplex.ui.myshows.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import zechs.zplex.R
import zechs.zplex.zplex_api.data.remote.api.MediaListItem
import zechs.zplex.data.model.PosterSize
import zechs.zplex.databinding.ItemMediaItemBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX

class MediaListItemViewHolder(
    private val binding: ItemMediaItemBinding,
    private val mediaAdapter: MediaListItemAdapter
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(media: MediaListItem) = with(binding) {

        tvTitle.text = media.title
        tvRelease.text = media.release

        // Rating
        val rating = media.imdbRating
        if (rating != null) {
            tvRating.text = "%.1f".format(rating)
            ratingCard.isVisible = true
        } else {
            ratingCard.isVisible = false
        }

        // Poster URL
        val posterUrl = media.posterPath?.let {
            "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}$it"
        }

        Glide.with(itemPoster)
            .load(posterUrl)
            .placeholder(R.drawable.no_poster)
            .error(R.drawable.no_poster)
            .thumbnail(0.25f)
            .centerCrop()
            .into(itemPoster)

        root.setOnClickListener {
            mediaAdapter.onClick.invoke(media)
        }
    }
}