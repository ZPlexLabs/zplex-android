package zechs.zplex.feature_admin.users

import zechs.zplex.common.ui.mvi.UiAction
import zechs.zplex.common.ui.mvi.UiError
import zechs.zplex.common.ui.mvi.UiEvent
import zechs.zplex.common.ui.mvi.UiState
import zechs.zplex.zplex_api.data.remote.api.admin.model.UserSummaryResponse

data class AdminUsersState(
    val isLoading: Boolean = true,
    val users: List<UserSummaryResponse> = emptyList(),
    val error: UiError? = null,
    val pendingDeleteUsername: String? = null
) : UiState

sealed interface AdminUsersAction : UiAction {
    data object Retry : AdminUsersAction
    data class OpenUser(val username: String) : AdminUsersAction
    data class RequestDelete(val username: String) : AdminUsersAction
    data object ConfirmDelete : AdminUsersAction
    data object DismissDeleteConfirm : AdminUsersAction
}

sealed interface AdminUsersEvent : UiEvent {
    data class OpenUserEdit(val username: String) : AdminUsersEvent
    data class ShowMessage(val message: String) : AdminUsersEvent
}
