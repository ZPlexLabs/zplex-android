package zechs.zplex.ui.files.adapter

import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import zechs.zplex.R
import zechs.zplex.databinding.ItemDriveFileBinding
import zechs.zplex.databinding.ItemLoadingBinding

sealed class FilesViewHolder(
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {


    class DriveFileViewHolder(
        private val itemBinding: ItemDriveFileBinding,
        val filesAdapter: FilesAdapter
    ) : FilesViewHolder(itemBinding) {

        fun bind(file: FilesDataModel.File) {
            val item = file.driveFile
            itemBinding.apply {

                val iconLink = item.iconLink128 ?: R.drawable.ic_my_drive_24

                ivFileType.load(iconLink) {
                    placeholder(R.drawable.ic_my_drive_24)
                    size(48, 48)
                }
                tvFileName.text = item.name

                val tvFileSizeTAG = "tvFileSize"

                tvFileSize.apply {
                    tag = if (item.size == null) {
                        tvFileSizeTAG
                    } else null

                    isGone = tag == tvFileSizeTAG
                    text = item.humanSize
                }

                root.setOnClickListener {
                    filesAdapter.onClickListener.invoke(item)
                }
            }
        }
    }

    class LoadingViewHolder(
        itemBinding: ItemLoadingBinding
    ) : FilesViewHolder(itemBinding)

}