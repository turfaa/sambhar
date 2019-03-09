package android.support.v7.widget.helper;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.recyclerview.R;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ChildDrawingOrderCallback;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.OnChildAttachStateChangeListener;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import java.util.ArrayList;
import java.util.List;

public class ItemTouchHelper extends ItemDecoration implements OnChildAttachStateChangeListener {
    static final int ACTION_MODE_DRAG_MASK = 16711680;
    private static final int ACTION_MODE_IDLE_MASK = 255;
    static final int ACTION_MODE_SWIPE_MASK = 65280;
    public static final int ACTION_STATE_DRAG = 2;
    public static final int ACTION_STATE_IDLE = 0;
    public static final int ACTION_STATE_SWIPE = 1;
    private static final int ACTIVE_POINTER_ID_NONE = -1;
    public static final int ANIMATION_TYPE_DRAG = 8;
    public static final int ANIMATION_TYPE_SWIPE_CANCEL = 4;
    public static final int ANIMATION_TYPE_SWIPE_SUCCESS = 2;
    private static final boolean DEBUG = false;
    static final int DIRECTION_FLAG_COUNT = 8;
    public static final int DOWN = 2;
    public static final int END = 32;
    public static final int LEFT = 4;
    private static final int PIXELS_PER_SECOND = 1000;
    public static final int RIGHT = 8;
    public static final int START = 16;
    private static final String TAG = "ItemTouchHelper";
    public static final int UP = 1;
    private int mActionState = 0;
    int mActivePointerId = -1;
    @NonNull
    Callback mCallback;
    private ChildDrawingOrderCallback mChildDrawingOrderCallback = null;
    private List<Integer> mDistances;
    private long mDragScrollStartTimeInMs;
    float mDx;
    float mDy;
    GestureDetectorCompat mGestureDetector;
    float mInitialTouchX;
    float mInitialTouchY;
    private ItemTouchHelperGestureListener mItemTouchHelperGestureListener;
    private float mMaxSwipeVelocity;
    private final OnItemTouchListener mOnItemTouchListener = new OnItemTouchListener() {
        public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
            ItemTouchHelper.this.mGestureDetector.onTouchEvent(motionEvent);
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                ItemTouchHelper.this.mActivePointerId = motionEvent.getPointerId(0);
                ItemTouchHelper.this.mInitialTouchX = motionEvent.getX();
                ItemTouchHelper.this.mInitialTouchY = motionEvent.getY();
                ItemTouchHelper.this.obtainVelocityTracker();
                if (ItemTouchHelper.this.mSelected == null) {
                    RecoverAnimation findAnimation = ItemTouchHelper.this.findAnimation(motionEvent);
                    if (findAnimation != null) {
                        ItemTouchHelper itemTouchHelper = ItemTouchHelper.this;
                        itemTouchHelper.mInitialTouchX -= findAnimation.mX;
                        itemTouchHelper = ItemTouchHelper.this;
                        itemTouchHelper.mInitialTouchY -= findAnimation.mY;
                        ItemTouchHelper.this.endRecoverAnimation(findAnimation.mViewHolder, true);
                        if (ItemTouchHelper.this.mPendingCleanup.remove(findAnimation.mViewHolder.itemView)) {
                            ItemTouchHelper.this.mCallback.clearView(ItemTouchHelper.this.mRecyclerView, findAnimation.mViewHolder);
                        }
                        ItemTouchHelper.this.select(findAnimation.mViewHolder, findAnimation.mActionState);
                        ItemTouchHelper.this.updateDxDy(motionEvent, ItemTouchHelper.this.mSelectedFlags, 0);
                    }
                }
            } else if (actionMasked == 3 || actionMasked == 1) {
                ItemTouchHelper.this.mActivePointerId = -1;
                ItemTouchHelper.this.select(null, 0);
            } else if (ItemTouchHelper.this.mActivePointerId != -1) {
                int findPointerIndex = motionEvent.findPointerIndex(ItemTouchHelper.this.mActivePointerId);
                if (findPointerIndex >= 0) {
                    ItemTouchHelper.this.checkSelectForSwipe(actionMasked, motionEvent, findPointerIndex);
                }
            }
            if (ItemTouchHelper.this.mVelocityTracker != null) {
                ItemTouchHelper.this.mVelocityTracker.addMovement(motionEvent);
            }
            if (ItemTouchHelper.this.mSelected != null) {
                return true;
            }
            return false;
        }

        public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
            ItemTouchHelper.this.mGestureDetector.onTouchEvent(motionEvent);
            if (ItemTouchHelper.this.mVelocityTracker != null) {
                ItemTouchHelper.this.mVelocityTracker.addMovement(motionEvent);
            }
            if (ItemTouchHelper.this.mActivePointerId != -1) {
                int actionMasked = motionEvent.getActionMasked();
                int findPointerIndex = motionEvent.findPointerIndex(ItemTouchHelper.this.mActivePointerId);
                if (findPointerIndex >= 0) {
                    ItemTouchHelper.this.checkSelectForSwipe(actionMasked, motionEvent, findPointerIndex);
                }
                ViewHolder viewHolder = ItemTouchHelper.this.mSelected;
                if (viewHolder != null) {
                    int i = 0;
                    if (actionMasked != 6) {
                        switch (actionMasked) {
                            case 1:
                                break;
                            case 2:
                                if (findPointerIndex >= 0) {
                                    ItemTouchHelper.this.updateDxDy(motionEvent, ItemTouchHelper.this.mSelectedFlags, findPointerIndex);
                                    ItemTouchHelper.this.moveIfNecessary(viewHolder);
                                    ItemTouchHelper.this.mRecyclerView.removeCallbacks(ItemTouchHelper.this.mScrollRunnable);
                                    ItemTouchHelper.this.mScrollRunnable.run();
                                    ItemTouchHelper.this.mRecyclerView.invalidate();
                                    break;
                                }
                                break;
                            case 3:
                                if (ItemTouchHelper.this.mVelocityTracker != null) {
                                    ItemTouchHelper.this.mVelocityTracker.clear();
                                    break;
                                }
                                break;
                        }
                        ItemTouchHelper.this.select(null, 0);
                        ItemTouchHelper.this.mActivePointerId = -1;
                    } else {
                        actionMasked = motionEvent.getActionIndex();
                        if (motionEvent.getPointerId(actionMasked) == ItemTouchHelper.this.mActivePointerId) {
                            if (actionMasked == 0) {
                                i = 1;
                            }
                            ItemTouchHelper.this.mActivePointerId = motionEvent.getPointerId(i);
                            ItemTouchHelper.this.updateDxDy(motionEvent, ItemTouchHelper.this.mSelectedFlags, actionMasked);
                        }
                    }
                }
            }
        }

        public void onRequestDisallowInterceptTouchEvent(boolean z) {
            if (z) {
                ItemTouchHelper.this.select(null, 0);
            }
        }
    };
    View mOverdrawChild = null;
    int mOverdrawChildPosition = -1;
    final List<View> mPendingCleanup = new ArrayList();
    List<RecoverAnimation> mRecoverAnimations = new ArrayList();
    RecyclerView mRecyclerView;
    final Runnable mScrollRunnable = new Runnable() {
        public void run() {
            if (ItemTouchHelper.this.mSelected != null && ItemTouchHelper.this.scrollIfNecessary()) {
                if (ItemTouchHelper.this.mSelected != null) {
                    ItemTouchHelper.this.moveIfNecessary(ItemTouchHelper.this.mSelected);
                }
                ItemTouchHelper.this.mRecyclerView.removeCallbacks(ItemTouchHelper.this.mScrollRunnable);
                ViewCompat.postOnAnimation(ItemTouchHelper.this.mRecyclerView, this);
            }
        }
    };
    ViewHolder mSelected = null;
    int mSelectedFlags;
    private float mSelectedStartX;
    private float mSelectedStartY;
    private int mSlop;
    private List<ViewHolder> mSwapTargets;
    private float mSwipeEscapeVelocity;
    private final float[] mTmpPosition = new float[2];
    private Rect mTmpRect;
    VelocityTracker mVelocityTracker;

    public static abstract class Callback {
        private static final int ABS_HORIZONTAL_DIR_FLAGS = 789516;
        public static final int DEFAULT_DRAG_ANIMATION_DURATION = 200;
        public static final int DEFAULT_SWIPE_ANIMATION_DURATION = 250;
        private static final long DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS = 2000;
        static final int RELATIVE_DIR_FLAGS = 3158064;
        private static final Interpolator sDragScrollInterpolator = new Interpolator() {
            public float getInterpolation(float f) {
                return (((f * f) * f) * f) * f;
            }
        };
        private static final Interpolator sDragViewScrollCapInterpolator = new Interpolator() {
            public float getInterpolation(float f) {
                f -= 1.0f;
                return ((((f * f) * f) * f) * f) + 1.0f;
            }
        };
        private int mCachedMaxScrollSpeed = -1;

        public static int convertToRelativeDirection(int i, int i2) {
            int i3 = i & ABS_HORIZONTAL_DIR_FLAGS;
            if (i3 == 0) {
                return i;
            }
            i &= i3 ^ -1;
            if (i2 == 0) {
                return i | (i3 << 2);
            }
            i2 = i3 << 1;
            return (i | (-789517 & i2)) | ((i2 & ABS_HORIZONTAL_DIR_FLAGS) << 2);
        }

        public static int makeFlag(int i, int i2) {
            return i2 << (i * 8);
        }

        public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, @NonNull ViewHolder viewHolder2) {
            return true;
        }

        public int convertToAbsoluteDirection(int i, int i2) {
            int i3 = i & RELATIVE_DIR_FLAGS;
            if (i3 == 0) {
                return i;
            }
            i &= i3 ^ -1;
            if (i2 == 0) {
                return i | (i3 >> 2);
            }
            i2 = i3 >> 1;
            return (i | (-3158065 & i2)) | ((i2 & RELATIVE_DIR_FLAGS) >> 2);
        }

        public int getBoundingBoxMargin() {
            return 0;
        }

        public float getMoveThreshold(@NonNull ViewHolder viewHolder) {
            return 0.5f;
        }

        public abstract int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder);

        public float getSwipeEscapeVelocity(float f) {
            return f;
        }

        public float getSwipeThreshold(@NonNull ViewHolder viewHolder) {
            return 0.5f;
        }

        public float getSwipeVelocityThreshold(float f) {
            return f;
        }

        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        public boolean isLongPressDragEnabled() {
            return true;
        }

        public abstract boolean onMove(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, @NonNull ViewHolder viewHolder2);

        public abstract void onSwiped(@NonNull ViewHolder viewHolder, int i);

        @NonNull
        public static ItemTouchUIUtil getDefaultUIUtil() {
            return ItemTouchUIUtilImpl.INSTANCE;
        }

        public static int makeMovementFlags(int i, int i2) {
            return makeFlag(2, i) | (makeFlag(1, i2) | makeFlag(0, i2 | i));
        }

        /* Access modifiers changed, original: final */
        public final int getAbsoluteMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
            return convertToAbsoluteDirection(getMovementFlags(recyclerView, viewHolder), ViewCompat.getLayoutDirection(recyclerView));
        }

        /* Access modifiers changed, original: 0000 */
        public boolean hasDragFlag(RecyclerView recyclerView, ViewHolder viewHolder) {
            return (getAbsoluteMovementFlags(recyclerView, viewHolder) & ItemTouchHelper.ACTION_MODE_DRAG_MASK) != 0;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean hasSwipeFlag(RecyclerView recyclerView, ViewHolder viewHolder) {
            return (getAbsoluteMovementFlags(recyclerView, viewHolder) & 65280) != 0;
        }

        /* JADX WARNING: Removed duplicated region for block: B:13:0x0056  */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0078  */
        /* JADX WARNING: Removed duplicated region for block: B:29:0x009a  */
        public android.support.v7.widget.RecyclerView.ViewHolder chooseDropTarget(@android.support.annotation.NonNull android.support.v7.widget.RecyclerView.ViewHolder r15, @android.support.annotation.NonNull java.util.List<android.support.v7.widget.RecyclerView.ViewHolder> r16, int r17, int r18) {
            /*
            r14 = this;
            r0 = r15;
            r1 = r0.itemView;
            r1 = r1.getWidth();
            r1 = r17 + r1;
            r2 = r0.itemView;
            r2 = r2.getHeight();
            r2 = r18 + r2;
            r3 = r0.itemView;
            r3 = r3.getLeft();
            r3 = r17 - r3;
            r4 = r0.itemView;
            r4 = r4.getTop();
            r4 = r18 - r4;
            r5 = r16.size();
            r6 = 0;
            r7 = -1;
            r8 = 0;
        L_0x0028:
            if (r8 >= r5) goto L_0x00be;
        L_0x002a:
            r9 = r16;
            r10 = r9.get(r8);
            r10 = (android.support.v7.widget.RecyclerView.ViewHolder) r10;
            if (r3 <= 0) goto L_0x0053;
        L_0x0034:
            r11 = r10.itemView;
            r11 = r11.getRight();
            r11 = r11 - r1;
            if (r11 >= 0) goto L_0x0053;
        L_0x003d:
            r12 = r10.itemView;
            r12 = r12.getRight();
            r13 = r0.itemView;
            r13 = r13.getRight();
            if (r12 <= r13) goto L_0x0053;
        L_0x004b:
            r11 = java.lang.Math.abs(r11);
            if (r11 <= r7) goto L_0x0053;
        L_0x0051:
            r6 = r10;
            goto L_0x0054;
        L_0x0053:
            r11 = r7;
        L_0x0054:
            if (r3 >= 0) goto L_0x0076;
        L_0x0056:
            r7 = r10.itemView;
            r7 = r7.getLeft();
            r7 = r7 - r17;
            if (r7 <= 0) goto L_0x0076;
        L_0x0060:
            r12 = r10.itemView;
            r12 = r12.getLeft();
            r13 = r0.itemView;
            r13 = r13.getLeft();
            if (r12 >= r13) goto L_0x0076;
        L_0x006e:
            r7 = java.lang.Math.abs(r7);
            if (r7 <= r11) goto L_0x0076;
        L_0x0074:
            r11 = r7;
            r6 = r10;
        L_0x0076:
            if (r4 >= 0) goto L_0x0098;
        L_0x0078:
            r7 = r10.itemView;
            r7 = r7.getTop();
            r7 = r7 - r18;
            if (r7 <= 0) goto L_0x0098;
        L_0x0082:
            r12 = r10.itemView;
            r12 = r12.getTop();
            r13 = r0.itemView;
            r13 = r13.getTop();
            if (r12 >= r13) goto L_0x0098;
        L_0x0090:
            r7 = java.lang.Math.abs(r7);
            if (r7 <= r11) goto L_0x0098;
        L_0x0096:
            r11 = r7;
            r6 = r10;
        L_0x0098:
            if (r4 <= 0) goto L_0x00b9;
        L_0x009a:
            r7 = r10.itemView;
            r7 = r7.getBottom();
            r7 = r7 - r2;
            if (r7 >= 0) goto L_0x00b9;
        L_0x00a3:
            r12 = r10.itemView;
            r12 = r12.getBottom();
            r13 = r0.itemView;
            r13 = r13.getBottom();
            if (r12 <= r13) goto L_0x00b9;
        L_0x00b1:
            r7 = java.lang.Math.abs(r7);
            if (r7 <= r11) goto L_0x00b9;
        L_0x00b7:
            r6 = r10;
            goto L_0x00ba;
        L_0x00b9:
            r7 = r11;
        L_0x00ba:
            r8 = r8 + 1;
            goto L_0x0028;
        L_0x00be:
            return r6;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.v7.widget.helper.ItemTouchHelper$Callback.chooseDropTarget(android.support.v7.widget.RecyclerView$ViewHolder, java.util.List, int, int):android.support.v7.widget.RecyclerView$ViewHolder");
        }

        public void onSelectedChanged(@Nullable ViewHolder viewHolder, int i) {
            if (viewHolder != null) {
                ItemTouchUIUtilImpl.INSTANCE.onSelected(viewHolder.itemView);
            }
        }

        private int getMaxDragScroll(RecyclerView recyclerView) {
            if (this.mCachedMaxScrollSpeed == -1) {
                this.mCachedMaxScrollSpeed = recyclerView.getResources().getDimensionPixelSize(R.dimen.item_touch_helper_max_drag_scroll_per_frame);
            }
            return this.mCachedMaxScrollSpeed;
        }

        public void onMoved(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, int i, @NonNull ViewHolder viewHolder2, int i2, int i3, int i4) {
            LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof ViewDropHandler) {
                ((ViewDropHandler) layoutManager).prepareForDrop(viewHolder.itemView, viewHolder2.itemView, i3, i4);
                return;
            }
            if (layoutManager.canScrollHorizontally()) {
                if (layoutManager.getDecoratedLeft(viewHolder2.itemView) <= recyclerView.getPaddingLeft()) {
                    recyclerView.scrollToPosition(i2);
                }
                if (layoutManager.getDecoratedRight(viewHolder2.itemView) >= recyclerView.getWidth() - recyclerView.getPaddingRight()) {
                    recyclerView.scrollToPosition(i2);
                }
            }
            if (layoutManager.canScrollVertically()) {
                if (layoutManager.getDecoratedTop(viewHolder2.itemView) <= recyclerView.getPaddingTop()) {
                    recyclerView.scrollToPosition(i2);
                }
                if (layoutManager.getDecoratedBottom(viewHolder2.itemView) >= recyclerView.getHeight() - recyclerView.getPaddingBottom()) {
                    recyclerView.scrollToPosition(i2);
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void onDraw(Canvas canvas, RecyclerView recyclerView, ViewHolder viewHolder, List<RecoverAnimation> list, int i, float f, float f2) {
            Canvas canvas2 = canvas;
            int size = list.size();
            for (int i2 = 0; i2 < size; i2++) {
                RecoverAnimation recoverAnimation = (RecoverAnimation) list.get(i2);
                recoverAnimation.update();
                int save = canvas.save();
                onChildDraw(canvas, recyclerView, recoverAnimation.mViewHolder, recoverAnimation.mX, recoverAnimation.mY, recoverAnimation.mActionState, false);
                canvas.restoreToCount(save);
            }
            if (viewHolder != null) {
                size = canvas.save();
                onChildDraw(canvas, recyclerView, viewHolder, f, f2, i, true);
                canvas.restoreToCount(size);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void onDrawOver(Canvas canvas, RecyclerView recyclerView, ViewHolder viewHolder, List<RecoverAnimation> list, int i, float f, float f2) {
            int i2;
            Canvas canvas2 = canvas;
            List<RecoverAnimation> list2 = list;
            int size = list.size();
            Object obj = null;
            for (i2 = 0; i2 < size; i2++) {
                RecoverAnimation recoverAnimation = (RecoverAnimation) list2.get(i2);
                int save = canvas.save();
                onChildDrawOver(canvas, recyclerView, recoverAnimation.mViewHolder, recoverAnimation.mX, recoverAnimation.mY, recoverAnimation.mActionState, false);
                canvas.restoreToCount(save);
            }
            if (viewHolder != null) {
                i2 = canvas.save();
                onChildDrawOver(canvas, recyclerView, viewHolder, f, f2, i, true);
                canvas.restoreToCount(i2);
            }
            for (size--; size >= 0; size--) {
                RecoverAnimation recoverAnimation2 = (RecoverAnimation) list2.get(size);
                if (recoverAnimation2.mEnded && !recoverAnimation2.mIsPendingCleanup) {
                    list2.remove(size);
                } else if (!recoverAnimation2.mEnded) {
                    obj = 1;
                }
            }
            if (obj != null) {
                recyclerView.invalidate();
            }
        }

        public void clearView(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
            ItemTouchUIUtilImpl.INSTANCE.clearView(viewHolder.itemView);
        }

        public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, float f, float f2, int i, boolean z) {
            ItemTouchUIUtilImpl.INSTANCE.onDraw(canvas, recyclerView, viewHolder.itemView, f, f2, i, z);
        }

        public void onChildDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView, ViewHolder viewHolder, float f, float f2, int i, boolean z) {
            ItemTouchUIUtilImpl.INSTANCE.onDrawOver(canvas, recyclerView, viewHolder.itemView, f, f2, i, z);
        }

        public long getAnimationDuration(@NonNull RecyclerView recyclerView, int i, float f, float f2) {
            ItemAnimator itemAnimator = recyclerView.getItemAnimator();
            if (itemAnimator == null) {
                return i == 8 ? 200 : 250;
            }
            long moveDuration;
            if (i == 8) {
                moveDuration = itemAnimator.getMoveDuration();
            } else {
                moveDuration = itemAnimator.getRemoveDuration();
            }
            return moveDuration;
        }

        public int interpolateOutOfBoundsScroll(@NonNull RecyclerView recyclerView, int i, int i2, int i3, long j) {
            float f = 1.0f;
            int signum = (int) (((float) (((int) Math.signum((float) i2)) * getMaxDragScroll(recyclerView))) * sDragViewScrollCapInterpolator.getInterpolation(Math.min(1.0f, (((float) Math.abs(i2)) * 1.0f) / ((float) i))));
            if (j <= DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS) {
                f = ((float) j) / 2000.0f;
            }
            signum = (int) (((float) signum) * sDragScrollInterpolator.getInterpolation(f));
            if (signum != 0) {
                return signum;
            }
            return i2 > 0 ? 1 : -1;
        }
    }

    private class ItemTouchHelperGestureListener extends SimpleOnGestureListener {
        private boolean mShouldReactToLongPress = true;

        public boolean onDown(MotionEvent motionEvent) {
            return true;
        }

        ItemTouchHelperGestureListener() {
        }

        /* Access modifiers changed, original: 0000 */
        public void doNotReactToLongPress() {
            this.mShouldReactToLongPress = false;
        }

        public void onLongPress(MotionEvent motionEvent) {
            if (this.mShouldReactToLongPress) {
                View findChildView = ItemTouchHelper.this.findChildView(motionEvent);
                if (findChildView != null) {
                    ViewHolder childViewHolder = ItemTouchHelper.this.mRecyclerView.getChildViewHolder(findChildView);
                    if (childViewHolder != null && ItemTouchHelper.this.mCallback.hasDragFlag(ItemTouchHelper.this.mRecyclerView, childViewHolder) && motionEvent.getPointerId(0) == ItemTouchHelper.this.mActivePointerId) {
                        int findPointerIndex = motionEvent.findPointerIndex(ItemTouchHelper.this.mActivePointerId);
                        float x = motionEvent.getX(findPointerIndex);
                        float y = motionEvent.getY(findPointerIndex);
                        ItemTouchHelper.this.mInitialTouchX = x;
                        ItemTouchHelper.this.mInitialTouchY = y;
                        ItemTouchHelper itemTouchHelper = ItemTouchHelper.this;
                        ItemTouchHelper.this.mDy = 0.0f;
                        itemTouchHelper.mDx = 0.0f;
                        if (ItemTouchHelper.this.mCallback.isLongPressDragEnabled()) {
                            ItemTouchHelper.this.select(childViewHolder, 2);
                        }
                    }
                }
            }
        }
    }

    private static class RecoverAnimation implements AnimatorListener {
        final int mActionState;
        final int mAnimationType;
        boolean mEnded = false;
        private float mFraction;
        boolean mIsPendingCleanup;
        boolean mOverridden = false;
        final float mStartDx;
        final float mStartDy;
        final float mTargetX;
        final float mTargetY;
        private final ValueAnimator mValueAnimator;
        final ViewHolder mViewHolder;
        float mX;
        float mY;

        public void onAnimationRepeat(Animator animator) {
        }

        public void onAnimationStart(Animator animator) {
        }

        RecoverAnimation(ViewHolder viewHolder, int i, int i2, float f, float f2, float f3, float f4) {
            this.mActionState = i2;
            this.mAnimationType = i;
            this.mViewHolder = viewHolder;
            this.mStartDx = f;
            this.mStartDy = f2;
            this.mTargetX = f3;
            this.mTargetY = f4;
            this.mValueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            this.mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    RecoverAnimation.this.setFraction(valueAnimator.getAnimatedFraction());
                }
            });
            this.mValueAnimator.setTarget(viewHolder.itemView);
            this.mValueAnimator.addListener(this);
            setFraction(0.0f);
        }

        public void setDuration(long j) {
            this.mValueAnimator.setDuration(j);
        }

        public void start() {
            this.mViewHolder.setIsRecyclable(false);
            this.mValueAnimator.start();
        }

        public void cancel() {
            this.mValueAnimator.cancel();
        }

        public void setFraction(float f) {
            this.mFraction = f;
        }

        public void update() {
            if (this.mStartDx == this.mTargetX) {
                this.mX = this.mViewHolder.itemView.getTranslationX();
            } else {
                this.mX = this.mStartDx + (this.mFraction * (this.mTargetX - this.mStartDx));
            }
            if (this.mStartDy == this.mTargetY) {
                this.mY = this.mViewHolder.itemView.getTranslationY();
            } else {
                this.mY = this.mStartDy + (this.mFraction * (this.mTargetY - this.mStartDy));
            }
        }

        public void onAnimationEnd(Animator animator) {
            if (!this.mEnded) {
                this.mViewHolder.setIsRecyclable(true);
            }
            this.mEnded = true;
        }

        public void onAnimationCancel(Animator animator) {
            setFraction(1.0f);
        }
    }

    public interface ViewDropHandler {
        void prepareForDrop(@NonNull View view, @NonNull View view2, int i, int i2);
    }

    public static abstract class SimpleCallback extends Callback {
        private int mDefaultDragDirs;
        private int mDefaultSwipeDirs;

        public SimpleCallback(int i, int i2) {
            this.mDefaultSwipeDirs = i2;
            this.mDefaultDragDirs = i;
        }

        public void setDefaultSwipeDirs(int i) {
            this.mDefaultSwipeDirs = i;
        }

        public void setDefaultDragDirs(int i) {
            this.mDefaultDragDirs = i;
        }

        public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
            return this.mDefaultSwipeDirs;
        }

        public int getDragDirs(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
            return this.mDefaultDragDirs;
        }

        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
            return Callback.makeMovementFlags(getDragDirs(recyclerView, viewHolder), getSwipeDirs(recyclerView, viewHolder));
        }
    }

    public void onChildViewAttachedToWindow(@NonNull View view) {
    }

    public ItemTouchHelper(@NonNull Callback callback) {
        this.mCallback = callback;
    }

    private static boolean hitTest(View view, float f, float f2, float f3, float f4) {
        return f >= f3 && f <= f3 + ((float) view.getWidth()) && f2 >= f4 && f2 <= f4 + ((float) view.getHeight());
    }

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (this.mRecyclerView != recyclerView) {
            if (this.mRecyclerView != null) {
                destroyCallbacks();
            }
            this.mRecyclerView = recyclerView;
            if (recyclerView != null) {
                Resources resources = recyclerView.getResources();
                this.mSwipeEscapeVelocity = resources.getDimension(R.dimen.item_touch_helper_swipe_escape_velocity);
                this.mMaxSwipeVelocity = resources.getDimension(R.dimen.item_touch_helper_swipe_escape_max_velocity);
                setupCallbacks();
            }
        }
    }

    private void setupCallbacks() {
        this.mSlop = ViewConfiguration.get(this.mRecyclerView.getContext()).getScaledTouchSlop();
        this.mRecyclerView.addItemDecoration(this);
        this.mRecyclerView.addOnItemTouchListener(this.mOnItemTouchListener);
        this.mRecyclerView.addOnChildAttachStateChangeListener(this);
        startGestureDetection();
    }

    private void destroyCallbacks() {
        this.mRecyclerView.removeItemDecoration(this);
        this.mRecyclerView.removeOnItemTouchListener(this.mOnItemTouchListener);
        this.mRecyclerView.removeOnChildAttachStateChangeListener(this);
        for (int size = this.mRecoverAnimations.size() - 1; size >= 0; size--) {
            this.mCallback.clearView(this.mRecyclerView, ((RecoverAnimation) this.mRecoverAnimations.get(0)).mViewHolder);
        }
        this.mRecoverAnimations.clear();
        this.mOverdrawChild = null;
        this.mOverdrawChildPosition = -1;
        releaseVelocityTracker();
        stopGestureDetection();
    }

    private void startGestureDetection() {
        this.mItemTouchHelperGestureListener = new ItemTouchHelperGestureListener();
        this.mGestureDetector = new GestureDetectorCompat(this.mRecyclerView.getContext(), this.mItemTouchHelperGestureListener);
    }

    private void stopGestureDetection() {
        if (this.mItemTouchHelperGestureListener != null) {
            this.mItemTouchHelperGestureListener.doNotReactToLongPress();
            this.mItemTouchHelperGestureListener = null;
        }
        if (this.mGestureDetector != null) {
            this.mGestureDetector = null;
        }
    }

    private void getSelectedDxDy(float[] fArr) {
        if ((this.mSelectedFlags & 12) != 0) {
            fArr[0] = (this.mSelectedStartX + this.mDx) - ((float) this.mSelected.itemView.getLeft());
        } else {
            fArr[0] = this.mSelected.itemView.getTranslationX();
        }
        if ((this.mSelectedFlags & 3) != 0) {
            fArr[1] = (this.mSelectedStartY + this.mDy) - ((float) this.mSelected.itemView.getTop());
        } else {
            fArr[1] = this.mSelected.itemView.getTranslationY();
        }
    }

    public void onDrawOver(Canvas canvas, RecyclerView recyclerView, State state) {
        float f;
        float f2;
        if (this.mSelected != null) {
            getSelectedDxDy(this.mTmpPosition);
            float f3 = this.mTmpPosition[0];
            f = this.mTmpPosition[1];
            f2 = f3;
        } else {
            f2 = 0.0f;
            f = 0.0f;
        }
        this.mCallback.onDrawOver(canvas, recyclerView, this.mSelected, this.mRecoverAnimations, this.mActionState, f2, f);
    }

    public void onDraw(Canvas canvas, RecyclerView recyclerView, State state) {
        float f;
        float f2;
        this.mOverdrawChildPosition = -1;
        if (this.mSelected != null) {
            getSelectedDxDy(this.mTmpPosition);
            float f3 = this.mTmpPosition[0];
            f = this.mTmpPosition[1];
            f2 = f3;
        } else {
            f2 = 0.0f;
            f = 0.0f;
        }
        this.mCallback.onDraw(canvas, recyclerView, this.mSelected, this.mRecoverAnimations, this.mActionState, f2, f);
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0131  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x013b  */
    public void select(@android.support.annotation.Nullable android.support.v7.widget.RecyclerView.ViewHolder r24, int r25) {
        /*
        r23 = this;
        r11 = r23;
        r12 = r24;
        r13 = r25;
        r0 = r11.mSelected;
        if (r12 != r0) goto L_0x000f;
    L_0x000a:
        r0 = r11.mActionState;
        if (r13 != r0) goto L_0x000f;
    L_0x000e:
        return;
    L_0x000f:
        r0 = -9223372036854775808;
        r11.mDragScrollStartTimeInMs = r0;
        r4 = r11.mActionState;
        r14 = 1;
        r11.endRecoverAnimation(r12, r14);
        r11.mActionState = r13;
        r15 = 2;
        if (r13 != r15) goto L_0x0030;
    L_0x001e:
        if (r12 == 0) goto L_0x0028;
    L_0x0020:
        r0 = r12.itemView;
        r11.mOverdrawChild = r0;
        r23.addChildDrawingOrderCallback();
        goto L_0x0030;
    L_0x0028:
        r0 = new java.lang.IllegalArgumentException;
        r1 = "Must pass a ViewHolder when dragging";
        r0.<init>(r1);
        throw r0;
    L_0x0030:
        r0 = r13 * 8;
        r10 = 8;
        r0 = r0 + r10;
        r0 = r14 << r0;
        r16 = r0 + -1;
        r0 = r11.mSelected;
        r9 = 0;
        if (r0 == 0) goto L_0x00f2;
    L_0x003e:
        r8 = r11.mSelected;
        r0 = r8.itemView;
        r0 = r0.getParent();
        if (r0 == 0) goto L_0x00de;
    L_0x0048:
        if (r4 != r15) goto L_0x004c;
    L_0x004a:
        r7 = 0;
        goto L_0x0051;
    L_0x004c:
        r0 = r11.swipeIfNecessary(r8);
        r7 = r0;
    L_0x0051:
        r23.releaseVelocityTracker();
        r0 = 4;
        r1 = 0;
        if (r7 == r0) goto L_0x007e;
    L_0x0058:
        if (r7 == r10) goto L_0x007e;
    L_0x005a:
        r2 = 16;
        if (r7 == r2) goto L_0x007e;
    L_0x005e:
        r2 = 32;
        if (r7 == r2) goto L_0x007e;
    L_0x0062:
        switch(r7) {
            case 1: goto L_0x006a;
            case 2: goto L_0x006a;
            default: goto L_0x0065;
        };
    L_0x0065:
        r17 = 0;
    L_0x0067:
        r18 = 0;
        goto L_0x0090;
    L_0x006a:
        r2 = r11.mDy;
        r2 = java.lang.Math.signum(r2);
        r3 = r11.mRecyclerView;
        r3 = r3.getHeight();
        r3 = (float) r3;
        r2 = r2 * r3;
        r18 = r2;
        r17 = 0;
        goto L_0x0090;
    L_0x007e:
        r2 = r11.mDx;
        r2 = java.lang.Math.signum(r2);
        r3 = r11.mRecyclerView;
        r3 = r3.getWidth();
        r3 = (float) r3;
        r2 = r2 * r3;
        r17 = r2;
        goto L_0x0067;
    L_0x0090:
        if (r4 != r15) goto L_0x0095;
    L_0x0092:
        r6 = 8;
        goto L_0x009a;
    L_0x0095:
        if (r7 <= 0) goto L_0x0099;
    L_0x0097:
        r6 = 2;
        goto L_0x009a;
    L_0x0099:
        r6 = 4;
    L_0x009a:
        r0 = r11.mTmpPosition;
        r11.getSelectedDxDy(r0);
        r0 = r11.mTmpPosition;
        r19 = r0[r9];
        r0 = r11.mTmpPosition;
        r20 = r0[r14];
        r5 = new android.support.v7.widget.helper.ItemTouchHelper$3;
        r0 = r5;
        r1 = r23;
        r2 = r8;
        r3 = r6;
        r14 = r5;
        r5 = r19;
        r15 = r6;
        r6 = r20;
        r21 = r7;
        r7 = r17;
        r22 = r8;
        r8 = r18;
        r9 = r21;
        r21 = 8;
        r10 = r22;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10);
        r0 = r11.mCallback;
        r1 = r11.mRecyclerView;
        r2 = r17 - r19;
        r3 = r18 - r20;
        r0 = r0.getAnimationDuration(r1, r15, r2, r3);
        r14.setDuration(r0);
        r0 = r11.mRecoverAnimations;
        r0.add(r14);
        r14.start();
        r9 = 1;
        goto L_0x00ee;
    L_0x00de:
        r0 = r8;
        r21 = 8;
        r1 = r0.itemView;
        r11.removeChildDrawingOrderCallbackIfNecessary(r1);
        r1 = r11.mCallback;
        r2 = r11.mRecyclerView;
        r1.clearView(r2, r0);
        r9 = 0;
    L_0x00ee:
        r0 = 0;
        r11.mSelected = r0;
        goto L_0x00f5;
    L_0x00f2:
        r21 = 8;
        r9 = 0;
    L_0x00f5:
        if (r12 == 0) goto L_0x0128;
    L_0x00f7:
        r0 = r11.mCallback;
        r1 = r11.mRecyclerView;
        r0 = r0.getAbsoluteMovementFlags(r1, r12);
        r0 = r0 & r16;
        r1 = r11.mActionState;
        r1 = r1 * 8;
        r0 = r0 >> r1;
        r11.mSelectedFlags = r0;
        r0 = r12.itemView;
        r0 = r0.getLeft();
        r0 = (float) r0;
        r11.mSelectedStartX = r0;
        r0 = r12.itemView;
        r0 = r0.getTop();
        r0 = (float) r0;
        r11.mSelectedStartY = r0;
        r11.mSelected = r12;
        r0 = 2;
        if (r13 != r0) goto L_0x0128;
    L_0x011f:
        r0 = r11.mSelected;
        r0 = r0.itemView;
        r1 = 0;
        r0.performHapticFeedback(r1);
        goto L_0x0129;
    L_0x0128:
        r1 = 0;
    L_0x0129:
        r0 = r11.mRecyclerView;
        r0 = r0.getParent();
        if (r0 == 0) goto L_0x0139;
    L_0x0131:
        r2 = r11.mSelected;
        if (r2 == 0) goto L_0x0136;
    L_0x0135:
        r1 = 1;
    L_0x0136:
        r0.requestDisallowInterceptTouchEvent(r1);
    L_0x0139:
        if (r9 != 0) goto L_0x0144;
    L_0x013b:
        r0 = r11.mRecyclerView;
        r0 = r0.getLayoutManager();
        r0.requestSimpleAnimationsInNextLayout();
    L_0x0144:
        r0 = r11.mCallback;
        r1 = r11.mSelected;
        r2 = r11.mActionState;
        r0.onSelectedChanged(r1, r2);
        r0 = r11.mRecyclerView;
        r0.invalidate();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.widget.helper.ItemTouchHelper.select(android.support.v7.widget.RecyclerView$ViewHolder, int):void");
    }

    /* Access modifiers changed, original: 0000 */
    public void postDispatchSwipe(final RecoverAnimation recoverAnimation, final int i) {
        this.mRecyclerView.post(new Runnable() {
            public void run() {
                if (ItemTouchHelper.this.mRecyclerView != null && ItemTouchHelper.this.mRecyclerView.isAttachedToWindow() && !recoverAnimation.mOverridden && recoverAnimation.mViewHolder.getAdapterPosition() != -1) {
                    ItemAnimator itemAnimator = ItemTouchHelper.this.mRecyclerView.getItemAnimator();
                    if ((itemAnimator == null || !itemAnimator.isRunning(null)) && !ItemTouchHelper.this.hasRunningRecoverAnim()) {
                        ItemTouchHelper.this.mCallback.onSwiped(recoverAnimation.mViewHolder, i);
                    } else {
                        ItemTouchHelper.this.mRecyclerView.post(this);
                    }
                }
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasRunningRecoverAnim() {
        int size = this.mRecoverAnimations.size();
        for (int i = 0; i < size; i++) {
            if (!((RecoverAnimation) this.mRecoverAnimations.get(i)).mEnded) {
                return true;
            }
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00cd  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0103  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00e7  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0112  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0088  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00cd  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00e7  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0103  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0106 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0112  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0088  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00cd  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0103  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00e7  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0106 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0112  */
    /* JADX WARNING: Missing block: B:32:0x00c7, code skipped:
            if (r1 > 0) goto L_0x00cb;
     */
    public boolean scrollIfNecessary() {
        /*
        r16 = this;
        r0 = r16;
        r1 = r0.mSelected;
        r2 = 0;
        r3 = -9223372036854775808;
        if (r1 != 0) goto L_0x000c;
    L_0x0009:
        r0.mDragScrollStartTimeInMs = r3;
        return r2;
    L_0x000c:
        r5 = java.lang.System.currentTimeMillis();
        r7 = r0.mDragScrollStartTimeInMs;
        r1 = (r7 > r3 ? 1 : (r7 == r3 ? 0 : -1));
        if (r1 != 0) goto L_0x0019;
    L_0x0016:
        r7 = 0;
        goto L_0x001d;
    L_0x0019:
        r7 = r0.mDragScrollStartTimeInMs;
        r7 = r5 - r7;
    L_0x001d:
        r1 = r0.mRecyclerView;
        r1 = r1.getLayoutManager();
        r9 = r0.mTmpRect;
        if (r9 != 0) goto L_0x002e;
    L_0x0027:
        r9 = new android.graphics.Rect;
        r9.<init>();
        r0.mTmpRect = r9;
    L_0x002e:
        r9 = r0.mSelected;
        r9 = r9.itemView;
        r10 = r0.mTmpRect;
        r1.calculateItemDecorationsForChild(r9, r10);
        r9 = r1.canScrollHorizontally();
        r10 = 0;
        if (r9 == 0) goto L_0x0081;
    L_0x003e:
        r9 = r0.mSelectedStartX;
        r11 = r0.mDx;
        r9 = r9 + r11;
        r9 = (int) r9;
        r11 = r0.mTmpRect;
        r11 = r11.left;
        r11 = r9 - r11;
        r12 = r0.mRecyclerView;
        r12 = r12.getPaddingLeft();
        r11 = r11 - r12;
        r12 = r0.mDx;
        r12 = (r12 > r10 ? 1 : (r12 == r10 ? 0 : -1));
        if (r12 >= 0) goto L_0x005b;
    L_0x0057:
        if (r11 >= 0) goto L_0x005b;
    L_0x0059:
        r12 = r11;
        goto L_0x0082;
    L_0x005b:
        r11 = r0.mDx;
        r11 = (r11 > r10 ? 1 : (r11 == r10 ? 0 : -1));
        if (r11 <= 0) goto L_0x0081;
    L_0x0061:
        r11 = r0.mSelected;
        r11 = r11.itemView;
        r11 = r11.getWidth();
        r9 = r9 + r11;
        r11 = r0.mTmpRect;
        r11 = r11.right;
        r9 = r9 + r11;
        r11 = r0.mRecyclerView;
        r11 = r11.getWidth();
        r12 = r0.mRecyclerView;
        r12 = r12.getPaddingRight();
        r11 = r11 - r12;
        r9 = r9 - r11;
        if (r9 <= 0) goto L_0x0081;
    L_0x007f:
        r12 = r9;
        goto L_0x0082;
    L_0x0081:
        r12 = 0;
    L_0x0082:
        r1 = r1.canScrollVertically();
        if (r1 == 0) goto L_0x00ca;
    L_0x0088:
        r1 = r0.mSelectedStartY;
        r9 = r0.mDy;
        r1 = r1 + r9;
        r1 = (int) r1;
        r9 = r0.mTmpRect;
        r9 = r9.top;
        r9 = r1 - r9;
        r11 = r0.mRecyclerView;
        r11 = r11.getPaddingTop();
        r9 = r9 - r11;
        r11 = r0.mDy;
        r11 = (r11 > r10 ? 1 : (r11 == r10 ? 0 : -1));
        if (r11 >= 0) goto L_0x00a5;
    L_0x00a1:
        if (r9 >= 0) goto L_0x00a5;
    L_0x00a3:
        r1 = r9;
        goto L_0x00cb;
    L_0x00a5:
        r9 = r0.mDy;
        r9 = (r9 > r10 ? 1 : (r9 == r10 ? 0 : -1));
        if (r9 <= 0) goto L_0x00ca;
    L_0x00ab:
        r9 = r0.mSelected;
        r9 = r9.itemView;
        r9 = r9.getHeight();
        r1 = r1 + r9;
        r9 = r0.mTmpRect;
        r9 = r9.bottom;
        r1 = r1 + r9;
        r9 = r0.mRecyclerView;
        r9 = r9.getHeight();
        r10 = r0.mRecyclerView;
        r10 = r10.getPaddingBottom();
        r9 = r9 - r10;
        r1 = r1 - r9;
        if (r1 <= 0) goto L_0x00ca;
    L_0x00c9:
        goto L_0x00cb;
    L_0x00ca:
        r1 = 0;
    L_0x00cb:
        if (r12 == 0) goto L_0x00e4;
    L_0x00cd:
        r9 = r0.mCallback;
        r10 = r0.mRecyclerView;
        r11 = r0.mSelected;
        r11 = r11.itemView;
        r11 = r11.getWidth();
        r13 = r0.mRecyclerView;
        r13 = r13.getWidth();
        r14 = r7;
        r12 = r9.interpolateOutOfBoundsScroll(r10, r11, r12, r13, r14);
    L_0x00e4:
        r14 = r12;
        if (r1 == 0) goto L_0x0103;
    L_0x00e7:
        r9 = r0.mCallback;
        r10 = r0.mRecyclerView;
        r11 = r0.mSelected;
        r11 = r11.itemView;
        r11 = r11.getHeight();
        r12 = r0.mRecyclerView;
        r13 = r12.getHeight();
        r12 = r1;
        r1 = r14;
        r14 = r7;
        r7 = r9.interpolateOutOfBoundsScroll(r10, r11, r12, r13, r14);
        r12 = r1;
        r1 = r7;
        goto L_0x0104;
    L_0x0103:
        r12 = r14;
    L_0x0104:
        if (r12 != 0) goto L_0x010c;
    L_0x0106:
        if (r1 == 0) goto L_0x0109;
    L_0x0108:
        goto L_0x010c;
    L_0x0109:
        r0.mDragScrollStartTimeInMs = r3;
        return r2;
    L_0x010c:
        r7 = r0.mDragScrollStartTimeInMs;
        r2 = (r7 > r3 ? 1 : (r7 == r3 ? 0 : -1));
        if (r2 != 0) goto L_0x0114;
    L_0x0112:
        r0.mDragScrollStartTimeInMs = r5;
    L_0x0114:
        r2 = r0.mRecyclerView;
        r2.scrollBy(r12, r1);
        r1 = 1;
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.widget.helper.ItemTouchHelper.scrollIfNecessary():boolean");
    }

    private List<ViewHolder> findSwapTargets(ViewHolder viewHolder) {
        ViewHolder viewHolder2 = viewHolder;
        if (this.mSwapTargets == null) {
            this.mSwapTargets = new ArrayList();
            this.mDistances = new ArrayList();
        } else {
            this.mSwapTargets.clear();
            this.mDistances.clear();
        }
        int boundingBoxMargin = this.mCallback.getBoundingBoxMargin();
        int round = Math.round(this.mSelectedStartX + this.mDx) - boundingBoxMargin;
        int round2 = Math.round(this.mSelectedStartY + this.mDy) - boundingBoxMargin;
        boundingBoxMargin *= 2;
        int width = (viewHolder2.itemView.getWidth() + round) + boundingBoxMargin;
        int height = (viewHolder2.itemView.getHeight() + round2) + boundingBoxMargin;
        boundingBoxMargin = (round + width) / 2;
        int i = (round2 + height) / 2;
        LayoutManager layoutManager = this.mRecyclerView.getLayoutManager();
        int childCount = layoutManager.getChildCount();
        int i2 = 0;
        while (i2 < childCount) {
            View childAt = layoutManager.getChildAt(i2);
            if (childAt != viewHolder2.itemView && childAt.getBottom() >= round2 && childAt.getTop() <= height && childAt.getRight() >= round && childAt.getLeft() <= width) {
                ViewHolder childViewHolder = this.mRecyclerView.getChildViewHolder(childAt);
                if (this.mCallback.canDropOver(this.mRecyclerView, this.mSelected, childViewHolder)) {
                    int abs = Math.abs(boundingBoxMargin - ((childAt.getLeft() + childAt.getRight()) / 2));
                    int abs2 = Math.abs(i - ((childAt.getTop() + childAt.getBottom()) / 2));
                    abs = (abs * abs) + (abs2 * abs2);
                    abs2 = this.mSwapTargets.size();
                    int i3 = 0;
                    int i4 = 0;
                    while (i3 < abs2 && abs > ((Integer) this.mDistances.get(i3)).intValue()) {
                        i4++;
                        i3++;
                        viewHolder2 = viewHolder;
                    }
                    this.mSwapTargets.add(i4, childViewHolder);
                    this.mDistances.add(i4, Integer.valueOf(abs));
                }
            }
            i2++;
            viewHolder2 = viewHolder;
        }
        return this.mSwapTargets;
    }

    /* Access modifiers changed, original: 0000 */
    public void moveIfNecessary(ViewHolder viewHolder) {
        if (!this.mRecyclerView.isLayoutRequested() && this.mActionState == 2) {
            float moveThreshold = this.mCallback.getMoveThreshold(viewHolder);
            int i = (int) (this.mSelectedStartX + this.mDx);
            int i2 = (int) (this.mSelectedStartY + this.mDy);
            if (((float) Math.abs(i2 - viewHolder.itemView.getTop())) >= ((float) viewHolder.itemView.getHeight()) * moveThreshold || ((float) Math.abs(i - viewHolder.itemView.getLeft())) >= ((float) viewHolder.itemView.getWidth()) * moveThreshold) {
                List findSwapTargets = findSwapTargets(viewHolder);
                if (findSwapTargets.size() != 0) {
                    ViewHolder chooseDropTarget = this.mCallback.chooseDropTarget(viewHolder, findSwapTargets, i, i2);
                    if (chooseDropTarget == null) {
                        this.mSwapTargets.clear();
                        this.mDistances.clear();
                        return;
                    }
                    int adapterPosition = chooseDropTarget.getAdapterPosition();
                    int adapterPosition2 = viewHolder.getAdapterPosition();
                    if (this.mCallback.onMove(this.mRecyclerView, viewHolder, chooseDropTarget)) {
                        this.mCallback.onMoved(this.mRecyclerView, viewHolder, adapterPosition2, chooseDropTarget, adapterPosition, i, i2);
                    }
                }
            }
        }
    }

    public void onChildViewDetachedFromWindow(@NonNull View view) {
        removeChildDrawingOrderCallbackIfNecessary(view);
        ViewHolder childViewHolder = this.mRecyclerView.getChildViewHolder(view);
        if (childViewHolder != null) {
            if (this.mSelected == null || childViewHolder != this.mSelected) {
                endRecoverAnimation(childViewHolder, false);
                if (this.mPendingCleanup.remove(childViewHolder.itemView)) {
                    this.mCallback.clearView(this.mRecyclerView, childViewHolder);
                }
            } else {
                select(null, 0);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void endRecoverAnimation(ViewHolder viewHolder, boolean z) {
        for (int size = this.mRecoverAnimations.size() - 1; size >= 0; size--) {
            RecoverAnimation recoverAnimation = (RecoverAnimation) this.mRecoverAnimations.get(size);
            if (recoverAnimation.mViewHolder == viewHolder) {
                recoverAnimation.mOverridden |= z;
                if (!recoverAnimation.mEnded) {
                    recoverAnimation.cancel();
                }
                this.mRecoverAnimations.remove(size);
                return;
            }
        }
    }

    public void getItemOffsets(Rect rect, View view, RecyclerView recyclerView, State state) {
        rect.setEmpty();
    }

    /* Access modifiers changed, original: 0000 */
    public void obtainVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTracker.obtain();
    }

    private void releaseVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private ViewHolder findSwipedView(MotionEvent motionEvent) {
        LayoutManager layoutManager = this.mRecyclerView.getLayoutManager();
        if (this.mActivePointerId == -1) {
            return null;
        }
        int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
        float x = motionEvent.getX(findPointerIndex) - this.mInitialTouchX;
        float y = motionEvent.getY(findPointerIndex) - this.mInitialTouchY;
        x = Math.abs(x);
        y = Math.abs(y);
        if (x < ((float) this.mSlop) && y < ((float) this.mSlop)) {
            return null;
        }
        if (x > y && layoutManager.canScrollHorizontally()) {
            return null;
        }
        if (y > x && layoutManager.canScrollVertically()) {
            return null;
        }
        View findChildView = findChildView(motionEvent);
        if (findChildView == null) {
            return null;
        }
        return this.mRecyclerView.getChildViewHolder(findChildView);
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Missing block: B:46:0x0094, code skipped:
            return;
     */
    public void checkSelectForSwipe(int r8, android.view.MotionEvent r9, int r10) {
        /*
        r7 = this;
        r0 = r7.mSelected;
        if (r0 != 0) goto L_0x0094;
    L_0x0004:
        r0 = 2;
        if (r8 != r0) goto L_0x0094;
    L_0x0007:
        r8 = r7.mActionState;
        if (r8 == r0) goto L_0x0094;
    L_0x000b:
        r8 = r7.mCallback;
        r8 = r8.isItemViewSwipeEnabled();
        if (r8 != 0) goto L_0x0015;
    L_0x0013:
        goto L_0x0094;
    L_0x0015:
        r8 = r7.mRecyclerView;
        r8 = r8.getScrollState();
        r1 = 1;
        if (r8 != r1) goto L_0x001f;
    L_0x001e:
        return;
    L_0x001f:
        r8 = r7.findSwipedView(r9);
        if (r8 != 0) goto L_0x0026;
    L_0x0025:
        return;
    L_0x0026:
        r2 = r7.mCallback;
        r3 = r7.mRecyclerView;
        r2 = r2.getAbsoluteMovementFlags(r3, r8);
        r3 = 65280; // 0xff00 float:9.1477E-41 double:3.22526E-319;
        r2 = r2 & r3;
        r2 = r2 >> 8;
        if (r2 != 0) goto L_0x0037;
    L_0x0036:
        return;
    L_0x0037:
        r3 = r9.getX(r10);
        r10 = r9.getY(r10);
        r4 = r7.mInitialTouchX;
        r3 = r3 - r4;
        r4 = r7.mInitialTouchY;
        r10 = r10 - r4;
        r4 = java.lang.Math.abs(r3);
        r5 = java.lang.Math.abs(r10);
        r6 = r7.mSlop;
        r6 = (float) r6;
        r6 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
        if (r6 >= 0) goto L_0x005c;
    L_0x0054:
        r6 = r7.mSlop;
        r6 = (float) r6;
        r6 = (r5 > r6 ? 1 : (r5 == r6 ? 0 : -1));
        if (r6 >= 0) goto L_0x005c;
    L_0x005b:
        return;
    L_0x005c:
        r6 = 0;
        r4 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1));
        if (r4 <= 0) goto L_0x0073;
    L_0x0061:
        r10 = (r3 > r6 ? 1 : (r3 == r6 ? 0 : -1));
        if (r10 >= 0) goto L_0x006a;
    L_0x0065:
        r10 = r2 & 4;
        if (r10 != 0) goto L_0x006a;
    L_0x0069:
        return;
    L_0x006a:
        r10 = (r3 > r6 ? 1 : (r3 == r6 ? 0 : -1));
        if (r10 <= 0) goto L_0x0085;
    L_0x006e:
        r10 = r2 & 8;
        if (r10 != 0) goto L_0x0085;
    L_0x0072:
        return;
    L_0x0073:
        r3 = (r10 > r6 ? 1 : (r10 == r6 ? 0 : -1));
        if (r3 >= 0) goto L_0x007c;
    L_0x0077:
        r3 = r2 & 1;
        if (r3 != 0) goto L_0x007c;
    L_0x007b:
        return;
    L_0x007c:
        r10 = (r10 > r6 ? 1 : (r10 == r6 ? 0 : -1));
        if (r10 <= 0) goto L_0x0085;
    L_0x0080:
        r10 = r2 & 2;
        if (r10 != 0) goto L_0x0085;
    L_0x0084:
        return;
    L_0x0085:
        r7.mDy = r6;
        r7.mDx = r6;
        r10 = 0;
        r9 = r9.getPointerId(r10);
        r7.mActivePointerId = r9;
        r7.select(r8, r1);
        return;
    L_0x0094:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.widget.helper.ItemTouchHelper.checkSelectForSwipe(int, android.view.MotionEvent, int):void");
    }

    /* Access modifiers changed, original: 0000 */
    public View findChildView(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        if (this.mSelected != null) {
            View view = this.mSelected.itemView;
            if (hitTest(view, x, y, this.mSelectedStartX + this.mDx, this.mSelectedStartY + this.mDy)) {
                return view;
            }
        }
        for (int size = this.mRecoverAnimations.size() - 1; size >= 0; size--) {
            RecoverAnimation recoverAnimation = (RecoverAnimation) this.mRecoverAnimations.get(size);
            View view2 = recoverAnimation.mViewHolder.itemView;
            if (hitTest(view2, x, y, recoverAnimation.mX, recoverAnimation.mY)) {
                return view2;
            }
        }
        return this.mRecyclerView.findChildViewUnder(x, y);
    }

    public void startDrag(@NonNull ViewHolder viewHolder) {
        if (!this.mCallback.hasDragFlag(this.mRecyclerView, viewHolder)) {
            Log.e(TAG, "Start drag has been called but dragging is not enabled");
        } else if (viewHolder.itemView.getParent() != this.mRecyclerView) {
            Log.e(TAG, "Start drag has been called with a view holder which is not a child of the RecyclerView which is controlled by this ItemTouchHelper.");
        } else {
            obtainVelocityTracker();
            this.mDy = 0.0f;
            this.mDx = 0.0f;
            select(viewHolder, 2);
        }
    }

    public void startSwipe(@NonNull ViewHolder viewHolder) {
        if (!this.mCallback.hasSwipeFlag(this.mRecyclerView, viewHolder)) {
            Log.e(TAG, "Start swipe has been called but swiping is not enabled");
        } else if (viewHolder.itemView.getParent() != this.mRecyclerView) {
            Log.e(TAG, "Start swipe has been called with a view holder which is not a child of the RecyclerView controlled by this ItemTouchHelper.");
        } else {
            obtainVelocityTracker();
            this.mDy = 0.0f;
            this.mDx = 0.0f;
            select(viewHolder, 1);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public RecoverAnimation findAnimation(MotionEvent motionEvent) {
        if (this.mRecoverAnimations.isEmpty()) {
            return null;
        }
        View findChildView = findChildView(motionEvent);
        for (int size = this.mRecoverAnimations.size() - 1; size >= 0; size--) {
            RecoverAnimation recoverAnimation = (RecoverAnimation) this.mRecoverAnimations.get(size);
            if (recoverAnimation.mViewHolder.itemView == findChildView) {
                return recoverAnimation;
            }
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    public void updateDxDy(MotionEvent motionEvent, int i, int i2) {
        float x = motionEvent.getX(i2);
        float y = motionEvent.getY(i2);
        this.mDx = x - this.mInitialTouchX;
        this.mDy = y - this.mInitialTouchY;
        if ((i & 4) == 0) {
            this.mDx = Math.max(0.0f, this.mDx);
        }
        if ((i & 8) == 0) {
            this.mDx = Math.min(0.0f, this.mDx);
        }
        if ((i & 1) == 0) {
            this.mDy = Math.max(0.0f, this.mDy);
        }
        if ((i & 2) == 0) {
            this.mDy = Math.min(0.0f, this.mDy);
        }
    }

    private int swipeIfNecessary(ViewHolder viewHolder) {
        if (this.mActionState == 2) {
            return 0;
        }
        int movementFlags = this.mCallback.getMovementFlags(this.mRecyclerView, viewHolder);
        int convertToAbsoluteDirection = (this.mCallback.convertToAbsoluteDirection(movementFlags, ViewCompat.getLayoutDirection(this.mRecyclerView)) & 65280) >> 8;
        if (convertToAbsoluteDirection == 0) {
            return 0;
        }
        movementFlags = (movementFlags & 65280) >> 8;
        int checkHorizontalSwipe;
        int checkVerticalSwipe;
        if (Math.abs(this.mDx) > Math.abs(this.mDy)) {
            checkHorizontalSwipe = checkHorizontalSwipe(viewHolder, convertToAbsoluteDirection);
            if (checkHorizontalSwipe > 0) {
                return (movementFlags & checkHorizontalSwipe) == 0 ? Callback.convertToRelativeDirection(checkHorizontalSwipe, ViewCompat.getLayoutDirection(this.mRecyclerView)) : checkHorizontalSwipe;
            } else {
                checkVerticalSwipe = checkVerticalSwipe(viewHolder, convertToAbsoluteDirection);
                if (checkVerticalSwipe > 0) {
                    return checkVerticalSwipe;
                }
            }
        }
        checkHorizontalSwipe = checkVerticalSwipe(viewHolder, convertToAbsoluteDirection);
        if (checkHorizontalSwipe > 0) {
            return checkHorizontalSwipe;
        }
        checkVerticalSwipe = checkHorizontalSwipe(viewHolder, convertToAbsoluteDirection);
        if (checkVerticalSwipe > 0) {
            return (movementFlags & checkVerticalSwipe) == 0 ? Callback.convertToRelativeDirection(checkVerticalSwipe, ViewCompat.getLayoutDirection(this.mRecyclerView)) : checkVerticalSwipe;
        }
        return 0;
    }

    private int checkHorizontalSwipe(ViewHolder viewHolder, int i) {
        if ((i & 12) != 0) {
            int i2 = 4;
            int i3 = this.mDx > 0.0f ? 8 : 4;
            if (this.mVelocityTracker != null && this.mActivePointerId > -1) {
                this.mVelocityTracker.computeCurrentVelocity(1000, this.mCallback.getSwipeVelocityThreshold(this.mMaxSwipeVelocity));
                float xVelocity = this.mVelocityTracker.getXVelocity(this.mActivePointerId);
                float yVelocity = this.mVelocityTracker.getYVelocity(this.mActivePointerId);
                if (xVelocity > 0.0f) {
                    i2 = 8;
                }
                float abs = Math.abs(xVelocity);
                if ((i2 & i) != 0 && i3 == i2 && abs >= this.mCallback.getSwipeEscapeVelocity(this.mSwipeEscapeVelocity) && abs > Math.abs(yVelocity)) {
                    return i2;
                }
            }
            float width = ((float) this.mRecyclerView.getWidth()) * this.mCallback.getSwipeThreshold(viewHolder);
            if ((i & i3) != 0 && Math.abs(this.mDx) > width) {
                return i3;
            }
        }
        return 0;
    }

    private int checkVerticalSwipe(ViewHolder viewHolder, int i) {
        if ((i & 3) != 0) {
            int i2 = 1;
            int i3 = this.mDy > 0.0f ? 2 : 1;
            if (this.mVelocityTracker != null && this.mActivePointerId > -1) {
                this.mVelocityTracker.computeCurrentVelocity(1000, this.mCallback.getSwipeVelocityThreshold(this.mMaxSwipeVelocity));
                float xVelocity = this.mVelocityTracker.getXVelocity(this.mActivePointerId);
                float yVelocity = this.mVelocityTracker.getYVelocity(this.mActivePointerId);
                if (yVelocity > 0.0f) {
                    i2 = 2;
                }
                float abs = Math.abs(yVelocity);
                if ((i2 & i) != 0 && i2 == i3 && abs >= this.mCallback.getSwipeEscapeVelocity(this.mSwipeEscapeVelocity) && abs > Math.abs(xVelocity)) {
                    return i2;
                }
            }
            float height = ((float) this.mRecyclerView.getHeight()) * this.mCallback.getSwipeThreshold(viewHolder);
            if ((i & i3) != 0 && Math.abs(this.mDy) > height) {
                return i3;
            }
        }
        return 0;
    }

    private void addChildDrawingOrderCallback() {
        if (VERSION.SDK_INT < 21) {
            if (this.mChildDrawingOrderCallback == null) {
                this.mChildDrawingOrderCallback = new ChildDrawingOrderCallback() {
                    public int onGetChildDrawingOrder(int i, int i2) {
                        if (ItemTouchHelper.this.mOverdrawChild == null) {
                            return i2;
                        }
                        int i3 = ItemTouchHelper.this.mOverdrawChildPosition;
                        if (i3 == -1) {
                            i3 = ItemTouchHelper.this.mRecyclerView.indexOfChild(ItemTouchHelper.this.mOverdrawChild);
                            ItemTouchHelper.this.mOverdrawChildPosition = i3;
                        }
                        if (i2 == i - 1) {
                            return i3;
                        }
                        if (i2 >= i3) {
                            i2++;
                        }
                        return i2;
                    }
                };
            }
            this.mRecyclerView.setChildDrawingOrderCallback(this.mChildDrawingOrderCallback);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void removeChildDrawingOrderCallbackIfNecessary(View view) {
        if (view == this.mOverdrawChild) {
            this.mOverdrawChild = null;
            if (this.mChildDrawingOrderCallback != null) {
                this.mRecyclerView.setChildDrawingOrderCallback(null);
            }
        }
    }
}
