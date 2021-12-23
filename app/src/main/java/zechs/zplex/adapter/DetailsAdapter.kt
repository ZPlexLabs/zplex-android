package zechs.zplex.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_detail.view.*
import zechs.zplex.R
import zechs.zplex.adapter.about.AboutDataModel

class DetailsAdapter : RecyclerView.Adapter<DetailsAdapter.DetailsViewHolder>() {

    data class DetailsData(val header: String, val items: List<AboutDataModel>)

    inner class DetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<DetailsData>() {
        override fun areItemsTheSame(oldItem: DetailsData, newItem: DetailsData): Boolean {
            return oldItem.items == newItem.items
        }

        override fun areContentsTheSame(oldItem: DetailsData, newItem: DetailsData): Boolean {
            return oldItem == newItem
        }
    }


    val differ = AsyncListDiffer(this, differCallback)

    override fun getItemCount() = differ.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        return DetailsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_detail, parent, false
            )
        )
    }

    private val seasonsAdapter by lazy { SeasonsAdapter() }
    private val castAdapter by lazy { CastAdapter() }
    private val similarAdapter by lazy { CurationAdapter() }
    private val recommendationAdapter by lazy { CurationAdapter() }
    private val videosAdapter by lazy { VideosAdapter() }

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        val aboutDataModel = differ.currentList[position]
        when (aboutDataModel.header) {
            "Seasons" -> {
                val seasonsList = aboutDataModel.items.filterIsInstance<AboutDataModel.Season>()
                holder.itemView.apply {
                    tv_header.text = resources.getString(R.string.seasons)
                    rv_list.apply {
                        adapter = seasonsAdapter
                        layoutManager = LinearLayoutManager(
                            context, LinearLayoutManager.HORIZONTAL, false
                        )
                    }
                    seasonsAdapter.differ.submitList(seasonsList)
                    seasonsAdapter.setOnItemClickListener { aboutDataModel ->
                        onItemClickListener?.let { it(aboutDataModel) }
                    }
                }
            }
            "Cast" -> {
                holder.itemView.apply {
                    val castsList = aboutDataModel.items.filterIsInstance<AboutDataModel.Cast>()
                    tv_header.text = resources.getString(R.string.cast)
                    rv_list.apply {
                        adapter = castAdapter
                        layoutManager = LinearLayoutManager(
                            context, LinearLayoutManager.HORIZONTAL, false
                        )
                    }
                    castAdapter.differ.submitList(castsList)
                }
            }
            "Similar" -> {
                holder.itemView.apply {
                    val similarList =
                        aboutDataModel.items.filterIsInstance<AboutDataModel.Curation>()
                    tv_header.text = resources.getString(R.string.similar)
                    rv_list.apply {
                        adapter = similarAdapter
                        layoutManager = LinearLayoutManager(
                            context, LinearLayoutManager.HORIZONTAL, false
                        )
                    }
                    similarAdapter.differ.submitList(similarList)
                    similarAdapter.setOnItemClickListener { aboutDataModel ->
                        onItemClickListener?.let { it(aboutDataModel) }
                    }
                }
            }
            "Recommendations" -> {
                holder.itemView.apply {
                    val similarList =
                        aboutDataModel.items.filterIsInstance<AboutDataModel.Curation>()
                    tv_header.text = resources.getString(R.string.recommendations)
                    rv_list.apply {
                        adapter = recommendationAdapter
                        layoutManager = LinearLayoutManager(
                            context, LinearLayoutManager.HORIZONTAL, false
                        )
                    }
                    recommendationAdapter.differ.submitList(similarList)
                    recommendationAdapter.setOnItemClickListener { aboutDataModel ->
                        onItemClickListener?.let { it(aboutDataModel) }
                    }
                }
            }
            "Related videos" -> {
                holder.itemView.apply {
                    val videosList = aboutDataModel.items.filterIsInstance<AboutDataModel.Video>()
                    tv_header.text = resources.getString(R.string.related_videos)
                    rv_list.apply {
                        adapter = videosAdapter
                        layoutManager = LinearLayoutManager(
                            context, LinearLayoutManager.HORIZONTAL, false
                        )
                    }
                    videosAdapter.differ.submitList(videosList)
                    videosAdapter.setOnItemClickListener { aboutDataModel ->
                        onItemClickListener?.let { it(aboutDataModel) }
                    }
                }
            }
            else -> {
                holder.itemView.rv_list.adapter = null
            }
        }
    }

    private var onItemClickListener: ((AboutDataModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (AboutDataModel) -> Unit) {
        onItemClickListener = listener
    }
}