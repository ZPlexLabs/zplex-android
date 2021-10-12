package zechs.zplex.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_media.view.*
import zechs.zplex.R
import zechs.zplex.models.drive.File
import zechs.zplex.utils.Constants.Companion.TMDB_API_KEY
import zechs.zplex.utils.Constants.Companion.ZPLEX_IMAGE_REDIRECT


class FilesAdapter : RecyclerView.Adapter<FilesAdapter.FilesViewHolder>() {

    inner class FilesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesViewHolder {
        return FilesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_media,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: FilesViewHolder, position: Int) {
        val file = differ.currentList[position]
        val name = file.name

        if (name.contains("TV") || name.contains("Movie")) {

            val redirectImagePoster = if (name.endsWith("TV")) {
                Uri.parse(
                    "${ZPLEX_IMAGE_REDIRECT}/tvdb/${
                        file.name.split(" - ").toTypedArray()[0]
                    }"
                )
            } else {
                Uri.parse(
                    "${ZPLEX_IMAGE_REDIRECT}/tmdb/${
                        file.name.split(" - ").toTypedArray()[0]
                    }?api_key=${TMDB_API_KEY}&language=en-US"
                )
            }

            holder.itemView.apply {
                Glide.with(this)
                    .load(redirectImagePoster)
                    .placeholder(R.color.cardColor)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(item_poster)
                setOnClickListener {
                    onItemClickListener?.let { it(file) }
                }
            }
        }
    }

    private var onItemClickListener: ((File) -> Unit)? = null

    fun setOnItemClickListener(listener: (File) -> Unit) {
        onItemClickListener = listener
    }
}