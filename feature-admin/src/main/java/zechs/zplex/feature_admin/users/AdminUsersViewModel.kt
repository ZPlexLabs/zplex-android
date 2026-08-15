package zechs.zplex.feature_admin.users

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import zechs.zplex.common.ui.mvi.MviViewModel
import zechs.zplex.common.ui.mvi.toUiError
import zechs.zplex.common.utils.Result
import zechs.zplex.zplex_api.data.repository.AdminRepository
import javax.inject.Inject

@HiltViewModel
class AdminUsersViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : MviViewModel<AdminUsersState, AdminUsersAction, AdminUsersEvent>(AdminUsersState()) {

    init {
        load()
    }

    override fun onAction(action: AdminUsersAction) {
        when (action) {
            AdminUsersAction.Retry -> load()
            is AdminUsersAction.OpenUser -> sendEvent(AdminUsersEvent.OpenUserEdit(action.username))
            is AdminUsersAction.RequestDelete -> setState { copy(pendingDeleteUsername = action.username) }
            AdminUsersAction.DismissDeleteConfirm -> setState { copy(pendingDeleteUsername = null) }
            AdminUsersAction.ConfirmDelete -> confirmDelete()
        }
    }

    private fun load() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = adminRepository.users()) {
                is Result.Success -> setState { copy(isLoading = false, users = result.data) }
                is Result.Error -> setState { copy(isLoading = false, error = result.toUiError()) }
            }
        }
    }

    private fun confirmDelete() {
        val username = currentState.pendingDeleteUsername ?: return
        viewModelScope.launch {
            when (val result = adminRepository.deleteUser(username)) {
                is Result.Success -> {
                    setState {
                        copy(
                            pendingDeleteUsername = null,
                            users = users.filterNot { it.username == username }
                        )
                    }
                }

                is Result.Error -> {
                    setState { copy(pendingDeleteUsername = null) }
                    sendEvent(AdminUsersEvent.ShowMessage(result.message))
                }
            }
        }
    }
}
