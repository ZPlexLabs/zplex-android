package zechs.zplex.feature_settings.profiles

import zechs.zplex.common.ui.mvi.UiAction
import zechs.zplex.common.ui.mvi.UiEvent
import zechs.zplex.common.ui.mvi.UiState
import zechs.zplex.zplex_api.data.local.accounts.SavedAccount

data class ProfilesState(
    val isLoading: Boolean = true,
    val accounts: List<SavedAccount> = emptyList(),
    val activeUsername: String? = null,
    val pendingRemoveUsername: String? = null,
    val showAddAccountConfirm: Boolean = false
) : UiState

sealed interface ProfilesAction : UiAction {
    data class SwitchTo(val account: SavedAccount) : ProfilesAction
    data class RequestRemove(val username: String) : ProfilesAction
    data object ConfirmRemove : ProfilesAction
    data object DismissRemove : ProfilesAction
    data object RequestAddAccount : ProfilesAction
    data object ConfirmAddAccount : ProfilesAction
    data object DismissAddAccount : ProfilesAction
}

sealed interface ProfilesEvent : UiEvent
