package zechs.zplex.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.transition.MaterialFade
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

    private val currentNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.mainNavHostFragment)
            ?.childFragmentManager
            ?.fragments
            ?.first()

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
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.aboutFragment -> {
                    window.navigationBarColor = Color.parseColor("#121115")
                    bottomNavigationView.isVisible = false
                    view1.isVisible = false

                    currentNavigationFragment?.apply {
                        enterTransition = MaterialFade()
                        exitTransition = MaterialSharedAxis(
                            MaterialSharedAxis.Y, false
                        ).apply {
                            duration = 500L
                        }
                        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
                    }
                }

                R.id.bigImageFragment -> bottomNavigationView.visibility = View.GONE
                else -> {
                    val colorPrimaryDark =
                        ContextCompat.getColor(this, R.color.colorPrimaryDark)
                    window.statusBarColor = colorPrimaryDark
                    window.navigationBarColor = colorPrimaryDark
                    bottomNavigationView.isVisible = true
                    view1.isVisible = true
                }
            }
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        FirebaseMessaging.getInstance().subscribeToTopic("all")
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.mainNavHostFragment
        ) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}