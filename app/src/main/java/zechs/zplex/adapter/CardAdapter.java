package zechs.zplex.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
    private final int layoutId;

    public CardAdapter(ArrayList<CardItem> cardItems, Context context, int layoutId) {
        this.cardItems = cardItems;
        this.context = context;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardAdapter.ViewHolder holder, int position) {
        CardItem cardItem = cardItems.get(position);
        if (cardItem.getType().equals("Music")) {
            String[] nameSplit = cardItem.getName().split(" - ", 2);
            holder.animeName.setText(nameSplit[0]);
        } else {
            holder.animeName.setText(cardItem.getName());
        }


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

        TextView animeName;
        ImageView posterView;

        public ViewHolder(@NonNull View view) {
            super(view);
            animeName = view.findViewById(R.id.item_name);
            posterView = view.findViewById(R.id.item_poster);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();
            CardItem cardItem = cardItems.get(position);
            Intent intent = new Intent(context, AboutActivity.class);
            intent.putExtra("NAME", cardItem.getName());
            intent.putExtra("TYPE", cardItem.getType());
            intent.putExtra("POSTERURL", cardItem.getPosterURL());
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

}