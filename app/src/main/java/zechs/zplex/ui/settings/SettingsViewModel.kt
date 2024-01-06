package zechs.zplex.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.utils.SessionManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        const val TAG = "SettingsViewModel"
    }

    fun saveMoviesFolder(id: String) = viewModelScope.launch {
        sessionManager.saveMovieFolder(id)
    }

    fun saveShowsFolder(id: String) = viewModelScope.launch {
        sessionManager.saveShowsFolder(id)
    }

    val hasMovieFolder = sessionManager.fetchMovieFolderFlow()
    val hasShowsFolder = sessionManager.fetchShowsFolderFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    fun logOut() = viewModelScope.launch(Dispatchers.IO) {
        _loading.value = true
        val savedMovies = async {
            tmdbRepository.getSavedMovies().value?.map { movie ->
                async { tmdbRepository.upsertMovie(movie.copy(fileId = null)) }
            }?.awaitAll()
        }

        val savedShows = async {
            tmdbRepository.getSavedShows().value?.map { show ->
                async { tmdbRepository.upsertShow(show.copy(fileId = null)) }
            }?.awaitAll()
        }

        savedMovies.await()
        savedShows.await()

        sessionManager.resetDataStore()

        _loading.value = false
    }

}