package zechs.zplex.ui.activity

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.android.synthetic.main.activity_zplex.*
import zechs.zplex.BuildConfig
import zechs.zplex.R
import zechs.zplex.db.WatchlistDatabase
import zechs.zplex.models.dataclass.MediaArgs
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.repository.FilesRepository
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.repository.WitchRepository
import zechs.zplex.ui.fragment.browse.BrowseViewModel
import zechs.zplex.ui.fragment.browse.BrowseViewModelProviderFactory
import zechs.zplex.ui.fragment.cast.CastViewModel
import zechs.zplex.ui.fragment.cast.CastViewModelProviderFactory
import zechs.zplex.ui.fragment.collection.CollectionViewModel
import zechs.zplex.ui.fragment.collection.CollectionViewModelProviderFactory
import zechs.zplex.ui.fragment.episodes.EpisodesViewModel
import zechs.zplex.ui.fragment.episodes.EpisodesViewModelProviderFactory
import zechs.zplex.ui.fragment.home.HomeViewModel
import zechs.zplex.ui.fragment.home.HomeViewModelProviderFactory
import zechs.zplex.ui.fragment.media.MediaViewModel
import zechs.zplex.ui.fragment.media.MediaViewModelProviderFactory
import zechs.zplex.ui.fragment.myshows.MyShowsViewModel
import zechs.zplex.ui.fragment.myshows.MyShowsViewModelProviderFactory
import zechs.zplex.ui.fragment.search.SearchViewModel
import zechs.zplex.ui.fragment.search.SearchViewModelProviderFactory
import zechs.zplex.ui.fragment.upcoming.UpcomingViewModel
import zechs.zplex.ui.fragment.upcoming.UpcomingViewModelProviderFactory
import zechs.zplex.ui.fragment.watch.WatchViewModel
import zechs.zplex.ui.fragment.watch.WatchViewModelProviderFactory
import zechs.zplex.utils.Constants.DRIVE_ZPLEX_RELEASES
import zechs.zplex.utils.Constants.THEMOVIEDB_ID_REGEX
import zechs.zplex.utils.Constants.VERSION_CODE_KEY
import zechs.zplex.utils.NotificationKeys
import java.util.*


class ZPlexActivity : AppCompatActivity() {

    private val thisTAG = "ZPlexActivity"

    private lateinit var navController: NavController

    lateinit var homeViewModel: HomeViewModel
    lateinit var browseViewModel: BrowseViewModel
    lateinit var searchViewModel: SearchViewModel
    lateinit var myShowsViewModel: MyShowsViewModel
    lateinit var mediaViewModel: MediaViewModel
    lateinit var episodesViewModel: EpisodesViewModel
    lateinit var watchViewModel: WatchViewModel
    lateinit var castViewModel: CastViewModel

    // lateinit var musicViewModel: MusicViewModel
    lateinit var collectionViewModel: CollectionViewModel
    lateinit var upcomingViewModel: UpcomingViewModel


    override fun onCreate(savedInstanceState: Bundle?) {

        val filesRepository = FilesRepository()
        val tmdbRepository = TmdbRepository(WatchlistDatabase(this))
        val witchRepository = WitchRepository()

        homeViewModel = ViewModelProvider(
            this,
            HomeViewModelProviderFactory(application, tmdbRepository)
        )[HomeViewModel::class.java]

        browseViewModel = ViewModelProvider(
            this,
            BrowseViewModelProviderFactory(application, tmdbRepository)
        )[BrowseViewModel::class.java]

        searchViewModel = ViewModelProvider(
            this,
            SearchViewModelProviderFactory(application, tmdbRepository)
        )[SearchViewModel::class.java]

        myShowsViewModel = ViewModelProvider(
            this,
            MyShowsViewModelProviderFactory(tmdbRepository)
        )[MyShowsViewModel::class.java]

        mediaViewModel = ViewModelProvider(
            this,
            MediaViewModelProviderFactory(
                application,
                filesRepository,
                tmdbRepository,
                witchRepository
            )
        )[MediaViewModel::class.java]

        episodesViewModel = ViewModelProvider(
            this,
            EpisodesViewModelProviderFactory(
                application,
                filesRepository,
                tmdbRepository,
                witchRepository
            )
        )[EpisodesViewModel::class.java]

        watchViewModel = ViewModelProvider(
            this,
            WatchViewModelProviderFactory(application, tmdbRepository)
        )[WatchViewModel::class.java]

        castViewModel = ViewModelProvider(
            this,
            CastViewModelProviderFactory(application, tmdbRepository)
        )[CastViewModel::class.java]

//        musicViewModel = ViewModelProvider(
//            this,
//            MusicViewModelProviderFactory(application, filesRepository)
//        )[MusicViewModel::class.java]

        collectionViewModel = ViewModelProvider(
            this,
            CollectionViewModelProviderFactory(application, tmdbRepository)
        )[CollectionViewModel::class.java]

        upcomingViewModel = ViewModelProvider(
            this,
            UpcomingViewModelProviderFactory(application, tmdbRepository)
        )[UpcomingViewModel::class.java]


        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_zplex)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.mainNavHostFragment
        ) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        val colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary)
        val colorPrimaryDark = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragmentMedia, R.id.castsFragment,
                R.id.episodesListFragment, R.id.fragmentList,
                R.id.fragmentCollection, R.id.searchFragment,
                R.id.shareBottomSheet -> {
                    window.navigationBarColor = colorPrimary
                    bottomNavigationView.isVisible = false
                    view1.isVisible = false
                }
                R.id.watchFragment -> {
                    bottomNavigationView.isVisible = false
                    view1.isVisible = false
                }
                R.id.bigImageFragment -> bottomNavigationView.visibility = View.GONE
                else -> {
                    window.navigationBarColor = colorPrimaryDark
                    bottomNavigationView.isVisible = true
                    view1.isVisible = true
                }
            }
        }

        @SuppressLint("HardwareIds")
        val deviceId: String = if (BuildConfig.DEBUG) {
            "ZPLEX_TEST_CHANNEL"
        } else {
            Settings.Secure.getString(
                this.contentResolver, Settings.Secure.ANDROID_ID
            )
        }

        val firebaseDefaultMap = HashMap<String, Any>()
        firebaseDefaultMap[VERSION_CODE_KEY] = BuildConfig.VERSION_CODE

        Firebase.apply {
            crashlytics.apply { setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG) }
            analytics.apply { setAnalyticsCollectionEnabled(!BuildConfig.DEBUG) }
            messaging.apply { subscribeToTopic(deviceId) }

            remoteConfig.apply {
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds = 3600
                }
                setDefaultsAsync(firebaseDefaultMap)
                fetch(0)
                fetchAndActivate().addOnCompleteListener(firebaseOnCompleteListener)
            }
        }

        doOnIntent(intent)
    }

    private val firebaseOnCompleteListener = OnCompleteListener<Boolean> {
        if (it.isSuccessful) {
            val latestAppVersion = Firebase.remoteConfig.getDouble(VERSION_CODE_KEY).toInt()
            if (latestAppVersion > BuildConfig.VERSION_CODE) {
                showUpdateNotification()
            }
            Log.d(
                "firebaseOnCompleteListener",
                "Update=${latestAppVersion > BuildConfig.VERSION_CODE}"
            )
        }
    }

    private fun showUpdateNotification() {
        val requestCode = Random().nextInt()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(DRIVE_ZPLEX_RELEASES)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_NOTIFICATION
        )

        val notificationBuilder = NotificationCompat.Builder(
            this,
            NotificationKeys.UPDATE_CHANNEL_ID
        ).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_zplex)
            .setContentTitle(getString(R.string.new_version_available))
            .setContentText(getString(R.string.update_msg))
            .setStyle(
                NotificationCompat
                    .BigTextStyle()
                    .bigText(getString(R.string.update_msg))
            ).setAutoCancel(true)
            .setSound(defaultSoundUri)
            .addAction(R.drawable.ic_update_24dp, getString(R.string.update), pendingIntent)

        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val channel = NotificationChannel(
            NotificationKeys.UPDATE_CHANNEL_ID,
            NotificationKeys.UPDATE_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(requestCode, notificationBuilder.build())
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        doOnIntent(intent)
    }

    private fun doOnIntent(intent: Intent?) {
        Log.d(thisTAG, "action=${intent?.action}, data=${intent?.data}")
        intent?.data?.let {
            getTmdbId(it)?.let { media ->
                val mediaArgs = MediaArgs(
                    media.id,
                    media.media_type ?: "tv",
                    media, null
                )
                val bundle = Bundle()
                bundle.putSerializable("media", mediaArgs)
                navController.navigate(R.id.fragmentMedia, bundle)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun getTmdbId(url: Uri): Media? {
        val items = THEMOVIEDB_ID_REGEX.toRegex().find(url.toString())?.destructured?.toList()
        items?.let {
            if (items.size >= 2) {
                val mediaType = it[0]
                val tmdbId = it[1].toInt()
                return Media(
                    id = tmdbId,
                    media_type = mediaType,
                    name = null,
                    poster_path = null,
                    title = null,
                    vote_average = null,
                    backdrop_path = null,
                    overview = null,
                    release_date = null
                )
            }
        }
        return null
    }
}