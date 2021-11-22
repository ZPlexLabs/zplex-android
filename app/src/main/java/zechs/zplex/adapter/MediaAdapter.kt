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
import kotlinx.android.synthetic.main.item_episode.view.*
import zechs.zplex.R
import zechs.zplex.models.drive.File
import zechs.zplex.utils.Constants.ZPLEX_IMAGE_REDIRECT


class MediaAdapter(
    private val tvdbId: Int,
) :
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
                R.layout.item_episode,
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
        val bytes = file.humanSize

        val count = try {
            "Episode ${episode.substring(4).toInt()}"
        } catch (nfe: NumberFormatException) {
            "Episode ${episode.substring(4)}"
        }

        val redirectImagePoster =
            Uri.parse(
                "${ZPLEX_IMAGE_REDIRECT}/tvdb/${tvdbId}/episodes/query?airedSeason=${
                    episode.substring(
                        1,
                        3
                    ).toInt()
                }&airedEpisode=${
                    episode.substring(4, 6).toInt()
                }"
            )

        holder.itemView.apply {
//            if (loadBig) {
//                if (bytes == "0") {
//                    episode_size.visibility = View.GONE
//                    episode_size_bg.visibility = View.GONE
//                } else {
//                    episode_size.visibility = View.VISIBLE
//                    episode_size_bg.visibility = View.VISIBLE
//                    episode_size.text = bytes
//                }
//            }

            episode_title.text = title.substring(0, title.length - 4)
            episode_count.text = count

            if (title.substring(0, title.length - 4) == count) {
                episode_count.visibility = View.INVISIBLE
            }

            Glide.with(this)
                .asBitmap()
                .load(redirectImagePoster)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.thumb)
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