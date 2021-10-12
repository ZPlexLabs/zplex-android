package zechs.zplex.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_actor.view.*
import zechs.zplex.R
import zechs.zplex.models.tmdb.credits.Cast
import zechs.zplex.utils.Constants.Companion.TMDB_IMAGE_PATH

class CreditsAdapter : RecyclerView.Adapter<CreditsAdapter.CreditsViewHolder>() {

    inner class CreditsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Cast>() {
        override fun areItemsTheSame(oldItem: Cast, newItem: Cast): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cast, newItem: Cast): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditsViewHolder {
        return CreditsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_actor,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        val limit = 20
        return if (differ.currentList.size > limit) limit else differ.currentList.size
    }

    override fun onBindViewHolder(holder: CreditsViewHolder, position: Int) {
        val data = differ.currentList[position]

        holder.itemView.apply {
            if (data.profile_path != null) {
                Glide.with(this)
                    .load("${TMDB_IMAGE_PATH}${data.profile_path}")
                    .placeholder(R.color.cardColor)
                    .into(actor_image)
            }
            actor_name.text = data.name
            role.text = data.character
        }
    }
}