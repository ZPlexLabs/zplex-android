package zechs.zplex.data.local.offline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import zechs.zplex.data.model.offline.OfflineSeason

@Dao
interface OfflineSeasonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSeason(offlineSeason: OfflineSeason): Long

    @Query("SELECT * FROM seasons WHERE tmdbId = :tmdbId")
    fun getAllSeasons(tmdbId: Int): List<OfflineSeason>

    @Query(
        "SELECT EXISTS(SELECT * FROM seasons WHERE tmdbId = :tmdbId AND seasonNumber = :seasonNumber)"
    )
    fun getSeason(tmdbId: Int, seasonNumber: Int): Boolean

    @Query("SELECT * FROM seasons WHERE tmdbId = :tmdbId  AND seasonNumber = :seasonNumber LIMIT 1")
    suspend fun getSeasonById(tmdbId: Int, seasonNumber: Int): OfflineSeason?

    @Query("DELETE FROM seasons WHERE tmdbId = :tmdbId  AND seasonNumber = :seasonNumber")
    suspend fun deleteSeason(tmdbId: Int, seasonNumber: Int)
}