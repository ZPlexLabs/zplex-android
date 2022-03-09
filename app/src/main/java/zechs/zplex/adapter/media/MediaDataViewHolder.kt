package zechs.zplex.adapter.media

import android.transition.TransitionManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import zechs.zplex.R
import zechs.zplex.ThisApp.Companion.context
import zechs.zplex.adapter.media.adapters.*
import zechs.zplex.databinding.ItemDetailBinding
import zechs.zplex.databinding.ItemMediaMetaBinding
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp

sealed class MediaDataViewHolder(
    binding: ViewBinding,
    val mediaDataAdapter: MediaDataAdapter
) : RecyclerView.ViewHolder(binding.root) {

    class MetaViewHolder(
        private val itemBinding: ItemMediaMetaBinding,
        mediaDataAdapter: MediaDataAdapter
    ) : MediaDataViewHolder(itemBinding, mediaDataAdapter) {

        private val miscAdapter by lazy { MiscAdapter() }

        fun bind(item: MediaDataModel.Meta) {
            val posterUrl = if (item.posterUrl == null) {
                R.drawable.no_poster
            } else {
                "${TMDB_IMAGE_PREFIX}/${PosterSize.w500}${item.posterUrl}"
            }

            context?.let { c ->
                GlideApp.with(c)
                    .load(posterUrl)
                    .placeholder(R.drawable.no_poster)
                    .into(itemBinding.ivPoster)

                itemBinding.posterCard.transitionName = "SHARED_${item.tmdbId}"
                itemBinding.ivPoster.setOnClickListener {
                    val aboutDataModel = AboutDataModel.Header(heading = item.posterUrl)
                    mediaDataAdapter.onItemClickListener?.let { it(aboutDataModel) }
                }
            }

            itemBinding.apply {
                tvTitle.text = item.title
                tvPlot.text = if (item.overview.isNullOrEmpty()) {
                    "No description"
                } else item.overview
                tvPlot.setOnClickListener {
                    TransitionManager.beginDelayedTransition(itemBinding.root)
                    tvPlot.maxLines = if (tvPlot.lineCount > 4) 4 else 1000
                }
                context?.let { c ->
                    rvListMisc.apply {
                        adapter = miscAdapter
                        layoutManager = LinearLayoutManager(
                            c, LinearLayoutManager.HORIZONTAL, false
                        )
                        itemAnimator = null
                    }
                }
                miscAdapter.differ.submitList(item.misc)
            }

        }
    }


    class DetailsViewHolder(
        private val itemBinding: ItemDetailBinding,
        mediaDataAdapter: MediaDataAdapter
    ) : MediaDataViewHolder(itemBinding, mediaDataAdapter) {

        private val seasonsAdapter by lazy { SeasonsAdapter() }
        private val castAdapter by lazy { CastAdapter() }
        private val relatedAdapter by lazy { CurationAdapter() }
        private val recommendationAdapter by lazy { CurationAdapter() }
        private val videosAdapter by lazy { VideosAdapter() }


        fun bind(item: MediaDataModel.Details) {
            when (item.header) {
                "Seasons" -> {
                    val seasonsList = item.items.filterIsInstance<AboutDataModel.Season>()
                    itemBinding.apply {
                        context?.let { c ->
                            tvHeader.text = c.resources.getString(R.string.seasons)
                            rvList.apply {
                                adapter = seasonsAdapter
                                layoutManager = LinearLayoutManager(
                                    c, LinearLayoutManager.HORIZONTAL, false
                                )
                                // itemAnimator = null
                                setItemViewCacheSize(0)
                                setHasFixedSize(false)
                            }
                        }
                        seasonsAdapter.differ.submitList(seasonsList)
                        seasonsAdapter.setOnItemClickListener { aboutDataModel ->
                            mediaDataAdapter.onItemClickListener?.let { it(aboutDataModel) }
                        }
                    }
                }
                "Related movies" -> {
                    val relatedList = item.items.filterIsInstance<AboutDataModel.Curation>()
                    itemBinding.apply {
                        context?.let { c ->
                            tvHeader.text = c.resources.getString(R.string.related_movies)
                            rvList.apply {
                                adapter = relatedAdapter
                                layoutManager = LinearLayoutManager(
                                    c, LinearLayoutManager.HORIZONTAL, false
                                )
                                // itemAnimator = null
                                setItemViewCacheSize(0)
                                setHasFixedSize(false)
                            }
                        }
                        relatedAdapter.differ.submitList(relatedList)
                        relatedAdapter.setOnItemClickListener { aboutDataModel ->
                            mediaDataAdapter.onItemClickListener?.let { it(aboutDataModel) }
                        }
                    }
                }
                "Cast" -> {
                    val castsList = item.items.filterIsInstance<AboutDataModel.Cast>()
                    itemBinding.apply {
                        context?.let { c ->
                            tvHeader.text = c.resources.getString(R.string.cast)
                            rvList.apply {
                                adapter = castAdapter
                                layoutManager = GridLayoutManager(
                                    c, 2, LinearLayoutManager.HORIZONTAL, false
                                )
                                // itemAnimator = null
                                setItemViewCacheSize(0)
                                setHasFixedSize(false)
                            }
                        }
                        // btnHeader.isInvisible = false
                        btnHeader.setOnClickListener {
                            mediaDataAdapter.viewAllOnClick("click")
                        }
                        castAdapter.differ.submitList(castsList)
                        castAdapter.setOnItemClickListener { aboutDataModel ->
                            mediaDataAdapter.onItemClickListener?.let { it(aboutDataModel) }
                        }
                    }
                }
                "Recommendations" -> {
                    val recommendationsList = item.items.filterIsInstance<AboutDataModel.Curation>()
                    itemBinding.apply {
                        context?.let { c ->
                            tvHeader.text = c.resources.getString(R.string.recommendations)
                            rvList.apply {
                                adapter = recommendationAdapter
                                layoutManager = LinearLayoutManager(
                                    c, LinearLayoutManager.HORIZONTAL, false
                                )
                                // itemAnimator = null
                                setItemViewCacheSize(0)
                                setHasFixedSize(false)
                            }
                        }
                        recommendationAdapter.differ.submitList(recommendationsList)
                        recommendationAdapter.setOnItemClickListener { aboutDataModel ->
                            mediaDataAdapter.onItemClickListener?.let { it(aboutDataModel) }
                        }
                    }
                }
                "Related videos" -> {
                    val videosList = item.items.filterIsInstance<AboutDataModel.Video>()
                    itemBinding.apply {
                        context?.let { c ->
                            tvHeader.text = c.resources.getString(R.string.related_videos)
                            rvList.apply {
                                adapter = videosAdapter
                                layoutManager = LinearLayoutManager(
                                    c, LinearLayoutManager.HORIZONTAL, false
                                )
                                // itemAnimator = null
                                setItemViewCacheSize(0)
                                setHasFixedSize(false)
                            }
                        }
                        videosAdapter.differ.submitList(videosList)
                        videosAdapter.setOnItemClickListener { aboutDataModel ->
                            mediaDataAdapter.onItemClickListener?.let { it(aboutDataModel) }
                        }
                    }
                }
                else -> {
                    itemBinding.rvList.adapter = null
                }
            }

        }
    }


}