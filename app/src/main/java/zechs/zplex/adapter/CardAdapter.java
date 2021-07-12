package zechs.zplex.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import zechs.zplex.R;
import zechs.zplex.activity.AboutActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private final ArrayList<CardItem> cardItems;
    private final Context context;
    private final Activity activity;

    public CardAdapter(ArrayList<CardItem> cardItems, Context context, Activity activity) {
        this.cardItems = cardItems;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardAdapter.ViewHolder holder, int position) {
        CardItem cardItem = cardItems.get(position);
        Glide.with(context)
                .load(cardItem.getPosterURL())
                .placeholder(R.color.cardColor)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.posterView);
    }

    @Override
    public int getItemCount() {
        return cardItems.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView posterView;

        public ViewHolder(@NonNull View view) {
            super(view);
            posterView = view.findViewById(R.id.item_poster);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();
            CardItem cardItem = cardItems.get(position);
            Intent intent = new Intent(activity, AboutActivity.class);
            intent.putExtra("NAME", cardItem.getName());
            intent.putExtra("TYPE", cardItem.getType());
            intent.putExtra("POSTERURL", cardItem.getPosterURL());
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_up, R.anim.no_animation);
        }
    }
}