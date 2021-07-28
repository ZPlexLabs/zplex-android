package zechs.zplex.adapter

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import zechs.zplex.R
import zechs.zplex.utils.ConverterUtils.Companion.getSize
import java.util.*

class EpisodeItem(
    var show: String,
    var episode: String,
    var episodeTitle: String,
    var playUrl: String,
    var bytes: String
)

class EpisodeAdapter(
    private val episodeItems: ArrayList<EpisodeItem>,
    private val context: Context?
) : RecyclerView.Adapter<EpisodeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_list, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episodeItem = episodeItems[position]
        holder.episodeCount.text = "Episode ${episodeItem.episode.substring(4).toInt()}"
        holder.episodeName.text = episodeItem.episodeTitle
        holder.offlineEp.setOnClickListener { v: View? ->
            if (context != null) {
                val dlRequest = DownloadManager.Request(Uri.parse(episodeItem.playUrl))
                val fileName =
                    "${episodeItem.show}/${episodeItem.episode} - ${episodeItem.episodeTitle}.mkv"
                dlRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                dlRequest.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )
                dlRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                dlRequest.setMimeType("video/x-matroska")
                val dlManager =
                    context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dlManager.enqueue(dlRequest)
                val size = getSize(episodeItem.bytes.toLong())
                Toast.makeText(context, "Download started ($size)", Toast.LENGTH_SHORT).show()
            }
        }
        Glide.with(context!!)
            .load(episodeItem.playUrl.substring(0, episodeItem.playUrl.length - 3) + "jpg")
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.thumbImage)
    }

    override fun getItemCount(): Int {
        return episodeItems.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var episodeCount: TextView = view.findViewById(R.id.episode_count)
        var episodeName: TextView = view.findViewById(R.id.episode_title)
        var offlineEp: AppCompatImageButton = view.findViewById(R.id.offline)
        var thumbImage: ImageView = view.findViewById(R.id.thumb)
        override fun onClick(view: View) {
            val position = layoutPosition
            val episodeItem = episodeItems[position]
            try {
                val vlcIntent = Intent(Intent.ACTION_VIEW)
                vlcIntent.setPackage("org.videolan.vlc")
                vlcIntent.component = ComponentName(
                    "org.videolan.vlc",
                    "org.videolan.vlc.gui.video.VideoPlayerActivity"
                )
                vlcIntent.setDataAndTypeAndNormalize(Uri.parse(episodeItem.playUrl), "video/*")
                vlcIntent.putExtra("title", episodeItem.episode + " - " + episodeItem.episodeTitle)
                vlcIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                context!!.startActivity(vlcIntent)
            } catch (notFoundException: ActivityNotFoundException) {
                notFoundException.printStackTrace()
                Toast.makeText(
                    context,
                    "VLC not found, Install VLC from Play Store",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        init {
            view.setOnClickListener(this)
        }
    }
}