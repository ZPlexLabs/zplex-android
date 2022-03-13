package zechs.zplex


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.utils.NotificationKeys
import java.util.*


class FirebaseMessagingService : FirebaseMessagingService() {

    private val tag = "MyFirebaseMsgService"

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d("onNewToken", p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(tag, "From: " + remoteMessage.from)

        if (remoteMessage.notification != null) {
            Log.d(tag, "Message  " + remoteMessage.data)
        }

        if (remoteMessage.data.isNotEmpty()) {

            val data = remoteMessage.data
            val title = data["title"]
            val tmdbId = data["body"]

            Log.d(tag, "Notification Title: $title")
            Log.d(tag, "Notification Body: $tmdbId")

            val requestCode = Random().nextInt()

            val intent = Intent(this, ZPlexActivity::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                setData("movie/$tmdbId".toUri())
            }

            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )

            val defaultSoundUri = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            )

            val notificationBuilder = NotificationCompat.Builder(this, NotificationKeys.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_new_releases_24dp)
                .setContentTitle("New release")
                .setContentText(title)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

            val notificationManager = getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            val channel = NotificationChannel(
                NotificationKeys.CHANNEL_ID,
                NotificationKeys.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(channel)
            notificationManager.notify(requestCode, notificationBuilder.build())
        }
    }
}