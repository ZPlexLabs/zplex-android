package zechs.zplex.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.episode_list.view.*
import zechs.zplex.R
import zechs.zplex.models.drive.File
import zechs.zplex.utils.Constants.Companion.ZPLEX
import java.net.IDN
import java.net.URI
import java.net.URL

class MediaAdapter(private val showName: String) :
    RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        return MediaViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.episode_list,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val file = differ.currentList[position]

        val fileName = file.name
        val episode = fileName.split(" - ".toRegex(), 2).toTypedArray()[0]
        val title = fileName.split(" - ".toRegex(), 2).toTypedArray()[1]
        val bytes = file.size.toString()

        val count = try {
            "Episode ${episode.substring(4).toInt()}"
        } catch (nfe: NumberFormatException) {
            "Episode ${episode.substring(4)}"
        }
        val episodeThumbURL =
            URL("${ZPLEX}${showName} - TV/${fileName.substring(0, fileName.length - 3)}jpg")

        val episodeThumbUri = file.thumbnailLink
            ?: URI(
                episodeThumbURL.protocol,
                episodeThumbURL.userInfo,
                IDN.toASCII(episodeThumbURL.host),
                episodeThumbURL.port,
                episodeThumbURL.path,
                episodeThumbURL.query,
                episodeThumbURL.ref
            ).toASCIIString()


        holder.itemView.apply {
            episode_title.text = title.substring(0, title.length - 4)
            episode_count.text = count

            Glide.with(context)
                .asBitmap()
                .load(episodeThumbUri)
                .placeholder(R.color.cardColor)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(thumb)

            setOnClickListener {
                onItemClickListener?.let { it(file) }
            }
        }
    }

    private var onItemClickListener: ((File) -> Unit)? = null

    fun setOnItemClickListener(listener: (File) -> Unit) {
        onItemClickListener = listener
    }
}