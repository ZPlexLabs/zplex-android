package zechs.zplex.ui.activity.main

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.ActivityMainBinding
import zechs.zplex.db.WatchlistDatabase
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.repository.TmdbRepository
import zechs.zplex.repository.WatchedRepository
import zechs.zplex.repository.ZPlexRepository
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
import zechs.zplex.utils.Constants.THEMOVIEDB_ID_REGEX


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    lateinit var homeViewModel: HomeViewModel
    lateinit var browseViewModel: BrowseViewModel
    lateinit var searchViewModel: SearchViewModel
    lateinit var myShowsViewModel: MyShowsViewModel
    lateinit var mediaViewModel: MediaViewModel
    lateinit var episodesViewModel: EpisodesViewModel
    lateinit var watchViewModel: WatchViewModel
    lateinit var castViewModel: CastViewModel
    lateinit var collectionViewModel: CollectionViewModel
    lateinit var upcomingViewModel: UpcomingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        val db = WatchlistDatabase(this)

        val tmdbRepository = TmdbRepository(db)
        val zplexRepository = ZPlexRepository()
        val watchedRepository = WatchedRepository(db)

        homeViewModel = ViewModelProvider(
            this,
            HomeViewModelProviderFactory(
                application, tmdbRepository, watchedRepository
            )
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
                tmdbRepository, zplexRepository
            )
        )[MediaViewModel::class.java]

        episodesViewModel = ViewModelProvider(
            this,
            EpisodesViewModelProviderFactory(
                application,
                tmdbRepository, zplexRepository
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

        collectionViewModel = ViewModelProvider(
            this,
            CollectionViewModelProviderFactory(application, tmdbRepository)
        )[CollectionViewModel::class.java]

        upcomingViewModel = ViewModelProvider(
            this,
            UpcomingViewModelProviderFactory(application, tmdbRepository)
        )[UpcomingViewModel::class.java]

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.mainNavHostFragment
        ) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragmentMedia, R.id.castsFragment,
                R.id.episodesListFragment, R.id.fragmentList,
                R.id.fragmentCollection, R.id.shareBottomSheet,
                R.id.watchFragment, R.id.bigImageFragment -> {
                    hideSlideDown(binding.bottomNavigationView)
                }
                else -> {
                    showSlideUp(binding.bottomNavigationView)
                }
            }
        }

        doOnIntent(intent)
    }

    private fun showSlideUp(view: View) = lifecycleScope.launch {
        if (view.isGone) {
            view.isVisible = true
            TranslateAnimation(
                0f, 0f,
                view.height.toFloat(), 0f
            ).apply {
                interpolator = AccelerateInterpolator()
                duration = 250L
            }.also {
                view.startAnimation(it)
            }
        }
    }

    private fun hideSlideDown(view: View) = lifecycleScope.launch {
        if (view.isVisible) {
            TranslateAnimation(
                0f, 0f, 0f,
                view.height.toFloat()
            ).apply {
                interpolator = AccelerateInterpolator()
                duration = 250L
            }.also {
                view.startAnimation(it)
            }
            view.isGone = true
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        doOnIntent(intent)
    }

    private fun doOnIntent(intent: Intent?) {
        Log.d(TAG, "action=${intent?.action}, data=${intent?.data}")
        intent?.data?.let {
            getTmdbId(it)?.let { media ->
                val bundle = Bundle()
                bundle.putSerializable("media", media)
                navController.navigate(R.id.fragmentMedia, bundle)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun getTmdbId(url: Uri): Media? {
        val items = THEMOVIEDB_ID_REGEX.toRegex().find(url.toString())?.destructured?.toList()
        return items?.let {
            if (items.size >= 2) {
                val mediaType = it[0]
                try {
                    val tmdbId = it[1].toInt()
                    Media(
                        id = tmdbId,
                        media_type = mediaType,
                        name = null,
                        poster_path = null,
                        title = null,
                        vote_average = null,
                        backdrop_path = null,
                        overview = null,
                        release_date = null,
                        first_air_date = null
                    )
                } catch (nfe: NumberFormatException) {
                    Toast.makeText(this, "Bad url", Toast.LENGTH_SHORT).show()
                }
            }
            null
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v: View? = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    companion object {
        const val TAG = "MainActivity"
    }
}