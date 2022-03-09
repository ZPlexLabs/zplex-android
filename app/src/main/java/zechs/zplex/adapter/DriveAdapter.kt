package zechs.zplex.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.databinding.ItemDriveBinding
import zechs.zplex.models.drive.File


class DriveAdapter(
    val setOnClickListener: (File) -> Unit
) : RecyclerView.Adapter<DriveAdapter.DriveViewHolder>() {

    class DriveViewHolder(
        private val itemBinding: ItemDriveBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(file: File) {
            itemBinding.apply {
                tvFilename.text = file.name
                tvFilesize.text = file.humanSize
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriveViewHolder {
        val itemBinding = ItemDriveBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DriveViewHolder(itemBinding)
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: DriveViewHolder, position: Int) {
        val file = differ.currentList[position]
        holder.bind(file)
        holder.itemView.setOnClickListener {
            setOnClickListener.invoke(file)
        }
    }
}
