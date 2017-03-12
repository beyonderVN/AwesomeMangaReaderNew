package ngohoanglong.com.awesomemangareader.activity.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
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

import java.util.List;

import ngohoanglong.com.awesomemangareader.AppState;
import ngohoanglong.com.awesomemangareader.R;
import ngohoanglong.com.awesomemangareader.activity.DetailActivity;
import ngohoanglong.com.awesomemangareader.model.Image;
import ngohoanglong.com.awesomemangareader.utils.ImageUtils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;


public class UsingRxjavaChapterFragment extends Fragment {
    private static final String TAG = "UsingRxjavaMangaPageFra";
    private static final String CHAPTER_POSITION = "CHAPTER_POSITION";

    //Data
    LruCache<String, Bitmap> lruCache;
    int chapterPosition;

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

    public static UsingRxjavaChapterFragment newInstance(int chapterPosition) {
        UsingRxjavaChapterFragment fragment = new UsingRxjavaChapterFragment();
        Bundle args = new Bundle();
        args.putInt(CHAPTER_POSITION, chapterPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chapterPosition = getArguments() != null ? getArguments().getInt(CHAPTER_POSITION) : null;


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

        View view = inflater.inflate(R.layout.fragment_using_rxjava_manga_page, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.rvImages);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new MangaAdapter(chapterPosition));

        return view;
    }

    class MangaAdapter extends RecyclerView.Adapter<MangaAdapter.ViewHolder> {


        List<Image> images;

        public MangaAdapter(int chapterPosition) {
            this.images = AppState.chapeters.get(chapterPosition).getImageList();
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
            holder.loadImage(images.get(position));
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            if (holder.subscription != null) holder.subscription.unsubscribe();
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
            Subscription subscription;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.ivImage);
                percent = (TextView) itemView.findViewById(R.id.tvStatus);
                viewAnimator = (ViewAnimator) itemView.findViewById(R.id.avPageStage);
            }

            void loadImage(final Image image) {
                subscription = null;
                imageView.setImageDrawable(null);
                percent.setText("loading !!!");
                viewAnimator.setDisplayedChild(0);
                Bitmap bm = lruCache.get(image.getUrl());
                if (bm != null) {

                    imageView.setImageBitmap(bm);
                    viewAnimator.setDisplayedChild(1);
                    return;
                }
                bm=ImageUtils.getBitmapFromDiskCache(image.getUrl());
                if (bm != null) {
                    final double wRatio = (double) bm.getWidth() / (double) imageView.getMeasuredWidth();
                    final int w = imageView.getMeasuredWidth()*5/3;
                    final int h = (int) (bm.getHeight()*5/3 / wRatio);
                    if (w > 0 && h > 0) {
                        bm = Bitmap.createScaledBitmap(bm, w, h, false);
                    }

                    imageView.setImageBitmap(bm);
                    viewAnimator.setDisplayedChild(1);
                    return;
                }
                subscription = ImageUtils.addBitmapToCatche(image.getUrl())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .takeUntil(stopEvent())
                        .doOnSubscribe(new Action0() {
                            @Override
                            public void call() {
                                percent.setText("0%");
                                viewAnimator.setDisplayedChild(0);
                            }
                        })
                        .subscribe(new Subscriber<String>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "onCompleted: ");
                                Bitmap bm = ImageUtils.getBitmapFromDiskCache(image.getUrl());
                                if (bm != null) {
                                    final double wRatio = (double) bm.getWidth() / (double) imageView.getMeasuredWidth();
                                    final int w = imageView.getMeasuredWidth()*5/3;
                                    final int h = (int) (bm.getHeight()*5/3 / wRatio);
                                    if (w > 0 && h > 0) {
                                        bm = Bitmap.createScaledBitmap(bm, w, h, false);
                                    }
                                    lruCache.put(image.getUrl(), bm);
                                    imageView.setImageBitmap(bm);
                                    viewAnimator.setDisplayedChild(1);
                                } else {

                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "onError: "+e.getMessage());
                                percent.setText("Error!");
                            }

                            @Override
                            public void onNext(String s) {
                                Log.d(TAG, "onNext: "+s);
                                percent.setText(s);
                            }
                        });
            }


        }


    }


}
