package zechs.zplex.db

import androidx.lifecycle.LiveData
import androidx.room.*
import zechs.zplex.models.tmdb.entities.Media

@Dao
interface WatchlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMedia(media: Media): Long

    @Query("SELECT * FROM watchlist")
    fun getAllMedia(): LiveData<List<Media>>

    @Query("SELECT EXISTS(SELECT * FROM watchlist WHERE id = :id)")
    fun getMedia(id: Int): LiveData<Boolean>

    @Delete
    suspend fun deleteMedia(media: Media)

}