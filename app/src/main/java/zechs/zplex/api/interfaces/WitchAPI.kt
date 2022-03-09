package zechs.zplex.api.interfaces

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import zechs.zplex.models.witch.DashVideoResponseItem
import zechs.zplex.models.witch.MessageResponse

interface WitchAPI {

    @GET("get_movie/{imdb_id}")
    suspend fun requestMovie(
        @Path("imdb_id")
        imdbId: String
    ): Response<MessageResponse>

    @GET("get_dash_video/{file_id}")
    suspend fun getDashVideos(
        @Path("file_id")
        fileId: String
    ): Response<List<DashVideoResponseItem>>

}