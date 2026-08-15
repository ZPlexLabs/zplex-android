package zechs.zplex.zplex_api.data.repository

import zechs.zplex.common.utils.Result
import zechs.zplex.common.utils.SafeApiCaller
import zechs.zplex.zplex_api.data.remote.api.playlist.PlaylistApi
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistDetailResponse
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistItemRequest
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistNameRequest
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistReorderRequest
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistResponse
import javax.inject.Inject

class PlaylistRepository @Inject constructor(
    private val api: PlaylistApi,
    private val safeApiCaller: SafeApiCaller
) {

    suspend fun playlists(): Result<List<PlaylistResponse>> =
        safeApiCaller.call { api.playlists() }

    suspend fun createPlaylist(name: String): Result<PlaylistResponse> =
        safeApiCaller.call { api.createPlaylist(PlaylistNameRequest(name)) }

    suspend fun playlist(playlistId: Long): Result<PlaylistDetailResponse> =
        safeApiCaller.call { api.playlist(playlistId) }

    suspend fun renamePlaylist(playlistId: Long, name: String): Result<Unit> =
        safeApiCaller.callUnit { api.renamePlaylist(playlistId, PlaylistNameRequest(name)) }

    suspend fun deletePlaylist(playlistId: Long): Result<Unit> =
        safeApiCaller.callUnit { api.deletePlaylist(playlistId) }

    suspend fun addItem(playlistId: Long, request: PlaylistItemRequest): Result<Unit> =
        safeApiCaller.callUnit { api.addItem(playlistId, request) }

    suspend fun removeItem(playlistId: Long, itemId: Long): Result<Unit> =
        safeApiCaller.callUnit { api.removeItem(playlistId, itemId) }

    suspend fun reorderItems(playlistId: Long, itemIds: List<Long>): Result<Unit> =
        safeApiCaller.callUnit { api.reorderItems(playlistId, PlaylistReorderRequest(itemIds)) }
}
