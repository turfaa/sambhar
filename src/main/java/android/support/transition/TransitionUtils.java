package android.support.transition;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

class TransitionUtils {
    private static final boolean HAS_IS_ATTACHED_TO_WINDOW = (VERSION.SDK_INT >= 19);
    private static final boolean HAS_OVERLAY = (VERSION.SDK_INT >= 18);
    private static final boolean HAS_PICTURE_BITMAP;
    private static final int MAX_IMAGE_SIZE = 1048576;

    static class MatrixEvaluator implements TypeEvaluator<Matrix> {
        final float[] mTempEndValues = new float[9];
        final Matrix mTempMatrix = new Matrix();
        final float[] mTempStartValues = new float[9];

        MatrixEvaluator() {
        }

        public Matrix evaluate(float f, Matrix matrix, Matrix matrix2) {
            matrix.getValues(this.mTempStartValues);
            matrix2.getValues(this.mTempEndValues);
            for (int i = 0; i < 9; i++) {
                this.mTempEndValues[i] = this.mTempStartValues[i] + ((this.mTempEndValues[i] - this.mTempStartValues[i]) * f);
            }
            this.mTempMatrix.setValues(this.mTempEndValues);
            return this.mTempMatrix;
        }
    }

    static {
        boolean z = false;
        if (VERSION.SDK_INT >= 28) {
            z = true;
        }
        HAS_PICTURE_BITMAP = z;
    }

    static View copyViewImage(ViewGroup viewGroup, View view, View view2) {
        Matrix matrix = new Matrix();
        matrix.setTranslate((float) (-view2.getScrollX()), (float) (-view2.getScrollY()));
        ViewUtils.transformMatrixToGlobal(view, matrix);
        ViewUtils.transformMatrixToLocal(viewGroup, matrix);
        RectF rectF = new RectF(0.0f, 0.0f, (float) view.getWidth(), (float) view.getHeight());
        matrix.mapRect(rectF);
        int round = Math.round(rectF.left);
        int round2 = Math.round(rectF.top);
        int round3 = Math.round(rectF.right);
        int round4 = Math.round(rectF.bottom);
        ImageView imageView = new ImageView(view.getContext());
        imageView.setScaleType(ScaleType.CENTER_CROP);
        Bitmap createViewBitmap = createViewBitmap(view, matrix, rectF, viewGroup);
        if (createViewBitmap != null) {
            imageView.setImageBitmap(createViewBitmap);
        }
        imageView.measure(MeasureSpec.makeMeasureSpec(round3 - round, 1073741824), MeasureSpec.makeMeasureSpec(round4 - round2, 1073741824));
        imageView.layout(round, round2, round3, round4);
        return imageView;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0088  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0071  */
    private static android.graphics.Bitmap createViewBitmap(android.view.View r8, android.graphics.Matrix r9, android.graphics.RectF r10, android.view.ViewGroup r11) {
        /*
        r0 = HAS_IS_ATTACHED_TO_WINDOW;
        r1 = 0;
        if (r0 == 0) goto L_0x0013;
    L_0x0005:
        r0 = r8.isAttachedToWindow();
        r0 = r0 ^ 1;
        if (r11 != 0) goto L_0x000e;
    L_0x000d:
        goto L_0x0014;
    L_0x000e:
        r2 = r11.isAttachedToWindow();
        goto L_0x0015;
    L_0x0013:
        r0 = 0;
    L_0x0014:
        r2 = 0;
    L_0x0015:
        r3 = HAS_OVERLAY;
        r4 = 0;
        if (r3 == 0) goto L_0x0031;
    L_0x001a:
        if (r0 == 0) goto L_0x0031;
    L_0x001c:
        if (r2 != 0) goto L_0x001f;
    L_0x001e:
        return r4;
    L_0x001f:
        r1 = r8.getParent();
        r1 = (android.view.ViewGroup) r1;
        r2 = r1.indexOfChild(r8);
        r3 = r11.getOverlay();
        r3.add(r8);
        goto L_0x0033;
    L_0x0031:
        r1 = r4;
        r2 = 0;
    L_0x0033:
        r3 = r10.width();
        r3 = java.lang.Math.round(r3);
        r5 = r10.height();
        r5 = java.lang.Math.round(r5);
        if (r3 <= 0) goto L_0x0099;
    L_0x0045:
        if (r5 <= 0) goto L_0x0099;
    L_0x0047:
        r4 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r6 = 1233125376; // 0x49800000 float:1048576.0 double:6.092448853E-315;
        r7 = r3 * r5;
        r7 = (float) r7;
        r6 = r6 / r7;
        r4 = java.lang.Math.min(r4, r6);
        r3 = (float) r3;
        r3 = r3 * r4;
        r3 = java.lang.Math.round(r3);
        r5 = (float) r5;
        r5 = r5 * r4;
        r5 = java.lang.Math.round(r5);
        r6 = r10.left;
        r6 = -r6;
        r10 = r10.top;
        r10 = -r10;
        r9.postTranslate(r6, r10);
        r9.postScale(r4, r4);
        r10 = HAS_PICTURE_BITMAP;
        if (r10 == 0) goto L_0x0088;
    L_0x0071:
        r10 = new android.graphics.Picture;
        r10.<init>();
        r3 = r10.beginRecording(r3, r5);
        r3.concat(r9);
        r8.draw(r3);
        r10.endRecording();
        r4 = android.graphics.Bitmap.createBitmap(r10);
        goto L_0x0099;
    L_0x0088:
        r10 = android.graphics.Bitmap.Config.ARGB_8888;
        r4 = android.graphics.Bitmap.createBitmap(r3, r5, r10);
        r10 = new android.graphics.Canvas;
        r10.<init>(r4);
        r10.concat(r9);
        r8.draw(r10);
    L_0x0099:
        r9 = HAS_OVERLAY;
        if (r9 == 0) goto L_0x00a9;
    L_0x009d:
        if (r0 == 0) goto L_0x00a9;
    L_0x009f:
        r9 = r11.getOverlay();
        r9.remove(r8);
        r1.addView(r8, r2);
    L_0x00a9:
        return r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.transition.TransitionUtils.createViewBitmap(android.view.View, android.graphics.Matrix, android.graphics.RectF, android.view.ViewGroup):android.graphics.Bitmap");
    }

    static Animator mergeAnimators(Animator animator, Animator animator2) {
        if (animator == null) {
            return animator2;
        }
        if (animator2 == null) {
            return animator;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(new Animator[]{animator, animator2});
        return animatorSet;
    }

    private TransitionUtils() {
    }
}
