package zechs.zplex.data.remote


import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import zechs.zplex.data.model.omdb.OmdbMovieResponse
import zechs.zplex.data.model.omdb.OmdbTvResponse

interface OmdbApi {

    @GET("/")
    suspend fun fetchMovieById(
        @Query("i") tmdbId: String,
        @Query("plot") plot: String = "full",
    ): Response<OmdbMovieResponse>

    @GET("/")
    suspend fun fetchTvById(
        @Query("i") tmdbId: String,
        @Query("plot") plot: String = "full",
    ): Response<OmdbTvResponse>

}