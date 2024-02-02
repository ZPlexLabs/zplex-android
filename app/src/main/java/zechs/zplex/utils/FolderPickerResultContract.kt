package zechs.zplex.utils

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import zechs.zplex.ui.folder_picker.FolderPickerActivity

data class StartFolderPicker(
    val title: String,
    val type: FolderType
)

data class SelectedFolder(
    val id: String,
    val name: String,
    val type: FolderType
)

enum class FolderType {
    MOVIES, SHOWS,
}

class FolderPickerResultContract : ActivityResultContract<StartFolderPicker, SelectedFolder?>() {

    companion object {
        const val RESULT_MOVIE_FOLDER = 10001
        const val RESULT_SHOW_FOLDER = 10002
    }

    override fun createIntent(context: Context, input: StartFolderPicker): Intent {
        return Intent(context, FolderPickerActivity::class.java).apply {
            putExtra(FolderPickerActivity.EXTRA_TITLE, input.title)
            putExtra(FolderPickerActivity.EXTRA_TYPE, input.type.name)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): SelectedFolder? {
        return when (resultCode) {
            RESULT_MOVIE_FOLDER -> {
                intent?.let {
                    SelectedFolder(
                        id = it.getStringExtra("id")!!,
                        name = it.getStringExtra("name")!!,
                        type = FolderType.MOVIES
                    )
                }
            }

            RESULT_SHOW_FOLDER -> {
                intent?.let {
                    SelectedFolder(
                        id = it.getStringExtra("id")!!,
                        name = it.getStringExtra("name")!!,
                        type = FolderType.SHOWS
                    )
                }
            }

            else -> null
        }
    }

}