package zechs.zplex.adapter.home

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import zechs.zplex.adapter.home.adapter.banner.BannerAdapter
import zechs.zplex.adapter.home.adapter.media.MediaAdapter
import zechs.zplex.adapter.watched.WatchedDataAdapter
import zechs.zplex.databinding.ItemHeadingBinding
import zechs.zplex.databinding.ItemList2Binding
import zechs.zplex.databinding.ItemList3Binding
import zechs.zplex.databinding.ItemListBinding

sealed class HomeViewHolder(
    val context: Context,
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    class HeadingViewHolder(
        context: Context,
        private val itemBinding: ItemHeadingBinding
    ) : HomeViewHolder(context, itemBinding) {
        fun bind(item: HomeDataModel.Header) {
            itemBinding.tvText.text = item.heading
        }
    }

    class MediaViewHolder(
        context: Context,
        private val itemBinding: ItemListBinding,
        homeDataAdapter: HomeDataAdapter
    ) : HomeViewHolder(context, itemBinding) {

        private val mediaAdapter by lazy {
            MediaAdapter { homeDataAdapter.homeOnClick.invoke(it) }
        }

        fun bind(item: HomeDataModel.Media) {
            val linearLayoutManager = object : LinearLayoutManager(
                context, HORIZONTAL, false
            ) {
                override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                    return lp?.let {
                        it.width = (0.28 * width).toInt()
                        true
                    } ?: super.checkLayoutParams(lp)
                }
            }

            itemBinding.rvList.apply {
                adapter = mediaAdapter
                layoutManager = linearLayoutManager
            }

            mediaAdapter.differ.submitList(item.media)
        }
    }

    class BannerViewHolder(
        context: Context,
        private val itemBinding: ItemList2Binding,
        homeDataAdapter: HomeDataAdapter
    ) : HomeViewHolder(context, itemBinding) {

        private val bannerAdapter by lazy {
            BannerAdapter { homeDataAdapter.homeOnClick.invoke(it) }
        }

        fun bind(item: HomeDataModel.Banner) {
            val linearLayoutManager = object : LinearLayoutManager(
                context, HORIZONTAL, false
            ) {
                override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                    return lp?.let {
                        it.width = (0.75 * width).toInt()
                        true
                    } ?: super.checkLayoutParams(lp)
                }
            }

            itemBinding.rvList.apply {
                adapter = bannerAdapter
                layoutManager = linearLayoutManager
            }
            bannerAdapter.differ.submitList(item.media)
        }
    }

    class WatchedViewHolder(
        context: Context,
        private val itemBinding: ItemList3Binding,
        homeDataAdapter: HomeDataAdapter
    ) : HomeViewHolder(context, itemBinding) {

        private val watchedAdapter by lazy {
            WatchedDataAdapter {
                homeDataAdapter.watchedOnClick.invoke((it))
            }
        }

        fun bind(item: HomeDataModel.Watched) {
            val linearLayoutManager = object : LinearLayoutManager(
                context, HORIZONTAL, false
            ) {
                override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                    return lp?.let {
                        it.width = (0.33 * width).toInt()
                        true
                    } ?: super.checkLayoutParams(lp)
                }
            }

            itemBinding.rvList.apply {
                adapter = watchedAdapter
                layoutManager = linearLayoutManager
            }
            watchedAdapter.differ.submitList(item.watched)
        }
    }

}