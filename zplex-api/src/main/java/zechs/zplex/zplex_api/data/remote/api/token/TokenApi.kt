package zechs.zplex.zplex_api.data.remote.api.token

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import zechs.zplex.zplex_api.data.remote.annotation.NoAuth
import zechs.zplex.zplex_api.data.remote.api.token.model.RefreshRequest
import zechs.zplex.zplex_api.data.remote.api.token.model.RefreshResponse

interface TokenApi {

    @NoAuth
    @POST("/api/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequest
    ): Response<RefreshResponse>


}