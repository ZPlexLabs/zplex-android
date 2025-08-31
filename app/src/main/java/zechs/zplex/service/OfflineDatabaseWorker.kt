package zechs.zplex.service

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import zechs.zplex.R
import zechs.zplex.data.local.offline.OfflineEpisodeDao
import zechs.zplex.data.local.offline.OfflineMovieDao
import zechs.zplex.data.local.offline.OfflineSeasonDao
import zechs.zplex.data.local.offline.OfflineShowDao
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.offline.OfflineEpisode
import zechs.zplex.data.model.offline.OfflineMovie
import zechs.zplex.data.model.offline.OfflineSeason
import zechs.zplex.data.model.offline.OfflineShow
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.utils.ext.deleteIfExistsSafely
import java.io.File
import javax.inject.Inject

class OfflineDatabaseWorkerFactory @Inject constructor(
    private val gson: Gson,
    private val tmdbRepository: TmdbRepository,
    private val offlineShowDao: OfflineShowDao,
    private val offlineSeasonDao: OfflineSeasonDao,
    private val offlineEpisodeDao: OfflineEpisodeDao,
    private val offlineMovieDao: OfflineMovieDao
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker =
        OfflineDatabaseWorker(appContext, workerParameters, gson, tmdbRepository, offlineShowDao, offlineSeasonDao, offlineEpisodeDao, offlineMovieDao)
}

@HiltWorker
class OfflineDatabaseWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val gson: Gson,
    private val tmdbRepository: TmdbRepository,
    private val offlineShowDao: OfflineShowDao,
    private val offlineSeasonDao: OfflineSeasonDao,
    private val offlineEpisodeDao: OfflineEpisodeDao,
    private val offlineMovieDao: OfflineMovieDao
) : CoroutineWorker(context, workerParams) {

    private val notificationManager = applicationContext.getSystemService(
        Context.NOTIFICATION_SERVICE
    ) as NotificationManager

    companion object {
        const val TAG = "OfflineDatabaseWorker"
    }

    override suspend fun doWork(): Result {
        val filePath = inputData.getString(DownloadWorker.FILE_PATH)
            ?: return fail("File path is required.")

        val file = File(filePath)

        // Common required fields
        val title = inputData.getString(DownloadWorker.FILE_TITLE)
            ?: return fail("Download title is required.", file)

        val tmdbId = inputData.getInt(DownloadWorker.TMDB_ID, 0)
            .takeIf { it != 0 }
            ?: return fail("TMDB ID is required.", file)

        val mediaType = inputData.getString(DownloadWorker.MEDIA_TYPE)
            ?: return fail("Media type is required.", file)

        val notificationId = inputData.getInt(DownloadWorker.NOTIFICATION_ID, 0)
            .takeIf { it != 0 }
            ?: return fail("Notification ID is required.", file)

        return try {
            when (mediaType) {
                MediaType.tv.name -> {
                    val seasonNumber = inputData.getInt(DownloadWorker.SEASON_NUMBER, 0)
                        .takeIf { it != 0 }
                        ?: return fail("Season number is required.", file)

                    val episodeNumber = inputData.getInt(DownloadWorker.EPISODE_NUMBER, 0)
                        .takeIf { it != 0 }
                        ?: return fail("Episode number is required.", file)

                    saveOfflineShow(title, tmdbId, seasonNumber, episodeNumber, filePath, notificationId)
                    Result.success()
                }

                MediaType.movie.name -> {
                    saveOfflineMovie(title, tmdbId, filePath, notificationId)
                    Result.success()
                }

                else -> {
                    fail("Unsupported media type: $mediaType", file)
                }
            }
        } catch (e: Exception) {
            fail(e.message ?: "Unable to write metadata.", file)
        }
    }

    private suspend fun saveOfflineMovie(
        title: String,
        tmdbId: Int,
        filePath: String,
        notificationId: Int
    ) {
        withContext(Dispatchers.IO) {
            val movie = tmdbRepository.getMovie(tmdbId, appendToQuery = null)
            if (movie.isSuccessful && movie.body() != null) {
                if (!offlineMovieDao.getMovie(tmdbId)) {
                    offlineMovieDao.upsertMovie(OfflineMovie(tmdbId, gson.toJson(movie.body()!!), filePath))
                }
                showDownloadCompleteNotification(notificationId, title)
            } else {
                throw Exception("Unable to fetch show details")
            }
        }
    }

    private suspend fun saveOfflineShow(
        title: String,
        tmdbId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        filePath: String,
        notificationId: Int
    ) {
        withContext(Dispatchers.IO) {
            val tvResponse = async { tmdbRepository.getShow(tmdbId, appendToQuery = null) }
            val seasonResponse = async { tmdbRepository.getSeason(tmdbId, seasonNumber) }
            val tv = tvResponse.await()
            val season = seasonResponse.await()
            if (tv.isSuccessful && tv.body() != null && season.isSuccessful && season.body() != null) {

                if (!offlineShowDao.getShow(tmdbId)) {
                    offlineShowDao.upsertShow(OfflineShow(tmdbId, gson.toJson(tv.body()!!)))
                }

                if (!offlineSeasonDao.getSeason(tmdbId, seasonNumber)) {
                    offlineSeasonDao.upsertSeason(
                        OfflineSeason(tmdbId, seasonNumber, gson.toJson(season.body()!!))
                    )
                }
                if (!offlineEpisodeDao.getEpisode(tmdbId, seasonNumber, episodeNumber)) {
                    offlineEpisodeDao.upsertEpisode(
                        OfflineEpisode(tmdbId, seasonNumber, episodeNumber, filePath)
                    )
                }
                showDownloadCompleteNotification(notificationId, title)
            } else {
                throw Exception("Unable to fetch show details")
            }
        }
    }

    private fun showDownloadCompleteNotification(notificationId: Int, title: String) {
        if (!hasNotificationPermission()) {
            Log.d(TAG, "Notification permission denied.")
            return
        }
        Log.d(DownloadWorker.TAG, "Download complete.")
        val notification = NotificationCompat.Builder(applicationContext, DownloadWorker.CHANNEL_ID)
            .setContentTitle("Download Complete")
            .setContentText(title)
            .setSmallIcon(R.drawable.ic_download_done_24)
            .setGroup(DownloadWorker.OFFLINE_DOWNLOADS_GROUP)
            .build()

        notificationManager.notify(notificationId, notification)
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

    private fun fail(message: String, file: File? = null): Result {
        Log.e(TAG, message)
        file?.deleteIfExistsSafely()
        return Result.failure()
    }
}