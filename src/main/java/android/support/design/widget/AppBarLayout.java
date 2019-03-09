package android.support.design.widget;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.Parcelable.Creator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.VisibleForTesting;
import android.support.design.R;
import android.support.design.internal.ThemeEnforcement;
import android.support.design.widget.CoordinatorLayout.DefaultBehavior;
import android.support.v4.math.MathUtils;
import android.support.v4.util.ObjectsCompat;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@DefaultBehavior(Behavior.class)
public class AppBarLayout extends LinearLayout {
    private static final int INVALID_SCROLL_RANGE = -1;
    static final int PENDING_ACTION_ANIMATE_ENABLED = 4;
    static final int PENDING_ACTION_COLLAPSED = 2;
    static final int PENDING_ACTION_EXPANDED = 1;
    static final int PENDING_ACTION_FORCE = 8;
    static final int PENDING_ACTION_NONE = 0;
    private int downPreScrollRange;
    private int downScrollRange;
    private boolean haveChildWithInterpolator;
    private WindowInsetsCompat lastInsets;
    private boolean liftOnScroll;
    private boolean liftable;
    private boolean liftableOverride;
    private boolean lifted;
    private List<BaseOnOffsetChangedListener> listeners;
    private int pendingAction;
    private int[] tmpStatesArray;
    private int totalScrollRange;

    public interface BaseOnOffsetChangedListener<T extends AppBarLayout> {
        void onOffsetChanged(T t, int i);
    }

    public static class LayoutParams extends android.widget.LinearLayout.LayoutParams {
        static final int COLLAPSIBLE_FLAGS = 10;
        static final int FLAG_QUICK_RETURN = 5;
        static final int FLAG_SNAP = 17;
        public static final int SCROLL_FLAG_ENTER_ALWAYS = 4;
        public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 8;
        public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 2;
        public static final int SCROLL_FLAG_SCROLL = 1;
        public static final int SCROLL_FLAG_SNAP = 16;
        public static final int SCROLL_FLAG_SNAP_MARGINS = 32;
        int scrollFlags = 1;
        Interpolator scrollInterpolator;

        @RestrictTo({Scope.LIBRARY_GROUP})
        @Retention(RetentionPolicy.SOURCE)
        public @interface ScrollFlags {
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.AppBarLayout_Layout);
            this.scrollFlags = obtainStyledAttributes.getInt(R.styleable.AppBarLayout_Layout_layout_scrollFlags, 0);
            if (obtainStyledAttributes.hasValue(R.styleable.AppBarLayout_Layout_layout_scrollInterpolator)) {
                this.scrollInterpolator = AnimationUtils.loadInterpolator(context, obtainStyledAttributes.getResourceId(R.styleable.AppBarLayout_Layout_layout_scrollInterpolator, 0));
            }
            obtainStyledAttributes.recycle();
        }

        public LayoutParams(int i, int i2) {
            super(i, i2);
        }

        public LayoutParams(int i, int i2, float f) {
            super(i, i2, f);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public LayoutParams(MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
        }

        @RequiresApi(19)
        public LayoutParams(android.widget.LinearLayout.LayoutParams layoutParams) {
            super(layoutParams);
        }

        @RequiresApi(19)
        public LayoutParams(LayoutParams layoutParams) {
            super(layoutParams);
            this.scrollFlags = layoutParams.scrollFlags;
            this.scrollInterpolator = layoutParams.scrollInterpolator;
        }

        public void setScrollFlags(int i) {
            this.scrollFlags = i;
        }

        public int getScrollFlags() {
            return this.scrollFlags;
        }

        public void setScrollInterpolator(Interpolator interpolator) {
            this.scrollInterpolator = interpolator;
        }

        public Interpolator getScrollInterpolator() {
            return this.scrollInterpolator;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean isCollapsible() {
            return (this.scrollFlags & 1) == 1 && (this.scrollFlags & 10) != 0;
        }
    }

    public interface OnOffsetChangedListener extends BaseOnOffsetChangedListener<AppBarLayout> {
        void onOffsetChanged(AppBarLayout appBarLayout, int i);
    }

    protected static class BaseBehavior<T extends AppBarLayout> extends HeaderBehavior<T> {
        private static final int INVALID_POSITION = -1;
        private static final int MAX_OFFSET_ANIMATION_DURATION = 600;
        private WeakReference<View> lastNestedScrollingChildRef;
        private int lastStartedType;
        private ValueAnimator offsetAnimator;
        private int offsetDelta;
        private int offsetToChildIndexOnLayout = -1;
        private boolean offsetToChildIndexOnLayoutIsMinHeight;
        private float offsetToChildIndexOnLayoutPerc;
        private BaseDragCallback onDragCallback;

        public static abstract class BaseDragCallback<T extends AppBarLayout> {
            public abstract boolean canDrag(@NonNull T t);
        }

        protected static class SavedState extends AbsSavedState {
            public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
                public SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
                    return new SavedState(parcel, classLoader);
                }

                public SavedState createFromParcel(Parcel parcel) {
                    return new SavedState(parcel, null);
                }

                public SavedState[] newArray(int i) {
                    return new SavedState[i];
                }
            };
            boolean firstVisibleChildAtMinimumHeight;
            int firstVisibleChildIndex;
            float firstVisibleChildPercentageShown;

            public SavedState(Parcel parcel, ClassLoader classLoader) {
                super(parcel, classLoader);
                this.firstVisibleChildIndex = parcel.readInt();
                this.firstVisibleChildPercentageShown = parcel.readFloat();
                this.firstVisibleChildAtMinimumHeight = parcel.readByte() != (byte) 0;
            }

            public SavedState(Parcelable parcelable) {
                super(parcelable);
            }

            public void writeToParcel(Parcel parcel, int i) {
                super.writeToParcel(parcel, i);
                parcel.writeInt(this.firstVisibleChildIndex);
                parcel.writeFloat(this.firstVisibleChildPercentageShown);
                parcel.writeByte((byte) this.firstVisibleChildAtMinimumHeight);
            }
        }

        private static boolean checkFlag(int i, int i2) {
            return (i & i2) == i2;
        }

        public BaseBehavior(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, T t, View view, View view2, int i, int i2) {
            boolean z = (i & 2) != 0 && (t.isLiftOnScroll() || canScrollChildren(coordinatorLayout, t, view));
            if (z && this.offsetAnimator != null) {
                this.offsetAnimator.cancel();
            }
            this.lastNestedScrollingChildRef = null;
            this.lastStartedType = i2;
            return z;
        }

        private boolean canScrollChildren(CoordinatorLayout coordinatorLayout, T t, View view) {
            return t.hasScrollableChildren() && coordinatorLayout.getHeight() - view.getHeight() <= t.getHeight();
        }

        public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, T t, View view, int i, int i2, int[] iArr, int i3) {
            if (i2 != 0) {
                int i4;
                int downNestedPreScrollRange;
                if (i2 < 0) {
                    i = -t.getTotalScrollRange();
                    i4 = i;
                    downNestedPreScrollRange = t.getDownNestedPreScrollRange() + i;
                } else {
                    i4 = -t.getUpNestedPreScrollRange();
                    downNestedPreScrollRange = 0;
                }
                if (i4 != downNestedPreScrollRange) {
                    iArr[1] = scroll(coordinatorLayout, t, i2, i4, downNestedPreScrollRange);
                    stopNestedScrollIfNeeded(i2, t, view, i3);
                }
            }
        }

        public void onNestedScroll(CoordinatorLayout coordinatorLayout, T t, View view, int i, int i2, int i3, int i4, int i5) {
            if (i4 < 0) {
                scroll(coordinatorLayout, t, i4, -t.getDownNestedScrollRange(), 0);
                stopNestedScrollIfNeeded(i4, t, view, i5);
            }
            if (t.isLiftOnScroll()) {
                t.setLiftedState(view.getScrollY() > 0);
            }
        }

        private void stopNestedScrollIfNeeded(int i, T t, View view, int i2) {
            if (i2 == 1) {
                i2 = getTopBottomOffsetForScrollingSibling();
                if ((i < 0 && i2 == 0) || (i > 0 && i2 == (-t.getDownNestedScrollRange()))) {
                    ViewCompat.stopNestedScroll(view, 1);
                }
            }
        }

        public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, T t, View view, int i) {
            if (this.lastStartedType == 0 || i == 1) {
                snapToChildIfNeeded(coordinatorLayout, t);
            }
            this.lastNestedScrollingChildRef = new WeakReference(view);
        }

        public void setDragCallback(@Nullable BaseDragCallback baseDragCallback) {
            this.onDragCallback = baseDragCallback;
        }

        private void animateOffsetTo(CoordinatorLayout coordinatorLayout, T t, int i, float f) {
            int round;
            int abs = Math.abs(getTopBottomOffsetForScrollingSibling() - i);
            f = Math.abs(f);
            if (f > 0.0f) {
                round = Math.round((((float) abs) / f) * 1000.0f) * 3;
            } else {
                round = (int) (((((float) abs) / ((float) t.getHeight())) + 1.0f) * 150.0f);
            }
            animateOffsetWithDuration(coordinatorLayout, t, i, round);
        }

        private void animateOffsetWithDuration(final CoordinatorLayout coordinatorLayout, final T t, int i, int i2) {
            if (getTopBottomOffsetForScrollingSibling() == i) {
                if (this.offsetAnimator != null && this.offsetAnimator.isRunning()) {
                    this.offsetAnimator.cancel();
                }
                return;
            }
            if (this.offsetAnimator == null) {
                this.offsetAnimator = new ValueAnimator();
                this.offsetAnimator.setInterpolator(android.support.design.animation.AnimationUtils.DECELERATE_INTERPOLATOR);
                this.offsetAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        BaseBehavior.this.setHeaderTopBottomOffset(coordinatorLayout, t, ((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
            } else {
                this.offsetAnimator.cancel();
            }
            this.offsetAnimator.setDuration((long) Math.min(i2, 600));
            this.offsetAnimator.setIntValues(new int[]{r0, i});
            this.offsetAnimator.start();
        }

        private int getChildIndexOnOffset(T t, int i) {
            int childCount = t.getChildCount();
            for (int i2 = 0; i2 < childCount; i2++) {
                View childAt = t.getChildAt(i2);
                int top = childAt.getTop();
                int bottom = childAt.getBottom();
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (checkFlag(layoutParams.getScrollFlags(), 32)) {
                    top -= layoutParams.topMargin;
                    bottom += layoutParams.bottomMargin;
                }
                int i3 = -i;
                if (top <= i3 && bottom >= i3) {
                    return i2;
                }
            }
            return -1;
        }

        private void snapToChildIfNeeded(CoordinatorLayout coordinatorLayout, T t) {
            int topBottomOffsetForScrollingSibling = getTopBottomOffsetForScrollingSibling();
            int childIndexOnOffset = getChildIndexOnOffset(t, topBottomOffsetForScrollingSibling);
            if (childIndexOnOffset >= 0) {
                View childAt = t.getChildAt(childIndexOnOffset);
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                int scrollFlags = layoutParams.getScrollFlags();
                if ((scrollFlags & 17) == 17) {
                    int i = -childAt.getTop();
                    int i2 = -childAt.getBottom();
                    if (childIndexOnOffset == t.getChildCount() - 1) {
                        i2 += t.getTopInset();
                    }
                    if (checkFlag(scrollFlags, 2)) {
                        i2 += ViewCompat.getMinimumHeight(childAt);
                    } else if (checkFlag(scrollFlags, 5)) {
                        int minimumHeight = ViewCompat.getMinimumHeight(childAt) + i2;
                        if (topBottomOffsetForScrollingSibling < minimumHeight) {
                            i = minimumHeight;
                        } else {
                            i2 = minimumHeight;
                        }
                    }
                    if (checkFlag(scrollFlags, 32)) {
                        i += layoutParams.topMargin;
                        i2 -= layoutParams.bottomMargin;
                    }
                    if (topBottomOffsetForScrollingSibling < (i2 + i) / 2) {
                        i = i2;
                    }
                    animateOffsetTo(coordinatorLayout, t, MathUtils.clamp(i, -t.getTotalScrollRange(), 0), 0.0f);
                }
            }
        }

        public boolean onMeasureChild(CoordinatorLayout coordinatorLayout, T t, int i, int i2, int i3, int i4) {
            if (((android.support.design.widget.CoordinatorLayout.LayoutParams) t.getLayoutParams()).height != -2) {
                return super.onMeasureChild(coordinatorLayout, t, i, i2, i3, i4);
            }
            coordinatorLayout.onMeasureChild(t, i, i2, MeasureSpec.makeMeasureSpec(0, 0), i4);
            return true;
        }

        public boolean onLayoutChild(CoordinatorLayout coordinatorLayout, T t, int i) {
            boolean onLayoutChild = super.onLayoutChild(coordinatorLayout, t, i);
            int pendingAction = t.getPendingAction();
            if (this.offsetToChildIndexOnLayout >= 0 && (pendingAction & 8) == 0) {
                View childAt = t.getChildAt(this.offsetToChildIndexOnLayout);
                int i2 = -childAt.getBottom();
                if (this.offsetToChildIndexOnLayoutIsMinHeight) {
                    i2 += ViewCompat.getMinimumHeight(childAt) + t.getTopInset();
                } else {
                    i2 += Math.round(((float) childAt.getHeight()) * this.offsetToChildIndexOnLayoutPerc);
                }
                setHeaderTopBottomOffset(coordinatorLayout, t, i2);
            } else if (pendingAction != 0) {
                Object obj = (pendingAction & 4) != 0 ? 1 : null;
                if ((pendingAction & 2) != 0) {
                    pendingAction = -t.getUpNestedPreScrollRange();
                    if (obj != null) {
                        animateOffsetTo(coordinatorLayout, t, pendingAction, 0.0f);
                    } else {
                        setHeaderTopBottomOffset(coordinatorLayout, t, pendingAction);
                    }
                } else if ((pendingAction & 1) != 0) {
                    if (obj != null) {
                        animateOffsetTo(coordinatorLayout, t, 0, 0.0f);
                    } else {
                        setHeaderTopBottomOffset(coordinatorLayout, t, 0);
                    }
                }
            }
            t.resetPendingAction();
            this.offsetToChildIndexOnLayout = -1;
            setTopAndBottomOffset(MathUtils.clamp(getTopAndBottomOffset(), -t.getTotalScrollRange(), 0));
            updateAppBarLayoutDrawableState(coordinatorLayout, t, getTopAndBottomOffset(), 0, true);
            t.dispatchOffsetUpdates(getTopAndBottomOffset());
            return onLayoutChild;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean canDragView(T t) {
            if (this.onDragCallback != null) {
                return this.onDragCallback.canDrag(t);
            }
            boolean z = true;
            if (this.lastNestedScrollingChildRef == null) {
                return true;
            }
            View view = (View) this.lastNestedScrollingChildRef.get();
            if (view == null || !view.isShown() || view.canScrollVertically(-1)) {
                z = false;
            }
            return z;
        }

        /* Access modifiers changed, original: 0000 */
        public void onFlingFinished(CoordinatorLayout coordinatorLayout, T t) {
            snapToChildIfNeeded(coordinatorLayout, t);
        }

        /* Access modifiers changed, original: 0000 */
        public int getMaxDragOffset(T t) {
            return -t.getDownNestedScrollRange();
        }

        /* Access modifiers changed, original: 0000 */
        public int getScrollRangeForDragFling(T t) {
            return t.getTotalScrollRange();
        }

        /* Access modifiers changed, original: 0000 */
        public int setHeaderTopBottomOffset(CoordinatorLayout coordinatorLayout, T t, int i, int i2, int i3) {
            int topBottomOffsetForScrollingSibling = getTopBottomOffsetForScrollingSibling();
            int i4 = 0;
            if (i2 == 0 || topBottomOffsetForScrollingSibling < i2 || topBottomOffsetForScrollingSibling > i3) {
                this.offsetDelta = 0;
            } else {
                int clamp = MathUtils.clamp(i, i2, i3);
                if (topBottomOffsetForScrollingSibling != clamp) {
                    i = t.hasChildWithInterpolator() ? interpolateOffset(t, clamp) : clamp;
                    boolean topAndBottomOffset = setTopAndBottomOffset(i);
                    i4 = topBottomOffsetForScrollingSibling - clamp;
                    this.offsetDelta = clamp - i;
                    if (!topAndBottomOffset && t.hasChildWithInterpolator()) {
                        coordinatorLayout.dispatchDependentViewsChanged(t);
                    }
                    t.dispatchOffsetUpdates(getTopAndBottomOffset());
                    updateAppBarLayoutDrawableState(coordinatorLayout, t, clamp, clamp < topBottomOffsetForScrollingSibling ? -1 : 1, false);
                }
            }
            return i4;
        }

        /* Access modifiers changed, original: 0000 */
        @VisibleForTesting
        public boolean isOffsetAnimatorRunning() {
            return this.offsetAnimator != null && this.offsetAnimator.isRunning();
        }

        private int interpolateOffset(T t, int i) {
            int abs = Math.abs(i);
            int childCount = t.getChildCount();
            int i2 = 0;
            int i3 = 0;
            while (i3 < childCount) {
                View childAt = t.getChildAt(i3);
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                Interpolator scrollInterpolator = layoutParams.getScrollInterpolator();
                if (abs < childAt.getTop() || abs > childAt.getBottom()) {
                    i3++;
                } else {
                    if (scrollInterpolator != null) {
                        childCount = layoutParams.getScrollFlags();
                        if ((childCount & 1) != 0) {
                            i2 = 0 + ((childAt.getHeight() + layoutParams.topMargin) + layoutParams.bottomMargin);
                            if ((childCount & 2) != 0) {
                                i2 -= ViewCompat.getMinimumHeight(childAt);
                            }
                        }
                        if (ViewCompat.getFitsSystemWindows(childAt)) {
                            i2 -= t.getTopInset();
                        }
                        if (i2 > 0) {
                            float f = (float) i2;
                            return Integer.signum(i) * (childAt.getTop() + Math.round(f * scrollInterpolator.getInterpolation(((float) (abs - childAt.getTop())) / f)));
                        }
                    }
                    return i;
                }
            }
            return i;
        }

        /* JADX WARNING: Removed duplicated region for block: B:18:0x0048  */
        /* JADX WARNING: Removed duplicated region for block: B:33:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x0061  */
        private void updateAppBarLayoutDrawableState(android.support.design.widget.CoordinatorLayout r6, T r7, int r8, int r9, boolean r10) {
            /*
            r5 = this;
            r0 = getAppBarChildOnOffset(r7, r8);
            if (r0 == 0) goto L_0x006e;
        L_0x0006:
            r1 = r0.getLayoutParams();
            r1 = (android.support.design.widget.AppBarLayout.LayoutParams) r1;
            r1 = r1.getScrollFlags();
            r2 = r1 & 1;
            r3 = 1;
            r4 = 0;
            if (r2 == 0) goto L_0x0041;
        L_0x0016:
            r2 = android.support.v4.view.ViewCompat.getMinimumHeight(r0);
            if (r9 <= 0) goto L_0x002f;
        L_0x001c:
            r9 = r1 & 12;
            if (r9 == 0) goto L_0x002f;
        L_0x0020:
            r8 = -r8;
            r9 = r0.getBottom();
            r9 = r9 - r2;
            r0 = r7.getTopInset();
            r9 = r9 - r0;
            if (r8 < r9) goto L_0x0041;
        L_0x002d:
            r8 = 1;
            goto L_0x0042;
        L_0x002f:
            r9 = r1 & 2;
            if (r9 == 0) goto L_0x0041;
        L_0x0033:
            r8 = -r8;
            r9 = r0.getBottom();
            r9 = r9 - r2;
            r0 = r7.getTopInset();
            r9 = r9 - r0;
            if (r8 < r9) goto L_0x0041;
        L_0x0040:
            goto L_0x002d;
        L_0x0041:
            r8 = 0;
        L_0x0042:
            r9 = r7.isLiftOnScroll();
            if (r9 == 0) goto L_0x0057;
        L_0x0048:
            r9 = r5.findFirstScrollingChild(r6);
            if (r9 == 0) goto L_0x0057;
        L_0x004e:
            r8 = r9.getScrollY();
            if (r8 <= 0) goto L_0x0056;
        L_0x0054:
            r8 = 1;
            goto L_0x0057;
        L_0x0056:
            r8 = 0;
        L_0x0057:
            r8 = r7.setLiftedState(r8);
            r9 = android.os.Build.VERSION.SDK_INT;
            r0 = 11;
            if (r9 < r0) goto L_0x006e;
        L_0x0061:
            if (r10 != 0) goto L_0x006b;
        L_0x0063:
            if (r8 == 0) goto L_0x006e;
        L_0x0065:
            r6 = r5.shouldJumpElevationState(r6, r7);
            if (r6 == 0) goto L_0x006e;
        L_0x006b:
            r7.jumpDrawablesToCurrentState();
        L_0x006e:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.design.widget.AppBarLayout$BaseBehavior.updateAppBarLayoutDrawableState(android.support.design.widget.CoordinatorLayout, android.support.design.widget.AppBarLayout, int, int, boolean):void");
        }

        private boolean shouldJumpElevationState(CoordinatorLayout coordinatorLayout, T t) {
            List dependents = coordinatorLayout.getDependents(t);
            int size = dependents.size();
            boolean z = false;
            for (int i = 0; i < size; i++) {
                android.support.design.widget.CoordinatorLayout.Behavior behavior = ((android.support.design.widget.CoordinatorLayout.LayoutParams) ((View) dependents.get(i)).getLayoutParams()).getBehavior();
                if (behavior instanceof ScrollingViewBehavior) {
                    if (((ScrollingViewBehavior) behavior).getOverlayTop() != 0) {
                        z = true;
                    }
                    return z;
                }
            }
            return false;
        }

        private static View getAppBarChildOnOffset(AppBarLayout appBarLayout, int i) {
            i = Math.abs(i);
            int childCount = appBarLayout.getChildCount();
            for (int i2 = 0; i2 < childCount; i2++) {
                View childAt = appBarLayout.getChildAt(i2);
                if (i >= childAt.getTop() && i <= childAt.getBottom()) {
                    return childAt;
                }
            }
            return null;
        }

        @Nullable
        private View findFirstScrollingChild(CoordinatorLayout coordinatorLayout) {
            int childCount = coordinatorLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = coordinatorLayout.getChildAt(i);
                if (childAt instanceof NestedScrollingChild) {
                    return childAt;
                }
            }
            return null;
        }

        /* Access modifiers changed, original: 0000 */
        public int getTopBottomOffsetForScrollingSibling() {
            return getTopAndBottomOffset() + this.offsetDelta;
        }

        public Parcelable onSaveInstanceState(CoordinatorLayout coordinatorLayout, T t) {
            Parcelable onSaveInstanceState = super.onSaveInstanceState(coordinatorLayout, t);
            int topAndBottomOffset = getTopAndBottomOffset();
            int childCount = t.getChildCount();
            boolean z = false;
            int i = 0;
            while (i < childCount) {
                View childAt = t.getChildAt(i);
                int bottom = childAt.getBottom() + topAndBottomOffset;
                if (childAt.getTop() + topAndBottomOffset > 0 || bottom < 0) {
                    i++;
                } else {
                    SavedState savedState = new SavedState(onSaveInstanceState);
                    savedState.firstVisibleChildIndex = i;
                    if (bottom == ViewCompat.getMinimumHeight(childAt) + t.getTopInset()) {
                        z = true;
                    }
                    savedState.firstVisibleChildAtMinimumHeight = z;
                    savedState.firstVisibleChildPercentageShown = ((float) bottom) / ((float) childAt.getHeight());
                    return savedState;
                }
            }
            return onSaveInstanceState;
        }

        public void onRestoreInstanceState(CoordinatorLayout coordinatorLayout, T t, Parcelable parcelable) {
            if (parcelable instanceof SavedState) {
                SavedState savedState = (SavedState) parcelable;
                super.onRestoreInstanceState(coordinatorLayout, t, savedState.getSuperState());
                this.offsetToChildIndexOnLayout = savedState.firstVisibleChildIndex;
                this.offsetToChildIndexOnLayoutPerc = savedState.firstVisibleChildPercentageShown;
                this.offsetToChildIndexOnLayoutIsMinHeight = savedState.firstVisibleChildAtMinimumHeight;
                return;
            }
            super.onRestoreInstanceState(coordinatorLayout, t, parcelable);
            this.offsetToChildIndexOnLayout = -1;
        }
    }

    public static class ScrollingViewBehavior extends HeaderScrollingViewBehavior {
        public /* bridge */ /* synthetic */ int getLeftAndRightOffset() {
            return super.getLeftAndRightOffset();
        }

        public /* bridge */ /* synthetic */ int getTopAndBottomOffset() {
            return super.getTopAndBottomOffset();
        }

        public /* bridge */ /* synthetic */ boolean onLayoutChild(CoordinatorLayout coordinatorLayout, View view, int i) {
            return super.onLayoutChild(coordinatorLayout, view, i);
        }

        public /* bridge */ /* synthetic */ boolean onMeasureChild(CoordinatorLayout coordinatorLayout, View view, int i, int i2, int i3, int i4) {
            return super.onMeasureChild(coordinatorLayout, view, i, i2, i3, i4);
        }

        public /* bridge */ /* synthetic */ boolean setLeftAndRightOffset(int i) {
            return super.setLeftAndRightOffset(i);
        }

        public /* bridge */ /* synthetic */ boolean setTopAndBottomOffset(int i) {
            return super.setTopAndBottomOffset(i);
        }

        public ScrollingViewBehavior(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ScrollingViewBehavior_Layout);
            setOverlayTop(obtainStyledAttributes.getDimensionPixelSize(R.styleable.ScrollingViewBehavior_Layout_behavior_overlapTop, 0));
            obtainStyledAttributes.recycle();
        }

        public boolean layoutDependsOn(CoordinatorLayout coordinatorLayout, View view, View view2) {
            return view2 instanceof AppBarLayout;
        }

        public boolean onDependentViewChanged(CoordinatorLayout coordinatorLayout, View view, View view2) {
            offsetChildAsNeeded(view, view2);
            updateLiftedStateIfNeeded(view, view2);
            return false;
        }

        public boolean onRequestChildRectangleOnScreen(CoordinatorLayout coordinatorLayout, View view, Rect rect, boolean z) {
            AppBarLayout findFirstDependency = findFirstDependency(coordinatorLayout.getDependencies(view));
            if (findFirstDependency != null) {
                rect.offset(view.getLeft(), view.getTop());
                Rect rect2 = this.tempRect1;
                rect2.set(0, 0, coordinatorLayout.getWidth(), coordinatorLayout.getHeight());
                if (!rect2.contains(rect)) {
                    findFirstDependency.setExpanded(false, z ^ 1);
                    return true;
                }
            }
            return false;
        }

        private void offsetChildAsNeeded(View view, View view2) {
            android.support.design.widget.CoordinatorLayout.Behavior behavior = ((android.support.design.widget.CoordinatorLayout.LayoutParams) view2.getLayoutParams()).getBehavior();
            if (behavior instanceof BaseBehavior) {
                ViewCompat.offsetTopAndBottom(view, (((view2.getBottom() - view.getTop()) + ((BaseBehavior) behavior).offsetDelta) + getVerticalLayoutGap()) - getOverlapPixelsForOffset(view2));
            }
        }

        /* Access modifiers changed, original: 0000 */
        public float getOverlapRatioForOffset(View view) {
            if (view instanceof AppBarLayout) {
                AppBarLayout appBarLayout = (AppBarLayout) view;
                int totalScrollRange = appBarLayout.getTotalScrollRange();
                int downNestedPreScrollRange = appBarLayout.getDownNestedPreScrollRange();
                int appBarLayoutOffset = getAppBarLayoutOffset(appBarLayout);
                if (downNestedPreScrollRange != 0 && totalScrollRange + appBarLayoutOffset <= downNestedPreScrollRange) {
                    return 0.0f;
                }
                totalScrollRange -= downNestedPreScrollRange;
                if (totalScrollRange != 0) {
                    return (((float) appBarLayoutOffset) / ((float) totalScrollRange)) + 1.0f;
                }
            }
            return 0.0f;
        }

        private static int getAppBarLayoutOffset(AppBarLayout appBarLayout) {
            android.support.design.widget.CoordinatorLayout.Behavior behavior = ((android.support.design.widget.CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).getBehavior();
            return behavior instanceof BaseBehavior ? ((BaseBehavior) behavior).getTopBottomOffsetForScrollingSibling() : 0;
        }

        /* Access modifiers changed, original: 0000 */
        public AppBarLayout findFirstDependency(List<View> list) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                View view = (View) list.get(i);
                if (view instanceof AppBarLayout) {
                    return (AppBarLayout) view;
                }
            }
            return null;
        }

        /* Access modifiers changed, original: 0000 */
        public int getScrollRange(View view) {
            if (view instanceof AppBarLayout) {
                return ((AppBarLayout) view).getTotalScrollRange();
            }
            return super.getScrollRange(view);
        }

        private void updateLiftedStateIfNeeded(View view, View view2) {
            if (view2 instanceof AppBarLayout) {
                AppBarLayout appBarLayout = (AppBarLayout) view2;
                if (appBarLayout.isLiftOnScroll()) {
                    appBarLayout.setLiftedState(view.getScrollY() > 0);
                }
            }
        }
    }

    public static class Behavior extends BaseBehavior<AppBarLayout> {

        public static abstract class DragCallback extends BaseDragCallback<AppBarLayout> {
        }

        public /* bridge */ /* synthetic */ int getLeftAndRightOffset() {
            return super.getLeftAndRightOffset();
        }

        public /* bridge */ /* synthetic */ int getTopAndBottomOffset() {
            return super.getTopAndBottomOffset();
        }

        public /* bridge */ /* synthetic */ boolean onLayoutChild(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, int i) {
            return super.onLayoutChild(coordinatorLayout, appBarLayout, i);
        }

        public /* bridge */ /* synthetic */ boolean onMeasureChild(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, int i, int i2, int i3, int i4) {
            return super.onMeasureChild(coordinatorLayout, appBarLayout, i, i2, i3, i4);
        }

        public /* bridge */ /* synthetic */ void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, View view, int i, int i2, int[] iArr, int i3) {
            super.onNestedPreScroll(coordinatorLayout, appBarLayout, view, i, i2, iArr, i3);
        }

        public /* bridge */ /* synthetic */ void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, View view, int i, int i2, int i3, int i4, int i5) {
            super.onNestedScroll(coordinatorLayout, appBarLayout, view, i, i2, i3, i4, i5);
        }

        public /* bridge */ /* synthetic */ void onRestoreInstanceState(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, Parcelable parcelable) {
            super.onRestoreInstanceState(coordinatorLayout, appBarLayout, parcelable);
        }

        public /* bridge */ /* synthetic */ Parcelable onSaveInstanceState(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout) {
            return super.onSaveInstanceState(coordinatorLayout, appBarLayout);
        }

        public /* bridge */ /* synthetic */ boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, View view, View view2, int i, int i2) {
            return super.onStartNestedScroll(coordinatorLayout, appBarLayout, view, view2, i, i2);
        }

        public /* bridge */ /* synthetic */ void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, View view, int i) {
            super.onStopNestedScroll(coordinatorLayout, appBarLayout, view, i);
        }

        public /* bridge */ /* synthetic */ void setDragCallback(@Nullable BaseDragCallback baseDragCallback) {
            super.setDragCallback(baseDragCallback);
        }

        public /* bridge */ /* synthetic */ boolean setLeftAndRightOffset(int i) {
            return super.setLeftAndRightOffset(i);
        }

        public /* bridge */ /* synthetic */ boolean setTopAndBottomOffset(int i) {
            return super.setTopAndBottomOffset(i);
        }

        public Behavior(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }
    }

    @Deprecated
    public float getTargetElevation() {
        return 0.0f;
    }

    public AppBarLayout(Context context) {
        this(context, null);
    }

    public AppBarLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.totalScrollRange = -1;
        this.downPreScrollRange = -1;
        this.downScrollRange = -1;
        this.pendingAction = 0;
        setOrientation(1);
        if (VERSION.SDK_INT >= 21) {
            ViewUtilsLollipop.setBoundsViewOutlineProvider(this);
            ViewUtilsLollipop.setStateListAnimatorFromAttrs(this, attributeSet, 0, R.style.Widget_Design_AppBarLayout);
        }
        TypedArray obtainStyledAttributes = ThemeEnforcement.obtainStyledAttributes(context, attributeSet, R.styleable.AppBarLayout, 0, R.style.Widget_Design_AppBarLayout, new int[0]);
        ViewCompat.setBackground(this, obtainStyledAttributes.getDrawable(R.styleable.AppBarLayout_android_background));
        if (obtainStyledAttributes.hasValue(R.styleable.AppBarLayout_expanded)) {
            setExpanded(obtainStyledAttributes.getBoolean(R.styleable.AppBarLayout_expanded, false), false, false);
        }
        if (VERSION.SDK_INT >= 21 && obtainStyledAttributes.hasValue(R.styleable.AppBarLayout_elevation)) {
            ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(this, (float) obtainStyledAttributes.getDimensionPixelSize(R.styleable.AppBarLayout_elevation, 0));
        }
        if (VERSION.SDK_INT >= 26) {
            if (obtainStyledAttributes.hasValue(R.styleable.AppBarLayout_android_keyboardNavigationCluster)) {
                setKeyboardNavigationCluster(obtainStyledAttributes.getBoolean(R.styleable.AppBarLayout_android_keyboardNavigationCluster, false));
            }
            if (obtainStyledAttributes.hasValue(R.styleable.AppBarLayout_android_touchscreenBlocksFocus)) {
                setTouchscreenBlocksFocus(obtainStyledAttributes.getBoolean(R.styleable.AppBarLayout_android_touchscreenBlocksFocus, false));
            }
        }
        this.liftOnScroll = obtainStyledAttributes.getBoolean(R.styleable.AppBarLayout_liftOnScroll, false);
        obtainStyledAttributes.recycle();
        ViewCompat.setOnApplyWindowInsetsListener(this, new OnApplyWindowInsetsListener() {
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
                return AppBarLayout.this.onWindowInsetChanged(windowInsetsCompat);
            }
        });
    }

    public void addOnOffsetChangedListener(BaseOnOffsetChangedListener baseOnOffsetChangedListener) {
        if (this.listeners == null) {
            this.listeners = new ArrayList();
        }
        if (baseOnOffsetChangedListener != null && !this.listeners.contains(baseOnOffsetChangedListener)) {
            this.listeners.add(baseOnOffsetChangedListener);
        }
    }

    public void addOnOffsetChangedListener(OnOffsetChangedListener onOffsetChangedListener) {
        addOnOffsetChangedListener((BaseOnOffsetChangedListener) onOffsetChangedListener);
    }

    public void removeOnOffsetChangedListener(BaseOnOffsetChangedListener baseOnOffsetChangedListener) {
        if (this.listeners != null && baseOnOffsetChangedListener != null) {
            this.listeners.remove(baseOnOffsetChangedListener);
        }
    }

    public void removeOnOffsetChangedListener(OnOffsetChangedListener onOffsetChangedListener) {
        removeOnOffsetChangedListener((BaseOnOffsetChangedListener) onOffsetChangedListener);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        invalidateScrollRanges();
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        invalidateScrollRanges();
        z = false;
        this.haveChildWithInterpolator = false;
        i = getChildCount();
        for (i2 = 0; i2 < i; i2++) {
            if (((LayoutParams) getChildAt(i2).getLayoutParams()).getScrollInterpolator() != null) {
                this.haveChildWithInterpolator = true;
                break;
            }
        }
        if (!this.liftableOverride) {
            if (this.liftOnScroll || hasCollapsibleChild()) {
                z = true;
            }
            setLiftableState(z);
        }
    }

    private boolean hasCollapsibleChild() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (((LayoutParams) getChildAt(i).getLayoutParams()).isCollapsible()) {
                return true;
            }
        }
        return false;
    }

    private void invalidateScrollRanges() {
        this.totalScrollRange = -1;
        this.downPreScrollRange = -1;
        this.downScrollRange = -1;
    }

    public void setOrientation(int i) {
        if (i == 1) {
            super.setOrientation(i);
            return;
        }
        throw new IllegalArgumentException("AppBarLayout is always vertical and does not support horizontal orientation");
    }

    public void setExpanded(boolean z) {
        setExpanded(z, ViewCompat.isLaidOut(this));
    }

    public void setExpanded(boolean z, boolean z2) {
        setExpanded(z, z2, true);
    }

    private void setExpanded(boolean z, boolean z2, boolean z3) {
        int i = 0;
        int i2 = (z ? 1 : 2) | (z2 ? 4 : 0);
        if (z3) {
            i = 8;
        }
        this.pendingAction = i2 | i;
        requestLayout();
    }

    /* Access modifiers changed, original: protected */
    public boolean checkLayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    /* Access modifiers changed, original: protected */
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -2);
    }

    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* Access modifiers changed, original: protected */
    public LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
        if (VERSION.SDK_INT >= 19 && (layoutParams instanceof android.widget.LinearLayout.LayoutParams)) {
            return new LayoutParams((android.widget.LinearLayout.LayoutParams) layoutParams);
        }
        if (layoutParams instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) layoutParams);
        }
        return new LayoutParams(layoutParams);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasChildWithInterpolator() {
        return this.haveChildWithInterpolator;
    }

    public final int getTotalScrollRange() {
        if (this.totalScrollRange != -1) {
            return this.totalScrollRange;
        }
        int childCount = getChildCount();
        int i = 0;
        for (int i2 = 0; i2 < childCount; i2++) {
            View childAt = getChildAt(i2);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            int measuredHeight = childAt.getMeasuredHeight();
            int i3 = layoutParams.scrollFlags;
            if ((i3 & 1) == 0) {
                break;
            }
            i += (measuredHeight + layoutParams.topMargin) + layoutParams.bottomMargin;
            if ((i3 & 2) != 0) {
                i -= ViewCompat.getMinimumHeight(childAt);
                break;
            }
        }
        childCount = Math.max(0, i - getTopInset());
        this.totalScrollRange = childCount;
        return childCount;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasScrollableChildren() {
        return getTotalScrollRange() != 0;
    }

    /* Access modifiers changed, original: 0000 */
    public int getUpNestedPreScrollRange() {
        return getTotalScrollRange();
    }

    /* Access modifiers changed, original: 0000 */
    public int getDownNestedPreScrollRange() {
        if (this.downPreScrollRange != -1) {
            return this.downPreScrollRange;
        }
        int childCount;
        int i = 0;
        for (childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = getChildAt(childCount);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            int measuredHeight = childAt.getMeasuredHeight();
            int i2 = layoutParams.scrollFlags;
            if ((i2 & 5) == 5) {
                i += layoutParams.topMargin + layoutParams.bottomMargin;
                if ((i2 & 8) != 0) {
                    i += ViewCompat.getMinimumHeight(childAt);
                } else if ((i2 & 2) != 0) {
                    i += measuredHeight - ViewCompat.getMinimumHeight(childAt);
                } else {
                    i += measuredHeight - getTopInset();
                }
            } else if (i > 0) {
                break;
            }
        }
        childCount = Math.max(0, i);
        this.downPreScrollRange = childCount;
        return childCount;
    }

    /* Access modifiers changed, original: 0000 */
    public int getDownNestedScrollRange() {
        if (this.downScrollRange != -1) {
            return this.downScrollRange;
        }
        int childCount = getChildCount();
        int i = 0;
        for (int i2 = 0; i2 < childCount; i2++) {
            View childAt = getChildAt(i2);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            int measuredHeight = childAt.getMeasuredHeight() + (layoutParams.topMargin + layoutParams.bottomMargin);
            int i3 = layoutParams.scrollFlags;
            if ((i3 & 1) == 0) {
                break;
            }
            i += measuredHeight;
            if ((i3 & 2) != 0) {
                i -= ViewCompat.getMinimumHeight(childAt) + getTopInset();
                break;
            }
        }
        childCount = Math.max(0, i);
        this.downScrollRange = childCount;
        return childCount;
    }

    /* Access modifiers changed, original: 0000 */
    public void dispatchOffsetUpdates(int i) {
        if (this.listeners != null) {
            int size = this.listeners.size();
            for (int i2 = 0; i2 < size; i2++) {
                BaseOnOffsetChangedListener baseOnOffsetChangedListener = (BaseOnOffsetChangedListener) this.listeners.get(i2);
                if (baseOnOffsetChangedListener != null) {
                    baseOnOffsetChangedListener.onOffsetChanged(this, i);
                }
            }
        }
    }

    public final int getMinimumHeightForVisibleOverlappingContent() {
        int topInset = getTopInset();
        int minimumHeight = ViewCompat.getMinimumHeight(this);
        if (minimumHeight != 0) {
            return (minimumHeight * 2) + topInset;
        }
        minimumHeight = getChildCount();
        minimumHeight = minimumHeight >= 1 ? ViewCompat.getMinimumHeight(getChildAt(minimumHeight - 1)) : 0;
        if (minimumHeight != 0) {
            return (minimumHeight * 2) + topInset;
        }
        return getHeight() / 3;
    }

    /* Access modifiers changed, original: protected */
    public int[] onCreateDrawableState(int i) {
        if (this.tmpStatesArray == null) {
            this.tmpStatesArray = new int[4];
        }
        int[] iArr = this.tmpStatesArray;
        int[] onCreateDrawableState = super.onCreateDrawableState(i + iArr.length);
        iArr[0] = this.liftable ? R.attr.state_liftable : -R.attr.state_liftable;
        int i2 = (this.liftable && this.lifted) ? R.attr.state_lifted : -R.attr.state_lifted;
        iArr[1] = i2;
        iArr[2] = this.liftable ? R.attr.state_collapsible : -R.attr.state_collapsible;
        i2 = (this.liftable && this.lifted) ? R.attr.state_collapsed : -R.attr.state_collapsed;
        iArr[3] = i2;
        return mergeDrawableStates(onCreateDrawableState, iArr);
    }

    public boolean setLiftable(boolean z) {
        this.liftableOverride = true;
        return setLiftableState(z);
    }

    private boolean setLiftableState(boolean z) {
        if (this.liftable == z) {
            return false;
        }
        this.liftable = z;
        refreshDrawableState();
        return true;
    }

    public boolean setLifted(boolean z) {
        return setLiftedState(z);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean setLiftedState(boolean z) {
        if (this.lifted == z) {
            return false;
        }
        this.lifted = z;
        refreshDrawableState();
        return true;
    }

    public void setLiftOnScroll(boolean z) {
        this.liftOnScroll = z;
    }

    public boolean isLiftOnScroll() {
        return this.liftOnScroll;
    }

    @Deprecated
    public void setTargetElevation(float f) {
        if (VERSION.SDK_INT >= 21) {
            ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(this, f);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public int getPendingAction() {
        return this.pendingAction;
    }

    /* Access modifiers changed, original: 0000 */
    public void resetPendingAction() {
        this.pendingAction = 0;
    }

    /* Access modifiers changed, original: final */
    @VisibleForTesting
    public final int getTopInset() {
        return this.lastInsets != null ? this.lastInsets.getSystemWindowInsetTop() : 0;
    }

    /* Access modifiers changed, original: 0000 */
    public WindowInsetsCompat onWindowInsetChanged(WindowInsetsCompat windowInsetsCompat) {
        Object obj = ViewCompat.getFitsSystemWindows(this) ? windowInsetsCompat : null;
        if (!ObjectsCompat.equals(this.lastInsets, obj)) {
            this.lastInsets = obj;
            invalidateScrollRanges();
        }
        return windowInsetsCompat;
    }
}
