package zechs.zplex.ui.activity

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.Constraints
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import zechs.zplex.FetchDatabaseDialog
import zechs.zplex.R
import zechs.zplex.adapter.FilesAdapter
import zechs.zplex.db.FilesDatabase
import zechs.zplex.repository.FilesRepository
import zechs.zplex.ui.FileViewModel
import zechs.zplex.ui.FilesViewModelProviderFactory
import zechs.zplex.utils.Constants.Companion.PAGE_TOKEN
import zechs.zplex.utils.Constants.Companion.SEARCH_DELAY_AMOUNT
import zechs.zplex.utils.Resource
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private var loadingHome: ProgressBar? = null
    private var rootView: ViewGroup? = null
    private var text = ""
    private var dbJson: File? = null
    private var fetchDatabaseDialog: FetchDatabaseDialog? = null
    private var searchView: SearchView? = null
    private var toolbar: Toolbar? = null
    private var errorView: View? = null
    private var errorText: TextView? = null
    private var noResultsRetry: MaterialButton? = null

    lateinit var viewModel: FileViewModel
    private lateinit var filesAdapter: FilesAdapter
    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filesRepository = FilesRepository(FilesDatabase(this))
        val viewModelProviderFactory = FilesViewModelProviderFactory(application, filesRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(FileViewModel::class.java)

        setContentView(R.layout.activity_main)
        dbJson = File("$filesDir/dbJson.json")
        filesAdapter = FilesAdapter()

        rootView = findViewById(R.id.root)
        errorView = findViewById(R.id.error_view)
        errorText = findViewById(R.id.error_txt)

        noResultsRetry = findViewById(R.id.retry_btn)
        noResultsRetry?.visibility = View.GONE

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        loadingHome = findViewById(R.id.loading_home)

        errorView?.visibility = View.GONE

        setupRecyclerView()

        viewModel.filesList.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { filesResponse ->
                        filesAdapter.differ.submitList(filesResponse.files.toList())
                        isLastPage = filesResponse.nextPageToken == null
                        println(filesResponse.nextPageToken)
                        Log.d("pageToken", PAGE_TOKEN)
                        media_view!!.visibility = View.VISIBLE
                        error_view!!.visibility = View.GONE
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(
                            applicationContext,
                            "An error occurred: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(tag, "An error occurred: $message")
                        media_view!!.visibility = View.GONE
                        error_view!!.visibility = View.VISIBLE
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        filesAdapter.setOnItemClickListener {
            val posterUrl = Uri.parse("https://zplex.zechs.workers.dev/0:/${it.name}/poster.jpg")
            val name = it.name.split(" - ").toTypedArray()[0]
            val type = it.name.split(" - ").toTypedArray()[1]
            val intent = Intent(this, AboutActivity::class.java)
            intent.putExtra("NAME", name)
            intent.putExtra("TYPE", type)
            intent.putExtra("POSTERURL", posterUrl.toString())
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
        }

        error_view.setOnClickListener {
            PAGE_TOKEN = ""
            viewModel.getDriveFiles(10, PAGE_TOKEN, setDriveQuery(text))
        }
    }

    private fun setDriveQuery(query: String): String {
        return if (query == "") {
            "mimeType='application/vnd.google-apps.folder' and parents in '0AASFDMjRqUB0Uk9PVA' and trashed = false"
        } else {
            "name contains '$query' and mimeType='application/vnd.google-apps.folder' and parents in '0AASFDMjRqUB0Uk9PVA' and trashed = false"

        }
    }

    private fun hideProgressBar() {
        loading_home.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        loading_home.visibility = View.VISIBLE
        isLoading = true
    }

    @DelicateCoroutinesApi
    fun subscribeToFirebaseMessaging() {
        GlobalScope.launch(Dispatchers.Main) {
            FirebaseMessaging.getInstance().subscribeToTopic("all")
        }
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as GridLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= 1

            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.getDriveFiles(10, PAGE_TOKEN, setDriveQuery(text))
                isScrolling = false
            } else {
                Log.d(tag, "Not paginating")
                media_view.setPadding(0, 0, 0, 0)
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }


    private fun setupRecyclerView() {
        filesAdapter = FilesAdapter()
        media_view.apply {
            adapter = filesAdapter
            layoutManager = GridLayoutManager(applicationContext, 2)
            addOnScrollListener(this@MainActivity.scrollListener)
        }
    }

    private fun queryDB(query: String) {
//        runOnUiThread {
//            TransitionManager.beginDelayedTransition(rootView!!)
//            mediaView!!.stopScroll()
//            mediaItems.clear()
//            errorView!!.visibility = View.GONE
//            loadingHome!!.visibility = View.VISIBLE
//            mediaView!!.visibility = View.GONE
//        }
//        try {
//            val parser = JsonParser()
//            val dbObject: Any = parser.parse(FileReader("$filesDir/dbJson.json"))
//            val jsonArray = JSONArray(dbObject.toString())
//            for (i in 0 until jsonArray.length()) {
//                val name = jsonArray.getJSONObject(i).getString("name")
//                val type = jsonArray.getJSONObject(i).getString("type")
//                if (name.lowercase(Locale.getDefault())
//                        .contains(query.lowercase(Locale.getDefault()))
//                ) {
//                    val posterURL = Constants.ZPlex + name + " - " + type + "/poster.jpg"
//                    val urlPoster = URL(posterURL)
//                    val uriPoster = URI(
//                        urlPoster.protocol,
//                        urlPoster.userInfo,
//                        IDN.toASCII(urlPoster.host),
//                        urlPoster.port,
//                        urlPoster.path,
//                        urlPoster.query,
//                        urlPoster.ref
//                    )
//                    runOnUiThread {
//                        mediaItems.add(
//                            CardItem(
//                                name,
//                                type,
//                                uriPoster.toASCIIString()
//                            )
//                        )
//                    }
//                }
//
//            }
//            mediaAdapter!!.notifyDataSetChanged()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        val handler = Handler(Looper.getMainLooper())
//        handler.postDelayed({
//            runOnUiThread {
//                if (mediaItems.size == 0) {
//                    mediaView!!.visibility = View.GONE
//                    errorView!!.visibility = View.VISIBLE
//                } else {
//                    errorText!!.setText(R.string.no_results)
//                    errorView!!.visibility = View.GONE
//                    mediaView!!.visibility = View.VISIBLE
//                    mediaView!!.smoothScrollToPosition(0)
//                }
//                loadingHome!!.visibility = View.GONE
//                Log.d("queryDB", "postDelayed executed")
//                if (toolbar!!.parent is AppBarLayout) {
//                    (toolbar!!.parent as AppBarLayout).setExpanded(true, true)
//                }
//            }
//        }, 500)
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
            if (dbJson!!.exists()) queryDB("")
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
//        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            mediaView!!.layoutManager = GridLayoutManager(this, 2)
//        } else {
//            mediaView!!.layoutManager = GridLayoutManager(this, 5)
//        }
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
            AsyncTask.execute {
                Glide.get(applicationContext).clearDiskCache()
            }
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

        var job: Job? = null

        searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                job?.cancel()
                job = MainScope().launch {
                    delay(SEARCH_DELAY_AMOUNT)
                    text = query
                    PAGE_TOKEN = ""
                    viewModel.getDriveFiles(10, "", setDriveQuery(text))
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        searchView!!.setOnCloseListener {
            job?.cancel()
            job = MainScope().launch {
                delay(250)
                text = ""
                PAGE_TOKEN = ""
                viewModel.getDriveFiles(10, "", setDriveQuery(text))
            }
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