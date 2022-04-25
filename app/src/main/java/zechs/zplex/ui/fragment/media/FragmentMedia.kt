package zechs.zplex.ui.fragment.media

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.adapter.media.AboutDataModel
import zechs.zplex.adapter.media.MediaDataAdapter
import zechs.zplex.adapter.media.MediaDataModel
import zechs.zplex.databinding.FragmentTempBinding
import zechs.zplex.models.Player
import zechs.zplex.models.dataclass.CastArgs
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show
import zechs.zplex.models.tmdb.BackdropSize
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.models.tmdb.entities.Cast
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.models.tmdb.entities.Season
import zechs.zplex.models.tmdb.entities.Video
import zechs.zplex.models.tmdb.media.MediaResponse
import zechs.zplex.ui.BaseFragment
import zechs.zplex.ui.activity.main.MainActivity
import zechs.zplex.ui.activity.player.PlayerActivity
import zechs.zplex.ui.fragment.image.BigImageViewModel
import zechs.zplex.ui.fragment.list.ListViewModel
import zechs.zplex.ui.fragment.shared_viewmodels.SeasonViewModel
import zechs.zplex.ui.movieResponseZplex
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.ConverterUtils
import zechs.zplex.utils.GlideApp
import zechs.zplex.utils.Resource
import zechs.zplex.utils.navigateSafe


class FragmentMedia : BaseFragment() {

    private var _binding: FragmentTempBinding? = null
    private val binding get() = _binding!!

    private val thisTAG = "FragmentMedia"

//    private var _movieDialog: LookupMovieDialog? = null
//    private val movieDialog get() = _movieDialog!!

    private lateinit var mediaViewModel: MediaViewModel
    private val seasonViewModel by activityViewModels<SeasonViewModel>()
    private val bigImageViewModel by activityViewModels<BigImageViewModel>()
    private val listViewModel by activityViewModels<ListViewModel>()
    private val args by navArgs<FragmentMediaArgs>()

    //    private var file: File? = null
//    private val streamsDialog by lazy {
//        StreamsDialog(
//            requireContext(),
//            onStreamClick = {
//                file?.let { f -> onStreamClick(it, f) }
//            },
//            onDownloadClick = {
//                file?.let { f -> onDownloadClick(it, f, requireContext()) }
//            }
//        )
//    }
    private val mediaDataAdapter by lazy {
        MediaDataAdapter { castsList?.let { setCastsList(it) } }
    }

    private var hasTransitionEnded = false
    private var isSuccess = false
    private var collectionName: String? = null
    private var extractedColor: Int? = null
    private var imdbId: String? = null
    private var isLastSeasonVisible = false
    private var castsList: List<Cast>? = null
    private var tmdbId: Int? = null
    private var showName: String? = null
    private var showPoster: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTempBinding.inflate(inflater, container, false)
        binding.ivPoster.transitionName = args.media.poster_path
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaViewModel = (activity as MainActivity).mediaViewModel
        setupRecyclerView()
        setDominantColorObserver()
//        setupDashStreamsObserver()

        val media = args.media

        val mediaType = media.media_type ?: when {
            media.name != null -> "tv"
            media.title != null -> "movie"
            else -> media.media_type
        }!!

        setupMediaViewModel(media.id, mediaType)

        when (mediaType) {
            "movie" -> {
                movieOnClick()
//                setupSearchObserverForMovie()
//                setupWitchMessageObserver()
                setupZplexMovieObserver()
            }
            "tv" -> {
                tvOnClick()
            }
            else -> {}
        }

        val posterUrl = if (media.poster_path == null) {
            R.drawable.no_poster
        } else {
            "$TMDB_IMAGE_PREFIX/${PosterSize.w500}${media.poster_path}"
        }

        showPoster = media.poster_path
        binding.apply {
            GlideApp.with(ivPoster)
                .load(posterUrl)
                .placeholder(R.drawable.no_poster)
                .addListener(glideRequestListener)
                .into(ivPoster)

            tvTitle.text = media.name ?: media.title
            media.overview?.let { plot ->
                spannablePlotText(tvPlot, plot, 160, "...more")
            }
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
            btnShare.setOnClickListener {
                // shareIntent()
                shareIntent(media)
            }
        }
    }

    private fun openImageFullSize(posterPath: String, imageView: ImageView) {
        imageView.transitionName = posterPath
        this.exitTransition = null
        bigImageViewModel.setImagePath(posterPath)

        val action = FragmentMediaDirections.actionFragmentMediaToBigImageFragment()
        val extras = FragmentNavigatorExtras(
            imageView to imageView.transitionName
        )
        findNavController().navigate(action, extras)
        Log.d("navigateToMedia", imageView.transitionName)
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
            resource?.let {
                mediaViewModel.calcDominantColor(it) { color ->
                    mediaViewModel.setDominantColor(color)
                }
            }
            return false
        }
    }

    override val enterTransitionListener = object : Transition.TransitionListener {
        override fun onTransitionStart(transition: Transition) {
        }

        override fun onTransitionEnd(transition: Transition) {
            if (!hasTransitionEnded && isSuccess) {
                isLoading(false)
                if (extractedColor != null && collectionName != null) {
                    spannableCollectionText(
                        binding.tvCollection,
                        collectionName!!,
                        extractedColor!!
                    )
                }
            }
            hasTransitionEnded = true
            transition.removeListener(this)
        }

        override fun onTransitionCancel(transition: Transition) {
            hasTransitionEnded = true
        }

        override fun onTransitionPause(transition: Transition) {
        }

        override fun onTransitionResume(transition: Transition) {
        }
    }


    private fun setupZplexMovieObserver() {
        mediaViewModel.movieZplex.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { movie ->
                handleZplexMovieResponse(movie)
            }
        }
    }

    private fun handleZplexMovieResponse(movie: Resource<movieResponseZplex>) {
        when (movie) {
            is Resource.Success -> {
                movie.data?.let {
                    playMovie(
                        Player(it.id, it.name, it.accessToken)
                    )
                }
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    movie.message ?: getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
            }
            is Resource.Loading -> {}
        }
    }

//    private fun setupWitchMessageObserver() {
//        mediaViewModel.witchMessage.observe(viewLifecycleOwner) { event ->
//            event.getContentIfNotHandled()?.let { message ->
//                witchDialogResponse(message)
//            }
//        }
//    }

//    private fun witchDialogResponse(message: String) {
//        movieDialog.apply {
//            changeLayouts(
//                loading = false, request = false, message = true
//            )
//            findViewById<TextView>(R.id.tv_witch_message).text = message
//        }
//    }

    private fun setupMediaViewModel(tmdbId: Int, mediaType: String) {
        mediaViewModel.getMedia(tmdbId, mediaType)
        mediaViewModel.mediaResponse.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { response ->
                when (response) {
                    is Resource.Success -> response.data?.let { doOnMediaSuccess(it) }
                    is Resource.Error -> {}
                    is Resource.Loading -> {
                        isLoading(true)
                        binding.tvCollection.isGone = true
                    }
                }
            }
        }
    }

    private fun isLoading(hide: Boolean) {
        binding.apply {
            loading.isInvisible = !hide
            rvList.isInvisible = hide
            groupUiElements.isInvisible = hide
        }
        if (!hide) showLastSeason(isLastSeasonVisible)
    }

    private fun doOnMediaSuccess(
        response: MediaResponse
    ) = viewLifecycleOwner.lifecycleScope.launch {

        setupMediaAdapterOnClickListener()

        val mediaType = args.media.media_type ?: "tv"

        when (mediaType) {
            "movie" -> {
                val movie = Movie(
                    id = response.id,
                    title = response.name ?: "",
                    media_type = "movie",
                    poster_path = response.poster_path,
                    vote_average = response.vote_average?.times(2)
                )
                setupMovieDatabaseObserver(movie)
            }
            "tv" -> {
                val show = Show(
                    id = response.id,
                    name = response.name ?: "",
                    media_type = "tv",
                    poster_path = response.poster_path,
                    vote_average = response.vote_average?.times(2)
                )
                setupShowDatabaseObserver(show)
            }
            else -> {}
        }

        val backdropUrl = if (response.backdrop_path == null) {
            R.drawable.no_thumb
        } else {
            "${TMDB_IMAGE_PREFIX}/${BackdropSize.w780}${response.backdrop_path}"
        }

        binding.apply {
            if (args.media.poster_path == null) {
                val posterUrl = "${TMDB_IMAGE_PREFIX}/${PosterSize.w500}${response.poster_path}"
                GlideApp.with(ivPoster)
                    .load(posterUrl)
                    .placeholder(R.drawable.no_poster)
                    .addListener(glideRequestListener)
                    .into(ivPoster)
                showPoster = response.poster_path
            }

            GlideApp.with(ivBackdrop)
                .load(backdropUrl)
                .placeholder(R.drawable.no_poster)
                .into(ivBackdrop)

            tvTitle.text = response.name

            response.overview?.let { plot ->
                spannablePlotText(tvPlot, plot, 160, "...more")
            }

            rbRating.rating = response.vote_average?.toFloat() ?: 0F
            tvRatingText.text = (response.vote_average?.toFloat() ?: 0F).toString()

            tvGenre.text = response.genres?.take(3)?.joinToString(
                truncated = "",
                separator = ", ",
            ) { it.name }

            var runtime = "${response.runtime ?: 0} min"
            runtime += if (response.year != null) {
                "  \u2022  ${response.year}"
            } else ""
            tvRuntime.text = runtime

            ivPoster.setOnClickListener {
                response.poster_path?.let { it1 -> openImageFullSize(it1, binding.ivPoster) }
            }
            ivBackdrop.setOnClickListener {
                response.backdrop_path?.let { it1 -> openImageFullSize(it1, binding.ivBackdrop) }
            }
        }

        tmdbId = response.id
        showName = response.name
        imdbId = response.imdb_id

        response.belongs_to_collection?.let {
            collectionName = it.name

            val collectionMedia = Media(
                id = it.id,
                media_type = null,
                name = it.name,
                poster_path = it.poster_path,
                title = it.title,
                vote_average = it.vote_average,
                backdrop_path = it.backdrop_path,
                overview = it.overview,
                release_date = it.release_date
            )
            binding.tvCollection.setOnClickListener {
                navigateToCollection(collectionMedia)
            }
        }

        val detailsList = mutableListOf<MediaDataModel.Details>()

        if (response.seasons.isNotEmpty()) {
            val lastEpisode = response.last_episode_to_air?.season_number
            lastEpisode?.let {
                val season = response.seasons.firstOrNull {
                    it.season_number == lastEpisode
                }
                season?.let {
                    val seasonPosterUrl = if (it.poster_path == null) {
                        R.drawable.no_poster
                    } else {
                        "$TMDB_IMAGE_PREFIX/${PosterSize.w342}${it.poster_path}"
                    }
                    binding.itemLastSeason.apply {
                        GlideApp.with(ivSeasonPoster)
                            .load(seasonPosterUrl)
                            .placeholder(R.drawable.no_poster)
                            .into(ivSeasonPoster)

                        val seasonName = "Season ${it.season_number}"
                        tvSeasonNumber.text = seasonName

                        var premiered = "$seasonName of ${response.name}"
                        var yearSeason = ""

                        val formattedDate = it.air_date?.let { date ->
                            yearSeason += "${date.take(4)} | "
                            ConverterUtils.parseDate(date)
                        }

                        yearSeason += "${it.episode_count} episodes"
                        tvYearEpisode.text = yearSeason

                        formattedDate?.let {
                            premiered += " premiered on $formattedDate."
                        }
                        val seasonPlot = if (it.overview.toString() == "") {
                            premiered
                        } else it.overview
                        tvSeasonPlot.text = seasonPlot
                        root.setOnClickListener { _ ->
                            navigateToSeason(
                                tmdbId = response.id,
                                seasonName = it.name,
                                seasonNumber = it.season_number,
                                showName = response.name,
                                posterPath = it.poster_path,
                                showPoster = showPoster
                            )
                        }

                    }
                }
                isLastSeasonVisible = true
            }

            binding.btnWatchNow.setOnClickListener {
                setSeasonsList(response.seasons)
            }
        } else {
            isLastSeasonVisible = false
        }

        castsList = response.cast
        if (response.cast.isNotEmpty()) {
            val castsList = response.cast.map {
                AboutDataModel.Cast(
                    character = it.character,
                    credit_id = it.credit_id,
                    person_id = it.id,
                    name = it.name,
                    profile_path = it.profile_path
                )
            }.take(8)

            detailsList.add(
                MediaDataModel.Details(
                    header = resources.getString(R.string.cast),
                    items = castsList
                )
            )
        }

        if (response.recommendations.isNotEmpty()) {
            val recommendationsList = response.recommendations.take(8)
                .filter { it.backdrop_path != null }
                .map { it.toCuration() }

            detailsList.add(
                MediaDataModel.Details(
                    header = resources.getString(R.string.recommendations),
                    items = recommendationsList
                )
            )
        }

        if (response.videos.isNotEmpty()) {
            val trailer = response.videos.firstOrNull() {
                it.site == "YouTube" && it.official && it.type == "Trailer"
            }
            trailer?.let { setupTrailer(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mediaDataAdapter.differ.submitList(detailsList.toList())
        }
        if (extractedColor != null && collectionName != null && hasTransitionEnded) {
            spannableCollectionText(binding.tvCollection, collectionName!!, extractedColor!!)
        }

        if (hasTransitionEnded) isLoading(false)
        isSuccess = true

    }

    private fun setupTrailer(trailer: Video) {
        binding.itemTrailer.apply {
            GlideApp.with(ivThumbnail)
                .load(trailer.thumbUrl)
                .placeholder(R.drawable.no_thumb)
                .into(ivThumbnail)


            root.isGone = false
            root.setOnClickListener { openWebLink(trailer.watchUrl) }

            tvVideoName.text = trailer.name
            tvSource.text = trailer.site
        }
    }

    private fun openWebLink(webUrl: String) {
        val launchWebIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
        startActivity(launchWebIntent)
    }

    private fun setupRecyclerView() {
        binding.rvList.apply {
            adapter = mediaDataAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
            itemAnimator = null
        }
    }

    private fun setupMediaAdapterOnClickListener() {
        mediaDataAdapter.setOnItemClickListener {
            when (it) {
                is AboutDataModel.Curation -> {
                    val media = it.toMedia()
                    val action = FragmentMediaDirections.actionFragmentMediaSelf(
                        media.copy(media_type = it.media_type)
                    )
                    findNavController().navigate(action)
                }
                is AboutDataModel.Cast -> {
                    val action = FragmentMediaDirections.actionFragmentMediaToCastsFragment(
                        CastArgs(it.credit_id, it.person_id, it.name, it.profile_path)
                    )
                    findNavController().navigateSafe(action)
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvList.adapter = null
        _binding = null
    }

    private fun setDominantColorObserver() {
        mediaViewModel.dominantColor.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { c ->
                context?.let { onDominantColor(c) }
            }
        }
    }

    private fun onDominantColor(c: Int) {
        binding.apply {
            val color = if (isDark(c)) lightUpColor(c) else c
            val tintColor = ColorStateList.valueOf(color)

            btnWatchNow.backgroundTintList = tintColor
            btnWatchNow.iconTint = ColorStateList.valueOf(getContrastColor(color))
            btnWatchNow.setTextColor(getContrastColor(color))

//            btnSave.setTextColor(tintColor)
//            btnSave.iconTint = tintColor
//
//            btnShare.setTextColor(tintColor)
//            btnShare.iconTint = tintColor

            rbRating.progressTintList = tintColor
            rbRating.progressBackgroundTintList = tintColor
            rbRating.secondaryProgressTintList = tintColor

            loading.indeterminateTintList = tintColor
            if (hasTransitionEnded && collectionName != null) {
                spannableCollectionText(tvCollection, collectionName!!, color)
            }
            extractedColor = color
        }
    }

    private fun getContrastColor(color: Int): Int {
        val y = (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.blue(color)) / 1000
        return if (y >= 128) Color.parseColor("#151515") else Color.parseColor("#DEFFFFFF")
    }

    private fun isDark(color: Int): Boolean {
        val luminance = ("%.5f".format(ColorUtils.calculateLuminance(color))).toFloat()
        val threshold = 0.09000
        val isDark = luminance < threshold
        Log.d(thisTAG, "luminance=$luminance, threshold=$threshold, isDark=$isDark")
        return isDark
    }

    private fun lightUpColor(color: Int): Int {
        return Color.HSVToColor(FloatArray(3).apply {
            Color.colorToHSV(color, this)
            this[2] *= 2.0f
        })
    }

    private fun spannablePlotText(
        textView: TextView, plot: String,
        limit: Int, suffixText: String
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

    private fun spannableCollectionText(
        textView: TextView, collectionName: String, tintColor: Int
    ) {
        val prefixText = "Part of the "
        val textColor = ForegroundColorSpan(Color.parseColor("#BDFFFFFF"))
        val accentColor = ForegroundColorSpan(tintColor)
        val stringBuilder = SpannableStringBuilder()

        val prefix = SpannableString(prefixText).apply {
            setSpan(textColor, 0, prefixText.length, 0)
        }

        val collection = SpannableString(collectionName).apply {
            setSpan(
                StyleSpan(Typeface.BOLD),
                0, collectionName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(accentColor, 0, collectionName.length, 0)
        }

        stringBuilder.append(prefix)
        stringBuilder.append(collection)

        textView.apply {
            setText(stringBuilder, TextView.BufferType.SPANNABLE)
            isGone = false
        }
    }

    private fun setupShowDatabaseObserver(show: Show) {
        var updateSaved = false
        mediaViewModel.getShow(show.id).observe(viewLifecycleOwner) { isSaved ->
            Log.d("getShow", "isSaved=$isSaved")
            binding.btnSave.apply {
                icon = ContextCompat.getDrawable(
                    context,
                    if (isSaved) {
                        R.drawable.ic_round_playlist_add_check_24
                    } else R.drawable.ic_add_24
                )
                setOnClickListener {
                    if (isSaved) {
                        mediaViewModel.deleteShow(show)
                        val snackBar = Snackbar.make(
                            binding.mainView, "${show.name} removed from your library",
                            Snackbar.LENGTH_SHORT
                        )
                        snackBar.setAction(
                            R.string.undo
                        ) {
                            mediaViewModel.saveShow(show)
                        }
                        snackBar.show()
                    } else {
                        mediaViewModel.saveShow(show)
                        val snackBar = Snackbar.make(
                            binding.mainView, "${show.name} added to your library",
                            Snackbar.LENGTH_SHORT
                        )
                        snackBar.setAction(
                            R.string.undo
                        ) {
                            mediaViewModel.deleteShow(show)
                        }
                        snackBar.show()
                    }
                }
            }
            if (isSaved && !updateSaved) {
                mediaViewModel.saveShow(show)
                updateSaved = true
            }
        }
    }

    private fun setupMovieDatabaseObserver(movie: Movie) {
        var updateSaved = false
        mediaViewModel.getMovie(movie.id).observe(viewLifecycleOwner) { isSaved ->
            Log.d("getMovie", "isSaved=$isSaved")
            binding.btnSave.apply {
                icon = ContextCompat.getDrawable(
                    context,
                    if (isSaved) {
                        R.drawable.ic_round_playlist_add_check_24
                    } else R.drawable.ic_add_24
                )
                setOnClickListener {
                    if (isSaved) {
                        mediaViewModel.deleteMovie(movie)
                        val snackBar = Snackbar.make(
                            binding.mainView, "${movie.title} removed from your library",
                            Snackbar.LENGTH_SHORT
                        )
                        snackBar.setAction(
                            R.string.undo
                        ) {
                            mediaViewModel.saveMovie(movie)
                        }
                        snackBar.show()
                    } else {
                        mediaViewModel.saveMovie(movie)
                        val snackBar = Snackbar.make(
                            binding.mainView, "${movie.title} added to your library",
                            Snackbar.LENGTH_SHORT
                        )
                        snackBar.setAction(
                            R.string.undo
                        ) {
                            mediaViewModel.deleteMovie(movie)
                        }
                        snackBar.show()
                    }
                }
            }
            if (isSaved && !updateSaved) {
                mediaViewModel.saveMovie(movie)
                updateSaved = true
            }
        }
    }

    private fun shareIntent() {
        val action = FragmentMediaDirections.actionFragmentMediaToShareBottomSheet()
        findNavController().navigateSafe(action)
    }

    private fun shareIntent(media: Media) {
        val showName = media.name ?: media.title
        val mediaType = media.media_type ?: "tv"
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "https://www.themoviedb.org/${mediaType}/${media.id}"
        )
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(sendIntent, showName)
        startActivity(shareIntent)
    }

//    private fun showMovieDialog(context: Context) {
//        if (_movieDialog == null) {
//            _movieDialog = LookupMovieDialog(context, extractedColor) { btnYesListener() }
//        }
//        movieDialog.show()
//
//        movieDialog.window?.apply {
//            attributes.windowAnimations = R.style.DialogAnimation
//            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            setLayout(
//                Constraints.LayoutParams.MATCH_PARENT,
//                Constraints.LayoutParams.WRAP_CONTENT
//            )
//        }
//
//        mediaViewModel.doSearchFor(args.media.tmdbId)
//    }

//    private fun getDriveDownloadUrl(fileId: String): String {
//        return "https://www.googleapis.com/drive/v3/files/${fileId}?supportAllDrives=True&alt=media"
//    }
//
//    private fun onDownloadClick(it: StreamsDataModel, file: File, context: Context) {
//        when (it) {
//            is StreamsDataModel.Original -> {
//                val outputPath = "$showName${file.name.takeLast(4)}"
//                queueDownload(
//                    context,
//                    getDriveDownloadUrl(file.id), outputPath,
//                    "Authorization",
//                    "Bearer ${SessionManager(context).fetchAuthToken()}"
//                )
//                streamsDialog.dismiss()
//            }
//            is StreamsDataModel.Stream -> {
//                val outputPath = "$showName${file.name.takeLast(4)}"
//                queueDownload(
//                    context, it.url, outputPath,
//                    "Cookie",
//                    "DRIVE_STREAM=${it.cookie}"
//                )
//                streamsDialog.dismiss()
//            }
//            else -> {}
//        }
//    }

//    private fun onStreamClick(it: StreamsDataModel, file: File) {
//        when (it) {
//            is StreamsDataModel.Original -> {
//                playMovie(file, null, null)
//            }
//            is StreamsDataModel.Stream -> {
//                playMovie(file, it.cookie, it.url)
//            }
//            else -> {}
//        }
//        streamsDialog.dismiss()
//    }

//    private fun showStreamsDialog(context: Context, f: File) {
//        file = f
//        streamsDialog.show()
//
//        streamsDialog.window?.apply {
//            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            setLayout(
//                Constraints.LayoutParams.MATCH_PARENT,
//                Constraints.LayoutParams.WRAP_CONTENT
//            )
//        }
//
//        val streamsView = streamsDialog.findViewById<RecyclerView>(R.id.rv_streams)
//
//        streamsView.apply {
//            adapter = streamsDialog.streamsDataAdapter
//            layoutManager = LinearLayoutManager(
//                context, LinearLayoutManager.VERTICAL, false
//            )
//        }
//
//        val adapterDiff = streamsDialog.streamsDataAdapter.differ
//        val streamsList = mutableListOf<StreamsDataModel>()
//        streamsList.add(
//            StreamsDataModel.Original(
//                title = "Original",
//                id = f.id
//            )
//        )
//
//        streamsList.add(StreamsDataModel.Loading)
//
//        adapterDiff.submitList(streamsList.toList())
//        mediaViewModel.getVideoInfo(f.id)
//    }

//    private fun btnYesListener() {
//        @SuppressLint("HardwareIds")
//        val deviceId: String = if (BuildConfig.DEBUG) {
//            "ZPLEX_TEST_CHANNEL"
//        } else {
//            Settings.Secure.getString(
//                context?.contentResolver, Settings.Secure.ANDROID_ID
//            )
//        }
//        movieDialog.changeLayouts(loading = true, request = false, message = false)
//        imdbId?.let { it -> mediaViewModel.requestMovie(it, tmdbId!!.toString(), deviceId) }
//        Log.d("btnYesListener", "imdbId=$imdbId")
//    }

    private fun movieOnClick() {
        binding.btnWatchNow.setOnClickListener {
            tmdbId?.let { it1 -> mediaViewModel.zplexGetMovie(it1) }
        }
    }


    private fun tvOnClick() {
        binding.btnWatchNow.apply {
            context?.let {
                text = it.getString(R.string.view_all_seasons)
                icon = ContextCompat.getDrawable(it, R.drawable.ic_video_24)
            }
        }
    }

//    private fun setupSearchObserverForMovie() {
//        mediaViewModel.searchList.observe(viewLifecycleOwner) { event ->
//            event.getContentIfNotHandled()?.let { responseEpisode ->
//                handleSearchResponse(responseEpisode)
//            }
//        }
//    }
//
//    private fun setupDashStreamsObserver() {
//        mediaViewModel.dashVideo.observe(viewLifecycleOwner) { event ->
//            event.getContentIfNotHandled()?.let { streamsResponse ->
//                handleDashStreamsResponse(streamsResponse)
//            }
//        }
//    }
//
//    private fun handleDashStreamsResponse(
//        streamsResponse: Resource<DashResponse>
//    ) {
//        when (streamsResponse) {
//            is Resource.Success -> {
//                val data = streamsResponse.data
//                if (data != null) {
//                    handleStreamsSuccess(data)
//                }
//                data?.error?.let {
//                    Toast.makeText(
//                        requireContext(),
//                        it, Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//            else -> {
//                val adapterDiff = streamsDialog.streamsDataAdapter.differ
//                val currentList = adapterDiff.currentList
//                val streamsList = mutableListOf<StreamsDataModel>()
//                streamsList.add(currentList.filterIsInstance<StreamsDataModel.Original>()[0])
//                adapterDiff.submitList(streamsList.toList())
//            }
//        }
//    }
//
//    private fun handleStreamsSuccess(dashResponse: DashResponse) {
//        val streams = dashResponse.streams
//        val adapterDiff = streamsDialog.streamsDataAdapter.differ
//        val currentList = adapterDiff.currentList
//
//        val streamsList = mutableListOf<StreamsDataModel>()
//        streamsList.add(currentList.filterIsInstance<StreamsDataModel.Original>()[0])
//
//        for (stream in streams) {
//            streamsList.add(
//                StreamsDataModel.Stream(
//                    name = stream.quality,
//                    url = stream.url,
//                    cookie = stream.driveStream
//                )
//            )
//
//        }
//        adapterDiff.submitList(streamsList.toList())
//    }
//
//    private fun handleSearchResponse(responseEpisode: Resource<DriveResponse>) {
//        when (responseEpisode) {
//            is Resource.Success -> handleMovieSearchSuccess(responseEpisode.data)
//            is Resource.Error -> handleMovieSearchError(responseEpisode.message)
//            is Resource.Loading -> {
//                movieDialog.changeLayouts(loading = true, request = false, message = false)
//            }
//        }
//    }
//
//    private fun handleMovieSearchError(message: String?) {
//        if (message != null) {
//            witchDialogResponse(message)
//        } else {
//            movieDialog.dismiss()
//            Toast.makeText(
//                context, R.string.something_went_wrong, Toast.LENGTH_SHORT
//            ).show()
//        }
//    }
//
//    private fun handleMovieSearchSuccess(responseEpisode: DriveResponse?) {
//        responseEpisode?.let { driveResponse ->
//            if (driveResponse.files.isNotEmpty()) {
//                val file = driveResponse.files[0]
//                movieDialog.dismiss()
//                // playMovie(file)
//                context?.let { showStreamsDialog(it, file) }
//            } else {
//                movieDialog.changeLayouts(
//                    loading = false,
//                    request = true,
//                    message = false
//                )
//            }
//        }
//    }

    private fun playMovie(player: Player) {
        val intent = Intent(activity, PlayerActivity::class.java)
        intent.putExtra("fileId", player.fileId)
        intent.putExtra("title", showName!!)
        intent.putExtra("accessToken", player.accessToken)
        intent.putExtra("tmdbId", tmdbId!!)
        intent.putExtra("name", showName!!)
        intent.putExtra("posterPath", args.media.poster_path)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity?.startActivity(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        hasTransitionEnded = true
    }

    override fun onResume() {
        super.onResume()
        hasTransitionEnded = true
        Log.d(thisTAG, "hasTransitionEnded=$hasTransitionEnded")
    }

    private fun showLastSeason(hide: Boolean) {
        binding.apply {
            itemLastSeason.root.isGone = !hide
            tvSeasonHeader.isGone = !hide
        }
    }

    private fun navigateToSeason(
        tmdbId: Int,
        seasonName: String,
        seasonNumber: Int,
        showName: String?,
        posterPath: String?,
        showPoster: String?
    ) {
        seasonViewModel.setShowSeason(
            tmdbId = tmdbId,
            seasonName = seasonName,
            seasonNumber = seasonNumber,
            showName = showName ?: "Unknown",
            posterPath = posterPath,
            showPoster = showPoster
        )
        findNavController().navigateSafe(R.id.action_fragmentMedia_to_episodeListFragment)
    }

    private fun setSeasonsList(seasons: List<Season>) {
        if (tmdbId != null && showName != null) {
            listViewModel.setListArgs(
                tmdbId!!,
                showName!!,
                showPoster,
                casts = null,
                seasons = seasons
            )
            findNavController().navigate(R.id.action_fragmentMedia_to_fragmentList)
        }
    }

    private fun setCastsList(casts: List<Cast>) {
        if (tmdbId != null && showName != null) {
            listViewModel.setListArgs(
                tmdbId!!,
                showName!!,
                showPoster,
                casts = casts,
                seasons = null
            )
            findNavController().navigateSafe(R.id.action_fragmentMedia_to_fragmentList)
        }
    }

    private fun navigateToCollection(media: Media) {
        val action = FragmentMediaDirections.actionFragmentMediaToFragmentCollection(media)
        findNavController().navigateSafe(action)
    }

}