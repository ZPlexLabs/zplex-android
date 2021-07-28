package zechs.zplex.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import zechs.zplex.R
import zechs.zplex.activity.AboutActivity
import java.util.*

class CardItem(var name: String, var type: String, var posterURL: String)

class CardAdapter(
    private val cardItems: ArrayList<CardItem>,
    private val context: Context,
    private val activity: Activity
) : RecyclerView.Adapter<CardAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.media_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardItem = cardItems[position]
        Glide.with(context)
            .load(cardItem.posterURL)
            .placeholder(R.color.cardColor)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.posterView)
    }

    override fun getItemCount(): Int {
        return cardItems.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var posterView: ImageView = view.findViewById(R.id.item_poster)
        override fun onClick(view: View) {
            val position = layoutPosition
            val cardItem = cardItems[position]
            val intent = Intent(activity, AboutActivity::class.java)
            intent.putExtra("NAME", cardItem.name)
            intent.putExtra("TYPE", cardItem.type)
            intent.putExtra("POSTERURL", cardItem.posterURL)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
        }

        init {
            view.setOnClickListener(this)
        }
    }
}