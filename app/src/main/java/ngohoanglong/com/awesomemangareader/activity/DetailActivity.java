package ngohoanglong.com.awesomemangareader.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.ViewAnimator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

import ngohoanglong.com.awesomemangareader.utils.DownloadUtils;
import ngohoanglong.com.awesomemangareader.customview.MyImageView;
import ngohoanglong.com.awesomemangareader.R;

import static ngohoanglong.com.awesomemangareader.MangaReaderApp.context;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";
    static final String URL_IMAGE = "URL_IMAGE";

    MyImageView imageView;
    TextView percent;
    ViewAnimator viewAnimator;
    String urlImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        urlImage = getIntent().getExtras().getString(URL_IMAGE);
        imageView = (MyImageView) findViewById(R.id.ivImage);
        percent = (TextView) findViewById(R.id.tvStatus);
        viewAnimator = (ViewAnimator) findViewById(R.id.avPageStage);
        imageView.setTransitionName(urlImage);

    }
    LoadImageTask loadImageTask;
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        loadImageTask = new LoadImageTask(this, urlImage);
        loadImageTask.execute();
    }

    public static Intent getActivityIntent(Context context, String url) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(URL_IMAGE, url);
        return intent;

    }

    public class LoadImageTask extends AsyncTask<String, Integer, Bitmap> {
        WeakReference<Activity> reference;
        String url;

        public LoadImageTask(Activity activity, String url) {
            reference = new WeakReference<Activity>(activity);
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            int count;
            try {
                final File file = DownloadUtils.getTemporaryFile(context,
                        url);
                Log.d(TAG, "    downloading to " + file);
                if (file.getAbsoluteFile().exists()) {
                    Log.d(TAG, "file.exists():true ");
                    return BitmapFactory.decodeFile(file.getAbsolutePath());
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
                    if (this.isCancelled()){
                        publishProgress(-1);
                        // closing streams
                        os.close();
                        input.close();

                        input.close();
                        os.close();
                        return null;
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
                input.close();
                os.close();
                return BitmapFactory.decodeFile(file.getAbsolutePath());
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            final DetailActivity detailActivity = (DetailActivity) reference.get();
            if (detailActivity != null && detailActivity.percent != null) {
                if (values[0] == -1) {
                    detailActivity.percent.setText("Cancel!!");
                    return;
                }
                detailActivity.percent.setText(values[0] + "%");
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            final DetailActivity detailActivity = (DetailActivity) reference.get();

            if (bitmap != null) {
                final double wRatio = (double) bitmap.getWidth() / (double) detailActivity.imageView.getMeasuredWidth();
                final int w = detailActivity.imageView.getMeasuredWidth()*3;
                final int h = (int) (bitmap.getHeight() *3/ wRatio);
                if (w > 0 && h > 0) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false);
                }
                detailActivity.viewAnimator.setDisplayedChild(1);
                detailActivity.imageView.setImageBitmap(bitmap);
            } else {
                detailActivity.percent.setText("Error!");
            }


        }

    }

    @Override
    protected void onDestroy() {
        loadImageTask.cancel(true);
        super.onDestroy();
    }
}
