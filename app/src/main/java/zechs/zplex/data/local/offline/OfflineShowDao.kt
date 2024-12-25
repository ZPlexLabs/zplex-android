package zechs.zplex.data.local.offline

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import zechs.zplex.data.model.offline.OfflineShow

@Dao
interface OfflineShowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertShow(offlineShow: OfflineShow): Long

    @Query("SELECT * FROM shows")
    fun getAllShows(): LiveData<List<OfflineShow>>

    @Query("SELECT EXISTS(SELECT * FROM shows WHERE id = :id)")
    fun getShow(id: Int): Boolean

    @Query("SELECT * FROM shows WHERE id = :id LIMIT 1")
    suspend fun getShowById(id: Int): OfflineShow?

    @Query("DELETE FROM shows WHERE id = :id")
    suspend fun deleteShowById(id: Int)
}