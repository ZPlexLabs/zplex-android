package zechs.zplex.zplex_api.data.local.cache

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogCacheDao {

    @Query("SELECT * FROM catalog_items WHERE cacheKey = :cacheKey ORDER BY position ASC")
    fun observe(cacheKey: String): Flow<List<CatalogItemEntity>>

    @Query("SELECT MAX(cachedAt) FROM catalog_items WHERE cacheKey = :cacheKey")
    suspend fun lastCachedAt(cacheKey: String): Long?

    @Upsert
    suspend fun upsertAll(items: List<CatalogItemEntity>)

    @Query("DELETE FROM catalog_items WHERE cacheKey = :cacheKey")
    suspend fun clear(cacheKey: String)

    @Transaction
    suspend fun replace(cacheKey: String, items: List<CatalogItemEntity>) {
        clear(cacheKey)
        upsertAll(items)
    }
}
