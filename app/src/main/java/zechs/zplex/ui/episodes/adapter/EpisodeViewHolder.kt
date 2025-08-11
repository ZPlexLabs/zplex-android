package zechs.zplex.ui.episodes.adapter

import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.progressindicator.LinearProgressIndicator
import zechs.zplex.R
import zechs.zplex.data.model.StillSize
import zechs.zplex.data.model.tmdb.entities.Episode
import zechs.zplex.databinding.ItemEpisodeBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.ext.ifNullOrEmpty

class EpisodeViewHolder(
    private val itemBinding: ItemEpisodeBinding,
    val episodesAdapter: EpisodesAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {
    fun bind(episode: Episode) {
        val count = "Episode ${episode.episode_number}"
        val title = episode.name?.ifNullOrEmpty { "No title" }
        itemBinding.apply {
            val watchProgressTAG = "watchProgressTAG"

            tvTitle.text = title
            tvEpisodeCount.text = count

            if (!episode.still_path.isNullOrEmpty()) {
                val episodeThumb = "${TMDB_IMAGE_PREFIX}/${StillSize.original}${episode.still_path}"
                ivThumb.load(episodeThumb) { placeholder(R.drawable.no_thumb) }
            }

            if (episode.progress == 0) {
                watchProgress.isGone = true
            } else {
                watchProgress.isGone = false
                if (watchProgress.tag == null) {
                    animateProgress(watchProgress, episode.progress)
                    watchProgress.tag = watchProgressTAG
                } else {
                    watchProgress.progress = episode.progress
                }
            }
            offlineBadge.isGone = !episode.offline
            finaleBadge.isInvisible = !episode.isSeasonFinale

            root.setOnClickListener {
                episodesAdapter.episodeOnClick.invoke(episode)
            }
            root.setOnLongClickListener() {
                episodesAdapter.episodeOnLongPress.invoke(episode)
                return@setOnLongClickListener true
            }
        }
    }

    private fun animateProgress(watchProgress: LinearProgressIndicator, targetProgress: Int) {
        val animator = ValueAnimator.ofInt(0, targetProgress)
        animator.duration = 500
        animator.interpolator = DecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            watchProgress.progress = animatedValue
        }

        animator.start()
    }
}