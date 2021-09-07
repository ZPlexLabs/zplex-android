package zechs.zplex.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import zechs.zplex.models.tvdb.SeriesResponse

interface TvdbAPI {

    @GET("series/{series_id}")
    suspend fun getSeries(
        @Path("series_id")
        series_id: Int,
    ): Response<SeriesResponse>

}