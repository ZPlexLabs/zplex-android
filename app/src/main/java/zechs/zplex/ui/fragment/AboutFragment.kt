package zechs.zplex.ui.fragment

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.transition.MaterialFade
import zechs.zplex.R
import zechs.zplex.adapter.ActorsAdapter
import zechs.zplex.adapter.MediaAdapter
import zechs.zplex.databinding.FragmentNewAboutBinding
import zechs.zplex.models.drive.File
import zechs.zplex.ui.FileViewModel
import zechs.zplex.ui.TvdbViewModel
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.utils.Constants.Companion.TVDB_IMAGE_PATH
import zechs.zplex.utils.Constants.Companion.TVDB_IMAGE_REDIRECT
import zechs.zplex.utils.Constants.Companion.ZPLEX
import zechs.zplex.utils.Resource
import java.lang.Integer.parseInt


class AboutFragment : Fragment() {

    private val args: AboutFragmentArgs by navArgs()
    private lateinit var binding: FragmentNewAboutBinding

    private lateinit var viewModel: TvdbViewModel
    private lateinit var fileViewModel: FileViewModel
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var actorsAdapter: ActorsAdapter

    private lateinit var groupedList: Map<String, List<File>>

    private val itTAG = "AboutFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentNewAboutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as ZPlexActivity).tvdbViewModel
        fileViewModel = (activity as ZPlexActivity).viewModel
        val prefix = args.seriesId.toString() + " - " + args.name

        actorsAdapter = ActorsAdapter()
        mediaAdapter = MediaAdapter(prefix)

        setupRecyclerView("Episodes")

        val file = args.file
        val seriesId = args.seriesId
        val type = args.type
        val name = args.name

        binding.tvTitle.text = name
        binding.mainView.visibility = View.VISIBLE

        binding.btnMyList.setOnClickListener {
            fileViewModel.saveFile(file)
            Snackbar.make(binding.root, "$name saved successfully", Snackbar.LENGTH_SHORT).show()
        }
        val redirectImagePoster =
            Uri.parse("${TVDB_IMAGE_REDIRECT}${seriesId}")

        context?.let {
            Glide.with(it)
                .asBitmap()
                .load(redirectImagePoster)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .override(Target.SIZE_ORIGINAL)
                .listener(object : RequestListener<Bitmap?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Bitmap?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Toast.makeText(
                            it,
                            R.string.failed_to_load_poster_image,
                            Toast.LENGTH_SHORT
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

                                    binding.frameLayout.background = gradientDrawable
                                    binding.tabs.setTabTextColors(
                                        ContextCompat.getColor(
                                            it,
                                            R.color.textColor_54
                                        ), accent
                                    )
                                    binding.tabs.setSelectedTabIndicatorColor(
                                        accent
                                    )

                                    binding.btnMyList.apply {
                                        setTextColor(accent)
                                        iconTint = ColorStateList.valueOf(accent)
                                    }

                                    binding.pbEpisodes.indeterminateTintList =
                                        ColorStateList.valueOf(accent)
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
                viewModel.getSeries(seriesId)
            }

            tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    TransitionManager.beginDelayedTransition(
                        binding.root,
                        MaterialFade()
                    )
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

            viewModel.getSeries(seriesId)

            viewModel.series.observe(viewLifecycleOwner, { response ->
                Log.d("viewModel, AboutFragment", "observing")
                when (response) {
                    is Resource.Success -> {
                        response.data?.let { seriesResponse ->
                            seriesResponse.data?.let {

                                val driveQuery =
                                    "name contains 'mkv' and '${file.id}' in parents and trashed = false"
                                fileViewModel.getMediaFiles(driveQuery)

                                TransitionManager.beginDelayedTransition(binding.root)
                                binding.seriesInfo.visibility = View.VISIBLE
                                binding.cgGenre.visibility = View.VISIBLE
                                tabLayout.visibility = View.VISIBLE
                                tabLayout.addTab(
                                    tabLayout.newTab().setText(getString(R.string.episodes))
                                )
                                tabLayout.addTab(
                                    tabLayout.newTab().setText(getString(R.string.cast))
                                )
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

                                val rating = it.rating
                                val year = it.firstAired?.take(4)
                                val yearMpaa = year + getString(R.string.divider) + rating
                                val miscText =
                                    "${it.network} • ${it.rating} • $year • ${it.runtime} min"
                                binding.tvTitle.text = it.seriesName
                                binding.tvYearMpaa.text = miscText

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
                            }
                        }
                    }

                    is Resource.Error -> {
                        binding.seriesInfo.visibility = View.INVISIBLE
                        response.message?.let { message ->
                            Toast.makeText(
                                context,
                                "An error occurred: $message",
                                Toast.LENGTH_SHORT
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
                            ivFanart.visibility = View.INVISIBLE
                            rvEpisodes.visibility = View.INVISIBLE
                            btnRetryEpisodes.visibility = View.INVISIBLE
                            pbEpisodes.visibility = View.VISIBLE
                            tabs.visibility = View.INVISIBLE
                            seasonsMenu.visibility = View.INVISIBLE
                        }
                    }
                }
            })


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

//                            TransitionManager.beginDelayedTransition(
//                                binding.root,
//                                MaterialFade()
//                            )

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
                            Toast.makeText(
                                context,
                                "An error occurred: $message",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(itTAG, "An error occurred: $message")
                        }
                    }

                    is Resource.Loading -> {
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

            viewModel.getActor(args.seriesId)

            viewModel.actors.observe(viewLifecycleOwner, { responseMedia ->
                when (responseMedia) {
                    is Resource.Success -> {
                        responseMedia.data?.let { actorsResponse ->
                            actorsAdapter.differ.submitList(actorsResponse.data.toList())
                        }
                    }

                    is Resource.Error -> {
                        responseMedia.message?.let { message ->
                            Toast.makeText(
                                context,
                                "An error occurred: $message",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(itTAG, "An error occurred: $message")
                        }
                    }

                    is Resource.Loading -> {

                    }
                }
            })
        }
    }

    private fun setupRecyclerView(tabText: String) {

        mediaAdapter.setOnItemClickListener {
            val playUrl = "${ZPLEX}${args.seriesId} - ${args.name} - TV/${it.name}"
            try {
                val vlcIntent = Intent(Intent.ACTION_VIEW)
                vlcIntent.setPackage("org.videolan.vlc")
                vlcIntent.component = ComponentName(
                    "org.videolan.vlc",
                    "org.videolan.vlc.gui.video.VideoPlayerActivity"
                )
                vlcIntent.setDataAndTypeAndNormalize(Uri.parse(playUrl), "video/*")
                vlcIntent.putExtra("title", it.name.dropLast(4))
                vlcIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                requireContext().startActivity(vlcIntent)
            } catch (notFoundException: ActivityNotFoundException) {
                notFoundException.printStackTrace()
                Toast.makeText(
                    context,
                    "VLC not found, Install VLC from Play Store",
                    Toast.LENGTH_LONG
                ).show()
            }
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
                Toast.makeText(context, " No adapter attached; skipping layout", LENGTH_LONG).show()
            }
        }

    }

}
