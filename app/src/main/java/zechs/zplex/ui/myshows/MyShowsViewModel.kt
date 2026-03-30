package zechs.zplex.ui.myshows

import android.app.Application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.utils.Pager
import zechs.zplex.utils.UiResult
import zechs.zplex.zplex_api.data.remote.api.MediaListItem
import zechs.zplex.zplex_api.data.repository.MoviesRepository
import zechs.zplex.zplex_api.data.repository.TvShowsRepository
import javax.inject.Inject

@HiltViewModel
class MyShowsViewModel @Inject constructor(
    app: Application,
    private val tvShowsRepository: TvShowsRepository,
    private val moviesRepository: MoviesRepository
) : BaseAndroidViewModel(app) {

    /* -------------------- TV SHOWS -------------------- */

    private val tvPager = Pager(
        scope = viewModelScope,
        request = { page ->
            tvShowsRepository.tvShows(pageNumber = page)
        }
    )

    val tvShows: StateFlow<UiResult<MediaListItem>> = tvPager.state

    fun loadTvShows(reset: Boolean = false) {
        tvPager.loadNext(reset)
    }

    fun retryTvShows() = tvPager.retry()

    fun refreshTvShows() = tvPager.refresh()


    /* -------------------- MOVIES -------------------- */

    private val moviePager = Pager(
        scope = viewModelScope,
        request = { page ->
            moviesRepository.movies(pageNumber = page)
        }
    )

    val movies: StateFlow<UiResult<MediaListItem>> = moviePager.state

    fun loadMovies(reset: Boolean = false) {
        moviePager.loadNext(reset)
    }

    fun retryMovies() = moviePager.retry()

    fun refreshMovies() = moviePager.refresh()
}