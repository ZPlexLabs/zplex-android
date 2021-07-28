package zechs.zplex.activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;
import zechs.zplex.R;
import zechs.zplex.adapter.EpisodeAdapter;
import zechs.zplex.adapter.EpisodeItem;
import zechs.zplex.utils.GlideApp;
import zechs.zplex.utils.LXStringRequest;

import static zechs.zplex.utils.Constants.ZPlex;

public class AboutActivity extends AppCompatActivity {

    public static final String TAG = "AboutActivity";
    final ArrayList<EpisodeItem> episodeItems = new ArrayList<>();
    EpisodeAdapter episodeAdapter;
    RecyclerView episodesView;
    String TYPE, NAME, POSTERURL;
    ImageView Poster;
    TextView Title, Year_MPAA, Genre, Plot;
    MaterialButton playButton, downloadButton, retryButton, retryEpisodes;
    NestedScrollView nestedScrollView;
    ProgressBar loadingAbout, loadingEpisodes;
    ViewGroup rootView;
    View errorView;
    TabLayout tabLayout;
    FrameLayout frameLayout;
    MaterialCardView posterCard;
    LXStringRequest stringRequest;
    RequestQueue requestQueue;
    JsonArrayRequest showArrayRequest;

    @ColorInt
    public static int getContrastColor(@ColorInt int color) {
        double a = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return a < 0.5 ? Color.BLACK : Color.WHITE;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent", "Intent received");
        TYPE = intent.getStringExtra("TYPE");
        NAME = intent.getStringExtra("NAME");
        POSTERURL = intent.getStringExtra("POSTERURL");
        TransitionManager.beginDelayedTransition(rootView);
        Poster.setImageResource(0);
        Title.setText("");
        Year_MPAA.setText("");
        Genre.setText("");
        Plot.setText("");
        tabLayout.removeAllTabs();
        episodeItems.clear();
        getDetails();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TYPE = getIntent().getStringExtra("TYPE");
        NAME = getIntent().getStringExtra("NAME");
        POSTERURL = getIntent().getStringExtra("POSTERURL");
        init();
        getDetails();
    }

    private void init() {
        rootView = findViewById(R.id.root);

        frameLayout = findViewById(R.id.frameLayout);
        loadingAbout = findViewById(R.id.loading_media);
        nestedScrollView = findViewById(R.id.main_view);
        errorView = findViewById(R.id.error_view);
        posterCard = findViewById(R.id.item_poster);

        Poster = findViewById(R.id.poster_view);
        Title = findViewById(R.id.item_title);
        Year_MPAA = findViewById(R.id.year_mpaa);
        Genre = findViewById(R.id.genre);
        Plot = findViewById(R.id.plot);
        playButton = findViewById(R.id.play_btn);
        loadingEpisodes = findViewById(R.id.loading_episodes);
        downloadButton = findViewById(R.id.down_btn);

        tabLayout = findViewById(R.id.tabs);

        episodesView = findViewById(R.id.episodes_view);
        retryButton = findViewById(R.id.retry_btn);
        retryButton.setOnClickListener(v -> getDetails());

        retryEpisodes = findViewById(R.id.retry_episodes);
        retryEpisodes.setOnClickListener(v -> getShow());

        ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) posterCard.getLayoutParams();
        newLayoutParams.topMargin = getStatusBarHeight() + 35;
        posterCard.setLayoutParams(newLayoutParams);

        episodesView.setHasFixedSize(true);
        episodeAdapter = new EpisodeAdapter(episodeItems, getApplicationContext());
        episodesView.setAdapter(episodeAdapter);
        episodesView.setPadding(0, 0, 0, getNavigationBarHeight() + 24);
    }

    @SuppressLint("SetTextI18n")
    private void getDetails() {
        try {
            errorView.setVisibility(View.GONE);
            nestedScrollView.setVisibility(View.VISIBLE);
            retryEpisodes.setVisibility(View.GONE);
            Title.setText(NAME);

            GlideApp.with(getApplicationContext())
                    .asBitmap()
                    .load(POSTERURL)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            Toast.makeText(getApplicationContext(), R.string.failed_to_load_poster_image, Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            if (resource != null) {
                                Palette.from(resource).generate(p -> {
                                    if (p != null) {
                                        int accent = p.getVibrantColor(p.getDominantColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));
                                        int colorPrimary = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
                                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                                new int[]{accent, colorPrimary});
                                        TransitionManager.beginDelayedTransition(rootView);
                                        frameLayout.setBackground(gradientDrawable);
                                        tabLayout.setTabTextColors(ContextCompat.getColor(getApplicationContext(), R.color.textColor_54), accent);
                                        tabLayout.setSelectedTabIndicatorColor(accent);
                                        playButton.setBackgroundColor(accent);
                                        playButton.setTextColor(getContrastColor(accent));
                                        playButton.setIconTint(ColorStateList.valueOf(getContrastColor(accent)));
                                        downloadButton.setIconTint(ColorStateList.valueOf(getContrastColor(colorPrimary)));
                                        downloadButton.setTextColor(getContrastColor(colorPrimary));
                                        loadingAbout.setIndeterminateTintList(ColorStateList.valueOf(accent));
                                        loadingEpisodes.setIndeterminateTintList(ColorStateList.valueOf(accent));
                                        downloadButton.setStrokeColor(ColorStateList.valueOf(accent));
                                        posterCard.setCardBackgroundColor(accent);
                                        retryButton.setTextColor(accent);
                                        retryEpisodes.setTextColor(accent);
                                    }
                                });
                            }
                            return false;
                        }
                    })
                    .into(Poster);


            requestQueue = Volley.newRequestQueue(this);
            String nfoURL;
            URL url;

            if (TYPE.equals("Movie")) {
                nfoURL = "https://zplex.zechs.workers.dev/0:/" + NAME + " - " + TYPE + "/movie.nfo";
            } else {
                nfoURL = "https://zplex.zechs.workers.dev/0:/" + NAME + " - " + TYPE + "/tvshow.nfo";
            }

            url = new URL(nfoURL);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), IDN.toASCII(url.getHost()), url.getPort(), url.getPath(), url.getQuery(), url.getRef());

            stringRequest = new LXStringRequest(Request.Method.GET, uri.toASCIIString(), response -> {
                XmlToJson xmlToJson = new XmlToJson.Builder(response).build();
                try {
                    JSONObject jsonObject = new JSONObject(xmlToJson.toString());

                    JSONObject root;
                    if (TYPE.equals("TV")) {
                        root = jsonObject.getJSONObject("tvshow");
                        downloadButton.setVisibility(View.GONE);
                    } else {
                        root = jsonObject.getJSONObject("movie");
                        downloadButton.setVisibility(View.VISIBLE);
                    }

                    TransitionManager.beginDelayedTransition(rootView);
                    String mpaa = root.getString("mpaa");
                    String runtime = root.getString("runtime");
                    String year = root.getString("year");
                    if (mpaa.equals("")) {
                        Year_MPAA.setText(year);
                    } else {
                        Year_MPAA.setText(year + getString(R.string.divider) + mpaa + "\n" + runtime + "min");
                    }
                    String plot = root.getString("plot");

                    StringBuilder genres = new StringBuilder();
                    JSONArray genreArray = new JSONArray(root.getString("genre"));
                    for (int i = 0; i < genreArray.length(); i++) {
                        genres.append(genreArray.getJSONObject(i).getString("content")).append(", ");
                    }
                    genres.deleteCharAt(genres.length() - 2);
                    Genre.setText(genres);
                    if (TYPE.equals("TV")) {
                        getShow();
                        if (plot.length() >= 200) {
                            Plot.setText(plot.substring(0, 200) + "...");
                            Plot.setOnClickListener(v -> {
                                TransitionManager.beginDelayedTransition(rootView);
                                Plot.setText(plot);
                            });
                        } else {
                            Plot.setText(plot);
                        }
                    } else {
                        Plot.setText(plot);
                        try {
                            URL playMovie = new URL(ZPlex + NAME + " - " + TYPE + "/" + NAME + ".mkv");
                            URI movieURI = new URI(playMovie.getProtocol(), playMovie.getUserInfo(), IDN.toASCII(playMovie.getHost()), playMovie.getPort(), playMovie.getPath(), playMovie.getQuery(), playMovie.getRef());

                            playButton.setOnClickListener(v -> {
                                try {
                                    Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
                                    vlcIntent.setPackage("org.videolan.vlc");
                                    vlcIntent.setComponent(new ComponentName("org.videolan.vlc", "org.videolan.vlc.gui.video.VideoPlayerActivity"));
                                    vlcIntent.setDataAndTypeAndNormalize(Uri.parse(movieURI.toASCIIString()), "video/*");
                                    vlcIntent.putExtra("title", NAME + " (" + year + ")");
                                    vlcIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                    Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                                    startActivity(vlcIntent, bundle);
                                } catch (ActivityNotFoundException notFoundException) {
                                    notFoundException.printStackTrace();
                                    Toast.makeText(getApplicationContext(), getString(R.string.vlc_not_found), Toast.LENGTH_LONG).show();
                                }
                            });

                            downloadButton.setOnClickListener(v -> {
                                DownloadManager.Request dlRequest = new DownloadManager.Request(Uri.parse(movieURI.toASCIIString()));
                                String fileName = NAME + " (" + year + ").mkv";
                                dlRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                                dlRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                                dlRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                                dlRequest.setMimeType("video/x-matroska");
                                DownloadManager dlManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                dlManager.enqueue(dlRequest);
                                Toast.makeText(getApplicationContext(), "Download started", Toast.LENGTH_SHORT).show();
                            });

                        } catch (MalformedURLException | URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    errorView.setVisibility(View.VISIBLE);
                    nestedScrollView.setVisibility(View.GONE);
                }

                Log.d("nfoVolleySuccess", "executed");
            }, error -> {
                errorView.setVisibility(View.VISIBLE);
                nestedScrollView.setVisibility(View.GONE);
                Log.d("nfoVolleyError", Arrays.toString(error.getStackTrace()));
            });

            stringRequest.setTag(TAG);
            stringRequest.setShouldCache(false);
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        } catch (MalformedURLException | URISyntaxException e) {
            errorView.setVisibility(View.VISIBLE);
            nestedScrollView.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    private void getShow() {
        loadingEpisodes.setVisibility(View.VISIBLE);
        retryEpisodes.setVisibility(View.GONE);
        try {
            tabLayout.removeAllTabs();
            URL episodesURL = new URL("https://wandering-witch.herokuapp.com/media/show?type=TV&name=" + NAME);
            URI episodesURI = new URI(episodesURL.getProtocol(), episodesURL.getUserInfo(), IDN.toASCII(episodesURL.getHost()), episodesURL.getPort(), episodesURL.getPath(), episodesURL.getQuery(), episodesURL.getRef());

            showArrayRequest = new JsonArrayRequest(episodesURI.toASCIIString(), episodesResponse -> {
                try {
                    for (int i = 0; i < episodesResponse.length(); i++) {
                        JSONObject obj = (JSONObject) episodesResponse.get(i);
                        Iterator<String> iterator = obj.keys();
                        while (iterator.hasNext()) {
                            String keyStr = iterator.next();
                            tabLayout.addTab(tabLayout.newTab().setText(keyStr));
                        }
                    }

                    TabLayout.Tab tab0 = tabLayout.getTabAt(0);
                    if (tab0 != null) {
                        if (tab0.getText() != null)
                            getEpisodes(tab0.getText().toString(), 0, episodesResponse);
                    }

                    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {
                            TransitionManager.beginDelayedTransition(rootView);
                            if (tab != null) {
                                if (tab.getText() != null)
                                    getEpisodes(tab.getText().toString(), tab.getPosition(), episodesResponse);
                            }
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {

                        }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {

                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    retryEpisodes.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "IOException | JSONException e", Toast.LENGTH_SHORT).show();
                }
                loadingEpisodes.setVisibility(View.GONE);
                playButton.setOnClickListener(v -> {
                    int scrollTo = ((View) tabLayout.getParent().getParent()).getTop() + tabLayout.getTop();
                    nestedScrollView.smoothScrollTo(0, scrollTo - 30);
                });
                Log.d("showQueue", "executed");
            }, errorEpisodes -> {
                retryEpisodes.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Failed fetching episodes", Toast.LENGTH_SHORT).show();
                loadingEpisodes.setVisibility(View.GONE);
                Log.d("showQueue", Arrays.toString(errorEpisodes.getStackTrace()));
            });

            showArrayRequest.setTag(TAG);
            showArrayRequest.setShouldCache(false);
            showArrayRequest.setRetryPolicy(new DefaultRetryPolicy(7000, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(showArrayRequest);
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
            retryEpisodes.setVisibility(View.VISIBLE);

        }
    }

    private void getEpisodes(String tabName, int index, JSONArray episodesResponse) {

        try {
            JSONObject obj = (JSONObject) episodesResponse.get(index);
            Object keyValue = obj.get(tabName);
            JSONArray seasonsArray = (JSONArray) keyValue;

            episodeItems.clear();

            for (int j = 0; j < seasonsArray.length(); j++) {
                JSONObject season = (JSONObject) seasonsArray.get(j);
                String file = (String) season.get("file");
                String episode = (file.split(" - ", 2))[0];
                String title = (file.split(" - ", 2))[1];
                String bytes = String.valueOf(season.get("bytes"));

                URL playURL = new URL(ZPlex + NAME + " - " + TYPE + "/" + file);
                URI playURI = new URI(playURL.getProtocol(), playURL.getUserInfo(), IDN.toASCII(playURL.getHost()), playURL.getPort(), playURL.getPath(), playURL.getQuery(), playURL.getRef());
                episodeItems.add(new EpisodeItem(NAME, episode, title.substring(0, title.length() - 4), playURI.toASCIIString(), bytes));
            }
            episodeAdapter.notifyDataSetChanged();
        } catch (JSONException | MalformedURLException | URISyntaxException je) {
            je.printStackTrace();
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavigationBarHeight() {
        boolean hasMenuKey = ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey();
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0 && !hasMenuKey) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null)
            requestQueue.cancelAll(TAG);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.no_animation, R.anim.slide_down);
    }
}