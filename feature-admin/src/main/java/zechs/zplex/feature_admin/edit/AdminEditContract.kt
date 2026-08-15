package zechs.zplex.feature_admin.edit

import zechs.zplex.common.ui.mvi.UiAction
import zechs.zplex.common.ui.mvi.UiError
import zechs.zplex.common.ui.mvi.UiEvent
import zechs.zplex.common.ui.mvi.UiState
import zechs.zplex.zplex_api.data.remote.api.admin.model.BlacklistEntry
import zechs.zplex.zplex_api.data.remote.api.config.model.Capability
import zechs.zplex.zplex_api.data.remote.api.suggestions.SearchSuggestion

/** Library ids match the backend's `zechs.zplex.common.model.Library` (1=Movies, 2=Shows). */
val LIBRARY_LABELS = linkedMapOf(1 to "Movies", 2 to "Shows")

/** Rating ranks match the backend's `RatingRank` (1..5); `NO_CEILING` mirrors the admin bootstrap value. */
const val NO_CEILING = Int.MAX_VALUE
val RATING_RANK_LABELS = linkedMapOf(
    1 to "General audiences",
    2 to "Parental guidance",
    3 to "Teen",
    4 to "Mature",
    5 to "Adults only",
    NO_CEILING to "No ceiling"
)

data class AdminEditState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: UiError? = null,
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val allCapabilities: List<Capability> = emptyList(),
    val selectedCapabilityIds: Set<Int> = emptySet(),
    val allowedLibraries: Set<Int> = emptySet(),
    val maxRatingRank: Int = 0,
    val allowUnrated: Boolean = false,
    val blacklist: List<BlacklistEntry> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<SearchSuggestion> = emptyList(),
    val showDeleteConfirm: Boolean = false
) : UiState

sealed interface AdminEditAction : UiAction {
    data object Retry : AdminEditAction
    data class ToggleCapability(val id: Int) : AdminEditAction
    data class ToggleLibrary(val id: Int) : AdminEditAction
    data class SetRatingRank(val rank: Int) : AdminEditAction
    data class SetAllowUnrated(val allow: Boolean) : AdminEditAction
    data object Save : AdminEditAction
    data class QueryChanged(val query: String) : AdminEditAction
    data class AddToBlacklist(val suggestion: SearchSuggestion) : AdminEditAction
    data class RemoveFromBlacklist(val entry: BlacklistEntry) : AdminEditAction
    data object RequestDelete : AdminEditAction
    data object ConfirmDelete : AdminEditAction
    data object DismissDeleteConfirm : AdminEditAction
}

sealed interface AdminEditEvent : UiEvent {
    data object NavigateBack : AdminEditEvent
    data class ShowMessage(val message: String) : AdminEditEvent
}
