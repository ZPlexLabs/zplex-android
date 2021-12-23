package zechs.zplex.repository

import zechs.zplex.api.RetrofitInstance

class FilesRepository {

    suspend fun getDriveFiles(
        pageSize: Int,
        accessToken: String,
        pageToken: String,
        driveQuery: String,
        orderBy: String
    ) =
        RetrofitInstance.api.getDriveFiles(
            pageSize = pageSize,
            accessToken = accessToken,
            pageToken = pageToken,
            q = driveQuery,
            orderBy = orderBy
        )
}