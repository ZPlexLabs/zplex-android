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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.ActivityMainBinding
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.utils.Constants.THEMOVIEDB_ID_REGEX


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
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