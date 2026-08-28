package zechs.zplex.zplex_api.data.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import zechs.zplex.zplex_api.data.local.config.ConfigStorage
import zechs.zplex.zplex_api.data.local.downloads.DownloadDao
import zechs.zplex.zplex_api.data.local.downloads.DownloadStatus
import zechs.zplex.zplex_api.data.repository.StreamRepository
import java.io.File
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Named

class MediaDownloadWorkerFactory @Inject constructor(
    private val downloadDao: DownloadDao,
    private val streamRepository: StreamRepository,
    private val configStorage: ConfigStorage,
    @param:Named("download_client") private val client: OkHttpClient
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker =
        MediaDownloadWorker(
            appContext, workerParameters, downloadDao, streamRepository, configStorage, client
        )
}

class MediaDownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val downloadDao: DownloadDao,
    private val streamRepository: StreamRepository,
    private val configStorage: ConfigStorage,
    private val client: OkHttpClient
) : CoroutineWorker(context, workerParams) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        val id = inputData.getString(KEY_DOWNLOAD_ID) ?: return Result.failure()
        val entity = downloadDao.getById(id) ?: return Result.failure()

        createNotificationChannel()
        setForeground(foregroundInfo(id, entity.title, 0, false))
        downloadDao.updateStatus(id, DownloadStatus.RUNNING, null, now())

        val host = configStorage.getConfig()?.streamingHost
            ?: return failWith(id, "Streaming host unavailable")

        return try {
            val file = downloadToFile(id, entity.fileId, entity.title, host)
            downloadDao.markCompleted(
                id, DownloadStatus.COMPLETED, file.path, file.length(), file.length(), now()
            )
            Result.success()
        } catch (stop: WorkerStopped) {
            downloadDao.updateStatus(id, DownloadStatus.PAUSED, null, now())
            Result.failure()
        } catch (e: Exception) {
            downloadDao.updateStatus(id, DownloadStatus.FAILED, e.message ?: "Download failed", now())
            Result.failure()
        }
    }

    private suspend fun downloadToFile(
        id: String,
        fileId: String,
        title: String,
        host: String
    ): File = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, DOWNLOADS_DIR).apply { mkdirs() }
        val finalFile = File(dir, id)
        if (finalFile.exists()) return@withContext finalFile

        val partFile = File(dir, "$id.part")
        var existing = if (partFile.exists()) partFile.length() else 0L

        var grant = fetchGrant(fileId, host)
        var response = execute(host, fileId, grant, existing)
        if (response.code == 401) {
            response.close()
            grant = fetchGrant(fileId, host)
            response = execute(host, fileId, grant, existing)
        }

        response.use { res ->
            if (!res.isSuccessful) throw IllegalStateException("HTTP ${res.code}")
            // Server ignored the Range request and sent the whole file.
            if (res.code == 200 && existing > 0) {
                partFile.delete()
                existing = 0
            }
            val body = res.body ?: throw IllegalStateException("Empty response body")
            val total = totalBytes(res.header("Content-Range"), existing, body.contentLength())

            RandomAccessFile(partFile, "rw").use { out ->
                out.seek(existing)
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var downloaded = existing
                var lastUpdate = 0L
                body.byteStream().use { input ->
                    var read = input.read(buffer)
                    while (read >= 0) {
                        if (isStopped) throw WorkerStopped()
                        out.write(buffer, 0, read)
                        downloaded += read
                        val ms = System.currentTimeMillis()
                        if (ms - lastUpdate >= PROGRESS_INTERVAL_MS) {
                            lastUpdate = ms
                            downloadDao.updateProgress(id, downloaded, total, now())
                            val percent = if (total > 0) ((downloaded * 100) / total).toInt() else 0
                            setForeground(foregroundInfo(id, title, percent, total <= 0))
                        }
                        read = input.read(buffer)
                    }
                }
            }
        }

        if (!partFile.renameTo(finalFile)) {
            partFile.copyTo(finalFile, overwrite = true)
            partFile.delete()
        }
        finalFile
    }

    private suspend fun fetchGrant(fileId: String, host: String): String =
        when (val result = streamRepository.getStreamUrl(fileId, host)) {
            is zechs.zplex.common.utils.Result.Success -> result.data.grantToken
            is zechs.zplex.common.utils.Result.Error -> throw IllegalStateException(result.message)
        }

    private fun execute(host: String, fileId: String, grant: String, offset: Long) =
        client.newCall(
            Request.Builder()
                .url("https://zplex-stream.zechs.workers.dev/api/stream/$fileId")
                .header("authorization", "Bearer $grant")
                .apply { if (offset > 0) header("Range", "bytes=$offset-") }
                .build()
        ).execute()

    private fun totalBytes(contentRange: String?, existing: Long, contentLength: Long): Long {
        contentRange?.substringAfter('/', "")?.toLongOrNull()?.let { return it }
        return if (contentLength > 0) existing + contentLength else -1
    }

    private suspend fun failWith(id: String, message: String): Result {
        downloadDao.updateStatus(id, DownloadStatus.FAILED, message, now())
        return Result.failure()
    }

    private fun foregroundInfo(
        id: String,
        title: String,
        percent: Int,
        indeterminate: Boolean
    ): ForegroundInfo {
        val cancel = controlIntent(DownloadControlReceiver.ACTION_CANCEL, id)
        val pause = controlIntent(DownloadControlReceiver.ACTION_PAUSE, id)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(if (indeterminate) "Downloading…" else "Downloading… $percent%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, percent, indeterminate)
            .addAction(android.R.drawable.ic_media_pause, "Pause", pause)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancel)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(id.hashCode(), notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(id.hashCode(), notification)
        }
    }

    private fun controlIntent(action: String, id: String): PendingIntent {
        val intent = Intent(context, DownloadControlReceiver::class.java).apply {
            this.action = action
            putExtra(DownloadControlReceiver.EXTRA_DOWNLOAD_ID, id)
        }
        return PendingIntent.getBroadcast(
            context,
            (action + id).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun now() = System.currentTimeMillis()

    private class WorkerStopped : Exception()

    companion object {
        const val KEY_DOWNLOAD_ID = "download_id"
        const val DOWNLOADS_DIR = "zplex-downloads"
        private const val CHANNEL_ID = "downloads"
        private const val PROGRESS_INTERVAL_MS = 1000L
    }
}
