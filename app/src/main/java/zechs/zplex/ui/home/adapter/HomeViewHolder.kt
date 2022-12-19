package zechs.zplex.ui.home.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import zechs.zplex.databinding.ItemHeadingBinding
import zechs.zplex.databinding.ItemListBinding
import zechs.zplex.ui.home.adapter.watched.WatchedDataAdapter
import zechs.zplex.ui.shared_adapters.banner.BannerAdapter
import zechs.zplex.ui.shared_adapters.media.MediaAdapter

sealed class HomeViewHolder(
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    class HeadingViewHolder(
        private val itemBinding: ItemHeadingBinding
    ) : HomeViewHolder(itemBinding) {
        fun bind(item: HomeDataModel.Header) {
            itemBinding.tvText.text = item.heading
        }
    }

    class ListViewHolder(
        private val itemBinding: ItemListBinding,
        homeDataAdapter: HomeDataAdapter
    ) : HomeViewHolder(itemBinding) {

        private val mediaAdapter by lazy {
            MediaAdapter {
                homeDataAdapter.homeClickListener.onClickMedia(it)
            }
        }

        private val bannerAdapter by lazy {
            BannerAdapter {
                homeDataAdapter.homeClickListener.onClickMedia(it)
            }
        }

        private val watchedAdapter by lazy {
            WatchedDataAdapter {
                homeDataAdapter.homeClickListener.onClickWatched(it)
            }
        }

        fun bindMedia(item: HomeDataModel.Media) {
            itemBinding.rvList.apply {
                adapter = mediaAdapter
                layoutManager = object : LinearLayoutManager(
                    context, HORIZONTAL, false
                ) {
                    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                        return lp?.let {
                            it.width = (0.28 * width).toInt()
                            true
                        } ?: super.checkLayoutParams(lp)
                    }
                }
            }

            mediaAdapter.submitList(item.media)
        }

        fun bindWatched(item: HomeDataModel.Watched) {
            itemBinding.rvList.apply {
                adapter = watchedAdapter
                layoutManager = object : LinearLayoutManager(
                    context, HORIZONTAL, false
                ) {
                    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                        return lp?.let {
                            it.width = (0.33 * width).toInt()
                            true
                        } ?: super.checkLayoutParams(lp)
                    }
                }
            }

            watchedAdapter.differ.submitList(item.watched)
        }

        fun bindBanner(item: HomeDataModel.Banner) {

            itemBinding.rvList.apply {
                adapter = bannerAdapter
                layoutManager = LinearLayoutManager(
                    context, LinearLayoutManager.HORIZONTAL, false
                )
            }
            bannerAdapter.submitList(item.media)
        }
    }


}