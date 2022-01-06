package zechs.zplex.adapter.browse.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_genre.view.*
import zechs.zplex.R
import zechs.zplex.models.dataclass.GenreList


class GenreAdapter : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    inner class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<GenreList>() {
        override fun areItemsTheSame(oldItem: GenreList, newItem: GenreList): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GenreList, newItem: GenreList): Boolean {
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

        holder.itemView.apply {
            tv_genre.text = genre.name
            iv_icon.setImageDrawable(genre.icon)
            setOnClickListener {
                onItemClickListener?.let { it(genre) }
            }
        }
    }

    private var onItemClickListener: ((GenreList) -> Unit)? = null

    fun setOnItemClickListener(listener: (GenreList) -> Unit) {
        onItemClickListener = listener
    }
}