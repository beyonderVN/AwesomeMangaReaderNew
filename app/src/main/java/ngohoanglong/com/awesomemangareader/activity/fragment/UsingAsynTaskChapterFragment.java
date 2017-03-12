package ngohoanglong.com.awesomemangareader.activity.fragment;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ngohoanglong.com.awesomemangareader.AppState;
import ngohoanglong.com.awesomemangareader.R;
import ngohoanglong.com.awesomemangareader.activity.DetailActivity;
import ngohoanglong.com.awesomemangareader.model.Chapter;
import ngohoanglong.com.awesomemangareader.model.Image;
import ngohoanglong.com.awesomemangareader.utils.DownloadUtils;

import static android.content.ContentValues.TAG;
import static ngohoanglong.com.awesomemangareader.MangaReaderApp.NUMBER_OF_CORES;
import static ngohoanglong.com.awesomemangareader.MangaReaderApp.context;


public class UsingAsynTaskChapterFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    ExecutorService loadImageExecutor;

    private Chapter chapter;


    public UsingAsynTaskChapterFragment() {

    }

    public static UsingAsynTaskChapterFragment newInstance(int page) {
        UsingAsynTaskChapterFragment fragment = new UsingAsynTaskChapterFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, page);
        fragment.setArguments(args);
        return fragment;
    }

    LruCache<String, Bitmap> lruCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            final int pst = getArguments().getInt(ARG_PARAM1);
            chapter = AppState.chapeters.get(pst);
            Log.d(TAG, "onCreateView: " + chapter.getImageList().size());
        }

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

        } else {
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            Log.d(TAG, "onCreateView: " + chapter.getImageList().size());
        }
    }

    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_manga_page, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.rvImages);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new MangaAdapter(chapter.getImageList()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadImageExecutor = Executors.newFixedThreadPool(NUMBER_OF_CORES * 4);
    }

    @Override
    public void onPause() {
        loadImageExecutor.shutdown();
        super.onPause();
    }

    class MangaAdapter extends RecyclerView.Adapter<MangaAdapter.ViewHolder> {


        List<Image> images;

        public MangaAdapter(List<Image> images) {
            this.images = images;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            holder.imageView.setTransitionName(images.get(position).getUrl());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((Activity) holder.itemView.getContext(), (View) holder.imageView, images.get(position).getUrl());
                    startActivity(DetailActivity.getActivityIntent(holder.imageView.getContext(), images.get(position).getUrl()), options.toBundle());
                }
            });
            holder.loadImage(images.get(position).getUrl());
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            Log.d(TAG, "onViewRecycled: ");
            if (holder.loadImageTask != null) {
                holder.loadImageTask.cancel(true);
            }
            super.onViewRecycled(holder);

        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView percent;
            ViewAnimator viewAnimator;
            String url;
            LoadImageTask loadImageTask;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.ivImage);
                percent = (TextView) itemView.findViewById(R.id.tvStatus);
                viewAnimator = (ViewAnimator) itemView.findViewById(R.id.avPageStage);

            }

            void loadImage(String url) {
                percent.setText("loading !!!");
                viewAnimator.setDisplayedChild(0);
                imageView.setImageDrawable(null);
                this.url = url;
                Bitmap bm = lruCache.get(url);
                if (bm != null) {
                    viewAnimator.setDisplayedChild(1);
                    imageView.setImageBitmap(bm);
                    return;
                }

                File file = null;

                if (file != null && file.getAbsoluteFile().exists()) {
                    Log.d(TAG, "file.exists():true ");
                    viewAnimator.setDisplayedChild(1);
                    imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                    return;
                }

                viewAnimator.setDisplayedChild(0);
                loadImageTask = new LoadImageTask(this, url);
                if (loadImageExecutor != null) {
                    loadImageTask.executeOnExecutor(loadImageExecutor);
                } else {
                    loadImageTask.execute();
                }

            }


        }


    }

    public class LoadImageTask extends AsyncTask<String, Integer, String> {
        WeakReference<MangaAdapter.ViewHolder> reference;
        String url;

        public LoadImageTask(MangaAdapter.ViewHolder viewHolder, String url) {
            reference = new WeakReference<MangaAdapter.ViewHolder>(viewHolder);
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
                    final MangaAdapter.ViewHolder viewHolder = reference.get();

                    if (viewHolder != null) {
                        if (isCancelled()||viewHolder.url!=url||viewHolder.loadImageTask!=this) {
                            publishProgress(-1);
                            os.close();
                            input.close();
                        }
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
            } catch (
                    Exception e)

            {
                Log.d(TAG, "Exception: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            final MangaAdapter.ViewHolder viewHolder = reference.get();

            if (viewHolder != null && viewHolder.percent != null) {

                if (values[0] == -1) {
                    Log.d(TAG, "onProgressUpdate: Cancel");
                    viewHolder.percent.setText("Cancel!!");
                    return;
                }
                Log.d(TAG, "onProgressUpdate: " + values[0]);
                viewHolder.percent.setText(values[0] + "%");
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            final MangaAdapter.ViewHolder viewHolder = reference.get();
            if (viewHolder != null && viewHolder.url == url) {
                Bitmap bm = BitmapFactory.decodeFile(result);
                if (bm != null) {
                    final double wRatio = (double) bm.getWidth() / (double) viewHolder.imageView.getMeasuredWidth();
                    final int w = viewHolder.imageView.getMeasuredWidth() * 3 / 2;
                    final int h = (int) (bm.getHeight() * 3 / 2 / wRatio);
                    if (w > 0 && h > 0) {
                        bm = Bitmap.createScaledBitmap(bm, w, h, false);
                    }
                    lruCache.put(url, bm);

                    viewHolder.imageView.setImageBitmap(bm);
                    viewHolder.viewAnimator.setDisplayedChild(1);
                } else {
                    viewHolder.percent.setText("Error!");
                }

            }

        }

    }


}
