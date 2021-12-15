package zechs.zplex.api.interfaces

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import zechs.zplex.models.tmdb.credits.CreditsResponse
import zechs.zplex.models.tmdb.movies.MoviesResponse
import zechs.zplex.models.tmdb.season.SeasonResponse
import zechs.zplex.models.tmdb.tv.TvResponse
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

    @GET("3/tv/{tv_id}")
    suspend fun getShow(
        @Path("tv_id")
        movies_id: Int,
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US",
        @Query("append_to_response")
        append_to_response: String = "credits"
    ): Response<TvResponse>

    @GET("3/tv/{tv_id}/season/{season_number}")
    suspend fun getSeason(
        @Path("tv_id")
        movies_id: Int,
        @Path("season_number")
        season_number: Int,
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US",
    ): Response<SeasonResponse>
}