package zechs.zplex.api.interfaces

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import zechs.zplex.models.tmdb.credits.CreditsResponse
import zechs.zplex.models.tmdb.movies.MoviesResponse
import zechs.zplex.utils.Constants.TMDB_API_KEY

interface TmdbAPI {

    @GET("3/movie/{movies_id}")
    suspend fun getMovie(
        @Path("movies_id")
        movies_id: Int,
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US"
    ): Response<MoviesResponse>

    @GET("3/movie/{movies_id}/credits")
    suspend fun getCredits(
        @Path("movies_id")
        movies_id: Int,
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US"
    ): Response<CreditsResponse>

}