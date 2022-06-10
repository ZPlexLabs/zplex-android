package zechs.zplex.adapter.cast

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import zechs.zplex.R
import zechs.zplex.adapter.shared_adapters.media.MediaAdapter
import zechs.zplex.databinding.ItemCastHeaderBinding
import zechs.zplex.databinding.ItemCastMetaBinding
import zechs.zplex.databinding.ItemHeadingBinding
import zechs.zplex.databinding.ItemListBinding
import zechs.zplex.models.tmdb.ProfileSize
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.GlideApp


sealed class CastViewHolder(
    val context: Context,
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    class HeadingViewHolder(
        context: Context,
        private val itemBinding: ItemHeadingBinding
    ) : CastViewHolder(context, itemBinding) {
        fun bind(item: CastDataModel.Heading) {
            itemBinding.tvText.text = item.heading
        }
    }

    class HeaderViewHolder(
        context: Context,
        private val itemBinding: ItemCastHeaderBinding,
        private val castDataAdapter: CastDataAdapter
    ) : CastViewHolder(context, itemBinding) {

        fun bind(item: CastDataModel.Header) {
            val posterUrl = if (item.profilePath == null) R.drawable.no_poster else {
                "$TMDB_IMAGE_PREFIX/${ProfileSize.h632}${item.profilePath}"
            }
            itemBinding.apply {
                GlideApp.with(ivPoster)
                    .load(posterUrl)
                    .placeholder(R.drawable.no_poster)
                    .into(ivPoster)
                tvName.text = item.name

                val lineLimit = 7 - tvName.lineCount

                val biography = if (item.biography == null || item.biography == "") {
                    "No biography available"
                } else item.biography
                tvBiography.text = biography
                tvBiography.maxLines = lineLimit
                tvBiography.setOnClickListener {
                    castDataAdapter.expandBiography.invoke(biography)
                }

            }
        }
    }

    class MetaViewHolder(
        context: Context,
        private val itemBinding: ItemCastMetaBinding
    ) : CastViewHolder(context, itemBinding) {

        private fun addChip(
            text: String,
            context: Context,
            drawable: Int,
            root: ViewGroup,
            chipGroup: ChipGroup
        ) {
            val layoutInflater = context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
            ) as LayoutInflater

            val mChip = layoutInflater.inflate(
                R.layout.ic_cast_chip,
                root,
                false
            ) as Chip

            mChip.text = text
            mChip.chipIcon = ContextCompat.getDrawable(context, drawable)
            chipGroup.addView(mChip)
        }

        fun bind(item: CastDataModel.Meta) {

            fun add(text: String, drawable: Int) {
                addChip(
                    text, context, drawable,
                    itemBinding.root, itemBinding.chipGroup
                )
            }

            itemBinding.apply {
                item.age?.let {
                    add("$it years old", R.drawable.ic_person_24)
                }

                val genderIcon = when (item.gender) {
                    0 -> R.drawable.ic_transgender_24dp
                    1 -> R.drawable.ic_female_24dp
                    2 -> R.drawable.ic_male_24dp
                    else -> R.drawable.ic_transgender_24dp
                }
                add(item.genderName, genderIcon)

                item.birthday?.let {
                    add("Born in ${it.take(4)}", R.drawable.ic_child_24)
                }

                item.death?.let {
                    add("Died in ${it.take(4)}", R.drawable.ic_face_sad_24)
                }

                item.place_of_birth?.let { add(it, R.drawable.ic_place_24) }
            }
        }
    }


    class ListViewHolder(
        context: Context,
        private val itemBinding: ItemListBinding,
        castDataAdapter: CastDataAdapter
    ) : CastViewHolder(context, itemBinding) {

        private val mediaAdapter by lazy {
            MediaAdapter(rating = true) {
                castDataAdapter.setOnClickListener.invoke(it)
            }
        }

        fun bindAppearedIn(item: CastDataModel.AppearsIn) {
            val gridLayoutManager = object : GridLayoutManager(
                context, 3
            ) {
                override fun checkLayoutParams(
                    lp: RecyclerView.LayoutParams?
                ) = lp?.let {
                    it.width = (0.30 * width).toInt()
                    true
                } ?: super.checkLayoutParams(lp)
            }

            itemBinding.rvList.apply {
                adapter = mediaAdapter
                layoutManager = gridLayoutManager
            }

            mediaAdapter.submitList(item.appearsIn)
        }
    }
}