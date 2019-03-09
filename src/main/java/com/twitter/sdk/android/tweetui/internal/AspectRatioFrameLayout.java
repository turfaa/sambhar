package com.twitter.sdk.android.tweetui.internal;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import com.twitter.sdk.android.tweetui.R;

public class AspectRatioFrameLayout extends FrameLayout {
    static final int ADJUST_DIMENSION_HEIGHT = 0;
    static final int ADJUST_DIMENSION_WIDTH = 1;
    private static final int DEFAULT_ADJUST_DIMENSION = 0;
    private static final float DEFAULT_ASPECT_RATIO = 1.0f;
    protected double aspectRatio;
    private int dimensionToAdjust;

    public AspectRatioFrameLayout(Context context) {
        this(context, null);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        initAttributes(i);
    }

    private void initAttributes(int i) {
        TypedArray obtainStyledAttributes = getContext().getTheme().obtainStyledAttributes(i, R.styleable.AspectRatioFrameLayout);
        try {
            this.aspectRatio = (double) obtainStyledAttributes.getFloat(R.styleable.AspectRatioFrameLayout_tw__frame_layout_aspect_ratio, DEFAULT_ASPECT_RATIO);
            this.dimensionToAdjust = obtainStyledAttributes.getInt(R.styleable.AspectRatioFrameLayout_tw__frame_layout_dimension_to_adjust, 0);
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    public void setAspectRatio(double d) {
        this.aspectRatio = d;
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int i, int i2) {
        int paddingLeft = getPaddingLeft() + getPaddingRight();
        int paddingBottom = getPaddingBottom() + getPaddingTop();
        double d;
        double d2;
        if (this.dimensionToAdjust == 0) {
            if (MeasureSpec.getMode(i) == 1073741824) {
                i = MeasureSpec.getSize(i) - paddingLeft;
            } else {
                super.onMeasure(i, i2);
                i = getMeasuredWidth() - paddingLeft;
            }
            d = (double) i;
            d2 = this.aspectRatio;
            Double.isNaN(d);
            i2 = (int) (d / d2);
        } else {
            if (MeasureSpec.getMode(i2) == 1073741824) {
                i = MeasureSpec.getSize(i2) - paddingBottom;
            } else {
                super.onMeasure(i, i2);
                i = getMeasuredHeight() - paddingBottom;
            }
            i2 = i;
            d = (double) i2;
            d2 = this.aspectRatio;
            Double.isNaN(d);
            i = (int) (d * d2);
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(i + paddingLeft, 1073741824), MeasureSpec.makeMeasureSpec(i2 + paddingBottom, 1073741824));
    }
}
