package zechs.zplex.ui.shared_adapters.video

import androidx.recyclerview.widget.RecyclerView
import coil.load
import zechs.zplex.R
import zechs.zplex.data.model.tmdb.entities.Video
import zechs.zplex.databinding.ItemVideoBinding

class VideoViewHolder(
    private val itemBinding: ItemVideoBinding,
    val videoAdapter: VideoAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(video: Video) {

        itemBinding.apply {
            ivBackdrop.load(video.thumbUrl) {
                placeholder(R.drawable.no_thumb)
            }

            tvSource.text = video.site
            tvVideoName.text = video.name

            root.setOnClickListener {
                videoAdapter.videoOnClick.invoke(video)
            }

        }
    }
}