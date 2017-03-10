package ngohoanglong.com.awesomemangareader;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MangaPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MangaPageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";


    // TODO: Rename and change types of parameters
    private MangaPage mangaPage;



    public MangaPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mangaPage Parameter 1.
     * @return A new instance of fragment MangaPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MangaPageFragment newInstance(MangaPage mangaPage) {
        MangaPageFragment fragment = new MangaPageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, mangaPage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ARG_PARAM1, mangaPage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (getArguments() != null) {
            mangaPage = (MangaPage) getArguments().getSerializable(ARG_PARAM1);
            Log.d(TAG, "onCreateView: "+mangaPage.getImageList().size());
        }
        View view = inflater.inflate(R.layout.fragment_manga_page, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rvImages);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        recyclerView.setAdapter(new MangaAdapter(mangaPage.getImageList()));
        return view;
    }

    class MangaAdapter extends RecyclerView.Adapter<MangaAdapter.ViewHolder>{


        List<String> strings;

        public MangaAdapter(List<String> strings) {
            this.strings = strings;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image, parent, false);
            return new ViewHolder (view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.loadImage(strings.get(position));
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            holder.cancelTask();
            super.onViewRecycled(holder);
        }

        @Override
        public int getItemCount() {
            return strings.size();
        }

        class  ViewHolder extends RecyclerView.ViewHolder {
            ThumpImageView imageView;
            DownloadImageTask downloadImageTask;
            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ThumpImageView) itemView.findViewById(R.id.ivImage);
            }
            void loadImage(String url){
                downloadImageTask = new DownloadImageTask(imageView);
                downloadImageTask.execute(url);
            }
            public void cancelTask() {
                downloadImageTask.cancel(true);
            }
        }

        private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
            private static final String TAG = "DownloadImageTask";
            WeakReference<ThumpImageView> weakImageView;

            @Override
            protected void onCancelled() {
                Log.d(TAG, "onCancelled: ");
                weakImageView.clear();
                super.onCancelled();
            }

            public DownloadImageTask(ThumpImageView bmImage) {
                this.weakImageView = new WeakReference<ThumpImageView>(bmImage);
            }

            protected Bitmap doInBackground(String... urls) {
                String urldisplay = urls[0];
                Bitmap bm = null;
                String fileName = "";
                Log.d(TAG, "doInBackground: "+urldisplay);
                fileName = urldisplay.substring(urldisplay.lastIndexOf("/") + 1);
                fileName = fileName+".jpg";
                if((bm = loadImageFromStorage(fileName))!=null){
                    Log.d(TAG, "getImageFromLocal: ");
                    return bm;
                }
                try {
                    Log.d(TAG, "getImageFromRemote: ");
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    bm = BitmapFactory.decodeStream(in);

                    double ratio = 1;
                    if (bm!=null){
                        ratio = (double)bm.getWidth()/(double)bm.getHeight();
                    }
                    int width = MangaReaderApp.width;
                    int height = (int) ((double)width/ratio);
                    bm = Bitmap.createScaledBitmap(bm, width, height, false);
                    if(saveToInternalStorage(bm,fileName)){
                        bm = loadImageFromStorage(fileName);
                    }
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                    bm = null;
                }
                return bm;
            }

            protected void onPostExecute(Bitmap result) {
                ThumpImageView iv = weakImageView.get();
                if (iv != null) {
                    iv.setImageBitmap(result);
                }
            }
            private boolean saveToInternalStorage(Bitmap bitmapImage, String fileName){

                ContextWrapper cw = new ContextWrapper(MangaReaderApp.context);
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                File mypath=new File(directory,fileName);
                FileOutputStream fos = null;

                try {
                    fos = new FileOutputStream(mypath);
                    // Use the compress method on the BitMap object to write image to the OutputStream
                    if(bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)){
                        Log.d(TAG, "saveToInternalStorage: success");
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
            private Bitmap loadImageFromStorage(String fileName)
            {
                try {
                    ContextWrapper cw = new ContextWrapper(MangaReaderApp.context);
                    File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                    File f=new File(directory.getPath(), fileName);
                    Log.d(TAG, "loadImageFromStorage: "+directory.getPath()+ fileName);
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                    return b;
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                    return null;
                }


            }

        }
    }
}
