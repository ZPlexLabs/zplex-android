package zechs.zplex.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileReader;
import java.net.IDN;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import zechs.zplex.FetchDatabaseDialog;
import zechs.zplex.R;
import zechs.zplex.adapter.CardAdapter;
import zechs.zplex.adapter.CardItem;

import static zechs.zplex.utils.Constants.ZPlex;

public class MainActivity extends AppCompatActivity {

    final ArrayList<CardItem> movieItems = new ArrayList<>();
    final ArrayList<CardItem> tvItems = new ArrayList<>();
    CardAdapter movieAdapter;
    CardAdapter tvAdapter;
    RecyclerView MoviesView, TvView;
    TextView expandMovies, expandTv;
    ProgressBar loadingHome;
    ViewGroup rootView;
    String text = "";
    File dbJson;
    JSONArray jsonArray;
    FetchDatabaseDialog fetchDatabaseDialog;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootView = findViewById(R.id.root);

        dbJson = new File(getFilesDir() + "/dbJson.json");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadingHome = findViewById(R.id.loading_home);
        expandMovies = findViewById(R.id.expand_movies);
        expandTv = findViewById(R.id.expand_tv);
        MoviesView = findViewById(R.id.horz_movies);
        TvView = findViewById(R.id.horz_tv);

        expandMovies.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MediaActivity.class);
            intent.putExtra("TYPE", "Movies");
            intent.putExtra("QUERY", text);
            intent.putExtra("JSON", jsonArray.toString());
            startActivity(intent);
        });

        expandTv.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MediaActivity.class);
            intent.putExtra("TYPE", "TV Shows");
            intent.putExtra("QUERY", text);
            intent.putExtra("JSON", jsonArray.toString());
            startActivity(intent);
        });

        int horizontalItems = getResources().getIdentifier("items", "layout", getPackageName());

        movieAdapter = new CardAdapter(movieItems, getApplicationContext(), horizontalItems);
        MoviesView.setAdapter(movieAdapter);

        tvAdapter = new CardAdapter(tvItems, getApplicationContext(), horizontalItems);
        TvView.setAdapter(tvAdapter);

        if (dbJson.exists()) {
            queryDB("");
        } else {
            fetchDatabase();
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
                TransitionManager.beginDelayedTransition(rootView);
                text = query;
                queryDB(text);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            TransitionManager.beginDelayedTransition(rootView);
            if (!text.equals("")) {
                text = "";
                queryDB(text);
            }
            return false;
        });

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setHint("Search...");
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.textColor));
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

    private void queryDB(String query) {
        movieItems.clear();
        tvItems.clear();
        loadingHome.setVisibility(View.VISIBLE);
        MoviesView.setVisibility(View.GONE);
        TvView.setVisibility(View.GONE);
        try {
            JsonParser parser = new JsonParser();
            Object dbObject = parser.parse(new FileReader(getFilesDir() + "/dbJson.json"));

            if (query.equals("")) {
                jsonArray = shuffleJsonArray(new JSONArray(dbObject.toString()));
            } else {
                jsonArray = new JSONArray(dbObject.toString());
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                String name = jsonArray.getJSONObject(i).getString("name");
                String type = jsonArray.getJSONObject(i).getString("type");
                if (name.toLowerCase().contains(query.toLowerCase())) {
                    if (type.equals("Movie")) {
                        if (movieItems.size() <= 8) {
                            String posterURL = ZPlex + name + " - " + type + "/poster.jpg";
                            URL urlPoster = new URL(posterURL);
                            URI uriPoster = new URI(urlPoster.getProtocol(), urlPoster.getUserInfo(), IDN.toASCII(urlPoster.getHost()), urlPoster.getPort(), urlPoster.getPath(), urlPoster.getQuery(), urlPoster.getRef());
                            runOnUiThread(() -> movieItems.add(new CardItem(name, type, uriPoster.toASCIIString())));
                        }
                    } else if (type.equals("TV")) {
                        if (tvItems.size() <= 8) {
                            String posterURL = ZPlex + name + " - " + type + "/poster.jpg";
                            URL urlPoster = new URL(posterURL);
                            URI uriPoster = new URI(urlPoster.getProtocol(), urlPoster.getUserInfo(), IDN.toASCII(urlPoster.getHost()), urlPoster.getPort(), urlPoster.getPath(), urlPoster.getQuery(), urlPoster.getRef());
                            runOnUiThread(() -> tvItems.add(new CardItem(name, type, uriPoster.toASCIIString())));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadingHome.setVisibility(View.GONE);
        MoviesView.setVisibility(View.VISIBLE);
        TvView.setVisibility(View.VISIBLE);
        movieAdapter.notifyDataSetChanged();
        tvAdapter.notifyDataSetChanged();
    }

    public JSONArray shuffleJsonArray(JSONArray array) throws JSONException {
        Random rnd = new Random();
        for (int i = array.length() - 1; i >= 0; i--) {
            int j = rnd.nextInt(i + 1);
            Object object = array.get(j);
            array.put(j, array.get(i));
            array.put(i, object);
        }
        return array;
    }

}