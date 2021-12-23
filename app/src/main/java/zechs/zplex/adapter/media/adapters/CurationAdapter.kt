package zechs.zplex.adapter.media.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_curate.view.*
import zechs.zplex.R
import zechs.zplex.adapter.media.AboutDataModel
import zechs.zplex.models.tmdb.PosterSize
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp


class CurationAdapter : RecyclerView.Adapter<CurationAdapter.CurationViewHolder>() {

    inner class CurationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<AboutDataModel.Curation>() {
        override fun areItemsTheSame(
            oldItem: AboutDataModel.Curation,
            newItem: AboutDataModel.Curation
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: AboutDataModel.Curation,
            newItem: AboutDataModel.Curation
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurationViewHolder {
        return CurationViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_curate, parent, false
            )
        )
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: CurationViewHolder, position: Int) {
        val curation = differ.currentList[position]

        val seasonPosterUrl = if (curation.poster_path == null) {
            R.drawable.no_poster
        } else {
            "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${curation.poster_path}"
        }
        val rating = curation.vote_average ?: 0.0

        holder.itemView.apply {
            tv_showName.text = curation.name ?: curation.title
            tv_rating.text = rating.toString().take(3)

            GlideApp.with(this)
                .load(seasonPosterUrl)
                .placeholder(R.drawable.no_poster)
                .into(item_poster)

            setOnClickListener {
                onItemClickListener?.let { it(curation) }
            }
        }
    }

    private var onItemClickListener: ((AboutDataModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (AboutDataModel) -> Unit) {
        onItemClickListener = listener
    }
}