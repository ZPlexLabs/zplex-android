package zechs.zplex.adapter.shared_adapters.casts

import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.databinding.ItemCastBinding
import zechs.zplex.models.tmdb.ProfileSize
import zechs.zplex.models.tmdb.entities.Cast
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp

class CastViewHolder(
    private val itemBinding: ItemCastBinding,
    val castAdapter: CastAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(cast: Cast) {
        val imageUrl = if (cast.profile_path != null) {
            "${TMDB_IMAGE_PREFIX}/${ProfileSize.w185}${cast.profile_path}"
        } else {
            R.drawable.no_actor
        }
        itemBinding.apply {
            GlideApp.with(actorImage)
                .load(imageUrl)
                .placeholder(R.drawable.no_actor)
                .into(actorImage)

            actorName.text = cast.name
            role.text = cast.character.split("/")[0]
            root.setOnClickListener {
                castAdapter.castOnClick.invoke(cast)
            }
        }
    }
}