package ngohoanglong.com.awesomemangareader.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ngohoanglong.com.awesomemangareader.BuildConfig;
import rx.Observable;
import rx.Subscriber;

import static android.content.ContentValues.TAG;

/**
 * Created by Admin on 12/03/2017.
 */

public class ImageUtils {
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 20; // 10MB
    private static final int DISK_CACHE_INDEX = 0;
    private static final String CACHE_DIR = "diskLruCache";

    private final Object mDiskCacheLock = new Object();
    public static DiskLruCache diskLruCache;

    private static Context context;

    public ImageUtils(Context context) {

        this.context = context;
        initDiskCache(context);
    }

    static void initDiskCache(Context context) {
        if (diskLruCache == null || diskLruCache.isClosed()) {
            try {
                File dir = getDiskCacheDir(context, CACHE_DIR);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                diskLruCache = DiskLruCache.open(dir, 1, 1, DEFAULT_DISK_CACHE_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Observable<String> addBitmapToCatche(final String url) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Loading !!!");
                if (diskLruCache != null) {
                    final String key = hashKeyForDisk(url);
                    OutputStream out = null;
                    InputStream input = null;
                    int count;
                    try {
                        DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
                        if (snapshot == null) {
                            final DiskLruCache.Editor editor = diskLruCache.edit(key);
                            if (editor != null) {
                                out = editor.newOutputStream(DISK_CACHE_INDEX);
                                URL u = new URL(url);
                                URLConnection conection = u.openConnection();
                                conection.connect();
                                int lenghtOfFile = conection.getContentLength();
                                input = new BufferedInputStream(u.openStream(), 8192);

                                byte data[] = new byte[1024];

                                long total = 0;
                                int percent = 0;
                                while ((count = input.read(data)) != -1) {
                                    total += count;
                                    final int newPercent = (int) ((total * 100) / lenghtOfFile);
                                    if (newPercent != percent) {
                                        percent = newPercent;
                                        subscriber.onNext(percent + "%");
                                    }

                                    out.write(data, 0, count);
                                }

                                editor.commit();
                                out.close();
                                subscriber.onCompleted();
                            }
                        } else {
                            snapshot.getInputStream(DISK_CACHE_INDEX).close();
                        }
                    } catch (final IOException e) {
                        Log.e(TAG, "addBitmapToCache - " + e);
                        subscriber.onError(e);
                    } catch (Exception e) {
                        Log.e(TAG, "addBitmapToCache - " + e);
                        subscriber.onError(e);
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                            if (input != null) {
                                input.close();
                            }
                        } catch (IOException e) {
                            subscriber.onError(e);
                        }
                    }

                }
                subscriber.onCompleted();
            }
        });

    }


    public static Bitmap getBitmapFromDiskCache(String url) {
        //BEGIN_INCLUDE(get_bitmap_from_disk_cache)
        final String key = hashKeyForDisk(url);
        Bitmap bitmap = null;

        if (diskLruCache != null) {
            InputStream inputStream = null;
            try {
                final DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
                if (snapshot != null) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Disk cache hit");
                    }
                    inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                    if (inputStream != null) {
                        FileDescriptor fd = ((FileInputStream) inputStream).getFD();

                        bitmap = BitmapFactory.decodeFileDescriptor(fd);
                    }
                }
            } catch (final IOException e) {
                Log.e(TAG, "getBitmapFromDiskCache - " + e);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                }
            }
        }
        return bitmap;
    }

    public static void clearCache() {

        if (diskLruCache != null && !diskLruCache.isClosed()) {
            try {
                diskLruCache.delete();
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Disk cache cleared");
                }
            } catch (IOException e) {
                Log.e(TAG, "clearCache - " + e);
            }
            diskLruCache = null;
            initDiskCache(context);
        }

    }


    static File getDiskCacheDir(Context context, String uniqueName) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(uniqueName, Context.MODE_PRIVATE);
        return directory;
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
