package zechs.zplex.zplex_api.data.remote.api.tvshows

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import zechs.zplex.zplex_api.data.local.PaginatedResponse
import zechs.zplex.zplex_api.data.remote.api.MediaListItem
import zechs.zplex.zplex_api.data.remote.api.enums.OrderBy
import zechs.zplex.zplex_api.data.remote.api.enums.SortBy

interface TvShowApi {

    @GET("/api/tvshows")
    suspend fun tvShows(
        @Query("sortBy") sortBy: SortBy? = null,
        @Query("orderBy") orderBy: OrderBy? = null,
        @Query("filterBy") filterBy: String? = null,
        @Query("pageNumber") pageNumber: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("includeNull") includeNull: Boolean? = true
    ): Response<PaginatedResponse<MediaListItem>>

    @GET("/api/tvshows/latest")
    suspend fun tvShowsLatest(): Response<List<LatestTvShow>>

    @GET("/api/tvshows/{tmdbId}")
    suspend fun tvShowDetails(
        @Path("tmdbId") tmdbId: Int
    ): Response<TvShowDetails>

    @GET("/api/tvshows/{tmdbId}/seasons")
    suspend fun seasons(
        @Path("tmdbId") tmdbId: Int
    ): Response<List<Season>>

    @GET("/api/tvshows/{tmdbId}/seasons/{seasonId}")
    suspend fun episodes(
        @Path("tmdbId") tmdbId: Int,
        @Path("seasonId") seasonId: Int
    ): Response<List<Episode>>
}