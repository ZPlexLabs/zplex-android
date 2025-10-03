package zechs.zplex.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import zechs.zplex.data.model.RefreshRequest
import zechs.zplex.data.model.RefreshResponse

interface ZPlexTokenApi {

    @POST("/api/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequest
    ): Response<RefreshResponse>

}