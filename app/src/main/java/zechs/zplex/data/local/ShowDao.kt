package zechs.zplex.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import zechs.zplex.data.model.entities.Show

@Dao
interface ShowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertShow(media: Show): Long

    @Query("SELECT * FROM shows ORDER BY id DESC")
    fun getAllShows(): LiveData<List<Show>>

    @Query("SELECT EXISTS(SELECT * FROM shows WHERE id = :id)")
    fun getShow(id: Int): LiveData<Boolean>

    @Query("SELECT * FROM shows WHERE id = :id LIMIT 1")
    suspend fun getShowById(id: Int): Show?

    @Query("DELETE FROM shows WHERE id = :id")
    suspend fun deleteShowById(id: Int)
}