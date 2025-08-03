package zechs.zplex.data.local


import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import zechs.zplex.data.model.entities.WatchedMovie

@Dao
interface WatchedMovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchedMovie(watchedMovie: WatchedMovie): Long

    @Query("SELECT * FROM `watched_movies`")
    fun getAllWatchedMovies(): LiveData<List<WatchedMovie>>

    @Query("SELECT * FROM `watched_movies` WHERE tmdbId = :tmdbId LIMIT 1")
    suspend fun getWatchedMovie(tmdbId: Int): WatchedMovie?

    @Query("SELECT * FROM `watched_movies` WHERE tmdbId = :tmdbId LIMIT 1")
    fun observeWatchedMovie(tmdbId: Int): LiveData<WatchedMovie?>

    @Query("DELETE FROM `watched_movies` WHERE tmdbId = :tmdbId")
    suspend fun deleteWatchedMovie(tmdbId: Int)

}