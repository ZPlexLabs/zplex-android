package zechs.zplex.utils;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIHolder {


    @GET("/media/library")
    Call<ResponseBody> getLibrary();

    @GET("/media/info")
    Call<ResponseBody> getInfo(@Query("type") String Type, @Query("name") String Name);

    @GET("/media/files")
    Call<ResponseBody> getEpisodes(@Query("type") String Type, @Query("name") String Name);

}
