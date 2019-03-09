package com.twitter.sdk.android.tweetui.internal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class OverlayImageView extends ImageView {
    Overlay overlay = new Overlay(null);

    protected static class Overlay {
        final Drawable drawable;

        Overlay(Drawable drawable) {
            this.drawable = drawable;
        }

        /* Access modifiers changed, original: protected */
        public void cleanupDrawable(ImageView imageView) {
            if (this.drawable != null) {
                this.drawable.setCallback(null);
                imageView.unscheduleDrawable(this.drawable);
            }
        }

        /* Access modifiers changed, original: protected */
        public void setDrawableBounds(int i, int i2) {
            if (this.drawable != null) {
                this.drawable.setBounds(0, 0, i, i2);
            }
        }

        /* Access modifiers changed, original: protected */
        public void setDrawableState(int[] iArr) {
            if (this.drawable != null && this.drawable.isStateful()) {
                this.drawable.setState(iArr);
            }
        }

        /* Access modifiers changed, original: protected */
        public void draw(Canvas canvas) {
            if (this.drawable != null) {
                this.drawable.draw(canvas);
            }
        }
    }

    public OverlayImageView(Context context) {
        super(context);
    }

    public OverlayImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.overlay.draw(canvas);
    }

    /* Access modifiers changed, original: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        this.overlay.setDrawableState(getDrawableState());
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        this.overlay.setDrawableBounds(getMeasuredWidth(), getMeasuredHeight());
    }

    /* Access modifiers changed, original: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.overlay.setDrawableBounds(i, i2);
    }

    public void invalidateDrawable(Drawable drawable) {
        if (drawable == this.overlay.drawable) {
            invalidate();
        } else {
            super.invalidateDrawable(drawable);
        }
    }

    public void setOverlayDrawable(Drawable drawable) {
        if (drawable != this.overlay.drawable) {
            this.overlay.cleanupDrawable(this);
            if (drawable != null) {
                drawable.setCallback(this);
            }
            this.overlay = new Overlay(drawable);
            this.overlay.setDrawableState(getDrawableState());
            requestLayout();
        }
    }
}
