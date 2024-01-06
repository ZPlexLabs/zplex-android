package zechs.zplex.ui.main

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.TranslateAnimation
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.ActivityMainBinding
import zechs.zplex.service.RemoteLibraryIndexingService


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val viewModel by viewModels<MainViewModel>()

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
                R.id.fragmentCollection, R.id.watchFragment,
                R.id.bigImageFragment, R.id.signInFragment,
                R.id.setupFragment, R.id.settingsFragment -> {
                    animationNavColorChange(R.color.statusBarColor)
                    hideSlideDown(binding.bottomNavigationView)
                }

                else -> {
                    animationNavColorChange(R.color.fadedColor)
                    showSlideUp(binding.bottomNavigationView)
                }
            }
        }

        folderSelectionObserver()
        startService(Intent(this, RemoteLibraryIndexingService::class.java))
    }

    private fun animationNavColorChange(
        @ColorRes color: Int
    ) = lifecycleScope.launch {
        val from = window.navigationBarColor
        val to = ContextCompat.getColor(applicationContext, color)
        ValueAnimator
            .ofArgb(from, to)
            .also {
                it.addUpdateListener { animator ->
                    window.navigationBarColor = (animator.animatedValue as Int)
                }
                it.start()
            }
    }

    private fun showSlideUp(
        view: View
    ) = lifecycleScope.launch {
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

    private fun hideSlideDown(
        view: View
    ) = lifecycleScope.launch {
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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


    private fun folderSelectionObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.needToPickFolder.collect { needToPickFolder ->
                    if (needToPickFolder && navController.currentDestination?.id != R.id.setupFragment) {
                        navController.navigate(R.id.setupFragment)
                    }
                    Log.d(TAG, "folderSelectionObserver: (needToPickFolder=$needToPickFolder)")
                }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }

}