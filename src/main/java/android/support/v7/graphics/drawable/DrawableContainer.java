package android.support.v7.graphics.drawable;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.SparseArray;

@RestrictTo({Scope.LIBRARY_GROUP})
class DrawableContainer extends Drawable implements Callback {
    private static final boolean DEBUG = false;
    private static final boolean DEFAULT_DITHER = true;
    private static final String TAG = "DrawableContainer";
    private int mAlpha = 255;
    private Runnable mAnimationRunnable;
    private BlockInvalidateCallback mBlockInvalidateCallback;
    private int mCurIndex = -1;
    private Drawable mCurrDrawable;
    private DrawableContainerState mDrawableContainerState;
    private long mEnterAnimationEnd;
    private long mExitAnimationEnd;
    private boolean mHasAlpha;
    private Rect mHotspotBounds;
    private Drawable mLastDrawable;
    private int mLastIndex = -1;
    private boolean mMutated;

    static class BlockInvalidateCallback implements Callback {
        private Callback mCallback;

        public void invalidateDrawable(@NonNull Drawable drawable) {
        }

        BlockInvalidateCallback() {
        }

        public BlockInvalidateCallback wrap(Callback callback) {
            this.mCallback = callback;
            return this;
        }

        public Callback unwrap() {
            Callback callback = this.mCallback;
            this.mCallback = null;
            return callback;
        }

        public void scheduleDrawable(@NonNull Drawable drawable, @NonNull Runnable runnable, long j) {
            if (this.mCallback != null) {
                this.mCallback.scheduleDrawable(drawable, runnable, j);
            }
        }

        public void unscheduleDrawable(@NonNull Drawable drawable, @NonNull Runnable runnable) {
            if (this.mCallback != null) {
                this.mCallback.unscheduleDrawable(drawable, runnable);
            }
        }
    }

    static abstract class DrawableContainerState extends ConstantState {
        boolean mAutoMirrored;
        boolean mCanConstantState;
        int mChangingConfigurations;
        boolean mCheckedConstantSize;
        boolean mCheckedConstantState;
        boolean mCheckedOpacity;
        boolean mCheckedPadding;
        boolean mCheckedStateful;
        int mChildrenChangingConfigurations;
        ColorFilter mColorFilter;
        int mConstantHeight;
        int mConstantMinimumHeight;
        int mConstantMinimumWidth;
        Rect mConstantPadding;
        boolean mConstantSize;
        int mConstantWidth;
        int mDensity = 160;
        boolean mDither;
        SparseArray<ConstantState> mDrawableFutures;
        Drawable[] mDrawables;
        int mEnterFadeDuration;
        int mExitFadeDuration;
        boolean mHasColorFilter;
        boolean mHasTintList;
        boolean mHasTintMode;
        int mLayoutDirection;
        boolean mMutated;
        int mNumChildren;
        int mOpacity;
        final DrawableContainer mOwner;
        Resources mSourceRes;
        boolean mStateful;
        ColorStateList mTintList;
        Mode mTintMode;
        boolean mVariablePadding;

        DrawableContainerState(DrawableContainerState drawableContainerState, DrawableContainer drawableContainer, Resources resources) {
            int i = 0;
            this.mVariablePadding = false;
            this.mConstantSize = false;
            this.mDither = true;
            this.mEnterFadeDuration = 0;
            this.mExitFadeDuration = 0;
            this.mOwner = drawableContainer;
            Resources resources2 = resources != null ? resources : drawableContainerState != null ? drawableContainerState.mSourceRes : null;
            this.mSourceRes = resources2;
            this.mDensity = DrawableContainer.resolveDensity(resources, drawableContainerState != null ? drawableContainerState.mDensity : 0);
            if (drawableContainerState != null) {
                this.mChangingConfigurations = drawableContainerState.mChangingConfigurations;
                this.mChildrenChangingConfigurations = drawableContainerState.mChildrenChangingConfigurations;
                this.mCheckedConstantState = true;
                this.mCanConstantState = true;
                this.mVariablePadding = drawableContainerState.mVariablePadding;
                this.mConstantSize = drawableContainerState.mConstantSize;
                this.mDither = drawableContainerState.mDither;
                this.mMutated = drawableContainerState.mMutated;
                this.mLayoutDirection = drawableContainerState.mLayoutDirection;
                this.mEnterFadeDuration = drawableContainerState.mEnterFadeDuration;
                this.mExitFadeDuration = drawableContainerState.mExitFadeDuration;
                this.mAutoMirrored = drawableContainerState.mAutoMirrored;
                this.mColorFilter = drawableContainerState.mColorFilter;
                this.mHasColorFilter = drawableContainerState.mHasColorFilter;
                this.mTintList = drawableContainerState.mTintList;
                this.mTintMode = drawableContainerState.mTintMode;
                this.mHasTintList = drawableContainerState.mHasTintList;
                this.mHasTintMode = drawableContainerState.mHasTintMode;
                if (drawableContainerState.mDensity == this.mDensity) {
                    if (drawableContainerState.mCheckedPadding) {
                        this.mConstantPadding = new Rect(drawableContainerState.mConstantPadding);
                        this.mCheckedPadding = true;
                    }
                    if (drawableContainerState.mCheckedConstantSize) {
                        this.mConstantWidth = drawableContainerState.mConstantWidth;
                        this.mConstantHeight = drawableContainerState.mConstantHeight;
                        this.mConstantMinimumWidth = drawableContainerState.mConstantMinimumWidth;
                        this.mConstantMinimumHeight = drawableContainerState.mConstantMinimumHeight;
                        this.mCheckedConstantSize = true;
                    }
                }
                if (drawableContainerState.mCheckedOpacity) {
                    this.mOpacity = drawableContainerState.mOpacity;
                    this.mCheckedOpacity = true;
                }
                if (drawableContainerState.mCheckedStateful) {
                    this.mStateful = drawableContainerState.mStateful;
                    this.mCheckedStateful = true;
                }
                Drawable[] drawableArr = drawableContainerState.mDrawables;
                this.mDrawables = new Drawable[drawableArr.length];
                this.mNumChildren = drawableContainerState.mNumChildren;
                SparseArray sparseArray = drawableContainerState.mDrawableFutures;
                if (sparseArray != null) {
                    this.mDrawableFutures = sparseArray.clone();
                } else {
                    this.mDrawableFutures = new SparseArray(this.mNumChildren);
                }
                int i2 = this.mNumChildren;
                while (i < i2) {
                    if (drawableArr[i] != null) {
                        ConstantState constantState = drawableArr[i].getConstantState();
                        if (constantState != null) {
                            this.mDrawableFutures.put(i, constantState);
                        } else {
                            this.mDrawables[i] = drawableArr[i];
                        }
                    }
                    i++;
                }
                return;
            }
            this.mDrawables = new Drawable[10];
            this.mNumChildren = 0;
        }

        public int getChangingConfigurations() {
            return this.mChangingConfigurations | this.mChildrenChangingConfigurations;
        }

        public final int addChild(Drawable drawable) {
            int i = this.mNumChildren;
            if (i >= this.mDrawables.length) {
                growArray(i, i + 10);
            }
            drawable.mutate();
            drawable.setVisible(false, true);
            drawable.setCallback(this.mOwner);
            this.mDrawables[i] = drawable;
            this.mNumChildren++;
            this.mChildrenChangingConfigurations = drawable.getChangingConfigurations() | this.mChildrenChangingConfigurations;
            invalidateCache();
            this.mConstantPadding = null;
            this.mCheckedPadding = false;
            this.mCheckedConstantSize = false;
            this.mCheckedConstantState = false;
            return i;
        }

        /* Access modifiers changed, original: 0000 */
        public void invalidateCache() {
            this.mCheckedOpacity = false;
            this.mCheckedStateful = false;
        }

        /* Access modifiers changed, original: final */
        public final int getCapacity() {
            return this.mDrawables.length;
        }

        private void createAllFutures() {
            if (this.mDrawableFutures != null) {
                int size = this.mDrawableFutures.size();
                for (int i = 0; i < size; i++) {
                    this.mDrawables[this.mDrawableFutures.keyAt(i)] = prepareDrawable(((ConstantState) this.mDrawableFutures.valueAt(i)).newDrawable(this.mSourceRes));
                }
                this.mDrawableFutures = null;
            }
        }

        private Drawable prepareDrawable(Drawable drawable) {
            if (VERSION.SDK_INT >= 23) {
                drawable.setLayoutDirection(this.mLayoutDirection);
            }
            drawable = drawable.mutate();
            drawable.setCallback(this.mOwner);
            return drawable;
        }

        public final int getChildCount() {
            return this.mNumChildren;
        }

        public final Drawable getChild(int i) {
            Drawable drawable = this.mDrawables[i];
            if (drawable != null) {
                return drawable;
            }
            if (this.mDrawableFutures != null) {
                int indexOfKey = this.mDrawableFutures.indexOfKey(i);
                if (indexOfKey >= 0) {
                    Drawable prepareDrawable = prepareDrawable(((ConstantState) this.mDrawableFutures.valueAt(indexOfKey)).newDrawable(this.mSourceRes));
                    this.mDrawables[i] = prepareDrawable;
                    this.mDrawableFutures.removeAt(indexOfKey);
                    if (this.mDrawableFutures.size() == 0) {
                        this.mDrawableFutures = null;
                    }
                    return prepareDrawable;
                }
            }
            return null;
        }

        /* Access modifiers changed, original: final */
        public final boolean setLayoutDirection(int i, int i2) {
            int i3 = this.mNumChildren;
            Drawable[] drawableArr = this.mDrawables;
            boolean z = false;
            for (int i4 = 0; i4 < i3; i4++) {
                if (drawableArr[i4] != null) {
                    boolean layoutDirection = VERSION.SDK_INT >= 23 ? drawableArr[i4].setLayoutDirection(i) : false;
                    if (i4 == i2) {
                        z = layoutDirection;
                    }
                }
            }
            this.mLayoutDirection = i;
            return z;
        }

        /* Access modifiers changed, original: final */
        public final void updateDensity(Resources resources) {
            if (resources != null) {
                this.mSourceRes = resources;
                int resolveDensity = DrawableContainer.resolveDensity(resources, this.mDensity);
                int i = this.mDensity;
                this.mDensity = resolveDensity;
                if (i != resolveDensity) {
                    this.mCheckedConstantSize = false;
                    this.mCheckedPadding = false;
                }
            }
        }

        /* Access modifiers changed, original: final */
        @RequiresApi(21)
        public final void applyTheme(Theme theme) {
            if (theme != null) {
                createAllFutures();
                int i = this.mNumChildren;
                Drawable[] drawableArr = this.mDrawables;
                int i2 = 0;
                while (i2 < i) {
                    if (drawableArr[i2] != null && drawableArr[i2].canApplyTheme()) {
                        drawableArr[i2].applyTheme(theme);
                        this.mChildrenChangingConfigurations |= drawableArr[i2].getChangingConfigurations();
                    }
                    i2++;
                }
                updateDensity(theme.getResources());
            }
        }

        @RequiresApi(21)
        public boolean canApplyTheme() {
            int i = this.mNumChildren;
            Drawable[] drawableArr = this.mDrawables;
            for (int i2 = 0; i2 < i; i2++) {
                Drawable drawable = drawableArr[i2];
                if (drawable == null) {
                    ConstantState constantState = (ConstantState) this.mDrawableFutures.get(i2);
                    if (constantState != null && constantState.canApplyTheme()) {
                        return true;
                    }
                } else if (drawable.canApplyTheme()) {
                    return true;
                }
            }
            return false;
        }

        /* Access modifiers changed, original: 0000 */
        public void mutate() {
            int i = this.mNumChildren;
            Drawable[] drawableArr = this.mDrawables;
            for (int i2 = 0; i2 < i; i2++) {
                if (drawableArr[i2] != null) {
                    drawableArr[i2].mutate();
                }
            }
            this.mMutated = true;
        }

        /* Access modifiers changed, original: final */
        public final void clearMutated() {
            this.mMutated = false;
        }

        public final void setVariablePadding(boolean z) {
            this.mVariablePadding = z;
        }

        public final Rect getConstantPadding() {
            if (this.mVariablePadding) {
                return null;
            }
            if (this.mConstantPadding != null || this.mCheckedPadding) {
                return this.mConstantPadding;
            }
            createAllFutures();
            Rect rect = new Rect();
            int i = this.mNumChildren;
            Drawable[] drawableArr = this.mDrawables;
            Rect rect2 = null;
            for (int i2 = 0; i2 < i; i2++) {
                if (drawableArr[i2].getPadding(rect)) {
                    if (rect2 == null) {
                        rect2 = new Rect(0, 0, 0, 0);
                    }
                    if (rect.left > rect2.left) {
                        rect2.left = rect.left;
                    }
                    if (rect.top > rect2.top) {
                        rect2.top = rect.top;
                    }
                    if (rect.right > rect2.right) {
                        rect2.right = rect.right;
                    }
                    if (rect.bottom > rect2.bottom) {
                        rect2.bottom = rect.bottom;
                    }
                }
            }
            this.mCheckedPadding = true;
            this.mConstantPadding = rect2;
            return rect2;
        }

        public final void setConstantSize(boolean z) {
            this.mConstantSize = z;
        }

        public final boolean isConstantSize() {
            return this.mConstantSize;
        }

        public final int getConstantWidth() {
            if (!this.mCheckedConstantSize) {
                computeConstantSize();
            }
            return this.mConstantWidth;
        }

        public final int getConstantHeight() {
            if (!this.mCheckedConstantSize) {
                computeConstantSize();
            }
            return this.mConstantHeight;
        }

        public final int getConstantMinimumWidth() {
            if (!this.mCheckedConstantSize) {
                computeConstantSize();
            }
            return this.mConstantMinimumWidth;
        }

        public final int getConstantMinimumHeight() {
            if (!this.mCheckedConstantSize) {
                computeConstantSize();
            }
            return this.mConstantMinimumHeight;
        }

        /* Access modifiers changed, original: protected */
        public void computeConstantSize() {
            this.mCheckedConstantSize = true;
            createAllFutures();
            int i = this.mNumChildren;
            Drawable[] drawableArr = this.mDrawables;
            this.mConstantHeight = -1;
            this.mConstantWidth = -1;
            int i2 = 0;
            this.mConstantMinimumHeight = 0;
            this.mConstantMinimumWidth = 0;
            while (i2 < i) {
                Drawable drawable = drawableArr[i2];
                int intrinsicWidth = drawable.getIntrinsicWidth();
                if (intrinsicWidth > this.mConstantWidth) {
                    this.mConstantWidth = intrinsicWidth;
                }
                intrinsicWidth = drawable.getIntrinsicHeight();
                if (intrinsicWidth > this.mConstantHeight) {
                    this.mConstantHeight = intrinsicWidth;
                }
                intrinsicWidth = drawable.getMinimumWidth();
                if (intrinsicWidth > this.mConstantMinimumWidth) {
                    this.mConstantMinimumWidth = intrinsicWidth;
                }
                int minimumHeight = drawable.getMinimumHeight();
                if (minimumHeight > this.mConstantMinimumHeight) {
                    this.mConstantMinimumHeight = minimumHeight;
                }
                i2++;
            }
        }

        public final void setEnterFadeDuration(int i) {
            this.mEnterFadeDuration = i;
        }

        public final int getEnterFadeDuration() {
            return this.mEnterFadeDuration;
        }

        public final void setExitFadeDuration(int i) {
            this.mExitFadeDuration = i;
        }

        public final int getExitFadeDuration() {
            return this.mExitFadeDuration;
        }

        public final int getOpacity() {
            if (this.mCheckedOpacity) {
                return this.mOpacity;
            }
            createAllFutures();
            int i = this.mNumChildren;
            Drawable[] drawableArr = this.mDrawables;
            int opacity = i > 0 ? drawableArr[0].getOpacity() : -2;
            for (int i2 = 1; i2 < i; i2++) {
                opacity = Drawable.resolveOpacity(opacity, drawableArr[i2].getOpacity());
            }
            this.mOpacity = opacity;
            this.mCheckedOpacity = true;
            return opacity;
        }

        public final boolean isStateful() {
            if (this.mCheckedStateful) {
                return this.mStateful;
            }
            createAllFutures();
            int i = this.mNumChildren;
            Drawable[] drawableArr = this.mDrawables;
            boolean z = false;
            for (int i2 = 0; i2 < i; i2++) {
                if (drawableArr[i2].isStateful()) {
                    z = true;
                    break;
                }
            }
            this.mStateful = z;
            this.mCheckedStateful = true;
            return z;
        }

        public void growArray(int i, int i2) {
            Drawable[] drawableArr = new Drawable[i2];
            System.arraycopy(this.mDrawables, 0, drawableArr, 0, i);
            this.mDrawables = drawableArr;
        }

        public synchronized boolean canConstantState() {
            if (this.mCheckedConstantState) {
                return this.mCanConstantState;
            }
            createAllFutures();
            this.mCheckedConstantState = true;
            int i = this.mNumChildren;
            Drawable[] drawableArr = this.mDrawables;
            for (int i2 = 0; i2 < i; i2++) {
                if (drawableArr[i2].getConstantState() == null) {
                    this.mCanConstantState = false;
                    return false;
                }
            }
            this.mCanConstantState = true;
            return true;
        }
    }

    DrawableContainer() {
    }

    public void draw(@NonNull Canvas canvas) {
        if (this.mCurrDrawable != null) {
            this.mCurrDrawable.draw(canvas);
        }
        if (this.mLastDrawable != null) {
            this.mLastDrawable.draw(canvas);
        }
    }

    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mDrawableContainerState.getChangingConfigurations();
    }

    @SuppressLint({"WrongConstant"})
    @TargetApi(23)
    private boolean needsMirroring() {
        return isAutoMirrored() && getLayoutDirection() == 1;
    }

    public boolean getPadding(@NonNull Rect rect) {
        boolean z;
        Rect constantPadding = this.mDrawableContainerState.getConstantPadding();
        if (constantPadding != null) {
            rect.set(constantPadding);
            z = (constantPadding.right | ((constantPadding.left | constantPadding.top) | constantPadding.bottom)) != 0;
        } else {
            z = this.mCurrDrawable != null ? this.mCurrDrawable.getPadding(rect) : super.getPadding(rect);
        }
        if (needsMirroring()) {
            int i = rect.left;
            rect.left = rect.right;
            rect.right = i;
        }
        return z;
    }

    @RequiresApi(21)
    public void getOutline(@NonNull Outline outline) {
        if (this.mCurrDrawable != null) {
            this.mCurrDrawable.getOutline(outline);
        }
    }

    public void setAlpha(int i) {
        if (!this.mHasAlpha || this.mAlpha != i) {
            this.mHasAlpha = true;
            this.mAlpha = i;
            if (this.mCurrDrawable == null) {
                return;
            }
            if (this.mEnterAnimationEnd == 0) {
                this.mCurrDrawable.setAlpha(i);
            } else {
                animate(false);
            }
        }
    }

    public int getAlpha() {
        return this.mAlpha;
    }

    public void setDither(boolean z) {
        if (this.mDrawableContainerState.mDither != z) {
            this.mDrawableContainerState.mDither = z;
            if (this.mCurrDrawable != null) {
                this.mCurrDrawable.setDither(this.mDrawableContainerState.mDither);
            }
        }
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mDrawableContainerState.mHasColorFilter = true;
        if (this.mDrawableContainerState.mColorFilter != colorFilter) {
            this.mDrawableContainerState.mColorFilter = colorFilter;
            if (this.mCurrDrawable != null) {
                this.mCurrDrawable.setColorFilter(colorFilter);
            }
        }
    }

    public void setTintList(ColorStateList colorStateList) {
        this.mDrawableContainerState.mHasTintList = true;
        if (this.mDrawableContainerState.mTintList != colorStateList) {
            this.mDrawableContainerState.mTintList = colorStateList;
            DrawableCompat.setTintList(this.mCurrDrawable, colorStateList);
        }
    }

    public void setTintMode(@NonNull Mode mode) {
        this.mDrawableContainerState.mHasTintMode = true;
        if (this.mDrawableContainerState.mTintMode != mode) {
            this.mDrawableContainerState.mTintMode = mode;
            DrawableCompat.setTintMode(this.mCurrDrawable, mode);
        }
    }

    public void setEnterFadeDuration(int i) {
        this.mDrawableContainerState.mEnterFadeDuration = i;
    }

    public void setExitFadeDuration(int i) {
        this.mDrawableContainerState.mExitFadeDuration = i;
    }

    /* Access modifiers changed, original: protected */
    public void onBoundsChange(Rect rect) {
        if (this.mLastDrawable != null) {
            this.mLastDrawable.setBounds(rect);
        }
        if (this.mCurrDrawable != null) {
            this.mCurrDrawable.setBounds(rect);
        }
    }

    public boolean isStateful() {
        return this.mDrawableContainerState.isStateful();
    }

    public void setAutoMirrored(boolean z) {
        if (this.mDrawableContainerState.mAutoMirrored != z) {
            this.mDrawableContainerState.mAutoMirrored = z;
            if (this.mCurrDrawable != null) {
                DrawableCompat.setAutoMirrored(this.mCurrDrawable, this.mDrawableContainerState.mAutoMirrored);
            }
        }
    }

    public boolean isAutoMirrored() {
        return this.mDrawableContainerState.mAutoMirrored;
    }

    public void jumpToCurrentState() {
        Object obj;
        if (this.mLastDrawable != null) {
            this.mLastDrawable.jumpToCurrentState();
            this.mLastDrawable = null;
            this.mLastIndex = -1;
            obj = 1;
        } else {
            obj = null;
        }
        if (this.mCurrDrawable != null) {
            this.mCurrDrawable.jumpToCurrentState();
            if (this.mHasAlpha) {
                this.mCurrDrawable.setAlpha(this.mAlpha);
            }
        }
        if (this.mExitAnimationEnd != 0) {
            this.mExitAnimationEnd = 0;
            obj = 1;
        }
        if (this.mEnterAnimationEnd != 0) {
            this.mEnterAnimationEnd = 0;
            obj = 1;
        }
        if (obj != null) {
            invalidateSelf();
        }
    }

    public void setHotspot(float f, float f2) {
        if (this.mCurrDrawable != null) {
            DrawableCompat.setHotspot(this.mCurrDrawable, f, f2);
        }
    }

    public void setHotspotBounds(int i, int i2, int i3, int i4) {
        if (this.mHotspotBounds == null) {
            this.mHotspotBounds = new Rect(i, i2, i3, i4);
        } else {
            this.mHotspotBounds.set(i, i2, i3, i4);
        }
        if (this.mCurrDrawable != null) {
            DrawableCompat.setHotspotBounds(this.mCurrDrawable, i, i2, i3, i4);
        }
    }

    public void getHotspotBounds(@NonNull Rect rect) {
        if (this.mHotspotBounds != null) {
            rect.set(this.mHotspotBounds);
        } else {
            super.getHotspotBounds(rect);
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean onStateChange(int[] iArr) {
        if (this.mLastDrawable != null) {
            return this.mLastDrawable.setState(iArr);
        }
        return this.mCurrDrawable != null ? this.mCurrDrawable.setState(iArr) : false;
    }

    /* Access modifiers changed, original: protected */
    public boolean onLevelChange(int i) {
        if (this.mLastDrawable != null) {
            return this.mLastDrawable.setLevel(i);
        }
        return this.mCurrDrawable != null ? this.mCurrDrawable.setLevel(i) : false;
    }

    public boolean onLayoutDirectionChanged(int i) {
        return this.mDrawableContainerState.setLayoutDirection(i, getCurrentIndex());
    }

    public int getIntrinsicWidth() {
        if (this.mDrawableContainerState.isConstantSize()) {
            return this.mDrawableContainerState.getConstantWidth();
        }
        return this.mCurrDrawable != null ? this.mCurrDrawable.getIntrinsicWidth() : -1;
    }

    public int getIntrinsicHeight() {
        if (this.mDrawableContainerState.isConstantSize()) {
            return this.mDrawableContainerState.getConstantHeight();
        }
        return this.mCurrDrawable != null ? this.mCurrDrawable.getIntrinsicHeight() : -1;
    }

    public int getMinimumWidth() {
        if (this.mDrawableContainerState.isConstantSize()) {
            return this.mDrawableContainerState.getConstantMinimumWidth();
        }
        return this.mCurrDrawable != null ? this.mCurrDrawable.getMinimumWidth() : 0;
    }

    public int getMinimumHeight() {
        if (this.mDrawableContainerState.isConstantSize()) {
            return this.mDrawableContainerState.getConstantMinimumHeight();
        }
        return this.mCurrDrawable != null ? this.mCurrDrawable.getMinimumHeight() : 0;
    }

    public void invalidateDrawable(@NonNull Drawable drawable) {
        if (this.mDrawableContainerState != null) {
            this.mDrawableContainerState.invalidateCache();
        }
        if (drawable == this.mCurrDrawable && getCallback() != null) {
            getCallback().invalidateDrawable(this);
        }
    }

    public void scheduleDrawable(@NonNull Drawable drawable, @NonNull Runnable runnable, long j) {
        if (drawable == this.mCurrDrawable && getCallback() != null) {
            getCallback().scheduleDrawable(this, runnable, j);
        }
    }

    public void unscheduleDrawable(@NonNull Drawable drawable, @NonNull Runnable runnable) {
        if (drawable == this.mCurrDrawable && getCallback() != null) {
            getCallback().unscheduleDrawable(this, runnable);
        }
    }

    public boolean setVisible(boolean z, boolean z2) {
        boolean visible = super.setVisible(z, z2);
        if (this.mLastDrawable != null) {
            this.mLastDrawable.setVisible(z, z2);
        }
        if (this.mCurrDrawable != null) {
            this.mCurrDrawable.setVisible(z, z2);
        }
        return visible;
    }

    public int getOpacity() {
        return (this.mCurrDrawable == null || !this.mCurrDrawable.isVisible()) ? -2 : this.mDrawableContainerState.getOpacity();
    }

    /* Access modifiers changed, original: 0000 */
    public void setCurrentIndex(int i) {
        selectDrawable(i);
    }

    /* Access modifiers changed, original: 0000 */
    public int getCurrentIndex() {
        return this.mCurIndex;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean selectDrawable(int i) {
        if (i == this.mCurIndex) {
            return false;
        }
        long uptimeMillis = SystemClock.uptimeMillis();
        if (this.mDrawableContainerState.mExitFadeDuration > 0) {
            if (this.mLastDrawable != null) {
                this.mLastDrawable.setVisible(false, false);
            }
            if (this.mCurrDrawable != null) {
                this.mLastDrawable = this.mCurrDrawable;
                this.mLastIndex = this.mCurIndex;
                this.mExitAnimationEnd = ((long) this.mDrawableContainerState.mExitFadeDuration) + uptimeMillis;
            } else {
                this.mLastDrawable = null;
                this.mLastIndex = -1;
                this.mExitAnimationEnd = 0;
            }
        } else if (this.mCurrDrawable != null) {
            this.mCurrDrawable.setVisible(false, false);
        }
        if (i < 0 || i >= this.mDrawableContainerState.mNumChildren) {
            this.mCurrDrawable = null;
            this.mCurIndex = -1;
        } else {
            Drawable child = this.mDrawableContainerState.getChild(i);
            this.mCurrDrawable = child;
            this.mCurIndex = i;
            if (child != null) {
                if (this.mDrawableContainerState.mEnterFadeDuration > 0) {
                    this.mEnterAnimationEnd = uptimeMillis + ((long) this.mDrawableContainerState.mEnterFadeDuration);
                }
                initializeDrawableForDisplay(child);
            }
        }
        if (!(this.mEnterAnimationEnd == 0 && this.mExitAnimationEnd == 0)) {
            if (this.mAnimationRunnable == null) {
                this.mAnimationRunnable = new Runnable() {
                    public void run() {
                        DrawableContainer.this.animate(true);
                        DrawableContainer.this.invalidateSelf();
                    }
                };
            } else {
                unscheduleSelf(this.mAnimationRunnable);
            }
            animate(true);
        }
        invalidateSelf();
        return true;
    }

    private void initializeDrawableForDisplay(Drawable drawable) {
        if (this.mBlockInvalidateCallback == null) {
            this.mBlockInvalidateCallback = new BlockInvalidateCallback();
        }
        drawable.setCallback(this.mBlockInvalidateCallback.wrap(drawable.getCallback()));
        try {
            if (this.mDrawableContainerState.mEnterFadeDuration <= 0 && this.mHasAlpha) {
                drawable.setAlpha(this.mAlpha);
            }
            if (this.mDrawableContainerState.mHasColorFilter) {
                drawable.setColorFilter(this.mDrawableContainerState.mColorFilter);
            } else {
                if (this.mDrawableContainerState.mHasTintList) {
                    DrawableCompat.setTintList(drawable, this.mDrawableContainerState.mTintList);
                }
                if (this.mDrawableContainerState.mHasTintMode) {
                    DrawableCompat.setTintMode(drawable, this.mDrawableContainerState.mTintMode);
                }
            }
            drawable.setVisible(isVisible(), true);
            drawable.setDither(this.mDrawableContainerState.mDither);
            drawable.setState(getState());
            drawable.setLevel(getLevel());
            drawable.setBounds(getBounds());
            if (VERSION.SDK_INT >= 23) {
                drawable.setLayoutDirection(getLayoutDirection());
            }
            if (VERSION.SDK_INT >= 19) {
                drawable.setAutoMirrored(this.mDrawableContainerState.mAutoMirrored);
            }
            Rect rect = this.mHotspotBounds;
            if (VERSION.SDK_INT >= 21 && rect != null) {
                drawable.setHotspotBounds(rect.left, rect.top, rect.right, rect.bottom);
            }
            drawable.setCallback(this.mBlockInvalidateCallback.unwrap());
        } catch (Throwable th) {
            drawable.setCallback(this.mBlockInvalidateCallback.unwrap());
        }
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:24:? A:{SYNTHETIC, RETURN, SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x007d A:{SKIP} */
    public void animate(boolean r13) {
        /*
        r12 = this;
        r0 = 1;
        r12.mHasAlpha = r0;
        r1 = android.os.SystemClock.uptimeMillis();
        r3 = r12.mCurrDrawable;
        r4 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r6 = 0;
        r7 = 0;
        if (r3 == 0) goto L_0x0040;
    L_0x0010:
        r9 = r12.mEnterAnimationEnd;
        r3 = (r9 > r7 ? 1 : (r9 == r7 ? 0 : -1));
        if (r3 == 0) goto L_0x0042;
    L_0x0016:
        r9 = r12.mEnterAnimationEnd;
        r3 = (r9 > r1 ? 1 : (r9 == r1 ? 0 : -1));
        if (r3 > 0) goto L_0x0026;
    L_0x001c:
        r3 = r12.mCurrDrawable;
        r9 = r12.mAlpha;
        r3.setAlpha(r9);
        r12.mEnterAnimationEnd = r7;
        goto L_0x0042;
    L_0x0026:
        r9 = r12.mEnterAnimationEnd;
        r9 = r9 - r1;
        r9 = r9 * r4;
        r3 = (int) r9;
        r9 = r12.mDrawableContainerState;
        r9 = r9.mEnterFadeDuration;
        r3 = r3 / r9;
        r9 = r12.mCurrDrawable;
        r3 = 255 - r3;
        r10 = r12.mAlpha;
        r3 = r3 * r10;
        r3 = r3 / 255;
        r9.setAlpha(r3);
        r3 = 1;
        goto L_0x0043;
    L_0x0040:
        r12.mEnterAnimationEnd = r7;
    L_0x0042:
        r3 = 0;
    L_0x0043:
        r9 = r12.mLastDrawable;
        if (r9 == 0) goto L_0x0078;
    L_0x0047:
        r9 = r12.mExitAnimationEnd;
        r11 = (r9 > r7 ? 1 : (r9 == r7 ? 0 : -1));
        if (r11 == 0) goto L_0x007a;
    L_0x004d:
        r9 = r12.mExitAnimationEnd;
        r11 = (r9 > r1 ? 1 : (r9 == r1 ? 0 : -1));
        if (r11 > 0) goto L_0x0061;
    L_0x0053:
        r0 = r12.mLastDrawable;
        r0.setVisible(r6, r6);
        r0 = 0;
        r12.mLastDrawable = r0;
        r0 = -1;
        r12.mLastIndex = r0;
        r12.mExitAnimationEnd = r7;
        goto L_0x007a;
    L_0x0061:
        r6 = r12.mExitAnimationEnd;
        r6 = r6 - r1;
        r6 = r6 * r4;
        r3 = (int) r6;
        r4 = r12.mDrawableContainerState;
        r4 = r4.mExitFadeDuration;
        r3 = r3 / r4;
        r4 = r12.mLastDrawable;
        r5 = r12.mAlpha;
        r3 = r3 * r5;
        r3 = r3 / 255;
        r4.setAlpha(r3);
        goto L_0x007b;
    L_0x0078:
        r12.mExitAnimationEnd = r7;
    L_0x007a:
        r0 = r3;
    L_0x007b:
        if (r13 == 0) goto L_0x0087;
    L_0x007d:
        if (r0 == 0) goto L_0x0087;
    L_0x007f:
        r13 = r12.mAnimationRunnable;
        r3 = 16;
        r1 = r1 + r3;
        r12.scheduleSelf(r13, r1);
    L_0x0087:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.graphics.drawable.DrawableContainer.animate(boolean):void");
    }

    @NonNull
    public Drawable getCurrent() {
        return this.mCurrDrawable;
    }

    /* Access modifiers changed, original: final */
    public final void updateDensity(Resources resources) {
        this.mDrawableContainerState.updateDensity(resources);
    }

    @RequiresApi(21)
    public void applyTheme(@NonNull Theme theme) {
        this.mDrawableContainerState.applyTheme(theme);
    }

    @RequiresApi(21)
    public boolean canApplyTheme() {
        return this.mDrawableContainerState.canApplyTheme();
    }

    public final ConstantState getConstantState() {
        if (!this.mDrawableContainerState.canConstantState()) {
            return null;
        }
        this.mDrawableContainerState.mChangingConfigurations = getChangingConfigurations();
        return this.mDrawableContainerState;
    }

    @NonNull
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            DrawableContainerState cloneConstantState = cloneConstantState();
            cloneConstantState.mutate();
            setConstantState(cloneConstantState);
            this.mMutated = true;
        }
        return this;
    }

    /* Access modifiers changed, original: 0000 */
    public DrawableContainerState cloneConstantState() {
        return this.mDrawableContainerState;
    }

    /* Access modifiers changed, original: 0000 */
    public void clearMutated() {
        this.mDrawableContainerState.clearMutated();
        this.mMutated = false;
    }

    /* Access modifiers changed, original: protected */
    public void setConstantState(DrawableContainerState drawableContainerState) {
        this.mDrawableContainerState = drawableContainerState;
        if (this.mCurIndex >= 0) {
            this.mCurrDrawable = drawableContainerState.getChild(this.mCurIndex);
            if (this.mCurrDrawable != null) {
                initializeDrawableForDisplay(this.mCurrDrawable);
            }
        }
        this.mLastIndex = -1;
        this.mLastDrawable = null;
    }

    static int resolveDensity(@Nullable Resources resources, int i) {
        if (resources != null) {
            i = resources.getDisplayMetrics().densityDpi;
        }
        return i == 0 ? 160 : i;
    }
}
