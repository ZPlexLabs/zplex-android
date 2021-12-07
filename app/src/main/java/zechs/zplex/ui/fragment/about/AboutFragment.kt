package zechs.zplex.ui.fragment.about

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.FragmentAboutBinding
import zechs.zplex.models.drive.File
import zechs.zplex.ui.activity.PlayerActivity
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.ArgsViewModel
import zechs.zplex.ui.fragment.ViewPagerAdapter
import zechs.zplex.ui.fragment.about.viewpager.*
import zechs.zplex.utils.Constants.TMDB_API_KEY
import zechs.zplex.utils.Constants.ZPLEX
import zechs.zplex.utils.Constants.ZPLEX_IMAGE_REDIRECT
import java.net.*


class AboutFragment : Fragment(R.layout.fragment_about) {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    private lateinit var aboutViewModel: AboutViewModel
    private val argsModel: ArgsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true).apply {
            duration = 500L
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
    }


    @DelicateCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAboutBinding.bind(view)

        aboutViewModel = (activity as ZPlexActivity).aboutViewModel

        argsModel.args.observe(viewLifecycleOwner, { arg ->
            GlobalScope.launch {
                val isSaved = aboutViewModel.getShow(arg.file.id)
                if (isSaved) {
                    binding.mbSave.apply {
                        text = context.getString(R.string.saved)
                        icon = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_round_favorite_24
                        )
                        setOnClickListener {
                            aboutViewModel.deleteShow(arg.file)
                            Snackbar.make(
                                binding.root,
                                "${arg.name} removed successfully",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            text = context.getString(R.string.save)
                            icon = ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_round_favorite_border_24
                            )
                            isClickable = false
                        }
                    }
                } else {
                    binding.mbSave.apply {
                        text = context.getString(R.string.save)
                        icon = ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_round_favorite_border_24
                        )
                        setOnClickListener {
                            aboutViewModel.saveShow(arg.file)
                            Snackbar.make(
                                binding.root,
                                "${arg.name} saved successfully",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            text = context.getString(R.string.saved)
                            icon = ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_round_favorite_24
                            )
                            isClickable = false
                        }
                    }
                }
            }

            val isTV = arg.type == "TV"
            Log.d("AboutFragment", "isTV = $isTV")

            if (isTV) {
                aboutViewModel.getSeries(arg.mediaId)

                val driveQuery =
                    "name contains 'mkv' and '${arg.file.id}' in parents and trashed = false"
                aboutViewModel.getMediaFiles(driveQuery)

                aboutViewModel.getActor(arg.mediaId)
            } else {
                aboutViewModel.getMovies(arg.mediaId)

                aboutViewModel.getCredits(arg.mediaId)
                binding.apply {
                    btnPlay.setOnClickListener {
                        playMedia(arg.file, arg.name)
                    }
                }
            }

            binding.moveViews.visibility = if (isTV) View.GONE else View.VISIBLE

            val fragmentList: ArrayList<Fragment> = if (isTV) {
                arrayListOf(
                    InfoFragment(),
                    EpisodesFragment(),
                    ActorsFragment()
                )
            } else {
                arrayListOf(
                    InfoFragment(),
                    CreditsFragment()
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
                            0 -> getString(R.string.info)
                            1 -> getString(R.string.episodes)
                            2 -> getString(R.string.cast)
                            else -> throw IndexOutOfBoundsException()
                        }
                    } else {
                        when (position) {
                            0 -> getString(R.string.info)
                            1 -> getString(R.string.cast)
                            else -> throw IndexOutOfBoundsException()
                        }
                    }
            }.attach()

            val redirectImagePoster = if (arg.type == "TV") {
                Uri.parse(
                    "${ZPLEX_IMAGE_REDIRECT}/tvdb/${
                        arg.file.name.split(" - ").toTypedArray()[0]
                    }"
                )
            } else {
                Uri.parse(
                    "${ZPLEX_IMAGE_REDIRECT}/tmdb/${
                        arg.file.name.split(" - ").toTypedArray()[0]
                    }?api_key=${TMDB_API_KEY}&language=en-US"
                )
            }

            context?.let { context ->
                Glide.with(context)
                    .asBitmap().format(DecodeFormat.PREFER_ARGB_8888)
                    .load(redirectImagePoster)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .listener(this@AboutFragment.imageRequestListener)
                    .into(binding.ivPoster)

                binding.btnDownload.setOnClickListener {
                    Toast.makeText(
                        context,
                        "This is one of those unimplemented features",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private val imageRequestListener = object : RequestListener<Bitmap?> {
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
            resource?.let { _ ->
                Palette.from(resource)
                    .maximumColorCount(36)
                    .generate { p: Palette? ->
                        if (p != null) {
                            p.dominantSwatch?.rgb?.let {
                                val accent = Color.argb(
                                    90,
                                    Color.red(it),
                                    Color.green(it),
                                    Color.blue(it)
                                )
                                binding.rootView.setBackgroundColor(accent)
                                activity?.window?.apply {
                                    statusBarColor = accent
                                    navigationBarColor = Color.parseColor("#00000000")
                                }
                            }
                        }
                    }
            }
            return false
        }
    }

    private fun playMedia(it: File, name: String) {
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
                            intent.putExtra("title", name)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            activity?.startActivity(intent)
                            dialog.dismiss()
                        }
                        1 -> {
                            playVLC(it, name)
                            dialog.dismiss()
                        }
                    }
                }
                .show()
        }
    }

    private fun playVLC(it: File, name: String) {

        val playUrl = "${ZPLEX}${it.name}"

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
            vlcIntent.putExtra("title", name)
            vlcIntent.flags =
                FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            requireContext().startActivity(vlcIntent)
            Log.d("playUrl", episodeURI)
        } catch (notFoundException: ActivityNotFoundException) {
            notFoundException.printStackTrace()
            Toast.makeText(
                context,
                "VLC not found, Install VLC from Play Store",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            Toast.makeText(
                context,
                e.localizedMessage,
                Toast.LENGTH_LONG
            ).show()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            Toast.makeText(
                context,
                e.localizedMessage,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}