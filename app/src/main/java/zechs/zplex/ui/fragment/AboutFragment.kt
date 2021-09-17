package zechs.zplex.ui.fragment

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Toast.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.adapter.ActorsAdapter
import zechs.zplex.adapter.CreditsAdapter
import zechs.zplex.adapter.MediaAdapter
import zechs.zplex.databinding.FragmentAboutBinding
import zechs.zplex.models.drive.File
import zechs.zplex.models.tmdb.credits.Cast
import zechs.zplex.ui.activity.PlayerActivity
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.viewmodel.file.FileViewModel
import zechs.zplex.ui.viewmodel.tmdb.TmdbViewModel
import zechs.zplex.ui.viewmodel.tvdb.TvdbViewModel
import zechs.zplex.utils.Constants.Companion.TMDB_API_KEY
import zechs.zplex.utils.Constants.Companion.TMDB_IMAGE_PATH
import zechs.zplex.utils.Constants.Companion.TVDB_IMAGE_PATH
import zechs.zplex.utils.Constants.Companion.ZPLEX
import zechs.zplex.utils.Constants.Companion.ZPLEX_IMAGE_REDIRECT
import zechs.zplex.utils.Resource
import java.lang.Integer.parseInt
import java.net.*


@DelicateCoroutinesApi
class AboutFragment : Fragment() {

    private val args: AboutFragmentArgs by navArgs()
    private lateinit var binding: FragmentAboutBinding

    private lateinit var tvdbViewModel: TvdbViewModel
    private lateinit var tmdbViewModel: TmdbViewModel
    private lateinit var fileViewModel: FileViewModel
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var actorsAdapter: ActorsAdapter
    private lateinit var creditsAdapter: CreditsAdapter

    private lateinit var groupedList: Map<String, List<File>>

    private val itTAG = "AboutFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAboutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tmdbViewModel = (activity as ZPlexActivity).tmdbViewModel
        tvdbViewModel = (activity as ZPlexActivity).tvdbViewModel
        fileViewModel = (activity as ZPlexActivity).viewModel
        val prefix = args.seriesId.toString() + " - " + args.name

        if (args.type == "TV") {
            actorsAdapter = ActorsAdapter()
        } else {
            creditsAdapter = CreditsAdapter()
        }

        mediaAdapter = MediaAdapter(args.seriesId)

        setupRecyclerView("Episodes")

        val file = args.file
        val seriesId = args.seriesId
        val moviesId = args.seriesId
        val type = args.type
        val name = args.name

        binding.tvTitle.text = name
        binding.seriesInfo.visibility = View.INVISIBLE
        binding.mediaData.visibility = View.INVISIBLE

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
                .listener(object : RequestListener<Bitmap?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Bitmap?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        makeText(
                            it,
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
                                    val accent = p.getVibrantColor(
                                        p.getDominantColor(
                                            ContextCompat.getColor(
                                                it, R.color.colorAccent
                                            )
                                        )
                                    )

                                    val colorPrimary =
                                        ContextCompat.getColor(
                                            it,
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
                                                it,
                                                R.color.textColor_54
                                            ), accent
                                        )
                                        tabs.setSelectedTabIndicatorColor(
                                            accent
                                        )
                                        pbEpisodes.indeterminateTintList =
                                            ColorStateList.valueOf(accent)
                                        network.setTextColor(accent)
                                        rating.setTextColor(accent)
                                        released.setTextColor(accent)
                                        runtime.setTextColor(accent)
                                        darkPlay.backgroundTintList = ColorStateList.valueOf(accent)
                                    }
                                }
                            }
                        }
                        return false
                    }
                })
                .into(binding.ivPoster)
        }

        val tabLayout = binding.tabs
        if (type == "TV") {

            binding.btnRetryEpisodes.setOnClickListener {
                tvdbViewModel.getSeries(seriesId)
            }

            tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    setupRecyclerView(tab.text.toString())
                }

                override fun onTabUnselected(
                    tab: TabLayout.Tab
                ) {
                }

                override fun onTabReselected(
                    tab: TabLayout.Tab
                ) {
                }
            })
            tvdbViewModel.getSeries(seriesId)

            tvdbViewModel.series.observe(viewLifecycleOwner, { response ->
                Log.d("viewModel, AboutFragment", "observing")
                when (response) {
                    is Resource.Success -> {
                        response.data?.let { seriesResponse ->
                            seriesResponse.data?.let {


                                tabLayout.addTab(
                                    tabLayout.newTab().setText(getString(R.string.episodes))
                                )
                                tabLayout.addTab(
                                    tabLayout.newTab().setText(getString(R.string.cast))
                                )
                                TransitionManager.beginDelayedTransition(binding.root)
                                binding.tvTitle.text = it.seriesName

                                binding.seriesInfo.visibility = View.VISIBLE
                                binding.cgGenre.visibility = View.VISIBLE
                                tabLayout.visibility = View.VISIBLE

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
                                        }
                                    }
                                }
                                context?.let { cont ->

                                    Glide.with(cont)
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
                    }

                    is Resource.Error -> {
                        binding.seriesInfo.visibility = View.INVISIBLE
                        response.message?.let { message ->
                            makeText(
                                context,
                                "An error occurred: $message",
                                LENGTH_SHORT
                            ).show()
                            Log.e(itTAG, "An error occurred: $message")
                        }
                    }
                    is Resource.Loading -> {
                        binding.apply {
                            cgGenre.removeAllViews()
                            tabLayout.removeAllTabs()
                            cgGenre.visibility = View.INVISIBLE
                            tabLayout.visibility = View.INVISIBLE
                            seriesInfo.visibility = View.INVISIBLE
                            mediaData.visibility = View.INVISIBLE
                            ivFanart.visibility = View.INVISIBLE
                        }
                    }
                }
            })

            val driveQuery =
                "name contains 'mkv' and '${file.id}' in parents and trashed = false"
            fileViewModel.getMediaFiles(driveQuery)

            fileViewModel.mediaList.observe(viewLifecycleOwner, { responseMedia ->
                when (responseMedia) {
                    is Resource.Success -> {
                        responseMedia.data?.let { filesResponse ->

                            val filesList = filesResponse.files.toList()

                            groupedList = filesList.groupBy {
                                val season = it.name.take(3)
                                val first = season.take(1).replace("S", "Season ")
                                val count = parseInt(season.drop(1))
                                first + count
                            }

                            val seasons = groupedList.keys.toList()
                            mediaAdapter.differ.submitList(groupedList[seasons[0]]?.toList())

                            binding.apply {
                                rvEpisodes.visibility = View.VISIBLE
                                btnRetryEpisodes.visibility = View.GONE
                                pbEpisodes.visibility = View.GONE
                                tabs.visibility = View.VISIBLE

                                seasonsMenu.apply {
                                    context?.let {
                                        visibility = View.VISIBLE
                                        text = seasons[0]
                                        val listPopupWindow =
                                            ListPopupWindow(
                                                it,
                                                null,
                                                R.attr.listPopupWindowStyle
                                            )
                                        val adapter = ArrayAdapter(
                                            it,
                                            R.layout.item_dropdown,
                                            seasons
                                        )

                                        listPopupWindow.anchorView = seasonsMenu
                                        listPopupWindow.setAdapter(adapter)

                                        setOnClickListener { listPopupWindow.show() }

                                        listPopupWindow.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                                            mediaAdapter.differ.submitList(groupedList[seasons[position]]?.toList())
                                            seasonsMenu.text = seasons[position]
                                            listPopupWindow.dismiss()
                                        }

                                    }
                                }
                            }
                        }
                    }

                    is Resource.Error -> {
                        binding.apply {
                            rvEpisodes.visibility = View.INVISIBLE
                            btnRetryEpisodes.visibility = View.VISIBLE
                            pbEpisodes.visibility = View.INVISIBLE
                            tabs.visibility = View.INVISIBLE
                            seasonsMenu.visibility = View.INVISIBLE
                        }
                        responseMedia.message?.let { message ->
                            makeText(
                                context,
                                "An error occurred: $message",
                                LENGTH_SHORT
                            ).show()
                            Log.e(itTAG, "An error occurred: $message")
                        }
                    }

                    is Resource.Loading -> {
                        mediaAdapter.differ.submitList(listOf<File>().toList())
                        binding.apply {
                            rvEpisodes.visibility = View.INVISIBLE
                            btnRetryEpisodes.visibility = View.INVISIBLE
                            pbEpisodes.visibility = View.VISIBLE
                            tabs.visibility = View.INVISIBLE
                            seasonsMenu.visibility = View.INVISIBLE
                        }
                    }
                }
            })

            tvdbViewModel.getActor(args.seriesId)

            tvdbViewModel.actors.observe(viewLifecycleOwner, { responseMedia ->
                when (responseMedia) {
                    is Resource.Success -> {
                        responseMedia.data?.let {
                            actorsAdapter.differ.submitList(it.data?.toList())
                        }
                    }

                    is Resource.Error -> {
                        responseMedia.message?.let { message ->
                            makeText(
                                context,
                                "An error occurred: $message",
                                LENGTH_SHORT
                            ).show()
                            Log.e(itTAG, "An error occurred: $message")
                        }
                    }

                    is Resource.Loading -> {

                    }
                }
            })
        }

        if (type == "Movie.mkv") {
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
                }, 350)
            }

            tmdbViewModel.getMovies(moviesId)

            tmdbViewModel.movies.observe(viewLifecycleOwner, { response ->
                Log.d("viewModel, AboutFragment", "observing")
                when (response) {
                    is Resource.Success -> {
                        response.data?.let {
                            tabLayout.addTab(
                                tabLayout.newTab().setText(getString(R.string.cast))
                            )
                            TransitionManager.beginDelayedTransition(binding.root)

                            binding.seriesInfo.visibility = View.VISIBLE
                            binding.cgGenre.visibility = View.VISIBLE
                            tabLayout.visibility = View.VISIBLE

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
                                            text =
                                                if (text.length > 178) trimPlot else plot
                                        }
                                    } else {
                                        text = plot
                                    }
                                }
                            }
                            context?.let { cont ->
                                Glide.with(cont)
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
                    }

                    is Resource.Error -> {
                        binding.seriesInfo.visibility = View.INVISIBLE
                        response.message?.let { message ->
                            makeText(
                                context,
                                "An error occurred: $message",
                                LENGTH_SHORT
                            ).show()
                            Log.e(itTAG, "An error occurred: $message")
                        }
                    }
                    is Resource.Loading -> {
                        binding.apply {
                            cgGenre.removeAllViews()
                            tabLayout.removeAllTabs()
                            cgGenre.visibility = View.INVISIBLE
                            tabLayout.visibility = View.INVISIBLE
                            seriesInfo.visibility = View.INVISIBLE
                            mediaData.visibility = View.INVISIBLE
                            ivFanart.visibility = View.INVISIBLE
                        }
                    }
                }
            })

            tmdbViewModel.getCredits(moviesId)

            tmdbViewModel.credits.observe(viewLifecycleOwner, { responseMedia ->
                when (responseMedia) {
                    is Resource.Success -> {
                        binding.apply {
                            pbEpisodes.visibility = View.INVISIBLE
                            rvEpisodes.visibility = View.VISIBLE
                        }
                        responseMedia.data?.let {
                            creditsAdapter.differ.submitList(it.cast?.toList())
                        }
                    }

                    is Resource.Error -> {
                        binding.apply {
                            pbEpisodes.visibility = View.INVISIBLE
                            rvEpisodes.visibility = View.INVISIBLE
                        }
                        responseMedia.message?.let { message ->
                            makeText(
                                context,
                                "An error occurred: $message",
                                LENGTH_SHORT
                            ).show()
                            Log.e(itTAG, "An error occurred: $message")
                        }
                    }

                    is Resource.Loading -> {
                        creditsAdapter.differ.submitList(listOf<Cast>().toList())
                        binding.apply {
                            pbEpisodes.visibility = View.VISIBLE
                            rvEpisodes.visibility = View.INVISIBLE
                        }
                    }
                }
            })
        }
    }

    private fun setupRecyclerView(tabText: String) {

        if (args.type == "TV") {
            mediaAdapter.setOnItemClickListener {
                playMedia(it)
            }

            when (tabText) {
                "Episodes" -> {
                    binding.rvEpisodes.apply {
                        adapter = mediaAdapter
                        layoutManager =
                            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                    }
                    binding.seasonsMenu.visibility = View.VISIBLE
                }

                "Cast" -> {
                    binding.rvEpisodes.apply {
                        adapter = actorsAdapter
                        layoutManager = GridLayoutManager(activity, 2)
                    }
                    binding.seasonsMenu.visibility = View.INVISIBLE
                }
                else -> {
                    makeText(context, " No adapter attached; skipping layout", LENGTH_LONG).show()
                }
            }
        } else {
            binding.rvEpisodes.apply {
                adapter = creditsAdapter
                layoutManager = GridLayoutManager(activity, 2)
            }
            binding.seasonsMenu.visibility = View.INVISIBLE
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
                                "${ZPLEX}${args.seriesId} - ${args.name} - TV/${it.name}"
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
}
