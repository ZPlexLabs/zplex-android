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
import kotlinx.android.synthetic.main.episode_list.view.*
import zechs.zplex.R
import zechs.zplex.models.drive.File
import zechs.zplex.utils.Constants.Companion.ZPLEX_IMAGE_REDIRECT


class MediaAdapter(private val tvdbId: Int) :
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
//
//        val episodeThumbUri: Uri
//        val cacheOption: RequestOptions
//
//        if (file.thumbnailLink == null) {
//            episodeThumbUri = redirectImagePoster
//            cacheOption = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
//        } else {
//            episodeThumbUri = Uri.parse(file.thumbnailLink)
//            cacheOption = RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
//        }

        holder.itemView.apply {
            episode_title.text = title.substring(0, title.length - 4)
            episode_count.text = count

            Glide.with(context)
                .asBitmap()
                .load(redirectImagePoster)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.color.cardColor)
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