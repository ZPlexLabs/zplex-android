package zechs.zplex.zplex_api.data.remote.api.stream

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import zechs.zplex.zplex_api.data.remote.api.stream.model.StreamGrantResponse

interface StreamApi {

    @GET("/api/stream/grant/{fileId}")
    suspend fun getStreamGrant(
        @Path("fileId") fileId: String
    ): Response<StreamGrantResponse>
}
