package zechs.zplex.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_zplex.*
import zechs.zplex.BuildConfig
import zechs.zplex.R
import zechs.zplex.db.FilesDatabase
import zechs.zplex.repository.FilesRepository
import zechs.zplex.repository.ReleasesRepository
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.repository.TvdbRepository
import zechs.zplex.ui.fragment.about.AboutViewModel
import zechs.zplex.ui.fragment.about.AboutViewModelProviderFactory
import zechs.zplex.ui.fragment.home.HomeViewModel
import zechs.zplex.ui.fragment.home.HomeViewModelProviderFactory
import zechs.zplex.ui.fragment.myshows.MyShowsViewModel
import zechs.zplex.ui.fragment.myshows.MyShowsViewModelProviderFactory
import zechs.zplex.ui.fragment.search.SearchViewModel
import zechs.zplex.ui.fragment.search.SearchViewModelProviderFactory


class ZPlexActivity : AppCompatActivity() {

    lateinit var homeViewModel: HomeViewModel
    lateinit var searchViewModel: SearchViewModel
    lateinit var myShowsViewModel: MyShowsViewModel
    lateinit var aboutViewModel: AboutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val releasesRepository = ReleasesRepository()
        val filesRepository = FilesRepository(FilesDatabase(this))
        val tvdbRepository = TvdbRepository()
        val tmdbRepository = TmdbRepository()

        homeViewModel = ViewModelProvider(
            this,
            HomeViewModelProviderFactory(
                application, filesRepository, releasesRepository
            )
        )[HomeViewModel::class.java]

        searchViewModel = ViewModelProvider(
            this,
            SearchViewModelProviderFactory(application, filesRepository)
        )[SearchViewModel::class.java]

        myShowsViewModel = ViewModelProvider(
            this,
            MyShowsViewModelProviderFactory(filesRepository)
        )[MyShowsViewModel::class.java]

        aboutViewModel = ViewModelProvider(
            this,
            AboutViewModelProviderFactory(
                application, filesRepository,
                tvdbRepository, tmdbRepository
            )
        )[AboutViewModel::class.java]

        setContentView(R.layout.activity_zplex)


        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.mainNavHostFragment
        ) as NavHostFragment

        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.searchFragment, R.id.myShowsFragment)
        )

        setSupportActionBar(toolbar)

        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.aboutFragment -> {
                    toolbar.title = ""
                    val transition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
//                    transition.duration = 500L
                    transition.excludeTarget(android.R.id.statusBarBackground, true)
                    transition.excludeTarget(android.R.id.navigationBarBackground, true)
                    TransitionManager.beginDelayedTransition(root, transition)
                    bottomNavigationView.visibility = View.GONE
//                    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_round_keyboard_backspace_24)
                    appBarLayout.visibility = View.GONE
//                    view1.visibility = View.GONE
//                    view2.visibility = View.GONE
//                    window.navigationBarColor = ContextCompat.getColor(this, R.color.cardColor)
//                    window.statusBarColor = ContextCompat.getColor(this, R.color.cardColor)
                }
                else -> {
                    window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
                    window.navigationBarColor =
                        ContextCompat.getColor(this, R.color.colorPrimaryDark)
                    val transition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
                    transition.duration = 500L
                    transition.excludeTarget(toolbar, true)
                    transition.excludeTarget(bottomNavigationView, true)
                    transition.excludeTarget(android.R.id.statusBarBackground, true)
                    transition.excludeTarget(android.R.id.navigationBarBackground, true)
                    TransitionManager.beginDelayedTransition(root, transition)
                    bottomNavigationView.visibility = View.VISIBLE
                    appBarLayout.visibility = View.VISIBLE
//                    view1.visibility = View.VISIBLE
//                    view2.visibility = View.VISIBLE
//                    if (destination.id == R.id.searchFragment) {
//                        appBarLayout.visibility = View.GONE
//                    } else {
//                        appBarLayout.visibility = View.VISIBLE
//                    }
                }
            }

            when (destination.id) {
                R.id.homeFragment -> {
                    toolbar.setTitle(R.string.home)
                }
                R.id.searchFragment -> {
//                    view1.visibility = View.GONE
                    toolbar.setTitle(R.string.search)
                }
                R.id.myShowsFragment -> {
                    toolbar.setTitle(R.string.my_shows)
                }
            }

            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
            FirebaseMessaging.getInstance().subscribeToTopic("all")

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}