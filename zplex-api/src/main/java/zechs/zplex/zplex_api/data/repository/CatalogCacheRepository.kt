package zechs.zplex.zplex_api.data.repository

import kotlinx.coroutines.flow.Flow
import zechs.zplex.common.connectivity.ConnectivityObserver
import zechs.zplex.common.connectivity.ConnectivityStatus
import zechs.zplex.common.utils.CacheResource
import zechs.zplex.common.utils.networkBoundResource
import zechs.zplex.zplex_api.data.local.cache.CatalogCacheDao
import zechs.zplex.zplex_api.data.local.cache.CatalogItemEntity
import zechs.zplex.zplex_api.data.remote.api.movies.LatestMovie
import zechs.zplex.zplex_api.data.remote.api.tvshows.LatestTvShow
import javax.inject.Inject

class CatalogCacheRepository @Inject constructor(
    private val dao: CatalogCacheDao,
    private val moviesRepository: MoviesRepository,
    private val tvShowsRepository: TvShowsRepository,
    private val connectivity: ConnectivityObserver
) {

    fun latestMovies(): Flow<CacheResource<List<CatalogItemEntity>>> = networkBoundResource(
        query = { dao.observe(KEY_LATEST_MOVIES) },
        fetch = { moviesRepository.latestShows() },
        saveFetchResult = { remote ->
            dao.replace(KEY_LATEST_MOVIES, remote.mapIndexed { index, item -> item.toEntity(KEY_LATEST_MOVIES, index) })
        },
        shouldFetch = ::shouldFetch
    )

    fun latestShows(): Flow<CacheResource<List<CatalogItemEntity>>> = networkBoundResource(
        query = { dao.observe(KEY_LATEST_SHOWS) },
        fetch = { tvShowsRepository.latestShows() },
        saveFetchResult = { remote ->
            dao.replace(KEY_LATEST_SHOWS, remote.mapIndexed { index, item -> item.toEntity(KEY_LATEST_SHOWS, index) })
        },
        shouldFetch = ::shouldFetch
    )

    private fun shouldFetch(cached: List<CatalogItemEntity>): Boolean {
        if (connectivity.currentStatus() != ConnectivityStatus.Available) return false
        val newest = cached.maxOfOrNull { it.cachedAt } ?: return true
        return System.currentTimeMillis() - newest > STALE_AFTER_MS
    }

    private fun LatestMovie.toEntity(key: String, position: Int) = CatalogItemEntity(
        cacheKey = key,
        tmdbId = tmdbId,
        title = title,
        posterPath = posterPath,
        backdropPath = backdropPath,
        imdbRating = null,
        release = release,
        position = position,
        cachedAt = System.currentTimeMillis()
    )

    private fun LatestTvShow.toEntity(key: String, position: Int) = CatalogItemEntity(
        cacheKey = key,
        tmdbId = tmdbId,
        title = title,
        posterPath = posterPath,
        backdropPath = backdropPath,
        imdbRating = null,
        release = release,
        position = position,
        cachedAt = System.currentTimeMillis()
    )

    private companion object {
        const val KEY_LATEST_MOVIES = "latest_movies"
        const val KEY_LATEST_SHOWS = "latest_shows"
        const val STALE_AFTER_MS = 15 * 60 * 1000L
    }
}
