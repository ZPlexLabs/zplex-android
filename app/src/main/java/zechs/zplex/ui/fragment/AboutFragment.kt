package zechs.zplex.ui.fragment

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast.*
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialFade
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.FragmentAboutBinding
import zechs.zplex.models.drive.File
import zechs.zplex.models.tmdb.movies.MoviesResponse
import zechs.zplex.models.tvdb.series.SeriesResponse
import zechs.zplex.ui.ViewPagerAdapter
import zechs.zplex.ui.activity.PlayerActivity
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.viewpager.CastFragment
import zechs.zplex.ui.fragment.viewpager.EpisodesFragment
import zechs.zplex.ui.viewmodel.file.FileViewModel
import zechs.zplex.ui.viewmodel.tmdb.TmdbViewModel
import zechs.zplex.ui.viewmodel.tvdb.TvdbViewModel
import zechs.zplex.utils.Constants.Companion.TMDB_API_KEY
import zechs.zplex.utils.Constants.Companion.TMDB_IMAGE_PATH
import zechs.zplex.utils.Constants.Companion.TVDB_IMAGE_PATH
import zechs.zplex.utils.Constants.Companion.ZPLEX
import zechs.zplex.utils.Constants.Companion.ZPLEX_IMAGE_REDIRECT
import zechs.zplex.utils.Resource
import java.net.*


@DelicateCoroutinesApi
class AboutFragment : Fragment(R.layout.fragment_about) {

    private val args: AboutFragmentArgs by navArgs()
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    private lateinit var tvdbViewModel: TvdbViewModel
    private lateinit var tmdbViewModel: TmdbViewModel
    private lateinit var fileViewModel: FileViewModel

    private val thisTAG = "AboutFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAboutBinding.bind(view)

        tmdbViewModel = (activity as ZPlexActivity).tmdbViewModel
        tvdbViewModel = (activity as ZPlexActivity).tvdbViewModel
        fileViewModel = (activity as ZPlexActivity).viewModel
        val prefix = args.seriesId.toString() + " - " + args.name

        val file = args.file
        val seriesId = args.seriesId
        val moviesId = args.seriesId
        val type = args.type
        val name = args.name

        binding.tvTitle.text = name
        binding.seriesInfo.visibility = View.INVISIBLE

        GlobalScope.launch {
            val isSaved = fileViewModel.getFile(file.id)

            (activity as ZPlexActivity).runOnUiThread {
                if (isSaved) {
                    binding.btnMyList.apply {
                        text = context.getString(R.string.saved)
                        icon = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_baseline_playlist_add_check_24
                        )
                        isClickable = false
                    }
                } else {
                    binding.btnMyList.apply {
                        text = context.getString(R.string.save)
                        icon = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_baseline_playlist_add_24
                        )
                        setOnClickListener {
                            fileViewModel.saveFile(file)
                            Snackbar.make(
                                binding.root,
                                "$name saved successfully",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            text = context.getString(R.string.saved)
                            icon = ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_baseline_playlist_add_check_24
                            )
                            isClickable = false
                        }
                    }
                }
            }
        }

        val redirectImagePoster = if (type == "TV") {
            Uri.parse(
                "${ZPLEX_IMAGE_REDIRECT}/tvdb/${
                    file.name.split(" - ").toTypedArray()[0]
                }"
            )
        } else {
            Uri.parse(
                "${ZPLEX_IMAGE_REDIRECT}/tmdb/${
                    file.name.split(" - ").toTypedArray()[0]
                }?api_key=${TMDB_API_KEY}&language=en-US"
            )
        }

        context?.let {
            Glide.with(it)
                .asBitmap()
                .load(redirectImagePoster)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .listener(this@AboutFragment.imageRequestListener)
                .into(binding.ivPoster)
        }

        setupViewPager(type == "TV", file)

        if (type == "TV") {

            tvdbViewModel.getSeries(seriesId)

            tvdbViewModel.series.observe(viewLifecycleOwner, { response ->
                when (response) {
                    is Resource.Success -> {
                        response.data?.let { seriesResponse ->
                            tvdbSuccess(seriesResponse)
                        }
                    }

                    is Resource.Error -> {
                        tvdbError(response)
                    }
                    is Resource.Loading ->
                        tvdbLoading()
                }
            })

        } else {
            binding.apply {
                darkTint.visibility = View.VISIBLE
                darkPlay.visibility = View.VISIBLE
            }

            Handler(Looper.getMainLooper()).postDelayed({
                TransitionManager.beginDelayedTransition(binding.root)
                binding.apply {
                    darkTint.visibility = View.INVISIBLE
                    darkPlay.visibility = View.INVISIBLE
                }
            }, 700)

            binding.posterCard.setOnClickListener {
                binding.apply {
                    darkTint.visibility = View.VISIBLE
                    darkPlay.visibility = View.VISIBLE
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    playMedia(file)
                }, 250)
            }

            tmdbViewModel.getMovies(moviesId)

            tmdbViewModel.movies.observe(viewLifecycleOwner, { response ->
                when (response) {
                    is Resource.Success -> {
                        response.data?.let {
                            tmdbSuccess(it)
                        }
                    }

                    is Resource.Error -> {
                        binding.seriesInfo.visibility = View.INVISIBLE
                        response.message?.let { message ->
                            makeText(
                                context,
                                "An error occurred: $message",
                                LENGTH_SHORT
                            ).show()
                            Log.e(thisTAG, "An error occurred: $message")
                        }
                    }
                    is Resource.Loading -> {
                        tmdbLoading()
                    }
                }
            })

        }
    }

    private fun playMedia(it: File) {
        val items = arrayOf("ExoPlayer", "VLC")

        context?.let { it1 ->
            MaterialAlertDialogBuilder(it1)
                .setBackground(ContextCompat.getDrawable(it1, R.drawable.popup_menu_bg))
                .setTitle("Play using")
                .setItems(items) { dialog, which ->
                    when (which) {
                        0 -> {
                            val intent = Intent(activity, PlayerActivity::class.java)
                            intent.putExtra("fileId", it.id)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            activity?.startActivity(intent)
                            dialog.dismiss()
                        }
                        1 -> {
                            val playUrl =
                                if (args.type == "TV") "${ZPLEX}${args.seriesId} - ${args.name} - TV/${it.name}" else "${ZPLEX}${args.name}"
                            try {
                                val episodeURL = URL(playUrl)
                                val episodeURI = URI(
                                    episodeURL.protocol,
                                    episodeURL.userInfo,
                                    IDN.toASCII(episodeURL.host),
                                    episodeURL.port,
                                    episodeURL.path,
                                    episodeURL.query,
                                    episodeURL.ref
                                ).toASCIIString().replace("?", "%3F")

                                val vlcIntent = Intent(Intent.ACTION_VIEW)
                                vlcIntent.setPackage("org.videolan.vlc")
                                vlcIntent.component = ComponentName(
                                    "org.videolan.vlc",
                                    "org.videolan.vlc.gui.video.VideoPlayerActivity"
                                )
                                vlcIntent.setDataAndTypeAndNormalize(
                                    Uri.parse(episodeURI),
                                    "video/*"
                                )
                                vlcIntent.putExtra("title", it.name.dropLast(4))
                                vlcIntent.flags =
                                    FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                                requireContext().startActivity(vlcIntent)
                            } catch (notFoundException: ActivityNotFoundException) {
                                notFoundException.printStackTrace()
                                makeText(
                                    context,
                                    "VLC not found, Install VLC from Play Store",
                                    LENGTH_LONG
                                ).show()
                            } catch (e: MalformedURLException) {
                                e.printStackTrace()
                                makeText(
                                    context,
                                    e.localizedMessage,
                                    LENGTH_LONG
                                ).show()
                            } catch (e: URISyntaxException) {
                                e.printStackTrace()
                                makeText(
                                    context,
                                    e.localizedMessage,
                                    LENGTH_LONG
                                ).show()
                            }
                            dialog.dismiss()
                        }
                    }
                }
                .setOnDismissListener {
                    binding.apply {
                        darkTint.visibility = View.INVISIBLE
                        darkPlay.visibility = View.INVISIBLE
                    }
                }
                .show()
        }
    }

    private val imageRequestListener = object : RequestListener<Bitmap?> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any,
            target: Target<Bitmap?>,
            isFirstResource: Boolean
        ): Boolean {
            makeText(
                requireContext(),
                R.string.failed_to_load_poster_image,
                LENGTH_SHORT
            ).show()
            return false
        }

        override fun onResourceReady(
            resource: Bitmap?,
            model: Any,
            target: Target<Bitmap?>,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            resource?.let { _ ->
                Palette.from(resource).generate { p: Palette? ->
                    p?.let { _ ->
                        var accent = p.getVibrantColor(
                            p.getDarkVibrantColor(
                                p.getDominantColor(
                                    ContextCompat.getColor(
                                        requireContext(), R.color.colorAccent
                                    )
                                )
                            )
                        )
                        accent =
                            if (getContrastColor(accent)) ContextCompat.getColor(
                                requireContext(), R.color.textColor
                            ) else accent


                        val colorPrimary =
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.colorPrimaryDark
                            )

                        val gradientDrawable = GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM,
                            intArrayOf(accent, colorPrimary)
                        )

                        binding.btnMyList.apply {
                            setTextColor(accent)
                            iconTint = ColorStateList.valueOf(accent)
                        }

                        binding.apply {
                            frameLayout.background = gradientDrawable
                            tabs.setTabTextColors(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.textColor_54
                                ), accent
                            )
                            tabs.setSelectedTabIndicatorColor(
                                accent
                            )
//                            pbEpisodes.indeterminateTintList =
//                                ColorStateList.valueOf(accent)
                            network.setTextColor(accent)
                            rating.setTextColor(accent)
                            released.setTextColor(accent)
                            runtime.setTextColor(accent)
                            darkPlay.backgroundTintList =
                                ColorStateList.valueOf(accent)
                        }

                    }
                }
            }
            return false
        }
    }

    private fun tvdbSuccess(seriesResponse: SeriesResponse) {

        val transition = MaterialFade()
        transition.excludeTarget(android.R.id.statusBarBackground, true)
        transition.excludeTarget(android.R.id.navigationBarBackground, true)
        TransitionManager.beginDelayedTransition(binding.root, transition)

        seriesResponse.data?.let {

            binding.seriesInfo.visibility = View.VISIBLE

            val runtimeText = "${it.runtime} min"
            binding.apply {
                tvTitle.text = it.seriesName
                network.text = it.network
                rating.text = it.rating
                released.text = it.firstAired?.take(4)
                runtime.text = runtimeText
            }
            it.genre?.forEach { text ->
                val mChip = layoutInflater.inflate(
                    R.layout.item_chip,
                    binding.root,
                    false
                ) as Chip
                mChip.text = text
                binding.cgGenre.addView(mChip)
            }

            it.overview?.let { plot ->
                binding.tvPlot.apply {
                    if (plot.length > 175) {
                        val trimPlot = plot.substring(0, 175) + "..."
                        text = trimPlot
                        setOnClickListener {
                            TransitionManager.beginDelayedTransition(
                                binding.root
                            )
                            text =
                                if (text.length > 178) trimPlot else plot
                        }
                    } else {
                        text = plot
                        setOnClickListener(null)
                    }
                }
            }

            if (it.fanart != null) {
                Glide.with(binding.root)
                    .asBitmap()
                    .load("${TVDB_IMAGE_PATH}${it.fanart}")
                    .placeholder(R.color.cardColor)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Bitmap?> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any,
                            target: Target<Bitmap?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Bitmap?,
                            model: Any,
                            target: Target<Bitmap?>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.ivFanart.visibility = View.VISIBLE
                            return false
                        }

                    })
                    .into(binding.ivFanart)
            }
        }

    }

    private fun tvdbError(response: Resource.Error<SeriesResponse>) {
        binding.seriesInfo.visibility = View.INVISIBLE
        response.message?.let { message ->
            makeText(
                context,
                "An error occurred: $message",
                LENGTH_SHORT
            ).show()
            Log.e(thisTAG, "An error occurred: $message")
        }
    }

    private fun tvdbLoading() {
        binding.apply {
            cgGenre.removeAllViews()
            seriesInfo.visibility = View.GONE
            ivFanart.visibility = View.INVISIBLE
        }
    }


    private fun tmdbSuccess(it: MoviesResponse) {
        val transition = MaterialFade()
        transition.excludeTarget(android.R.id.statusBarBackground, true)
        transition.excludeTarget(android.R.id.navigationBarBackground, true)
        TransitionManager.beginDelayedTransition(binding.root, transition)

        binding.seriesInfo.visibility = View.VISIBLE

        val runtimeText = "${it.runtime} min"
        binding.apply {
            tvTitle.text = it.title
            it.production_companies?.let { companies ->
                if (companies.isNotEmpty()) {
                    network.text = companies[0].name
                }
            }
            rating.text = it.vote_average.toString()
            released.text = it.release_date?.take(4)
            runtime.text = runtimeText
        }
        it.genres?.forEach { genre ->
            val mChip = layoutInflater.inflate(
                R.layout.item_chip,
                binding.root,
                false
            ) as Chip
            mChip.text = genre.name
            binding.cgGenre.addView(mChip)
        }

        it.overview?.let { plot ->
            binding.tvPlot.apply {
                if (plot.length > 175) {
                    val trimPlot = plot.substring(0, 175) + "..."
                    text = trimPlot
                    setOnClickListener {
                        TransitionManager.beginDelayedTransition(
                            binding.root
                        )
                        text = if (text.length > 178) trimPlot else plot
                    }
                } else {
                    text = plot
                    setOnClickListener(null)
                }
            }
        }

        if (it.backdrop_path != null) {
            Glide.with(binding.ivFanart)
                .asBitmap()
                .load("${TMDB_IMAGE_PATH}${it.backdrop_path}")
                .placeholder(R.color.cardColor)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Bitmap?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Bitmap?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any,
                        target: Target<Bitmap?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.ivFanart.visibility = View.VISIBLE
                        return false
                    }

                })
                .into(binding.ivFanart)
        }
    }

    private fun tmdbLoading() {
        binding.apply {
            cgGenre.removeAllViews()
            cgGenre.visibility = View.INVISIBLE
            seriesInfo.visibility = View.INVISIBLE
            ivFanart.visibility = View.INVISIBLE
        }
    }

    private fun setupViewPager(isTV: Boolean, file: File) {

        val fragmentList: ArrayList<Fragment> = if (isTV) {
            arrayListOf(
                EpisodesFragment(file, args),
                CastFragment(isTV, args.seriesId),
            )
        } else {
            arrayListOf(
                CastFragment(isTV, args.seriesId)
            )
        }

        val adapter = ViewPagerAdapter(
            fragmentList,
            childFragmentManager,
            lifecycle
        )

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text =
                if (isTV) {
                    when (position) {
                        0 -> getString(R.string.episodes)
                        1 -> getString(R.string.cast)
                        else -> throw IndexOutOfBoundsException()
                    }
                } else {
                    when (position) {
                        0 -> getString(R.string.cast)
                        else -> throw IndexOutOfBoundsException()
                    }
                }
        }.attach()
    }

    fun getContrastColor(@ColorInt color: Int): Boolean {
        val ratio =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return ratio >= 0.80
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            cgGenre.removeAllViews()
            seriesInfo.visibility = View.GONE
            ivFanart.visibility = View.INVISIBLE
            if (tabs.tabCount > 0) tabs.removeAllTabs()
            Glide.get(ivPoster.context).clearMemory()
        }
        _binding = null
    }
}
