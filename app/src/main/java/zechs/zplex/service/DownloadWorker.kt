package zechs.zplex.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okio.use
import zechs.zplex.R
import zechs.zplex.data.model.drive.DriveClient
import zechs.zplex.data.model.drive.FileResponse
import zechs.zplex.data.repository.DriveRepository
import zechs.zplex.utils.SessionManager
import zechs.zplex.utils.state.Resource
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Locale
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.pow
import kotlin.random.Random

class DownloadWorkerFactory @Inject constructor(
    private val driveRepository: DriveRepository,
    private val sessionManager: SessionManager
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker =
        DownloadWorker(appContext, workerParameters, driveRepository, sessionManager)
}


@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val driveRepository: DriveRepository,
    private val sessionManager: SessionManager
) : CoroutineWorker(context, workerParams) {

    private val notificationManager = applicationContext.getSystemService(
        Context.NOTIFICATION_SERVICE
    ) as NotificationManager

    companion object {
        const val DOWNLOADS_FOLDER_NAME = "zplex-downloads"
        const val TAG = "FileDownloadWorker"
        const val FILE_PATH = "filePath"
        const val NOTIFICATION_ID = "notificationId"
        const val FILE_ID = "fileId"
        const val FILE_TITLE = "fileTitle"
        const val TMDB_ID = "tmdbId"
        const val OFFLINE_DOWNLOADS_GROUP = "OFFLINE_DOWNLOADS_GROUP"
        const val SEASON_NUMBER = "seasonNumber"
        const val EPISODE_NUMBER = "episodeNumber"
        const val CHANNEL_ID = "download_channel"
        const val CHANNEL_NAME = "Downloads"

        fun getDownloadsFolderPath(context: Context): File {
            return File(context.filesDir, DOWNLOADS_FOLDER_NAME)
        }
    }

    override suspend fun doWork(): Result {
        val client = sessionManager.fetchClient() ?: run {
            Log.d(TAG, "Drive Client is required.")
            return Result.failure()
        }
        val fileId = inputData.getString(FILE_ID) ?: run {
            Log.d(TAG, "Download fileId is required.")
            return Result.failure()
        }
        val title = inputData.getString(FILE_TITLE) ?: run {
            Log.d(TAG, "Download title is required.")
            return Result.failure()
        }
        val seasonNumber = inputData.getInt(SEASON_NUMBER, 0)
        if (seasonNumber == 0) {
            Log.d(TAG, "Season number is required.")
            return Result.failure()
        }
        val tmdbId = inputData.getInt(TMDB_ID, 0)
        if (tmdbId == 0) {
            Log.d(TAG, "tmdbId is required.")
            return Result.failure()
        }
        val episodeNumber = inputData.getInt(EPISODE_NUMBER, 0)
        if (episodeNumber == 0) {
            Log.d(TAG, "episodeNumber is required.")
            return Result.failure()
        }

        ensureDownloadsFolder()
        createNotificationChannel()
        val notificationId = Random.nextInt(1, Integer.MAX_VALUE)

        val file = downloadFile(
            client = client,
            title = title,
            fileId = fileId,
            notificationId = notificationId
        )

        return if (file != null) {
            val outputData = workDataOf(
                FILE_TITLE to title,
                TMDB_ID to tmdbId,
                SEASON_NUMBER to seasonNumber,
                EPISODE_NUMBER to episodeNumber,
                NOTIFICATION_ID to notificationId,
                FILE_PATH to file.path
            )
            Result.success(outputData)
        } else {
            Result.failure()
        }
    }

    private fun calculateMD5(filePath: String): String {
        val md = MessageDigest.getInstance("MD5")
        val fileInputStream = FileInputStream(filePath)
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
            md.update(buffer, 0, bytesRead)
        }
        fileInputStream.close()
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    private suspend fun downloadFile(
        client: DriveClient,
        title: String,
        fileId: String,
        notificationId: Int,
    ): File? {
        try {
            if (getFilePath(fileId).exists()) {
                val remoteFile = getRemoteFile(fileId, client)
                if (remoteFile != null) {
                    val localFile = getFilePath(fileId)
                    val localChecksum = calculateMD5(localFile.absolutePath)
                    val remoteChecksum = remoteFile.md5Checksum
                    if (localChecksum == remoteChecksum) {
                        Log.d(TAG, "File already downloaded...")
                        return localFile
                    } else {
                        localFile.delete()
                    }
                }
//                Log.d(TAG, "Already exists. (File=$fileId, title=$title)")
//                showDownloadErrorNotification(notificationId, "Already exists", title)
//                return null
            }

            when (val token = driveRepository.fetchAccessToken(client)) {
                is Resource.Error -> {
                    Log.d(TAG, "Unable to fetch Access token")
                    return null
                }

                is Resource.Success -> {
                    val accessToken = token.data!!.accessToken
                    val response: ResponseBody = driveRepository.downloadFile(
                        fileId = fileId,
                        accessToken = accessToken
                    )
                    return withContext(Dispatchers.IO) {
                        writeDownload(
                            responseBody = response,
                            fileId = fileId,
                            notificationId = notificationId,
                            title = title
                        )
                    }
                }

                else -> {
                    Log.d(TAG, "Regression broke Download Worker")
                    return null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Something went wrong!", e)
            showDownloadErrorNotification(notificationId, "Download Failed", title)
            return null
        }
    }

    private suspend fun getRemoteFile(
        fileId: String,
        client: DriveClient
    ): FileResponse? {
        when (val token = driveRepository.fetchAccessToken(client)) {
            is Resource.Error -> {
                Log.d(TAG, "Unable to fetch Access token")
                return null
            }

            is Resource.Success -> {
                val accessToken = token.data!!.accessToken
                return driveRepository.getFile(
                    fileId = fileId,
                    accessToken = accessToken
                )
            }

            else -> {
                Log.d(TAG, "Regression broke Download Worker")
                return null
            }
        }
    }

    private fun showDownloadErrorNotification(
        notificationId: Int,
        title: String,
        content: String
    ) {
        if (!hasNotificationPermission()) {
            Log.d(TAG, "Notification permission denied.")
            return
        }
        Log.d(TAG, content)
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setOngoing(false)
            .setGroup(OFFLINE_DOWNLOADS_GROUP)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun writeDownload(
        responseBody: ResponseBody,
        fileId: String,
        notificationId: Int,
        title: String
    ): File {
        val file = File.createTempFile("drive_file_", ".tmp", getDownloadsFolderPath(context))
        val handler = Handler(Looper.getMainLooper())
        var isRunning = true
        var progressBytes = 0L
        var previousBytes = 0L
        val totalBytes = responseBody.contentLength()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val cancelIntent = Intent(applicationContext, DownloadActionReceiver::class.java)
            .apply {
                action = DownloadActionReceiver.CANCEL_ACTION
                putExtra(DownloadActionReceiver.DOWNLOAD_NAME, title)
                putExtra(DownloadActionReceiver.NOTIFICATION_ID, notificationId)
                putExtra(DownloadActionReceiver.DOWNLOAD_ID, fileId)
            }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val updateRunnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    val bytesChangedInInterval = progressBytes - previousBytes
                    val progress = ((progressBytes * 100) / totalBytes).toInt()
                    val remainingBytes = totalBytes - progressBytes
                    val remainingTimeInSeconds = if (bytesChangedInInterval > 0) {
                        (remainingBytes / bytesChangedInInterval).toInt()
                    } else -1

                    val remainingTimeFormatted = if (remainingTimeInSeconds >= 0) {
                        val minutes = remainingTimeInSeconds / 60
                        val seconds = remainingTimeInSeconds % 60
                        "${minutes}m ${seconds}s"
                    } else "Calculating..."

                    showProgressNotification(
                        notificationId = notificationId,
                        cancelIntent = cancelPendingIntent,
                        title = title,
                        downloaded = humanReadableSize(progressBytes),
                        total = humanReadableSize(totalBytes),
                        remainingTime = remainingTimeFormatted,
                        speed = humanReadableSpeed(bytesChangedInInterval),
                        progress = progress
                    )
                    previousBytes = progressBytes
                    handler.postDelayed(this, 1000L)
                }
            }
        }

        try {
            responseBody.byteStream().use { inputStream ->
                file.outputStream().use { outputStream ->
                    handler.post(updateRunnable)

                    var bytes = inputStream.read(buffer)
                    while (bytes >= 0) {
                        outputStream.write(buffer, 0, bytes)
                        progressBytes += bytes
                        bytes = inputStream.read(buffer)
                        if (isStopped) {
                            throw DownloadCancelled()
                        }
                    }
                    isRunning = false
                    handler.removeCallbacks(updateRunnable)
                }
            }

            // Rename temp file to final file
            val destinationFile = File(file.parentFile, fileId)
            file.renameTo(destinationFile)

            Log.d(TAG, "Write download completed (fileId=$fileId)")
            return destinationFile
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}")
            if (file.exists()) {
                Log.d(TAG, "Delete temp file: ${file.absolutePath}")
                file.delete()
            }
            throw e
        } finally {
            isRunning = false
            handler.removeCallbacks(updateRunnable)
        }
    }

    private fun showProgressNotification(
        notificationId: Int,
        cancelIntent: PendingIntent,
        title: String,
        downloaded: String,
        total: String,
        remainingTime: String,
        speed: String,
        progress: Int,
    ) {
        val notificationView = RemoteViews(
            applicationContext.packageName,
            R.layout.notification_download
        ).apply {
            setTextViewText(R.id.title, title)
            setTextViewText(R.id.progress_text, "Progress: $progress%")
            setTextViewText(R.id.downloaded_total, "Downloaded: $downloaded / $total")
            setTextViewText(R.id.remaining_time, "Remaining Time: $remainingTime")
            setProgressBar(R.id.progress_bar, 100, progress, false)
            setTextViewText(R.id.speed_text, "Speed: $speed")
            setOnClickPendingIntent(R.id.cancel_btn, cancelIntent)
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setCustomContentView(notificationView)
            .setCustomBigContentView(notificationView)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setGroup(OFFLINE_DOWNLOADS_GROUP)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            /* id = */ CHANNEL_ID,
            /* name = */ CHANNEL_NAME,
            /* importance = */ NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun getFilePath(fileName: String): File {
        return File(getDownloadsFolderPath(context), fileName)
    }

    private fun ensureDownloadsFolder() {
        val folderPath = File(context.filesDir, DOWNLOADS_FOLDER_NAME)
        if (!folderPath.exists()) {
            Log.d(TAG, "Created ${folderPath.absolutePath}")
            folderPath.mkdir()
        }
    }

    private fun getDownloadsFolderPathAsString(): String {
        return File(context.filesDir, DOWNLOADS_FOLDER_NAME).absolutePath
    }

    private fun humanReadableSpeed(bytes: Long): String {
        return humanReadableSize(bytes) + "/s"
    }

    private fun humanReadableSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB", "TB", "PB", "EB")
        val exp = (ln(bytes.toDouble()) / ln(1024.0)).toInt()
        val size = bytes / 1024.0.pow(exp.toDouble())
        return String.format(Locale.ENGLISH, "%.1f %s", size, units[exp - 1])
    }


}

class DownloadCancelled : Exception()
