package zechs.zplex.repository

import zechs.zplex.api.RetrofitInstance
import zechs.zplex.db.FilesDatabase
import zechs.zplex.models.drive.File

class FilesRepository(
    private val db: FilesDatabase
) {

    suspend fun getDriveFiles(
        pageSize: Int,
        accessToken: String,
        pageToken: String,
        driveQuery: String
    ) =
        RetrofitInstance.api.getDriveFiles(
            pageSize = pageSize,
            accessToken = accessToken,
            pageToken = pageToken,
            q = driveQuery
        )

    suspend fun upsert(file: File) = db.getFilesDao().upsert(file)

    fun getSavedFiles() = db.getFilesDao().getAllFiles()

    suspend fun deleteFile(file: File) = db.getFilesDao().deleteFile(file)

}