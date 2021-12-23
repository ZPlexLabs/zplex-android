package zechs.zplex.adapter.about

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.databinding.*

class AboutDataAdapter : RecyclerView.Adapter<AboutDataViewHolder>() {

    @JvmField
    var onItemClickListener: ((AboutDataModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (AboutDataModel) -> Unit) {
        onItemClickListener = listener
    }

    private val differCallback = object : DiffUtil.ItemCallback<AboutDataModel>() {
        override fun areItemsTheSame(
            oldItem: AboutDataModel,
            newItem: AboutDataModel
        ): Boolean = when {
            oldItem is AboutDataModel.Header && newItem
                    is AboutDataModel.Header && oldItem.heading == newItem.heading
            -> true
            oldItem is AboutDataModel.Season && newItem
                    is AboutDataModel.Season && oldItem.id == newItem.id
            -> true
            oldItem is AboutDataModel.Cast && newItem
                    is AboutDataModel.Cast && oldItem.credit_id == newItem.credit_id
            -> true
            oldItem is AboutDataModel.Curation && newItem
                    is AboutDataModel.Curation && oldItem.id == newItem.id
            -> true
            oldItem is AboutDataModel.Video && newItem
                    is AboutDataModel.Video && oldItem.key == newItem.key
            -> true
            else -> false
        }

        override fun areContentsTheSame(
            oldItem: AboutDataModel, newItem: AboutDataModel
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AboutDataViewHolder {
        return when (viewType) {
            R.layout.item_header ->
                AboutDataViewHolder.HeaderViewHolder(
                    ItemHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    ), this
                )
            R.layout.item_season ->
                AboutDataViewHolder.SeasonViewHolder(
                    ItemSeasonBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    ), this
                )
            R.layout.item_cast ->
                AboutDataViewHolder.CastViewHolder(
                    ItemCastBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    ), this
                )
            R.layout.item_curate ->
                AboutDataViewHolder.CurationViewHolder(
                    ItemCurateBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    ), this
                )
            R.layout.item_video ->
                AboutDataViewHolder.VideosViewHolder(
                    ItemVideoBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    ), this
                )
            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun onBindViewHolder(
        holder: AboutDataViewHolder, position: Int
    ) {
        when (holder) {
            is AboutDataViewHolder.HeaderViewHolder ->
                holder.bind(differ.currentList[position] as AboutDataModel.Header)
            is AboutDataViewHolder.SeasonViewHolder ->
                holder.bind(differ.currentList[position] as AboutDataModel.Season)
            is AboutDataViewHolder.CastViewHolder ->
                holder.bind(differ.currentList[position] as AboutDataModel.Cast)
            is AboutDataViewHolder.CurationViewHolder ->
                holder.bind(differ.currentList[position] as AboutDataModel.Curation)
            is AboutDataViewHolder.VideosViewHolder ->
                holder.bind(differ.currentList[position] as AboutDataModel.Video)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is AboutDataModel.Header -> R.layout.item_header
            is AboutDataModel.Season -> R.layout.item_season
            is AboutDataModel.Collection -> R.layout.item_season
            is AboutDataModel.Cast -> R.layout.item_cast
            is AboutDataModel.Curation -> R.layout.item_curate
            is AboutDataModel.Video -> R.layout.item_video
        }
    }
}