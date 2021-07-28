package zechs.zplex.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.File;
import java.io.FileReader;
import java.net.IDN;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import zechs.zplex.FetchDatabaseDialog;
import zechs.zplex.R;
import zechs.zplex.adapter.CardAdapter;
import zechs.zplex.adapter.CardItem;

import static zechs.zplex.utils.Constants.ZPlex;

public class MainActivity extends AppCompatActivity {

    final ArrayList<CardItem> mediaItems = new ArrayList<>();
    CardAdapter mediaAdapter;
    RecyclerView mediaView;
    ProgressBar loadingHome;
    ViewGroup rootView;
    String text = "";
    File dbJson;
    FetchDatabaseDialog fetchDatabaseDialog;
    SearchView searchView;
    TabLayout tabLayout;
    Toolbar toolbar;
    View errorView;
    TextView errorText;
    MaterialButton noResultsRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootView = findViewById(R.id.root);

        dbJson = new File(getFilesDir() + "/dbJson.json");
        errorView = findViewById(R.id.error_view);
        errorText = findViewById(R.id.error_txt);
        noResultsRetry = findViewById(R.id.retry_btn);
        noResultsRetry.setVisibility(View.GONE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadingHome = findViewById(R.id.loading_home);
        mediaView = findViewById(R.id.media_view);

        mediaAdapter = new CardAdapter(mediaItems, getApplicationContext(), MainActivity.this);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mediaView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            mediaView.setLayoutManager(new GridLayoutManager(this, 5));
        }

        mediaView.setAdapter(mediaAdapter);
        tabLayout = findViewById(R.id.tabs);

        tabLayout.addTab(tabLayout.newTab().setText("Movies"));
        tabLayout.addTab(tabLayout.newTab().setText("TV Shows"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                queryDB(text);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        errorView.setVisibility(View.GONE);

        new Thread(() -> FirebaseMessaging.getInstance().subscribeToTopic("all")).start();

        if (dbJson.exists()) {
            queryDB("");
        } else {
            fetchDatabase();
        }
    }

    private void queryDB(String query) {
        runOnUiThread(() -> {
            TransitionManager.beginDelayedTransition(rootView);
            mediaView.stopScroll();
            mediaItems.clear();
            errorView.setVisibility(View.GONE);
            loadingHome.setVisibility(View.VISIBLE);
            mediaView.setVisibility(View.GONE);
        });

        try {
            JsonParser parser = new JsonParser();
            Object dbObject = parser.parse(new FileReader(getFilesDir() + "/dbJson.json"));

            JSONArray jsonArray = new JSONArray(dbObject.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                String name = jsonArray.getJSONObject(i).getString("name");
                String type = jsonArray.getJSONObject(i).getString("type");
                if (name.toLowerCase().contains(query.toLowerCase())) {
                    String mediaType;
                    if (tabLayout.getSelectedTabPosition() == 0) {
                        mediaType = "Movie";
                    } else {
                        mediaType = "TV";
                    }
                    if (type.equals(mediaType)) {
                        String posterURL = ZPlex + name + " - " + type + "/poster.jpg";
                        URL urlPoster = new URL(posterURL);
                        URI uriPoster = new URI(urlPoster.getProtocol(), urlPoster.getUserInfo(), IDN.toASCII(urlPoster.getHost()), urlPoster.getPort(), urlPoster.getPath(), urlPoster.getQuery(), urlPoster.getRef());
                        runOnUiThread(() -> mediaItems.add(new CardItem(name, type, uriPoster.toASCIIString())));

                    }
                }
            }

            mediaAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> runOnUiThread(() -> {
            if (mediaItems.size() == 0) {
                mediaView.setVisibility(View.GONE);
                errorView.setVisibility(View.VISIBLE);
            } else {
                errorText.setText(R.string.no_results);
                errorView.setVisibility(View.GONE);
                mediaView.setVisibility(View.VISIBLE);
                mediaView.smoothScrollToPosition(0);
            }
            loadingHome.setVisibility(View.GONE);
            Log.d("queryDB", "postDelayed executed");
            if (toolbar.getParent() instanceof AppBarLayout) {
                ((AppBarLayout) toolbar.getParent()).setExpanded(true, true);
            }
        }), 500);

    }

    private void fetchDatabase() {
        Toast.makeText(getApplicationContext(), getString(R.string.updating_library), Toast.LENGTH_LONG).show();
        fetchDatabaseDialog = new FetchDatabaseDialog(MainActivity.this);
        Window dialogWindow = fetchDatabaseDialog.getWindow();
        fetchDatabaseDialog.show();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setLayout(Constraints.LayoutParams.MATCH_PARENT, Constraints.LayoutParams.MATCH_PARENT);
        fetchDatabaseDialog.setOnDismissListener(dialog -> {
            if (dbJson.exists())
                queryDB("");
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mediaView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            mediaView.setLayoutManager(new GridLayoutManager(this, 5));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem update = menu.findItem(R.id.update_library);
        update.setOnMenuItemClickListener(item -> {
            fetchDatabase();
            return false;
        });

        MenuItem clear = menu.findItem(R.id.clear_image_cache);
        clear.setOnMenuItemClickListener(item -> {
            AsyncTask.execute(() -> Glide.get(getApplicationContext()).clearDiskCache());
            Toast.makeText(getApplicationContext(), getString(R.string.clearing_image_cache), Toast.LENGTH_LONG).show();
            return false;
        });

        MenuItem repository = menu.findItem(R.id.app_update);
        repository.setOnMenuItemClickListener(item -> {
            Intent updateRepository = new Intent(Intent.ACTION_VIEW);
            updateRepository.setData(Uri.parse("https://drive.google.com/drive/folders/1WXS6bC7JBS1sxg0dYjUQp3Ql-wtCTrUe"));
            startActivity(updateRepository);
            return false;
        });

        searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> runOnUiThread(() -> {
                    text = query;
                    queryDB(text);
                }), 150);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> runOnUiThread(() -> {
                if (!text.equals("")) {
                    text = "";
                    queryDB(text);
                }
            }), 150);
            return false;
        });

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setHint("Search...");
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.textColor_87));
        searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.textColor_54));

        int searchImgId = androidx.appcompat.R.id.search_button;
        ImageView searchImg = searchView.findViewById(searchImgId);
        searchImg.setImageResource(R.drawable.ic_search);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }
}