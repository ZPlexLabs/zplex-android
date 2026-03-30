package zechs.zplex.zplex_api.data.remote.api.config

import retrofit2.Response
import retrofit2.http.GET
import zechs.zplex.zplex_api.data.remote.api.config.model.Capability
import zechs.zplex.zplex_api.data.remote.api.config.model.ConfigResponse

interface ConfigApi {

    @GET("/api/config")
    suspend fun config(): Response<ConfigResponse>

    @GET("/api/config/capabilities")
    suspend fun capabilities(): Response<List<Capability>>

}