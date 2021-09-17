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
import zechs.zplex.models.tvdb.actors.Data
import zechs.zplex.utils.Constants.Companion.TVDB_IMAGE_PATH


class ActorsAdapter : RecyclerView.Adapter<ActorsAdapter.ActorsViewHolder>() {

    inner class ActorsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Data>() {
        override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorsViewHolder {
        return ActorsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_actor,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return if (differ.currentList.size > 10) 10 else differ.currentList.size
    }

    override fun onBindViewHolder(holder: ActorsViewHolder, position: Int) {
        val data = differ.currentList[position]

        holder.itemView.apply {
            Glide.with(context)
                .load("${TVDB_IMAGE_PATH}${data.image}")
                .placeholder(R.color.cardColor)
                .into(actor_image)

            actor_name.text = data.name
            role.text = data.role
        }
    }
}