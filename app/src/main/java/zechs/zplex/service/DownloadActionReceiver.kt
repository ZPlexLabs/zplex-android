package zechs.zplex.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager


class DownloadActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ${intent.action}")
        if (intent.action == CANCEL_ACTION) {
            val downloadName = intent.getStringExtra(DOWNLOAD_NAME) ?: return
            Log.d(TAG, "downloadName: $downloadName")
            val downloadId = intent.getStringExtra(DOWNLOAD_ID) ?: return
            Log.d(TAG, "downloadId: $downloadName")
            val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
            Log.d(TAG, "notificationId: $downloadName")
            if (notificationId == 0) return
            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWorkByTag(downloadId)
//            downloadWorkManager.cancelAllWorkByTag(downloadId)
            NotificationManagerCompat.from(context).cancel(notificationId)
            Log.d(TAG, "Download cancelled: $downloadName")
        }
    }

    companion object {
        private const val TAG = "DownloadActionReceiver"
        const val CANCEL_ACTION = "CANCEL_ACTION"
        const val DOWNLOAD_NAME = "DOWNLOAD_NAME"
        const val DOWNLOAD_ID = "DOWNLOAD_ID"
        const val NOTIFICATION_ID = "NOTIFICATION_ID"
    }
}