package ngohoanglong.com.awesomemangareader;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static android.content.ContentValues.TAG;
import static java.lang.System.in;
import static ngohoanglong.com.awesomemangareader.MangaReaderApp.context;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UsingAsynTaskMangaPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsingAsynTaskMangaPageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";


    // TODO: Rename and change types of parameters
    private MangaPage mangaPage;


    public UsingAsynTaskMangaPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mangaPage Parameter 1.
     * @return A new instance of fragment UsingServiceMangaPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UsingAsynTaskMangaPageFragment newInstance(MangaPage mangaPage) {
        UsingAsynTaskMangaPageFragment fragment = new UsingAsynTaskMangaPageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, mangaPage);
        fragment.setArguments(args);
        return fragment;
    }
    LruCache<String,Bitmap> lruCache;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
final int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);
        final int cacheSize = maxMemory/8;

        lruCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount()/1024;
            }
        };

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ARG_PARAM1, mangaPage);
    }
    RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (getArguments() != null) {
            mangaPage = (MangaPage) getArguments().getSerializable(ARG_PARAM1);
            Log.d(TAG, "onCreateView: " + mangaPage.getImageList().size());
        }
        View view = inflater.inflate(R.layout.fragment_manga_page, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.rvImages);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new MangaAdapter(mangaPage.getImageList()));
        return view;
    }

    @Override
    public void onStop() {

        super.onStop();

    }

    class MangaAdapter extends RecyclerView.Adapter<MangaAdapter.ViewHolder> {


        List<String> strings;

        public MangaAdapter(List<String> strings) {
            this.strings = strings;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.loadImage(strings.get(position));
            holder.imageView.setTransitionName(strings.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((Activity) holder.itemView.getContext(), (View)holder.imageView, strings.get(position));
                    startActivity(DetailActivity.getActivityIntent(holder.imageView.getContext(),strings.get(position)),options.toBundle());
                }
            });
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            holder.viewAnimator.setDisplayedChild(0);
            holder.percent.setText("0%");
            Log.d(TAG, "onViewRecycled: ");
            holder.loadImageTask.cancel(true);
            super.onViewRecycled(holder);

        }

        @Override
        public int getItemCount() {
            return strings.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView percent;
            ViewAnimator viewAnimator;
            String url ;
            LoadImageTask loadImageTask;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.ivImage);
                percent = (TextView) itemView.findViewById(R.id.tvPercent);
                viewAnimator = (ViewAnimator) itemView.findViewById(R.id.avPageStage);
            }

            void loadImage(String url) {

                this.url = url;
                Bitmap bm = lruCache.get(url);
                if(bm!=null){
                    viewAnimator.setDisplayedChild(1);
                    imageView.setImageBitmap(bm);
                    return;
                }
                loadImageTask = new LoadImageTask(this,url);
                loadImageTask.execute();
            }


        }


    }
    public  class LoadImageTask extends AsyncTask<String,Integer,Bitmap>{
        WeakReference<MangaAdapter.ViewHolder> reference;
        String url ;
        public LoadImageTask(MangaAdapter.ViewHolder viewHolder, String url) {
            reference = new WeakReference<MangaAdapter.ViewHolder>(viewHolder);
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            int count;
            try{
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
                // getting file length
                int lenghtOfFile = conection.getContentLength();

//                final InputStream in = (InputStream)
//                        new URL(url).getContent();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(u.openStream(), 8192);

                final OutputStream os =
                        new FileOutputStream(file);

                byte data[] = new byte[1024];

                long total = 0;
                int percent=0;
                while ((count = input.read(data)) != -1) {

                    final MangaAdapter.ViewHolder viewHolder = reference.get();
                    if(isCancelled()) {
                        publishProgress(-1);
                        // closing streams
                        os.close();
                        input.close();

                        in.close();
                        os.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    final int newPercent = (int) ((total * 100) / lenghtOfFile);
                    if(newPercent!=percent) {
                        publishProgress(percent);
                        percent=newPercent;
                    }
                    // writing data to file
                    os.write(data, 0, count);
                }

                // flushing output
                os.flush();

                // closing streams
                os.close();
                input.close();

                in.close();
                os.close();
                return BitmapFactory.decodeFile(file.getAbsolutePath());
            }catch (Exception e){
                Log.d(TAG, "Exception: "+e.getMessage());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            final MangaAdapter.ViewHolder viewHolder = reference.get();
            if(viewHolder!= null&&viewHolder.percent!=null){
                if(values[0]==-1) {
                    viewHolder.percent.setText("Cancel!!");
                    return;
                }
                viewHolder.percent.setText(values[0]+"%");
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            final MangaAdapter.ViewHolder viewHolder = reference.get();
            if(viewHolder.url == url){
                if(bitmap!=null) {
                    final double wRatio = (double) bitmap.getWidth() / (double) viewHolder.imageView.getMeasuredWidth();
                    final int w = viewHolder.imageView.getMeasuredWidth();
                    final int h = (int) (bitmap.getHeight() / wRatio);
                    if(w>0&&h>0){
                        bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false);
                    }
                    lruCache.put(url, bitmap);
                    viewHolder.viewAnimator.setDisplayedChild(1);
                    viewHolder.imageView.setImageBitmap(bitmap);
                }else {
                    viewHolder.percent.setText("Error!");
                }

            }

        }
    }

    public static Bitmap decodeBitmap(InputStream inputStream, int width, int height) throws IOException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;


        BitmapFactory.decodeStream(inputStream, null, options);
        options.inSampleSize = caculatorInSmapleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    private synchronized static int caculatorInSmapleSize(BitmapFactory.Options options, int width, int height) {
        final int h = options.outHeight;
        final int w = options.outWidth;
        int inSmapleSize = 16;
        if (width == 0 || height == 0) return inSmapleSize;
        if (h > height || w > width) {
            final int hRatio = Math.round((float) h / (float) height);
            final int wRatio = Math.round((float) w / (float) width);
            inSmapleSize = hRatio < wRatio ? hRatio : wRatio;

        }
        return inSmapleSize;
    }

    private synchronized static Bitmap loadImageFromStorage(String fileName) {
        FileInputStream fileInputStream = null;
        try {

            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f = new File(directory.getPath(), fileName);
            Log.d(TAG, "loadImageFromStorage: " + directory.getPath() + fileName);
            fileInputStream = new FileInputStream(f);
            Bitmap b = BitmapFactory.decodeStream(fileInputStream);
            fileInputStream.close();
            return b;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return null;
    }

    private synchronized static boolean saveToInternalStorage(Bitmap bitmapImage, String fileName) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory, fileName);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            if (bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                fos.close();
                return true;
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
