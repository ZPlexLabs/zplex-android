package zechs.zplex.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import zechs.zplex.R

class FiltersDialog(
    context: Context,
) : Dialog(context) {

    private val thisTAG = "FiltersDialog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_filters)


    }

}