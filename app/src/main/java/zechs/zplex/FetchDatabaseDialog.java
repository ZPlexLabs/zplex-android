package zechs.zplex;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import zechs.zplex.utils.APIHolder;

import static zechs.zplex.utils.Constants.API;

public class FetchDatabaseDialog extends Dialog {

    public final Activity activity;

    public FetchDatabaseDialog(Activity a) {
        super(a);
        this.activity = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.fetch_dialog);

        File dbFile = new File(getContext().getFilesDir() + "/dbJson.json");
        if (dbFile.exists())
            dbFile.delete();

        Retrofit retrofit1 = new Retrofit.Builder()
                .baseUrl(API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        APIHolder randomAPI = retrofit1.create(APIHolder.class);
        Call<ResponseBody> callQuery = randomAPI.getLibrary();
        callQuery.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), "Failed to fetch library", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    try {
                        if (response.body() != null) {
                            String JsonRequest = response.body().string();
                            try {
                                FileWriter file = new FileWriter(getContext().getFilesDir() + "/dbJson.json");
                                file.write(JsonRequest);
                                file.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            dismiss();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "IOException e", Toast.LENGTH_SHORT).show();

                    }
                }

            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Toast.makeText(getContext(), "Failed to load library", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}