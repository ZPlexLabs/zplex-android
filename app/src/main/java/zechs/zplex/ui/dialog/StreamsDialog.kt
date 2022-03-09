package zechs.zplex.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.adapter.streams.StreamsDataAdapter

class StreamsDialog(
    context: Context,
) : Dialog(context) {

    private val thisTAG = "StreamsDialog"
    val streamsDataAdapter by lazy { StreamsDataAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_streams)

        val streamsView = findViewById<RecyclerView>(R.id.rv_streams)

        streamsView.apply {
            adapter = streamsDataAdapter
            layoutManager = LinearLayoutManager(
                context, LinearLayoutManager.VERTICAL, false
            )
        }
    }

}