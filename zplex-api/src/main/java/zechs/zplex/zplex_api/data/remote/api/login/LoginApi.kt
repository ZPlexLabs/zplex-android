package zechs.zplex.zplex_api.data.remote.api.login

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import zechs.zplex.zplex_api.data.remote.annotation.NoAuth
import zechs.zplex.zplex_api.data.remote.api.login.model.LoginRequest
import zechs.zplex.zplex_api.data.remote.api.login.model.LoginSuccessResponse
import zechs.zplex.zplex_api.data.remote.api.login.model.SignupRequest

interface LoginApi {

    @POST("/api/auth/login")
    @NoAuth
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginSuccessResponse>

    @POST("/api/auth/signup")
    @NoAuth
    suspend fun signup(
        @Body signupRequest: SignupRequest
    ): Response<Unit>

}
