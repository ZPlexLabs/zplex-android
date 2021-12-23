package zechs.zplex.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_genre.view.*
import zechs.zplex.R
import zechs.zplex.models.tmdb.genre.Genre


class GenreAdapter : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    inner class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Genre>() {
        override fun areItemsTheSame(oldItem: Genre, newItem: Genre): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Genre, newItem: Genre): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        return GenreViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_genre, parent, false
            )
        )
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = differ.currentList[position]
        holder.itemView.tv_genre.text = genre.name
        holder.itemView.setOnClickListener {
            onItemClickListener?.let { it(genre) }
        }
    }

    private var onItemClickListener: ((Genre) -> Unit)? = null

    fun setOnItemClickListener(listener: (Genre) -> Unit) {
        onItemClickListener = listener
    }
}