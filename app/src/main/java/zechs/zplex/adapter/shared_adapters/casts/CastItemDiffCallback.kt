package zechs.zplex.adapter.shared_adapters.casts

import androidx.recyclerview.widget.DiffUtil
import zechs.zplex.models.tmdb.entities.Cast

class CastItemDiffCallback : DiffUtil.ItemCallback<Cast>() {

    override fun areItemsTheSame(
        oldItem: Cast, newItem: Cast
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: Cast, newItem: Cast
    ) = oldItem == newItem

}