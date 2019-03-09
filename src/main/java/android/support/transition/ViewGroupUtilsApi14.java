package android.support.transition;

import android.animation.LayoutTransition;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class ViewGroupUtilsApi14 {
    private static final int LAYOUT_TRANSITION_CHANGING = 4;
    private static final String TAG = "ViewGroupUtilsApi14";
    private static Method sCancelMethod;
    private static boolean sCancelMethodFetched;
    private static LayoutTransition sEmptyLayoutTransition;
    private static Field sLayoutSuppressedField;
    private static boolean sLayoutSuppressedFieldFetched;

    /* JADX WARNING: Removed duplicated region for block: B:33:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:39:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0096  */
    static void suppressLayout(@android.support.annotation.NonNull android.view.ViewGroup r5, boolean r6) {
        /*
        r0 = sEmptyLayoutTransition;
        r1 = 1;
        r2 = 0;
        r3 = 0;
        if (r0 != 0) goto L_0x002a;
    L_0x0007:
        r0 = new android.support.transition.ViewGroupUtilsApi14$1;
        r0.<init>();
        sEmptyLayoutTransition = r0;
        r0 = sEmptyLayoutTransition;
        r4 = 2;
        r0.setAnimator(r4, r3);
        r0 = sEmptyLayoutTransition;
        r0.setAnimator(r2, r3);
        r0 = sEmptyLayoutTransition;
        r0.setAnimator(r1, r3);
        r0 = sEmptyLayoutTransition;
        r4 = 3;
        r0.setAnimator(r4, r3);
        r0 = sEmptyLayoutTransition;
        r4 = 4;
        r0.setAnimator(r4, r3);
    L_0x002a:
        if (r6 == 0) goto L_0x004a;
    L_0x002c:
        r6 = r5.getLayoutTransition();
        if (r6 == 0) goto L_0x0044;
    L_0x0032:
        r0 = r6.isRunning();
        if (r0 == 0) goto L_0x003b;
    L_0x0038:
        cancelLayoutTransition(r6);
    L_0x003b:
        r0 = sEmptyLayoutTransition;
        if (r6 == r0) goto L_0x0044;
    L_0x003f:
        r0 = android.support.transition.R.id.transition_layout_save;
        r5.setTag(r0, r6);
    L_0x0044:
        r6 = sEmptyLayoutTransition;
        r5.setLayoutTransition(r6);
        goto L_0x009e;
    L_0x004a:
        r5.setLayoutTransition(r3);
        r6 = sLayoutSuppressedFieldFetched;
        if (r6 != 0) goto L_0x006a;
    L_0x0051:
        r6 = android.view.ViewGroup.class;
        r0 = "mLayoutSuppressed";
        r6 = r6.getDeclaredField(r0);	 Catch:{ NoSuchFieldException -> 0x0061 }
        sLayoutSuppressedField = r6;	 Catch:{ NoSuchFieldException -> 0x0061 }
        r6 = sLayoutSuppressedField;	 Catch:{ NoSuchFieldException -> 0x0061 }
        r6.setAccessible(r1);	 Catch:{ NoSuchFieldException -> 0x0061 }
        goto L_0x0068;
    L_0x0061:
        r6 = "ViewGroupUtilsApi14";
        r0 = "Failed to access mLayoutSuppressed field by reflection";
        android.util.Log.i(r6, r0);
    L_0x0068:
        sLayoutSuppressedFieldFetched = r1;
    L_0x006a:
        r6 = sLayoutSuppressedField;
        if (r6 == 0) goto L_0x0087;
    L_0x006e:
        r6 = sLayoutSuppressedField;	 Catch:{ IllegalAccessException -> 0x0080 }
        r6 = r6.getBoolean(r5);	 Catch:{ IllegalAccessException -> 0x0080 }
        if (r6 == 0) goto L_0x007e;
    L_0x0076:
        r0 = sLayoutSuppressedField;	 Catch:{ IllegalAccessException -> 0x007c }
        r0.setBoolean(r5, r2);	 Catch:{ IllegalAccessException -> 0x007c }
        goto L_0x007e;
    L_0x007c:
        r2 = r6;
        goto L_0x0080;
    L_0x007e:
        r2 = r6;
        goto L_0x0087;
    L_0x0080:
        r6 = "ViewGroupUtilsApi14";
        r0 = "Failed to get mLayoutSuppressed field by reflection";
        android.util.Log.i(r6, r0);
    L_0x0087:
        if (r2 == 0) goto L_0x008c;
    L_0x0089:
        r5.requestLayout();
    L_0x008c:
        r6 = android.support.transition.R.id.transition_layout_save;
        r6 = r5.getTag(r6);
        r6 = (android.animation.LayoutTransition) r6;
        if (r6 == 0) goto L_0x009e;
    L_0x0096:
        r0 = android.support.transition.R.id.transition_layout_save;
        r5.setTag(r0, r3);
        r5.setLayoutTransition(r6);
    L_0x009e:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.transition.ViewGroupUtilsApi14.suppressLayout(android.view.ViewGroup, boolean):void");
    }

    private static void cancelLayoutTransition(LayoutTransition layoutTransition) {
        if (!sCancelMethodFetched) {
            try {
                sCancelMethod = LayoutTransition.class.getDeclaredMethod("cancel", new Class[0]);
                sCancelMethod.setAccessible(true);
            } catch (NoSuchMethodException unused) {
                Log.i(TAG, "Failed to access cancel method by reflection");
            }
            sCancelMethodFetched = true;
        }
        if (sCancelMethod != null) {
            try {
                sCancelMethod.invoke(layoutTransition, new Object[0]);
            } catch (IllegalAccessException unused2) {
                Log.i(TAG, "Failed to access cancel method by reflection");
            } catch (InvocationTargetException unused3) {
                Log.i(TAG, "Failed to invoke cancel method by reflection");
            }
        }
    }

    private ViewGroupUtilsApi14() {
    }
}
