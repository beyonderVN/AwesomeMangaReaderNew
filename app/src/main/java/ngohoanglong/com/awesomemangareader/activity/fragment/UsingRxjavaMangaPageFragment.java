package ngohoanglong.com.awesomemangareader.activity.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import ngohoanglong.com.awesomemangareader.activity.DetailActivity;
import ngohoanglong.com.awesomemangareader.utils.DownloadUtils;
import ngohoanglong.com.awesomemangareader.Page;
import ngohoanglong.com.awesomemangareader.R;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static ngohoanglong.com.awesomemangareader.MangaReaderApp.context;


public class UsingRxjavaMangaPageFragment extends Fragment {
    private static final String TAG = "UsingRxjavaMangaPageFra";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //Data
    private Page page;
    LruCache<String, Bitmap> lruCache;


    //View
    RecyclerView recyclerView;

    //Rx
    private static final int START = 0;
    private static final int STOP = 1;
    private PublishSubject<Integer> stopEvent = PublishSubject.create();
    private PublishSubject<Integer> startEvent = PublishSubject.create();

    public Observable<Integer> stopEvent() {
        return stopEvent.asObservable()
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Log.d(TAG, "stopEvent: " + integer);
                    }
                });
    }

    public Observable<Integer> startEvent() {
        return startEvent.asObservable();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (getUserVisibleHint()&&getUserVisibleHint()!=isVisibleToUser) {
            Log.d(TAG, "getUserVisibleHint() ");
            Log.d(TAG, "isVisibleToUser: ");
            stopEvent.onNext(STOP);
        } else {
//            stopEvent.onNext(STOP);
        }
        super.setUserVisibleHint(isVisibleToUser);

    }

    @Override
    public void onStart() {
        super.onStart();
        startEvent.onNext(START);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopEvent.onNext(STOP);
    }

    public static UsingRxjavaMangaPageFragment newInstance(Page page) {
        UsingRxjavaMangaPageFragment fragment = new UsingRxjavaMangaPageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            page = (Page) getArguments().getSerializable(ARG_PARAM1);
            Log.d(TAG, "onCreateView: " + page.getImageList().size());
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getArguments() != null) {
            page = (Page) getArguments().getSerializable(ARG_PARAM1);
            Log.d(TAG, "onCreateView: " + page.getImageList().size());
        }
        View view = inflater.inflate(R.layout.fragment_using_rxjava_manga_page, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.rvImages);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
//        recyclerView.setAdapter(new MangaAdapter(page.getImageList()));

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ARG_PARAM1, page);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

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

            holder.imageView.setTransitionName(strings.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((Activity) holder.itemView.getContext(), (View) holder.imageView, strings.get(position));
                    startActivity(DetailActivity.getActivityIntent(holder.imageView.getContext(), strings.get(position)), options.toBundle());
                }
            });
            holder.loadImage(strings.get(position));
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            if (holder.subscription != null) holder.subscription.unsubscribe();
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
            Subscription subscription;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.ivImage);
                percent = (TextView) itemView.findViewById(R.id.tvStatus);
                viewAnimator = (ViewAnimator) itemView.findViewById(R.id.avPageStage);
            }

            void loadImage(final String url) {
                subscription = null;
                imageView.setImageDrawable(null);
                percent.setText("0%");
                viewAnimator.setDisplayedChild(0);
                Bitmap bm = lruCache.get(url);
                if (bm != null) {

                    imageView.setImageBitmap(bm);
                    viewAnimator.setDisplayedChild(1);
                    return;
                }
                subscription = Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        int count;
                        InputStream input = null;
                        OutputStream os = null;

                        try {
                            final File file = DownloadUtils.getTemporaryFile(context,
                                    url);
                            if (file.getAbsoluteFile().exists()) {
                                if(!subscriber.isUnsubscribed()){
                                    subscriber.onNext(file.getAbsolutePath());
                                    subscriber.onCompleted();
                                }
                            }
                            URL u = new URL(url);
                            URLConnection conection = u.openConnection();
                            conection.connect();
                            int lenghtOfFile = conection.getContentLength();
                            input = new BufferedInputStream(u.openStream(), 8192);

                            os = new FileOutputStream(file);

                            byte data[] = new byte[1024];
                            subscriber.onNext("0%");
                            long total = 0;
                            int percent = 0;
                            while ((count = input.read(data)) != -1) {
                                total += count;
                                final int newPercent = (int) ((total * 100) / lenghtOfFile);
                                if (newPercent != percent) {
                                    subscriber.onNext(percent + "%");
                                    percent = newPercent;
                                }
                                os.write(data, 0, count);
                            }

                            os.flush();
                            os.close();
                            input.close();
                            if(!subscriber.isUnsubscribed()){
                                subscriber.onNext(file.getAbsolutePath());
                                subscriber.onCompleted();
                            }

                        } catch (Exception e) {

                            Log.d(TAG, "Exception: " + e.getMessage());
                        } finally {
                            if (input != null) {
                                try {
                                    input.close();
                                } catch (IOException ioe) {
                                }
                            }
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e) {
                                }
                            }

                        }



                    }
                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
//                        .skipUntil(startEvent)
                        .takeUntil(stopEvent())
                        .doOnSubscribe(new Action0() {
                            @Override
                            public void call() {
                                percent.setText("0%");
                                viewAnimator.setDisplayedChild(0);
                            }
                        })

                        .doOnNext(new Action1<String>() {
                                      @Override
                                      public void call(String s) {
                                          Log.d(TAG, "doOnNext: "+s);
                                          percent.setText(s);
                                      }
                                  }
                        )
                        .last()
                        .subscribe(new Subscriber<String>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "onCompleted: ");


                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "onError: ");
                            }

                            @Override
                            public void onNext(String s) {
                                Log.d(TAG, "onNext: "+s);
                                Bitmap bm = BitmapFactory.decodeFile(s);
                                if (bm != null) {
                                    final double wRatio = (double) bm.getWidth() / (double) imageView.getMeasuredWidth();
                                    final int w = imageView.getMeasuredWidth()*5/3;
                                    final int h = (int) (bm.getHeight()*5/3 / wRatio);
                                    if (w > 0 && h > 0) {
                                        bm = Bitmap.createScaledBitmap(bm, w, h, false);
                                    }
                                    lruCache.put(url, bm);
                                    imageView.setImageBitmap(bm);
                                    viewAnimator.setDisplayedChild(1);
                                } else {
                                    percent.setText("Error!");
                                }
                            }
                        });
            }


        }


    }


}
