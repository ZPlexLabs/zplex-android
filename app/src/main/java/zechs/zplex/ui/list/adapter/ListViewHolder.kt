package zechs.zplex.ui.list.adapter

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.databinding.ItemListBinding
import zechs.zplex.ui.shared_adapters.casts.CastAdapter
import zechs.zplex.ui.shared_adapters.detailed_media.DetailedMediaAdapter
import zechs.zplex.ui.shared_adapters.season.SeasonsAdapter
import zechs.zplex.ui.shared_adapters.video.VideoAdapter


class ListViewHolder(
    private val itemBinding: ItemListBinding,
    private val listDataAdapter: ListDataAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bindSeason(item: ListDataModel.Seasons) {
        val seasonsAdapter by lazy {
            SeasonsAdapter(
                showName = item.showName,
                seasonOnClick = {
                    listDataAdapter.listClickListener.onClickSeason(it)
                }
            )
        }
        itemBinding.rvList.apply {
            adapter = seasonsAdapter
            layoutManager = LinearLayoutManager(
                context, LinearLayoutManager.VERTICAL, false
            )
        }
        seasonsAdapter.submitList(item.seasons)
    }

    private val mediaAdapter by lazy {
        DetailedMediaAdapter {
            listDataAdapter.listClickListener.onClickMedia(it)
        }
    }


    fun bindMedia(item: ListDataModel.Media) {
        itemBinding.rvList.apply {
            adapter = mediaAdapter
            layoutManager = LinearLayoutManager(
                context, LinearLayoutManager.VERTICAL, false
            )
        }
        mediaAdapter.submitList(item.media)
    }


    private val castAdapter by lazy {
        CastAdapter {
            listDataAdapter.listClickListener.onClickCast(it)
        }
    }

    fun bindCasts(item: ListDataModel.Casts) {

        itemBinding.rvList.apply {
            adapter = castAdapter
            layoutManager = object : GridLayoutManager(
                context, 3
            ) {
                override fun checkLayoutParams(
                    lp: RecyclerView.LayoutParams?
                ) = lp?.let {
                    it.width = (0.30 * width).toInt()
                    true
                } ?: super.checkLayoutParams(lp)
            }
        }
        castAdapter.submitList(item.casts)
    }

    private val videoAdapter by lazy {
        VideoAdapter {
            listDataAdapter.listClickListener.onClickVideo(it)
        }
    }


    fun bindVideo(item: ListDataModel.Videos) {
        itemBinding.rvList.apply {
            adapter = videoAdapter
            layoutManager = object : LinearLayoutManager(
                context, VERTICAL, false
            ) {
                override fun checkLayoutParams(
                    lp: RecyclerView.LayoutParams?
                ) = lp?.let {
                    it.bottomMargin = 50
                    true
                } ?: super.checkLayoutParams(lp)
            }
        }
        videoAdapter.submitList(item.videos)
    }

}