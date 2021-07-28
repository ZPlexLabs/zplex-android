package zechs.zplex.adapter;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import zechs.zplex.R;
import zechs.zplex.utils.ConverterUtils;

import static android.content.Context.DOWNLOAD_SERVICE;


public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.ViewHolder> {

    private final ArrayList<EpisodeItem> episodeItems;
    private final Context context;

    public EpisodeAdapter(ArrayList<EpisodeItem> episodeItems, Context context) {
        this.episodeItems = episodeItems;
        this.context = context;
    }

    @NonNull
    @Override
    public EpisodeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.episode_list, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull EpisodeAdapter.ViewHolder holder, int position) {
        EpisodeItem episodeItem = episodeItems.get(position);
        holder.episodeCount.setText("Episode " + Integer.parseInt(episodeItem.getEpisode().substring(4)));
        holder.episodeName.setText(episodeItem.getEpisodeTitle());

        holder.offlineEp.setOnClickListener(v -> {
            if (context != null) {
                DownloadManager.Request dlRequest = new DownloadManager.Request(Uri.parse(episodeItem.getPlayUrl()));
                String fileName = episodeItem.getShow() + "/" + episodeItem.getEpisode() + " - " + episodeItem.getEpisodeTitle() + ".mkv";
                dlRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                dlRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                dlRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                dlRequest.setMimeType("video/x-matroska");
                DownloadManager dlManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                dlManager.enqueue(dlRequest);
                String size = ConverterUtils.Companion.getSize(Long.parseLong(episodeItem.getBytes()));
                Toast.makeText(context, "Download started (" + size + ")", Toast.LENGTH_SHORT).show();
            }
        });

        Glide.with(context)
                .load(episodeItem.getPlayUrl().substring(0, episodeItem.getPlayUrl().length() - 3) + "jpg")
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.thumbImage);
    }

    @Override
    public int getItemCount() {
        return episodeItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView episodeCount, episodeName;
        AppCompatImageButton offlineEp;
        ImageView thumbImage;

        public ViewHolder(@NonNull View view) {
            super(view);
            offlineEp = view.findViewById(R.id.offline);
            episodeCount = view.findViewById(R.id.episode_count);
            episodeName = view.findViewById(R.id.episode_title);
            thumbImage = view.findViewById(R.id.thumb);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();
            EpisodeItem episodeItem = episodeItems.get(position);
            try {
                Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
                vlcIntent.setPackage("org.videolan.vlc");
                vlcIntent.setComponent(new ComponentName("org.videolan.vlc", "org.videolan.vlc.gui.video.VideoPlayerActivity"));
                vlcIntent.setDataAndTypeAndNormalize(Uri.parse(episodeItem.getPlayUrl()), "video/*");
                vlcIntent.putExtra("title", episodeItem.getEpisode() + " - " + episodeItem.getEpisodeTitle());
                vlcIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                context.startActivity(vlcIntent);
            } catch (ActivityNotFoundException notFoundException) {
                notFoundException.printStackTrace();
                Toast.makeText(context, "VLC not found, Install VLC from Play Store", Toast.LENGTH_LONG).show();
            }
        }
    }
}