package android.support.design.widget;

import android.animation.TimeInterpolator;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.support.annotation.ColorInt;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.VisibleForTesting;
import android.support.design.animation.AnimationUtils;
import android.support.v4.math.MathUtils;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.appcompat.R;
import android.support.v7.widget.TintTypedArray;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;

@RestrictTo({Scope.LIBRARY_GROUP})
public final class CollapsingTextHelper {
    private static final boolean DEBUG_DRAW = false;
    private static final Paint DEBUG_DRAW_PAINT = null;
    private static final boolean USE_SCALING_TEXTURE = (VERSION.SDK_INT < 18);
    private boolean boundsChanged;
    private final Rect collapsedBounds;
    private float collapsedDrawX;
    private float collapsedDrawY;
    private int collapsedShadowColor;
    private float collapsedShadowDx;
    private float collapsedShadowDy;
    private float collapsedShadowRadius;
    private ColorStateList collapsedTextColor;
    private int collapsedTextGravity = 16;
    private float collapsedTextSize = 15.0f;
    private Typeface collapsedTypeface;
    private final RectF currentBounds;
    private float currentDrawX;
    private float currentDrawY;
    private float currentTextSize;
    private Typeface currentTypeface;
    private boolean drawTitle;
    private final Rect expandedBounds;
    private float expandedDrawX;
    private float expandedDrawY;
    private float expandedFraction;
    private int expandedShadowColor;
    private float expandedShadowDx;
    private float expandedShadowDy;
    private float expandedShadowRadius;
    private ColorStateList expandedTextColor;
    private int expandedTextGravity = 16;
    private float expandedTextSize = 15.0f;
    private Bitmap expandedTitleTexture;
    private Typeface expandedTypeface;
    private boolean isRtl;
    private TimeInterpolator positionInterpolator;
    private float scale;
    private int[] state;
    private CharSequence text;
    private final TextPaint textPaint;
    private TimeInterpolator textSizeInterpolator;
    private CharSequence textToDraw;
    private float textureAscent;
    private float textureDescent;
    private Paint texturePaint;
    private final TextPaint tmpPaint;
    private boolean useTexture;
    private final View view;

    static {
        if (DEBUG_DRAW_PAINT != null) {
            DEBUG_DRAW_PAINT.setAntiAlias(true);
            DEBUG_DRAW_PAINT.setColor(-65281);
        }
    }

    public CollapsingTextHelper(View view) {
        this.view = view;
        this.textPaint = new TextPaint(129);
        this.tmpPaint = new TextPaint(this.textPaint);
        this.collapsedBounds = new Rect();
        this.expandedBounds = new Rect();
        this.currentBounds = new RectF();
    }

    public void setTextSizeInterpolator(TimeInterpolator timeInterpolator) {
        this.textSizeInterpolator = timeInterpolator;
        recalculate();
    }

    public void setPositionInterpolator(TimeInterpolator timeInterpolator) {
        this.positionInterpolator = timeInterpolator;
        recalculate();
    }

    public void setExpandedTextSize(float f) {
        if (this.expandedTextSize != f) {
            this.expandedTextSize = f;
            recalculate();
        }
    }

    public void setCollapsedTextSize(float f) {
        if (this.collapsedTextSize != f) {
            this.collapsedTextSize = f;
            recalculate();
        }
    }

    public void setCollapsedTextColor(ColorStateList colorStateList) {
        if (this.collapsedTextColor != colorStateList) {
            this.collapsedTextColor = colorStateList;
            recalculate();
        }
    }

    public void setExpandedTextColor(ColorStateList colorStateList) {
        if (this.expandedTextColor != colorStateList) {
            this.expandedTextColor = colorStateList;
            recalculate();
        }
    }

    public void setExpandedBounds(int i, int i2, int i3, int i4) {
        if (!rectEquals(this.expandedBounds, i, i2, i3, i4)) {
            this.expandedBounds.set(i, i2, i3, i4);
            this.boundsChanged = true;
            onBoundsChanged();
        }
    }

    public void setCollapsedBounds(int i, int i2, int i3, int i4) {
        if (!rectEquals(this.collapsedBounds, i, i2, i3, i4)) {
            this.collapsedBounds.set(i, i2, i3, i4);
            this.boundsChanged = true;
            onBoundsChanged();
        }
    }

    public float calculateCollapsedTextWidth() {
        if (this.text == null) {
            return 0.0f;
        }
        getTextPaintCollapsed(this.tmpPaint);
        return this.tmpPaint.measureText(this.text, 0, this.text.length());
    }

    public float getCollapsedTextHeight() {
        getTextPaintCollapsed(this.tmpPaint);
        return -this.tmpPaint.ascent();
    }

    public void getCollapsedTextActualBounds(RectF rectF) {
        float calculateCollapsedTextWidth;
        boolean calculateIsRtl = calculateIsRtl(this.text);
        if (calculateIsRtl) {
            calculateCollapsedTextWidth = ((float) this.collapsedBounds.right) - calculateCollapsedTextWidth();
        } else {
            calculateCollapsedTextWidth = (float) this.collapsedBounds.left;
        }
        rectF.left = calculateCollapsedTextWidth;
        rectF.top = (float) this.collapsedBounds.top;
        rectF.right = !calculateIsRtl ? rectF.left + calculateCollapsedTextWidth() : (float) this.collapsedBounds.right;
        rectF.bottom = ((float) this.collapsedBounds.top) + getCollapsedTextHeight();
    }

    private void getTextPaintCollapsed(TextPaint textPaint) {
        textPaint.setTextSize(this.collapsedTextSize);
        textPaint.setTypeface(this.collapsedTypeface);
    }

    /* Access modifiers changed, original: 0000 */
    public void onBoundsChanged() {
        boolean z = this.collapsedBounds.width() > 0 && this.collapsedBounds.height() > 0 && this.expandedBounds.width() > 0 && this.expandedBounds.height() > 0;
        this.drawTitle = z;
    }

    public void setExpandedTextGravity(int i) {
        if (this.expandedTextGravity != i) {
            this.expandedTextGravity = i;
            recalculate();
        }
    }

    public int getExpandedTextGravity() {
        return this.expandedTextGravity;
    }

    public void setCollapsedTextGravity(int i) {
        if (this.collapsedTextGravity != i) {
            this.collapsedTextGravity = i;
            recalculate();
        }
    }

    public int getCollapsedTextGravity() {
        return this.collapsedTextGravity;
    }

    public void setCollapsedTextAppearance(int i) {
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(this.view.getContext(), i, R.styleable.TextAppearance);
        if (obtainStyledAttributes.hasValue(R.styleable.TextAppearance_android_textColor)) {
            this.collapsedTextColor = obtainStyledAttributes.getColorStateList(R.styleable.TextAppearance_android_textColor);
        }
        if (obtainStyledAttributes.hasValue(R.styleable.TextAppearance_android_textSize)) {
            this.collapsedTextSize = (float) obtainStyledAttributes.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, (int) this.collapsedTextSize);
        }
        this.collapsedShadowColor = obtainStyledAttributes.getInt(R.styleable.TextAppearance_android_shadowColor, 0);
        this.collapsedShadowDx = obtainStyledAttributes.getFloat(R.styleable.TextAppearance_android_shadowDx, 0.0f);
        this.collapsedShadowDy = obtainStyledAttributes.getFloat(R.styleable.TextAppearance_android_shadowDy, 0.0f);
        this.collapsedShadowRadius = obtainStyledAttributes.getFloat(R.styleable.TextAppearance_android_shadowRadius, 0.0f);
        obtainStyledAttributes.recycle();
        if (VERSION.SDK_INT >= 16) {
            this.collapsedTypeface = readFontFamilyTypeface(i);
        }
        recalculate();
    }

    public void setExpandedTextAppearance(int i) {
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(this.view.getContext(), i, R.styleable.TextAppearance);
        if (obtainStyledAttributes.hasValue(R.styleable.TextAppearance_android_textColor)) {
            this.expandedTextColor = obtainStyledAttributes.getColorStateList(R.styleable.TextAppearance_android_textColor);
        }
        if (obtainStyledAttributes.hasValue(R.styleable.TextAppearance_android_textSize)) {
            this.expandedTextSize = (float) obtainStyledAttributes.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, (int) this.expandedTextSize);
        }
        this.expandedShadowColor = obtainStyledAttributes.getInt(R.styleable.TextAppearance_android_shadowColor, 0);
        this.expandedShadowDx = obtainStyledAttributes.getFloat(R.styleable.TextAppearance_android_shadowDx, 0.0f);
        this.expandedShadowDy = obtainStyledAttributes.getFloat(R.styleable.TextAppearance_android_shadowDy, 0.0f);
        this.expandedShadowRadius = obtainStyledAttributes.getFloat(R.styleable.TextAppearance_android_shadowRadius, 0.0f);
        obtainStyledAttributes.recycle();
        if (VERSION.SDK_INT >= 16) {
            this.expandedTypeface = readFontFamilyTypeface(i);
        }
        recalculate();
    }

    private Typeface readFontFamilyTypeface(int i) {
        TypedArray obtainStyledAttributes = this.view.getContext().obtainStyledAttributes(i, new int[]{16843692});
        try {
            String string = obtainStyledAttributes.getString(0);
            if (string != null) {
                Typeface create = Typeface.create(string, 0);
                return create;
            }
            obtainStyledAttributes.recycle();
            return null;
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    public void setCollapsedTypeface(Typeface typeface) {
        if (this.collapsedTypeface != typeface) {
            this.collapsedTypeface = typeface;
            recalculate();
        }
    }

    public void setExpandedTypeface(Typeface typeface) {
        if (this.expandedTypeface != typeface) {
            this.expandedTypeface = typeface;
            recalculate();
        }
    }

    public void setTypefaces(Typeface typeface) {
        this.expandedTypeface = typeface;
        this.collapsedTypeface = typeface;
        recalculate();
    }

    public Typeface getCollapsedTypeface() {
        return this.collapsedTypeface != null ? this.collapsedTypeface : Typeface.DEFAULT;
    }

    public Typeface getExpandedTypeface() {
        return this.expandedTypeface != null ? this.expandedTypeface : Typeface.DEFAULT;
    }

    public void setExpansionFraction(float f) {
        f = MathUtils.clamp(f, 0.0f, 1.0f);
        if (f != this.expandedFraction) {
            this.expandedFraction = f;
            calculateCurrentOffsets();
        }
    }

    public final boolean setState(int[] iArr) {
        this.state = iArr;
        if (!isStateful()) {
            return false;
        }
        recalculate();
        return true;
    }

    public final boolean isStateful() {
        return (this.collapsedTextColor != null && this.collapsedTextColor.isStateful()) || (this.expandedTextColor != null && this.expandedTextColor.isStateful());
    }

    public float getExpansionFraction() {
        return this.expandedFraction;
    }

    public float getCollapsedTextSize() {
        return this.collapsedTextSize;
    }

    public float getExpandedTextSize() {
        return this.expandedTextSize;
    }

    private void calculateCurrentOffsets() {
        calculateOffsets(this.expandedFraction);
    }

    private void calculateOffsets(float f) {
        interpolateBounds(f);
        this.currentDrawX = lerp(this.expandedDrawX, this.collapsedDrawX, f, this.positionInterpolator);
        this.currentDrawY = lerp(this.expandedDrawY, this.collapsedDrawY, f, this.positionInterpolator);
        setInterpolatedTextSize(lerp(this.expandedTextSize, this.collapsedTextSize, f, this.textSizeInterpolator));
        if (this.collapsedTextColor != this.expandedTextColor) {
            this.textPaint.setColor(blendColors(getCurrentExpandedTextColor(), getCurrentCollapsedTextColor(), f));
        } else {
            this.textPaint.setColor(getCurrentCollapsedTextColor());
        }
        this.textPaint.setShadowLayer(lerp(this.expandedShadowRadius, this.collapsedShadowRadius, f, null), lerp(this.expandedShadowDx, this.collapsedShadowDx, f, null), lerp(this.expandedShadowDy, this.collapsedShadowDy, f, null), blendColors(this.expandedShadowColor, this.collapsedShadowColor, f));
        ViewCompat.postInvalidateOnAnimation(this.view);
    }

    @ColorInt
    private int getCurrentExpandedTextColor() {
        if (this.state != null) {
            return this.expandedTextColor.getColorForState(this.state, 0);
        }
        return this.expandedTextColor.getDefaultColor();
    }

    @ColorInt
    @VisibleForTesting
    public int getCurrentCollapsedTextColor() {
        if (this.state != null) {
            return this.collapsedTextColor.getColorForState(this.state, 0);
        }
        return this.collapsedTextColor.getDefaultColor();
    }

    private void calculateBaseOffsets() {
        float f = this.currentTextSize;
        calculateUsingTextSize(this.collapsedTextSize);
        float f2 = 0.0f;
        float measureText = this.textToDraw != null ? this.textPaint.measureText(this.textToDraw, 0, this.textToDraw.length()) : 0.0f;
        int absoluteGravity = GravityCompat.getAbsoluteGravity(this.collapsedTextGravity, this.isRtl);
        int i = absoluteGravity & 112;
        if (i == 48) {
            this.collapsedDrawY = ((float) this.collapsedBounds.top) - this.textPaint.ascent();
        } else if (i != 80) {
            this.collapsedDrawY = ((float) this.collapsedBounds.centerY()) + (((this.textPaint.descent() - this.textPaint.ascent()) / 2.0f) - this.textPaint.descent());
        } else {
            this.collapsedDrawY = (float) this.collapsedBounds.bottom;
        }
        absoluteGravity &= GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        if (absoluteGravity == 1) {
            this.collapsedDrawX = ((float) this.collapsedBounds.centerX()) - (measureText / 2.0f);
        } else if (absoluteGravity != 5) {
            this.collapsedDrawX = (float) this.collapsedBounds.left;
        } else {
            this.collapsedDrawX = ((float) this.collapsedBounds.right) - measureText;
        }
        calculateUsingTextSize(this.expandedTextSize);
        if (this.textToDraw != null) {
            f2 = this.textPaint.measureText(this.textToDraw, 0, this.textToDraw.length());
        }
        int absoluteGravity2 = GravityCompat.getAbsoluteGravity(this.expandedTextGravity, this.isRtl);
        int i2 = absoluteGravity2 & 112;
        if (i2 == 48) {
            this.expandedDrawY = ((float) this.expandedBounds.top) - this.textPaint.ascent();
        } else if (i2 != 80) {
            this.expandedDrawY = ((float) this.expandedBounds.centerY()) + (((this.textPaint.descent() - this.textPaint.ascent()) / 2.0f) - this.textPaint.descent());
        } else {
            this.expandedDrawY = (float) this.expandedBounds.bottom;
        }
        absoluteGravity2 &= GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        if (absoluteGravity2 == 1) {
            this.expandedDrawX = ((float) this.expandedBounds.centerX()) - (f2 / 2.0f);
        } else if (absoluteGravity2 != 5) {
            this.expandedDrawX = (float) this.expandedBounds.left;
        } else {
            this.expandedDrawX = ((float) this.expandedBounds.right) - f2;
        }
        clearTexture();
        setInterpolatedTextSize(f);
    }

    private void interpolateBounds(float f) {
        this.currentBounds.left = lerp((float) this.expandedBounds.left, (float) this.collapsedBounds.left, f, this.positionInterpolator);
        this.currentBounds.top = lerp(this.expandedDrawY, this.collapsedDrawY, f, this.positionInterpolator);
        this.currentBounds.right = lerp((float) this.expandedBounds.right, (float) this.collapsedBounds.right, f, this.positionInterpolator);
        this.currentBounds.bottom = lerp((float) this.expandedBounds.bottom, (float) this.collapsedBounds.bottom, f, this.positionInterpolator);
    }

    public void draw(Canvas canvas) {
        int save = canvas.save();
        if (this.textToDraw != null && this.drawTitle) {
            float f;
            float f2 = this.currentDrawX;
            float f3 = this.currentDrawY;
            Object obj = (!this.useTexture || this.expandedTitleTexture == null) ? null : 1;
            float f4;
            if (obj != null) {
                f = this.textureAscent * this.scale;
                f4 = this.textureDescent;
                f4 = this.scale;
            } else {
                f = this.textPaint.ascent() * this.scale;
                this.textPaint.descent();
                f4 = this.scale;
            }
            if (obj != null) {
                f3 += f;
            }
            float f5 = f3;
            if (this.scale != 1.0f) {
                canvas.scale(this.scale, this.scale, f2, f5);
            }
            if (obj != null) {
                canvas.drawBitmap(this.expandedTitleTexture, f2, f5, this.texturePaint);
            } else {
                canvas.drawText(this.textToDraw, 0, this.textToDraw.length(), f2, f5, this.textPaint);
            }
        }
        canvas.restoreToCount(save);
    }

    private boolean calculateIsRtl(CharSequence charSequence) {
        Object obj = 1;
        if (ViewCompat.getLayoutDirection(this.view) != 1) {
            obj = null;
        }
        return (obj != null ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL : TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR).isRtl(charSequence, 0, charSequence.length());
    }

    private void setInterpolatedTextSize(float f) {
        calculateUsingTextSize(f);
        boolean z = USE_SCALING_TEXTURE && this.scale != 1.0f;
        this.useTexture = z;
        if (this.useTexture) {
            ensureExpandedTexture();
        }
        ViewCompat.postInvalidateOnAnimation(this.view);
    }

    private void calculateUsingTextSize(float f) {
        if (this.text != null) {
            float f2;
            Object obj;
            float width = (float) this.collapsedBounds.width();
            float width2 = (float) this.expandedBounds.width();
            boolean z = true;
            if (isClose(f, this.collapsedTextSize)) {
                Object obj2;
                f = this.collapsedTextSize;
                this.scale = 1.0f;
                if (this.currentTypeface != this.collapsedTypeface) {
                    this.currentTypeface = this.collapsedTypeface;
                    obj2 = 1;
                } else {
                    obj2 = null;
                }
                f2 = f;
                obj = obj2;
            } else {
                f2 = this.expandedTextSize;
                if (this.currentTypeface != this.expandedTypeface) {
                    this.currentTypeface = this.expandedTypeface;
                    obj = 1;
                } else {
                    obj = null;
                }
                if (isClose(f, this.expandedTextSize)) {
                    this.scale = 1.0f;
                } else {
                    this.scale = f / this.expandedTextSize;
                }
                f = this.collapsedTextSize / this.expandedTextSize;
                width = width2 * f > width ? Math.min(width / f, width2) : width2;
            }
            if (width > 0.0f) {
                obj = (this.currentTextSize == f2 && !this.boundsChanged && obj == null) ? null : 1;
                this.currentTextSize = f2;
                this.boundsChanged = false;
            }
            if (this.textToDraw == null || r6 != null) {
                this.textPaint.setTextSize(this.currentTextSize);
                this.textPaint.setTypeface(this.currentTypeface);
                TextPaint textPaint = this.textPaint;
                if (this.scale == 1.0f) {
                    z = false;
                }
                textPaint.setLinearText(z);
                CharSequence ellipsize = TextUtils.ellipsize(this.text, this.textPaint, width, TruncateAt.END);
                if (!TextUtils.equals(ellipsize, this.textToDraw)) {
                    this.textToDraw = ellipsize;
                    this.isRtl = calculateIsRtl(this.textToDraw);
                }
            }
        }
    }

    private void ensureExpandedTexture() {
        if (this.expandedTitleTexture == null && !this.expandedBounds.isEmpty() && !TextUtils.isEmpty(this.textToDraw)) {
            calculateOffsets(0.0f);
            this.textureAscent = this.textPaint.ascent();
            this.textureDescent = this.textPaint.descent();
            int round = Math.round(this.textPaint.measureText(this.textToDraw, 0, this.textToDraw.length()));
            int round2 = Math.round(this.textureDescent - this.textureAscent);
            if (round > 0 && round2 > 0) {
                this.expandedTitleTexture = Bitmap.createBitmap(round, round2, Config.ARGB_8888);
                new Canvas(this.expandedTitleTexture).drawText(this.textToDraw, 0, this.textToDraw.length(), 0.0f, ((float) round2) - this.textPaint.descent(), this.textPaint);
                if (this.texturePaint == null) {
                    this.texturePaint = new Paint(3);
                }
            }
        }
    }

    public void recalculate() {
        if (this.view.getHeight() > 0 && this.view.getWidth() > 0) {
            calculateBaseOffsets();
            calculateCurrentOffsets();
        }
    }

    public void setText(CharSequence charSequence) {
        if (charSequence == null || !charSequence.equals(this.text)) {
            this.text = charSequence;
            this.textToDraw = null;
            clearTexture();
            recalculate();
        }
    }

    public CharSequence getText() {
        return this.text;
    }

    private void clearTexture() {
        if (this.expandedTitleTexture != null) {
            this.expandedTitleTexture.recycle();
            this.expandedTitleTexture = null;
        }
    }

    private static boolean isClose(float f, float f2) {
        return Math.abs(f - f2) < 0.001f;
    }

    public ColorStateList getExpandedTextColor() {
        return this.expandedTextColor;
    }

    public ColorStateList getCollapsedTextColor() {
        return this.collapsedTextColor;
    }

    private static int blendColors(int i, int i2, float f) {
        float f2 = 1.0f - f;
        return Color.argb((int) ((((float) Color.alpha(i)) * f2) + (((float) Color.alpha(i2)) * f)), (int) ((((float) Color.red(i)) * f2) + (((float) Color.red(i2)) * f)), (int) ((((float) Color.green(i)) * f2) + (((float) Color.green(i2)) * f)), (int) ((((float) Color.blue(i)) * f2) + (((float) Color.blue(i2)) * f)));
    }

    private static float lerp(float f, float f2, float f3, TimeInterpolator timeInterpolator) {
        if (timeInterpolator != null) {
            f3 = timeInterpolator.getInterpolation(f3);
        }
        return AnimationUtils.lerp(f, f2, f3);
    }

    private static boolean rectEquals(Rect rect, int i, int i2, int i3, int i4) {
        return rect.left == i && rect.top == i2 && rect.right == i3 && rect.bottom == i4;
    }
}
