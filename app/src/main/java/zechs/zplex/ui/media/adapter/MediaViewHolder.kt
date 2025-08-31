package zechs.zplex.ui.media.adapter

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import coil.request.ImageRequest
import coil.request.SuccessResult
import zechs.zplex.R
import zechs.zplex.data.model.BackdropSize
import zechs.zplex.data.model.PosterSize
import zechs.zplex.databinding.ItemListWithHeadingBinding
import zechs.zplex.databinding.ItemMediaButtonsBinding
import zechs.zplex.databinding.ItemMediaCollectionBinding
import zechs.zplex.databinding.ItemMediaHeaderBinding
import zechs.zplex.databinding.ItemMediaSeasonBinding
import zechs.zplex.databinding.ItemMediaTitleBinding
import zechs.zplex.ui.list.adapter.ListDataModel
import zechs.zplex.ui.shared_adapters.casts.CastAdapter
import zechs.zplex.ui.shared_adapters.media.MediaAdapter
import zechs.zplex.ui.shared_adapters.video.VideoAdapter
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.util.SpannableTextView.spannablePlotText

sealed class MediaViewHolder(
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    class HeaderViewHolder(
        private val itemBinding: ItemMediaHeaderBinding,
        mediaDataAdapter: MediaDataAdapter
    ) : MediaViewHolder(itemBinding) {

        private val listener = mediaDataAdapter.mediaClickListener
        private var hasLoadedResource = false
        private var isPoster = false

        private val imageRequestListener = object : ImageRequest.Listener {
            override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                super.onSuccess(request, result)
                val resource = result.drawable
                if (!hasLoadedResource && isPoster) {
                    listener.setImageResource(resource)
                    hasLoadedResource = true
                }
            }
        }

        fun bind(item: MediaDataModel.Header) {
            val posterUrl = if (item.posterPath == null) R.drawable.no_poster else {
                isPoster = true
                "$TMDB_IMAGE_PREFIX/${PosterSize.w500}${item.posterPath}"
            }
            val backdropUrl = if (item.backdropPath == null) {
                itemBinding.backdropFrame.isInvisible = true
                R.drawable.no_thumb
            } else {
                "$TMDB_IMAGE_PREFIX/${BackdropSize.w780}${item.backdropPath}"
            }
            itemBinding.apply {
                ivPoster.load(posterUrl) {
                    placeholder(R.drawable.no_poster)
                    listener(imageRequestListener)
                }

                ivBackdrop.load(backdropUrl) {
                    placeholder(R.drawable.no_thumb)
                }

                rbRating.rating = item.rating.toFloat()
                tvRatingText.text = item.rating.toString()
                tvGenre.text = item.genre
                tvRuntime.text = item.runtime

                val rbRatingTag = "rbRatingTAG"
                if (rbRating.tag != rbRatingTag) {
                    listener.setRatingBarView(rbRating)
                }
                rbRating.tag = rbRatingTag

                ivPoster.setOnClickListener {
                    listener.openImageInBig(
                        imagePath = item.posterPath,
                        imageView = ivPoster
                    )
                }
                ivImdbLogo.isGone = !item.isImdbRating
                ivBackdrop.setOnClickListener {
                    listener.openImageInBig(
                        imagePath = item.backdropPath,
                        imageView = ivBackdrop
                    )
                }
            }
        }
    }

    class TitleViewHolder(
        private val itemBinding: ItemMediaTitleBinding
    ) : MediaViewHolder(itemBinding) {

        fun bind(item: MediaDataModel.Title) {
            itemBinding.apply {
                tvTitle.text = item.title
                spannablePlotText(tvPlot, item.plot, 160, "...more", rootView)
            }
        }
    }

    class LatestSeasonViewHolder(
        private val itemBinding: ItemMediaSeasonBinding,
        private val mediaDataAdapter: MediaDataAdapter
    ) : MediaViewHolder(itemBinding) {
        fun bind(item: MediaDataModel.LatestSeason) {
            val seasonPosterUrl = if (item.seasonPosterPath == null) R.drawable.no_poster else {
                "$TMDB_IMAGE_PREFIX/${PosterSize.w342}${item.seasonPosterPath}"
            }
            itemBinding.apply {
                ivSeasonPoster.load(seasonPosterUrl) {
                    placeholder(R.drawable.no_poster)
                }
                tvSeasonNumber.text = item.seasonName
                tvYearEpisode.text = item.seasonYearAndEpisodeCount
                tvSeasonPlot.text = item.seasonPlot
                root.setOnClickListener {
                    mediaDataAdapter.mediaClickListener.lastSeasonClick(item)
                }
            }
        }
    }

    class PartOfCollectionViewHolder(
        private val itemBinding: ItemMediaCollectionBinding,
        private val mediaDataAdapter: MediaDataAdapter
    ) : MediaViewHolder(itemBinding) {
        fun bind(item: MediaDataModel.PartOfCollection) {
            itemBinding.apply {
                val bannerUrl = if (item.bannerPoster == null) R.drawable.no_thumb else {
                    "$TMDB_IMAGE_PREFIX/${BackdropSize.w780}${item.bannerPoster}"
                }
                ivBanner.load(bannerUrl) {
                    placeholder(R.drawable.no_thumb)
                }
                tvCollection.text = item.collectionName
                root.setOnClickListener {
                    mediaDataAdapter.mediaClickListener.collectionClick(item.collectionId)
                }
            }
        }
    }

    class ButtonViewHolder(
        private val itemBinding: ItemMediaButtonsBinding,
        mediaDataAdapter: MediaDataAdapter
    ) : MediaViewHolder(itemBinding) {

        private fun getDrawable(@DrawableRes drawable: Int): Drawable? {
            return ContextCompat.getDrawable(itemBinding.root.context, drawable)
        }

        private val listener = mediaDataAdapter.mediaClickListener

        fun bindShow(item: MediaDataModel.ShowButton) {
            itemBinding.apply {
                btnWatchNow.apply {
                    text = context.getString(R.string.view_all_seasons)
                    icon = getDrawable(R.drawable.ic_video_24)
                    setOnClickListener {
                        listener.showWatchNow(item.seasons)
                    }
                    val btnWatchNowTag = "btnWatchNowTAG"
                    if (tag != btnWatchNowTag) {
                        listener.setButtonView(this)
                    }
                    tag = btnWatchNowTag
                }

                btnSave.apply {
                    if (tag != item.show.id) {
                        listener.showWatchlist(this, item.show)
                    }
                    tag = item.show.id
                }

                btnShare.setOnClickListener {
                    listener.showShare(item.show.id, item.show.name, item.imdbId)
                }
            }
        }

        fun bindMovie(item: MediaDataModel.MovieButton) {
            itemBinding.apply {
                btnWatchNow.apply {
                    icon = getDrawable(R.drawable.ic_play_circle_24)
                    setOnClickListener {
                        listener.movieWatchNow(item.movie, item.year)
                    }
                    setOnLongClickListener {
                        listener.movieLongClickWatchNow(item.movie, item.year)
                        return@setOnLongClickListener true
                    }

                    val btnWatchNowTag = "btnWatchNowTAG"
                    if (tag != btnWatchNowTag) {
                        listener.setButtonView(this)
                        listener.setMovieWatchNowButton(this)
                    }
                    tag = btnWatchNowTag
                }

                btnSave.apply {
                    if (tag != item.movie.id) {
                        listener.movieWatchlist(this, item.movie)
                    }
                    tag = item.movie.id
                }

                btnShare.setOnClickListener {
                    listener.movieShare(item.movie.id, item.movie.title, item.imdbId)
                }
            }
        }
    }

    class ListViewHolder(
        private val itemBinding: ItemListWithHeadingBinding,
        private val mediaDataAdapter: MediaDataAdapter
    ) : MediaViewHolder(itemBinding) {

        private val context = itemBinding.rvList.context
        private val linearLayoutManager = object : LinearLayoutManager(context, HORIZONTAL, false) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                return lp?.let {
                    it.width = (0.30 * width).toInt()
                    true
                } ?: super.checkLayoutParams(lp)
            }
        }

        private val mediaAdapter by lazy {
            MediaAdapter {
                mediaDataAdapter.mediaClickListener.onClickMedia(it)
            }
        }

        fun bindRecommendations(item: MediaDataModel.Recommendations) {
            itemBinding.apply {
                tvText.text = item.heading
                btnViewAll.apply {
                    isInvisible = false
                    setOnClickListener {
                        mediaDataAdapter.mediaClickListener.onClickViewAll(
                            ListDataModel.Media(
                                heading = item.heading,
                                media = item.recommendations
                            )
                        )
                    }
                }
                rvList.apply {
                    adapter = mediaAdapter
                    layoutManager = linearLayoutManager
                }
            }

            mediaAdapter.submitList(item.recommendations)
        }

        fun bindMoreFromCompany(item: MediaDataModel.MoreFromCompany) {
            itemBinding.apply {
                tvText.text = item.heading
                btnViewAll.apply {
                    isInvisible = false
                    setOnClickListener {
                        mediaDataAdapter.mediaClickListener.onClickViewAll(
                            ListDataModel.Media(
                                heading = item.heading,
                                media = item.more
                            )
                        )
                    }
                }
                rvList.apply {
                    adapter = mediaAdapter
                    layoutManager = linearLayoutManager
                }
            }

            mediaAdapter.submitList(item.more)
        }

        private val castAdapter by lazy {
            CastAdapter {
                mediaDataAdapter.mediaClickListener.onClickCast(it)
            }
        }

        fun bindCasts(item: MediaDataModel.Casts) {
            itemBinding.apply {
                tvText.text = item.heading
                btnViewAll.apply {
                    isInvisible = false
                    setOnClickListener {
                        mediaDataAdapter.mediaClickListener.onClickViewAll(
                            ListDataModel.Casts(item.casts)
                        )
                    }
                }
                rvList.apply {
                    adapter = castAdapter
                    layoutManager = linearLayoutManager
                }
            }
            castAdapter.submitList(item.casts)
        }

        private val videoAdapter by lazy {
            VideoAdapter {
                mediaDataAdapter.mediaClickListener.onClickVideo(it)
            }
        }

        fun bindVideos(item: MediaDataModel.Videos) {
            itemBinding.apply {
                tvText.text = item.heading
                btnViewAll.apply {
                    isInvisible = false
                    setOnClickListener {
                        mediaDataAdapter.mediaClickListener.onClickViewAll(
                            ListDataModel.Videos(item.videos)
                        )
                    }
                }
                rvList.apply {
                    adapter = videoAdapter
                    layoutManager = LinearLayoutManager(this.context, HORIZONTAL, false)
                }
            }

            videoAdapter.submitList(item.videos)
        }
    }
}