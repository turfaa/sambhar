package android.support.design.chip;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.support.annotation.AnimatorRes;
import android.support.annotation.AttrRes;
import android.support.annotation.BoolRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.annotation.XmlRes;
import android.support.design.R;
import android.support.design.animation.MotionSpec;
import android.support.design.canvas.CanvasCompat;
import android.support.design.drawable.DrawableUtils;
import android.support.design.internal.ThemeEnforcement;
import android.support.design.resources.MaterialResources;
import android.support.design.resources.TextAppearance;
import android.support.design.ripple.RippleUtils;
import android.support.v4.content.res.ResourcesCompat.FontCallback;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.graphics.drawable.TintAwareDrawable;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.text.BidiFormatter;
import android.support.v4.view.ViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Xml;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParserException;

public class ChipDrawable extends Drawable implements TintAwareDrawable, Callback {
    private static final boolean DEBUG = false;
    private static final int[] DEFAULT_STATE = new int[]{16842910};
    private static final String NAMESPACE_APP = "http://schemas.android.com/apk/res-auto";
    private int alpha = 255;
    private boolean checkable;
    @Nullable
    private Drawable checkedIcon;
    private boolean checkedIconVisible;
    @Nullable
    private ColorStateList chipBackgroundColor;
    private float chipCornerRadius;
    private float chipEndPadding;
    @Nullable
    private Drawable chipIcon;
    private float chipIconSize;
    @Nullable
    private ColorStateList chipIconTint;
    private boolean chipIconVisible;
    private float chipMinHeight;
    private final Paint chipPaint = new Paint(1);
    private float chipStartPadding;
    @Nullable
    private ColorStateList chipStrokeColor;
    private float chipStrokeWidth;
    @Nullable
    private Drawable closeIcon;
    @Nullable
    private CharSequence closeIconContentDescription;
    private float closeIconEndPadding;
    private float closeIconSize;
    private float closeIconStartPadding;
    private int[] closeIconStateSet;
    @Nullable
    private ColorStateList closeIconTint;
    private boolean closeIconVisible;
    @Nullable
    private ColorFilter colorFilter;
    @Nullable
    private ColorStateList compatRippleColor;
    private final Context context;
    private boolean currentChecked;
    @ColorInt
    private int currentChipBackgroundColor;
    @ColorInt
    private int currentChipStrokeColor;
    @ColorInt
    private int currentCompatRippleColor;
    @ColorInt
    private int currentTextColor;
    @ColorInt
    private int currentTint;
    @Nullable
    private final Paint debugPaint;
    private WeakReference<Delegate> delegate = new WeakReference(null);
    private final FontCallback fontCallback = new FontCallback() {
        public void onFontRetrievalFailed(int i) {
        }

        public void onFontRetrieved(@NonNull Typeface typeface) {
            ChipDrawable.this.textWidthDirty = true;
            ChipDrawable.this.onSizeChange();
            ChipDrawable.this.invalidateSelf();
        }
    };
    private final FontMetrics fontMetrics = new FontMetrics();
    @Nullable
    private MotionSpec hideMotionSpec;
    private float iconEndPadding;
    private float iconStartPadding;
    private int maxWidth;
    private final PointF pointF = new PointF();
    @Nullable
    private CharSequence rawText;
    private final RectF rectF = new RectF();
    @Nullable
    private ColorStateList rippleColor;
    private boolean shouldDrawText;
    @Nullable
    private MotionSpec showMotionSpec;
    @Nullable
    private TextAppearance textAppearance;
    private float textEndPadding;
    private final TextPaint textPaint = new TextPaint(1);
    private float textStartPadding;
    private float textWidth;
    private boolean textWidthDirty = true;
    @Nullable
    private ColorStateList tint;
    @Nullable
    private PorterDuffColorFilter tintFilter;
    @Nullable
    private Mode tintMode = Mode.SRC_IN;
    private TruncateAt truncateAt;
    @Nullable
    private CharSequence unicodeWrappedText;
    private boolean useCompatRipple;

    public interface Delegate {
        void onChipDrawableSizeChange();
    }

    public int getOpacity() {
        return -3;
    }

    public static ChipDrawable createFromAttributes(Context context, AttributeSet attributeSet, @AttrRes int i, @StyleRes int i2) {
        ChipDrawable chipDrawable = new ChipDrawable(context);
        chipDrawable.loadFromAttributes(attributeSet, i, i2);
        return chipDrawable;
    }

    public static ChipDrawable createFromResource(Context context, @XmlRes int i) {
        try {
            int next;
            XmlResourceParser xml = context.getResources().getXml(i);
            do {
                next = xml.next();
                if (next == 2) {
                    break;
                }
            } while (next != 1);
            if (next != 2) {
                throw new XmlPullParserException("No start tag found");
            } else if (TextUtils.equals(xml.getName(), "chip")) {
                AttributeSet asAttributeSet = Xml.asAttributeSet(xml);
                next = asAttributeSet.getStyleAttribute();
                if (next == 0) {
                    next = R.style.Widget_MaterialComponents_Chip_Entry;
                }
                return createFromAttributes(context, asAttributeSet, R.attr.chipStandaloneStyle, next);
            } else {
                throw new XmlPullParserException("Must have a <chip> start tag");
            }
        } catch (IOException | XmlPullParserException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Can't load chip resource ID #0x");
            stringBuilder.append(Integer.toHexString(i));
            NotFoundException notFoundException = new NotFoundException(stringBuilder.toString());
            notFoundException.initCause(e);
            throw notFoundException;
        }
    }

    private ChipDrawable(Context context) {
        this.context = context;
        this.rawText = "";
        this.textPaint.density = context.getResources().getDisplayMetrics().density;
        this.debugPaint = null;
        if (this.debugPaint != null) {
            this.debugPaint.setStyle(Style.STROKE);
        }
        setState(DEFAULT_STATE);
        setCloseIconState(DEFAULT_STATE);
        this.shouldDrawText = true;
    }

    private void loadFromAttributes(AttributeSet attributeSet, @AttrRes int i, @StyleRes int i2) {
        TypedArray obtainStyledAttributes = ThemeEnforcement.obtainStyledAttributes(this.context, attributeSet, R.styleable.Chip, i, i2, new int[0]);
        setChipBackgroundColor(MaterialResources.getColorStateList(this.context, obtainStyledAttributes, R.styleable.Chip_chipBackgroundColor));
        setChipMinHeight(obtainStyledAttributes.getDimension(R.styleable.Chip_chipMinHeight, 0.0f));
        setChipCornerRadius(obtainStyledAttributes.getDimension(R.styleable.Chip_chipCornerRadius, 0.0f));
        setChipStrokeColor(MaterialResources.getColorStateList(this.context, obtainStyledAttributes, R.styleable.Chip_chipStrokeColor));
        setChipStrokeWidth(obtainStyledAttributes.getDimension(R.styleable.Chip_chipStrokeWidth, 0.0f));
        setRippleColor(MaterialResources.getColorStateList(this.context, obtainStyledAttributes, R.styleable.Chip_rippleColor));
        setText(obtainStyledAttributes.getText(R.styleable.Chip_android_text));
        setTextAppearance(MaterialResources.getTextAppearance(this.context, obtainStyledAttributes, R.styleable.Chip_android_textAppearance));
        switch (obtainStyledAttributes.getInt(R.styleable.Chip_android_ellipsize, 0)) {
            case 1:
                setEllipsize(TruncateAt.START);
                break;
            case 2:
                setEllipsize(TruncateAt.MIDDLE);
                break;
            case 3:
                setEllipsize(TruncateAt.END);
                break;
        }
        setChipIconVisible(obtainStyledAttributes.getBoolean(R.styleable.Chip_chipIconVisible, false));
        if (!(attributeSet == null || attributeSet.getAttributeValue(NAMESPACE_APP, "chipIconEnabled") == null || attributeSet.getAttributeValue(NAMESPACE_APP, "chipIconVisible") != null)) {
            setChipIconVisible(obtainStyledAttributes.getBoolean(R.styleable.Chip_chipIconEnabled, false));
        }
        setChipIcon(MaterialResources.getDrawable(this.context, obtainStyledAttributes, R.styleable.Chip_chipIcon));
        setChipIconTint(MaterialResources.getColorStateList(this.context, obtainStyledAttributes, R.styleable.Chip_chipIconTint));
        setChipIconSize(obtainStyledAttributes.getDimension(R.styleable.Chip_chipIconSize, 0.0f));
        setCloseIconVisible(obtainStyledAttributes.getBoolean(R.styleable.Chip_closeIconVisible, false));
        if (!(attributeSet == null || attributeSet.getAttributeValue(NAMESPACE_APP, "closeIconEnabled") == null || attributeSet.getAttributeValue(NAMESPACE_APP, "closeIconVisible") != null)) {
            setCloseIconVisible(obtainStyledAttributes.getBoolean(R.styleable.Chip_closeIconEnabled, false));
        }
        setCloseIcon(MaterialResources.getDrawable(this.context, obtainStyledAttributes, R.styleable.Chip_closeIcon));
        setCloseIconTint(MaterialResources.getColorStateList(this.context, obtainStyledAttributes, R.styleable.Chip_closeIconTint));
        setCloseIconSize(obtainStyledAttributes.getDimension(R.styleable.Chip_closeIconSize, 0.0f));
        setCheckable(obtainStyledAttributes.getBoolean(R.styleable.Chip_android_checkable, false));
        setCheckedIconVisible(obtainStyledAttributes.getBoolean(R.styleable.Chip_checkedIconVisible, false));
        if (!(attributeSet == null || attributeSet.getAttributeValue(NAMESPACE_APP, "checkedIconEnabled") == null || attributeSet.getAttributeValue(NAMESPACE_APP, "checkedIconVisible") != null)) {
            setCheckedIconVisible(obtainStyledAttributes.getBoolean(R.styleable.Chip_checkedIconEnabled, false));
        }
        setCheckedIcon(MaterialResources.getDrawable(this.context, obtainStyledAttributes, R.styleable.Chip_checkedIcon));
        setShowMotionSpec(MotionSpec.createFromAttribute(this.context, obtainStyledAttributes, R.styleable.Chip_showMotionSpec));
        setHideMotionSpec(MotionSpec.createFromAttribute(this.context, obtainStyledAttributes, R.styleable.Chip_hideMotionSpec));
        setChipStartPadding(obtainStyledAttributes.getDimension(R.styleable.Chip_chipStartPadding, 0.0f));
        setIconStartPadding(obtainStyledAttributes.getDimension(R.styleable.Chip_iconStartPadding, 0.0f));
        setIconEndPadding(obtainStyledAttributes.getDimension(R.styleable.Chip_iconEndPadding, 0.0f));
        setTextStartPadding(obtainStyledAttributes.getDimension(R.styleable.Chip_textStartPadding, 0.0f));
        setTextEndPadding(obtainStyledAttributes.getDimension(R.styleable.Chip_textEndPadding, 0.0f));
        setCloseIconStartPadding(obtainStyledAttributes.getDimension(R.styleable.Chip_closeIconStartPadding, 0.0f));
        setCloseIconEndPadding(obtainStyledAttributes.getDimension(R.styleable.Chip_closeIconEndPadding, 0.0f));
        setChipEndPadding(obtainStyledAttributes.getDimension(R.styleable.Chip_chipEndPadding, 0.0f));
        setMaxWidth(obtainStyledAttributes.getDimensionPixelSize(R.styleable.Chip_android_maxWidth, ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED));
        obtainStyledAttributes.recycle();
    }

    public void setUseCompatRipple(boolean z) {
        if (this.useCompatRipple != z) {
            this.useCompatRipple = z;
            updateCompatRippleColor();
            onStateChange(getState());
        }
    }

    public boolean getUseCompatRipple() {
        return this.useCompatRipple;
    }

    public void setDelegate(@Nullable Delegate delegate) {
        this.delegate = new WeakReference(delegate);
    }

    /* Access modifiers changed, original: protected */
    public void onSizeChange() {
        Delegate delegate = (Delegate) this.delegate.get();
        if (delegate != null) {
            delegate.onChipDrawableSizeChange();
        }
    }

    public void getChipTouchBounds(RectF rectF) {
        calculateChipTouchBounds(getBounds(), rectF);
    }

    public void getCloseIconTouchBounds(RectF rectF) {
        calculateCloseIconTouchBounds(getBounds(), rectF);
    }

    public int getIntrinsicWidth() {
        return Math.min(Math.round((((((this.chipStartPadding + calculateChipIconWidth()) + this.textStartPadding) + getTextWidth()) + this.textEndPadding) + calculateCloseIconWidth()) + this.chipEndPadding), this.maxWidth);
    }

    public int getIntrinsicHeight() {
        return (int) this.chipMinHeight;
    }

    private boolean showsChipIcon() {
        return this.chipIconVisible && this.chipIcon != null;
    }

    private boolean showsCheckedIcon() {
        return this.checkedIconVisible && this.checkedIcon != null && this.currentChecked;
    }

    private boolean showsCloseIcon() {
        return this.closeIconVisible && this.closeIcon != null;
    }

    private boolean canShowCheckedIcon() {
        return this.checkedIconVisible && this.checkedIcon != null && this.checkable;
    }

    /* Access modifiers changed, original: 0000 */
    public float calculateChipIconWidth() {
        if (showsChipIcon() || showsCheckedIcon()) {
            return (this.iconStartPadding + this.chipIconSize) + this.iconEndPadding;
        }
        return 0.0f;
    }

    private float getTextWidth() {
        if (!this.textWidthDirty) {
            return this.textWidth;
        }
        this.textWidth = calculateTextWidth(this.unicodeWrappedText);
        this.textWidthDirty = false;
        return this.textWidth;
    }

    private float calculateTextWidth(@Nullable CharSequence charSequence) {
        return charSequence == null ? 0.0f : this.textPaint.measureText(charSequence, 0, charSequence.length());
    }

    private float calculateCloseIconWidth() {
        return showsCloseIcon() ? (this.closeIconStartPadding + this.closeIconSize) + this.closeIconEndPadding : 0.0f;
    }

    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (!bounds.isEmpty() && getAlpha() != 0) {
            int i = 0;
            if (this.alpha < 255) {
                i = CanvasCompat.saveLayerAlpha(canvas, (float) bounds.left, (float) bounds.top, (float) bounds.right, (float) bounds.bottom, this.alpha);
            }
            drawChipBackground(canvas, bounds);
            drawChipStroke(canvas, bounds);
            drawCompatRipple(canvas, bounds);
            drawChipIcon(canvas, bounds);
            drawCheckedIcon(canvas, bounds);
            if (this.shouldDrawText) {
                drawText(canvas, bounds);
            }
            drawCloseIcon(canvas, bounds);
            drawDebug(canvas, bounds);
            if (this.alpha < 255) {
                canvas.restoreToCount(i);
            }
        }
    }

    private void drawChipBackground(@NonNull Canvas canvas, Rect rect) {
        this.chipPaint.setColor(this.currentChipBackgroundColor);
        this.chipPaint.setStyle(Style.FILL);
        this.chipPaint.setColorFilter(getTintColorFilter());
        this.rectF.set(rect);
        canvas.drawRoundRect(this.rectF, this.chipCornerRadius, this.chipCornerRadius, this.chipPaint);
    }

    private void drawChipStroke(@NonNull Canvas canvas, Rect rect) {
        if (this.chipStrokeWidth > 0.0f) {
            this.chipPaint.setColor(this.currentChipStrokeColor);
            this.chipPaint.setStyle(Style.STROKE);
            this.chipPaint.setColorFilter(getTintColorFilter());
            this.rectF.set(((float) rect.left) + (this.chipStrokeWidth / 2.0f), ((float) rect.top) + (this.chipStrokeWidth / 2.0f), ((float) rect.right) - (this.chipStrokeWidth / 2.0f), ((float) rect.bottom) - (this.chipStrokeWidth / 2.0f));
            float f = this.chipCornerRadius - (this.chipStrokeWidth / 2.0f);
            canvas.drawRoundRect(this.rectF, f, f, this.chipPaint);
        }
    }

    private void drawCompatRipple(@NonNull Canvas canvas, Rect rect) {
        this.chipPaint.setColor(this.currentCompatRippleColor);
        this.chipPaint.setStyle(Style.FILL);
        this.rectF.set(rect);
        canvas.drawRoundRect(this.rectF, this.chipCornerRadius, this.chipCornerRadius, this.chipPaint);
    }

    private void drawChipIcon(@NonNull Canvas canvas, Rect rect) {
        if (showsChipIcon()) {
            calculateChipIconBounds(rect, this.rectF);
            float f = this.rectF.left;
            float f2 = this.rectF.top;
            canvas.translate(f, f2);
            this.chipIcon.setBounds(0, 0, (int) this.rectF.width(), (int) this.rectF.height());
            this.chipIcon.draw(canvas);
            canvas.translate(-f, -f2);
        }
    }

    private void drawCheckedIcon(@NonNull Canvas canvas, Rect rect) {
        if (showsCheckedIcon()) {
            calculateChipIconBounds(rect, this.rectF);
            float f = this.rectF.left;
            float f2 = this.rectF.top;
            canvas.translate(f, f2);
            this.checkedIcon.setBounds(0, 0, (int) this.rectF.width(), (int) this.rectF.height());
            this.checkedIcon.draw(canvas);
            canvas.translate(-f, -f2);
        }
    }

    private void drawText(@NonNull Canvas canvas, Rect rect) {
        if (this.unicodeWrappedText != null) {
            Align calculateTextOriginAndAlignment = calculateTextOriginAndAlignment(rect, this.pointF);
            calculateTextBounds(rect, this.rectF);
            if (this.textAppearance != null) {
                this.textPaint.drawableState = getState();
                this.textAppearance.updateDrawState(this.context, this.textPaint, this.fontCallback);
            }
            this.textPaint.setTextAlign(calculateTextOriginAndAlignment);
            int i = 0;
            Object obj = Math.round(getTextWidth()) > Math.round(this.rectF.width()) ? 1 : null;
            if (obj != null) {
                i = canvas.save();
                canvas.clipRect(this.rectF);
            }
            CharSequence charSequence = this.unicodeWrappedText;
            if (!(obj == null || this.truncateAt == null)) {
                charSequence = TextUtils.ellipsize(this.unicodeWrappedText, this.textPaint, this.rectF.width(), this.truncateAt);
            }
            CharSequence charSequence2 = charSequence;
            canvas.drawText(charSequence2, 0, charSequence2.length(), this.pointF.x, this.pointF.y, this.textPaint);
            if (obj != null) {
                canvas.restoreToCount(i);
            }
        }
    }

    private void drawCloseIcon(@NonNull Canvas canvas, Rect rect) {
        if (showsCloseIcon()) {
            calculateCloseIconBounds(rect, this.rectF);
            float f = this.rectF.left;
            float f2 = this.rectF.top;
            canvas.translate(f, f2);
            this.closeIcon.setBounds(0, 0, (int) this.rectF.width(), (int) this.rectF.height());
            this.closeIcon.draw(canvas);
            canvas.translate(-f, -f2);
        }
    }

    private void drawDebug(@NonNull Canvas canvas, Rect rect) {
        if (this.debugPaint != null) {
            this.debugPaint.setColor(ColorUtils.setAlphaComponent(ViewCompat.MEASURED_STATE_MASK, 127));
            canvas.drawRect(rect, this.debugPaint);
            if (showsChipIcon() || showsCheckedIcon()) {
                calculateChipIconBounds(rect, this.rectF);
                canvas.drawRect(this.rectF, this.debugPaint);
            }
            if (this.unicodeWrappedText != null) {
                canvas.drawLine((float) rect.left, rect.exactCenterY(), (float) rect.right, rect.exactCenterY(), this.debugPaint);
            }
            if (showsCloseIcon()) {
                calculateCloseIconBounds(rect, this.rectF);
                canvas.drawRect(this.rectF, this.debugPaint);
            }
            this.debugPaint.setColor(ColorUtils.setAlphaComponent(SupportMenu.CATEGORY_MASK, 127));
            calculateChipTouchBounds(rect, this.rectF);
            canvas.drawRect(this.rectF, this.debugPaint);
            this.debugPaint.setColor(ColorUtils.setAlphaComponent(-16711936, 127));
            calculateCloseIconTouchBounds(rect, this.rectF);
            canvas.drawRect(this.rectF, this.debugPaint);
        }
    }

    private void calculateChipIconBounds(Rect rect, RectF rectF) {
        rectF.setEmpty();
        if (showsChipIcon() || showsCheckedIcon()) {
            float f = this.chipStartPadding + this.iconStartPadding;
            if (DrawableCompat.getLayoutDirection(this) == 0) {
                rectF.left = ((float) rect.left) + f;
                rectF.right = rectF.left + this.chipIconSize;
            } else {
                rectF.right = ((float) rect.right) - f;
                rectF.left = rectF.right - this.chipIconSize;
            }
            rectF.top = rect.exactCenterY() - (this.chipIconSize / 2.0f);
            rectF.bottom = rectF.top + this.chipIconSize;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public Align calculateTextOriginAndAlignment(Rect rect, PointF pointF) {
        pointF.set(0.0f, 0.0f);
        Align align = Align.LEFT;
        if (this.unicodeWrappedText != null) {
            float calculateChipIconWidth = (this.chipStartPadding + calculateChipIconWidth()) + this.textStartPadding;
            if (DrawableCompat.getLayoutDirection(this) == 0) {
                pointF.x = ((float) rect.left) + calculateChipIconWidth;
                align = Align.LEFT;
            } else {
                pointF.x = ((float) rect.right) - calculateChipIconWidth;
                align = Align.RIGHT;
            }
            pointF.y = ((float) rect.centerY()) - calculateTextCenterFromBaseline();
        }
        return align;
    }

    private float calculateTextCenterFromBaseline() {
        this.textPaint.getFontMetrics(this.fontMetrics);
        return (this.fontMetrics.descent + this.fontMetrics.ascent) / 2.0f;
    }

    private void calculateTextBounds(Rect rect, RectF rectF) {
        rectF.setEmpty();
        if (this.unicodeWrappedText != null) {
            float calculateChipIconWidth = (this.chipStartPadding + calculateChipIconWidth()) + this.textStartPadding;
            float calculateCloseIconWidth = (this.chipEndPadding + calculateCloseIconWidth()) + this.textEndPadding;
            if (DrawableCompat.getLayoutDirection(this) == 0) {
                rectF.left = ((float) rect.left) + calculateChipIconWidth;
                rectF.right = ((float) rect.right) - calculateCloseIconWidth;
            } else {
                rectF.left = ((float) rect.left) + calculateCloseIconWidth;
                rectF.right = ((float) rect.right) - calculateChipIconWidth;
            }
            rectF.top = (float) rect.top;
            rectF.bottom = (float) rect.bottom;
        }
    }

    private void calculateCloseIconBounds(Rect rect, RectF rectF) {
        rectF.setEmpty();
        if (showsCloseIcon()) {
            float f = this.chipEndPadding + this.closeIconEndPadding;
            if (DrawableCompat.getLayoutDirection(this) == 0) {
                rectF.right = ((float) rect.right) - f;
                rectF.left = rectF.right - this.closeIconSize;
            } else {
                rectF.left = ((float) rect.left) + f;
                rectF.right = rectF.left + this.closeIconSize;
            }
            rectF.top = rect.exactCenterY() - (this.closeIconSize / 2.0f);
            rectF.bottom = rectF.top + this.closeIconSize;
        }
    }

    private void calculateChipTouchBounds(Rect rect, RectF rectF) {
        rectF.set(rect);
        if (showsCloseIcon()) {
            float f = (((this.chipEndPadding + this.closeIconEndPadding) + this.closeIconSize) + this.closeIconStartPadding) + this.textEndPadding;
            if (DrawableCompat.getLayoutDirection(this) == 0) {
                rectF.right = ((float) rect.right) - f;
            } else {
                rectF.left = ((float) rect.left) + f;
            }
        }
    }

    private void calculateCloseIconTouchBounds(Rect rect, RectF rectF) {
        rectF.setEmpty();
        if (showsCloseIcon()) {
            float f = (((this.chipEndPadding + this.closeIconEndPadding) + this.closeIconSize) + this.closeIconStartPadding) + this.textEndPadding;
            if (DrawableCompat.getLayoutDirection(this) == 0) {
                rectF.right = (float) rect.right;
                rectF.left = rectF.right - f;
            } else {
                rectF.left = (float) rect.left;
                rectF.right = ((float) rect.left) + f;
            }
            rectF.top = (float) rect.top;
            rectF.bottom = (float) rect.bottom;
        }
    }

    public boolean isStateful() {
        return isStateful(this.chipBackgroundColor) || isStateful(this.chipStrokeColor) || ((this.useCompatRipple && isStateful(this.compatRippleColor)) || isStateful(this.textAppearance) || canShowCheckedIcon() || isStateful(this.chipIcon) || isStateful(this.checkedIcon) || isStateful(this.tint));
    }

    public boolean isCloseIconStateful() {
        return isStateful(this.closeIcon);
    }

    public boolean setCloseIconState(@NonNull int[] iArr) {
        if (!Arrays.equals(this.closeIconStateSet, iArr)) {
            this.closeIconStateSet = iArr;
            if (showsCloseIcon()) {
                return onStateChange(getState(), iArr);
            }
        }
        return false;
    }

    @NonNull
    public int[] getCloseIconState() {
        return this.closeIconStateSet;
    }

    /* Access modifiers changed, original: protected */
    public boolean onStateChange(int[] iArr) {
        return onStateChange(iArr, getCloseIconState());
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x0099  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00ba  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00c9  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00d8  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00e1  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00e6  */
    private boolean onStateChange(int[] r6, int[] r7) {
        /*
        r5 = this;
        r0 = super.onStateChange(r6);
        r1 = r5.chipBackgroundColor;
        r2 = 0;
        if (r1 == 0) goto L_0x0012;
    L_0x0009:
        r1 = r5.chipBackgroundColor;
        r3 = r5.currentChipBackgroundColor;
        r1 = r1.getColorForState(r6, r3);
        goto L_0x0013;
    L_0x0012:
        r1 = 0;
    L_0x0013:
        r3 = r5.currentChipBackgroundColor;
        r4 = 1;
        if (r3 == r1) goto L_0x001b;
    L_0x0018:
        r5.currentChipBackgroundColor = r1;
        r0 = 1;
    L_0x001b:
        r1 = r5.chipStrokeColor;
        if (r1 == 0) goto L_0x0028;
    L_0x001f:
        r1 = r5.chipStrokeColor;
        r3 = r5.currentChipStrokeColor;
        r1 = r1.getColorForState(r6, r3);
        goto L_0x0029;
    L_0x0028:
        r1 = 0;
    L_0x0029:
        r3 = r5.currentChipStrokeColor;
        if (r3 == r1) goto L_0x0030;
    L_0x002d:
        r5.currentChipStrokeColor = r1;
        r0 = 1;
    L_0x0030:
        r1 = r5.compatRippleColor;
        if (r1 == 0) goto L_0x003d;
    L_0x0034:
        r1 = r5.compatRippleColor;
        r3 = r5.currentCompatRippleColor;
        r1 = r1.getColorForState(r6, r3);
        goto L_0x003e;
    L_0x003d:
        r1 = 0;
    L_0x003e:
        r3 = r5.currentCompatRippleColor;
        if (r3 == r1) goto L_0x0049;
    L_0x0042:
        r5.currentCompatRippleColor = r1;
        r1 = r5.useCompatRipple;
        if (r1 == 0) goto L_0x0049;
    L_0x0048:
        r0 = 1;
    L_0x0049:
        r1 = r5.textAppearance;
        if (r1 == 0) goto L_0x005e;
    L_0x004d:
        r1 = r5.textAppearance;
        r1 = r1.textColor;
        if (r1 == 0) goto L_0x005e;
    L_0x0053:
        r1 = r5.textAppearance;
        r1 = r1.textColor;
        r3 = r5.currentTextColor;
        r1 = r1.getColorForState(r6, r3);
        goto L_0x005f;
    L_0x005e:
        r1 = 0;
    L_0x005f:
        r3 = r5.currentTextColor;
        if (r3 == r1) goto L_0x0066;
    L_0x0063:
        r5.currentTextColor = r1;
        r0 = 1;
    L_0x0066:
        r1 = r5.getState();
        r3 = 16842912; // 0x10100a0 float:2.3694006E-38 double:8.321504E-317;
        r1 = hasState(r1, r3);
        if (r1 == 0) goto L_0x0079;
    L_0x0073:
        r1 = r5.checkable;
        if (r1 == 0) goto L_0x0079;
    L_0x0077:
        r1 = 1;
        goto L_0x007a;
    L_0x0079:
        r1 = 0;
    L_0x007a:
        r3 = r5.currentChecked;
        if (r3 == r1) goto L_0x0094;
    L_0x007e:
        r3 = r5.checkedIcon;
        if (r3 == 0) goto L_0x0094;
    L_0x0082:
        r0 = r5.calculateChipIconWidth();
        r5.currentChecked = r1;
        r1 = r5.calculateChipIconWidth();
        r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1));
        if (r0 == 0) goto L_0x0093;
    L_0x0090:
        r0 = 1;
        r1 = 1;
        goto L_0x0095;
    L_0x0093:
        r0 = 1;
    L_0x0094:
        r1 = 0;
    L_0x0095:
        r3 = r5.tint;
        if (r3 == 0) goto L_0x00a1;
    L_0x0099:
        r2 = r5.tint;
        r3 = r5.currentTint;
        r2 = r2.getColorForState(r6, r3);
    L_0x00a1:
        r3 = r5.currentTint;
        if (r3 == r2) goto L_0x00b2;
    L_0x00a5:
        r5.currentTint = r2;
        r0 = r5.tint;
        r2 = r5.tintMode;
        r0 = android.support.design.drawable.DrawableUtils.updateTintFilter(r5, r0, r2);
        r5.tintFilter = r0;
        r0 = 1;
    L_0x00b2:
        r2 = r5.chipIcon;
        r2 = isStateful(r2);
        if (r2 == 0) goto L_0x00c1;
    L_0x00ba:
        r2 = r5.chipIcon;
        r2 = r2.setState(r6);
        r0 = r0 | r2;
    L_0x00c1:
        r2 = r5.checkedIcon;
        r2 = isStateful(r2);
        if (r2 == 0) goto L_0x00d0;
    L_0x00c9:
        r2 = r5.checkedIcon;
        r6 = r2.setState(r6);
        r0 = r0 | r6;
    L_0x00d0:
        r6 = r5.closeIcon;
        r6 = isStateful(r6);
        if (r6 == 0) goto L_0x00df;
    L_0x00d8:
        r6 = r5.closeIcon;
        r6 = r6.setState(r7);
        r0 = r0 | r6;
    L_0x00df:
        if (r0 == 0) goto L_0x00e4;
    L_0x00e1:
        r5.invalidateSelf();
    L_0x00e4:
        if (r1 == 0) goto L_0x00e9;
    L_0x00e6:
        r5.onSizeChange();
    L_0x00e9:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.design.chip.ChipDrawable.onStateChange(int[], int[]):boolean");
    }

    private static boolean isStateful(@Nullable ColorStateList colorStateList) {
        return colorStateList != null && colorStateList.isStateful();
    }

    private static boolean isStateful(@Nullable Drawable drawable) {
        return drawable != null && drawable.isStateful();
    }

    private static boolean isStateful(@Nullable TextAppearance textAppearance) {
        return (textAppearance == null || textAppearance.textColor == null || !textAppearance.textColor.isStateful()) ? false : true;
    }

    @TargetApi(23)
    public boolean onLayoutDirectionChanged(int i) {
        int onLayoutDirectionChanged = super.onLayoutDirectionChanged(i);
        if (showsChipIcon()) {
            onLayoutDirectionChanged |= this.chipIcon.setLayoutDirection(i);
        }
        if (showsCheckedIcon()) {
            onLayoutDirectionChanged |= this.checkedIcon.setLayoutDirection(i);
        }
        if (showsCloseIcon()) {
            onLayoutDirectionChanged |= this.closeIcon.setLayoutDirection(i);
        }
        if (onLayoutDirectionChanged != 0) {
            invalidateSelf();
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public boolean onLevelChange(int i) {
        boolean onLevelChange = super.onLevelChange(i);
        if (showsChipIcon()) {
            onLevelChange |= this.chipIcon.setLevel(i);
        }
        if (showsCheckedIcon()) {
            onLevelChange |= this.checkedIcon.setLevel(i);
        }
        if (showsCloseIcon()) {
            onLevelChange |= this.closeIcon.setLevel(i);
        }
        if (onLevelChange) {
            invalidateSelf();
        }
        return onLevelChange;
    }

    public boolean setVisible(boolean z, boolean z2) {
        boolean visible = super.setVisible(z, z2);
        if (showsChipIcon()) {
            visible |= this.chipIcon.setVisible(z, z2);
        }
        if (showsCheckedIcon()) {
            visible |= this.checkedIcon.setVisible(z, z2);
        }
        if (showsCloseIcon()) {
            visible |= this.closeIcon.setVisible(z, z2);
        }
        if (visible) {
            invalidateSelf();
        }
        return visible;
    }

    public void setAlpha(int i) {
        if (this.alpha != i) {
            this.alpha = i;
            invalidateSelf();
        }
    }

    public int getAlpha() {
        return this.alpha;
    }

    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        if (this.colorFilter != colorFilter) {
            this.colorFilter = colorFilter;
            invalidateSelf();
        }
    }

    @Nullable
    public ColorFilter getColorFilter() {
        return this.colorFilter;
    }

    public void setTintList(@Nullable ColorStateList colorStateList) {
        if (this.tint != colorStateList) {
            this.tint = colorStateList;
            onStateChange(getState());
        }
    }

    public void setTintMode(@NonNull Mode mode) {
        if (this.tintMode != mode) {
            this.tintMode = mode;
            this.tintFilter = DrawableUtils.updateTintFilter(this, this.tint, mode);
            invalidateSelf();
        }
    }

    @TargetApi(21)
    public void getOutline(@NonNull Outline outline) {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) {
            outline.setRoundRect(0, 0, getIntrinsicWidth(), getIntrinsicHeight(), this.chipCornerRadius);
        } else {
            outline.setRoundRect(bounds, this.chipCornerRadius);
        }
        outline.setAlpha(((float) getAlpha()) / 255.0f);
    }

    public void invalidateDrawable(@NonNull Drawable drawable) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    public void scheduleDrawable(@NonNull Drawable drawable, @NonNull Runnable runnable, long j) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.scheduleDrawable(this, runnable, j);
        }
    }

    public void unscheduleDrawable(@NonNull Drawable drawable, @NonNull Runnable runnable) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.unscheduleDrawable(this, runnable);
        }
    }

    private void unapplyChildDrawable(@Nullable Drawable drawable) {
        if (drawable != null) {
            drawable.setCallback(null);
        }
    }

    private void applyChildDrawable(@Nullable Drawable drawable) {
        if (drawable != null) {
            drawable.setCallback(this);
            DrawableCompat.setLayoutDirection(drawable, DrawableCompat.getLayoutDirection(this));
            drawable.setLevel(getLevel());
            drawable.setVisible(isVisible(), false);
            if (drawable == this.closeIcon) {
                if (drawable.isStateful()) {
                    drawable.setState(getCloseIconState());
                }
                DrawableCompat.setTintList(drawable, this.closeIconTint);
            } else if (drawable.isStateful()) {
                drawable.setState(getState());
            }
        }
    }

    @Nullable
    private ColorFilter getTintColorFilter() {
        return this.colorFilter != null ? this.colorFilter : this.tintFilter;
    }

    private void updateCompatRippleColor() {
        this.compatRippleColor = this.useCompatRipple ? RippleUtils.convertToRippleDrawableColor(this.rippleColor) : null;
    }

    private static boolean hasState(@Nullable int[] iArr, @AttrRes int i) {
        if (iArr == null) {
            return false;
        }
        for (int i2 : iArr) {
            if (i2 == i) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public ColorStateList getChipBackgroundColor() {
        return this.chipBackgroundColor;
    }

    public void setChipBackgroundColorResource(@ColorRes int i) {
        setChipBackgroundColor(AppCompatResources.getColorStateList(this.context, i));
    }

    public void setChipBackgroundColor(@Nullable ColorStateList colorStateList) {
        if (this.chipBackgroundColor != colorStateList) {
            this.chipBackgroundColor = colorStateList;
            onStateChange(getState());
        }
    }

    public float getChipMinHeight() {
        return this.chipMinHeight;
    }

    public void setChipMinHeightResource(@DimenRes int i) {
        setChipMinHeight(this.context.getResources().getDimension(i));
    }

    public void setChipMinHeight(float f) {
        if (this.chipMinHeight != f) {
            this.chipMinHeight = f;
            invalidateSelf();
            onSizeChange();
        }
    }

    public float getChipCornerRadius() {
        return this.chipCornerRadius;
    }

    public void setChipCornerRadiusResource(@DimenRes int i) {
        setChipCornerRadius(this.context.getResources().getDimension(i));
    }

    public void setChipCornerRadius(float f) {
        if (this.chipCornerRadius != f) {
            this.chipCornerRadius = f;
            invalidateSelf();
        }
    }

    @Nullable
    public ColorStateList getChipStrokeColor() {
        return this.chipStrokeColor;
    }

    public void setChipStrokeColorResource(@ColorRes int i) {
        setChipStrokeColor(AppCompatResources.getColorStateList(this.context, i));
    }

    public void setChipStrokeColor(@Nullable ColorStateList colorStateList) {
        if (this.chipStrokeColor != colorStateList) {
            this.chipStrokeColor = colorStateList;
            onStateChange(getState());
        }
    }

    public float getChipStrokeWidth() {
        return this.chipStrokeWidth;
    }

    public void setChipStrokeWidthResource(@DimenRes int i) {
        setChipStrokeWidth(this.context.getResources().getDimension(i));
    }

    public void setChipStrokeWidth(float f) {
        if (this.chipStrokeWidth != f) {
            this.chipStrokeWidth = f;
            this.chipPaint.setStrokeWidth(f);
            invalidateSelf();
        }
    }

    @Nullable
    public ColorStateList getRippleColor() {
        return this.rippleColor;
    }

    public void setRippleColorResource(@ColorRes int i) {
        setRippleColor(AppCompatResources.getColorStateList(this.context, i));
    }

    public void setRippleColor(@Nullable ColorStateList colorStateList) {
        if (this.rippleColor != colorStateList) {
            this.rippleColor = colorStateList;
            updateCompatRippleColor();
            onStateChange(getState());
        }
    }

    @NonNull
    public CharSequence getText() {
        return this.rawText;
    }

    public void setTextResource(@StringRes int i) {
        setText(this.context.getResources().getString(i));
    }

    public void setText(@Nullable CharSequence charSequence) {
        if (charSequence == null) {
            charSequence = "";
        }
        if (this.rawText != charSequence) {
            this.rawText = charSequence;
            this.unicodeWrappedText = BidiFormatter.getInstance().unicodeWrap(charSequence);
            this.textWidthDirty = true;
            invalidateSelf();
            onSizeChange();
        }
    }

    @Nullable
    public TextAppearance getTextAppearance() {
        return this.textAppearance;
    }

    public void setTextAppearanceResource(@StyleRes int i) {
        setTextAppearance(new TextAppearance(this.context, i));
    }

    public void setTextAppearance(@Nullable TextAppearance textAppearance) {
        if (this.textAppearance != textAppearance) {
            this.textAppearance = textAppearance;
            if (textAppearance != null) {
                textAppearance.updateMeasureState(this.context, this.textPaint, this.fontCallback);
                this.textWidthDirty = true;
            }
            onStateChange(getState());
            onSizeChange();
        }
    }

    public TruncateAt getEllipsize() {
        return this.truncateAt;
    }

    public void setEllipsize(@Nullable TruncateAt truncateAt) {
        this.truncateAt = truncateAt;
    }

    public boolean isChipIconVisible() {
        return this.chipIconVisible;
    }

    @Deprecated
    public boolean isChipIconEnabled() {
        return isChipIconVisible();
    }

    public void setChipIconVisible(@BoolRes int i) {
        setChipIconVisible(this.context.getResources().getBoolean(i));
    }

    public void setChipIconVisible(boolean z) {
        if (this.chipIconVisible != z) {
            boolean showsChipIcon = showsChipIcon();
            this.chipIconVisible = z;
            z = showsChipIcon();
            if ((showsChipIcon != z ? 1 : null) != null) {
                if (z) {
                    applyChildDrawable(this.chipIcon);
                } else {
                    unapplyChildDrawable(this.chipIcon);
                }
                invalidateSelf();
                onSizeChange();
            }
        }
    }

    @Deprecated
    public void setChipIconEnabledResource(@BoolRes int i) {
        setChipIconVisible(i);
    }

    @Deprecated
    public void setChipIconEnabled(boolean z) {
        setChipIconVisible(z);
    }

    @Nullable
    public Drawable getChipIcon() {
        return this.chipIcon != null ? DrawableCompat.unwrap(this.chipIcon) : null;
    }

    public void setChipIconResource(@DrawableRes int i) {
        setChipIcon(AppCompatResources.getDrawable(this.context, i));
    }

    public void setChipIcon(@Nullable Drawable drawable) {
        Drawable chipIcon = getChipIcon();
        if (chipIcon != drawable) {
            float calculateChipIconWidth = calculateChipIconWidth();
            this.chipIcon = drawable != null ? DrawableCompat.wrap(drawable).mutate() : null;
            float calculateChipIconWidth2 = calculateChipIconWidth();
            unapplyChildDrawable(chipIcon);
            if (showsChipIcon()) {
                applyChildDrawable(this.chipIcon);
            }
            invalidateSelf();
            if (calculateChipIconWidth != calculateChipIconWidth2) {
                onSizeChange();
            }
        }
    }

    @Nullable
    public ColorStateList getChipIconTint() {
        return this.chipIconTint;
    }

    public void setChipIconTintResource(@ColorRes int i) {
        setChipIconTint(AppCompatResources.getColorStateList(this.context, i));
    }

    public void setChipIconTint(@Nullable ColorStateList colorStateList) {
        if (this.chipIconTint != colorStateList) {
            this.chipIconTint = colorStateList;
            if (showsChipIcon()) {
                DrawableCompat.setTintList(this.chipIcon, colorStateList);
            }
            onStateChange(getState());
        }
    }

    public float getChipIconSize() {
        return this.chipIconSize;
    }

    public void setChipIconSizeResource(@DimenRes int i) {
        setChipIconSize(this.context.getResources().getDimension(i));
    }

    public void setChipIconSize(float f) {
        if (this.chipIconSize != f) {
            float calculateChipIconWidth = calculateChipIconWidth();
            this.chipIconSize = f;
            f = calculateChipIconWidth();
            invalidateSelf();
            if (calculateChipIconWidth != f) {
                onSizeChange();
            }
        }
    }

    public boolean isCloseIconVisible() {
        return this.closeIconVisible;
    }

    @Deprecated
    public boolean isCloseIconEnabled() {
        return isCloseIconVisible();
    }

    public void setCloseIconVisible(@BoolRes int i) {
        setCloseIconVisible(this.context.getResources().getBoolean(i));
    }

    public void setCloseIconVisible(boolean z) {
        if (this.closeIconVisible != z) {
            boolean showsCloseIcon = showsCloseIcon();
            this.closeIconVisible = z;
            z = showsCloseIcon();
            if ((showsCloseIcon != z ? 1 : null) != null) {
                if (z) {
                    applyChildDrawable(this.closeIcon);
                } else {
                    unapplyChildDrawable(this.closeIcon);
                }
                invalidateSelf();
                onSizeChange();
            }
        }
    }

    @Deprecated
    public void setCloseIconEnabledResource(@BoolRes int i) {
        setCloseIconVisible(i);
    }

    @Deprecated
    public void setCloseIconEnabled(boolean z) {
        setCloseIconVisible(z);
    }

    @Nullable
    public Drawable getCloseIcon() {
        return this.closeIcon != null ? DrawableCompat.unwrap(this.closeIcon) : null;
    }

    public void setCloseIconResource(@DrawableRes int i) {
        setCloseIcon(AppCompatResources.getDrawable(this.context, i));
    }

    public void setCloseIcon(@Nullable Drawable drawable) {
        Drawable closeIcon = getCloseIcon();
        if (closeIcon != drawable) {
            float calculateCloseIconWidth = calculateCloseIconWidth();
            this.closeIcon = drawable != null ? DrawableCompat.wrap(drawable).mutate() : null;
            float calculateCloseIconWidth2 = calculateCloseIconWidth();
            unapplyChildDrawable(closeIcon);
            if (showsCloseIcon()) {
                applyChildDrawable(this.closeIcon);
            }
            invalidateSelf();
            if (calculateCloseIconWidth != calculateCloseIconWidth2) {
                onSizeChange();
            }
        }
    }

    @Nullable
    public ColorStateList getCloseIconTint() {
        return this.closeIconTint;
    }

    public void setCloseIconTintResource(@ColorRes int i) {
        setCloseIconTint(AppCompatResources.getColorStateList(this.context, i));
    }

    public void setCloseIconTint(@Nullable ColorStateList colorStateList) {
        if (this.closeIconTint != colorStateList) {
            this.closeIconTint = colorStateList;
            if (showsCloseIcon()) {
                DrawableCompat.setTintList(this.closeIcon, colorStateList);
            }
            onStateChange(getState());
        }
    }

    public float getCloseIconSize() {
        return this.closeIconSize;
    }

    public void setCloseIconSizeResource(@DimenRes int i) {
        setCloseIconSize(this.context.getResources().getDimension(i));
    }

    public void setCloseIconSize(float f) {
        if (this.closeIconSize != f) {
            this.closeIconSize = f;
            invalidateSelf();
            if (showsCloseIcon()) {
                onSizeChange();
            }
        }
    }

    public void setCloseIconContentDescription(@Nullable CharSequence charSequence) {
        if (this.closeIconContentDescription != charSequence) {
            this.closeIconContentDescription = BidiFormatter.getInstance().unicodeWrap(charSequence);
            invalidateSelf();
        }
    }

    @Nullable
    public CharSequence getCloseIconContentDescription() {
        return this.closeIconContentDescription;
    }

    public boolean isCheckable() {
        return this.checkable;
    }

    public void setCheckableResource(@BoolRes int i) {
        setCheckable(this.context.getResources().getBoolean(i));
    }

    public void setCheckable(boolean z) {
        if (this.checkable != z) {
            this.checkable = z;
            float calculateChipIconWidth = calculateChipIconWidth();
            if (!z && this.currentChecked) {
                this.currentChecked = false;
            }
            float calculateChipIconWidth2 = calculateChipIconWidth();
            invalidateSelf();
            if (calculateChipIconWidth != calculateChipIconWidth2) {
                onSizeChange();
            }
        }
    }

    public boolean isCheckedIconVisible() {
        return this.checkedIconVisible;
    }

    @Deprecated
    public boolean isCheckedIconEnabled() {
        return isCheckedIconVisible();
    }

    public void setCheckedIconVisible(@BoolRes int i) {
        setCheckedIconVisible(this.context.getResources().getBoolean(i));
    }

    public void setCheckedIconVisible(boolean z) {
        if (this.checkedIconVisible != z) {
            boolean showsCheckedIcon = showsCheckedIcon();
            this.checkedIconVisible = z;
            z = showsCheckedIcon();
            if ((showsCheckedIcon != z ? 1 : null) != null) {
                if (z) {
                    applyChildDrawable(this.checkedIcon);
                } else {
                    unapplyChildDrawable(this.checkedIcon);
                }
                invalidateSelf();
                onSizeChange();
            }
        }
    }

    @Deprecated
    public void setCheckedIconEnabledResource(@BoolRes int i) {
        setCheckedIconVisible(this.context.getResources().getBoolean(i));
    }

    @Deprecated
    public void setCheckedIconEnabled(boolean z) {
        setCheckedIconVisible(z);
    }

    @Nullable
    public Drawable getCheckedIcon() {
        return this.checkedIcon;
    }

    public void setCheckedIconResource(@DrawableRes int i) {
        setCheckedIcon(AppCompatResources.getDrawable(this.context, i));
    }

    public void setCheckedIcon(@Nullable Drawable drawable) {
        if (this.checkedIcon != drawable) {
            float calculateChipIconWidth = calculateChipIconWidth();
            this.checkedIcon = drawable;
            float calculateChipIconWidth2 = calculateChipIconWidth();
            unapplyChildDrawable(this.checkedIcon);
            applyChildDrawable(this.checkedIcon);
            invalidateSelf();
            if (calculateChipIconWidth != calculateChipIconWidth2) {
                onSizeChange();
            }
        }
    }

    @Nullable
    public MotionSpec getShowMotionSpec() {
        return this.showMotionSpec;
    }

    public void setShowMotionSpecResource(@AnimatorRes int i) {
        setShowMotionSpec(MotionSpec.createFromResource(this.context, i));
    }

    public void setShowMotionSpec(@Nullable MotionSpec motionSpec) {
        this.showMotionSpec = motionSpec;
    }

    @Nullable
    public MotionSpec getHideMotionSpec() {
        return this.hideMotionSpec;
    }

    public void setHideMotionSpecResource(@AnimatorRes int i) {
        setHideMotionSpec(MotionSpec.createFromResource(this.context, i));
    }

    public void setHideMotionSpec(@Nullable MotionSpec motionSpec) {
        this.hideMotionSpec = motionSpec;
    }

    public float getChipStartPadding() {
        return this.chipStartPadding;
    }

    public void setChipStartPaddingResource(@DimenRes int i) {
        setChipStartPadding(this.context.getResources().getDimension(i));
    }

    public void setChipStartPadding(float f) {
        if (this.chipStartPadding != f) {
            this.chipStartPadding = f;
            invalidateSelf();
            onSizeChange();
        }
    }

    public float getIconStartPadding() {
        return this.iconStartPadding;
    }

    public void setIconStartPaddingResource(@DimenRes int i) {
        setIconStartPadding(this.context.getResources().getDimension(i));
    }

    public void setIconStartPadding(float f) {
        if (this.iconStartPadding != f) {
            float calculateChipIconWidth = calculateChipIconWidth();
            this.iconStartPadding = f;
            f = calculateChipIconWidth();
            invalidateSelf();
            if (calculateChipIconWidth != f) {
                onSizeChange();
            }
        }
    }

    public float getIconEndPadding() {
        return this.iconEndPadding;
    }

    public void setIconEndPaddingResource(@DimenRes int i) {
        setIconEndPadding(this.context.getResources().getDimension(i));
    }

    public void setIconEndPadding(float f) {
        if (this.iconEndPadding != f) {
            float calculateChipIconWidth = calculateChipIconWidth();
            this.iconEndPadding = f;
            f = calculateChipIconWidth();
            invalidateSelf();
            if (calculateChipIconWidth != f) {
                onSizeChange();
            }
        }
    }

    public float getTextStartPadding() {
        return this.textStartPadding;
    }

    public void setTextStartPaddingResource(@DimenRes int i) {
        setTextStartPadding(this.context.getResources().getDimension(i));
    }

    public void setTextStartPadding(float f) {
        if (this.textStartPadding != f) {
            this.textStartPadding = f;
            invalidateSelf();
            onSizeChange();
        }
    }

    public float getTextEndPadding() {
        return this.textEndPadding;
    }

    public void setTextEndPaddingResource(@DimenRes int i) {
        setTextEndPadding(this.context.getResources().getDimension(i));
    }

    public void setTextEndPadding(float f) {
        if (this.textEndPadding != f) {
            this.textEndPadding = f;
            invalidateSelf();
            onSizeChange();
        }
    }

    public float getCloseIconStartPadding() {
        return this.closeIconStartPadding;
    }

    public void setCloseIconStartPaddingResource(@DimenRes int i) {
        setCloseIconStartPadding(this.context.getResources().getDimension(i));
    }

    public void setCloseIconStartPadding(float f) {
        if (this.closeIconStartPadding != f) {
            this.closeIconStartPadding = f;
            invalidateSelf();
            if (showsCloseIcon()) {
                onSizeChange();
            }
        }
    }

    public float getCloseIconEndPadding() {
        return this.closeIconEndPadding;
    }

    public void setCloseIconEndPaddingResource(@DimenRes int i) {
        setCloseIconEndPadding(this.context.getResources().getDimension(i));
    }

    public void setCloseIconEndPadding(float f) {
        if (this.closeIconEndPadding != f) {
            this.closeIconEndPadding = f;
            invalidateSelf();
            if (showsCloseIcon()) {
                onSizeChange();
            }
        }
    }

    public float getChipEndPadding() {
        return this.chipEndPadding;
    }

    public void setChipEndPaddingResource(@DimenRes int i) {
        setChipEndPadding(this.context.getResources().getDimension(i));
    }

    public void setChipEndPadding(float f) {
        if (this.chipEndPadding != f) {
            this.chipEndPadding = f;
            invalidateSelf();
            onSizeChange();
        }
    }

    @Px
    public int getMaxWidth() {
        return this.maxWidth;
    }

    public void setMaxWidth(@Px int i) {
        this.maxWidth = i;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean shouldDrawText() {
        return this.shouldDrawText;
    }

    /* Access modifiers changed, original: 0000 */
    public void setShouldDrawText(boolean z) {
        this.shouldDrawText = z;
    }
}
