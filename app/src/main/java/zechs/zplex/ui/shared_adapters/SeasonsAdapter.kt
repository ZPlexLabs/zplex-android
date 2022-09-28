package zechs.zplex.ui.shared_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.data.model.PosterSize
import zechs.zplex.data.model.tmdb.entities.Season
import zechs.zplex.databinding.ItemDetailedMediaBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp
import zechs.zplex.utils.util.Converter


class SeasonsAdapter(
    private val showName: String,
    val setOnClickListener: (Season) -> Unit
) : RecyclerView.Adapter<SeasonsAdapter.SeasonViewHolder>() {

    inner class SeasonViewHolder(
        private val showName: String,
        private val itemBinding: ItemDetailedMediaBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(season: Season) {
            itemBinding.apply {
                val seasonPosterUrl = if (season.poster_path == null) {
                    R.drawable.no_poster
                } else {
                    "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${season.poster_path}"
                }

                GlideApp.with(ivPoster)
                    .load(seasonPosterUrl)
                    .placeholder(R.drawable.no_poster)
                    .into(ivPoster)

                val seasonName = "Season ${season.season_number}"
                tvTitle.text = season.name

                var premiered = "$seasonName of $showName"
                var yearSeason = ""

                val formattedDate = season.air_date?.let { date ->
                    yearSeason += "${date.take(4)} | "
                    Converter.parseDate(date)
                }

                yearSeason += "${season.episode_count} episodes"
                tvYear.text = yearSeason

                formattedDate?.let {
                    premiered += " premiered on $formattedDate."
                }
                val seasonPlot = if (season.overview.toString() == "") {
                    premiered
                } else season.overview
                tvPlot.text = seasonPlot
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Season>() {
        override fun areItemsTheSame(oldItem: Season, newItem: Season): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Season, newItem: Season): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeasonViewHolder {
        val itemBinding = ItemDetailedMediaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SeasonViewHolder(showName, itemBinding)
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: SeasonViewHolder, position: Int) {
        val season = differ.currentList[position]
        holder.bind(season)
        holder.itemView.setOnClickListener {
            setOnClickListener.invoke(season)
        }
    }
}
