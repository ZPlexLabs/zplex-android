package zechs.zplex.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.databinding.ItemLastSeasonBinding
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.models.tmdb.entities.Season
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.ConverterUtils
import zechs.zplex.utils.GlideApp


class SeasonsAdapter(
    private val showName: String,
    val setOnClickListener: (Season) -> Unit
) : RecyclerView.Adapter<SeasonsAdapter.SeasonViewHolder>() {

    class SeasonViewHolder(
        private val showName: String,

        private val itemBinding: ItemLastSeasonBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(season: Season) {
            itemBinding.apply {
                val seasonPosterUrl = if (season.poster_path == null) {
                    R.drawable.no_poster
                } else {
                    "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${season.poster_path}"
                }

                GlideApp.with(ivSeasonPoster)
                    .load(seasonPosterUrl)
                    .placeholder(R.drawable.no_poster)
                    .into(ivSeasonPoster)

                val seasonName = "Season ${season.season_number}"
                tvSeasonNumber.text = season.name

                var premiered = "$seasonName of $showName"
                var yearSeason = ""

                val formattedDate = season.air_date?.let { date ->
                    yearSeason += "${date.take(4)} | "
                    ConverterUtils.parseDate(date)
                }

                yearSeason += "${season.episode_count} episodes"
                tvYearEpisode.text = yearSeason

                formattedDate?.let {
                    premiered += " premiered on $formattedDate."
                }
                val seasonPlot = if (season.overview.toString() == "") {
                    premiered
                } else season.overview
                tvSeasonPlot.text = seasonPlot
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
        val itemBinding = ItemLastSeasonBinding.inflate(
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
