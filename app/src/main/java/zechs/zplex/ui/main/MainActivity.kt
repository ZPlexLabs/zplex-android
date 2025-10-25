package zechs.zplex.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.Interpolator
import android.view.animation.TranslateAnimation
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.ThisApp
import zechs.zplex.databinding.ActivityMainBinding
import zechs.zplex.service.CacheCleanupWorker
import zechs.zplex.service.RemoteLibraryIndexingService
import zechs.zplex.utils.Constants.CACHE_TTL_IN_DAYS
import zechs.zplex.utils.MaterialMotionInterpolator
import zechs.zplex.utils.ext.navigateSafe
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var workManager: WorkManager

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.mainNavHostFragment
        ) as NavHostFragment
        navController = navHostFragment.navController

//        ViewCompat.setOnApplyWindowInsetsListener(binding.zplexFrame) { view, insets ->
//            val bars = insets.getInsets(
//                WindowInsetsCompat.Type.systemBars()
//                        or WindowInsetsCompat.Type.displayCutout()
//            )
//            view.updatePadding(
//                left = bars.left,
//                top = bars.top,
//                right = bars.right,
//                bottom = bars.bottom,
//            )
//            WindowInsetsCompat.CONSUMED
//        }
        redirectOnLogin()

        if (!hasNotificationPermission()) {
            requestNotificationPermission()
        }

        scheduleCacheCleanup()
    }

    private fun scheduleCacheCleanup() {
        val cleanupWorkRequest = PeriodicWorkRequestBuilder<CacheCleanupWorker>(
            CACHE_TTL_IN_DAYS, TimeUnit.DAYS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "CacheCleanupJob",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupWorkRequest
        )
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

    private fun redirectOnLogin() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hasLoggedIn
                    .filter { it }
                    .collect {
                        Log.d(TAG, "User logged in, navigating to Landing Fragment")
                        navController.navigateSafe(R.id.action_serverFragment_to_landingFragment)
                    }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }

}