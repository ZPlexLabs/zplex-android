package zechs.zplex.activity

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.Constraints
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonParser
import org.json.JSONArray
import zechs.zplex.FetchDatabaseDialog
import zechs.zplex.R
import zechs.zplex.adapter.CardAdapter
import zechs.zplex.adapter.CardItem
import zechs.zplex.utils.Constants
import java.io.File
import java.io.FileReader
import java.net.IDN
import java.net.URI
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {
    private val mediaItems = ArrayList<CardItem>()
    private var mediaAdapter: CardAdapter? = null
    private var mediaView: RecyclerView? = null
    private var loadingHome: ProgressBar? = null
    private var rootView: ViewGroup? = null
    private var text = ""
    private var dbJson: File? = null
    private var fetchDatabaseDialog: FetchDatabaseDialog? = null
    private var searchView: SearchView? = null
    private var tabLayout: TabLayout? = null
    private var toolbar: Toolbar? = null
    private var errorView: View? = null
    private var errorText: TextView? = null
    private var noResultsRetry: MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbJson = File("$filesDir/dbJson.json")

        rootView = findViewById(R.id.root)
        errorView = findViewById(R.id.error_view)
        errorText = findViewById(R.id.error_txt)

        noResultsRetry = findViewById(R.id.retry_btn)
        noResultsRetry?.visibility = View.GONE

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        loadingHome = findViewById(R.id.loading_home)
        mediaView = findViewById(R.id.media_view)
        mediaAdapter = CardAdapter(mediaItems, applicationContext, this@MainActivity)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mediaView?.layoutManager = GridLayoutManager(this, 2)
        } else {
            mediaView?.layoutManager = GridLayoutManager(this, 5)
        }
        mediaView?.adapter = mediaAdapter
        tabLayout = findViewById(R.id.tabs)
        tabLayout?.newTab()?.let { tabLayout?.addTab(it.setText("Movies")) }
        tabLayout?.newTab()?.let { tabLayout?.addTab(it.setText("TV Shows")) }
        tabLayout?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                queryDB(text)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        errorView?.visibility = View.GONE
        Thread { FirebaseMessaging.getInstance().subscribeToTopic("all") }.start()
        if (dbJson!!.exists()) {
            queryDB("")
        } else {
            fetchDatabase()
        }
    }

    private fun queryDB(query: String) {
        runOnUiThread {
            TransitionManager.beginDelayedTransition(rootView!!)
            mediaView!!.stopScroll()
            mediaItems.clear()
            errorView!!.visibility = View.GONE
            loadingHome!!.visibility = View.VISIBLE
            mediaView!!.visibility = View.GONE
        }
        try {
            val parser = JsonParser()
            val dbObject: Any = parser.parse(FileReader("$filesDir/dbJson.json"))
            val jsonArray = JSONArray(dbObject.toString())
            for (i in 0 until jsonArray.length()) {
                val name = jsonArray.getJSONObject(i).getString("name")
                val type = jsonArray.getJSONObject(i).getString("type")
                if (name.lowercase(Locale.getDefault())
                        .contains(query.lowercase(Locale.getDefault()))
                ) {
                    val mediaType: String = if (tabLayout!!.selectedTabPosition == 0) {
                        "Movie"
                    } else {
                        "TV"
                    }
                    if (type == mediaType) {
                        val posterURL = Constants.ZPlex + name + " - " + type + "/poster.jpg"
                        val urlPoster = URL(posterURL)
                        val uriPoster = URI(
                            urlPoster.protocol,
                            urlPoster.userInfo,
                            IDN.toASCII(urlPoster.host),
                            urlPoster.port,
                            urlPoster.path,
                            urlPoster.query,
                            urlPoster.ref
                        )
                        runOnUiThread {
                            mediaItems.add(
                                CardItem(
                                    name,
                                    type,
                                    uriPoster.toASCIIString()
                                )
                            )
                        }
                    }
                }
            }
            mediaAdapter!!.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            runOnUiThread {
                if (mediaItems.size == 0) {
                    mediaView!!.visibility = View.GONE
                    errorView!!.visibility = View.VISIBLE
                } else {
                    errorText!!.setText(R.string.no_results)
                    errorView!!.visibility = View.GONE
                    mediaView!!.visibility = View.VISIBLE
                    mediaView!!.smoothScrollToPosition(0)
                }
                loadingHome!!.visibility = View.GONE
                Log.d("queryDB", "postDelayed executed")
                if (toolbar!!.parent is AppBarLayout) {
                    (toolbar!!.parent as AppBarLayout).setExpanded(true, true)
                }
            }
        }, 500)
    }

    private fun fetchDatabase() {
        Toast.makeText(applicationContext, getString(R.string.updating_library), Toast.LENGTH_LONG)
            .show()
        fetchDatabaseDialog = FetchDatabaseDialog(this@MainActivity)
        val dialogWindow = fetchDatabaseDialog!!.window
        fetchDatabaseDialog!!.show()
        dialogWindow!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogWindow.setGravity(Gravity.CENTER)
        dialogWindow.setLayout(
            Constraints.LayoutParams.MATCH_PARENT,
            Constraints.LayoutParams.MATCH_PARENT
        )
        fetchDatabaseDialog!!.setOnDismissListener {
            if (dbJson!!.exists()) queryDB(
                ""
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mediaView!!.layoutManager = GridLayoutManager(this, 2)
        } else {
            mediaView!!.layoutManager = GridLayoutManager(this, 5)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        val update = menu.findItem(R.id.update_library)
        update.setOnMenuItemClickListener {
            fetchDatabase()
            false
        }
        val clear = menu.findItem(R.id.clear_image_cache)
        clear.setOnMenuItemClickListener {
            AsyncTask.execute { Glide.get(applicationContext).clearDiskCache() }
            Toast.makeText(
                applicationContext,
                getString(R.string.clearing_image_cache),
                Toast.LENGTH_LONG
            ).show()
            false
        }
        val repository = menu.findItem(R.id.app_update)
        repository.setOnMenuItemClickListener {
            val updateRepository = Intent(Intent.ACTION_VIEW)
            updateRepository.data =
                Uri.parse("https://drive.google.com/drive/folders/1WXS6bC7JBS1sxg0dYjUQp3Ql-wtCTrUe")
            startActivity(updateRepository)
            false
        }
        searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    runOnUiThread {
                        text = query
                        queryDB(text)
                    }
                }, 150)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        searchView!!.setOnCloseListener {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                runOnUiThread {
                    if (text != "") {
                        text = ""
                        queryDB(text)
                    }
                }
            }, 150)
            false
        }
        val searchEditText =
            searchView!!.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.hint = "Search..."
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.textColor_87))
        searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.textColor_54))
        val searchImgId = androidx.appcompat.R.id.search_button
        val searchImg = searchView!!.findViewById<ImageView>(searchImgId)
        searchImg.setImageResource(R.drawable.ic_search)
        return true
    }

    override fun onBackPressed() {
        if (!searchView!!.isIconified) {
            searchView!!.isIconified = true
        } else {
            super.onBackPressed()
        }
    }
}