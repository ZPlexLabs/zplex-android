package zechs.zplex.ui.fragment.collection

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import zechs.zplex.R
import zechs.zplex.adapter.CollectionAdapter
import zechs.zplex.databinding.FragmentCollectionBinding
import zechs.zplex.models.tmdb.BackdropSize
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.models.tmdb.collection.CollectionsResponse
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.ui.BaseFragment
import zechs.zplex.ui.activity.main.MainActivity
import zechs.zplex.ui.fragment.image.BigImageViewModel
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp
import zechs.zplex.utils.Resource
import zechs.zplex.utils.navigateSafe

class FragmentCollection : BaseFragment() {

    private var _binding: FragmentCollectionBinding? = null
    private val binding get() = _binding!!

    private val bigImageViewModel by activityViewModels<BigImageViewModel>()
    private lateinit var collectionViewModel: CollectionViewModel
    private val args by navArgs<FragmentCollectionArgs>()

    private val collectionsAdapter by lazy {
        CollectionAdapter { navigateMedia(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionBinding.inflate(inflater, container, false)
        binding.ivPoster.transitionName = args.media.poster_path
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectionViewModel = (activity as MainActivity).collectionViewModel
        setupRecyclerView()
        setupCollectionViewModel(args.media.id)

        val posterUrl = if (args.media.poster_path == null) {
            R.drawable.no_poster
        } else {
            "${TMDB_IMAGE_PREFIX}/${PosterSize.w500}${args.media.poster_path}"
        }

        binding.apply {
            GlideApp.with(ivPoster)
                .load(posterUrl)
                .placeholder(R.drawable.no_poster)
                .addListener(glideRequestListener)
                .into(ivPoster)

            tvTitle.text = args.media.let { args.media.name ?: args.media.title }

            args.media.overview?.let { plot ->
                spannablePlotText(tvPlot, plot, 200, "...more")
                if (plot.isEmpty()) tvPlot.isGone = true
            }

            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
            ivPoster.setOnClickListener {
                args.media.poster_path?.let { it1 -> openImageFullSize(it1, binding.ivPoster) }
            }
            ivBackdrop.setOnClickListener {
                args.media.backdrop_path?.let { it1 -> openImageFullSize(it1, binding.ivBackdrop) }
            }
        }
    }

    private fun setupCollectionViewModel(collectionId: Int) {
        collectionViewModel.getCollection(collectionId)
            .observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Resource.Success -> response.data?.let { doOnMediaSuccess(it) }
                    is Resource.Error -> {}
                    is Resource.Loading -> isLoading(true)
                }
            }
    }

    private fun isLoading(hide: Boolean) {
        binding.loading.isInvisible = !hide
    }

    private fun doOnMediaSuccess(response: CollectionsResponse) {

        val backdropUrl = if (response.backdrop_path == null) {
            R.drawable.no_thumb
        } else {
            "$TMDB_IMAGE_PREFIX/${BackdropSize.w780}${response.backdrop_path}"
        }

        binding.apply {
            GlideApp.with(ivBackdrop)
                .load(backdropUrl)
                .placeholder(R.drawable.no_poster)
                .into(ivBackdrop)

            tvTitle.text = response.name
            response.overview?.let { plot ->
                spannablePlotText(tvPlot, plot, 200, "...more")
                if (plot.isEmpty()) tvPlot.isGone = true
            }
        }

        val parts = response.parts.sortedBy {
            if (it.release_date.isNullOrEmpty()) {
                9999
            } else it.release_date.take(4).toInt()
        }

        collectionsAdapter.differ.submitList(parts)
        isLoading(false)
    }

    private fun setupRecyclerView() {
        binding.rvList.apply {
            adapter = collectionsAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
            itemAnimator = null
        }
    }

    private val glideRequestListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            parentFragment?.startPostponedEnterTransition()
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            parentFragment?.startPostponedEnterTransition()
            return false
        }
    }

    private fun openImageFullSize(posterPath: String, imageView: ImageView) {
        imageView.transitionName = posterPath
        this.exitTransition = null
        bigImageViewModel.setImagePath(posterPath)

        val action = FragmentCollectionDirections.actionFragmentCollectionToBigImageFragment()
        val extras = FragmentNavigatorExtras(
            imageView to imageView.transitionName
        )
        findNavController().navigate(action, extras)
        Log.d("navigateToMedia", imageView.transitionName)
    }

    private fun navigateMedia(media: Media) {
        val action = FragmentCollectionDirections.actionFragmentCollectionToFragmentMedia(
            media.copy(media_type = media.media_type ?: "movie")
        )
        findNavController().navigateSafe(action)
    }

    private fun spannablePlotText(
        textView: TextView, plot: String,
        limit: Int, suffixText: String
    ) {
        val textColor = ForegroundColorSpan(Color.parseColor("#BDFFFFFF"))
        val suffixColor = ForegroundColorSpan(Color.parseColor("#DEFFFFFF"))

        if (plot.length > 250) {
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

            textView.apply {
                setText(stringBuilder, TextView.BufferType.SPANNABLE)

                setOnClickListener {
                    TransitionManager.beginDelayedTransition(binding.root)
                    if (text.length > (limit + suffixText.length)) {
                        setText(stringBuilder, TextView.BufferType.SPANNABLE)
                    } else text = plot
                }
            }
        } else {
            textView.text = plot
            textView.setOnClickListener(null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}