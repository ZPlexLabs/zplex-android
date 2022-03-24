package zechs.zplex.api.interfaces

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import zechs.zplex.models.witch.DashVideoResponseItem
import zechs.zplex.models.witch.MessageResponse

interface WitchAPI {

    @GET("yts_movie/")
    suspend fun requestMovie(
        @Query("imdb_id")
        imdbId: String,
        @Query("tmdb_id")
        tmdbId: String,
        @Query("device_id")
        deviceId: String
    ): Response<MessageResponse>

    @GET("get_dash_video/{file_id}")
    suspend fun getDashVideos(
        @Path("file_id")
        fileId: String
    ): Response<List<DashVideoResponseItem>?>

}