package zechs.zplex.zplex_api.data.remote.api.playlist

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistDetailResponse
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistItemRequest
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistNameRequest
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistReorderRequest
import zechs.zplex.zplex_api.data.remote.api.playlist.model.PlaylistResponse

interface PlaylistApi {

    @GET("/api/me/playlists")
    suspend fun playlists(): Response<List<PlaylistResponse>>

    @POST("/api/me/playlists")
    suspend fun createPlaylist(@Body request: PlaylistNameRequest): Response<PlaylistResponse>

    @GET("/api/me/playlists/{playlistId}")
    suspend fun playlist(@Path("playlistId") playlistId: Long): Response<PlaylistDetailResponse>

    @PUT("/api/me/playlists/{playlistId}")
    suspend fun renamePlaylist(
        @Path("playlistId") playlistId: Long,
        @Body request: PlaylistNameRequest
    ): Response<Unit>

    @DELETE("/api/me/playlists/{playlistId}")
    suspend fun deletePlaylist(@Path("playlistId") playlistId: Long): Response<Unit>

    @POST("/api/me/playlists/{playlistId}/items")
    suspend fun addItem(
        @Path("playlistId") playlistId: Long,
        @Body request: PlaylistItemRequest
    ): Response<Unit>

    @DELETE("/api/me/playlists/{playlistId}/items/{itemId}")
    suspend fun removeItem(
        @Path("playlistId") playlistId: Long,
        @Path("itemId") itemId: Long
    ): Response<Unit>

    @PUT("/api/me/playlists/{playlistId}/items/order")
    suspend fun reorderItems(
        @Path("playlistId") playlistId: Long,
        @Body request: PlaylistReorderRequest
    ): Response<Unit>
}
