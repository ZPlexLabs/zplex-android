package zechs.zplex.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
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

    @Query("SELECT * FROM `watched_shows` WHERE tmdbId = :tmdbId AND seasonNumber = :season ORDER BY createdAt DESC LIMIT 1")
    fun getLastWatchedEpisode(tmdbId: Int, season: Int): Flow<WatchedShow?>

    @Query(
        "SELECT * FROM `watched_shows` " +
                "WHERE tmdbId = :tmdbId " +
                "AND seasonNumber = :season"
    )
    fun getWatchedSeasonAsFlow(tmdbId: Int, season: Int): Flow<List<WatchedShow>>

    @Query(
        "SELECT * FROM `watched_shows` " +
                "WHERE tmdbId = :tmdbId " +
                "AND seasonNumber = :season"
    )
    suspend fun getWatchedSeason(tmdbId: Int, season: Int): List<WatchedShow>

    @Query("DELETE FROM `watched_shows` WHERE tmdbId = :tmdbId")
    suspend fun deleteWatchedShow(tmdbId: Int)

}