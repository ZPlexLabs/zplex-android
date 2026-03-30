package zechs.zplex.ui.zplex

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.ActivityZplexBinding
import zechs.zplex.zplex_api.data.local.auth.AuthState

@AndroidEntryPoint
class ZPlexActivity : AppCompatActivity() {

    private lateinit var binding: ActivityZplexBinding
    private lateinit var navController: NavController

    private val viewModel by viewModels<ZPlexViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityZplexBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.mainNavHostFragment
        ) as NavHostFragment
        navController = navHostFragment.navController

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.updatePadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        redirectOnLogin()

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
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            // show splash / do nothing
                        }

                        is AuthState.LoggedIn -> {
                            setGraph(true)
                        }

                        is AuthState.LoggedOut -> {
                            setGraph(false)
                        }
                    }
                }
            }
        }
    }
    private var graphSet = false

    private fun setGraph(isLoggedIn: Boolean) {
        Log.d(TAG, "AuthState -> isLoggedIn=$isLoggedIn")

        val newGraph = navController.navInflater.inflate(
            if (isLoggedIn) R.navigation.zplex_graph
            else zechs.zplex.feature_auth.R.navigation.auth_nav_graph
        )

        val newGraphId = newGraph.id
        Log.d(TAG, "GraphCheck -> newGraphId=$newGraphId")

        if (graphSet) {
            val currentGraphId = navController.graph.id
            Log.d(TAG, "GraphCheck -> currentGraphId=$currentGraphId")

            if (currentGraphId == newGraphId) {
                Log.d(TAG, "GraphSwitch -> skipped (same graph)")
                updateBottomNav(isLoggedIn)
                return
            }
        } else {
            Log.d(TAG, "GraphCheck -> no graph set yet")
        }

        Log.d(TAG, "GraphSwitch -> switching to ${if (isLoggedIn) "MAIN_GRAPH" else "AUTH_GRAPH"}")

        navController.graph = newGraph
        graphSet = true

        updateBottomNav(isLoggedIn)
    }

    private fun updateBottomNav(isLoggedIn: Boolean) {
        Log.d(TAG, "BottomNav -> isVisible=$isLoggedIn")

        binding.bottomNavigationView.isGone = !isLoggedIn

        if (isLoggedIn) {
            Log.d(TAG, "BottomNav -> attaching with NavController")
            binding.bottomNavigationView.setupWithNavController(navController)
        } else {
            Log.d(TAG, "BottomNav -> hidden, skipping setup")
        }
    }

    companion object {
        const val TAG = "ZPlexActivity"
    }

}