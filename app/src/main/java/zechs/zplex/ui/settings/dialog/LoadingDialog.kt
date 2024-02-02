package zechs.zplex.ui.settings.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import zechs.zplex.R

class LoadingDialog(
    context: Context,
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_loading)
    }

}