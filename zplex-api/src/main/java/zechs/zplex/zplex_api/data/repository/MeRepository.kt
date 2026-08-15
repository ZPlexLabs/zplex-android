package zechs.zplex.zplex_api.data.repository

import zechs.zplex.common.utils.Result
import zechs.zplex.common.utils.SafeApiCaller
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.remote.api.me.MeApi
import zechs.zplex.zplex_api.data.remote.api.me.model.ContinueWatchingItem
import zechs.zplex.zplex_api.data.remote.api.me.model.PlayedRequest
import zechs.zplex.zplex_api.data.remote.api.me.model.PlayedResponse
import zechs.zplex.zplex_api.data.remote.api.me.model.ProgressUpdateRequest
import zechs.zplex.zplex_api.data.remote.api.me.model.WatchlistItemResponse
import zechs.zplex.zplex_api.data.remote.api.me.model.WatchlistRequest
import javax.inject.Inject

class MeRepository @Inject constructor(
    private val api: MeApi,
    private val safeApiCaller: SafeApiCaller
) {

    suspend fun updateProgress(request: ProgressUpdateRequest): Result<Unit> =
        safeApiCaller.callUnit { api.updateProgress(request) }

    suspend fun continueWatching(): Result<List<ContinueWatchingItem>> =
        safeApiCaller.call { api.continueWatching() }

    suspend fun history(): Result<List<ContinueWatchingItem>> =
        safeApiCaller.call { api.history() }

    suspend fun dismissContinueWatching(id: Long): Result<Unit> =
        safeApiCaller.callUnit { api.dismissContinueWatching(id) }

    suspend fun watchlist(): Result<List<WatchlistItemResponse>> =
        safeApiCaller.call { api.watchlist() }

    suspend fun addToWatchlist(request: WatchlistRequest): Result<Unit> =
        safeApiCaller.callUnit { api.addToWatchlist(request) }

    suspend fun removeFromWatchlist(mediaType: MediaType, tmdbId: Int): Result<Unit> =
        safeApiCaller.callUnit { api.removeFromWatchlist(mediaType, tmdbId) }

    suspend fun played(): Result<List<PlayedResponse>> =
        safeApiCaller.call { api.played() }

    suspend fun markPlayed(request: PlayedRequest): Result<Unit> =
        safeApiCaller.callUnit { api.markPlayed(request) }

    suspend fun unmarkPlayed(
        mediaType: MediaType,
        tmdbId: Int,
        seasonNumber: Int = 0,
        episodeNumber: Int = 0
    ): Result<Unit> =
        safeApiCaller.callUnit { api.unmarkPlayed(mediaType, tmdbId, seasonNumber, episodeNumber) }
}
