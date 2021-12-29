package zechs.zplex.ui.fragment.myshows


import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show
import zechs.zplex.models.tmdb.entities.Media
import zechs.zplex.repository.TmdbRepository


class MyShowsViewModel(
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    fun deleteShow(show: Show) = viewModelScope.launch {
        tmdbRepository.deleteShow(show)
    }

    fun deleteMovie(movie: Movie) = viewModelScope.launch {
        tmdbRepository.deleteMovie(movie)
    }

    val movies = tmdbRepository.getSavedMovies()
    val shows = tmdbRepository.getSavedShows()

    val savedMedia = movies.combineWith(shows) { movie, show ->
        movie?.let { show?.let { it1 -> handleSavedMedia(it, it1) } }
    }

    private fun <T, K, R> LiveData<T>.combineWith(
        liveData: LiveData<K>,
        block: (T?, K?) -> R
    ): LiveData<R> {
        val result = MediatorLiveData<R>()
        result.addSource(this) {
            result.value = block(this.value, liveData.value)
        }
        result.addSource(liveData) {
            result.value = block(this.value, liveData.value)
        }
        return result
    }

    private fun handleSavedMedia(
        movies: List<Movie>,
        shows: List<Show>
    ): List<Media> {
        val movie: List<Media> = movies.map {
            Media(
                id = it.id,
                media_type = it.media_type,
                name = null,
                poster_path = it.poster_path,
                title = it.title,
                vote_average = null
            )
        }

        val show: List<Media> = shows.map {
            Media(
                id = it.id,
                media_type = it.media_type,
                name = it.name,
                poster_path = it.poster_path,
                title = null,
                vote_average = null
            )
        }
        return movie.plus(show).sortedBy { it.name }
    }

}