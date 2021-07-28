package zechs.zplex

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import zechs.zplex.utils.Constants
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

class FetchDatabaseDialog(context: Context) : Dialog(context) {

    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.fetch_dialog)

        val dbFile = File(context.filesDir.toString() + "/dbJson.json")

        if (dbFile.exists()) {
            val isDeleted = dbFile.delete()
            Log.d("isDeleted", isDeleted.toString())
        }

        val requestQueue = Volley.newRequestQueue(context)
        val url = Constants.API + "/media/library"
        val objectArray = JsonArrayRequest(url, { response: JSONArray ->
            try {
                val file = FileWriter(context.filesDir.toString() + "/dbJson.json")
                file.write(response.toString())
                file.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to load library", Toast.LENGTH_SHORT).show()
            }
            dismiss()
            Log.d("FetchDatabaseDialog", "executed")
        }) { error: VolleyError ->
            Toast.makeText(
                context,
                "Failed to fetch library, Please try again!",
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
            Log.d("FetchDatabaseDialog", Arrays.toString(error.stackTrace))
        }
        objectArray.setShouldCache(false)
        objectArray.retryPolicy =
            DefaultRetryPolicy(5000, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(objectArray)
    }

    override fun onBackPressed() {
        //super.onBackPressed();
    }
}