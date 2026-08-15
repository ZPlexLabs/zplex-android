package zechs.zplex.zplex_api.data.remote.api.me

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType
import zechs.zplex.zplex_api.data.remote.api.me.model.ContinueWatchingItem
import zechs.zplex.zplex_api.data.remote.api.me.model.PlayedRequest
import zechs.zplex.zplex_api.data.remote.api.me.model.PlayedResponse
import zechs.zplex.zplex_api.data.remote.api.me.model.ProgressUpdateRequest
import zechs.zplex.zplex_api.data.remote.api.me.model.WatchlistItemResponse
import zechs.zplex.zplex_api.data.remote.api.me.model.WatchlistRequest

interface MeApi {

    @PUT("/api/me/progress")
    suspend fun updateProgress(@Body request: ProgressUpdateRequest): Response<Unit>

    @GET("/api/me/continue-watching")
    suspend fun continueWatching(): Response<List<ContinueWatchingItem>>

    @GET("/api/me/history")
    suspend fun history(): Response<List<ContinueWatchingItem>>

    @DELETE("/api/me/continue-watching/{id}")
    suspend fun dismissContinueWatching(@Path("id") id: Long): Response<Unit>

    @GET("/api/me/watchlist")
    suspend fun watchlist(): Response<List<WatchlistItemResponse>>

    @POST("/api/me/watchlist")
    suspend fun addToWatchlist(@Body request: WatchlistRequest): Response<Unit>

    @DELETE("/api/me/watchlist/{mediaType}/{tmdbId}")
    suspend fun removeFromWatchlist(
        @Path("mediaType") mediaType: MediaType,
        @Path("tmdbId") tmdbId: Int
    ): Response<Unit>

    @GET("/api/me/played")
    suspend fun played(): Response<List<PlayedResponse>>

    @POST("/api/me/played")
    suspend fun markPlayed(@Body request: PlayedRequest): Response<Unit>

    @DELETE("/api/me/played/{mediaType}/{tmdbId}")
    suspend fun unmarkPlayed(
        @Path("mediaType") mediaType: MediaType,
        @Path("tmdbId") tmdbId: Int,
        @Query("seasonNumber") seasonNumber: Int = 0,
        @Query("episodeNumber") episodeNumber: Int = 0
    ): Response<Unit>
}
