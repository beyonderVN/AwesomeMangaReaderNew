package ngohoanglong.com.awesomemangareader;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import java.io.File;

import okhttp3.OkHttpClient;

/**
 * Created by Admin on 09/03/2017.
 */

public class MangaReaderApp extends Application {
    private static final String TAG = "MangaReaderApp";
    public static Context context   ;
    public static int width = 0;
    public static OkHttpClient client;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
//        deleteLocalImages();
        client = new OkHttpClient();
    }


    void deleteLocalImages(){
        ContextWrapper cw = new ContextWrapper(MangaReaderApp.context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        if (directory.isDirectory())
        {
            String[] children = directory.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(directory, children[i]).delete();
                Log.d(TAG, "deleteLocalImages: "+children[i]);
            }
        }
    }



}
