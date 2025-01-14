package zechs.zplex.ui.main

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.Interpolator
import android.view.animation.TranslateAnimation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigationrail.NavigationRailView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zechs.zplex.utils.MaterialMotionInterpolator
import zechs.zplex.R
import zechs.zplex.databinding.ActivityMainBinding
import zechs.zplex.service.RemoteLibraryIndexingService

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(
            this,
            com.google.android.material.R.color.m3_sys_color_dark_surface_container
        )
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
                R.id.settingsFragment -> {
                    animationNavColorChange(com.google.android.material.R.color.m3_sys_color_dark_surface)
                    hideSlideDown(binding.bottomNavigationView)
                }

                else -> {
                    animationNavColorChange(com.google.android.material.R.color.m3_sys_color_dark_surface_container)
                    showSlideUp(binding.bottomNavigationView)
                }
            }
        }

        startService(Intent(this, RemoteLibraryIndexingService::class.java))

        if (!hasNotificationPermission()) {
            requestNotificationPermission()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        }
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.i(TAG, "Notification permission was ${if (isGranted) "granted" else "denied"}")
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkNotificationPermission() {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i(TAG, "Notification granted!")
            }

            shouldShowRequestPermissionRationale(permission) -> {
                Log.i(TAG, "Notification permission denied permanently")
            }

            else -> {
                requestNotificationPermission.launch(permission)
            }
        }
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

    private fun slideAnimation(
        view: View,
        fromXDelta: Float, toXDelta: Float,
        fromYDelta: Float, toYDelta: Float,
        show: Boolean
    ) = lifecycleScope.launch {
        if (view.animation?.hasStarted() == true && view.animation?.hasEnded() == false) {
            return@launch
        }
        if (show && view.isVisible) {
            return@launch
        }
        if (!show && view.isGone) {
            return@launch
        }

        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val animationListener = object : AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                view.setLayerType(View.LAYER_TYPE_NONE, null)
                view.isGone = !show
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        }

        val interpolator = if (show) {
            MaterialMotionInterpolator.getEmphasizedDecelerateInterpolator()
        } else {
            MaterialMotionInterpolator.getEmphasizedAccelerateInterpolator()
        }
        createTranslateAnimation(
            fromXDelta,
            toXDelta,
            fromYDelta,
            toYDelta,
            interpolator,
            animationListener
        ).also { view.startAnimation(it) }
    }

    private fun createTranslateAnimation(
        fromXDelta: Float, toXDelta: Float,
        fromYDelta: Float, toYDelta: Float,
        interpolator: Interpolator,
        animationListener: AnimationListener? = null
    ): TranslateAnimation {
        return TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta).apply {
            this.interpolator = interpolator
            duration = 250L
            animationListener?.let { setAnimationListener(it) }
        }
    }

    private fun showSlideUp(view: View) {
        slideAnimation(view, 0f, 0f, view.height.toFloat(), 0f, true)
    }

    private fun hideSlideDown(view: View) {
        slideAnimation(view, 0f, 0f, 0f, view.height.toFloat(), false)
    }

    private fun showSlideInFromLeft(view: View) {
        slideAnimation(view, -view.width.toFloat(), 0f, 0f, 0f, true)
    }

    private fun hideSlideOutFromLeft(view: View) {
        slideAnimation(view, 0f, -view.width.toFloat(), 0f, 0f, false)
    }


    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    companion object {
        const val TAG = "MainActivity"
    }

}