package zechs.zplex;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import static zechs.zplex.utils.Constants.API;

public class FetchDatabaseDialog extends Dialog {

    public FetchDatabaseDialog(Activity a) {
        super(a);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.fetch_dialog);

        File dbFile = new File(getContext().getFilesDir() + "/dbJson.json");
        if (dbFile.exists()) {
            boolean isDeleted = dbFile.delete();
            Log.d("isDeleted", String.valueOf(isDeleted));
        }

        if (getContext() != null) {
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            String url = API + "/media/library";

            JsonArrayRequest objectArray = new JsonArrayRequest(url, response -> {
                try {
                    FileWriter file = new FileWriter(getContext().getFilesDir() + "/dbJson.json");
                    file.write(response.toString());
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to load library", Toast.LENGTH_SHORT).show();
                }
                dismiss();
                Log.d("FetchDatabaseDialog", "executed");

            }, error -> {
                Toast.makeText(getContext(), "Failed to fetch library, Please try again!", Toast.LENGTH_SHORT).show();
                dismiss();
                Log.d("FetchDatabaseDialog", Arrays.toString(error.getStackTrace()));

            });
            objectArray.setShouldCache(false);
            objectArray.setRetryPolicy(new DefaultRetryPolicy(5000, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(objectArray);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}