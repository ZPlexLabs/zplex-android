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
        fields: String = "nextPageToken, files(id, name)",
        @Query("q")
        q: String = "mimeType='application/vnd.google-apps.folder' and parents in '0AASFDMjRqUB0Uk9PVA' and trashed = false",
    ): Response<DriveResponse>

}