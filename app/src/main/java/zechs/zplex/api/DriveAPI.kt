package zechs.zplex.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import zechs.zplex.models.drive.DriveResponse

interface DriveAPI {

    @GET("drive/v3/files")
    suspend fun getDriveFiles(
        @Query("access_token")
        accessToken: String,
        @Query("supportsAllDrives")
        supportsAllDrives: Boolean = true,
        @Query("includeItemsFromAllDrives")
        includeItemsFromAllDrives: Boolean = true,
        @Query("pageSize")
        pageSize: Int,
        @Query("pageToken")
        pageToken: String = "",
        @Query("fields")
        fields: String = "nextPageToken, files(id, name, size, thumbnailLink)",
        @Query("q")
        q: String = "mimeType='application/vnd.google-apps.folder' and '0AASFDMjRqUB0Uk9PVA' in parents and trashed = false",
        @Query("orderBy")
        orderBy: String = "modifiedTime desc"
    ): Response<DriveResponse>

}