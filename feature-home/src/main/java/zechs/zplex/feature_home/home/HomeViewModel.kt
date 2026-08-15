package zechs.zplex.feature_home.home

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import zechs.zplex.common.ui.mvi.MviViewModel
import zechs.zplex.common.ui.mvi.UiError
import zechs.zplex.common.utils.CacheResource
import zechs.zplex.common.utils.Result
import zechs.zplex.common.utils.TmdbImage
import zechs.zplex.zplex_api.data.local.cache.CatalogItemEntity
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.remote.api.me.model.ContinueWatchingItem
import zechs.zplex.zplex_api.data.repository.CatalogCacheRepository
import zechs.zplex.zplex_api.data.repository.MeRepository
import zechs.zplex.zplex_api.data.repository.MoviesRepository
import zechs.zplex.zplex_api.data.repository.TvShowsRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val meRepository: MeRepository,
    private val moviesRepository: MoviesRepository,
    private val tvShowsRepository: TvShowsRepository,
    private val catalogCacheRepository: CatalogCacheRepository
) : MviViewModel<HomeState, HomeAction, HomeEvent>(HomeState()) {

    private val continueWatching = MutableStateFlow<List<ContinueWatchingCard>>(emptyList())
    private val refreshTrigger = MutableStateFlow(0L)

    init {
        observeRows()
        refresh()
    }

    override fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.Refresh -> refresh()
            is HomeAction.RemoveContinueWatching -> removeContinueWatching(action.id)
            is HomeAction.OpenDetail ->
                sendEvent(HomeEvent.NavigateToDetail(action.mediaType, action.tmdbId))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRows() {
        viewModelScope.launch {
            refreshTrigger.flatMapLatest {
                combine(
                    catalogCacheRepository.latestMovies(),
                    catalogCacheRepository.latestShows(),
                    continueWatching
                ) { movies, shows, cw -> Triple(movies, shows, cw) }
            }.collect { (movies, shows, cw) ->
                val rows = buildList {
                    if (cw.isNotEmpty()) add(HomeRow.ContinueWatching(cw))
                    latestRow("latest_movies", "Latest Movies", MediaType.MOVIE, movies)?.let(::add)
                    latestRow("latest_shows", "Latest Shows", MediaType.SHOW, shows)?.let(::add)
                }
                val stillLoading = rows.isEmpty() &&
                    (movies is CacheResource.Loading || shows is CacheResource.Loading)
                setState {
                    copy(rows = rows, isLoading = stillLoading, error = firstError(movies, shows))
                }
            }
        }
    }

    private fun refresh() {
        setState { copy(isRefreshing = true) }
        viewModelScope.launch {
            loadContinueWatching()
            refreshTrigger.value = System.currentTimeMillis()
            setState { copy(isRefreshing = false) }
        }
    }

    private fun removeContinueWatching(id: Long) {
        viewModelScope.launch {
            val previous = continueWatching.value
            continueWatching.value = previous.filterNot { it.id == id }
            val result = meRepository.dismissContinueWatching(id)
            if (result is Result.Error) {
                continueWatching.value = previous
                sendEvent(HomeEvent.ShowMessage(result.message))
            }
        }
    }

    private suspend fun loadContinueWatching() {
        when (val result = meRepository.continueWatching()) {
            is Result.Success -> continueWatching.value = coroutineScope {
                result.data.map { async { it.toCard() } }.awaitAll().filterNotNull()
            }
            is Result.Error -> Unit
        }
    }

    private suspend fun ContinueWatchingItem.toCard(): ContinueWatchingCard? {
        val progress =
            if (durationMs > 0L) (progressMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
        return when (mediaType) {
            MediaType.MOVIE -> {
                val details = moviesRepository.movieDetails(tmdbId)
                if (details !is Result.Success) return null
                ContinueWatchingCard(
                    id = id,
                    mediaType = mediaType,
                    tmdbId = tmdbId,
                    title = details.data.title,
                    subtitle = details.data.releaseYear?.toString().orEmpty(),
                    backdropUrl = TmdbImage.backdrop(details.data.backdropPath),
                    progress = progress
                )
            }
            MediaType.SHOW -> {
                val details = tvShowsRepository.tvShowDetails(tmdbId)
                if (details !is Result.Success) return null
                ContinueWatchingCard(
                    id = id,
                    mediaType = mediaType,
                    tmdbId = tmdbId,
                    title = details.data.title,
                    subtitle = "S%d · E%d".format(seasonNumber, episodeNumber),
                    backdropUrl = TmdbImage.backdrop(details.data.backdropPath),
                    progress = progress
                )
            }
        }
    }

    private fun latestRow(
        key: String,
        title: String,
        mediaType: MediaType,
        resource: CacheResource<List<CatalogItemEntity>>
    ): HomeRow.Latest? {
        val items = resource.dataOrNull().orEmpty()
        if (items.isEmpty()) return null
        return HomeRow.Latest(key, title, mediaType, items.map { it.toPoster(mediaType) })
    }

    private fun CatalogItemEntity.toPoster(mediaType: MediaType) = PosterCard(
        tmdbId = tmdbId,
        mediaType = mediaType,
        title = title,
        posterUrl = TmdbImage.poster(posterPath),
        backdropUrl = TmdbImage.backdrop(backdropPath),
        rating = imdbRating?.let { "%.1f".format(it) }
    )

    private fun <T> CacheResource<T>.dataOrNull(): T? = when (this) {
        is CacheResource.Loading -> data
        is CacheResource.Success -> data
        is CacheResource.Error -> data
    }

    private fun firstError(vararg resources: CacheResource<*>): UiError? = resources
        .filterIsInstance<CacheResource.Error<*>>()
        .firstOrNull { it.data == null }
        ?.let { UiError(it.message, it.details) }
}
