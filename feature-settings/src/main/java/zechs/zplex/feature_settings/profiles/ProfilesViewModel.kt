package zechs.zplex.feature_settings.profiles

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import zechs.zplex.common.ui.mvi.MviViewModel
import zechs.zplex.zplex_api.data.local.user.UserStorage
import zechs.zplex.zplex_api.data.repository.AccountSwitchRepository
import javax.inject.Inject

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val accountSwitchRepository: AccountSwitchRepository,
    private val userStorage: UserStorage
) : MviViewModel<ProfilesState, ProfilesAction, ProfilesEvent>(ProfilesState()) {

    init {
        combine(
            accountSwitchRepository.savedAccounts(),
            userStorage.userFlow()
        ) { accounts, user -> accounts to user?.username }
            .onEach { (accounts, activeUsername) ->
                setState { copy(isLoading = false, accounts = accounts, activeUsername = activeUsername) }
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: ProfilesAction) {
        when (action) {
            is ProfilesAction.SwitchTo -> viewModelScope.launch {
                accountSwitchRepository.switchTo(action.account)
            }

            is ProfilesAction.RequestRemove -> setState { copy(pendingRemoveUsername = action.username) }
            ProfilesAction.DismissRemove -> setState { copy(pendingRemoveUsername = null) }
            ProfilesAction.ConfirmRemove -> {
                val username = currentState.pendingRemoveUsername ?: return
                viewModelScope.launch {
                    accountSwitchRepository.removeAccount(username)
                    setState { copy(pendingRemoveUsername = null) }
                }
            }

            ProfilesAction.RequestAddAccount -> setState { copy(showAddAccountConfirm = true) }
            ProfilesAction.DismissAddAccount -> setState { copy(showAddAccountConfirm = false) }
            ProfilesAction.ConfirmAddAccount -> viewModelScope.launch {
                setState { copy(showAddAccountConfirm = false) }
                accountSwitchRepository.addAccount()
            }
        }
    }
}
