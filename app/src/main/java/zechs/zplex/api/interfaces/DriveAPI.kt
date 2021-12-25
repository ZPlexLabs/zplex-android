package zechs.zplex.api.interfaces

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
        fields: String = "nextPageToken, files(id, name, size)",
        @Query("q")
        q: String,
        @Query("orderBy")
        orderBy: String = "modifiedTime desc"
    ): Response<DriveResponse>

}