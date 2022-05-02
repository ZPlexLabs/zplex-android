package zechs.zplex.adapter.shared_adapters.episode

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.databinding.ItemEpisodeBinding
import zechs.zplex.models.zplex.Episode

class EpisodeAdapter(
    val context: Context,
    val accessToken: String?,
    val episodeOnClick: (Episode, String, Boolean) -> Unit
) : ListAdapter<Episode, EpisodeViewHolder>(EpisodeItemDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = EpisodeViewHolder(
        context = context,
        itemBinding = ItemEpisodeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ),
        episodeAdapter = this
    )

    override fun onBindViewHolder(
        holder: EpisodeViewHolder, position: Int
    ) {
        val isLast = position == itemCount
        holder.bind(getItem(position), accessToken, isLast)
    }

}