package zechs.zplex.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
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

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.aboutFragment -> bottomNavigationView.visibility = View.GONE
                R.id.bigImageFragment -> bottomNavigationView.visibility = View.GONE
                else -> {
                    val colorPrimaryDark = ContextCompat.getColor(this, R.color.colorPrimaryDark)
                    window.statusBarColor = colorPrimaryDark
                    window.navigationBarColor = colorPrimaryDark
                    bottomNavigationView.visibility = View.VISIBLE
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