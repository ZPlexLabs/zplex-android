package zechs.zplex.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import zechs.zplex.R
import zechs.zplex.adapter.streams.StreamsDataAdapter
import zechs.zplex.adapter.streams.StreamsDataModel

class StreamsDialog(
    context: Context,
    private val onStreamClick: (StreamsDataModel) -> Unit,
    private val onDownloadClick: (StreamsDataModel) -> Unit,
) : Dialog(context) {

    val streamsDataAdapter by lazy {
        StreamsDataAdapter(
            setOnStreamClickListener = { onStreamClick(it) },
            setOnDownloadClickListener = { onDownloadClick(it) }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_streams)
    }

}