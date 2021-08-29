package zechs.zplex.api

import retrofit2.Response
import retrofit2.http.GET
import zechs.zplex.models.witch.ReleasesResponse

interface WitchAPI {

    @GET("zplex/releases/")
    suspend fun getZPlexReleases(): Response<ReleasesResponse>
}