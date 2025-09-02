package zechs.zplex.data.repository

import zechs.zplex.data.local.WatchedMovieDao
import zechs.zplex.data.local.WatchedShowDao
import zechs.zplex.data.model.entities.WatchedMovie
import zechs.zplex.data.model.entities.WatchedShow
import javax.inject.Inject


class WatchedRepository @Inject constructor(
    private val watchedShowDao: WatchedShowDao,
    private val watchedMovieDao: WatchedMovieDao
) {
    suspend fun upsertWatchedMovie(
        watchedMovie: WatchedMovie
    ) = watchedMovieDao.upsertWatchedMovie(watchedMovie)

    suspend fun deleteWatchedMovie(
        tmdbId: Int
    ) = watchedMovieDao.deleteWatchedMovie(tmdbId)

    suspend fun getWatchedMovie(
        tmdbId: Int
    ) = watchedMovieDao.getWatchedMovie(tmdbId)

    fun observeWatchedMovie(
        tmdbId: Int
    ) = watchedMovieDao.observeWatchedMovie(tmdbId)

    fun getAllWatchedMovies() = watchedMovieDao.getAllWatchedMovies()

    suspend fun upsertWatchedShow(
        watchedShow: WatchedShow
    ) = watchedShowDao.upsertWatchedShow(watchedShow)

    suspend fun deleteWatchedShow(
        tmdbId: Int
    ) = watchedShowDao.deleteWatchedShow(tmdbId)

    suspend fun getWatchedShow(
        tmdbId: Int,
        season: Int,
        episode: Int
    ) = watchedShowDao.getWatchedShow(tmdbId, season, episode)

    fun getLastWatchedEpisode(
        tmdbId: Int,
        seasonNumber: Int
    ) = watchedShowDao.getLastWatchedEpisode(tmdbId, seasonNumber)

    suspend fun getWatchedSeason(
        tmdbId: Int,
        season: Int
    ) = watchedShowDao.getWatchedSeason(tmdbId, season)


    fun getWatchedSeasonAsFlow(
        tmdbId: Int,
        season: Int
    ) = watchedShowDao.getWatchedSeasonAsFlow(tmdbId, season)

    fun getAllWatchedShows() = watchedShowDao.getAllWatchedShows()

}
