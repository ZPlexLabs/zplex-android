package zechs.zplex.ui.files.adapter

import androidx.annotation.Keep
import zechs.zplex.googledrive.data.remote.api.drive.model.DriveFile

sealed class FilesDataModel {

    @Keep
    data class File(
        val driveFile: DriveFile
    ) : FilesDataModel()

    data object Loading : FilesDataModel()

}