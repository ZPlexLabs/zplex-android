package zechs.zplex.api.interfaces

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import zechs.zplex.models.zplex.MovieResponse
import zechs.zplex.models.zplex.SeasonResponse


interface ZPlexAPI {

    @GET("api/v1/zplex/get_movie")
    suspend fun getMovie(
        @Query("tmdb_id")
        tmdb_id: Int
    ): Response<MovieResponse>

    @GET("api/v1/zplex/get_season")
    suspend fun getSeason(
        @Query("tmdb_id")
        tmdb_id: Int,
        @Query("season")
        season: Int
    ): Response<SeasonResponse>
}