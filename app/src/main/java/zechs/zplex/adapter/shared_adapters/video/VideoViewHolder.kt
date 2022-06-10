package zechs.zplex.adapter.shared_adapters.video

import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.databinding.ItemVideoBinding
import zechs.zplex.models.tmdb.entities.Video
import zechs.zplex.utils.GlideApp

class VideoViewHolder(
    private val itemBinding: ItemVideoBinding,
    val videoAdapter: VideoAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(video: Video) {

        itemBinding.apply {
            ivBackdrop.apply {
                GlideApp.with(this)
                    .load(video.thumbUrl)
                    .placeholder(R.drawable.no_thumb)
                    .into(ivBackdrop)
            }

            tvSource.text = video.site
            tvVideoName.text = video.name

            root.setOnClickListener {
                videoAdapter.videoOnClick.invoke(video)
            }

        }
    }
}