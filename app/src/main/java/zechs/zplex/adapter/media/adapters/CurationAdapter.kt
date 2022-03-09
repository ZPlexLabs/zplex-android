package zechs.zplex.adapter.media.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recom.view.*
import zechs.zplex.R
import zechs.zplex.adapter.media.AboutDataModel
import zechs.zplex.models.tmdb.BackdropSize
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
                R.layout.item_recom, parent, false
            )
        )
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: CurationViewHolder, position: Int) {
        val media = differ.currentList[position]

        val backdropUrl = if (media.backdrop_path == null) {
            R.drawable.no_thumb
        } else {
            "$TMDB_IMAGE_PREFIX/${BackdropSize.w300}${media.backdrop_path}"
        }

        holder.itemView.apply {
            tv_title.text = media.name ?: media.title

            GlideApp.with(this)
                .load(backdropUrl)
                .placeholder(R.drawable.no_poster)
                .into(ivBackdrop)

            setOnClickListener {
                onItemClickListener?.let { it(media) }
            }
        }
    }

    private var onItemClickListener: ((AboutDataModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (AboutDataModel) -> Unit) {
        onItemClickListener = listener
    }
}