package android.support.design.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.support.annotation.BoolRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Dimension;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.StringRes;
import android.support.design.R;
import android.support.design.animation.AnimationUtils;
import android.support.design.internal.ThemeEnforcement;
import android.support.design.internal.ViewUtils;
import android.support.design.resources.MaterialResources;
import android.support.design.ripple.RippleUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pools.Pool;
import android.support.v4.util.Pools.SimplePool;
import android.support.v4.util.Pools.SynchronizedPool;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PointerIconCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.DecorView;
import android.support.v4.view.ViewPager.OnAdapterChangeListener;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.TooltipCompat;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

@DecorView
public class TabLayout extends HorizontalScrollView {
    private static final int ANIMATION_DURATION = 300;
    @Dimension(unit = 0)
    static final int DEFAULT_GAP_TEXT_ICON = 8;
    @Dimension(unit = 0)
    private static final int DEFAULT_HEIGHT = 48;
    @Dimension(unit = 0)
    private static final int DEFAULT_HEIGHT_WITH_TEXT_ICON = 72;
    @Dimension(unit = 0)
    static final int FIXED_WRAP_GUTTER_MIN = 16;
    public static final int GRAVITY_CENTER = 1;
    public static final int GRAVITY_FILL = 0;
    public static final int INDICATOR_GRAVITY_BOTTOM = 0;
    public static final int INDICATOR_GRAVITY_CENTER = 1;
    public static final int INDICATOR_GRAVITY_STRETCH = 3;
    public static final int INDICATOR_GRAVITY_TOP = 2;
    private static final int INVALID_WIDTH = -1;
    @Dimension(unit = 0)
    private static final int MIN_INDICATOR_WIDTH = 24;
    public static final int MODE_FIXED = 1;
    public static final int MODE_SCROLLABLE = 0;
    @Dimension(unit = 0)
    private static final int TAB_MIN_WIDTH_MARGIN = 56;
    private static final Pool<Tab> tabPool = new SynchronizedPool(16);
    private AdapterChangeListener adapterChangeListener;
    private int contentInsetStart;
    private BaseOnTabSelectedListener currentVpSelectedListener;
    boolean inlineLabel;
    int mode;
    private TabLayoutOnPageChangeListener pageChangeListener;
    private PagerAdapter pagerAdapter;
    private DataSetObserver pagerAdapterObserver;
    private final int requestedTabMaxWidth;
    private final int requestedTabMinWidth;
    private ValueAnimator scrollAnimator;
    private final int scrollableTabMinWidth;
    private BaseOnTabSelectedListener selectedListener;
    private final ArrayList<BaseOnTabSelectedListener> selectedListeners;
    private Tab selectedTab;
    private boolean setupViewPagerImplicitly;
    private final SlidingTabIndicator slidingTabIndicator;
    final int tabBackgroundResId;
    int tabGravity;
    ColorStateList tabIconTint;
    android.graphics.PorterDuff.Mode tabIconTintMode;
    int tabIndicatorAnimationDuration;
    boolean tabIndicatorFullWidth;
    int tabIndicatorGravity;
    int tabMaxWidth;
    int tabPaddingBottom;
    int tabPaddingEnd;
    int tabPaddingStart;
    int tabPaddingTop;
    ColorStateList tabRippleColorStateList;
    @Nullable
    Drawable tabSelectedIndicator;
    int tabTextAppearance;
    ColorStateList tabTextColors;
    float tabTextMultiLineSize;
    float tabTextSize;
    private final RectF tabViewContentBounds;
    private final Pool<TabView> tabViewPool;
    private final ArrayList<Tab> tabs;
    boolean unboundedRipple;
    ViewPager viewPager;

    public interface BaseOnTabSelectedListener<T extends Tab> {
        void onTabReselected(T t);

        void onTabSelected(T t);

        void onTabUnselected(T t);
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    private class PagerAdapterObserver extends DataSetObserver {
        PagerAdapterObserver() {
        }

        public void onChanged() {
            TabLayout.this.populateFromPagerAdapter();
        }

        public void onInvalidated() {
            TabLayout.this.populateFromPagerAdapter();
        }
    }

    private class SlidingTabIndicator extends LinearLayout {
        private final GradientDrawable defaultSelectionIndicator;
        private ValueAnimator indicatorAnimator;
        private int indicatorLeft = -1;
        private int indicatorRight = -1;
        private int layoutDirection = -1;
        private int selectedIndicatorHeight;
        private final Paint selectedIndicatorPaint;
        int selectedPosition = -1;
        float selectionOffset;

        SlidingTabIndicator(Context context) {
            super(context);
            setWillNotDraw(false);
            this.selectedIndicatorPaint = new Paint();
            this.defaultSelectionIndicator = new GradientDrawable();
        }

        /* Access modifiers changed, original: 0000 */
        public void setSelectedIndicatorColor(int i) {
            if (this.selectedIndicatorPaint.getColor() != i) {
                this.selectedIndicatorPaint.setColor(i);
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void setSelectedIndicatorHeight(int i) {
            if (this.selectedIndicatorHeight != i) {
                this.selectedIndicatorHeight = i;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public boolean childrenNeedLayout() {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (getChildAt(i).getWidth() <= 0) {
                    return true;
                }
            }
            return false;
        }

        /* Access modifiers changed, original: 0000 */
        public void setIndicatorPositionFromTabPosition(int i, float f) {
            if (this.indicatorAnimator != null && this.indicatorAnimator.isRunning()) {
                this.indicatorAnimator.cancel();
            }
            this.selectedPosition = i;
            this.selectionOffset = f;
            updateIndicatorPosition();
        }

        /* Access modifiers changed, original: 0000 */
        public float getIndicatorPosition() {
            return ((float) this.selectedPosition) + this.selectionOffset;
        }

        public void onRtlPropertiesChanged(int i) {
            super.onRtlPropertiesChanged(i);
            if (VERSION.SDK_INT < 23 && this.layoutDirection != i) {
                requestLayout();
                this.layoutDirection = i;
            }
        }

        /* Access modifiers changed, original: protected */
        public void onMeasure(int i, int i2) {
            super.onMeasure(i, i2);
            if (MeasureSpec.getMode(i) == 1073741824) {
                Object obj = 1;
                if (TabLayout.this.mode == 1 && TabLayout.this.tabGravity == 1) {
                    int childCount = getChildCount();
                    int i3 = 0;
                    int i4 = 0;
                    for (int i5 = 0; i5 < childCount; i5++) {
                        View childAt = getChildAt(i5);
                        if (childAt.getVisibility() == 0) {
                            i4 = Math.max(i4, childAt.getMeasuredWidth());
                        }
                    }
                    if (i4 > 0) {
                        if (i4 * childCount <= getMeasuredWidth() - (TabLayout.this.dpToPx(16) * 2)) {
                            Object obj2 = null;
                            while (i3 < childCount) {
                                LayoutParams layoutParams = (LayoutParams) getChildAt(i3).getLayoutParams();
                                if (layoutParams.width != i4 || layoutParams.weight != 0.0f) {
                                    layoutParams.width = i4;
                                    layoutParams.weight = 0.0f;
                                    obj2 = 1;
                                }
                                i3++;
                            }
                            obj = obj2;
                        } else {
                            TabLayout.this.tabGravity = 0;
                            TabLayout.this.updateTabViews(false);
                        }
                        if (obj != null) {
                            super.onMeasure(i, i2);
                        }
                    }
                }
            }
        }

        /* Access modifiers changed, original: protected */
        public void onLayout(boolean z, int i, int i2, int i3, int i4) {
            super.onLayout(z, i, i2, i3, i4);
            if (this.indicatorAnimator == null || !this.indicatorAnimator.isRunning()) {
                updateIndicatorPosition();
                return;
            }
            this.indicatorAnimator.cancel();
            animateIndicatorToPosition(this.selectedPosition, Math.round((1.0f - this.indicatorAnimator.getAnimatedFraction()) * ((float) this.indicatorAnimator.getDuration())));
        }

        private void updateIndicatorPosition() {
            int i;
            int i2;
            View childAt = getChildAt(this.selectedPosition);
            if (childAt == null || childAt.getWidth() <= 0) {
                i = -1;
                i2 = -1;
            } else {
                i = childAt.getLeft();
                i2 = childAt.getRight();
                if (!TabLayout.this.tabIndicatorFullWidth && (childAt instanceof TabView)) {
                    calculateTabViewContentBounds((TabView) childAt, TabLayout.this.tabViewContentBounds);
                    i = (int) TabLayout.this.tabViewContentBounds.left;
                    i2 = (int) TabLayout.this.tabViewContentBounds.right;
                }
                if (this.selectionOffset > 0.0f && this.selectedPosition < getChildCount() - 1) {
                    childAt = getChildAt(this.selectedPosition + 1);
                    int left = childAt.getLeft();
                    int right = childAt.getRight();
                    if (!TabLayout.this.tabIndicatorFullWidth && (childAt instanceof TabView)) {
                        calculateTabViewContentBounds((TabView) childAt, TabLayout.this.tabViewContentBounds);
                        left = (int) TabLayout.this.tabViewContentBounds.left;
                        right = (int) TabLayout.this.tabViewContentBounds.right;
                    }
                    i = (int) ((this.selectionOffset * ((float) left)) + ((1.0f - this.selectionOffset) * ((float) i)));
                    i2 = (int) ((this.selectionOffset * ((float) right)) + ((1.0f - this.selectionOffset) * ((float) i2)));
                }
            }
            setIndicatorPosition(i, i2);
        }

        /* Access modifiers changed, original: 0000 */
        public void setIndicatorPosition(int i, int i2) {
            if (i != this.indicatorLeft || i2 != this.indicatorRight) {
                this.indicatorLeft = i;
                this.indicatorRight = i2;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void animateIndicatorToPosition(final int i, int i2) {
            if (this.indicatorAnimator != null && this.indicatorAnimator.isRunning()) {
                this.indicatorAnimator.cancel();
            }
            View childAt = getChildAt(i);
            if (childAt == null) {
                updateIndicatorPosition();
                return;
            }
            int left = childAt.getLeft();
            int right = childAt.getRight();
            if (!TabLayout.this.tabIndicatorFullWidth && (childAt instanceof TabView)) {
                calculateTabViewContentBounds((TabView) childAt, TabLayout.this.tabViewContentBounds);
                left = (int) TabLayout.this.tabViewContentBounds.left;
                right = (int) TabLayout.this.tabViewContentBounds.right;
            }
            final int i3 = left;
            final int i4 = right;
            final int i5 = this.indicatorLeft;
            final int i6 = this.indicatorRight;
            if (!(i5 == i3 && i6 == i4)) {
                ValueAnimator valueAnimator = new ValueAnimator();
                this.indicatorAnimator = valueAnimator;
                valueAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                valueAnimator.setDuration((long) i2);
                valueAnimator.setFloatValues(new float[]{0.0f, 1.0f});
                valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float animatedFraction = valueAnimator.getAnimatedFraction();
                        SlidingTabIndicator.this.setIndicatorPosition(AnimationUtils.lerp(i5, i3, animatedFraction), AnimationUtils.lerp(i6, i4, animatedFraction));
                    }
                });
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        SlidingTabIndicator.this.selectedPosition = i;
                        SlidingTabIndicator.this.selectionOffset = 0.0f;
                    }
                });
                valueAnimator.start();
            }
        }

        private void calculateTabViewContentBounds(TabView tabView, RectF rectF) {
            int access$500 = tabView.getContentWidth();
            if (access$500 < TabLayout.this.dpToPx(24)) {
                access$500 = TabLayout.this.dpToPx(24);
            }
            int left = (tabView.getLeft() + tabView.getRight()) / 2;
            access$500 /= 2;
            rectF.set((float) (left - access$500), 0.0f, (float) (left + access$500), 0.0f);
        }

        public void draw(Canvas canvas) {
            int i = 0;
            int intrinsicHeight = TabLayout.this.tabSelectedIndicator != null ? TabLayout.this.tabSelectedIndicator.getIntrinsicHeight() : 0;
            if (this.selectedIndicatorHeight >= 0) {
                intrinsicHeight = this.selectedIndicatorHeight;
            }
            switch (TabLayout.this.tabIndicatorGravity) {
                case 0:
                    i = getHeight() - intrinsicHeight;
                    intrinsicHeight = getHeight();
                    break;
                case 1:
                    i = (getHeight() - intrinsicHeight) / 2;
                    intrinsicHeight = (getHeight() + intrinsicHeight) / 2;
                    break;
                case 2:
                    break;
                case 3:
                    intrinsicHeight = getHeight();
                    break;
                default:
                    intrinsicHeight = 0;
                    break;
            }
            if (this.indicatorLeft >= 0 && this.indicatorRight > this.indicatorLeft) {
                Drawable wrap = DrawableCompat.wrap(TabLayout.this.tabSelectedIndicator != null ? TabLayout.this.tabSelectedIndicator : this.defaultSelectionIndicator);
                wrap.setBounds(this.indicatorLeft, i, this.indicatorRight, intrinsicHeight);
                if (this.selectedIndicatorPaint != null) {
                    if (VERSION.SDK_INT == 21) {
                        wrap.setColorFilter(this.selectedIndicatorPaint.getColor(), android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        DrawableCompat.setTint(wrap, this.selectedIndicatorPaint.getColor());
                    }
                }
                wrap.draw(canvas);
            }
            super.draw(canvas);
        }
    }

    public static class Tab {
        public static final int INVALID_POSITION = -1;
        private CharSequence contentDesc;
        private View customView;
        private Drawable icon;
        public TabLayout parent;
        private int position = -1;
        private Object tag;
        private CharSequence text;
        public TabView view;

        @Nullable
        public Object getTag() {
            return this.tag;
        }

        @NonNull
        public Tab setTag(@Nullable Object obj) {
            this.tag = obj;
            return this;
        }

        @Nullable
        public View getCustomView() {
            return this.customView;
        }

        @NonNull
        public Tab setCustomView(@Nullable View view) {
            this.customView = view;
            updateView();
            return this;
        }

        @NonNull
        public Tab setCustomView(@LayoutRes int i) {
            return setCustomView(LayoutInflater.from(this.view.getContext()).inflate(i, this.view, false));
        }

        @Nullable
        public Drawable getIcon() {
            return this.icon;
        }

        public int getPosition() {
            return this.position;
        }

        /* Access modifiers changed, original: 0000 */
        public void setPosition(int i) {
            this.position = i;
        }

        @Nullable
        public CharSequence getText() {
            return this.text;
        }

        @NonNull
        public Tab setIcon(@Nullable Drawable drawable) {
            this.icon = drawable;
            updateView();
            return this;
        }

        @NonNull
        public Tab setIcon(@DrawableRes int i) {
            if (this.parent != null) {
                return setIcon(AppCompatResources.getDrawable(this.parent.getContext(), i));
            }
            throw new IllegalArgumentException("Tab not attached to a TabLayout");
        }

        @NonNull
        public Tab setText(@Nullable CharSequence charSequence) {
            if (TextUtils.isEmpty(this.contentDesc) && !TextUtils.isEmpty(charSequence)) {
                this.view.setContentDescription(charSequence);
            }
            this.text = charSequence;
            updateView();
            return this;
        }

        @NonNull
        public Tab setText(@StringRes int i) {
            if (this.parent != null) {
                return setText(this.parent.getResources().getText(i));
            }
            throw new IllegalArgumentException("Tab not attached to a TabLayout");
        }

        public void select() {
            if (this.parent != null) {
                this.parent.selectTab(this);
                return;
            }
            throw new IllegalArgumentException("Tab not attached to a TabLayout");
        }

        public boolean isSelected() {
            if (this.parent != null) {
                return this.parent.getSelectedTabPosition() == this.position;
            } else {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
        }

        @NonNull
        public Tab setContentDescription(@StringRes int i) {
            if (this.parent != null) {
                return setContentDescription(this.parent.getResources().getText(i));
            }
            throw new IllegalArgumentException("Tab not attached to a TabLayout");
        }

        @NonNull
        public Tab setContentDescription(@Nullable CharSequence charSequence) {
            this.contentDesc = charSequence;
            updateView();
            return this;
        }

        @Nullable
        public CharSequence getContentDescription() {
            return this.view == null ? null : this.view.getContentDescription();
        }

        /* Access modifiers changed, original: 0000 */
        public void updateView() {
            if (this.view != null) {
                this.view.update();
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void reset() {
            this.parent = null;
            this.view = null;
            this.tag = null;
            this.icon = null;
            this.text = null;
            this.contentDesc = null;
            this.position = -1;
            this.customView = null;
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabGravity {
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabIndicatorGravity {
    }

    class TabView extends LinearLayout {
        @Nullable
        private Drawable baseBackgroundDrawable;
        private ImageView customIconView;
        private TextView customTextView;
        private View customView;
        private int defaultMaxLines = 2;
        private ImageView iconView;
        private Tab tab;
        private TextView textView;

        public TabView(Context context) {
            super(context);
            updateBackgroundDrawable(context);
            ViewCompat.setPaddingRelative(this, TabLayout.this.tabPaddingStart, TabLayout.this.tabPaddingTop, TabLayout.this.tabPaddingEnd, TabLayout.this.tabPaddingBottom);
            setGravity(17);
            setOrientation(TabLayout.this.inlineLabel ^ 1);
            setClickable(true);
            ViewCompat.setPointerIcon(this, PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND));
        }

        private void updateBackgroundDrawable(Context context) {
            if (TabLayout.this.tabBackgroundResId != 0) {
                this.baseBackgroundDrawable = AppCompatResources.getDrawable(context, TabLayout.this.tabBackgroundResId);
                if (this.baseBackgroundDrawable != null && this.baseBackgroundDrawable.isStateful()) {
                    this.baseBackgroundDrawable.setState(getDrawableState());
                }
            } else {
                this.baseBackgroundDrawable = null;
            }
            Drawable gradientDrawable = new GradientDrawable();
            ((GradientDrawable) gradientDrawable).setColor(0);
            if (TabLayout.this.tabRippleColorStateList != null) {
                Drawable gradientDrawable2 = new GradientDrawable();
                gradientDrawable2.setCornerRadius(1.0E-5f);
                gradientDrawable2.setColor(-1);
                ColorStateList convertToRippleDrawableColor = RippleUtils.convertToRippleDrawableColor(TabLayout.this.tabRippleColorStateList);
                if (VERSION.SDK_INT >= 21) {
                    if (TabLayout.this.unboundedRipple) {
                        gradientDrawable = null;
                    }
                    if (TabLayout.this.unboundedRipple) {
                        gradientDrawable2 = null;
                    }
                    gradientDrawable = new RippleDrawable(convertToRippleDrawableColor, gradientDrawable, gradientDrawable2);
                } else {
                    DrawableCompat.setTintList(DrawableCompat.wrap(gradientDrawable2), convertToRippleDrawableColor);
                    gradientDrawable = new LayerDrawable(new Drawable[]{gradientDrawable, gradientDrawable2});
                }
            }
            ViewCompat.setBackground(this, gradientDrawable);
            TabLayout.this.invalidate();
        }

        private void drawBackground(Canvas canvas) {
            if (this.baseBackgroundDrawable != null) {
                this.baseBackgroundDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());
                this.baseBackgroundDrawable.draw(canvas);
            }
        }

        /* Access modifiers changed, original: protected */
        public void drawableStateChanged() {
            super.drawableStateChanged();
            int[] drawableState = getDrawableState();
            int i = 0;
            if (this.baseBackgroundDrawable != null && this.baseBackgroundDrawable.isStateful()) {
                i = 0 | this.baseBackgroundDrawable.setState(drawableState);
            }
            if (i != 0) {
                invalidate();
                TabLayout.this.invalidate();
            }
        }

        public boolean performClick() {
            boolean performClick = super.performClick();
            if (this.tab == null) {
                return performClick;
            }
            if (!performClick) {
                playSoundEffect(0);
            }
            this.tab.select();
            return true;
        }

        public void setSelected(boolean z) {
            Object obj = isSelected() != z ? 1 : null;
            super.setSelected(z);
            if (obj != null && z && VERSION.SDK_INT < 16) {
                sendAccessibilityEvent(4);
            }
            if (this.textView != null) {
                this.textView.setSelected(z);
            }
            if (this.iconView != null) {
                this.iconView.setSelected(z);
            }
            if (this.customView != null) {
                this.customView.setSelected(z);
            }
        }

        public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
            super.onInitializeAccessibilityEvent(accessibilityEvent);
            accessibilityEvent.setClassName(android.support.v7.app.ActionBar.Tab.class.getName());
        }

        @TargetApi(14)
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
            accessibilityNodeInfo.setClassName(android.support.v7.app.ActionBar.Tab.class.getName());
        }

        public void onMeasure(int i, int i2) {
            int size = MeasureSpec.getSize(i);
            int mode = MeasureSpec.getMode(i);
            int tabMaxWidth = TabLayout.this.getTabMaxWidth();
            if (tabMaxWidth > 0 && (mode == 0 || size > tabMaxWidth)) {
                i = MeasureSpec.makeMeasureSpec(TabLayout.this.tabMaxWidth, Integer.MIN_VALUE);
            }
            super.onMeasure(i, i2);
            if (this.textView != null) {
                float f = TabLayout.this.tabTextSize;
                mode = this.defaultMaxLines;
                Object obj = 1;
                if (this.iconView != null && this.iconView.getVisibility() == 0) {
                    mode = 1;
                } else if (this.textView != null && this.textView.getLineCount() > 1) {
                    f = TabLayout.this.tabTextMultiLineSize;
                }
                float textSize = this.textView.getTextSize();
                int lineCount = this.textView.getLineCount();
                int maxLines = TextViewCompat.getMaxLines(this.textView);
                if (f != textSize || (maxLines >= 0 && mode != maxLines)) {
                    if (TabLayout.this.mode == 1 && f > textSize && lineCount == 1) {
                        Layout layout = this.textView.getLayout();
                        if (layout == null || approximateLineWidth(layout, 0, f) > ((float) ((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight()))) {
                            obj = null;
                        }
                    }
                    if (obj != null) {
                        this.textView.setTextSize(0, f);
                        this.textView.setMaxLines(mode);
                        super.onMeasure(i, i2);
                    }
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void setTab(@Nullable Tab tab) {
            if (tab != this.tab) {
                this.tab = tab;
                update();
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void reset() {
            setTab(null);
            setSelected(false);
        }

        /* Access modifiers changed, original: final */
        public final void update() {
            Tab tab = this.tab;
            Drawable drawable = null;
            View customView = tab != null ? tab.getCustomView() : null;
            if (customView != null) {
                TabView parent = customView.getParent();
                if (parent != this) {
                    if (parent != null) {
                        parent.removeView(customView);
                    }
                    addView(customView);
                }
                this.customView = customView;
                if (this.textView != null) {
                    this.textView.setVisibility(8);
                }
                if (this.iconView != null) {
                    this.iconView.setVisibility(8);
                    this.iconView.setImageDrawable(null);
                }
                this.customTextView = (TextView) customView.findViewById(16908308);
                if (this.customTextView != null) {
                    this.defaultMaxLines = TextViewCompat.getMaxLines(this.customTextView);
                }
                this.customIconView = (ImageView) customView.findViewById(16908294);
            } else {
                if (this.customView != null) {
                    removeView(this.customView);
                    this.customView = null;
                }
                this.customTextView = null;
                this.customIconView = null;
            }
            boolean z = false;
            if (this.customView == null) {
                if (this.iconView == null) {
                    ImageView imageView = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.design_layout_tab_icon, this, false);
                    addView(imageView, 0);
                    this.iconView = imageView;
                }
                if (!(tab == null || tab.getIcon() == null)) {
                    drawable = DrawableCompat.wrap(tab.getIcon()).mutate();
                }
                if (drawable != null) {
                    DrawableCompat.setTintList(drawable, TabLayout.this.tabIconTint);
                    if (TabLayout.this.tabIconTintMode != null) {
                        DrawableCompat.setTintMode(drawable, TabLayout.this.tabIconTintMode);
                    }
                }
                if (this.textView == null) {
                    TextView textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.design_layout_tab_text, this, false);
                    addView(textView);
                    this.textView = textView;
                    this.defaultMaxLines = TextViewCompat.getMaxLines(this.textView);
                }
                TextViewCompat.setTextAppearance(this.textView, TabLayout.this.tabTextAppearance);
                if (TabLayout.this.tabTextColors != null) {
                    this.textView.setTextColor(TabLayout.this.tabTextColors);
                }
                updateTextAndIcon(this.textView, this.iconView);
            } else if (!(this.customTextView == null && this.customIconView == null)) {
                updateTextAndIcon(this.customTextView, this.customIconView);
            }
            if (!(tab == null || TextUtils.isEmpty(tab.contentDesc))) {
                setContentDescription(tab.contentDesc);
            }
            if (tab != null && tab.isSelected()) {
                z = true;
            }
            setSelected(z);
        }

        /* Access modifiers changed, original: final */
        public final void updateOrientation() {
            setOrientation(TabLayout.this.inlineLabel ^ 1);
            if (this.customTextView == null && this.customIconView == null) {
                updateTextAndIcon(this.textView, this.iconView);
            } else {
                updateTextAndIcon(this.customTextView, this.customIconView);
            }
        }

        private void updateTextAndIcon(@Nullable TextView textView, @Nullable ImageView imageView) {
            Drawable mutate = (this.tab == null || this.tab.getIcon() == null) ? null : DrawableCompat.wrap(this.tab.getIcon()).mutate();
            CharSequence text = this.tab != null ? this.tab.getText() : null;
            if (imageView != null) {
                if (mutate != null) {
                    imageView.setImageDrawable(mutate);
                    imageView.setVisibility(0);
                    setVisibility(0);
                } else {
                    imageView.setVisibility(8);
                    imageView.setImageDrawable(null);
                }
            }
            int isEmpty = TextUtils.isEmpty(text) ^ 1;
            if (textView != null) {
                if (isEmpty != 0) {
                    textView.setText(text);
                    textView.setVisibility(0);
                    setVisibility(0);
                } else {
                    textView.setVisibility(8);
                    textView.setText(null);
                }
            }
            if (imageView != null) {
                MarginLayoutParams marginLayoutParams = (MarginLayoutParams) imageView.getLayoutParams();
                int dpToPx = (isEmpty == 0 || imageView.getVisibility() != 0) ? 0 : TabLayout.this.dpToPx(8);
                if (TabLayout.this.inlineLabel) {
                    if (dpToPx != MarginLayoutParamsCompat.getMarginEnd(marginLayoutParams)) {
                        MarginLayoutParamsCompat.setMarginEnd(marginLayoutParams, dpToPx);
                        marginLayoutParams.bottomMargin = 0;
                        imageView.setLayoutParams(marginLayoutParams);
                        imageView.requestLayout();
                    }
                } else if (dpToPx != marginLayoutParams.bottomMargin) {
                    marginLayoutParams.bottomMargin = dpToPx;
                    MarginLayoutParamsCompat.setMarginEnd(marginLayoutParams, 0);
                    imageView.setLayoutParams(marginLayoutParams);
                    imageView.requestLayout();
                }
            }
            CharSequence access$100 = this.tab != null ? this.tab.contentDesc : null;
            if (isEmpty != 0) {
                access$100 = null;
            }
            TooltipCompat.setTooltipText(this, access$100);
        }

        private int getContentWidth() {
            r0 = new View[3];
            int i = 0;
            r0[0] = this.textView;
            r0[1] = this.iconView;
            r0[2] = this.customView;
            int length = r0.length;
            int i2 = 0;
            int i3 = 0;
            Object obj = null;
            while (i < length) {
                View view = r0[i];
                if (view != null && view.getVisibility() == 0) {
                    i3 = obj != null ? Math.min(i3, view.getLeft()) : view.getLeft();
                    i2 = obj != null ? Math.max(i2, view.getRight()) : view.getRight();
                    obj = 1;
                }
                i++;
            }
            return i2 - i3;
        }

        public Tab getTab() {
            return this.tab;
        }

        private float approximateLineWidth(Layout layout, int i, float f) {
            return layout.getLineWidth(i) * (f / layout.getPaint().getTextSize());
        }
    }

    private class AdapterChangeListener implements OnAdapterChangeListener {
        private boolean autoRefresh;

        AdapterChangeListener() {
        }

        public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter pagerAdapter, @Nullable PagerAdapter pagerAdapter2) {
            if (TabLayout.this.viewPager == viewPager) {
                TabLayout.this.setPagerAdapter(pagerAdapter2, this.autoRefresh);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void setAutoRefresh(boolean z) {
            this.autoRefresh = z;
        }
    }

    public interface OnTabSelectedListener extends BaseOnTabSelectedListener<Tab> {
    }

    public static class TabLayoutOnPageChangeListener implements OnPageChangeListener {
        private int previousScrollState;
        private int scrollState;
        private final WeakReference<TabLayout> tabLayoutRef;

        public TabLayoutOnPageChangeListener(TabLayout tabLayout) {
            this.tabLayoutRef = new WeakReference(tabLayout);
        }

        public void onPageScrollStateChanged(int i) {
            this.previousScrollState = this.scrollState;
            this.scrollState = i;
        }

        public void onPageScrolled(int i, float f, int i2) {
            TabLayout tabLayout = (TabLayout) this.tabLayoutRef.get();
            if (tabLayout != null) {
                boolean z = false;
                boolean z2 = this.scrollState != 2 || this.previousScrollState == 1;
                if (!(this.scrollState == 2 && this.previousScrollState == 0)) {
                    z = true;
                }
                tabLayout.setScrollPosition(i, f, z2, z);
            }
        }

        public void onPageSelected(int i) {
            TabLayout tabLayout = (TabLayout) this.tabLayoutRef.get();
            if (tabLayout != null && tabLayout.getSelectedTabPosition() != i && i < tabLayout.getTabCount()) {
                boolean z = this.scrollState == 0 || (this.scrollState == 2 && this.previousScrollState == 0);
                tabLayout.selectTab(tabLayout.getTabAt(i), z);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void reset() {
            this.scrollState = 0;
            this.previousScrollState = 0;
        }
    }

    public static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
        private final ViewPager viewPager;

        public void onTabReselected(Tab tab) {
        }

        public void onTabUnselected(Tab tab) {
        }

        public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
            this.viewPager = viewPager;
        }

        public void onTabSelected(Tab tab) {
            this.viewPager.setCurrentItem(tab.getPosition());
        }
    }

    public TabLayout(Context context) {
        this(context, null);
    }

    public TabLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.tabStyle);
    }

    public TabLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.tabs = new ArrayList();
        this.tabViewContentBounds = new RectF();
        this.tabMaxWidth = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        this.selectedListeners = new ArrayList();
        this.tabViewPool = new SimplePool(12);
        setHorizontalScrollBarEnabled(false);
        this.slidingTabIndicator = new SlidingTabIndicator(context);
        super.addView(this.slidingTabIndicator, 0, new FrameLayout.LayoutParams(-2, -1));
        TypedArray obtainStyledAttributes = ThemeEnforcement.obtainStyledAttributes(context, attributeSet, R.styleable.TabLayout, i, R.style.Widget_Design_TabLayout, R.styleable.TabLayout_tabTextAppearance);
        this.slidingTabIndicator.setSelectedIndicatorHeight(obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabIndicatorHeight, -1));
        this.slidingTabIndicator.setSelectedIndicatorColor(obtainStyledAttributes.getColor(R.styleable.TabLayout_tabIndicatorColor, 0));
        setSelectedTabIndicator(MaterialResources.getDrawable(context, obtainStyledAttributes, R.styleable.TabLayout_tabIndicator));
        setSelectedTabIndicatorGravity(obtainStyledAttributes.getInt(R.styleable.TabLayout_tabIndicatorGravity, 0));
        setTabIndicatorFullWidth(obtainStyledAttributes.getBoolean(R.styleable.TabLayout_tabIndicatorFullWidth, true));
        i = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabPadding, 0);
        this.tabPaddingBottom = i;
        this.tabPaddingEnd = i;
        this.tabPaddingTop = i;
        this.tabPaddingStart = i;
        this.tabPaddingStart = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingStart, this.tabPaddingStart);
        this.tabPaddingTop = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingTop, this.tabPaddingTop);
        this.tabPaddingEnd = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingEnd, this.tabPaddingEnd);
        this.tabPaddingBottom = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingBottom, this.tabPaddingBottom);
        this.tabTextAppearance = obtainStyledAttributes.getResourceId(R.styleable.TabLayout_tabTextAppearance, R.style.TextAppearance_Design_Tab);
        TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(this.tabTextAppearance, android.support.v7.appcompat.R.styleable.TextAppearance);
        try {
            this.tabTextSize = (float) obtainStyledAttributes2.getDimensionPixelSize(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize, 0);
            this.tabTextColors = MaterialResources.getColorStateList(context, obtainStyledAttributes2, android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor);
            if (obtainStyledAttributes.hasValue(R.styleable.TabLayout_tabTextColor)) {
                this.tabTextColors = MaterialResources.getColorStateList(context, obtainStyledAttributes, R.styleable.TabLayout_tabTextColor);
            }
            if (obtainStyledAttributes.hasValue(R.styleable.TabLayout_tabSelectedTextColor)) {
                this.tabTextColors = createColorStateList(this.tabTextColors.getDefaultColor(), obtainStyledAttributes.getColor(R.styleable.TabLayout_tabSelectedTextColor, 0));
            }
            this.tabIconTint = MaterialResources.getColorStateList(context, obtainStyledAttributes, R.styleable.TabLayout_tabIconTint);
            this.tabIconTintMode = ViewUtils.parseTintMode(obtainStyledAttributes.getInt(R.styleable.TabLayout_tabIconTintMode, -1), null);
            this.tabRippleColorStateList = MaterialResources.getColorStateList(context, obtainStyledAttributes, R.styleable.TabLayout_tabRippleColor);
            this.tabIndicatorAnimationDuration = obtainStyledAttributes.getInt(R.styleable.TabLayout_tabIndicatorAnimationDuration, ANIMATION_DURATION);
            this.requestedTabMinWidth = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabMinWidth, -1);
            this.requestedTabMaxWidth = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabMaxWidth, -1);
            this.tabBackgroundResId = obtainStyledAttributes.getResourceId(R.styleable.TabLayout_tabBackground, 0);
            this.contentInsetStart = obtainStyledAttributes.getDimensionPixelSize(R.styleable.TabLayout_tabContentStart, 0);
            this.mode = obtainStyledAttributes.getInt(R.styleable.TabLayout_tabMode, 1);
            this.tabGravity = obtainStyledAttributes.getInt(R.styleable.TabLayout_tabGravity, 0);
            this.inlineLabel = obtainStyledAttributes.getBoolean(R.styleable.TabLayout_tabInlineLabel, false);
            this.unboundedRipple = obtainStyledAttributes.getBoolean(R.styleable.TabLayout_tabUnboundedRipple, false);
            obtainStyledAttributes.recycle();
            Resources resources = getResources();
            this.tabTextMultiLineSize = (float) resources.getDimensionPixelSize(R.dimen.design_tab_text_size_2line);
            this.scrollableTabMinWidth = resources.getDimensionPixelSize(R.dimen.design_tab_scrollable_min_width);
            applyModeAndGravity();
        } finally {
            obtainStyledAttributes2.recycle();
        }
    }

    public void setSelectedTabIndicatorColor(@ColorInt int i) {
        this.slidingTabIndicator.setSelectedIndicatorColor(i);
    }

    @Deprecated
    public void setSelectedTabIndicatorHeight(int i) {
        this.slidingTabIndicator.setSelectedIndicatorHeight(i);
    }

    public void setScrollPosition(int i, float f, boolean z) {
        setScrollPosition(i, f, z, true);
    }

    /* Access modifiers changed, original: 0000 */
    public void setScrollPosition(int i, float f, boolean z, boolean z2) {
        int round = Math.round(((float) i) + f);
        if (round >= 0 && round < this.slidingTabIndicator.getChildCount()) {
            if (z2) {
                this.slidingTabIndicator.setIndicatorPositionFromTabPosition(i, f);
            }
            if (this.scrollAnimator != null && this.scrollAnimator.isRunning()) {
                this.scrollAnimator.cancel();
            }
            scrollTo(calculateScrollXForTab(i, f), 0);
            if (z) {
                setSelectedTabView(round);
            }
        }
    }

    public void addTab(@NonNull Tab tab) {
        addTab(tab, this.tabs.isEmpty());
    }

    public void addTab(@NonNull Tab tab, int i) {
        addTab(tab, i, this.tabs.isEmpty());
    }

    public void addTab(@NonNull Tab tab, boolean z) {
        addTab(tab, this.tabs.size(), z);
    }

    public void addTab(@NonNull Tab tab, int i, boolean z) {
        if (tab.parent == this) {
            configureTab(tab, i);
            addTabView(tab);
            if (z) {
                tab.select();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
    }

    private void addTabFromItemView(@NonNull TabItem tabItem) {
        Tab newTab = newTab();
        if (tabItem.text != null) {
            newTab.setText(tabItem.text);
        }
        if (tabItem.icon != null) {
            newTab.setIcon(tabItem.icon);
        }
        if (tabItem.customLayout != 0) {
            newTab.setCustomView(tabItem.customLayout);
        }
        if (!TextUtils.isEmpty(tabItem.getContentDescription())) {
            newTab.setContentDescription(tabItem.getContentDescription());
        }
        addTab(newTab);
    }

    @Deprecated
    public void setOnTabSelectedListener(@Nullable BaseOnTabSelectedListener baseOnTabSelectedListener) {
        if (this.selectedListener != null) {
            removeOnTabSelectedListener(this.selectedListener);
        }
        this.selectedListener = baseOnTabSelectedListener;
        if (baseOnTabSelectedListener != null) {
            addOnTabSelectedListener(baseOnTabSelectedListener);
        }
    }

    public void addOnTabSelectedListener(@NonNull BaseOnTabSelectedListener baseOnTabSelectedListener) {
        if (!this.selectedListeners.contains(baseOnTabSelectedListener)) {
            this.selectedListeners.add(baseOnTabSelectedListener);
        }
    }

    public void removeOnTabSelectedListener(@NonNull BaseOnTabSelectedListener baseOnTabSelectedListener) {
        this.selectedListeners.remove(baseOnTabSelectedListener);
    }

    public void clearOnTabSelectedListeners() {
        this.selectedListeners.clear();
    }

    @NonNull
    public Tab newTab() {
        Tab createTabFromPool = createTabFromPool();
        createTabFromPool.parent = this;
        createTabFromPool.view = createTabView(createTabFromPool);
        return createTabFromPool;
    }

    /* Access modifiers changed, original: protected */
    public Tab createTabFromPool() {
        Tab tab = (Tab) tabPool.acquire();
        return tab == null ? new Tab() : tab;
    }

    /* Access modifiers changed, original: protected */
    public boolean releaseFromTabPool(Tab tab) {
        return tabPool.release(tab);
    }

    public int getTabCount() {
        return this.tabs.size();
    }

    @Nullable
    public Tab getTabAt(int i) {
        return (i < 0 || i >= getTabCount()) ? null : (Tab) this.tabs.get(i);
    }

    public int getSelectedTabPosition() {
        return this.selectedTab != null ? this.selectedTab.getPosition() : -1;
    }

    public void removeTab(Tab tab) {
        if (tab.parent == this) {
            removeTabAt(tab.getPosition());
            return;
        }
        throw new IllegalArgumentException("Tab does not belong to this TabLayout.");
    }

    public void removeTabAt(int i) {
        int position = this.selectedTab != null ? this.selectedTab.getPosition() : 0;
        removeTabViewAt(i);
        Tab tab = (Tab) this.tabs.remove(i);
        if (tab != null) {
            tab.reset();
            releaseFromTabPool(tab);
        }
        int size = this.tabs.size();
        for (int i2 = i; i2 < size; i2++) {
            ((Tab) this.tabs.get(i2)).setPosition(i2);
        }
        if (position == i) {
            selectTab(this.tabs.isEmpty() ? null : (Tab) this.tabs.get(Math.max(0, i - 1)));
        }
    }

    public void removeAllTabs() {
        for (int childCount = this.slidingTabIndicator.getChildCount() - 1; childCount >= 0; childCount--) {
            removeTabViewAt(childCount);
        }
        Iterator it = this.tabs.iterator();
        while (it.hasNext()) {
            Tab tab = (Tab) it.next();
            it.remove();
            tab.reset();
            releaseFromTabPool(tab);
        }
        this.selectedTab = null;
    }

    public void setTabMode(int i) {
        if (i != this.mode) {
            this.mode = i;
            applyModeAndGravity();
        }
    }

    public int getTabMode() {
        return this.mode;
    }

    public void setTabGravity(int i) {
        if (this.tabGravity != i) {
            this.tabGravity = i;
            applyModeAndGravity();
        }
    }

    public int getTabGravity() {
        return this.tabGravity;
    }

    public void setSelectedTabIndicatorGravity(int i) {
        if (this.tabIndicatorGravity != i) {
            this.tabIndicatorGravity = i;
            ViewCompat.postInvalidateOnAnimation(this.slidingTabIndicator);
        }
    }

    public int getTabIndicatorGravity() {
        return this.tabIndicatorGravity;
    }

    public void setTabIndicatorFullWidth(boolean z) {
        this.tabIndicatorFullWidth = z;
        ViewCompat.postInvalidateOnAnimation(this.slidingTabIndicator);
    }

    public boolean isTabIndicatorFullWidth() {
        return this.tabIndicatorFullWidth;
    }

    public void setInlineLabel(boolean z) {
        if (this.inlineLabel != z) {
            this.inlineLabel = z;
            for (int i = 0; i < this.slidingTabIndicator.getChildCount(); i++) {
                View childAt = this.slidingTabIndicator.getChildAt(i);
                if (childAt instanceof TabView) {
                    ((TabView) childAt).updateOrientation();
                }
            }
            applyModeAndGravity();
        }
    }

    public void setInlineLabelResource(@BoolRes int i) {
        setInlineLabel(getResources().getBoolean(i));
    }

    public boolean isInlineLabel() {
        return this.inlineLabel;
    }

    public void setUnboundedRipple(boolean z) {
        if (this.unboundedRipple != z) {
            this.unboundedRipple = z;
            for (int i = 0; i < this.slidingTabIndicator.getChildCount(); i++) {
                View childAt = this.slidingTabIndicator.getChildAt(i);
                if (childAt instanceof TabView) {
                    ((TabView) childAt).updateBackgroundDrawable(getContext());
                }
            }
        }
    }

    public void setUnboundedRippleResource(@BoolRes int i) {
        setUnboundedRipple(getResources().getBoolean(i));
    }

    public boolean hasUnboundedRipple() {
        return this.unboundedRipple;
    }

    public void setTabTextColors(@Nullable ColorStateList colorStateList) {
        if (this.tabTextColors != colorStateList) {
            this.tabTextColors = colorStateList;
            updateAllTabs();
        }
    }

    @Nullable
    public ColorStateList getTabTextColors() {
        return this.tabTextColors;
    }

    public void setTabTextColors(int i, int i2) {
        setTabTextColors(createColorStateList(i, i2));
    }

    public void setTabIconTint(@Nullable ColorStateList colorStateList) {
        if (this.tabIconTint != colorStateList) {
            this.tabIconTint = colorStateList;
            updateAllTabs();
        }
    }

    public void setTabIconTintResource(@ColorRes int i) {
        setTabIconTint(AppCompatResources.getColorStateList(getContext(), i));
    }

    @Nullable
    public ColorStateList getTabIconTint() {
        return this.tabIconTint;
    }

    @Nullable
    public ColorStateList getTabRippleColor() {
        return this.tabRippleColorStateList;
    }

    public void setTabRippleColor(@Nullable ColorStateList colorStateList) {
        if (this.tabRippleColorStateList != colorStateList) {
            this.tabRippleColorStateList = colorStateList;
            for (int i = 0; i < this.slidingTabIndicator.getChildCount(); i++) {
                View childAt = this.slidingTabIndicator.getChildAt(i);
                if (childAt instanceof TabView) {
                    ((TabView) childAt).updateBackgroundDrawable(getContext());
                }
            }
        }
    }

    public void setTabRippleColorResource(@ColorRes int i) {
        setTabRippleColor(AppCompatResources.getColorStateList(getContext(), i));
    }

    @Nullable
    public Drawable getTabSelectedIndicator() {
        return this.tabSelectedIndicator;
    }

    public void setSelectedTabIndicator(@Nullable Drawable drawable) {
        if (this.tabSelectedIndicator != drawable) {
            this.tabSelectedIndicator = drawable;
            ViewCompat.postInvalidateOnAnimation(this.slidingTabIndicator);
        }
    }

    public void setSelectedTabIndicator(@DrawableRes int i) {
        if (i != 0) {
            setSelectedTabIndicator(AppCompatResources.getDrawable(getContext(), i));
        } else {
            setSelectedTabIndicator(null);
        }
    }

    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        setupWithViewPager(viewPager, true);
    }

    public void setupWithViewPager(@Nullable ViewPager viewPager, boolean z) {
        setupWithViewPager(viewPager, z, false);
    }

    private void setupWithViewPager(@Nullable ViewPager viewPager, boolean z, boolean z2) {
        if (this.viewPager != null) {
            if (this.pageChangeListener != null) {
                this.viewPager.removeOnPageChangeListener(this.pageChangeListener);
            }
            if (this.adapterChangeListener != null) {
                this.viewPager.removeOnAdapterChangeListener(this.adapterChangeListener);
            }
        }
        if (this.currentVpSelectedListener != null) {
            removeOnTabSelectedListener(this.currentVpSelectedListener);
            this.currentVpSelectedListener = null;
        }
        if (viewPager != null) {
            this.viewPager = viewPager;
            if (this.pageChangeListener == null) {
                this.pageChangeListener = new TabLayoutOnPageChangeListener(this);
            }
            this.pageChangeListener.reset();
            viewPager.addOnPageChangeListener(this.pageChangeListener);
            this.currentVpSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
            addOnTabSelectedListener(this.currentVpSelectedListener);
            PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null) {
                setPagerAdapter(adapter, z);
            }
            if (this.adapterChangeListener == null) {
                this.adapterChangeListener = new AdapterChangeListener();
            }
            this.adapterChangeListener.setAutoRefresh(z);
            viewPager.addOnAdapterChangeListener(this.adapterChangeListener);
            setScrollPosition(viewPager.getCurrentItem(), 0.0f, true);
        } else {
            this.viewPager = null;
            setPagerAdapter(null, false);
        }
        this.setupViewPagerImplicitly = z2;
    }

    @Deprecated
    public void setTabsFromPagerAdapter(@Nullable PagerAdapter pagerAdapter) {
        setPagerAdapter(pagerAdapter, false);
    }

    public boolean shouldDelayChildPressedState() {
        return getTabScrollRange() > 0;
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.viewPager == null) {
            ViewParent parent = getParent();
            if (parent instanceof ViewPager) {
                setupWithViewPager((ViewPager) parent, true, true);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.setupViewPagerImplicitly) {
            setupWithViewPager(null);
            this.setupViewPagerImplicitly = false;
        }
    }

    private int getTabScrollRange() {
        return Math.max(0, ((this.slidingTabIndicator.getWidth() - getWidth()) - getPaddingLeft()) - getPaddingRight());
    }

    /* Access modifiers changed, original: 0000 */
    public void setPagerAdapter(@Nullable PagerAdapter pagerAdapter, boolean z) {
        if (!(this.pagerAdapter == null || this.pagerAdapterObserver == null)) {
            this.pagerAdapter.unregisterDataSetObserver(this.pagerAdapterObserver);
        }
        this.pagerAdapter = pagerAdapter;
        if (z && pagerAdapter != null) {
            if (this.pagerAdapterObserver == null) {
                this.pagerAdapterObserver = new PagerAdapterObserver();
            }
            pagerAdapter.registerDataSetObserver(this.pagerAdapterObserver);
        }
        populateFromPagerAdapter();
    }

    /* Access modifiers changed, original: 0000 */
    public void populateFromPagerAdapter() {
        removeAllTabs();
        if (this.pagerAdapter != null) {
            int count = this.pagerAdapter.getCount();
            for (int i = 0; i < count; i++) {
                addTab(newTab().setText(this.pagerAdapter.getPageTitle(i)), false);
            }
            if (this.viewPager != null && count > 0) {
                count = this.viewPager.getCurrentItem();
                if (count != getSelectedTabPosition() && count < getTabCount()) {
                    selectTab(getTabAt(count));
                }
            }
        }
    }

    private void updateAllTabs() {
        int size = this.tabs.size();
        for (int i = 0; i < size; i++) {
            ((Tab) this.tabs.get(i)).updateView();
        }
    }

    private TabView createTabView(@NonNull Tab tab) {
        TabView tabView = this.tabViewPool != null ? (TabView) this.tabViewPool.acquire() : null;
        if (tabView == null) {
            tabView = new TabView(getContext());
        }
        tabView.setTab(tab);
        tabView.setFocusable(true);
        tabView.setMinimumWidth(getTabMinWidth());
        if (TextUtils.isEmpty(tab.contentDesc)) {
            tabView.setContentDescription(tab.text);
        } else {
            tabView.setContentDescription(tab.contentDesc);
        }
        return tabView;
    }

    private void configureTab(Tab tab, int i) {
        tab.setPosition(i);
        this.tabs.add(i, tab);
        int size = this.tabs.size();
        while (true) {
            i++;
            if (i < size) {
                ((Tab) this.tabs.get(i)).setPosition(i);
            } else {
                return;
            }
        }
    }

    private void addTabView(Tab tab) {
        this.slidingTabIndicator.addView(tab.view, tab.getPosition(), createLayoutParamsForTabs());
    }

    public void addView(View view) {
        addViewInternal(view);
    }

    public void addView(View view, int i) {
        addViewInternal(view);
    }

    public void addView(View view, ViewGroup.LayoutParams layoutParams) {
        addViewInternal(view);
    }

    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        addViewInternal(view);
    }

    private void addViewInternal(View view) {
        if (view instanceof TabItem) {
            addTabFromItemView((TabItem) view);
            return;
        }
        throw new IllegalArgumentException("Only TabItem instances can be added to TabLayout");
    }

    private LayoutParams createLayoutParamsForTabs() {
        LayoutParams layoutParams = new LayoutParams(-2, -1);
        updateTabViewLayoutParams(layoutParams);
        return layoutParams;
    }

    private void updateTabViewLayoutParams(LayoutParams layoutParams) {
        if (this.mode == 1 && this.tabGravity == 0) {
            layoutParams.width = 0;
            layoutParams.weight = 1.0f;
            return;
        }
        layoutParams.width = -2;
        layoutParams.weight = 0.0f;
    }

    /* Access modifiers changed, original: 0000 */
    @Dimension(unit = 1)
    public int dpToPx(@Dimension(unit = 0) int i) {
        return Math.round(getResources().getDisplayMetrics().density * ((float) i));
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < this.slidingTabIndicator.getChildCount(); i++) {
            View childAt = this.slidingTabIndicator.getChildAt(i);
            if (childAt instanceof TabView) {
                ((TabView) childAt).drawBackground(canvas);
            }
        }
        super.onDraw(canvas);
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Missing block: B:17:0x0067, code skipped:
            if (r1.getMeasuredWidth() != getMeasuredWidth()) goto L_0x0069;
     */
    /* JADX WARNING: Missing block: B:18:0x0069, code skipped:
            r6 = 1;
     */
    /* JADX WARNING: Missing block: B:20:0x0073, code skipped:
            if (r1.getMeasuredWidth() < getMeasuredWidth()) goto L_0x0069;
     */
    public void onMeasure(int r6, int r7) {
        /*
        r5 = this;
        r0 = r5.getDefaultHeight();
        r0 = r5.dpToPx(r0);
        r1 = r5.getPaddingTop();
        r0 = r0 + r1;
        r1 = r5.getPaddingBottom();
        r0 = r0 + r1;
        r1 = android.view.View.MeasureSpec.getMode(r7);
        r2 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r3 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        if (r1 == r2) goto L_0x0024;
    L_0x001c:
        if (r1 == 0) goto L_0x001f;
    L_0x001e:
        goto L_0x0030;
    L_0x001f:
        r7 = android.view.View.MeasureSpec.makeMeasureSpec(r0, r3);
        goto L_0x0030;
    L_0x0024:
        r7 = android.view.View.MeasureSpec.getSize(r7);
        r7 = java.lang.Math.min(r0, r7);
        r7 = android.view.View.MeasureSpec.makeMeasureSpec(r7, r3);
    L_0x0030:
        r0 = android.view.View.MeasureSpec.getSize(r6);
        r1 = android.view.View.MeasureSpec.getMode(r6);
        if (r1 == 0) goto L_0x004a;
    L_0x003a:
        r1 = r5.requestedTabMaxWidth;
        if (r1 <= 0) goto L_0x0041;
    L_0x003e:
        r0 = r5.requestedTabMaxWidth;
        goto L_0x0048;
    L_0x0041:
        r1 = 56;
        r1 = r5.dpToPx(r1);
        r0 = r0 - r1;
    L_0x0048:
        r5.tabMaxWidth = r0;
    L_0x004a:
        super.onMeasure(r6, r7);
        r6 = r5.getChildCount();
        r0 = 1;
        if (r6 != r0) goto L_0x0096;
    L_0x0054:
        r6 = 0;
        r1 = r5.getChildAt(r6);
        r2 = r5.mode;
        switch(r2) {
            case 0: goto L_0x006b;
            case 1: goto L_0x005f;
            default: goto L_0x005e;
        };
    L_0x005e:
        goto L_0x0076;
    L_0x005f:
        r2 = r1.getMeasuredWidth();
        r4 = r5.getMeasuredWidth();
        if (r2 == r4) goto L_0x0076;
    L_0x0069:
        r6 = 1;
        goto L_0x0076;
    L_0x006b:
        r2 = r1.getMeasuredWidth();
        r4 = r5.getMeasuredWidth();
        if (r2 >= r4) goto L_0x0076;
    L_0x0075:
        goto L_0x0069;
    L_0x0076:
        if (r6 == 0) goto L_0x0096;
    L_0x0078:
        r6 = r5.getPaddingTop();
        r0 = r5.getPaddingBottom();
        r6 = r6 + r0;
        r0 = r1.getLayoutParams();
        r0 = r0.height;
        r6 = getChildMeasureSpec(r7, r6, r0);
        r7 = r5.getMeasuredWidth();
        r7 = android.view.View.MeasureSpec.makeMeasureSpec(r7, r3);
        r1.measure(r7, r6);
    L_0x0096:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.design.widget.TabLayout.onMeasure(int, int):void");
    }

    private void removeTabViewAt(int i) {
        TabView tabView = (TabView) this.slidingTabIndicator.getChildAt(i);
        this.slidingTabIndicator.removeViewAt(i);
        if (tabView != null) {
            tabView.reset();
            this.tabViewPool.release(tabView);
        }
        requestLayout();
    }

    private void animateToTab(int i) {
        if (i != -1) {
            if (getWindowToken() == null || !ViewCompat.isLaidOut(this) || this.slidingTabIndicator.childrenNeedLayout()) {
                setScrollPosition(i, 0.0f, true);
                return;
            }
            if (getScrollX() != calculateScrollXForTab(i, 0.0f)) {
                ensureScrollAnimator();
                this.scrollAnimator.setIntValues(new int[]{r0, r2});
                this.scrollAnimator.start();
            }
            this.slidingTabIndicator.animateIndicatorToPosition(i, this.tabIndicatorAnimationDuration);
        }
    }

    private void ensureScrollAnimator() {
        if (this.scrollAnimator == null) {
            this.scrollAnimator = new ValueAnimator();
            this.scrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            this.scrollAnimator.setDuration((long) this.tabIndicatorAnimationDuration);
            this.scrollAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    TabLayout.this.scrollTo(((Integer) valueAnimator.getAnimatedValue()).intValue(), 0);
                }
            });
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setScrollAnimatorListener(AnimatorListener animatorListener) {
        ensureScrollAnimator();
        this.scrollAnimator.addListener(animatorListener);
    }

    private void setSelectedTabView(int i) {
        int childCount = this.slidingTabIndicator.getChildCount();
        if (i < childCount) {
            int i2 = 0;
            while (i2 < childCount) {
                View childAt = this.slidingTabIndicator.getChildAt(i2);
                boolean z = true;
                childAt.setSelected(i2 == i);
                if (i2 != i) {
                    z = false;
                }
                childAt.setActivated(z);
                i2++;
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void selectTab(Tab tab) {
        selectTab(tab, true);
    }

    /* Access modifiers changed, original: 0000 */
    public void selectTab(Tab tab, boolean z) {
        Tab tab2 = this.selectedTab;
        if (tab2 != tab) {
            int position = tab != null ? tab.getPosition() : -1;
            if (z) {
                if ((tab2 == null || tab2.getPosition() == -1) && position != -1) {
                    setScrollPosition(position, 0.0f, true);
                } else {
                    animateToTab(position);
                }
                if (position != -1) {
                    setSelectedTabView(position);
                }
            }
            this.selectedTab = tab;
            if (tab2 != null) {
                dispatchTabUnselected(tab2);
            }
            if (tab != null) {
                dispatchTabSelected(tab);
            }
        } else if (tab2 != null) {
            dispatchTabReselected(tab);
            animateToTab(tab.getPosition());
        }
    }

    private void dispatchTabSelected(@NonNull Tab tab) {
        for (int size = this.selectedListeners.size() - 1; size >= 0; size--) {
            ((BaseOnTabSelectedListener) this.selectedListeners.get(size)).onTabSelected(tab);
        }
    }

    private void dispatchTabUnselected(@NonNull Tab tab) {
        for (int size = this.selectedListeners.size() - 1; size >= 0; size--) {
            ((BaseOnTabSelectedListener) this.selectedListeners.get(size)).onTabUnselected(tab);
        }
    }

    private void dispatchTabReselected(@NonNull Tab tab) {
        for (int size = this.selectedListeners.size() - 1; size >= 0; size--) {
            ((BaseOnTabSelectedListener) this.selectedListeners.get(size)).onTabReselected(tab);
        }
    }

    private int calculateScrollXForTab(int i, float f) {
        int i2 = 0;
        if (this.mode != 0) {
            return 0;
        }
        View childAt = this.slidingTabIndicator.getChildAt(i);
        i++;
        View childAt2 = i < this.slidingTabIndicator.getChildCount() ? this.slidingTabIndicator.getChildAt(i) : null;
        int width = childAt != null ? childAt.getWidth() : 0;
        if (childAt2 != null) {
            i2 = childAt2.getWidth();
        }
        i = (childAt.getLeft() + (width / 2)) - (getWidth() / 2);
        int i3 = (int) ((((float) (width + i2)) * 0.5f) * f);
        return ViewCompat.getLayoutDirection(this) == 0 ? i + i3 : i - i3;
    }

    private void applyModeAndGravity() {
        ViewCompat.setPaddingRelative(this.slidingTabIndicator, this.mode == 0 ? Math.max(0, this.contentInsetStart - this.tabPaddingStart) : 0, 0, 0, 0);
        switch (this.mode) {
            case 0:
                this.slidingTabIndicator.setGravity(GravityCompat.START);
                break;
            case 1:
                this.slidingTabIndicator.setGravity(1);
                break;
        }
        updateTabViews(true);
    }

    /* Access modifiers changed, original: 0000 */
    public void updateTabViews(boolean z) {
        for (int i = 0; i < this.slidingTabIndicator.getChildCount(); i++) {
            View childAt = this.slidingTabIndicator.getChildAt(i);
            childAt.setMinimumWidth(getTabMinWidth());
            updateTabViewLayoutParams((LayoutParams) childAt.getLayoutParams());
            if (z) {
                childAt.requestLayout();
            }
        }
    }

    private static ColorStateList createColorStateList(int i, int i2) {
        r1 = new int[2][];
        int[] iArr = new int[]{SELECTED_STATE_SET, i2};
        r1[1] = EMPTY_STATE_SET;
        iArr[1] = i;
        return new ColorStateList(r1, iArr);
    }

    @Dimension(unit = 0)
    private int getDefaultHeight() {
        int size = this.tabs.size();
        Object obj = null;
        for (int i = 0; i < size; i++) {
            Tab tab = (Tab) this.tabs.get(i);
            if (tab != null && tab.getIcon() != null && !TextUtils.isEmpty(tab.getText())) {
                obj = 1;
                break;
            }
        }
        return (obj == null || this.inlineLabel) ? 48 : 72;
    }

    private int getTabMinWidth() {
        if (this.requestedTabMinWidth != -1) {
            return this.requestedTabMinWidth;
        }
        return this.mode == 0 ? this.scrollableTabMinWidth : 0;
    }

    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return generateDefaultLayoutParams();
    }

    /* Access modifiers changed, original: 0000 */
    public int getTabMaxWidth() {
        return this.tabMaxWidth;
    }
}
