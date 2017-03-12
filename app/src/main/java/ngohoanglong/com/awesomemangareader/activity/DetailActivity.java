package ngohoanglong.com.awesomemangareader.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.ViewAnimator;

import ngohoanglong.com.awesomemangareader.R;
import ngohoanglong.com.awesomemangareader.customview.MyImageView;
import ngohoanglong.com.awesomemangareader.utils.ImageUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";
    static final String URL_IMAGE = "URL_IMAGE";

    MyImageView imageView;
    TextView percent;
    ViewAnimator viewAnimator;
    String urlImage;

//    rxjava
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
    protected void onStop() {
        stopEvent.onNext(STOP);
        super.onStop();
    }

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

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Bitmap bm = ImageUtils.getBitmapFromDiskCache(urlImage);
        if (bm != null) {
            Log.d(TAG, "ImageUtils.getBitmapFromDiskCache(urlImage) is not null");
            final double wRatio = (double) bm.getWidth() / (double) imageView.getMeasuredWidth();
            final int w = imageView.getMeasuredWidth()*5/3;
            final int h = (int) (bm.getHeight()*5/3 / wRatio);
            if (w > 0 && h > 0) {
                bm = Bitmap.createScaledBitmap(bm, w, h, false);
            }

            imageView.setImageBitmap(bm);
            viewAnimator.setDisplayedChild(1);
        } else {
            ImageUtils.addBitmapToCatche(urlImage)
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
                            Bitmap bm = ImageUtils.getBitmapFromDiskCache(urlImage);
                            if (bm != null) {
                                final double wRatio = (double) bm.getWidth() / (double) imageView.getMeasuredWidth();
                                final int w = imageView.getMeasuredWidth()*5/3;
                                final int h = (int) (bm.getHeight()*5/3 / wRatio);
                                if (w > 0 && h > 0) {
                                    bm = Bitmap.createScaledBitmap(bm, w, h, false);
                                }

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

    public static Intent getActivityIntent(Context context, String url) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(URL_IMAGE, url);
        return intent;

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
