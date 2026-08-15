package zechs.zplex.zplex_api.data.local.downloads

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    fun observeById(id: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getById(id: String): DownloadEntity?

    @Upsert
    suspend fun upsert(entity: DownloadEntity)

    @Query("UPDATE downloads SET downloadedBytes = :downloaded, totalBytes = :total, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProgress(id: String, downloaded: Long, total: Long, updatedAt: Long)

    @Query("UPDATE downloads SET status = :status, errorMessage = :error, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: DownloadStatus, error: String?, updatedAt: Long)

    @Query("UPDATE downloads SET status = :status, filePath = :filePath, downloadedBytes = :downloaded, totalBytes = :total, errorMessage = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markCompleted(id: String, status: DownloadStatus, filePath: String, downloaded: Long, total: Long, updatedAt: Long)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun delete(id: String)
}
