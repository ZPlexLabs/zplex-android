package zechs.zplex.feature_settings.history

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import zechs.zplex.common.ui.mvi.MviViewModel
import zechs.zplex.common.ui.mvi.toUiError
import zechs.zplex.common.utils.Result
import zechs.zplex.common.utils.TmdbImage
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.remote.api.me.model.ContinueWatchingItem
import zechs.zplex.zplex_api.data.repository.MeRepository
import zechs.zplex.zplex_api.data.repository.MoviesRepository
import zechs.zplex.zplex_api.data.repository.TvShowsRepository
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val meRepository: MeRepository,
    private val moviesRepository: MoviesRepository,
    private val tvShowsRepository: TvShowsRepository
) : MviViewModel<HistoryState, HistoryAction, HistoryEvent>(HistoryState()) {

    init {
        load()
    }

    override fun onAction(action: HistoryAction) {
        when (action) {
            HistoryAction.Retry -> load()
            is HistoryAction.Remove -> remove(action.id)
        }
    }

    private fun load() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = meRepository.history()) {
                is Result.Success -> {
                    val rows = coroutineScope {
                        result.data.map { async { it.toRow() } }.awaitAll().filterNotNull()
                    }
                    setState { copy(isLoading = false, items = rows) }
                }

                is Result.Error -> setState { copy(isLoading = false, error = result.toUiError()) }
            }
        }
    }

    private fun remove(id: Long) {
        val previous = currentState.items
        setState { copy(items = items.filterNot { it.id == id }) }
        viewModelScope.launch {
            val result = meRepository.dismissContinueWatching(id)
            if (result is Result.Error) {
                setState { copy(items = previous) }
            }
        }
    }

    private suspend fun ContinueWatchingItem.toRow(): HistoryRow? {
        val progress = if (durationMs > 0L) (progressMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
        return when (mediaType) {
            MediaType.MOVIE -> {
                val details = moviesRepository.movieDetails(tmdbId)
                if (details !is Result.Success) return null
                HistoryRow(
                    id = id,
                    mediaType = mediaType,
                    tmdbId = tmdbId,
                    title = details.data.title,
                    subtitle = details.data.releaseYear?.toString().orEmpty(),
                    posterUrl = TmdbImage.poster(details.data.posterPath),
                    progress = progress
                )
            }

            MediaType.SHOW -> {
                val details = tvShowsRepository.tvShowDetails(tmdbId)
                if (details !is Result.Success) return null
                HistoryRow(
                    id = id,
                    mediaType = mediaType,
                    tmdbId = tmdbId,
                    title = details.data.title,
                    subtitle = "S%d · E%d".format(seasonNumber, episodeNumber),
                    posterUrl = TmdbImage.poster(details.data.posterPath),
                    progress = progress
                )
            }
        }
    }
}
