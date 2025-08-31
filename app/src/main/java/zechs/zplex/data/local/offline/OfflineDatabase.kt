package zechs.zplex.data.local.offline

import androidx.room.Database
import androidx.room.RoomDatabase
import zechs.zplex.data.model.offline.OfflineEpisode
import zechs.zplex.data.model.offline.OfflineMovie
import zechs.zplex.data.model.offline.OfflineSeason
import zechs.zplex.data.model.offline.OfflineShow


@Database(
    entities = [
        OfflineShow::class,
        OfflineMovie::class,
        OfflineSeason::class,
        OfflineEpisode::class
    ],
    version = 2,
    exportSchema = false
)

abstract class OfflineDatabase : RoomDatabase() {

    abstract fun getOfflineShowDao(): OfflineShowDao
    abstract fun getOfflineMovieDao(): OfflineMovieDao
    abstract fun getOfflineSeasonDao(): OfflineSeasonDao
    abstract fun getOfflineEpisodeDao(): OfflineEpisodeDao

}