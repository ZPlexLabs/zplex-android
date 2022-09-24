package zechs.zplex.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `watched` " +
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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS `watched`")
    }
}


val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `watched_movies` " +
                    "(`tmdbId` INTEGER NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`mediaType` TEXT NOT NULL, " +
                    "`posterPath` TEXT, " +
                    "`watchedDuration` INTEGER NOT NULL, " +
                    "`totalDuration` INTEGER NOT NULL, " +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT)"
        )

        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `watched_shows` " +
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