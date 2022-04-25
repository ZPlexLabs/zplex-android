package zechs.zplex.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
                "zplex_db.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()


        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE `watched` " +
                            "(`tmdbId` INTEGER NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`mediaType` TEXT, " +
                            "`posterPath` TEXT, " +
                            "`seasonNumber` INTEGER NOT NULL, " +
                            "`episodeNumber` INTEGER NOT NULL, " +
                            "`watchedDuration` INTEGER NOT NULL, " +
                            "`totalDuration` INTEGER NOT NULL, " +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE `watched`")
            }
        }


        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE `watched_movies` " +
                            "(`tmdbId` INTEGER NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`mediaType` TEXT NOT NULL, " +
                            "`posterPath` TEXT, " +
                            "`watchedDuration` INTEGER NOT NULL, " +
                            "`totalDuration` INTEGER NOT NULL, " +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT)"
                )

                database.execSQL(
                    "CREATE TABLE `watched_shows` " +
                            "(`tmdbId` INTEGER NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`mediaType` TEXT NOT NULL, " +
                            "`posterPath` TEXT, " +
                            "`seasonNumber` INTEGER NOT NULL, " +
                            "`episodeNumber` INTEGER NOT NULL, " +
                            "`watchedDuration` INTEGER NOT NULL, " +
                            "`totalDuration` INTEGER NOT NULL, " +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT)"
                )
            }
        }
    }
}