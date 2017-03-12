package ngohoanglong.com.awesomemangareader.activity.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

import ngohoanglong.com.awesomemangareader.Page;
import ngohoanglong.com.awesomemangareader.utils.DownloadUtils;
import ngohoanglong.com.awesomemangareader.R;
import ngohoanglong.com.awesomemangareader.service.ThreadPoolDownloadService;
import ngohoanglong.com.awesomemangareader.activity.DetailActivity;

import static android.content.ContentValues.TAG;
import static ngohoanglong.com.awesomemangareader.MangaReaderApp.context;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UsingServiceMangaPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsingServiceMangaPageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";


    // TODO: Rename and change types of parameters
    private Page page;


    public UsingServiceMangaPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param page Parameter 1.
     * @return A new instance of fragment UsingServiceMangaPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UsingServiceMangaPageFragment newInstance(Page page) {
        UsingServiceMangaPageFragment fragment = new UsingServiceMangaPageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, page);
        fragment.setArguments(args);
        return fragment;
    }

    LruCache<String, Bitmap> lruCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ARG_PARAM1, page);
    }

    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (getArguments() != null) {
            page = (Page) getArguments().getSerializable(ARG_PARAM1);
            Log.d(TAG, "onCreateView: " + page.getImageList().size());
        }
        View view = inflater.inflate(R.layout.fragment_manga_page, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.rvImages);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
//        recyclerView.setAdapter(new MangaAdapter(page.getImageList()));
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
            if (position % 2 == 0) {
                holder.viewAnimator.setInAnimation(holder.imageView.getContext(), R.anim.in_from_left);
            } else {
                holder.viewAnimator.setInAnimation(holder.imageView.getContext(), R.anim.in_from_right);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((Activity) holder.itemView.getContext(), (View) holder.imageView, strings.get(position));
                    startActivity(DetailActivity.getActivityIntent(holder.imageView.getContext(), strings.get(position)), options.toBundle());
                }
            });
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            holder.handler.clear();
            holder.handler = null;
            holder.viewAnimator.setDisplayedChild(0);
            Log.d(TAG, "onViewRecycled: ");
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
            String url;
            MessengerHandler handler = null;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.ivImage);
                percent = (TextView) itemView.findViewById(R.id.tvStatus);
                viewAnimator = (ViewAnimator) itemView.findViewById(R.id.avPageStage);

            }

            void loadImage(String url) {
                viewAnimator.setDisplayedChild(0);
                this.url = url;
                handler = new MessengerHandler(this, url);
                percent.setText("Loading !!!");
                if (imageView == null) return;
                Bitmap bm = lruCache.get(url);
                if (bm != null) {
                    Log.d(TAG, "onBindVieHolder: " + url);

                    imageView.setImageBitmap(bm);
                    viewAnimator.setDisplayedChild(1);
                    return;
                }
                try {
                    final File file = DownloadUtils.getTemporaryFile(context,
                            url);
                    int count;
                    Log.d(TAG, "    downloading to " + file);
                    if (file.getAbsoluteFile().exists()) {
                        Log.d(TAG, "file.exists():true ");
                        imageView.setImageBitmap(bm);
                        viewAnimator.setDisplayedChild(1);
                        return;
                    }

                } catch (Exception e) {
                    Log.d(TAG, "onBindVieHolder: " + e.getMessage());
                }
                Intent threadsIntent = ThreadPoolDownloadService.makeIntent(itemView.getContext(), handler,
                        url);
                itemView.getContext().startService(threadsIntent);
            }

            class MessengerHandler extends Handler {
                WeakReference<ViewHolder> outerClass;
                String url;

                public MessengerHandler(ViewHolder outer, String url) {
                    outerClass = new WeakReference<ViewHolder>(outer);
                    this.url = url;
                }

                public void clear() {
                    outerClass.clear();
                }

                ;

                @Override
                public void handleMessage(Message msg) {
                    final ViewHolder vh = outerClass.get();

                    if (vh != null && vh.imageView != null) {
                        Bitmap bm = BitmapFactory.decodeFile(msg.getData().getString(DownloadUtils.PATHNAME_KEY));
                        if (bm != null) {
                            final double wRatio = (double) bm.getWidth() / (double) vh.imageView.getMeasuredWidth();
                            final int w = vh.imageView.getMeasuredWidth();
                            final int h = (int) (bm.getHeight() / wRatio);
                            if (w > 0 && h > 0) {
                                bm = Bitmap.createScaledBitmap(bm, w, h, false);
                            }

                        }
                        if (bm != null) {
                            lruCache.put(url, bm);

                            vh.imageView.setImageBitmap(bm);
                            vh.viewAnimator.setDisplayedChild(1);
                        }


                    }
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
