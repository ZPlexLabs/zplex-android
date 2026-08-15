package zechs.zplex.feature_player

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import zechs.zplex.common.player.PlayerItem
import zechs.zplex.common.utils.Result
import zechs.zplex.feature_player.data.PlayerPrefsStore
import zechs.zplex.zplex_api.data.local.config.ConfigStorage
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.remote.api.me.model.PlayedRequest
import zechs.zplex.zplex_api.data.remote.api.me.model.ProgressUpdateRequest
import zechs.zplex.zplex_api.data.repository.MeRepository
import zechs.zplex.zplex_api.data.repository.StreamRepository
import javax.inject.Inject

sealed interface StreamResult {
    data class Ready(val url: String, val grant: String) : StreamResult
    data class Failed(val message: String) : StreamResult
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val streamRepository: StreamRepository,
    private val configStorage: ConfigStorage,
    private val prefsStore: PlayerPrefsStore,
    private val meRepository: MeRepository
) : ViewModel() {

    suspend fun resolveStream(fileId: String): StreamResult {
        val host = configStorage.getConfig()?.streamingHost
            ?: return StreamResult.Failed("Streaming host is not configured")
        return when (val result = streamRepository.getStreamUrl(fileId, host)) {
            is Result.Success -> StreamResult.Ready(result.data.url, result.data.grantToken)
            is Result.Error -> StreamResult.Failed(result.message)
        }
    }

    suspend fun updateProgress(item: PlayerItem, progressMs: Long, durationMs: Long) {
        if (item.tmdbId <= 0 || durationMs <= 0L) return
        meRepository.updateProgress(
            ProgressUpdateRequest(
                mediaType = if (item.isTv) MediaType.SHOW else MediaType.MOVIE,
                tmdbId = item.tmdbId,
                seasonNumber = if (item.isTv) item.seasonNumber else null,
                episodeNumber = if (item.isTv) item.episodeNumber else null,
                progressMs = progressMs,
                durationMs = durationMs
            )
        )
    }

    suspend fun markPlayed(item: PlayerItem) {
        if (item.tmdbId <= 0) return
        meRepository.markPlayed(
            PlayedRequest(
                mediaType = if (item.isTv) MediaType.SHOW else MediaType.MOVIE,
                tmdbId = item.tmdbId,
                seasonNumber = if (item.isTv) item.seasonNumber else null,
                episodeNumber = if (item.isTv) item.episodeNumber else null
            )
        )
    }

    suspend fun preferredAudioLang(tmdbId: Int): String? = prefsStore.audioLang(tmdbId)

    suspend fun preferredSubLang(tmdbId: Int): String? = prefsStore.subLang(tmdbId)

    suspend fun saveTrackPrefs(tmdbId: Int, audioLang: String?, subLang: String?) =
        prefsStore.save(tmdbId, audioLang, subLang)
}
