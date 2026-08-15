package zechs.zplex.feature_settings.account

import zechs.zplex.common.ui.mvi.UiAction
import zechs.zplex.common.ui.mvi.UiEvent
import zechs.zplex.common.ui.mvi.UiState
import zechs.zplex.common.ui.theme.ThemeMode

data class AccountState(
    val isLoading: Boolean = true,
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val isAdult: Boolean = false,
    val isAdmin: Boolean = false,
    val capabilityLabels: List<String> = emptyList(),
    val streamingHost: String? = null,
    val appVersion: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showLogoutConfirm: Boolean = false
) : UiState

sealed interface AccountAction : UiAction {
    data class SetTheme(val mode: ThemeMode) : AccountAction
    data object RequestLogout : AccountAction
    data object ConfirmLogout : AccountAction
    data object DismissLogoutConfirm : AccountAction
    data object OpenHistory : AccountAction
    data object OpenAdmin : AccountAction
    data object OpenProfiles : AccountAction
    data object OpenKidsMode : AccountAction
}

sealed interface AccountEvent : UiEvent {
    data object NavigateToHistory : AccountEvent
    data object NavigateToAdmin : AccountEvent
    data class ShowMessage(val message: String) : AccountEvent
}
