package zechs.zplex.ui.shared_adapters.season

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.data.model.tmdb.entities.Season
import zechs.zplex.databinding.ItemDetailedMediaBinding

class SeasonsAdapter(
    private val showName: String,
    val seasonOnClick: (Season) -> Unit
) : ListAdapter<Season, SeasonViewHolder>(SeasonItemDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = SeasonViewHolder(
        showName = showName,
        itemBinding = ItemDetailedMediaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ),
        seasonsAdapter = this
    )

    override fun onBindViewHolder(
        holder: SeasonViewHolder, position: Int
    ) {
        holder.bind(getItem(position))
    }

}