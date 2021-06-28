package zechs.zplex.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.net.IDN;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import zechs.zplex.R;
import zechs.zplex.adapter.CardAdapter;
import zechs.zplex.adapter.CardItem;

import static zechs.zplex.utils.Constants.ZPlex;

public class MediaActivity extends AppCompatActivity {

    final ArrayList<CardItem> mediaItems = new ArrayList<>();
    String TYPE, JSON, QUERY;
    CardAdapter mediaAdapter;
    RecyclerView mediaView;
    ProgressBar loadingMedia;
    JSONArray jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        TYPE = getIntent().getStringExtra("TYPE");
        QUERY = (getIntent().getStringExtra("QUERY"));
        JSON = (getIntent().getStringExtra("JSON"));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_back));
        }
        toolbar.setTitle(TYPE);

        loadingMedia = findViewById(R.id.loading_media);
        mediaView = findViewById(R.id.media_view);
        int horizontalItems = getResources().getIdentifier("media_item", "layout", getPackageName());

        mediaAdapter = new CardAdapter(mediaItems, getApplicationContext(), horizontalItems);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mediaView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            mediaView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        mediaView.setAdapter(mediaAdapter);

        if (TYPE.equals("Movies")) {
            queryDB(QUERY, "Movie");
        } else {
            queryDB(QUERY, "TV");
        }
    }

    private void queryDB(String query, String mediaType) {
        mediaItems.clear();
        loadingMedia.setVisibility(View.VISIBLE);
        mediaView.setVisibility(View.GONE);
        try {
            jsonArray = new JSONArray(JSON);

            for (int i = 0; i < jsonArray.length(); i++) {
                String name = jsonArray.getJSONObject(i).getString("name");
                String type = jsonArray.getJSONObject(i).getString("type");
                if (name.toLowerCase().contains(query.toLowerCase())) {
                    if (type.equals(mediaType)) {
                        String posterURL = ZPlex + name + " - " + type + "/poster.jpg";
                        URL urlPoster = new URL(posterURL);
                        URI uriPoster = new URI(urlPoster.getProtocol(), urlPoster.getUserInfo(), IDN.toASCII(urlPoster.getHost()), urlPoster.getPort(), urlPoster.getPath(), urlPoster.getQuery(), urlPoster.getRef());
                        runOnUiThread(() -> mediaItems.add(new CardItem(name, type, uriPoster.toASCIIString())));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadingMedia.setVisibility(View.GONE);
        mediaView.setVisibility(View.VISIBLE);
        mediaAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mediaView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            mediaView.setLayoutManager(new GridLayoutManager(this, 4));
        }
    }
}