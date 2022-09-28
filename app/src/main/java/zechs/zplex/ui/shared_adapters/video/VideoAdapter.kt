package zechs.zplex.ui.shared_adapters.video

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.data.model.tmdb.entities.Video
import zechs.zplex.databinding.ItemVideoBinding

class VideoAdapter(
    val videoOnClick: (Video) -> Unit
) : ListAdapter<Video, VideoViewHolder>(VideoItemDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = VideoViewHolder(
        itemBinding = ItemVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ),
        videoAdapter = this
    )

    override fun onBindViewHolder(
        holder: VideoViewHolder, position: Int
    ) {
        holder.bind(getItem(position))
    }

}