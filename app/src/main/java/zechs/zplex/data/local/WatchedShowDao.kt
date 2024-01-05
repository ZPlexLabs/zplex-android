package zechs.zplex.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
                "AND seasonNumber = :season " +
                "AND episodeNumber = :episode " +
                "LIMIT 1"
    )
    suspend fun getWatchedShow(tmdbId: Int, season: Int, episode: Int): WatchedShow?

    @Query("DELETE FROM `watched_shows` WHERE tmdbId = :tmdbId")
    suspend fun deleteWatchedShow(tmdbId: Int)

}