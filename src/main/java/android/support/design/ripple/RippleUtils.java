package android.support.design.ripple;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.ColorUtils;
import android.util.StateSet;

@RestrictTo({Scope.LIBRARY_GROUP})
public class RippleUtils {
    private static final int[] FOCUSED_STATE_SET = new int[]{16842908};
    private static final int[] HOVERED_FOCUSED_STATE_SET = new int[]{16843623, 16842908};
    private static final int[] HOVERED_STATE_SET = new int[]{16843623};
    private static final int[] PRESSED_STATE_SET = new int[]{16842919};
    private static final int[] SELECTED_FOCUSED_STATE_SET = new int[]{16842913, 16842908};
    private static final int[] SELECTED_HOVERED_FOCUSED_STATE_SET = new int[]{16842913, 16843623, 16842908};
    private static final int[] SELECTED_HOVERED_STATE_SET = new int[]{16842913, 16843623};
    private static final int[] SELECTED_PRESSED_STATE_SET = new int[]{16842913, 16842919};
    private static final int[] SELECTED_STATE_SET = new int[]{16842913};
    public static final boolean USE_FRAMEWORK_RIPPLE = (VERSION.SDK_INT >= 21);

    private RippleUtils() {
    }

    @NonNull
    public static ColorStateList convertToRippleDrawableColor(@Nullable ColorStateList colorStateList) {
        if (USE_FRAMEWORK_RIPPLE) {
            r0 = new int[2][];
            int[] iArr = new int[]{SELECTED_STATE_SET, getColorForState(colorStateList, SELECTED_PRESSED_STATE_SET)};
            r0[1] = StateSet.NOTHING;
            iArr[1] = getColorForState(colorStateList, PRESSED_STATE_SET);
            return new ColorStateList(r0, iArr);
        }
        r4 = new int[10][];
        int[] iArr2 = new int[]{SELECTED_PRESSED_STATE_SET, getColorForState(colorStateList, SELECTED_PRESSED_STATE_SET), SELECTED_HOVERED_FOCUSED_STATE_SET, getColorForState(colorStateList, SELECTED_HOVERED_FOCUSED_STATE_SET), SELECTED_FOCUSED_STATE_SET, getColorForState(colorStateList, SELECTED_FOCUSED_STATE_SET), SELECTED_HOVERED_STATE_SET, getColorForState(colorStateList, SELECTED_HOVERED_STATE_SET), SELECTED_STATE_SET, 0};
        r4[5] = PRESSED_STATE_SET;
        iArr2[5] = getColorForState(colorStateList, PRESSED_STATE_SET);
        r4[6] = HOVERED_FOCUSED_STATE_SET;
        iArr2[6] = getColorForState(colorStateList, HOVERED_FOCUSED_STATE_SET);
        r4[7] = FOCUSED_STATE_SET;
        iArr2[7] = getColorForState(colorStateList, FOCUSED_STATE_SET);
        r4[8] = HOVERED_STATE_SET;
        iArr2[8] = getColorForState(colorStateList, HOVERED_STATE_SET);
        r4[9] = StateSet.NOTHING;
        iArr2[9] = 0;
        return new ColorStateList(r4, iArr2);
    }

    @ColorInt
    private static int getColorForState(@Nullable ColorStateList colorStateList, int[] iArr) {
        int colorForState = colorStateList != null ? colorStateList.getColorForState(iArr, colorStateList.getDefaultColor()) : 0;
        return USE_FRAMEWORK_RIPPLE ? doubleAlpha(colorForState) : colorForState;
    }

    @ColorInt
    @TargetApi(21)
    private static int doubleAlpha(@ColorInt int i) {
        return ColorUtils.setAlphaComponent(i, Math.min(Color.alpha(i) * 2, 255));
    }
}
