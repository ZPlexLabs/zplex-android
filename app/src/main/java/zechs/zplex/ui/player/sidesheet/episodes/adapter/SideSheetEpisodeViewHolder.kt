package zechs.zplex.ui.player.sidesheet.episodes.adapter

import android.animation.ValueAnimator
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.progressindicator.LinearProgressIndicator
import zechs.zplex.R
import zechs.zplex.data.model.StillSize
import zechs.zplex.data.model.tmdb.entities.Episode
import zechs.zplex.databinding.ItemEpisodeBinding
import zechs.zplex.databinding.ItemSidesheetEpisodeBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.MaterialMotionInterpolator
import zechs.zplex.utils.ext.ifNullOrEmpty

class SideSheetEpisodeViewHolder(
    private val itemBinding: ItemSidesheetEpisodeBinding,
    val episodesAdapter: SideSheetEpisodesAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {
    fun bind(episode: Episode) {
        val count = "Episode ${episode.episode_number}"
        val title = episode.name?.ifNullOrEmpty { "No title" }
        itemBinding.apply {
            val watchProgressTAG = "watchProgressTAG"

            tvTitle.text = title
            tvEpisodeCount.text = count
            // tvOverview.text = episode.overview ?: "No description"

            if (!episode.still_path.isNullOrEmpty()) {
                val episodeThumb = "${TMDB_IMAGE_PREFIX}/${StillSize.w300}${episode.still_path}"
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

            root.setOnClickListener {
                episodesAdapter.episodeOnClick.invoke(episode)
            }
        }
    }

    private fun animateProgress(watchProgress: LinearProgressIndicator, targetProgress: Int) {
        val animator = ValueAnimator.ofInt(0, targetProgress)
        animator.duration = 500
        animator.interpolator = MaterialMotionInterpolator.getEmphasizedInterpolator()

        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            watchProgress.progress = animatedValue
        }

        animator.start()
    }
}