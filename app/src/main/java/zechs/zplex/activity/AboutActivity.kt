package zechs.zplex.activity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import zechs.zplex.R
import zechs.zplex.adapter.EpisodeAdapter
import zechs.zplex.adapter.EpisodeItem
import zechs.zplex.utils.Constants
import zechs.zplex.utils.GlideApp
import zechs.zplex.utils.LXStringRequest
import java.net.*
import java.util.*

class AboutActivity : AppCompatActivity() {
    private val tag = "AboutActivity"
    private val episodeItems = ArrayList<EpisodeItem>()
    private var episodeAdapter: EpisodeAdapter? = null
    private var episodesView: RecyclerView? = null
    private var typeShow: String? = null
    private var nameShow: String? = null
    private var posterURL: String? = null
    private var poster: ImageView? = null
    private var title: TextView? = null
    private var yearMpaa: TextView? = null
    private var genre: TextView? = null
    private var plot: TextView? = null
    private var playButton: MaterialButton? = null
    private var downloadButton: MaterialButton? = null
    private var retryButton: MaterialButton? = null
    private var retryEpisodes: MaterialButton? = null
    private var nestedScrollView: NestedScrollView? = null
    private var loadingAbout: ProgressBar? = null
    private var loadingEpisodes: ProgressBar? = null
    private var rootView: ViewGroup? = null
    private var errorView: View? = null
    private var tabLayout: TabLayout? = null
    private var frameLayout: FrameLayout? = null
    private var posterCard: MaterialCardView? = null
    private var stringRequest: LXStringRequest? = null
    private var requestQueue: RequestQueue? = null
    private var showArrayRequest: JsonArrayRequest? = null

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("onNewIntent", "Intent received")
        typeShow = intent.getStringExtra("TYPE")
        nameShow = intent.getStringExtra("NAME")
        posterURL = intent.getStringExtra("POSTERURL")
        TransitionManager.beginDelayedTransition(rootView!!)
        poster!!.setImageResource(0)
        title!!.text = ""
        yearMpaa!!.text = ""
        genre!!.text = ""
        plot!!.text = ""
        tabLayout!!.removeAllTabs()
        episodeItems.clear()
        fetchDetails()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        typeShow = intent.getStringExtra("TYPE")
        nameShow = intent.getStringExtra("NAME")
        posterURL = intent.getStringExtra("POSTERURL")
        init()
        fetchDetails()
    }

    private fun init() {
        rootView = findViewById(R.id.root)
        frameLayout = findViewById(R.id.frameLayout)
        loadingAbout = findViewById(R.id.loading_media)
        nestedScrollView = findViewById(R.id.main_view)
        errorView = findViewById(R.id.error_view)
        posterCard = findViewById(R.id.item_poster)
        poster = findViewById(R.id.poster_view)
        title = findViewById(R.id.item_title)
        yearMpaa = findViewById(R.id.year_mpaa)
        genre = findViewById(R.id.genre)
        plot = findViewById(R.id.plot)
        playButton = findViewById(R.id.play_btn)
        loadingEpisodes = findViewById(R.id.loading_episodes)
        downloadButton = findViewById(R.id.down_btn)
        tabLayout = findViewById(R.id.tabs)
        episodesView = findViewById(R.id.episodes_view)

        retryButton = findViewById(R.id.retry_btn)
        retryButton?.setOnClickListener { fetchDetails() }

        retryEpisodes = findViewById(R.id.retry_episodes)
        retryEpisodes?.setOnClickListener { getShow() }

        val newLayoutParams = posterCard?.layoutParams as ConstraintLayout.LayoutParams
        newLayoutParams.topMargin = statusBarHeight() + 35
        posterCard?.layoutParams = newLayoutParams

        episodesView?.setHasFixedSize(true)
        episodeAdapter = EpisodeAdapter(episodeItems, applicationContext)
        episodesView?.adapter = episodeAdapter
        episodesView?.setPadding(0, 0, 0, navigationBarHeight() + 24)
    }

    @SuppressLint("SetTextI18n")
    private fun fetchDetails() {
        try {
            errorView!!.visibility = View.GONE
            nestedScrollView!!.visibility = View.VISIBLE
            retryEpisodes!!.visibility = View.GONE
            title!!.text = nameShow
            GlideApp.with(applicationContext)
                .asBitmap()
                .load(posterURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .override(Target.SIZE_ORIGINAL)
                .listener(object : RequestListener<Bitmap?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Bitmap?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Toast.makeText(
                            applicationContext,
                            R.string.failed_to_load_poster_image,
                            Toast.LENGTH_SHORT
                        ).show()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any,
                        target: Target<Bitmap?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let {
                            Palette.from(resource).generate { p: Palette? ->
                                p?.let {
                                    val accent = p.getVibrantColor(
                                        p.getDominantColor(
                                            ContextCompat.getColor(
                                                applicationContext, R.color.colorAccent
                                            )
                                        )
                                    )

                                    val colorPrimary = ContextCompat.getColor(
                                        applicationContext,
                                        R.color.colorPrimary
                                    )

                                    val gradientDrawable = GradientDrawable(
                                        GradientDrawable.Orientation.TOP_BOTTOM,
                                        intArrayOf(accent, colorPrimary)
                                    )

                                    TransitionManager.beginDelayedTransition(rootView!!)
                                    frameLayout!!.background = gradientDrawable
                                    tabLayout!!.setTabTextColors(
                                        ContextCompat.getColor(
                                            applicationContext,
                                            R.color.textColor_54
                                        ), accent
                                    )
                                    tabLayout!!.setSelectedTabIndicatorColor(accent)
                                    playButton!!.setBackgroundColor(accent)
                                    playButton!!.setTextColor(getContrastColor(accent))
                                    playButton!!.iconTint =
                                        ColorStateList.valueOf(getContrastColor(accent))
                                    downloadButton!!.iconTint =
                                        ColorStateList.valueOf(getContrastColor(colorPrimary))
                                    downloadButton!!.setTextColor(getContrastColor(colorPrimary))
                                    loadingAbout!!.indeterminateTintList =
                                        ColorStateList.valueOf(accent)
                                    loadingEpisodes!!.indeterminateTintList =
                                        ColorStateList.valueOf(accent)
                                    downloadButton!!.strokeColor = ColorStateList.valueOf(accent)
                                    posterCard!!.setCardBackgroundColor(accent)
                                    retryButton!!.setTextColor(accent)
                                    retryEpisodes!!.setTextColor(accent)
                                }
                            }
                        }
                        return false
                    }
                })
                .into(poster!!)
            requestQueue = Volley.newRequestQueue(this)
            val url: URL
            val nfoURL: String = if (typeShow == "Movie") {
                "https://zplex.zechs.workers.dev/0:/$nameShow - $typeShow/movie.nfo"
            } else {
                "https://zplex.zechs.workers.dev/0:/$nameShow - $typeShow/tvshow.nfo"
            }
            url = URL(nfoURL)
            val uri = URI(
                url.protocol,
                url.userInfo,
                IDN.toASCII(url.host),
                url.port,
                url.path,
                url.query,
                url.ref
            )
            stringRequest =
                LXStringRequest(Request.Method.GET, uri.toASCIIString(), { response: String? ->
                    val xmlToJson = XmlToJson.Builder(response!!).build()
                    try {
                        val jsonObject = JSONObject(xmlToJson.toString())
                        val root: JSONObject
                        if (typeShow == "TV") {
                            root = jsonObject.getJSONObject("tvshow")
                            downloadButton!!.visibility = View.GONE
                        } else {
                            root = jsonObject.getJSONObject("movie")
                            downloadButton!!.visibility = View.VISIBLE
                        }
                        TransitionManager.beginDelayedTransition(rootView!!)
                        val mpaa = root.getString("mpaa")
                        val runtime = root.getString("runtime")
                        val year = root.getString("year")
                        if (mpaa == "") {
                            yearMpaa!!.text = year
                        } else {
                            yearMpaa!!.text = """
                        $year${getString(R.string.divider)}$mpaa
                        ${runtime}min
                        """.trimIndent()
                        }
                        val plot = root.getString("plot")
                        val genres = StringBuilder()
                        val genreArray = JSONArray(root.getString("genre"))
                        for (i in 0 until genreArray.length()) {
                            genres.append(genreArray.getJSONObject(i).getString("content"))
                                .append(", ")
                        }
                        genres.deleteCharAt(genres.length - 2)
                        genre!!.text = genres
                        if (typeShow == "TV") {
                            getShow()
                            if (plot.length >= 200) {
                                this.plot!!.text = plot.substring(0, 200) + "..."
                                this.plot!!.setOnClickListener {
                                    TransitionManager.beginDelayedTransition(
                                        rootView!!
                                    )
                                    this.plot!!.text = plot
                                }
                            } else {
                                this.plot!!.text = plot
                            }
                        } else {
                            this.plot!!.text = plot
                            try {
                                val playMovie =
                                    URL(Constants.ZPlex + nameShow + " - " + typeShow + "/" + nameShow + ".mkv")
                                val movieURI = URI(
                                    playMovie.protocol,
                                    playMovie.userInfo,
                                    IDN.toASCII(playMovie.host),
                                    playMovie.port,
                                    playMovie.path,
                                    playMovie.query,
                                    playMovie.ref
                                )
                                playButton!!.setOnClickListener {
                                    try {
                                        val vlcIntent = Intent(Intent.ACTION_VIEW)
                                        vlcIntent.setPackage("org.videolan.vlc")
                                        vlcIntent.component = ComponentName(
                                            "org.videolan.vlc",
                                            "org.videolan.vlc.gui.video.VideoPlayerActivity"
                                        )
                                        vlcIntent.setDataAndTypeAndNormalize(
                                            Uri.parse(movieURI.toASCIIString()),
                                            "video/*"
                                        )
                                        vlcIntent.putExtra("title", "$nameShow ($year)")
                                        vlcIntent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                                        val bundle = ActivityOptionsCompat.makeCustomAnimation(
                                            applicationContext,
                                            android.R.anim.fade_in,
                                            android.R.anim.fade_out
                                        ).toBundle()
                                        startActivity(vlcIntent, bundle)
                                    } catch (notFoundException: ActivityNotFoundException) {
                                        notFoundException.printStackTrace()
                                        Toast.makeText(
                                            applicationContext,
                                            getString(R.string.vlc_not_found),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }

                                downloadButton!!.setOnClickListener {
                                    val dlRequest =
                                        DownloadManager.Request(Uri.parse(movieURI.toASCIIString()))
                                    val fileName = "$nameShow ($year).mkv"
                                    dlRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                                    dlRequest.setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS,
                                        fileName
                                    )
                                    dlRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                                    dlRequest.setMimeType("video/x-matroska")
                                    val dlManager =
                                        getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                                    dlManager.enqueue(dlRequest)
                                    Toast.makeText(
                                        applicationContext,
                                        "Download started",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: MalformedURLException) {
                                e.printStackTrace()
                            } catch (e: URISyntaxException) {
                                e.printStackTrace()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        errorView!!.visibility = View.VISIBLE
                        nestedScrollView!!.visibility = View.GONE
                    }
                    Log.d("nfoVolleySuccess", "executed")
                }) { error: VolleyError ->
                    errorView!!.visibility = View.VISIBLE
                    nestedScrollView!!.visibility = View.GONE
                    Log.d("nfoVolleyError", Arrays.toString(error.stackTrace))
                }
            stringRequest!!.tag = tag
            stringRequest!!.setShouldCache(false)
            stringRequest!!.retryPolicy =
                DefaultRetryPolicy(5000, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            requestQueue?.add(stringRequest)
        } catch (e: MalformedURLException) {
            errorView!!.visibility = View.VISIBLE
            nestedScrollView!!.visibility = View.GONE
            e.printStackTrace()
        } catch (e: URISyntaxException) {
            errorView!!.visibility = View.VISIBLE
            nestedScrollView!!.visibility = View.GONE
            e.printStackTrace()
        }
    }

    private fun getShow() {
        loadingEpisodes!!.visibility = View.VISIBLE
        retryEpisodes!!.visibility = View.GONE
        try {
            tabLayout!!.removeAllTabs()
            val episodesURL =
                URL("https://wandering-witch.herokuapp.com/media/show?type=TV&name=$nameShow")
            val episodesURI = URI(
                episodesURL.protocol,
                episodesURL.userInfo,
                IDN.toASCII(episodesURL.host),
                episodesURL.port,
                episodesURL.path,
                episodesURL.query,
                episodesURL.ref
            )
            showArrayRequest =
                JsonArrayRequest(episodesURI.toASCIIString(), { episodesResponse: JSONArray ->
                    try {
                        for (i in 0 until episodesResponse.length()) {
                            val obj = episodesResponse[i] as JSONObject
                            val iterator = obj.keys()
                            while (iterator.hasNext()) {
                                val keyStr = iterator.next()
                                tabLayout!!.addTab(tabLayout!!.newTab().setText(keyStr))
                            }
                        }
                        val tab0 = tabLayout!!.getTabAt(0)
                        tab0?.let {
                            tab0.text?.let {
                                getEpisodes(
                                    tab0.text.toString(),
                                    0,
                                    episodesResponse
                                )
                            }
                        }
                        tabLayout!!.addOnTabSelectedListener(object : OnTabSelectedListener {
                            override fun onTabSelected(tab: TabLayout.Tab) {
                                TransitionManager.beginDelayedTransition(rootView!!)
                                tab.let {
                                    tab.text?.let {
                                        getEpisodes(
                                            tab.text.toString(),
                                            tab.position,
                                            episodesResponse
                                        )
                                    }
                                }
                            }

                            override fun onTabUnselected(tab: TabLayout.Tab) {}
                            override fun onTabReselected(tab: TabLayout.Tab) {}
                        })
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        retryEpisodes!!.visibility = View.VISIBLE
                        Toast.makeText(
                            applicationContext,
                            "IOException | JSONException e",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    loadingEpisodes!!.visibility = View.GONE
                    playButton!!.setOnClickListener {
                        val scrollTo = (tabLayout!!.parent.parent as View).top + tabLayout!!.top
                        nestedScrollView!!.smoothScrollTo(0, scrollTo - 30)
                    }
                    Log.d("showQueue", "executed")
                }) { errorEpisodes: VolleyError ->
                    retryEpisodes!!.visibility = View.VISIBLE
                    Toast.makeText(
                        applicationContext,
                        "Failed fetching episodes",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadingEpisodes!!.visibility = View.GONE
                    Log.d("showQueue", Arrays.toString(errorEpisodes.stackTrace))
                }
            showArrayRequest!!.tag = tag
            showArrayRequest!!.setShouldCache(false)
            showArrayRequest!!.retryPolicy =
                DefaultRetryPolicy(7000, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            requestQueue!!.add(showArrayRequest)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            retryEpisodes!!.visibility = View.VISIBLE
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            retryEpisodes!!.visibility = View.VISIBLE
        }
    }

    private fun getEpisodes(tabName: String, index: Int, episodesResponse: JSONArray) {
        try {
            val obj = episodesResponse[index] as JSONObject
            val keyValue = obj[tabName]
            val seasonsArray = keyValue as JSONArray
            episodeItems.clear()
            for (j in 0 until seasonsArray.length()) {
                val season = seasonsArray[j] as JSONObject
                val file = season["file"] as String
                val episode = file.split(" - ".toRegex(), 2).toTypedArray()[0]
                val title = file.split(" - ".toRegex(), 2).toTypedArray()[1]
                val bytes = season["bytes"].toString()
                val playURL = URL(Constants.ZPlex + nameShow + " - " + typeShow + "/" + file)
                val playURI = URI(
                    playURL.protocol,
                    playURL.userInfo,
                    IDN.toASCII(playURL.host),
                    playURL.port,
                    playURL.path,
                    playURL.query,
                    playURL.ref
                )
                episodeItems.add(
                    EpisodeItem(
                        nameShow!!,
                        episode,
                        title.substring(0, title.length - 4),
                        playURI.toASCIIString(),
                        bytes
                    )
                )
            }
            episodeAdapter!!.notifyDataSetChanged()
        } catch (je: JSONException) {
            je.printStackTrace()
        } catch (je: MalformedURLException) {
            je.printStackTrace()
        } catch (je: URISyntaxException) {
            je.printStackTrace()
        }
    }

    private fun statusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun navigationBarHeight(): Int {
        val hasMenuKey = ViewConfiguration.get(applicationContext).hasPermanentMenuKey()
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0 && !hasMenuKey) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    override fun onDestroy() {
        super.onDestroy()
        requestQueue.let {
            requestQueue!!.cancelAll(tag)
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed();
        finish()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
    }

    @ColorInt
    fun getContrastColor(@ColorInt color: Int): Int {
        val a = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(
            color
        )) / 255
        return if (a < 0.5) Color.BLACK else Color.WHITE
    }
}