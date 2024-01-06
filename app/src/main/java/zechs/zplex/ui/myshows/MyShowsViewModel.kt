package zechs.zplex.ui.myshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import zechs.zplex.data.model.entities.Movie
import zechs.zplex.data.model.entities.Show
import zechs.zplex.data.repository.TmdbRepository
import javax.inject.Inject

@HiltViewModel
class MyShowsViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    fun saveShow(show: Show) = viewModelScope.launch {
        tmdbRepository.upsertShow(show)
    }

    fun deleteShow(tmdbId: Int) = viewModelScope.launch {
        tmdbRepository.deleteShow(tmdbId)
    }

    fun saveMovie(movie: Movie) = viewModelScope.launch {
        tmdbRepository.upsertMovie(movie)
    }

    fun deleteMovie(tmdbId: Int) = viewModelScope.launch {
        tmdbRepository.deleteMovie(tmdbId)
    }

    val movies = tmdbRepository.getSavedMovies()
    val shows = tmdbRepository.getSavedShows()

}