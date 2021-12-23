package zechs.zplex.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_misc_list.view.*
import zechs.zplex.R
import zechs.zplex.models.misc.Pairs

class MiscAdapter : RecyclerView.Adapter<MiscAdapter.MiscViewHolder>() {

    inner class MiscViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Pairs>() {
        override fun areItemsTheSame(oldItem: Pairs, newItem: Pairs): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(oldItem: Pairs, newItem: Pairs): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiscViewHolder {
        return MiscViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_misc_list, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: MiscViewHolder, position: Int) {
        val data = differ.currentList[position]

        holder.itemView.apply {
            if (position == differ.currentList.size - 1) view2.isGone = true
            tv_key.text = data.key
            tv_value.text = data.value
        }
    }
}