package zechs.zplex.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
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

        bottomNavigationView.setupWithNavController(mainNavHostFragment.findNavController())

        mainNavHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id == R.id.aboutFragment) {
                    bottomNavigationView.visibility = View.GONE
                } else {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }

        GlobalScope.launch(Dispatchers.Main) {
            FirebaseMessaging.getInstance().subscribeToTopic("all")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = mainNavHostFragment.findNavController()
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}