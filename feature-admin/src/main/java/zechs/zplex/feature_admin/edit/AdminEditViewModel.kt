package zechs.zplex.feature_admin.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import zechs.zplex.common.ui.mvi.MviViewModel
import zechs.zplex.common.ui.mvi.UiError
import zechs.zplex.common.ui.mvi.toUiError
import zechs.zplex.common.utils.Result
import zechs.zplex.zplex_api.data.local.config.ConfigStorage
import zechs.zplex.zplex_api.data.remote.api.admin.model.BlacklistEntry
import zechs.zplex.zplex_api.data.remote.api.admin.model.UserAccessRequest
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.remote.api.suggestions.SearchSuggestion
import zechs.zplex.zplex_api.data.remote.api.suggestions.SuggestionType
import zechs.zplex.zplex_api.data.repository.AdminRepository
import zechs.zplex.zplex_api.data.repository.SuggestionsRepository
import javax.inject.Inject

@HiltViewModel
class AdminEditViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val configStorage: ConfigStorage,
    private val suggestionsRepository: SuggestionsRepository,
    savedStateHandle: SavedStateHandle
) : MviViewModel<AdminEditState, AdminEditAction, AdminEditEvent>(
    AdminEditState(username = checkNotNull(savedStateHandle.get<String>("username")))
) {

    private var catalog: List<SearchSuggestion> = emptyList()

    init {
        load()
    }

    override fun onAction(action: AdminEditAction) {
        when (action) {
            AdminEditAction.Retry -> load()
            is AdminEditAction.ToggleCapability -> setState {
                copy(selectedCapabilityIds = selectedCapabilityIds.toggled(action.id))
            }

            is AdminEditAction.ToggleLibrary -> setState {
                copy(allowedLibraries = allowedLibraries.toggled(action.id))
            }

            is AdminEditAction.SetRatingRank -> setState { copy(maxRatingRank = action.rank) }
            is AdminEditAction.SetAllowUnrated -> setState { copy(allowUnrated = action.allow) }
            AdminEditAction.Save -> save()
            is AdminEditAction.QueryChanged -> onQueryChanged(action.query)
            is AdminEditAction.AddToBlacklist -> addToBlacklist(action.suggestion)
            is AdminEditAction.RemoveFromBlacklist -> removeFromBlacklist(action.entry)
            AdminEditAction.RequestDelete -> setState { copy(showDeleteConfirm = true) }
            AdminEditAction.DismissDeleteConfirm -> setState { copy(showDeleteConfirm = false) }
            AdminEditAction.ConfirmDelete -> deleteUser()
        }
    }

    private fun load() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            coroutineScope {
                val capabilities = configStorage.getCapabilities().orEmpty()
                when (val result = adminRepository.users()) {
                    is Result.Success -> {
                        val user = result.data.firstOrNull { it.username == currentState.username }
                        if (user == null) {
                            setState { copy(isLoading = false, error = UiError("User not found")) }
                            return@coroutineScope
                        }
                        setState {
                            copy(
                                isLoading = false,
                                firstName = user.firstName,
                                lastName = user.lastName,
                                allCapabilities = capabilities,
                                selectedCapabilityIds = user.capabilities.toSet(),
                                allowedLibraries = user.allowedLibraries.toSet(),
                                maxRatingRank = user.maxRatingRank,
                                allowUnrated = user.allowUnrated,
                                blacklist = user.blacklist
                            )
                        }
                    }

                    is Result.Error -> setState { copy(isLoading = false, error = result.toUiError()) }
                }
            }
            when (val result = suggestionsRepository.searchSuggestions()) {
                is Result.Success -> catalog = result.data
                is Result.Error -> Unit
            }
        }
    }

    private fun save() {
        val state = currentState
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            val capabilitiesResult = adminRepository.updateCapabilities(
                state.username, state.selectedCapabilityIds.toList()
            )
            val accessResult = adminRepository.updateAccess(
                state.username,
                UserAccessRequest(
                    allowedLibraries = state.allowedLibraries.toList(),
                    maxRatingRank = state.maxRatingRank,
                    allowUnrated = state.allowUnrated
                )
            )
            setState { copy(isSaving = false) }
            val error = (capabilitiesResult as? Result.Error) ?: (accessResult as? Result.Error)
            if (error != null) {
                sendEvent(AdminEditEvent.ShowMessage(error.message))
            } else {
                sendEvent(AdminEditEvent.ShowMessage("Saved"))
                sendEvent(AdminEditEvent.NavigateBack)
            }
        }
    }

    private fun onQueryChanged(query: String) {
        val results = if (query.isBlank()) {
            emptyList()
        } else {
            catalog.filter { it.title.contains(query, ignoreCase = true) }
        }
        setState { copy(searchQuery = query, searchResults = results) }
    }

    private fun addToBlacklist(suggestion: SearchSuggestion) {
        val mediaType = suggestion.type.toMediaType()
        viewModelScope.launch {
            val username = currentState.username
            when (val result = adminRepository.addBlacklist(username, mediaType, suggestion.tmdbId)) {
                is Result.Success -> setState {
                    copy(
                        blacklist = blacklist + BlacklistEntry(mediaType, suggestion.tmdbId),
                        searchQuery = "",
                        searchResults = emptyList()
                    )
                }

                is Result.Error -> sendEvent(AdminEditEvent.ShowMessage(result.message))
            }
        }
    }

    private fun removeFromBlacklist(entry: BlacklistEntry) {
        viewModelScope.launch {
            val username = currentState.username
            when (val result = adminRepository.removeBlacklist(username, entry.mediaType, entry.tmdbId)) {
                is Result.Success -> setState { copy(blacklist = blacklist - entry) }
                is Result.Error -> sendEvent(AdminEditEvent.ShowMessage(result.message))
            }
        }
    }

    private fun deleteUser() {
        val username = currentState.username
        viewModelScope.launch {
            setState { copy(showDeleteConfirm = false) }
            when (val result = adminRepository.deleteUser(username)) {
                is Result.Success -> sendEvent(AdminEditEvent.NavigateBack)
                is Result.Error -> sendEvent(AdminEditEvent.ShowMessage(result.message))
            }
        }
    }

    private fun SuggestionType.toMediaType(): MediaType = when (this) {
        SuggestionType.movie -> MediaType.MOVIE
        SuggestionType.show -> MediaType.SHOW
    }

    private fun Set<Int>.toggled(id: Int): Set<Int> =
        if (id in this) this - id else this + id
}
