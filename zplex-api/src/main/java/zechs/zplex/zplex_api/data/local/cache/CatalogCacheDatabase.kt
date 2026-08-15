package zechs.zplex.zplex_api.data.local.cache

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CatalogItemEntity::class], version = 1, exportSchema = false)
abstract class CatalogCacheDatabase : RoomDatabase() {
    abstract fun catalogCacheDao(): CatalogCacheDao

    companion object {
        const val NAME = "catalog-cache"
    }
}
