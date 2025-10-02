package zechs.zplex.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import zechs.zplex.data.model.LoginRequest
import zechs.zplex.data.model.LoginSuccessResponse

interface ZPlexApi {

    @POST("/api/auth/login")
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginSuccessResponse>

}