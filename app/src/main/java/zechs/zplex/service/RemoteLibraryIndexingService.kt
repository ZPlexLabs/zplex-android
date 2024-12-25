package zechs.zplex.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import zechs.zplex.data.remote.RemoteLibrary
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class RemoteLibraryIndexingService : LifecycleService() {

    companion object {
        const val NOTIFICATION_ID = 100001

        const val NOTIFICATION_CHANNEL_ID = "indexing_library"
        const val NOTIFICATION_CHANNEL_NAME = "Indexing Library"

        var isServiceRunning = false

    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notification.build())
    }

    @Inject
    lateinit var indexer: RemoteLibrary

    @Inject
    lateinit var notification: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var indexingStateFlow: IndexingStateFlow

    private var job: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (!isServiceRunning) {
            startForegroundService()
            doWork()
            isServiceRunning = true
        }
        return START_STICKY;
    }

    private fun doWork() {
        job = lifecycleScope.launch(Dispatchers.IO) {
            indexingStateFlow.serviceStarted()
            indexer.indexMovies()
            indexer.indexShows()
            indexingStateFlow.serviceStopped()
            stopSelf()
        }
    }


    override fun onDestroy() {
        job?.cancel()
        job = null
        isServiceRunning = false
        indexingStateFlow.serviceStopped()
        super.onDestroy()
    }

}