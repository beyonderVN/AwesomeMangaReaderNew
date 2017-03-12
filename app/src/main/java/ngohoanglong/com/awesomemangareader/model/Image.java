package ngohoanglong.com.awesomemangareader.model;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import ngohoanglong.com.awesomemangareader.utils.DownloadUtils;

import static android.content.ContentValues.TAG;
import static ngohoanglong.com.awesomemangareader.MangaReaderApp.context;

/**
 * Created by Admin on 12/03/2017.
 */
;

public class Image {
    String url;
    Bitmap bitmap;


    public void setStatus(int status) {
        this.status = status;
    }

    int status = 0;

    public Image() {
    }

    public Image(String url, Bitmap bitmap) {
        this.url = url;
        this.bitmap = bitmap;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Image{" +
                "url='" + url + '\'' +
                ", bitmap=" + bitmap +
                ", status=" + status +
                '}';
    }


    public class LoadImageTask extends AsyncTask<String, Integer, String> {
        String url;

        public LoadImageTask(String url) {
            this.url = url;
        }

        @Override
        protected String doInBackground(String... params) {
            int count;
            try {
                final File file = DownloadUtils.getTemporaryFile(context,
                        url);
                Log.d(TAG, "    downloading to " + file);
                if (file.getAbsoluteFile().exists()) {
                    Log.d(TAG, "file.exists():true ");
                    file.getAbsolutePath();
                }
                URL u = new URL(url);
                URLConnection conection = u.openConnection();
                conection.connect();
                int lenghtOfFile = conection.getContentLength();
                InputStream input = new BufferedInputStream(u.openStream(), 8192);

                final OutputStream os =
                        new FileOutputStream(file);

                byte data[] = new byte[1024];

                long total = 0;
                int percent = 0;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        publishProgress(-1);
                        os.close();
                        input.close();
                        cancel(true);
                    }
                    total += count;
                    final int newPercent = (int) ((total * 100) / lenghtOfFile);
                    if (newPercent != percent) {
                        publishProgress(percent);
                        percent = newPercent;
                    }
                    os.write(data, 0, count);
                }

                os.flush();
                os.close();
                input.close();
                return file.getAbsolutePath();
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            Log.d(TAG, "onProgressUpdate: " + values[0]);

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

}
