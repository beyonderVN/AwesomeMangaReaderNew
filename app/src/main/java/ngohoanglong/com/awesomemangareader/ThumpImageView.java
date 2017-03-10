package ngohoanglong.com.awesomemangareader;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by yuvaraj on 3/4/16.
 */
public class ThumpImageView extends android.support.v7.widget.AppCompatImageView {
    private static final String TAG = "DynamicHeightImageView";
    private double whRatio = 0;

    public ThumpImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumpImageView(Context context) {
        super(context);
    }

    public void setRatio(double ratio) {
        whRatio = ratio;
        Log.d(TAG, "setRatio: "+whRatio);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

}
