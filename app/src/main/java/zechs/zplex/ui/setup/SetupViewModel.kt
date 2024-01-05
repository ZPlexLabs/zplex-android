package zechs.zplex.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import zechs.zplex.utils.SessionManager
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        const val TAG = "SetupViewModel"
    }

    fun saveMoviesFolder(id: String) = viewModelScope.launch {
        sessionManager.saveMovieFolder(id)
    }

    fun saveShowsFolder(id: String) = viewModelScope.launch {
        sessionManager.saveShowsFolder(id)
    }

    val hasBothFolders = sessionManager.hasBothFolders()

}