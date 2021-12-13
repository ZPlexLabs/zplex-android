package zechs.zplex.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_episode.view.*
import zechs.zplex.R
import zechs.zplex.models.drive.File
import zechs.zplex.utils.Constants.ZPLEX_IMAGE_REDIRECT
import zechs.zplex.utils.Constants.regexFile

class MediaAdapter(private val tvdbId: Int) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

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
                R.layout.item_episode, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val file = differ.currentList[position]
        val nameSplit = regexFile.toRegex().find(file.name)?.destructured?.toList()

        if (nameSplit != null) {
            val seasonCount = nameSplit[0].toInt()
            val episodeCount = nameSplit[1].toInt()
            val episodeTitle = nameSplit[3]
            // val episodeExtension = nameSplit[4]

            val count = "Episode $episodeCount"

            val redirectImagePoster = Uri.parse(
                "${ZPLEX_IMAGE_REDIRECT}/tvdb/${
                    tvdbId
                }/episodes/query?airedSeason=$seasonCount&airedEpisode=$episodeCount"
            )

            holder.itemView.apply {
                if (episodeTitle == count) episode_count.isGone = true

                episode_title.text = if (episodeTitle.isEmpty()) "No title" else episodeTitle
                episode_count.text = count
                episode_size.text = file.humanSize

                Glide.with(this)
                    .asBitmap()
                    .load(redirectImagePoster)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.no_thumb)
                    .into(thumb)

                setOnClickListener {
                    onItemClickListener?.let { it(file) }
                }
            }
        }
    }

    private var onItemClickListener: ((File) -> Unit)? = null

    fun setOnItemClickListener(listener: (File) -> Unit) {
        onItemClickListener = listener
    }
}