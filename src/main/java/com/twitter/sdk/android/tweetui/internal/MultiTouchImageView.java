package com.twitter.sdk.android.tweetui.internal;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.twitter.sdk.android.tweetui.internal.SwipeToDismissTouchListener.SwipeableViewProvider;

public class MultiTouchImageView extends ImageView implements SwipeableViewProvider {
    private static final float DOUBLE_TAP_SCALE_FACTOR = 2.0f;
    private static final float MINIMUM_SCALE_FACTOR = 1.0f;
    private static final long SCALE_ANIMATION_DURATION = 300;
    boolean allowIntercept;
    final Matrix baseMatrix;
    final Matrix drawMatrix;
    final RectF drawRect;
    final GestureDetector gestureDetector;
    final float[] matrixValues;
    final ScaleGestureDetector scaleGestureDetector;
    final Matrix updateMatrix;
    final RectF viewRect;

    public MultiTouchImageView(Context context) {
        this(context, null);
    }

    public MultiTouchImageView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MultiTouchImageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.drawMatrix = new Matrix();
        this.baseMatrix = new Matrix();
        this.updateMatrix = new Matrix();
        this.viewRect = new RectF();
        this.drawRect = new RectF();
        this.matrixValues = new float[9];
        this.scaleGestureDetector = new ScaleGestureDetector(context, new SimpleOnScaleGestureListener() {
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                MultiTouchImageView.this.setScale(scaleGestureDetector.getScaleFactor(), scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
                MultiTouchImageView.this.setImageMatrix();
                return true;
            }

            public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
                if (MultiTouchImageView.this.getScale() < MultiTouchImageView.MINIMUM_SCALE_FACTOR) {
                    MultiTouchImageView.this.reset();
                    MultiTouchImageView.this.setImageMatrix();
                }
            }
        });
        this.gestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
                MultiTouchImageView.this.setTranslate(-f, -f2);
                MultiTouchImageView.this.setImageMatrix();
                if (MultiTouchImageView.this.allowIntercept && !MultiTouchImageView.this.scaleGestureDetector.isInProgress()) {
                    MultiTouchImageView.this.requestDisallowInterceptTouchEvent(false);
                }
                return true;
            }

            public boolean onDoubleTap(MotionEvent motionEvent) {
                if (MultiTouchImageView.this.getScale() > MultiTouchImageView.MINIMUM_SCALE_FACTOR) {
                    MultiTouchImageView.this.animateScale(MultiTouchImageView.this.getScale(), MultiTouchImageView.MINIMUM_SCALE_FACTOR, motionEvent.getX(), motionEvent.getY());
                } else {
                    MultiTouchImageView.this.animateScale(MultiTouchImageView.this.getScale(), MultiTouchImageView.DOUBLE_TAP_SCALE_FACTOR, motionEvent.getX(), motionEvent.getY());
                }
                return true;
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isInitializationComplete() {
        Drawable drawable = getDrawable();
        return drawable != null && drawable.getIntrinsicWidth() > 0;
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (isInitializationComplete()) {
            initializeViewRect();
            initializeBaseMatrix(getDrawable());
            setImageMatrix();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void initializeViewRect() {
        this.viewRect.set((float) getPaddingLeft(), (float) getPaddingTop(), (float) (getWidth() - getPaddingRight()), (float) (getHeight() - getPaddingBottom()));
    }

    /* Access modifiers changed, original: 0000 */
    public void initializeBaseMatrix(Drawable drawable) {
        RectF rectF = new RectF(0.0f, 0.0f, (float) drawable.getIntrinsicWidth(), (float) drawable.getIntrinsicHeight());
        this.baseMatrix.reset();
        this.baseMatrix.setRectToRect(rectF, this.viewRect, ScaleToFit.CENTER);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!isInitializationComplete()) {
            return false;
        }
        boolean z = true;
        requestDisallowInterceptTouchEvent(true);
        Object obj = (this.gestureDetector.onTouchEvent(motionEvent) || this.scaleGestureDetector.onTouchEvent(motionEvent)) ? 1 : null;
        if (obj == null && !super.onTouchEvent(motionEvent)) {
            z = false;
        }
        return z;
    }

    /* Access modifiers changed, original: 0000 */
    public void requestDisallowInterceptTouchEvent(boolean z) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(z);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setScale(float f, float f2, float f3) {
        this.updateMatrix.postScale(f, f, f2, f3);
    }

    /* Access modifiers changed, original: 0000 */
    public float getScale() {
        this.updateMatrix.getValues(this.matrixValues);
        return this.matrixValues[0];
    }

    /* Access modifiers changed, original: 0000 */
    public void setTranslate(float f, float f2) {
        this.updateMatrix.postTranslate(f, f2);
    }

    /* Access modifiers changed, original: 0000 */
    public void reset() {
        this.updateMatrix.reset();
    }

    /* Access modifiers changed, original: 0000 */
    public void updateMatrixBounds() {
        RectF drawRect = getDrawRect(getDrawMatrix());
        float f = 0.0f;
        float height = drawRect.height() <= this.viewRect.height() ? ((this.viewRect.height() - drawRect.height()) / DOUBLE_TAP_SCALE_FACTOR) - drawRect.top : drawRect.top > 0.0f ? -drawRect.top : drawRect.bottom < this.viewRect.height() ? this.viewRect.height() - drawRect.bottom : 0.0f;
        if (drawRect.width() <= this.viewRect.width()) {
            this.allowIntercept = true;
            f = ((this.viewRect.width() - drawRect.width()) / DOUBLE_TAP_SCALE_FACTOR) - drawRect.left;
        } else if (drawRect.left > 0.0f) {
            this.allowIntercept = true;
            f = -drawRect.left;
        } else if (drawRect.right < this.viewRect.width()) {
            this.allowIntercept = true;
            f = this.viewRect.width() - drawRect.right;
        } else {
            this.allowIntercept = false;
        }
        setTranslate(f, height);
    }

    /* Access modifiers changed, original: 0000 */
    public RectF getDrawRect(Matrix matrix) {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            this.drawRect.set(0.0f, 0.0f, (float) drawable.getIntrinsicWidth(), (float) drawable.getIntrinsicHeight());
            matrix.mapRect(this.drawRect);
        }
        return this.drawRect;
    }

    /* Access modifiers changed, original: 0000 */
    public Matrix getDrawMatrix() {
        this.drawMatrix.set(this.baseMatrix);
        this.drawMatrix.postConcat(this.updateMatrix);
        return this.drawMatrix;
    }

    /* Access modifiers changed, original: 0000 */
    public void setImageMatrix() {
        updateMatrixBounds();
        setScaleType(ScaleType.MATRIX);
        setImageMatrix(getDrawMatrix());
    }

    /* Access modifiers changed, original: 0000 */
    public void animateScale(float f, float f2, final float f3, final float f4) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{f, f2});
        ofFloat.setDuration(SCALE_ANIMATION_DURATION);
        ofFloat.setInterpolator(new AccelerateDecelerateInterpolator());
        ofFloat.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                MultiTouchImageView.this.setScale(((Float) valueAnimator.getAnimatedValue()).floatValue() / MultiTouchImageView.this.getScale(), f3, f4);
                MultiTouchImageView.this.setImageMatrix();
            }
        });
        ofFloat.start();
    }

    public boolean canBeSwiped() {
        return getScale() == MINIMUM_SCALE_FACTOR;
    }
}
