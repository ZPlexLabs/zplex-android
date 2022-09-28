package zechs.zplex.data.local


import androidx.lifecycle.LiveData
import androidx.room.*
import zechs.zplex.data.model.entities.WatchedShow

@Dao
interface WatchedShowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchedShow(watchedShow: WatchedShow): Long

    @Query("SELECT * FROM `watched_shows`")
    fun getAllWatchedShows(): LiveData<List<WatchedShow>>

    @Query(
        "SELECT * FROM `watched_shows` " +
                "WHERE tmdbId = :tmdbId " +
                "LIMIT 1"
    )
    suspend fun getWatchedShow(tmdbId: Int): WatchedShow?

    @Delete
    suspend fun deleteWatchedShow(watchedShow: WatchedShow)

}