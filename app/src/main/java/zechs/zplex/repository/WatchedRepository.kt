package zechs.zplex.repository

import zechs.zplex.db.WatchlistDatabase
import zechs.zplex.models.dataclass.WatchedMovie
import zechs.zplex.models.dataclass.WatchedShow

class WatchedRepository(
    private val db: WatchlistDatabase
) {

    suspend fun upsertWatchedMovie(
        WatchedMovie: WatchedMovie
    ) = db.getWatchedMovieDao().upsertWatchedMovie(WatchedMovie)

    suspend fun deleteWatchedMovie(
        WatchedMovie: WatchedMovie
    ) = db.getWatchedMovieDao().deleteWatchedMovie(WatchedMovie)

    suspend fun getWatchedMovie(tmdbId: Int) = db.getWatchedMovieDao().getWatchedMovie(tmdbId)

    fun getAllWatchedMovies() = db.getWatchedMovieDao().getAllWatchedMovies()


    suspend fun upsertWatchedShow(
        WatchedShow: WatchedShow
    ) = db.getWatchedShowDao().upsertWatchedShow(WatchedShow)

    suspend fun deleteWatchedShow(
        WatchedShow: WatchedShow
    ) = db.getWatchedShowDao().deleteWatchedShow(WatchedShow)

    suspend fun getWatchedShow(tmdbId: Int) = db.getWatchedShowDao().getWatchedShow(tmdbId)

    fun getAllWatchedShows() = db.getWatchedShowDao().getAllWatchedShows()

}
