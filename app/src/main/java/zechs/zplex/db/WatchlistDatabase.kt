package zechs.zplex.db

import androidx.room.Database
import androidx.room.RoomDatabase
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show
import zechs.zplex.models.dataclass.WatchedMovie
import zechs.zplex.models.dataclass.WatchedShow


@Database(
    entities = [Movie::class, Show::class, WatchedMovie::class, WatchedShow::class],
    version = 4,
    exportSchema = false
)

abstract class WatchlistDatabase : RoomDatabase() {

    abstract fun getMovieDao(): MovieDao
    abstract fun getShowDao(): ShowDao
    abstract fun getWatchedMovieDao(): WatchedMovieDao
    abstract fun getWatchedShowDao(): WatchedShowDao

}