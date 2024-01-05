package zechs.zplex.ui.myshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.entities.Movie
import zechs.zplex.data.model.entities.Show
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.data.repository.TmdbRepository
import javax.inject.Inject

@HiltViewModel
class MyShowsViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    fun saveShow(show: Show) = viewModelScope.launch {
        tmdbRepository.upsertShow(show)
    }

    fun deleteShow(show: Show) = viewModelScope.launch {
        tmdbRepository.deleteShow(show)
    }

    fun saveMovie(movie: Movie) = viewModelScope.launch {
        tmdbRepository.upsertMovie(movie)
    }

    fun deleteMovie(movie: Movie) = viewModelScope.launch {
        tmdbRepository.deleteMovie(movie)
    }

    val movies = tmdbRepository.getSavedMovies()
    val shows = tmdbRepository.getSavedShows()

    private fun handleSavedMedia(
        movies: List<Movie>,
        shows: List<Show>
    ): List<Media> {
        val movie: List<Media> = movies.map {
            Media(
                id = it.id,
                media_type = MediaType.movie,
                name = null,
                poster_path = it.poster_path,
                title = it.title,
                vote_average = it.vote_average,
                backdrop_path = null,
                overview = null,
                release_date = null,
                first_air_date = null
            )
        }

        val show: List<Media> = shows.map {
            Media(
                id = it.id,
                media_type = MediaType.tv,
                name = it.name,
                poster_path = it.poster_path,
                title = null,
                vote_average = it.vote_average,
                backdrop_path = null,
                overview = null,
                release_date = null,
                first_air_date = null
            )
        }
        return movie.plus(show).sortedBy { it.id }
    }

}