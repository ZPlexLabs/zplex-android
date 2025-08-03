package zechs.zplex.data.local.api_cache

import androidx.room.Database
import androidx.room.RoomDatabase
import zechs.zplex.data.model.ApiCache

@Database(
    entities = [ApiCache::class],
    version = 1,
    exportSchema = false
)
abstract class ApiCacheDatabase : RoomDatabase() {
    abstract fun getApiCacheDao(): ApiCacheDao
}