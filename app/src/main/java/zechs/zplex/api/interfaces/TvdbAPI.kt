package zechs.zplex.api.interfaces

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import zechs.zplex.models.tvdb.actors.ActorsResponse
import zechs.zplex.models.tvdb.series.SeriesResponse

interface TvdbAPI {

    @GET("series/{series_id}")
    suspend fun getSeries(
        @Path("series_id")
        series_id: Int,
    ): Response<SeriesResponse>

    @GET("series/{series_id}/actors")
    suspend fun getActors(
        @Path("series_id")
        series_id: Int,
    ): Response<ActorsResponse>

}