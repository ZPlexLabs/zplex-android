package zechs.zplex

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import zechs.zplex.activity.AboutActivity
import zechs.zplex.utils.Constants
import java.net.*
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {

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
            val body = data["body"]

            Log.d(tag, "Notification Title: $title")
            Log.d(tag, "Notification Body: $body")

            var show: String? = null
            var episode: String? = null
            var episodeTitle: String? = null
            var bodyText: String? = null

            if (body != null) {
                show = body.split(" - ", ignoreCase = false, limit = 3).toTypedArray()[0]
                episode = body.split(" - ", ignoreCase = false, limit = 3).toTypedArray()[1]
                episodeTitle = body.split(" - ", ignoreCase = false, limit = 3).toTypedArray()[2]
                bodyText = try {
                    "$show Season ${episode.substring(1, 3).toInt()}, Episode ${
                        episode.substring(4).toInt()
                    } - $episodeTitle"
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                    "$episode - $episodeTitle"
                }
            }
            val posterURL = Constants.ZPlex + show + " - TV" + "/poster.jpg"
            var playURI: String? = null

            try {
                val playURL =
                    URL(Constants.ZPlex + show + " - TV" + "/" + episode + " - " + episodeTitle + ".mkv")
                playURI = URI(
                    playURL.protocol,
                    playURL.userInfo,
                    IDN.toASCII(playURL.host),
                    playURL.port,
                    playURL.path,
                    playURL.query,
                    playURL.ref
                ).toString()
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }

            Log.d("show", show!!)
            Log.d("bodyText", bodyText!!)
            Log.d("PlayURL", playURI!!)
            Log.d("posterURL", posterURL)

            val requestCode = Random().nextInt()
            val channelId = "New releases"

            val vlcIntent = Intent(Intent.ACTION_VIEW)
            vlcIntent.setPackage("org.videolan.vlc")
            vlcIntent.component =
                ComponentName("org.videolan.vlc", "org.videolan.vlc.gui.video.VideoPlayerActivity")
            vlcIntent.setDataAndTypeAndNormalize(Uri.parse(playURI), "video/*")
            vlcIntent.putExtra("title", "$episode - $episodeTitle")
            vlcIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val finalVlcIntent = PendingIntent.getActivity(
                this,
                requestCode,
                vlcIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationIntent = Intent(this, AboutActivity::class.java)
            notificationIntent.putExtra("NAME", show)
            notificationIntent.putExtra("TYPE", "TV")
            notificationIntent.putExtra("POSTERURL", posterURL)
            notificationIntent.flags = (Intent.FLAG_ACTIVITY_SINGLE_TOP
                    or Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

            val intent = PendingIntent.getActivity(
                this,
                requestCode,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_outline_new_releases_24)
                .setContentTitle(title)
                .setContentText(bodyText)
                .setAutoCancel(true)
                .setContentIntent(intent)
                .setStyle(NotificationCompat.BigTextStyle().bigText(title))
                .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .addAction(R.drawable.ic_baseline_play_arrow_24, "Watch now", finalVlcIntent)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setDefaults(Notification.DEFAULT_ALL)


            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel =
                NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
            val notify = Notification()
            notify.flags = Notification.FLAG_ONLY_ALERT_ONCE
            val random = Random()
            manager.notify(random.nextInt(), builder.build())
        }
    }
}