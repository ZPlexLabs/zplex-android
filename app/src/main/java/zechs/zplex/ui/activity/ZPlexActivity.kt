package zechs.zplex.ui.activity

import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.activity_zplex.*
import zechs.zplex.BuildConfig
import zechs.zplex.R
import zechs.zplex.db.WatchlistDatabase
import zechs.zplex.repository.FilesRepository
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.ui.fragment.browse.BrowseViewModel
import zechs.zplex.ui.fragment.browse.BrowseViewModelProviderFactory
import zechs.zplex.ui.fragment.cast.CastViewModel
import zechs.zplex.ui.fragment.cast.CastViewModelProviderFactory
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
import zechs.zplex.ui.fragment.watch.WatchViewModel
import zechs.zplex.ui.fragment.watch.WatchViewModelProviderFactory


class ZPlexActivity : AppCompatActivity() {

    lateinit var homeViewModel: HomeViewModel
    lateinit var browseViewModel: BrowseViewModel
    lateinit var searchViewModel: SearchViewModel
    lateinit var myShowsViewModel: MyShowsViewModel
    lateinit var mediaViewModel: MediaViewModel
    lateinit var episodesViewModel: EpisodesViewModel
    lateinit var watchViewModel: WatchViewModel
    lateinit var castViewModel: CastViewModel

//    private val currentNavigationFragment: Fragment?
//        get() = supportFragmentManager.findFragmentById(R.id.mainNavHostFragment)
//            ?.childFragmentManager
//            ?.fragments
//            ?.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementsUseOverlay = false
        super.onCreate(savedInstanceState)

        val filesRepository = FilesRepository()
        val tmdbRepository = TmdbRepository(WatchlistDatabase(this))

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
            MediaViewModelProviderFactory(application, filesRepository, tmdbRepository)
        )[MediaViewModel::class.java]

        episodesViewModel = ViewModelProvider(
            this,
            EpisodesViewModelProviderFactory(application, filesRepository, tmdbRepository)
        )[EpisodesViewModel::class.java]

        watchViewModel = ViewModelProvider(
            this,
            WatchViewModelProviderFactory(application, tmdbRepository)
        )[WatchViewModel::class.java]

        castViewModel = ViewModelProvider(
            this,
            CastViewModelProviderFactory(application, tmdbRepository)
        )[CastViewModel::class.java]

        setContentView(R.layout.activity_zplex)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.mainNavHostFragment
        ) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        val colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary)
        val colorPrimaryDark = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragmentMedia -> {
                    window.navigationBarColor = colorPrimary
                    bottomNavigationView.isVisible = false
                    view1.isVisible = false
                }
                R.id.episodesListFragment -> {
                    bottomNavigationView.isVisible = false
                    view1.isVisible = false
                }
                R.id.watchFragment -> {
                    bottomNavigationView.isVisible = false
                    view1.isVisible = false
                }
                R.id.castsFragment -> {
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

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.mainNavHostFragment
        ) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}