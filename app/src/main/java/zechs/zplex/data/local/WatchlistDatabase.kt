package zechs.zplex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import zechs.zplex.data.model.entities.Movie
import zechs.zplex.data.model.entities.Show
import zechs.zplex.data.model.entities.WatchedMovie
import zechs.zplex.data.model.entities.WatchedShow


@Database(
    entities = [
        Movie::class,
        Show::class,
        WatchedMovie::class,
        WatchedShow::class
    ],
    version = 2,
    exportSchema = false
)

abstract class WatchlistDatabase : RoomDatabase() {

    abstract fun getMovieDao(): MovieDao
    abstract fun getShowDao(): ShowDao
    abstract fun getWatchedMovieDao(): WatchedMovieDao
    abstract fun getWatchedShowDao(): WatchedShowDao

}