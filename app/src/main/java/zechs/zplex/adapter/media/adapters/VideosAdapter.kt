package zechs.zplex.adapter.media.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_video.view.*
import zechs.zplex.R
import zechs.zplex.adapter.media.AboutDataModel
import zechs.zplex.utils.GlideApp


class VideosAdapter : RecyclerView.Adapter<VideosAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<AboutDataModel.Video>() {
        override fun areItemsTheSame(
            oldItem: AboutDataModel.Video,
            newItem: AboutDataModel.Video
        ): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(
            oldItem: AboutDataModel.Video,
            newItem: AboutDataModel.Video
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_video, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = differ.currentList[position]

        holder.itemView.apply {
            tv_source.text = video.site
            tv_videoName.text = video.name

            GlideApp.with(this)
                .load(video.thumbUrl)
                .placeholder(R.drawable.no_thumb)
                .into(thumb)

            setOnClickListener {
                onItemClickListener?.let { it(video) }
            }
        }
    }

    private var onItemClickListener: ((AboutDataModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (AboutDataModel) -> Unit) {
        onItemClickListener = listener
    }
}