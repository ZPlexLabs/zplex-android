package zechs.zplex.adapter.media

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.transition.MaterialFade
import zechs.zplex.R
import zechs.zplex.adapter.shared_adapters.casts.CastAdapter
import zechs.zplex.adapter.shared_adapters.media.MediaAdapter
import zechs.zplex.adapter.shared_adapters.video.VideoAdapter
import zechs.zplex.databinding.*
import zechs.zplex.models.tmdb.BackdropSize
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp

sealed class MediaViewHolder(
    val context: Context,
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    class HeadingViewHolder(
        context: Context,
        private val itemBinding: ItemHeadingBinding
    ) : MediaViewHolder(context, itemBinding) {
        fun bind(item: MediaDataModel.Heading) {
            itemBinding.tvText.text = item.heading
        }
    }

    class HeaderViewHolder(
        context: Context,
        private val itemBinding: ItemMediaHeaderBinding,
        mediaDataAdapter: MediaDataAdapter
    ) : MediaViewHolder(context, itemBinding) {

        private val listener = mediaDataAdapter.mediaClickListener
        private var hasLoadedResource = false
        private var isPoster = false

        private val glideRequestListener = object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                if (!hasLoadedResource && resource != null && isPoster) {
                    listener.setImageResource(resource)
                    hasLoadedResource = true
                }
                return false
            }
        }

        fun bind(item: MediaDataModel.Header) {
            val posterUrl = if (item.posterPath == null) R.drawable.no_poster else {
                isPoster = true
                "$TMDB_IMAGE_PREFIX/${PosterSize.w500}${item.posterPath}"
            }
            val backdropUrl = if (item.backdropPath == null) R.drawable.no_thumb else {
                "$TMDB_IMAGE_PREFIX/${BackdropSize.w780}${item.backdropPath}"
            }
            itemBinding.apply {
                GlideApp.with(ivPoster)
                    .load(posterUrl)
                    .addListener(glideRequestListener)
                    .placeholder(R.drawable.no_poster)
                    .into(ivPoster)

                GlideApp.with(ivBackdrop)
                    .load(backdropUrl)
                    .placeholder(R.drawable.no_thumb)
                    .into(ivBackdrop)

                rbRating.rating = item.rating.toFloat()
                tvRatingText.text = item.rating.toString()
                tvGenre.text = item.genre
                tvRuntime.text = item.runtime

                val rbRatingTag = "rbRatingTAG"
                if (rbRating.tag != rbRatingTag) {
                    listener.setRatingBarView(rbRating)
                }
                rbRating.tag = rbRatingTag
            }
        }
    }

    class TitleViewHolder(
        context: Context,
        private val itemBinding: ItemMediaTitleBinding
    ) : MediaViewHolder(context, itemBinding) {

        private fun spannablePlotText(
            textView: TextView, plot: String,
            limit: Int, suffixText: String,
            root: ViewGroup
        ) {
            val textColor = ForegroundColorSpan(Color.parseColor("#BDFFFFFF"))
            val suffixColor = ForegroundColorSpan(Color.parseColor("#DEFFFFFF"))

            if (plot.length > 200) {
                val stringBuilder = SpannableStringBuilder()

                val plotText = SpannableString(plot.substring(0, limit)).apply {
                    setSpan(textColor, 0, limit, 0)
                }

                val readMore = SpannableString(suffixText).apply {
                    setSpan(
                        StyleSpan(Typeface.BOLD),
                        0, suffixText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(suffixColor, 0, suffixText.length, 0)
                }

                stringBuilder.append(plotText)
                stringBuilder.append(readMore)

                val textViewTag = "textViewTAG"
                textView.apply {
                    if (tag != null) {
                        text = plot
                    } else {
                        setText(stringBuilder, TextView.BufferType.SPANNABLE)
                    }
                    setOnClickListener {
                        TransitionManager.beginDelayedTransition(root)
                        if (text.length > (limit + suffixText.length)) {
                            setText(stringBuilder, TextView.BufferType.SPANNABLE)
                            tag = null
                        } else {
                            text = plot
                            tag = textViewTag
                        }
                    }
                }
            } else {
                textView.text = plot
                textView.setOnClickListener(null)
            }
        }

        fun bind(item: MediaDataModel.Title) {
            itemBinding.apply {
                tvTitle.text = item.title
                spannablePlotText(tvPlot, item.plot, 160, "...more", rootView)
            }
        }
    }

    class LatestSeasonViewHolder(
        context: Context,
        private val itemBinding: ItemMediaSeasonBinding,
        private val mediaDataAdapter: MediaDataAdapter
    ) : MediaViewHolder(context, itemBinding) {
        fun bind(item: MediaDataModel.LatestSeason) {
            val seasonPosterUrl = if (item.seasonPosterPath == null) R.drawable.no_poster else {
                "$TMDB_IMAGE_PREFIX/${PosterSize.w342}${item.seasonPosterPath}"
            }
            itemBinding.apply {
                GlideApp.with(ivSeasonPoster)
                    .load(seasonPosterUrl)
                    .placeholder(R.drawable.no_poster)
                    .into(ivSeasonPoster)
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
        context: Context,
        private val itemBinding: ItemMediaCollectionBinding,
        private val mediaDataAdapter: MediaDataAdapter
    ) : MediaViewHolder(context, itemBinding) {
        fun bind(item: MediaDataModel.PartOfCollection) {
            itemBinding.apply {
                val bannerUrl = if (item.bannerPoster == null) R.drawable.no_thumb else {
                    "$TMDB_IMAGE_PREFIX/${BackdropSize.w780}${item.bannerPoster}"
                }
                GlideApp.with(ivBanner)
                    .load(bannerUrl)
                    .placeholder(R.drawable.no_thumb)
                    .into(ivBanner)

                tvCollection.text = item.collectionName
                root.setOnClickListener {
                    mediaDataAdapter.mediaClickListener.collectionClick(item.collectionId)
                }
            }
        }
    }

    class ButtonViewHolder(
        context: Context,
        private val itemBinding: ItemMediaButtonsBinding,
        mediaDataAdapter: MediaDataAdapter
    ) : MediaViewHolder(context, itemBinding) {

        private fun getDrawable(@DrawableRes drawable: Int): Drawable? {
            return ContextCompat.getDrawable(context, drawable)
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
                    listener.showShare(item.show)
                }
            }
        }

        fun bindMovie(item: MediaDataModel.MovieButton) {
            itemBinding.apply {
                btnWatchNow.apply {
                    text = context.getString(R.string.watch_now)
                    icon = getDrawable(R.drawable.ic_round_play_circle_outline_24)
                    setOnClickListener {
                        listener.movieWatchNow(item.movie.id)
                    }

                    val btnWatchNowTag = "btnWatchNowTAG"
                    if (tag != btnWatchNowTag) {
                        listener.setButtonView(this)
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
                    listener.movieShare(item.movie)
                }
            }
        }
    }

    class ListViewHolder(
        context: Context,
        private val itemBinding: ItemListBinding,
        mediaDataAdapter: MediaDataAdapter
    ) : MediaViewHolder(context, itemBinding) {

        private val mediaAdapter by lazy {
            MediaAdapter {
                mediaDataAdapter.mediaClickListener.onClickMedia(it)
            }
        }

        private val linearLayoutManager = object : LinearLayoutManager(
            context, HORIZONTAL, false
        ) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                return lp?.let {
                    it.width = (0.30 * width).toInt()
                    true
                } ?: super.checkLayoutParams(lp)
            }
        }

        fun bindRecommendations(item: MediaDataModel.Recommendations) {
            itemBinding.rvList.apply {
                adapter = mediaAdapter
                layoutManager = linearLayoutManager
            }

            mediaAdapter.submitList(item.recommendations)
        }

        fun bindMoreFromCompany(item: MediaDataModel.MoreFromCompany) {
            itemBinding.rvList.apply {
                adapter = mediaAdapter
                layoutManager = linearLayoutManager
            }

            mediaAdapter.submitList(item.more)
        }

        private val castAdapter by lazy {
            CastAdapter {
                mediaDataAdapter.mediaClickListener.onClickCast(it)
            }
        }

        fun bindCasts(item: MediaDataModel.Casts) {
            itemBinding.rvList.apply {
                adapter = castAdapter
                layoutManager = linearLayoutManager
            }

            castAdapter.submitList(item.casts)
        }

        private val videoAdapter by lazy {
            VideoAdapter {
                mediaDataAdapter.mediaClickListener.onClickVideo(it)
            }
        }

        fun bindVideos(item: MediaDataModel.Videos) {
            itemBinding.rvList.apply {
                adapter = videoAdapter
                layoutManager = LinearLayoutManager(
                    context, LinearLayoutManager.HORIZONTAL, false
                )
            }

            videoAdapter.submitList(item.videos)
        }
    }
}