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
import zechs.zplex.ui.FileViewModel
import zechs.zplex.ui.FilesViewModelProviderFactory
import zechs.zplex.ui.ReleaseLogViewModel
import zechs.zplex.ui.ReleaseLogViewModelProviderFactory


class ZPlexActivity : AppCompatActivity() {

    lateinit var viewModel: FileViewModel
    lateinit var logsViewModel: ReleaseLogViewModel

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