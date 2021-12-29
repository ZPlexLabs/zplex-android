package zechs.zplex.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import zechs.zplex.models.dataclass.Movie
import zechs.zplex.models.dataclass.Show


@Database(
    entities = [Movie::class, Show::class],
    version = 1,
    exportSchema = false
)

abstract class WatchlistDatabase : RoomDatabase() {

    abstract fun getMovieDao(): MovieDao
    abstract fun getShowDao(): ShowDao

    companion object {
        @Volatile
        private var instance: WatchlistDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                WatchlistDatabase::class.java,
                "watchlist_db.db"
            ).fallbackToDestructiveMigration().build()
    }
}