package zechs.zplex.activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import zechs.zplex.R;
import zechs.zplex.adapter.EpisodeAdapter;
import zechs.zplex.adapter.EpisodeItem;
import zechs.zplex.utils.APIHolder;

import static zechs.zplex.utils.Constants.API;
import static zechs.zplex.utils.Constants.ZPlex;

public class AboutActivity extends AppCompatActivity {

    final ArrayList<EpisodeItem> episodeItems = new ArrayList<>();
    EpisodeAdapter episodeAdapter;
    RecyclerView episodesView;
    String TYPE, NAME, POSTERURL;
    ImageView Poster;
    TextView Title, Year_MPAA, Genre, Plot;
    FloatingActionButton playButton;
    NestedScrollView nestedScrollView;
    MaterialCardView episodesCard;
    Group Details;
    ProgressBar loadingAbout, loadingEpisodes;
    Call<ResponseBody> call, call2;
    ConstraintLayout rootView;
    MaterialButton retryEpisodes;
    View errorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TYPE = getIntent().getStringExtra("TYPE");
        NAME = getIntent().getStringExtra("NAME");
        POSTERURL = getIntent().getStringExtra("POSTERURL");
        rootView = findViewById(R.id.root_layout);
        errorView = findViewById(R.id.error_view);
        errorView.setVisibility(View.GONE);

        loadingAbout = findViewById(R.id.loading_about);
        retryEpisodes = findViewById(R.id.retry_episodes);
        retryEpisodes.setVisibility(View.GONE);

        loadingEpisodes = findViewById(R.id.loading_episodes);
        Details = findViewById(R.id.details);
        Poster = findViewById(R.id.item_poster);
        Title = findViewById(R.id.item_title);
        Year_MPAA = findViewById(R.id.year_mpaa);
        Genre = findViewById(R.id.genre);
        Plot = findViewById(R.id.plot);
        playButton = findViewById(R.id.play_btn);
        nestedScrollView = findViewById(R.id.about_scroll_view);
        episodesCard = findViewById(R.id.tv_episodes);

        episodesView = findViewById(R.id.episodes_view);
        episodesView.setHasFixedSize(true);
        episodeAdapter = new EpisodeAdapter(episodeItems, getApplicationContext());
        episodesView.setAdapter(episodeAdapter);

        Title.setText(NAME);
        Glide.with(getApplicationContext())
                .load(POSTERURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .override(Target.SIZE_ORIGINAL)
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(getApplicationContext(), "Couldn't fetch poster image", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        int scrollToTitle = ((View) Title.getParent().getParent()).getTop() + Title.getTop();
                        nestedScrollView.scrollTo(0, scrollToTitle);
                        return false;
                    }
                })
                .into(Poster);


        Details.setVisibility(View.GONE);
        loadingAbout.setVisibility(View.VISIBLE);
        episodesView.setVisibility(View.GONE);
        loadingEpisodes.setVisibility(View.VISIBLE);
        episodesCard.setVisibility(View.GONE);

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!nestedScrollView.canScrollVertically(1))
                nestedScrollView.setPadding(0, 0, 0, getNavigationBarHeight() + 8);
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        APIHolder randomAPI = retrofit.create(APIHolder.class);
        call = randomAPI.getInfo(TYPE, NAME);
        call.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    errorView.setVisibility(View.VISIBLE);
                    rootView.setVisibility(View.GONE);
                } else {
                    try {
                        if (response.body() != null) {
                            errorView.setVisibility(View.GONE);
                            rootView.setVisibility(View.VISIBLE);

                            TransitionManager.beginDelayedTransition(rootView);
                            String JsonRequest = response.body().string();
                            JSONObject jsonObject = new JSONObject(JsonRequest);
                            JSONObject root;
                            if (TYPE.equals("TV")) {
                                root = jsonObject.getJSONObject("tvshow");
                            } else {
                                root = jsonObject.getJSONObject("movie");
                            }

                            String genre = root.getString("genre");
                            String mpaa = root.getString("mpaa");
                            if (mpaa.equals("null")) {
                                Year_MPAA.setText(root.getString("year"));
                            } else {
                                Year_MPAA.setText(root.getString("year") + getString(R.string.divider) + root.getString("mpaa"));
                            }
                            Genre.setText((((String.join(", ", genre).replace("\"", "")).replace(",", ", ")).replace("[", "")).replace("]", ""));
                            Plot.setText(root.getString("plot"));
                            Details.setVisibility(View.VISIBLE);
                            loadingAbout.setVisibility(View.GONE);
                            if (TYPE.equals("TV")) {
                                episodesCard.setVisibility(View.VISIBLE);
                                playButton.setOnClickListener(v -> {
                                    int scrollToEpisodes = ((View) episodesCard.getParent().getParent()).getTop() + episodesCard.getTop();
                                    nestedScrollView.smoothScrollTo(0, scrollToEpisodes);
                                });

                                Retrofit retrofit1 = new Retrofit.Builder()
                                        .baseUrl(API)
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build();
                                APIHolder episodesAPI = retrofit1.create(APIHolder.class);
                                call2 = episodesAPI.getEpisodes(TYPE, NAME);
                                call2.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                                        if (!response.isSuccessful()) {
                                            retryEpisodes.setVisibility(View.VISIBLE);
                                            Toast.makeText(getApplicationContext(), getString(R.string.failed_to_load_episodes), Toast.LENGTH_SHORT).show();
                                        } else {
                                            try {
                                                retryEpisodes.setVisibility(View.GONE);
                                                if (response.body() != null) {
                                                    String JsonRequest = response.body().string();
                                                    JSONObject jsonObject = new JSONObject(JsonRequest);
                                                    JSONArray jsonArray = jsonObject.getJSONArray("episodes");
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        String file = jsonArray.getJSONObject(i).getString("file");
                                                        String episode = (file.split(" - ", 2))[0];
                                                        String title = (file.split(" - ", 2))[1];
                                                        String bytes = jsonArray.getJSONObject(i).getString("bytes");

                                                        String playURL = ZPlex + NAME + " - " + TYPE + "/" + file;
                                                        URL url = new URL(playURL);
                                                        URI uri = new URI(url.getProtocol(), url.getUserInfo(), IDN.toASCII(url.getHost()), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                                                        episodeItems.add(new EpisodeItem(NAME, episode, title.substring(0, title.length() - 4), uri.toASCIIString(), bytes));
                                                    }
                                                }
                                            } catch (IOException | JSONException | URISyntaxException e) {
                                                e.printStackTrace();
                                                Toast.makeText(getApplicationContext(), "IOException | JSONException e", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        episodesView.setVisibility(View.VISIBLE);
                                        loadingEpisodes.setVisibility(View.GONE);
                                        episodeAdapter.notifyDataSetChanged();
                                    }


                                    @Override
                                    public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                                        retryEpisodes.setVisibility(View.VISIBLE);
                                        if (!call2.isCanceled())
                                            Toast.makeText(getApplicationContext(), getString(R.string.failed_to_load_episodes), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                try {
                                    String playURL = ZPlex + NAME + " - " + TYPE + "/" + NAME + ".mkv";
                                    URL url = new URL(playURL);
                                    URI uri = new URI(url.getProtocol(), url.getUserInfo(), IDN.toASCII(url.getHost()), url.getPort(), url.getPath(), url.getQuery(), url.getRef());

                                    playButton.setOnClickListener(v -> {
                                        try {
                                            Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
                                            vlcIntent.setPackage("org.videolan.vlc");
                                            vlcIntent.setComponent(new ComponentName("org.videolan.vlc", "org.videolan.vlc.gui.video.VideoPlayerActivity"));
                                            vlcIntent.setDataAndTypeAndNormalize(Uri.parse(uri.toASCIIString()), "video/*");
                                            vlcIntent.putExtra("title", NAME);
                                            vlcIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                                            startActivity(vlcIntent, bundle);
                                        } catch (ActivityNotFoundException notFoundException) {
                                            notFoundException.printStackTrace();
                                            Toast.makeText(getApplicationContext(), getString(R.string.vlc_not_found), Toast.LENGTH_LONG).show();
                                        }
                                    });

                                    playButton.setOnLongClickListener(v -> {
                                        DownloadManager.Request dlRequest = new DownloadManager.Request(Uri.parse(uri.toASCIIString()));
                                        String fileName = NAME + ".mkv";
                                        dlRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                                        dlRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                                        dlRequest.setMimeType("video/x-matroska");
                                        DownloadManager dlManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                        dlManager.enqueue(dlRequest);
                                        Toast.makeText(getApplicationContext(), "Download started", Toast.LENGTH_SHORT).show();
                                        return false;
                                    });

                                } catch (MalformedURLException | URISyntaxException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "IOException | JSONException e", Toast.LENGTH_SHORT).show();
                    }
                }
            }


            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                loadingAbout.setVisibility(View.GONE);
                rootView.setVisibility(View.GONE);
                errorView.setVisibility(View.VISIBLE);
                if (!call.isCanceled())
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_to_fetch_details), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null)
            call.cancel();
        if (call2 != null)
            call2.cancel();
    }

    public int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}