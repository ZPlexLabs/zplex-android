package zechs.zplex.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import zechs.zplex.common.ui.kids.KidsModeViewModel
import zechs.zplex.common.ui.theme.ThemeMode
import zechs.zplex.common.ui.theme.ThemeViewModel
import zechs.zplex.common.ui.theme.ZplexTheme

private val KIDS_MODE_DESTINATIONS = setOf(
    TopLevelDestination.HOME, TopLevelDestination.MOVIES, TopLevelDestination.SHOWS
)

/**
 * Adaptive navigation shell: bottom bar on compact widths, nav rail on larger screens.
 * Shared detail/player destinations render full-screen without the navigation suite.
 */
@Composable
fun ZplexAppShell(
    navController: NavHostController = rememberNavController(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    kidsModeViewModel: KidsModeViewModel = hiltViewModel()
) {
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    val kidsModeEnabled by kidsModeViewModel.isEnabled.collectAsStateWithLifecycle()

    ZplexTheme(darkTheme = themeMode.resolveIsDark()) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination
        val isTopLevel = TopLevelDestination.entries.any { destination ->
            currentDestination?.hierarchy?.any { it.route == destination.route } == true
        }

        val layoutType = if (isTopLevel) {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
        } else {
            NavigationSuiteType.None
        }

        val visibleDestinations = if (kidsModeEnabled) {
            TopLevelDestination.entries.filter { it in KIDS_MODE_DESTINATIONS }
        } else {
            TopLevelDestination.entries
        }

        Box(modifier = Modifier.fillMaxSize()) {
            NavigationSuiteScaffold(
                layoutType = layoutType,
                navigationSuiteItems = {
                    visibleDestinations.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == destination.route
                        } == true
                        item(
                            selected = selected,
                            onClick = { navController.navigateToTopLevel(destination) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            ) {
                ZplexNavHost(navController = navController)
            }

            if (kidsModeEnabled) {
                KidsModeExitButton(
                    kidsModeViewModel = kidsModeViewModel,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun KidsModeExitButton(
    kidsModeViewModel: KidsModeViewModel,
    modifier: Modifier = Modifier
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    IconButton(onClick = { showPinDialog = true }, modifier = modifier) {
        Icon(Icons.Filled.Lock, contentDescription = "Exit kids mode")
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pin = ""
                error = null
            },
            title = { Text("Enter PIN to exit kids mode") },
            text = {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = error != null,
                    supportingText = { error?.let { Text(it) } }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        if (kidsModeViewModel.verifyPinAndExit(pin)) {
                            showPinDialog = false
                            pin = ""
                            error = null
                        } else {
                            error = "Incorrect PIN"
                        }
                    }
                }) { Text("Exit") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinDialog = false
                    pin = ""
                    error = null
                }) { Text("Cancel") }
            }
        )
    }
}

private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun ThemeMode.resolveIsDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

