package zechs.zplex.common.ui.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

/** Provided by `ZplexAppShell` so any screen can surface a one-shot message consistently. */
val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }
