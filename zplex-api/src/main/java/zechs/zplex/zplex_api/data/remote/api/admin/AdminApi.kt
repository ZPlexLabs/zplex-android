package zechs.zplex.zplex_api.data.remote.api.admin

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import zechs.zplex.zplex_api.data.remote.api.admin.model.BlacklistRequest
import zechs.zplex.zplex_api.data.remote.api.admin.model.UpdateCapabilityRequest
import zechs.zplex.zplex_api.data.remote.api.admin.model.UserAccessRequest
import zechs.zplex.zplex_api.data.remote.api.admin.model.UserSummaryResponse
import zechs.zplex.zplex_api.data.remote.api.enums.MediaType

interface AdminApi {

    @GET("/api/auth/admin/users")
    suspend fun users(): Response<List<UserSummaryResponse>>

    @PUT("/api/auth/admin/users/{username}/capabilities")
    suspend fun updateCapabilities(
        @Path("username") username: String,
        @Body request: UpdateCapabilityRequest
    ): Response<Unit>

    @DELETE("/api/auth/admin/users/{username}")
    suspend fun deleteUser(@Path("username") username: String): Response<Unit>

    @PUT("/api/auth/admin/users/{username}/access")
    suspend fun updateAccess(
        @Path("username") username: String,
        @Body request: UserAccessRequest
    ): Response<Unit>

    @POST("/api/auth/admin/users/{username}/blacklist")
    suspend fun addBlacklist(
        @Path("username") username: String,
        @Body request: BlacklistRequest
    ): Response<Unit>

    @DELETE("/api/auth/admin/users/{username}/blacklist/{mediaType}/{tmdbId}")
    suspend fun removeBlacklist(
        @Path("username") username: String,
        @Path("mediaType") mediaType: MediaType,
        @Path("tmdbId") tmdbId: Int
    ): Response<Unit>
}
