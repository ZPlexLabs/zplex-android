package zechs.zplex.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.logs_item.view.*
import zechs.zplex.R
import zechs.zplex.models.witch.ReleasesLog
import zechs.zplex.utils.Constants.Companion.ZPLEX
import zechs.zplex.utils.Constants.Companion.ZPLEX_IMAGE_REDIRECT
import zechs.zplex.utils.ConverterUtils
import java.text.SimpleDateFormat
import java.util.*


class LogsAdapter : RecyclerView.Adapter<LogsAdapter.LogsViewHolder>() {

    inner class LogsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<ReleasesLog>() {
        override fun areItemsTheSame(oldItem: ReleasesLog, newItem: ReleasesLog): Boolean {
            return oldItem.file == newItem.file
        }

        override fun areContentsTheSame(oldItem: ReleasesLog, newItem: ReleasesLog): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogsViewHolder {
        return LogsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.logs_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: LogsViewHolder, position: Int) {
        val log = differ.currentList[position]
        val tvdbId = log.folder.split(" - ", ignoreCase = false, limit = 3).toTypedArray()[0]
        val show = log.folder.split(" - ", ignoreCase = false, limit = 3).toTypedArray()[1]
        val type = log.folder.split(" - ", ignoreCase = false, limit = 3).toTypedArray()[2]

        val episode = log.file.split(" - ", ignoreCase = false, limit = 2).toTypedArray()[0]
        val episodeTitle =
            (log.file.split(" - ", ignoreCase = false, limit = 2).toTypedArray()[1]).dropLast(4)
        val posterUrl = Uri.parse("${ZPLEX}${tvdbId} - $show - TV/poster.jpg")
        val episodeThumbUrl = Uri.parse("${ZPLEX}${log.folder}/${log.file.dropLast(4)}.jpg")

        val bodyText = try {
            "Ep ${
                episode.substring(4).toInt()
            } - $episodeTitle"
        } catch (nfe: NumberFormatException) {
            nfe.printStackTrace()
            "$episode - $episodeTitle"
        }
        var time = log.time

        if (time.length == 20) {
            time = time.replace("Z", ".000Z")
        }

        val utcFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")

        val date = utcFormat.parse(time)
        val pstFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        pstFormat.timeZone = TimeZone.getTimeZone("IST")

        val redirectImagePoster =
            Uri.parse(
                "${ZPLEX_IMAGE_REDIRECT}/tvdb/${tvdbId}/episodes/query?airedSeason=${
                    episode.substring(
                        1,
                        3
                    ).toInt()
                }&airedEpisode=${
                    episode.substring(4).toInt()
                }"
            )

        holder.itemView.apply {
            tv_show.text = show
            tv_episode.text = bodyText
            date?.let {
                tv_time.text = ConverterUtils.toDuration(pstFormat.format(date))
            }
            Glide.with(context)
                .asBitmap()
                .load(redirectImagePoster)
                .placeholder(R.color.cardColor)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(iv_thumb)

            setOnClickListener {
                onItemClickListener?.let { it(log) }
            }
        }
    }

    private var onItemClickListener: ((ReleasesLog) -> Unit)? = null

    fun setOnItemClickListener(listener: (ReleasesLog) -> Unit) {
        onItemClickListener = listener
    }
}