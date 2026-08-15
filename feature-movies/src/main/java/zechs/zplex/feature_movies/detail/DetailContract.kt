package zechs.zplex.feature_movies.detail

import zechs.zplex.common.ui.mvi.UiAction
import zechs.zplex.common.player.PlayerArgs
import zechs.zplex.common.ui.mvi.UiError
import zechs.zplex.common.ui.mvi.UiEvent
import zechs.zplex.common.ui.mvi.UiState
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.remote.api.media.Cast
import zechs.zplex.zplex_api.data.remote.api.media.Crew
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistResponse
import zechs.zplex.zplex_api.data.remote.api.tvshows.Episode
import zechs.zplex.zplex_api.data.remote.api.tvshows.Season

data class DetailState(
    val isLoading: Boolean = true,
    val error: UiError? = null,
    val mediaType: MediaType = MediaType.MOVIE,
    val tmdbId: Int = 0,
    val header: DetailHeader? = null,
    val overview: String? = null,
    val tagline: String? = null,
    val director: String? = null,
    val genres: List<String> = emptyList(),
    val studios: List<String> = emptyList(),
    val collections: List<String> = emptyList(),
    val cast: List<Cast> = emptyList(),
    val crew: List<Crew> = emptyList(),
    val trailerUrl: String? = null,
    val resume: ResumeInfo? = null,
    val playableFileId: String? = null,
    val inWatchlist: Boolean = false,
    val isPlayed: Boolean = false,
    val accent: Int? = null,
    val playlists: List<PlaylistResponse> = emptyList(),
    val showPlaylistPicker: Boolean = false,
    // Show-specific
    val seasons: List<Season> = emptyList(),
    val selectedSeasonId: Int? = null,
    val episodes: List<EpisodeRow> = emptyList(),
    val episodesLoading: Boolean = false,
    val nextUp: EpisodeRow? = null
) : UiState

data class DetailHeader(
    val title: String,
    val logoUrl: String?,
    val backdropUrl: String?,
    val posterUrl: String?,
    val meta: List<String>
)

data class ResumeInfo(
    val fileId: String,
    val progressMs: Long,
    val durationMs: Long,
    val label: String
) {
    val fraction: Float
        get() = if (durationMs > 0) (progressMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

data class EpisodeRow(
    val episode: Episode,
    val played: Boolean,
    val progressMs: Long,
    val durationMs: Long
) {
    val fraction: Float
        get() = if (durationMs > 0) (progressMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

sealed interface DetailAction : UiAction {
    data object Play : DetailAction
    data object ToggleWatchlist : DetailAction
    data object TogglePlayed : DetailAction
    data object OpenTrailer : DetailAction
    data object Download : DetailAction
    data object ShowPlaylistPicker : DetailAction
    data object DismissPlaylistPicker : DetailAction
    data class AddToPlaylist(val playlistId: Long) : DetailAction
    data class CreatePlaylistAndAdd(val name: String) : DetailAction
    data class SelectSeason(val seasonId: Int) : DetailAction
    data class PlayEpisode(val row: EpisodeRow) : DetailAction
    data class ToggleEpisodePlayed(val row: EpisodeRow) : DetailAction
    data object Retry : DetailAction
}

sealed interface DetailEvent : UiEvent {
    data class NavigateToPlayer(val args: PlayerArgs) : DetailEvent
    data class OpenUrl(val url: String) : DetailEvent
    data class ShowMessage(val message: String) : DetailEvent
}
