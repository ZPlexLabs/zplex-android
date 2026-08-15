package zechs.zplex.zplex_api.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import zechs.zplex.zplex_api.data.download.DownloadRequest
import zechs.zplex.zplex_api.data.download.MediaDownloadWorker
import zechs.zplex.zplex_api.data.local.downloads.DownloadDao
import zechs.zplex.zplex_api.data.local.downloads.DownloadEntity
import zechs.zplex.zplex_api.data.local.downloads.DownloadStatus
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    private val downloadDao: DownloadDao,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
) {

    fun observeDownloads(): Flow<List<DownloadEntity>> = downloadDao.observeAll()

    fun observeDownload(id: String): Flow<DownloadEntity?> = downloadDao.observeById(id)

    suspend fun enqueue(request: DownloadRequest) {
        val now = System.currentTimeMillis()
        val existing = downloadDao.getById(request.id)
        downloadDao.upsert(
            DownloadEntity(
                id = request.id,
                fileId = request.fileId,
                tmdbId = request.tmdbId,
                mediaType = request.mediaType,
                title = request.title,
                subtitle = request.subtitle,
                seasonNumber = request.seasonNumber,
                episodeNumber = request.episodeNumber,
                posterPath = request.posterPath,
                status = DownloadStatus.QUEUED,
                downloadedBytes = existing?.downloadedBytes ?: 0,
                totalBytes = existing?.totalBytes ?: 0,
                filePath = existing?.filePath,
                errorMessage = null,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
        enqueueWork(request.id, ExistingWorkPolicy.KEEP)
    }

    suspend fun pause(id: String) {
        downloadDao.updateStatus(id, DownloadStatus.PAUSED, null, System.currentTimeMillis())
        workManager.cancelUniqueWork(id)
    }

    suspend fun resume(id: String) {
        downloadDao.updateStatus(id, DownloadStatus.QUEUED, null, System.currentTimeMillis())
        enqueueWork(id, ExistingWorkPolicy.REPLACE)
    }

    suspend fun cancel(id: String) {
        workManager.cancelUniqueWork(id)
        deleteFiles(id)
        downloadDao.delete(id)
    }

    suspend fun delete(id: String) = cancel(id)

    private fun enqueueWork(id: String, policy: ExistingWorkPolicy) {
        val request = OneTimeWorkRequestBuilder<MediaDownloadWorker>()
            .setInputData(workDataOf(MediaDownloadWorker.KEY_DOWNLOAD_ID to id))
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .addTag(id)
            .build()
        workManager.enqueueUniqueWork(id, policy, request)
    }

    private fun deleteFiles(id: String) {
        val dir = File(context.filesDir, MediaDownloadWorker.DOWNLOADS_DIR)
        File(dir, id).delete()
        File(dir, "$id.part").delete()
    }
}
