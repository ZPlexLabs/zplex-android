package zechs.zplex.ui.fragment.music

import android.app.PendingIntent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Fade
import androidx.transition.TransitionSet
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.material.transition.MaterialSharedAxis
import zechs.zplex.R
import zechs.zplex.adapter.DriveAdapter
import zechs.zplex.databinding.FragmentMusicBinding
import zechs.zplex.models.drive.DriveResponse
import zechs.zplex.models.drive.File
import zechs.zplex.utils.Constants.ZPLEX_MUSIC_ID
import zechs.zplex.utils.Resource
import zechs.zplex.utils.SessionManager


class FragmentMusic : Fragment(R.layout.fragment_music) {

    private val thisTAG = "FragmentMusic"
    private var _binding: FragmentMusicBinding? = null
    private val binding get() = _binding!!

    private lateinit var musicViewModel: MusicViewModel
    private val args by navArgs<FragmentMusicArgs>()

    private val driveAdapter by lazy {
        DriveAdapter { listOnClick(it) }
    }

    private lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = TransitionSet().apply {
            addTransition(
                MaterialSharedAxis(
                    MaterialSharedAxis.Y, true
                ).apply {
                    interpolator = LinearInterpolator()
                    duration = 500
                })

            addTransition(Fade().apply {
                interpolator = LinearInterpolator()
            })
        }

        exitTransition = MaterialSharedAxis(
            MaterialSharedAxis.Y, true
        ).apply {
            interpolator = LinearInterpolator()
            duration = 500
        }

        returnTransition = TransitionSet().apply {
            addTransition(
                MaterialSharedAxis(
                    MaterialSharedAxis.Y, false
                ).apply {
                    interpolator = LinearInterpolator()
                    duration = 220
                })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMusicBinding.bind(view)

        //musicViewModel = (activity as ZPlexActivity).musicViewModel

        setupRecyclerView()
        setDriveFileListObserver()
        setupExoplayer()

        if (args.folderId != "null") {
            musicViewModel.openDriveFolder(args.folderId)
            binding.toolbar.apply {

                setNavigationOnClickListener {
                    exoPlayer.clearMediaItems()
                    exoPlayer.pause()
                    exoPlayer.release()
                    findNavController().navigateUp()
                }

                if (args.folderId == ZPLEX_MUSIC_ID) {
                    navigationIcon = null
                    title = null
                } else {
                    title = args.folderName
                    navigationIcon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_round_keyboard_backspace_24
                    )
                }
            }
        }
    }

    private fun setupExoplayer() {
        val httpDataSourceFactory = DefaultHttpDataSource
            .Factory()
            .setAllowCrossProtocolRedirects(true)

        context?.let {
            val dataSourceFactory = DataSource.Factory {
                val dataSource = httpDataSourceFactory.createDataSource()
                dataSource.setRequestProperty(
                    "Authorization",
                    "Bearer ${SessionManager(it).fetchAuthToken()}"
                )
                dataSource
            }

            val rendererFactory = DefaultRenderersFactory(it)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)

            exoPlayer = ExoPlayer.Builder(it, rendererFactory)
                .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
                .build()

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build()

            exoPlayer.apply {
                setAudioAttributes(audioAttributes, true)
                addListener(
                    object : Player.Listener {
                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            super.onMediaItemTransition(mediaItem, reason)
                            if (mediaItem?.localConfiguration != null) {
                                val metadata = mediaItem.localConfiguration!!.tag
                                println("metadata=$metadata")
                            }
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            super.onPlayerError(error)
                            Toast.makeText(
                                it, error.message, Toast.LENGTH_LONG
                            ).show()
                        }
                    })

                trackSelectionParameters = this.trackSelectionParameters
                    .buildUpon()
                    .setPreferredAudioLanguage("en")
                    .build()

                val playerNotificationManager: PlayerNotificationManager
                val notificationId = 1234

                playerNotificationManager = PlayerNotificationManager.Builder(
                    it,
                    notificationId,
                    "ZPlex Music Service",
                    object : PlayerNotificationManager.MediaDescriptionAdapter {
                        override fun getCurrentContentTitle(player: Player): CharSequence {
                            return "title"
                        }

                        override fun createCurrentContentIntent(player: Player): PendingIntent? {
                            return null
                        }

                        override fun getCurrentContentText(player: Player): CharSequence {
                            return "ContentText"
                        }

                        override fun getCurrentSubText(player: Player): CharSequence {
                            return ""
                        }

                        override fun getCurrentLargeIcon(
                            player: Player,
                            callback: PlayerNotificationManager.BitmapCallback
                        ): Bitmap? {
                            return null
                        }
                    }).setChannelDescriptionResourceId(R.string.app_name)
                    .build()

                playerNotificationManager.setPriority(NotificationCompat.PRIORITY_DEFAULT)

                playerNotificationManager.setPlayer(this)
            }

        }
    }


    private fun setupRecyclerView() {
        binding.rvDriveList.apply {
            adapter = driveAdapter
            layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
            )
        }
    }

    private fun setDriveFileListObserver() {
//        musicViewModel.driveList.observe(viewLifecycleOwner) { event ->
//            event.getContentIfNotHandled()?.let { responseDrive ->
//                handleDriveFileListResponse(responseDrive)
//            }
//        }
    }

    private fun handleDriveFileListResponse(it: Resource<DriveResponse>) {
        when (it) {
            is Resource.Success -> {
                it.data?.let { driveResponse -> driveSuccess(driveResponse) }
            }
            is Resource.Error -> isLoading(false)
            is Resource.Loading -> {
                driveAdapter.differ.submitList(listOf())
                isLoading(true)
            }
        }
    }

    private fun isLoading(loading: Boolean) {
        binding.progressBar.isInvisible = !loading
        binding.rvDriveList.isInvisible = loading
    }

    private fun driveSuccess(driveResponse: DriveResponse) {
        driveAdapter.differ.submitList(driveResponse.files)

        isLoading(false)
    }

    private fun listOnClick(item: File) {
        if (item.size == null) {
            val action = FragmentMusicDirections.actionMusicFragmentSelf(
                item.id, item.name
            )
            findNavController().navigate(action)
        } else {
            exoPlayer.pause()
            exoPlayer.clearMediaItems()
            val musicFiles = driveAdapter.differ.currentList.filter {
                it.name.endsWith(".flac")
            }.map {
                MediaItem.fromUri(getStreamUrl(it.id))
            }
            val music = musicFiles.indexOf(MediaItem.fromUri(getStreamUrl(item.id)))

            exoPlayer.apply {
                setMediaItems(musicFiles)
                this.prepare()
                seekTo(music, C.TIME_UNSET)
                play()
            }
        }
    }

    private fun getStreamUrl(fileId: String): Uri {
        return Uri.parse(
            "https://www.googleapis.com/drive/v3/files/${
                fileId
            }?supportsAllDrives=True&alt=media"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}