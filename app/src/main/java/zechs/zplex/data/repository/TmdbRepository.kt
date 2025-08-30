package zechs.zplex.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import zechs.zplex.data.local.MovieDao
import zechs.zplex.data.local.ShowDao
import zechs.zplex.data.local.api_cache.ApiCacheDao
import zechs.zplex.data.model.ApiCache
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.Order
import zechs.zplex.data.model.SortBy
import zechs.zplex.data.model.entities.Movie
import zechs.zplex.data.model.entities.Show
import zechs.zplex.data.model.tmdb.keyword.TmdbKeyword
import zechs.zplex.data.model.tmdb.media.MovieResponse
import zechs.zplex.data.model.tmdb.media.TvResponse
import zechs.zplex.data.model.tmdb.search.SearchResponse
import zechs.zplex.data.model.tmdb.season.SeasonResponse
import zechs.zplex.data.remote.OmdbApi
import zechs.zplex.data.remote.TmdbApi
import zechs.zplex.utils.ext.nullIfNAOrElse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbRepository @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val omdbApi: OmdbApi,
    private val movieDao: MovieDao,
    private val showDao: ShowDao,
    private val apiCacheDao: ApiCacheDao,
    private val gson: Gson
) {

    companion object {
        private const val TAG = "TmdbRepository"
    }

    suspend fun upsertMovie(
        movie: Movie
    ) = movieDao.upsertMovie(movie)

    suspend fun fetchMovieById(
        id: Int
    ) = movieDao.getMovieById(id)

    fun fetchMovie(
        id: Int
    ) = movieDao.getMovie(id)

    suspend fun deleteMovie(
        tmdbId: Int
    ) = movieDao.deleteMovieById(tmdbId)

    fun getSavedMovies() = movieDao.getAllMovies()

    suspend fun upsertShow(
        show: Show
    ) = showDao.upsertShow(show)

    suspend fun fetchShowById(
        id: Int
    ) = showDao.getShowById(id)

    fun fetchShow(
        id: Int
    ) = showDao.getShow(id)

    suspend fun deleteShow(
        tmdbId: Int
    ) = showDao.deleteShowById(tmdbId)

    fun getSavedShows() = showDao.getAllShows()
    suspend fun getShow(
        tvId: Int,
        appendToQuery: String? = "credits,recommendations,videos,external_ids"
    ): Response<TvResponse> {
        val cacheKey = "show_${tvId}${if (appendToQuery != null) "_${appendToQuery}" else ""}"
        val existingCache = apiCacheDao.getCacheById(cacheKey)
        if (existingCache != null && existingCache.expiration >= System.currentTimeMillis()) {
            try {
                val parsed = parseCache<TvResponse>(existingCache.classType, existingCache.body)
                Log.d(TAG, "Retrieved cache successfully with key: $cacheKey")
                return Response.success(parsed)
            } catch (e: Exception) {
                Log.d(TAG, "Cache deserialization failed: ${e.message}")
            }
        }
        try {
            val tmdbResponse = tmdbApi.getShow(tvId, append_to_response = appendToQuery)
            var show = tmdbResponse.body()

            if (tmdbResponse.isSuccessful && show != null) {
                val expiration = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
                val imdbId = show.external_ids?.get("imdb_id")
                if (!imdbId.isNullOrBlank()) {
                    val omdbResponse = omdbApi.fetchTvById(imdbId)
                    if (omdbResponse.isSuccessful) {
                        val imdbRating = omdbResponse.body()
                            ?.imdbRating
                            ?.nullIfNAOrElse { it.toDoubleOrNull() }

                        if (imdbRating != null) {
                            show = show.copy(
                                vote_average = imdbRating,
                                isImdbRating = true
                            )
                        }
                    }
                }

                apiCacheDao.addCache(
                    ApiCache(
                        id = cacheKey,
                        body = gson.toJson(show),
                        classType = TvResponse::class.java.name,
                        expiration = expiration
                    )
                )
                Log.d(TAG, "Saved cache with key: $cacheKey")

                return Response.success(show)
            }
            return tmdbResponse

        } catch (e: Exception) {
            return Response.error(500, "Exception: ${e.message}".toResponseBody(null))
        }
    }


    suspend fun getMovie(
        movieId: Int,
        appendToQuery: String? = "credits,recommendations,videos"
    ): Response<MovieResponse> {
        val cacheKey = "movie_${movieId}${if (appendToQuery != null) "_${appendToQuery}" else ""}"
        val existingCache = apiCacheDao.getCacheById(cacheKey)
        if (existingCache != null && existingCache.expiration >= System.currentTimeMillis()) {
            try {
                val parsed = parseCache<MovieResponse>(existingCache.classType, existingCache.body)
                Log.d(TAG, "Retrieved cache successfully with key: $cacheKey")
                return Response.success(parsed)
            } catch (e: Exception) {
                Log.d(TAG, "Cache deserialization failed: ${e.message}")
            }
        }
        try {
            val tmdbResponse = tmdbApi.getMovie(movieId, append_to_response = appendToQuery)
            var movie = tmdbResponse.body()

            if (tmdbResponse.isSuccessful && movie != null) {
                val expiration = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L

                val imdbId = movie.imdb_id
                if (!imdbId.isNullOrBlank()) {
                    val omdbResponse = omdbApi.fetchMovieById(imdbId)
                    if (omdbResponse.isSuccessful) {
                        val imdbRating = omdbResponse.body()
                            ?.imdbRating
                            ?.nullIfNAOrElse { it.toDoubleOrNull() }

                        if (imdbRating != null) {
                            movie = movie.copy(
                                vote_average = imdbRating,
                                isImdbRating = true
                            )
                        }
                    }
                }

                apiCacheDao.addCache(
                    ApiCache(
                        id = cacheKey,
                        body = gson.toJson(movie),
                        classType = MovieResponse::class.java.name,
                        expiration = expiration
                    )
                )
                Log.d(TAG, "Saved cache with key: $cacheKey")
                return Response.success(movie)
            }
            return tmdbResponse
        } catch (e: Exception) {
            return Response.error(500, "Exception: ${e.message}".toResponseBody(null))
        }
    }

    suspend fun getSeason(
        tvId: Int,
        seasonNumber: Int
    ): Response<SeasonResponse> {
        val cacheKey = "${tvId}_season_${seasonNumber}"
        val existingCache = apiCacheDao.getCacheById(cacheKey)
        if (existingCache != null) {
            if (existingCache.expiration < System.currentTimeMillis()) {
                apiCacheDao.deleteCacheById(cacheKey)
                Log.d(TAG, "Deleting cache with key: $cacheKey")
            } else {
                try {
                    val parsed =
                        parseCache<SeasonResponse>(existingCache.classType, existingCache.body)
                    Log.d(TAG, "Retrieved cache successfully with key: $cacheKey")
                    return Response.success(parsed)
                } catch (e: Exception) {
                    Log.d(
                        TAG,
                        "Cache deserialization failed (cache will be deleted): " + e.message.toString()
                    )
                    apiCacheDao.deleteCacheById(cacheKey)
                }
            }
        }
        val season = tmdbApi.getSeason(tvId, seasonNumber)
        if (season.isSuccessful && season.body() != null) {
            val expiration = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
            Log.d(TAG, "Creating cache with key: $cacheKey")
            apiCacheDao.addCache(
                ApiCache(
                    id = cacheKey,
                    body = gson.toJson(season.body()),
                    classType = SeasonResponse::class.java.name,
                    expiration = expiration
                )
            )
        }
        return season
    }

    suspend fun getEpisode(
        tvId: Int,
        seasonNumber: Int,
        episodeNumber: Int
    ) = tmdbApi.getEpisode(tvId, seasonNumber, episodeNumber)

    suspend fun getSearch(
        query: String,
        page: Int
    ) = tmdbApi.getSearch(query = query, page = page)

    suspend fun getCollection(
        collectionId: Int
    ) = tmdbApi.getCollection(collection_id = collectionId)

    suspend fun getTrending(
        timeWindow: String
    ) = tmdbApi.getTrending(timeWindow)

    suspend fun getUpcoming(page: Int) = tmdbApi.getUpcoming(page = page)

    suspend fun getBrowse(
        mediaType: MediaType,
        sortBy: SortBy,
        order: Order,
        page: Int,
        withKeyword: List<TmdbKeyword>?,
        withGenres: Int?,
    ): Response<SearchResponse> {
        val keywords = try {
            withKeyword
                ?.map { keyword -> keyword.id }
                ?.joinToString(separator = ",")
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Unable to parse keywords")
            null
        }
        return tmdbApi.getBrowse(
            media_type = mediaType,
            sort_by = "${sortBy.name}.${order.name}",
            page = page,
            with_keywords = keywords,
            with_genres = withGenres,
        )
    }

    suspend fun getInTheatres(
        dateStart: String,
        dateEnd: String
    ) = tmdbApi.getInTheatres(
        release_date_start = dateStart,
        release_date_end = dateEnd
    )

    suspend fun getPopularOnStreaming() = tmdbApi.getPopularOnStreaming()

    suspend fun getShowsFromCompany(
        companyId: Int,
        page: Int
    ): Response<SearchResponse> {
        val cacheKey = "show_company_${companyId}_${page}"
        val existingCache = apiCacheDao.getCacheById(cacheKey)
        if (existingCache != null) {
            if (existingCache.expiration < System.currentTimeMillis()) {
                apiCacheDao.deleteCacheById(cacheKey)
                Log.d(TAG, "Deleting cache with key: $cacheKey")
            } else {
                try {
                    val parsed =
                        parseCache<SearchResponse>(existingCache.classType, existingCache.body)
                    Log.d(TAG, "Retrieved cache successfully with key: $cacheKey")
                    return Response.success(parsed)
                } catch (e: Exception) {
                    Log.d(
                        TAG,
                        "Cache deserialization failed (cache will be deleted): " + e.message.toString()
                    )
                    apiCacheDao.deleteCacheById(cacheKey)
                }
            }
        }
        val company = tmdbApi.getFromCompany(
            media_type = MediaType.tv,
            with_companies = companyId,
            page = page
        )
        if (company.isSuccessful && company.body() != null) {
            val expiration = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
            Log.d(TAG, "Creating cache with key: $cacheKey")
            apiCacheDao.addCache(
                ApiCache(
                    id = cacheKey,
                    body = gson.toJson(company.body()),
                    classType = SearchResponse::class.java.name,
                    expiration = expiration
                )
            )
        }
        return company
    }

    suspend fun getMoviesFromCompany(
        companyId: Int,
        page: Int
    ): Response<SearchResponse> {
        val cacheKey = "movie_company_${companyId}_${page}"
        val existingCache = apiCacheDao.getCacheById(cacheKey)
        if (existingCache != null) {
            if (existingCache.expiration < System.currentTimeMillis()) {
                apiCacheDao.deleteCacheById(cacheKey)
                Log.d(TAG, "Deleting cache with key: $cacheKey")
            } else {
                try {
                    val parsed =
                        parseCache<SearchResponse>(existingCache.classType, existingCache.body)
                    Log.d(TAG, "Retrieved cache successfully with key: $cacheKey")
                    return Response.success(parsed)
                } catch (e: Exception) {
                    Log.d(
                        TAG,
                        "Cache deserialization failed (cache will be deleted): " + e.message.toString()
                    )
                    apiCacheDao.deleteCacheById(cacheKey)
                }
            }
        }
        val company = tmdbApi.getFromCompany(
            media_type = MediaType.movie,
            with_companies = companyId,
            page = page
        )
        if (company.isSuccessful && company.body() != null) {
            val expiration = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
            Log.d(TAG, "Creating cache with key: $cacheKey")
            apiCacheDao.addCache(
                ApiCache(
                    id = cacheKey,
                    body = gson.toJson(company.body()),
                    classType = SearchResponse::class.java.name,
                    expiration = expiration
                )
            )
        }
        return company
    }

    suspend fun getPerson(
        personId: Int
    ) = tmdbApi.getPerson(personId)

    suspend fun searchKeyword(
        query: String
    ) = tmdbApi.searchKeyword(
        query = query
    )

    @Suppress("UNCHECKED_CAST")
    private fun <T> parseCache(classType: String, body: String): T {
        val clazz = Class.forName(classType)
        val type = TypeToken.get(clazz).type
        val parsed = gson.fromJson<Any>(body, type)

        return parsed as T
    }
}