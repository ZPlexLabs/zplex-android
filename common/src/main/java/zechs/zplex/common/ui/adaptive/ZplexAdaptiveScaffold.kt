package zechs.zplex.common.ui.adaptive

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable

@Composable
fun ZplexAdaptiveScaffold(
    windowSizeClass: WindowSizeClass,
    navigation: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        Scaffold(bottomBar = { NavigationBar { navigation() } }) { content() }
    } else {
        Row {
            NavigationRail { navigation() }
            Scaffold { content() }
        }
    }
}