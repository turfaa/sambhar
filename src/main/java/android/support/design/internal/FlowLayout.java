package android.support.design.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.design.R;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

@RestrictTo({Scope.LIBRARY_GROUP})
public class FlowLayout extends ViewGroup {
    private int itemSpacing;
    private int lineSpacing;
    private boolean singleLine;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FlowLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.singleLine = false;
        loadFromAttributes(context, attributeSet);
    }

    @TargetApi(21)
    public FlowLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.singleLine = false;
        loadFromAttributes(context, attributeSet);
    }

    private void loadFromAttributes(Context context, AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.FlowLayout, 0, 0);
        this.lineSpacing = obtainStyledAttributes.getDimensionPixelSize(R.styleable.FlowLayout_lineSpacing, 0);
        this.itemSpacing = obtainStyledAttributes.getDimensionPixelSize(R.styleable.FlowLayout_itemSpacing, 0);
        obtainStyledAttributes.recycle();
    }

    /* Access modifiers changed, original: protected */
    public int getLineSpacing() {
        return this.lineSpacing;
    }

    /* Access modifiers changed, original: protected */
    public void setLineSpacing(int i) {
        this.lineSpacing = i;
    }

    /* Access modifiers changed, original: protected */
    public int getItemSpacing() {
        return this.itemSpacing;
    }

    /* Access modifiers changed, original: protected */
    public void setItemSpacing(int i) {
        this.itemSpacing = i;
    }

    /* Access modifiers changed, original: protected */
    public boolean isSingleLine() {
        return this.singleLine;
    }

    public void setSingleLine(boolean z) {
        this.singleLine = z;
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int i, int i2) {
        int size = MeasureSpec.getSize(i);
        int mode = MeasureSpec.getMode(i);
        int size2 = MeasureSpec.getSize(i2);
        int mode2 = MeasureSpec.getMode(i2);
        int i3 = (mode == Integer.MIN_VALUE || mode == 1073741824) ? size : ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        i3 -= getPaddingRight();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int i4 = paddingTop;
        int i5 = 0;
        for (int i6 = 0; i6 < getChildCount(); i6++) {
            View childAt = getChildAt(i6);
            if (childAt.getVisibility() == 8) {
                int i7 = i;
                int i8 = i2;
            } else {
                int i9;
                int i10;
                measureChild(childAt, i, i2);
                LayoutParams layoutParams = childAt.getLayoutParams();
                if (layoutParams instanceof MarginLayoutParams) {
                    MarginLayoutParams marginLayoutParams = (MarginLayoutParams) layoutParams;
                    i9 = marginLayoutParams.leftMargin + 0;
                    i10 = marginLayoutParams.rightMargin + 0;
                } else {
                    i9 = 0;
                    i10 = 0;
                }
                int i11 = paddingLeft;
                if ((paddingLeft + i9) + childAt.getMeasuredWidth() > i3 && !isSingleLine()) {
                    i4 = this.lineSpacing + paddingTop;
                    i11 = getPaddingLeft();
                }
                paddingTop = (i11 + i9) + childAt.getMeasuredWidth();
                paddingLeft = childAt.getMeasuredHeight() + i4;
                if (paddingTop > i5) {
                    i5 = paddingTop;
                }
                paddingTop = paddingLeft;
                paddingLeft = i11 + (((i9 + i10) + childAt.getMeasuredWidth()) + this.itemSpacing);
            }
        }
        setMeasuredDimension(getMeasuredDimension(size, mode, i5), getMeasuredDimension(size2, mode2, paddingTop));
    }

    private static int getMeasuredDimension(int i, int i2, int i3) {
        if (i2 != Integer.MIN_VALUE) {
            return i2 != 1073741824 ? i3 : i;
        } else {
            return Math.min(i3, i);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (getChildCount() != 0) {
            Object obj = 1;
            if (ViewCompat.getLayoutDirection(this) != 1) {
                obj = null;
            }
            int paddingRight = obj != null ? getPaddingRight() : getPaddingLeft();
            int paddingLeft = obj != null ? getPaddingLeft() : getPaddingRight();
            int paddingTop = getPaddingTop();
            i3 = (i3 - i) - paddingLeft;
            paddingLeft = paddingRight;
            int i5 = paddingTop;
            for (i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                if (childAt.getVisibility() != 8) {
                    int marginStart;
                    int marginEnd;
                    LayoutParams layoutParams = childAt.getLayoutParams();
                    if (layoutParams instanceof MarginLayoutParams) {
                        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) layoutParams;
                        marginStart = MarginLayoutParamsCompat.getMarginStart(marginLayoutParams);
                        marginEnd = MarginLayoutParamsCompat.getMarginEnd(marginLayoutParams);
                    } else {
                        marginEnd = 0;
                        marginStart = 0;
                    }
                    int measuredWidth = (paddingLeft + marginStart) + childAt.getMeasuredWidth();
                    if (!this.singleLine && measuredWidth > i3) {
                        i5 = paddingTop + this.lineSpacing;
                        paddingLeft = paddingRight;
                    }
                    paddingTop = paddingLeft + marginStart;
                    measuredWidth = childAt.getMeasuredWidth() + paddingTop;
                    int measuredHeight = childAt.getMeasuredHeight() + i5;
                    if (obj != null) {
                        childAt.layout(i3 - measuredWidth, i5, (i3 - paddingLeft) - marginStart, measuredHeight);
                    } else {
                        childAt.layout(paddingTop, i5, measuredWidth, measuredHeight);
                    }
                    paddingLeft += ((marginStart + marginEnd) + childAt.getMeasuredWidth()) + this.itemSpacing;
                    paddingTop = measuredHeight;
                }
            }
        }
    }
}
