package zechs.zplex.feature_movies.detail

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import zechs.zplex.common.ui.mvi.MviViewModel
import zechs.zplex.common.ui.mvi.toUiError
import zechs.zplex.common.utils.Result
import zechs.zplex.common.utils.TmdbImage
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.remote.api.me.model.ContinueWatchingItem
import zechs.zplex.zplex_api.data.remote.api.me.model.PlayedRequest
import zechs.zplex.zplex_api.data.remote.api.me.model.PlayedResponse
import zechs.zplex.zplex_api.data.remote.api.me.model.WatchlistRequest
import zechs.zplex.zplex_api.data.remote.api.movies.MovieDetails
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistItemRequest
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistResponse
import zechs.zplex.zplex_api.data.remote.api.tvshows.TvShowDetails
import zechs.zplex.zplex_api.data.repository.MeRepository
import zechs.zplex.zplex_api.data.repository.MoviesRepository
import zechs.zplex.zplex_api.data.repository.PlaylistRepository
import zechs.zplex.zplex_api.data.repository.TvShowsRepository
import javax.inject.Inject

@HiltViewModel
class MediaDetailViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository,
    private val tvShowsRepository: TvShowsRepository,
    private val meRepository: MeRepository,
    private val playlistRepository: PlaylistRepository
) : MviViewModel<DetailState, DetailAction, DetailEvent>(DetailState()) {

    private var loadedKey: Pair<MediaType, Int>? = null

    fun load(mediaType: MediaType, tmdbId: Int) {
        if (loadedKey == mediaType to tmdbId) return
        loadedKey = mediaType to tmdbId
        setState { DetailState(isLoading = true, mediaType = mediaType, tmdbId = tmdbId) }
        viewModelScope.launch { loadInternal(mediaType, tmdbId) }
    }

    override fun onAction(action: DetailAction) {
        when (action) {
            DetailAction.Play -> play()
            DetailAction.ToggleWatchlist -> toggleWatchlist()
            DetailAction.TogglePlayed -> togglePlayed()
            DetailAction.OpenTrailer -> currentState.trailerUrl
                ?.let { sendEvent(DetailEvent.OpenUrl(it)) }
                ?: sendEvent(DetailEvent.ShowMessage("No trailer available"))

            DetailAction.Download -> sendEvent(DetailEvent.ShowMessage("Downloads arrive in a later milestone"))
            DetailAction.ShowPlaylistPicker -> setState { copy(showPlaylistPicker = true) }
            DetailAction.DismissPlaylistPicker -> setState { copy(showPlaylistPicker = false) }
            is DetailAction.AddToPlaylist -> addToPlaylist(action.playlistId)
            is DetailAction.CreatePlaylistAndAdd -> createPlaylistAndAdd(action.name)
            is DetailAction.SelectSeason -> selectSeason(action.seasonId)
            is DetailAction.PlayEpisode -> action.row.episode.fileId
                ?.let { sendEvent(DetailEvent.NavigateToPlayer(it)) }
                ?: sendEvent(DetailEvent.ShowMessage("Episode not available"))

            is DetailAction.ToggleEpisodePlayed -> toggleEpisodePlayed(action.row)
            DetailAction.Retry -> reload()
        }
    }

    private fun reload() {
        val mediaType = currentState.mediaType
        val tmdbId = currentState.tmdbId
        loadedKey = null
        load(mediaType, tmdbId)
    }

    private suspend fun loadInternal(mediaType: MediaType, tmdbId: Int) = coroutineScope {
        val watchlistD = async { meRepository.watchlist() }
        val playedD = async { meRepository.played() }
        val continueD = async { meRepository.continueWatching() }
        val playlistsD = async { playlistRepository.playlists() }

        val inWatchlist = watchlistD.await().dataOrEmpty()
            .any { it.tmdbId == tmdbId && it.mediaType == mediaType }
        val played = playedD.await().dataOrEmpty()
        val continueItem = continueD.await().dataOrEmpty()
            .firstOrNull { it.tmdbId == tmdbId && it.mediaType == mediaType }
        val playlists = playlistsD.await().dataOrEmpty()

        when (mediaType) {
            MediaType.MOVIE -> loadMovie(tmdbId, inWatchlist, played, continueItem, playlists)
            MediaType.SHOW -> loadShow(tmdbId, inWatchlist, playlists)
        }
    }

    private suspend fun loadMovie(
        tmdbId: Int,
        inWatchlist: Boolean,
        played: List<PlayedResponse>,
        continueItem: ContinueWatchingItem?,
        playlists: List<PlaylistResponse>
    ) {
        when (val result = moviesRepository.movieDetails(tmdbId)) {
            is Result.Success -> {
                val d = result.data
                val resume = continueItem?.takeIf { it.progressMs > 0 && d.fileId != null }?.let {
                    ResumeInfo(d.fileId!!, it.progressMs, it.durationMs, "Resume")
                }
                setState {
                    copy(
                        isLoading = false,
                        error = null,
                        header = DetailHeader(
                            title = d.title,
                            logoUrl = TmdbImage.logo(d.logoPath),
                            backdropUrl = TmdbImage.backdrop(d.backdropPath),
                            posterUrl = TmdbImage.poster(d.posterPath),
                            meta = movieMeta(d)
                        ),
                        overview = d.plot,
                        tagline = d.tagline,
                        director = d.director,
                        genres = d.genres?.map { it.name }.orEmpty(),
                        studios = d.studios?.mapNotNull { it.name }.orEmpty(),
                        collections = d.collections?.map { it.name }.orEmpty(),
                        cast = d.casts.orEmpty(),
                        crew = d.crews.orEmpty(),
                        trailerUrl = d.trailerLink,
                        resume = resume,
                        playableFileId = d.fileId,
                        inWatchlist = inWatchlist,
                        isPlayed = played.any { it.tmdbId == tmdbId && it.mediaType == MediaType.MOVIE },
                        playlists = playlists
                    )
                }
            }

            is Result.Error -> setState { copy(isLoading = false, error = result.toUiError()) }
        }
    }

    private suspend fun loadShow(
        tmdbId: Int,
        inWatchlist: Boolean,
        playlists: List<PlaylistResponse>
    ) {
        when (val result = tvShowsRepository.tvShowDetails(tmdbId)) {
            is Result.Success -> {
                val d = result.data
                setState {
                    copy(
                        isLoading = false,
                        error = null,
                        header = DetailHeader(
                            title = d.title,
                            logoUrl = TmdbImage.logo(d.logoPath),
                            backdropUrl = TmdbImage.backdrop(d.backdropPath),
                            posterUrl = TmdbImage.poster(d.posterPath),
                            meta = showMeta(d)
                        ),
                        overview = d.plot,
                        director = d.director,
                        genres = d.genres?.map { it.name }.orEmpty(),
                        studios = d.studios?.mapNotNull { it.name }.orEmpty(),
                        cast = d.casts.orEmpty(),
                        crew = d.crews.orEmpty(),
                        trailerUrl = d.trailerLink,
                        inWatchlist = inWatchlist,
                        playlists = playlists
                    )
                }
            }

            is Result.Error -> setState { copy(isLoading = false, error = result.toUiError()) }
        }
    }

    private fun play() {
        val fileId = currentState.resume?.fileId ?: currentState.playableFileId
        if (fileId != null) sendEvent(DetailEvent.NavigateToPlayer(fileId))
        else sendEvent(DetailEvent.ShowMessage("This title is not available to play"))
    }

    private fun toggleWatchlist() {
        val target = !currentState.inWatchlist
        val mediaType = currentState.mediaType
        val tmdbId = currentState.tmdbId
        setState { copy(inWatchlist = target) }
        viewModelScope.launch {
            val result = if (target) {
                meRepository.addToWatchlist(WatchlistRequest(mediaType, tmdbId))
            } else {
                meRepository.removeFromWatchlist(mediaType, tmdbId)
            }
            if (result is Result.Error) {
                setState { copy(inWatchlist = !target) }
                sendEvent(DetailEvent.ShowMessage(result.message))
            }
        }
    }

    private fun togglePlayed() {
        val target = !currentState.isPlayed
        val mediaType = currentState.mediaType
        val tmdbId = currentState.tmdbId
        setState { copy(isPlayed = target) }
        viewModelScope.launch {
            val result = if (target) {
                meRepository.markPlayed(PlayedRequest(mediaType, tmdbId, null, null))
            } else {
                meRepository.unmarkPlayed(mediaType, tmdbId)
            }
            if (result is Result.Error) {
                setState { copy(isPlayed = !target) }
                sendEvent(DetailEvent.ShowMessage(result.message))
            }
        }
    }

    private fun addToPlaylist(playlistId: Long) {
        val mediaType = currentState.mediaType
        val tmdbId = currentState.tmdbId
        setState { copy(showPlaylistPicker = false) }
        viewModelScope.launch {
            val result = playlistRepository.addItem(playlistId, PlaylistItemRequest(mediaType, tmdbId))
            sendEvent(
                DetailEvent.ShowMessage(
                    if (result is Result.Success) "Added to playlist"
                    else (result as Result.Error).message
                )
            )
        }
    }

    private fun createPlaylistAndAdd(name: String) {
        val mediaType = currentState.mediaType
        val tmdbId = currentState.tmdbId
        setState { copy(showPlaylistPicker = false) }
        viewModelScope.launch {
            when (val created = playlistRepository.createPlaylist(name)) {
                is Result.Success -> {
                    playlistRepository.addItem(created.data.id, PlaylistItemRequest(mediaType, tmdbId))
                    playlistRepository.playlists().let { refreshed ->
                        if (refreshed is Result.Success) setState { copy(playlists = refreshed.data) }
                    }
                    sendEvent(DetailEvent.ShowMessage("Added to ${created.data.name}"))
                }

                is Result.Error -> sendEvent(DetailEvent.ShowMessage(created.message))
            }
        }
    }

    // Season/episode handling is completed in the show-detail task.
    private fun selectSeason(seasonId: Int) = Unit
    private fun toggleEpisodePlayed(row: EpisodeRow) = Unit

    private fun movieMeta(d: MovieDetails): List<String> = buildList {
        d.releaseYear?.let { add(it.toString()) }
        d.runtime?.let { add(runtimeText(it.toInt())) }
        d.imdbRating?.takeIf { it.isNotBlank() }?.let { add("★ $it") }
        d.parentalRating?.takeIf { it.isNotBlank() }?.let { add(it) }
    }

    private fun showMeta(d: TvShowDetails): List<String> = buildList {
        d.release?.takeIf { it.isNotBlank() }?.let { add(it) }
        d.imdbRating?.takeIf { it.isNotBlank() }?.let { add("★ $it") }
        d.parentalRating?.takeIf { it.isNotBlank() }?.let { add(it) }
    }
}

internal fun runtimeText(minutes: Int): String {
    if (minutes <= 0) return ""
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }
}

private fun <T> Result<List<T>>.dataOrEmpty(): List<T> =
    (this as? Result.Success)?.data ?: emptyList()
