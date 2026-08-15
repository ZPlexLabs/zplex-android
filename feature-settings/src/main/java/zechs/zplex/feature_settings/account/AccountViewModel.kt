package zechs.zplex.feature_settings.account

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import zechs.zplex.common.ui.mvi.MviViewModel
import zechs.zplex.common.ui.theme.ThemeMode
import zechs.zplex.common.ui.theme.ThemePrefsStore
import zechs.zplex.zplex_api.data.local.config.ConfigStorage
import zechs.zplex.zplex_api.data.local.session.SessionStorage
import zechs.zplex.zplex_api.data.local.user.User
import zechs.zplex.zplex_api.data.local.user.UserStorage
import javax.inject.Inject

private const val ADMIN_CAPABILITY_ID = 6

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userStorage: UserStorage,
    private val sessionStorage: SessionStorage,
    private val configStorage: ConfigStorage,
    private val themePrefsStore: ThemePrefsStore,
    @ApplicationContext private val context: Context
) : MviViewModel<AccountState, AccountAction, AccountEvent>(AccountState()) {

    init {
        setState { copy(appVersion = readAppVersion()) }
        userStorage.userFlow().onEach { user -> applyUser(user) }.launchIn(viewModelScope)
        themePrefsStore.themeModeFlow().onEach { mode ->
            setState { copy(themeMode = mode) }
        }.launchIn(viewModelScope)
        viewModelScope.launch { loadServerInfo() }
    }

    override fun onAction(action: AccountAction) {
        when (action) {
            is AccountAction.SetTheme -> viewModelScope.launch { themePrefsStore.setThemeMode(action.mode) }
            AccountAction.RequestLogout -> setState { copy(showLogoutConfirm = true) }
            AccountAction.DismissLogoutConfirm -> setState { copy(showLogoutConfirm = false) }
            AccountAction.ConfirmLogout -> logout()
            AccountAction.OpenHistory -> sendEvent(AccountEvent.NavigateToHistory)
            AccountAction.OpenAdmin -> sendEvent(AccountEvent.NavigateToAdmin)
            AccountAction.OpenProfiles -> sendEvent(AccountEvent.NavigateToProfiles)
            AccountAction.OpenKidsMode -> sendEvent(AccountEvent.NavigateToKidsMode)
        }
    }

    private suspend fun applyUser(user: User?) = coroutineScope {
        if (user == null) return@coroutineScope
        val capabilities = configStorage.getCapabilities().orEmpty()
        val labels = capabilities.filter { it.id in user.capabilities }.map { it.label }
        setState {
            copy(
                isLoading = false,
                firstName = user.firstName,
                lastName = user.lastName,
                username = user.username,
                isAdult = user.isAdult,
                isAdmin = ADMIN_CAPABILITY_ID in user.capabilities,
                capabilityLabels = labels
            )
        }
    }

    private suspend fun loadServerInfo() {
        val host = configStorage.getConfig()?.streamingHost
        setState { copy(streamingHost = host) }
    }

    private fun logout() {
        viewModelScope.launch {
            sessionStorage.clearSession()
            userStorage.clearUser()
        }
    }

    private fun readAppVersion(): String = try {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        info.versionName.orEmpty()
    } catch (e: PackageManager.NameNotFoundException) {
        ""
    }
}
