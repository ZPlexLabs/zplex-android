package zechs.zplex.adapter.about

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import zechs.zplex.R
import zechs.zplex.databinding.*
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.models.tmdb.ProfileSize
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import java.text.DecimalFormat


sealed class AboutDataViewHolder(
    binding: ViewBinding,
    val aboutDataAdapter: AboutDataAdapter
) : RecyclerView.ViewHolder(binding.root) {

    class HeaderViewHolder(
        private val itemBinding: ItemHeaderBinding,
        aboutDataAdapter: AboutDataAdapter
    ) : AboutDataViewHolder(itemBinding, aboutDataAdapter) {
        fun bind(item: AboutDataModel.Header) {
            itemBinding.tvHeaderText.text = item.heading
        }
    }

    class SeasonViewHolder(
        private val itemBinding: ItemSeasonBinding,
        aboutDataAdapter: AboutDataAdapter
    ) : AboutDataViewHolder(itemBinding, aboutDataAdapter) {
        fun bind(season: AboutDataModel.Season) {
            val seasonPosterUrl = "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${season.poster_path}"
            val seasonName = "Season ${season.season_number}"
            val totalEpisode = "${season.episode_count} episodes"

            itemBinding.apply {
                seasonNumber.text = seasonName
                episodeCount.text = totalEpisode

                Glide.with(itemPoster)
                    .load(seasonPosterUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.no_poster)
                    .into(itemPoster)
            }
        }
    }

    class CastViewHolder(
        private val itemBinding: ItemCastBinding,
        aboutDataAdapter: AboutDataAdapter
    ) : AboutDataViewHolder(itemBinding, aboutDataAdapter) {
        fun bind(cast: AboutDataModel.Cast) {

            val imageUrl = if (cast.profile_path != null) {
                "${TMDB_IMAGE_PREFIX}/${ProfileSize.w185}${cast.profile_path}"
            } else {
                R.drawable.no_actor
            }

            itemBinding.apply {
                actorName.text = cast.name
                role.text = cast.character

                Glide.with(actorImage)
                    .load(imageUrl)
                    .placeholder(R.drawable.no_actor)
                    .into(actorImage)
            }
        }
    }

    class CurationViewHolder(
        private val itemBinding: ItemCurateBinding,
        aboutDataAdapter: AboutDataAdapter
    ) : AboutDataViewHolder(itemBinding, aboutDataAdapter) {
        fun bind(curation: AboutDataModel.Curation) {
            val posterUrl = if (curation.poster_path != null) {
                "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${curation.poster_path}"
            } else {
                R.drawable.no_poster
            }

            val round = DecimalFormat("#.#")
            val rating = round.format(curation.vote_average)

            itemBinding.apply {
                tvShowName.text = curation.name
                tvRating.text = rating

                Glide.with(itemPoster)
                    .load(posterUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.no_poster)
                    .into(itemPoster)

            }
        }
    }

    class VideosViewHolder(
        private val itemBinding: ItemVideoBinding,
        aboutDataAdapter: AboutDataAdapter
    ) : AboutDataViewHolder(itemBinding, aboutDataAdapter) {
        fun bind(video: AboutDataModel.Video) {
            itemBinding.apply {
                tvSource.text = video.site
                tvVideoName.text = video.name

                Glide.with(thumb)
                    .load(video.thumbUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.no_poster)
                    .into(thumb)
            }
        }
    }

}