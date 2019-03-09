package com.twitter.sdk.android.tweetui.internal;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

public class SwipeToDismissTouchListener implements OnTouchListener {
    private Callback callback;
    private final float closeThreshold;
    private float initialY;
    private boolean isMoving;
    private float lastX;
    private float lastY;
    private final float maxTranslate;
    private int pointerIndex;
    private int touchSlop;

    public interface Callback {
        void onDismiss();

        void onMove(float f);
    }

    public interface SwipeableViewProvider {
        boolean canBeSwiped();
    }

    public static SwipeToDismissTouchListener createFromView(View view, Callback callback) {
        return new SwipeToDismissTouchListener(callback, ViewConfiguration.get(view.getContext()).getScaledTouchSlop(), ((float) view.getContext().getResources().getDisplayMetrics().heightPixels) * 0.5f);
    }

    SwipeToDismissTouchListener(Callback callback, int i, float f) {
        this(callback, i, f, 0.2f * f);
    }

    SwipeToDismissTouchListener(Callback callback, int i, float f, float f2) {
        setCallback(callback);
        this.touchSlop = i;
        this.maxTranslate = f;
        this.closeThreshold = f2;
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouch(View view, MotionEvent motionEvent) {
        boolean handleTouchEvent;
        if (!(view instanceof SwipeableViewProvider) || ((SwipeableViewProvider) view).canBeSwiped() || isMoving()) {
            handleTouchEvent = handleTouchEvent(view, motionEvent);
        } else {
            handleTouchEvent = false;
        }
        if (handleTouchEvent || view.onTouchEvent(motionEvent)) {
            return true;
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean handleTouchEvent(View view, MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 5) {
            switch (actionMasked) {
                case 0:
                    this.lastX = motionEvent.getRawX();
                    float rawY = motionEvent.getRawY();
                    this.lastY = rawY;
                    this.initialY = rawY;
                    this.isMoving = false;
                    this.pointerIndex = motionEvent.getPointerId(motionEvent.getPointerCount() - 1);
                    break;
                case 1:
                case 3:
                    boolean z = (isValidPointer(motionEvent) && this.isMoving) ? settleOrCloseView(view) : false;
                    this.isMoving = false;
                    return z;
                case 2:
                    float rawX = motionEvent.getRawX();
                    float rawY2 = motionEvent.getRawY();
                    float f = rawY2 - this.initialY;
                    float f2 = rawX - this.lastX;
                    float f3 = rawY2 - this.lastY;
                    this.lastX = rawX;
                    this.lastY = rawY2;
                    if (isValidPointer(motionEvent) && (this.isMoving || (hasMovedEnoughInProperYDirection(f) && hasMovedMoreInYDirectionThanX(f2, f3)))) {
                        this.isMoving = true;
                        moveView(view, f3);
                        break;
                    }
            }
        }
        settleView(view);
        this.isMoving = false;
        this.pointerIndex = -1;
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasMovedEnoughInProperYDirection(float f) {
        return Math.abs(f) > ((float) this.touchSlop);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasMovedMoreInYDirectionThanX(float f, float f2) {
        return Math.abs(f2) > Math.abs(f);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isMoving() {
        return this.isMoving;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isValidPointer(MotionEvent motionEvent) {
        return this.pointerIndex >= 0 && motionEvent.getPointerCount() == 1;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean settleOrCloseView(View view) {
        float translationY = view.getTranslationY();
        if (translationY > this.closeThreshold || translationY < (-this.closeThreshold)) {
            if (this.callback != null) {
                this.callback.onDismiss();
            }
            return true;
        }
        settleView(view);
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public void settleView(View view) {
        if (view.getTranslationY() != 0.0f) {
            ObjectAnimator duration = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, new float[]{0.0f}).setDuration(100);
            duration.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    if (SwipeToDismissTouchListener.this.callback != null) {
                        SwipeToDismissTouchListener.this.callback.onMove(floatValue);
                    }
                }
            });
            duration.start();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void moveView(View view, float f) {
        float translationY = view.getTranslationY();
        double d = (double) f;
        double calculateTension = calculateTension(translationY);
        Double.isNaN(d);
        f = bound(translationY + ((float) (d * calculateTension)));
        view.setTranslationY(f);
        if (this.callback != null) {
            this.callback.onMove(f);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public double calculateTension(float f) {
        return 1.0d - (Math.pow((double) Math.abs(f), 2.0d) / Math.pow((double) (this.closeThreshold * 2.0f), 2.0d));
    }

    /* Access modifiers changed, original: 0000 */
    public float bound(float f) {
        if (f < (-this.maxTranslate)) {
            return -this.maxTranslate;
        }
        return f > this.maxTranslate ? this.maxTranslate : f;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
