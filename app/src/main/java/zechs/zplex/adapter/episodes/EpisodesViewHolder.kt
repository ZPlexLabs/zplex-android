package zechs.zplex.adapter.episodes

import android.content.Context
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import zechs.zplex.R
import zechs.zplex.adapter.shared_adapters.episode.EpisodeAdapter
import zechs.zplex.databinding.ItemEpisodeHeaderBinding
import zechs.zplex.databinding.ItemListBinding
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp


sealed class EpisodesViewHolder(
    val context: Context,
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    class HeaderViewHolder(
        context: Context,
        private val itemBinding: ItemEpisodeHeaderBinding
    ) : EpisodesViewHolder(context, itemBinding) {
        fun bind(item: EpisodesDataModel.Header) {
            val posterUrl = if (item.seasonPosterPath != null) {
                "${TMDB_IMAGE_PREFIX}/${PosterSize.w780}${item.seasonPosterPath}"
            } else R.drawable.no_thumb

            val overviewText = item.seasonOverview.ifEmpty { "No description" }

            itemBinding.apply {
                tvSeasonNumber.text = item.seasonNumber
                tvPlot.text = overviewText

                val tvSeasonNameTAG = "tvSeasonNameTAG"

                if (item.seasonName.isNullOrEmpty() || item.seasonName == item.seasonNumber) {
                    tvSeasonName.tag = tvSeasonNameTAG
                    tvSeasonName.isGone = true
                } else {
                    tvSeasonName.tag = null
                    tvSeasonName.text = item.seasonName
                }

                tvSeasonName.isGone = tvSeasonName.tag == tvSeasonNameTAG

                GlideApp.with(ivPoster)
                    .load(posterUrl)
                    .placeholder(R.drawable.no_thumb)
                    .into(ivPoster)
            }
        }
    }

    class ListViewHolder(
        context: Context,
        private val itemBinding: ItemListBinding,
        private val episodesDataAdapter: EpisodesDataAdapter
    ) : EpisodesViewHolder(context, itemBinding) {

        fun bindEpisodes(item: EpisodesDataModel.Episodes) {
            val episodeAdapter by lazy {
                EpisodeAdapter(
                    context = context,
                    episodeOnClick = { episode, accessToken, isLast ->
                        episodesDataAdapter.setOnEpisodeClick.invoke(
                            episode, accessToken, isLast
                        )
                    },
                    accessToken = item.accessToken
                )
            }
            itemBinding.rvList.apply {
                adapter = episodeAdapter
                layoutManager = LinearLayoutManager(
                    context, LinearLayoutManager.VERTICAL, false
                )
            }
            episodeAdapter.submitList(item.episodes)
        }
    }
}