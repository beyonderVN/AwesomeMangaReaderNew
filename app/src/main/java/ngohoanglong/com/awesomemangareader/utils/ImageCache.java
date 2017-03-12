package ngohoanglong.com.awesomemangareader.utils;

import java.util.HashMap;

import ngohoanglong.com.awesomemangareader.model.Image;

/**
 * Created by Admin on 12/03/2017.
 */

public class ImageCache {
    private static final ImageCache ourInstance = new ImageCache();

    HashMap<String,Image> imagesHM;
//    DiskLruCache diskLruCache ;


    public static ImageCache getInstance() {
        return ourInstance;
    }

    private ImageCache() {
        imagesHM = new HashMap<>();

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
//        diskLruCache = new DiskLruCache();
    }


}
