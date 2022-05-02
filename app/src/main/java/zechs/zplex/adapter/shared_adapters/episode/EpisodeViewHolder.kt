package zechs.zplex.adapter.shared_adapters.episode

import android.content.Context
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.databinding.ItemEpisodeBinding
import zechs.zplex.models.tmdb.StillSize
import zechs.zplex.models.zplex.Episode
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp
import zechs.zplex.utils.SpannableTextView

class EpisodeViewHolder(
    private val context: Context,
    private val itemBinding: ItemEpisodeBinding,
    val episodeAdapter: EpisodeAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(episode: Episode, accessToken: String?, isLast: Boolean) {

        val episodeStillUrl = if (episode.still_path.isNullOrEmpty()) {
            itemBinding.ivThumb.isGone = true
            R.drawable.no_thumb
        } else {
            "${TMDB_IMAGE_PREFIX}/${StillSize.original}${episode.still_path}"
        }

        val count = "Episode ${episode.episode_number}"
        val title = episode.name.ifEmpty { "No title" }

        itemBinding.apply {
            val ivThumbTAG = "ivThumbTAG"
            val tvEpisodeCountTAG = "tvEpisodeCountTAG"
            val btnPlayTAG = "btnPlayTAG"

            if (episode.still_path.isNullOrEmpty() || ivThumb.tag == ivThumbTAG) {
                ivThumb.tag = ivThumbTAG
                ivThumb.isGone = true
            } else {
                ivThumb.tag = null
                GlideApp.with(ivThumb)
                    .asBitmap()
                    .load(episodeStillUrl)
                    .placeholder(R.drawable.no_thumb)
                    .into(ivThumb)
            }

            if (count == title || tvEpisodeCount.tag == tvEpisodeCountTAG) {
                tvEpisodeCount.tag = tvEpisodeCountTAG
                tvEpisodeCount.isInvisible = true
            } else {
                tvEpisodeCount.tag = null
                tvEpisodeCount.text = count
            }

            println("filedId = ${episode.file_id}")
            if (episode.file_id == null || btnPlay.tag == btnPlayTAG) {
                btnPlay.tag = btnPlayTAG
                btnPlay.text = context.getString(R.string.not_available)
                btnPlay.setOnClickListener(null)
            } else {
                btnPlay.tag = null
                btnPlay.text = context.getString(R.string.play)
                btnPlay.setOnClickListener {
                    episodeAdapter.episodeOnClick.invoke(episode, accessToken!!, isLast)
                }
            }

            val overviewText = if (episode.overview.isNullOrEmpty()) {
                "No description"
            } else episode.overview

            SpannableTextView.spannablePlotText(
                tvOverview, overviewText, 180, "...more", root
            )
            tvTitle.text = title
        }
    }
}