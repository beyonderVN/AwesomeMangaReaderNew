package ngohoanglong.com.awesomemangareader.activity.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
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
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;


public class ChapterFragment extends Fragment {
    private static final String TAG = "UsingRxjavaMangaPageFra";
    private static final String ARG_PARAM1 = "param1";


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

    public static ChapterFragment newInstance(int chapterPosition) {
        ChapterFragment fragment = new ChapterFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, chapterPosition);
        fragment.setArguments(args);
        return fragment;
    }

//    data index
    int chapterPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chapterPosition = getArguments() != null ? getArguments().getInt(ARG_PARAM1) : null;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_using_rxjava_manga_page, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.rvImages);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new ChapterAdapter(chapterPosition));

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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


    class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder> {

        public ChapterAdapter(int chapterPosition) {
            images = AppState.chapters.get(chapterPosition).getImageList();
        }
        List<Image> images;
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((Activity) holder.itemView.getContext(), (View) holder.imageView, AppState.chapters.get(position).getTitle());
                    startActivity(DetailActivity.getActivityIntent(holder.imageView.getContext(), images.get(position).getUrl()), options.toBundle());
                }
            });
            holder.onBindVieHolder(images.get(position));
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
            TextView tvStatus;
            ViewAnimator viewAnimator;
            Subscription subscription;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.ivImage);
                tvStatus = (TextView) itemView.findViewById(R.id.tvStatus);
                viewAnimator = (ViewAnimator) itemView.findViewById(R.id.avPageStage);
            }

            void onBindVieHolder(final Image image) {
                tvStatus.setText(image.getUrl());
            }


        }


    }


}
