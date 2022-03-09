package zechs.zplex.api.interfaces

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import zechs.zplex.models.enum.MediaType
import zechs.zplex.models.tmdb.collection.CollectionsResponse
import zechs.zplex.models.tmdb.credit.CreditResponse
import zechs.zplex.models.tmdb.credit.PersonResponse
import zechs.zplex.models.tmdb.entities.Episode
import zechs.zplex.models.tmdb.media.MovieResponse
import zechs.zplex.models.tmdb.media.TvResponse
import zechs.zplex.models.tmdb.search.SearchResponse
import zechs.zplex.models.tmdb.season.SeasonResponse
import zechs.zplex.utils.Constants.TMDB_API_KEY

interface TmdbAPI {

    @GET("3/tv/{tv_id}")
    suspend fun getShow(
        @Path("tv_id")
        tv_id: Int,
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US",
        @Query("append_to_response")
        append_to_response: String = "credits,recommendations,videos"
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

    @GET("3/tv/{tv_id}/season/{season_number}/episode/{episode_number}")
    suspend fun getEpisode(
        @Path("tv_id")
        movies_id: Int,
        @Path("season_number")
        season_number: Int,
        @Path("episode_number")
        episode_number: Int,
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US",
    ): Response<Episode>

    @GET("3/search/multi")
    suspend fun getSearch(
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US",
        @Query("query")
        query: String,
        @Query("page")
        page: Int = 1,
        @Query("include_adult")
        include_adult: Boolean = true
    ): Response<SearchResponse>

    @GET("3/movie/{movie_id}")
    suspend fun getMovie(
        @Path("movie_id")
        movie_id: Int,
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US",
        @Query("append_to_response")
        append_to_response: String = "credits,recommendations,videos"
    ): Response<MovieResponse>

    @GET("3/collection/{collection_id}")
    suspend fun getCollection(
        @Path("collection_id")
        collection_id: Int,
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US"
    ): Response<CollectionsResponse>

    @GET("3/person/{person_id}")
    suspend fun getPeople(
        @Path("person_id")
        person_id: Int,
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US"
    ): Response<PersonResponse>

    @GET("3/credit/{credit_id}")
    suspend fun getCredit(
        @Path("credit_id")
        credit_id: String,
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US"
    ): Response<CreditResponse>

    @GET("3/trending/all/{time_window}")
    suspend fun getTrending(
        @Path("time_window")
        time_window: String,
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US"
    ): Response<SearchResponse>

    @GET("3/discover/{media_type}")
    suspend fun getDiscover(
        @Path("media_type")
        media_type: String,
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US",
        @Query("sort_by")
        sort_by: String,
        @Query("page")
        page: Int,
        @Query("with_keywords")
        with_keywords: Int?,
        @Query("with_genres")
        with_genres: Int?,
        @Query("first_air_date_year")
        first_air_date_year: Int?,
    ): Response<SearchResponse>

    @GET("3/discover/movie")
    suspend fun getInTheatres(
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("region")
        region: String = "US",
        @Query("with_release_type")
        sort_by: String = "3|2",
    ): Response<SearchResponse>

    @GET("3/tv/on_the_air")
    suspend fun getStreaming(
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US",
        @Query("page")
        page: Int = 1,
    ): Response<SearchResponse>

    @GET("3/discover/{media_type}")
    suspend fun getBrowse(
        @Path("media_type")
        media_type: MediaType,
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US",
        @Query("sort_by")
        sort_by: String,
        @Query("page")
        page: Int,
        @Query("with_keywords")
        with_keywords: Int?,
        @Query("with_genres")
        with_genres: Int?,
        @Query("include_adult")
        include_adult: Boolean = false
    ): Response<SearchResponse>


    @GET("3/movie/upcoming")
    suspend fun getUpcoming(
        @Query("api_key")
        api_key: String = TMDB_API_KEY,
        @Query("language")
        language: String = "en-US",
        @Query("page")
        page: Int,
    ): Response<SearchResponse>

}