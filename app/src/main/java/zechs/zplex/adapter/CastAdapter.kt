package zechs.zplex.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_cast.view.*
import zechs.zplex.R
import zechs.zplex.models.tmdb.ProfileSize
import zechs.zplex.models.tmdb.tv.Cast
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX

class CastAdapter : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    inner class CastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Cast>() {
        override fun areItemsTheSame(oldItem: Cast, newItem: Cast): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cast, newItem: Cast): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        return CastViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_cast, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        val cast = differ.currentList[position]

        val imageUrl = if (cast.profile_path != null) {
            "${TMDB_IMAGE_PREFIX}/${ProfileSize.w185}${cast.profile_path}"
        } else {
            R.drawable.no_actor
        }

        holder.itemView.apply {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.no_actor)
                .into(actor_image)

            actor_name.text = cast.name
            role.text = cast.character
            setOnClickListener {
                onItemClickListener?.let { it(cast) }
            }
        }
    }

    private var onItemClickListener: ((Cast) -> Unit)? = null

    fun setOnItemClickListener(listener: (Cast) -> Unit) {
        onItemClickListener = listener
    }
}