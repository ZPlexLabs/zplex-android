package zechs.zplex.ui.files.adapter

import androidx.annotation.Keep
import zechs.zplex.data.model.drive.DriveFile

sealed class FilesDataModel {

    @Keep
    data class File(
        val driveFile: DriveFile
    ) : FilesDataModel()

    data object Loading : FilesDataModel()

}