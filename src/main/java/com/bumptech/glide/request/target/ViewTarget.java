package com.bumptech.glide.request.target;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.util.Preconditions;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Deprecated
public abstract class ViewTarget<T extends View, Z> extends BaseTarget<Z> {
    private static final String TAG = "ViewTarget";
    private static boolean isTagUsedAtLeastOnce;
    @Nullable
    private static Integer tagId;
    @Nullable
    private OnAttachStateChangeListener attachStateListener;
    private boolean isAttachStateListenerAdded;
    private boolean isClearedByUs;
    private final SizeDeterminer sizeDeterminer;
    protected final T view;

    @VisibleForTesting
    static final class SizeDeterminer {
        private static final int PENDING_SIZE = 0;
        @Nullable
        @VisibleForTesting
        static Integer maxDisplayLength;
        private final List<SizeReadyCallback> cbs = new ArrayList();
        @Nullable
        private SizeDeterminerLayoutListener layoutListener;
        private final View view;
        boolean waitForLayout;

        private static final class SizeDeterminerLayoutListener implements OnPreDrawListener {
            private final WeakReference<SizeDeterminer> sizeDeterminerRef;

            SizeDeterminerLayoutListener(@NonNull SizeDeterminer sizeDeterminer) {
                this.sizeDeterminerRef = new WeakReference(sizeDeterminer);
            }

            public boolean onPreDraw() {
                if (Log.isLoggable(ViewTarget.TAG, 2)) {
                    String str = ViewTarget.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("OnGlobalLayoutListener called attachStateListener=");
                    stringBuilder.append(this);
                    Log.v(str, stringBuilder.toString());
                }
                SizeDeterminer sizeDeterminer = (SizeDeterminer) this.sizeDeterminerRef.get();
                if (sizeDeterminer != null) {
                    sizeDeterminer.checkCurrentDimens();
                }
                return true;
            }
        }

        private boolean isDimensionValid(int i) {
            return i > 0 || i == Integer.MIN_VALUE;
        }

        SizeDeterminer(@NonNull View view) {
            this.view = view;
        }

        private static int getMaxDisplayLength(@NonNull Context context) {
            if (maxDisplayLength == null) {
                Display defaultDisplay = ((WindowManager) Preconditions.checkNotNull((WindowManager) context.getSystemService("window"))).getDefaultDisplay();
                Point point = new Point();
                defaultDisplay.getSize(point);
                maxDisplayLength = Integer.valueOf(Math.max(point.x, point.y));
            }
            return maxDisplayLength.intValue();
        }

        private void notifyCbs(int i, int i2) {
            Iterator it = new ArrayList(this.cbs).iterator();
            while (it.hasNext()) {
                ((SizeReadyCallback) it.next()).onSizeReady(i, i2);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void checkCurrentDimens() {
            if (!this.cbs.isEmpty()) {
                int targetWidth = getTargetWidth();
                int targetHeight = getTargetHeight();
                if (isViewStateAndSizeValid(targetWidth, targetHeight)) {
                    notifyCbs(targetWidth, targetHeight);
                    clearCallbacksAndListener();
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void getSize(@NonNull SizeReadyCallback sizeReadyCallback) {
            int targetWidth = getTargetWidth();
            int targetHeight = getTargetHeight();
            if (isViewStateAndSizeValid(targetWidth, targetHeight)) {
                sizeReadyCallback.onSizeReady(targetWidth, targetHeight);
                return;
            }
            if (!this.cbs.contains(sizeReadyCallback)) {
                this.cbs.add(sizeReadyCallback);
            }
            if (this.layoutListener == null) {
                ViewTreeObserver viewTreeObserver = this.view.getViewTreeObserver();
                this.layoutListener = new SizeDeterminerLayoutListener(this);
                viewTreeObserver.addOnPreDrawListener(this.layoutListener);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void removeCallback(@NonNull SizeReadyCallback sizeReadyCallback) {
            this.cbs.remove(sizeReadyCallback);
        }

        /* Access modifiers changed, original: 0000 */
        public void clearCallbacksAndListener() {
            ViewTreeObserver viewTreeObserver = this.view.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.removeOnPreDrawListener(this.layoutListener);
            }
            this.layoutListener = null;
            this.cbs.clear();
        }

        private boolean isViewStateAndSizeValid(int i, int i2) {
            return isDimensionValid(i) && isDimensionValid(i2);
        }

        private int getTargetHeight() {
            int paddingTop = this.view.getPaddingTop() + this.view.getPaddingBottom();
            LayoutParams layoutParams = this.view.getLayoutParams();
            return getTargetDimen(this.view.getHeight(), layoutParams != null ? layoutParams.height : 0, paddingTop);
        }

        private int getTargetWidth() {
            int paddingLeft = this.view.getPaddingLeft() + this.view.getPaddingRight();
            LayoutParams layoutParams = this.view.getLayoutParams();
            return getTargetDimen(this.view.getWidth(), layoutParams != null ? layoutParams.width : 0, paddingLeft);
        }

        private int getTargetDimen(int i, int i2, int i3) {
            int i4 = i2 - i3;
            if (i4 > 0) {
                return i4;
            }
            if (this.waitForLayout && this.view.isLayoutRequested()) {
                return 0;
            }
            i -= i3;
            if (i > 0) {
                return i;
            }
            if (this.view.isLayoutRequested() || i2 != -2) {
                return 0;
            }
            if (Log.isLoggable(ViewTarget.TAG, 4)) {
                Log.i(ViewTarget.TAG, "Glide treats LayoutParams.WRAP_CONTENT as a request for an image the size of this device's screen dimensions. If you want to load the original image and are ok with the corresponding memory cost and OOMs (depending on the input size), use .override(Target.SIZE_ORIGINAL). Otherwise, use LayoutParams.MATCH_PARENT, set layout_width and layout_height to fixed dimension, or use .override() with fixed dimensions.");
            }
            return getMaxDisplayLength(this.view.getContext());
        }
    }

    public ViewTarget(@NonNull T t) {
        this.view = (View) Preconditions.checkNotNull(t);
        this.sizeDeterminer = new SizeDeterminer(t);
    }

    @Deprecated
    public ViewTarget(@NonNull T t, boolean z) {
        this(t);
        if (z) {
            waitForLayout();
        }
    }

    @NonNull
    public final ViewTarget<T, Z> clearOnDetach() {
        if (this.attachStateListener != null) {
            return this;
        }
        this.attachStateListener = new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View view) {
                ViewTarget.this.resumeMyRequest();
            }

            public void onViewDetachedFromWindow(View view) {
                ViewTarget.this.pauseMyRequest();
            }
        };
        maybeAddAttachStateListener();
        return this;
    }

    /* Access modifiers changed, original: 0000 */
    public void resumeMyRequest() {
        Request request = getRequest();
        if (request != null && request.isCleared()) {
            request.begin();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void pauseMyRequest() {
        Request request = getRequest();
        if (request != null) {
            this.isClearedByUs = true;
            request.clear();
            this.isClearedByUs = false;
        }
    }

    @NonNull
    public final ViewTarget<T, Z> waitForLayout() {
        this.sizeDeterminer.waitForLayout = true;
        return this;
    }

    @CallSuper
    public void onLoadStarted(@Nullable Drawable drawable) {
        super.onLoadStarted(drawable);
        maybeAddAttachStateListener();
    }

    private void maybeAddAttachStateListener() {
        if (this.attachStateListener != null && !this.isAttachStateListenerAdded) {
            this.view.addOnAttachStateChangeListener(this.attachStateListener);
            this.isAttachStateListenerAdded = true;
        }
    }

    private void maybeRemoveAttachStateListener() {
        if (this.attachStateListener != null && this.isAttachStateListenerAdded) {
            this.view.removeOnAttachStateChangeListener(this.attachStateListener);
            this.isAttachStateListenerAdded = false;
        }
    }

    @NonNull
    public T getView() {
        return this.view;
    }

    @CallSuper
    public void getSize(@NonNull SizeReadyCallback sizeReadyCallback) {
        this.sizeDeterminer.getSize(sizeReadyCallback);
    }

    @CallSuper
    public void removeCallback(@NonNull SizeReadyCallback sizeReadyCallback) {
        this.sizeDeterminer.removeCallback(sizeReadyCallback);
    }

    @CallSuper
    public void onLoadCleared(@Nullable Drawable drawable) {
        super.onLoadCleared(drawable);
        this.sizeDeterminer.clearCallbacksAndListener();
        if (!this.isClearedByUs) {
            maybeRemoveAttachStateListener();
        }
    }

    public void setRequest(@Nullable Request request) {
        setTag(request);
    }

    @Nullable
    public Request getRequest() {
        Object tag = getTag();
        if (tag == null) {
            return null;
        }
        if (tag instanceof Request) {
            return (Request) tag;
        }
        throw new IllegalArgumentException("You must not call setTag() on a view Glide is targeting");
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Target for: ");
        stringBuilder.append(this.view);
        return stringBuilder.toString();
    }

    private void setTag(@Nullable Object obj) {
        if (tagId == null) {
            isTagUsedAtLeastOnce = true;
            this.view.setTag(obj);
            return;
        }
        this.view.setTag(tagId.intValue(), obj);
    }

    @Nullable
    private Object getTag() {
        if (tagId == null) {
            return this.view.getTag();
        }
        return this.view.getTag(tagId.intValue());
    }

    public static void setTagId(int i) {
        if (tagId != null || isTagUsedAtLeastOnce) {
            throw new IllegalArgumentException("You cannot set the tag id more than once or change the tag id after the first request has been made");
        }
        tagId = Integer.valueOf(i);
    }
}
