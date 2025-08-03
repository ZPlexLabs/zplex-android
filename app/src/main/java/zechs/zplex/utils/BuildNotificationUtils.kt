package zechs.zplex.utils

import android.content.Context
import androidx.core.app.NotificationCompat
import zechs.zplex.R

class BuildNotificationUtils {
    companion object {
        fun buildIndexingServiceNotification(
            context: Context,
            notificationChannelId: String
        ): NotificationCompat.Builder {
            return internalBuildIndexingServiceNotification(context, notificationChannelId, null)
        }

        fun buildIndexingServiceNotification(
            context: Context,
            notificationChannelId: String,
            contentText: String
        ): NotificationCompat.Builder {
            return internalBuildIndexingServiceNotification(
                context,
                notificationChannelId,
                contentText
            )
        }

        private fun internalBuildIndexingServiceNotification(
            context: Context,
            notificationChannelId: String,
            contentText: String?
        ): NotificationCompat.Builder {
            val builder = NotificationCompat.Builder(context, notificationChannelId)
                .setContentTitle(context.getString(R.string.indexing))

                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_library_24dp)
            if (contentText != null) {
                builder.setContentText(contentText)
            }
            return builder
        }
    }
}