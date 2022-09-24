package zechs.zplex.repository

import zechs.zplex.db.WatchedMovieDao
import zechs.zplex.db.WatchedShowDao
import zechs.zplex.models.dataclass.WatchedMovie
import zechs.zplex.models.dataclass.WatchedShow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WatchedRepository @Inject constructor(
    private val watchedMovieDao: WatchedMovieDao,
    private val watchedShowDao: WatchedShowDao
) {
    suspend fun upsertWatchedMovie(
        WatchedMovie: WatchedMovie
    ) = watchedMovieDao.upsertWatchedMovie(WatchedMovie)

    suspend fun deleteWatchedMovie(
        WatchedMovie: WatchedMovie
    ) = watchedMovieDao.deleteWatchedMovie(WatchedMovie)

    suspend fun getWatchedMovie(
        tmdbId: Int
    ) = watchedMovieDao.getWatchedMovie(tmdbId)

    fun getAllWatchedMovies() = watchedMovieDao.getAllWatchedMovies()

    suspend fun upsertWatchedShow(
        WatchedShow: WatchedShow
    ) = watchedShowDao.upsertWatchedShow(WatchedShow)

    suspend fun deleteWatchedShow(
        WatchedShow: WatchedShow
    ) = watchedShowDao.deleteWatchedShow(WatchedShow)

    suspend fun getWatchedShow(
        tmdbId: Int
    ) = watchedShowDao.getWatchedShow(tmdbId)

    fun getAllWatchedShows() = watchedShowDao.getAllWatchedShows()

}
