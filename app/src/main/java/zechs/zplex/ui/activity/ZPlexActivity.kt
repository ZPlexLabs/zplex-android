package zechs.zplex.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_zplex.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.db.FilesDatabase
import zechs.zplex.repository.FilesRepository
import zechs.zplex.repository.ReleasesRepository
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.repository.TvdbRepository
import zechs.zplex.ui.viewmodel.file.FileViewModel
import zechs.zplex.ui.viewmodel.file.FilesViewModelProviderFactory
import zechs.zplex.ui.viewmodel.release_log.ReleaseLogViewModel
import zechs.zplex.ui.viewmodel.release_log.ReleaseLogViewModelProviderFactory
import zechs.zplex.ui.viewmodel.tmdb.TmdbViewModel
import zechs.zplex.ui.viewmodel.tmdb.TmdbViewModelProviderFactory
import zechs.zplex.ui.viewmodel.tvdb.TvdbViewModel
import zechs.zplex.ui.viewmodel.tvdb.TvdbViewModelProviderFactory


class ZPlexActivity : AppCompatActivity() {

    lateinit var viewModel: FileViewModel
    lateinit var logsViewModel: ReleaseLogViewModel
    lateinit var tvdbViewModel: TvdbViewModel
    lateinit var tmdbViewModel: TmdbViewModel

    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filesRepository = FilesRepository(FilesDatabase(this))
        val viewModelProviderFactory = FilesViewModelProviderFactory(application, filesRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(FileViewModel::class.java)

        val logsRepository = ReleasesRepository()
        val logsModelProviderFactory =
            ReleaseLogViewModelProviderFactory(application, logsRepository)
        logsViewModel =
            ViewModelProvider(this, logsModelProviderFactory).get(ReleaseLogViewModel::class.java)

        val seriesRepository = TvdbRepository()
        val seriesModelProviderFactory =
            TvdbViewModelProviderFactory(application, seriesRepository)
        tvdbViewModel =
            ViewModelProvider(this, seriesModelProviderFactory).get(TvdbViewModel::class.java)

        val moviesRepository = TmdbRepository()
        val moviesModelProviderFactory =
            TmdbViewModelProviderFactory(application, moviesRepository)
        tmdbViewModel =
            ViewModelProvider(this, moviesModelProviderFactory).get(TmdbViewModel::class.java)


        setContentView(R.layout.activity_zplex)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.mainNavHostFragment
        ) as NavHostFragment

        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.aboutFragment) {
                val transition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
                transition.duration = 500L
                transition.excludeTarget(android.R.id.statusBarBackground, true)
                transition.excludeTarget(android.R.id.navigationBarBackground, true)
                TransitionManager.beginDelayedTransition(root, transition)
                bottomNavigationView.visibility = View.GONE
                appBarLayout.visibility = View.GONE
                view1.visibility = View.GONE
                view2.visibility = View.GONE
                window.navigationBarColor = ContextCompat.getColor(this, R.color.cardColor)
                window.statusBarColor = ContextCompat.getColor(this, R.color.cardColor)
            } else {
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
                window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
                val transition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
                transition.duration = 500L
                transition.excludeTarget(toolbar, true)
                transition.excludeTarget(bottomNavigationView, true)
                transition.excludeTarget(android.R.id.statusBarBackground, true)
                transition.excludeTarget(android.R.id.navigationBarBackground, true)
                TransitionManager.beginDelayedTransition(root, transition)
                bottomNavigationView.visibility = View.VISIBLE
                view1.visibility = View.VISIBLE
                view2.visibility = View.VISIBLE
                if (destination.id == R.id.searchFragment) {
                    appBarLayout.visibility = View.GONE
                } else {
                    appBarLayout.visibility = View.VISIBLE
                }
            }

            when (destination.id) {
                R.id.homeFragment -> {
                    view1.visibility = View.VISIBLE
                    toolbar.setTitle(R.string.home)
                }
                R.id.searchFragment -> {
                    view1.visibility = View.GONE
                    toolbar.setTitle(R.string.search)
                }
                R.id.myShowsFragment -> {
                    view1.visibility = View.VISIBLE
                    toolbar.setTitle(R.string.my_shows)
                }
            }

        }

        GlobalScope.launch(Dispatchers.Main) {
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