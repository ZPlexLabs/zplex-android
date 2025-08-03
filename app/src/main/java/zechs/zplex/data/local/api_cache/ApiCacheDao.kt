package zechs.zplex.data.local.api_cache

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import zechs.zplex.data.model.ApiCache

@Dao
interface ApiCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCache(cache: ApiCache)

    @Query("SELECT * FROM api_caches WHERE id = :id LIMIT 1")
    suspend fun getCacheById(id: String): ApiCache?

    @Query("DELETE FROM api_caches WHERE id = :id")
    suspend fun deleteCacheById(id: String)

    @Query("DELETE FROM api_caches WHERE expiration < :currentTime")
    suspend fun deleteExpiredCache(currentTime: Long)

    @Query("SELECT COUNT(id) FROM api_caches")
    fun getCacheCountLiveData(): LiveData<Int>

    @Query("DELETE FROM api_caches")
    suspend fun resetCache()

}