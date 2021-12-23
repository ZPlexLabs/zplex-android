package zechs.zplex.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_season.view.*
import zechs.zplex.R
import zechs.zplex.adapter.about.AboutDataModel
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp


class SeasonsAdapter : RecyclerView.Adapter<SeasonsAdapter.SeasonsViewHolder>() {

    inner class SeasonsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<AboutDataModel.Season>() {
        override fun areItemsTheSame(
            oldItem: AboutDataModel.Season,
            newItem: AboutDataModel.Season
        ): Boolean {
            return oldItem.season_number == newItem.season_number
        }

        override fun areContentsTheSame(
            oldItem: AboutDataModel.Season,
            newItem: AboutDataModel.Season
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeasonsViewHolder {
        return SeasonsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_season, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: SeasonsViewHolder, position: Int) {
        val season = differ.currentList[position]

        val seasonPosterUrl = if (season.poster_path != null) {
            "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${season.poster_path}"
        } else {
            R.drawable.no_poster
        }

        val seasonName = "Season ${season.season_number}"
        val episodeCount = "${season.episode_count} episodes"

        holder.itemView.apply {
            season_number.text = seasonName
            episode_count.text = episodeCount

            GlideApp.with(this)
                .load(seasonPosterUrl)
                .placeholder(R.drawable.no_poster)
                .into(item_poster)
            setOnClickListener {
                onItemClickListener?.let { it(season) }
            }
        }
    }

    private var onItemClickListener: ((AboutDataModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (AboutDataModel) -> Unit) {
        onItemClickListener = listener
    }
}