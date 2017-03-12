package ngohoanglong.com.awesomemangareader;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import java.io.File;

import ngohoanglong.com.awesomemangareader.utils.ImageUtils;

/**
 * Created by Admin on 09/03/2017.
 */
//http://222.255.207.13:5000/fsdownload/0S628zt2x/JSON%20files.zip
public class MangaReaderApp extends Application {
    private static final String TAG = "MangaReaderApp";

    public static Context context   ;
    public static int width = 0;
    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LeakCanary.install(this);
        new ImageUtils(this);

    }


    public static void deleteAllLocalImages(){
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
