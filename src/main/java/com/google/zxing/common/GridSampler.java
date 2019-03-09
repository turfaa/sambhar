package com.google.zxing.common;

import com.google.zxing.NotFoundException;

public abstract class GridSampler {
    private static GridSampler gridSampler = new DefaultGridSampler();

    public abstract BitMatrix sampleGrid(BitMatrix bitMatrix, int i, int i2, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15, float f16) throws NotFoundException;

    public abstract BitMatrix sampleGrid(BitMatrix bitMatrix, int i, int i2, PerspectiveTransform perspectiveTransform) throws NotFoundException;

    public static void setGridSampler(GridSampler gridSampler) {
        gridSampler = gridSampler;
    }

    public static GridSampler getInstance() {
        return gridSampler;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0034  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0075  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0071  */
    protected static void checkAndNudgePoints(com.google.zxing.common.BitMatrix r9, float[] r10) throws com.google.zxing.NotFoundException {
        /*
        r0 = r9.getWidth();
        r9 = r9.getHeight();
        r1 = 0;
        r2 = 1;
        r3 = 0;
        r4 = 1;
    L_0x000c:
        r5 = r10.length;
        r6 = 0;
        r7 = -1;
        if (r3 >= r5) goto L_0x0048;
    L_0x0011:
        if (r4 == 0) goto L_0x0048;
    L_0x0013:
        r4 = r10[r3];
        r4 = (int) r4;
        r5 = r3 + 1;
        r8 = r10[r5];
        r8 = (int) r8;
        if (r4 < r7) goto L_0x0043;
    L_0x001d:
        if (r4 > r0) goto L_0x0043;
    L_0x001f:
        if (r8 < r7) goto L_0x0043;
    L_0x0021:
        if (r8 > r9) goto L_0x0043;
    L_0x0023:
        if (r4 != r7) goto L_0x0029;
    L_0x0025:
        r10[r3] = r6;
    L_0x0027:
        r4 = 1;
        goto L_0x0032;
    L_0x0029:
        if (r4 != r0) goto L_0x0031;
    L_0x002b:
        r4 = r0 + -1;
        r4 = (float) r4;
        r10[r3] = r4;
        goto L_0x0027;
    L_0x0031:
        r4 = 0;
    L_0x0032:
        if (r8 != r7) goto L_0x0038;
    L_0x0034:
        r10[r5] = r6;
    L_0x0036:
        r4 = 1;
        goto L_0x0040;
    L_0x0038:
        if (r8 != r9) goto L_0x0040;
    L_0x003a:
        r4 = r9 + -1;
        r4 = (float) r4;
        r10[r5] = r4;
        goto L_0x0036;
    L_0x0040:
        r3 = r3 + 2;
        goto L_0x000c;
    L_0x0043:
        r9 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r9;
    L_0x0048:
        r3 = r10.length;
        r3 = r3 + -2;
        r4 = 1;
    L_0x004c:
        if (r3 < 0) goto L_0x0085;
    L_0x004e:
        if (r4 == 0) goto L_0x0085;
    L_0x0050:
        r4 = r10[r3];
        r4 = (int) r4;
        r5 = r3 + 1;
        r8 = r10[r5];
        r8 = (int) r8;
        if (r4 < r7) goto L_0x0080;
    L_0x005a:
        if (r4 > r0) goto L_0x0080;
    L_0x005c:
        if (r8 < r7) goto L_0x0080;
    L_0x005e:
        if (r8 > r9) goto L_0x0080;
    L_0x0060:
        if (r4 != r7) goto L_0x0066;
    L_0x0062:
        r10[r3] = r6;
    L_0x0064:
        r4 = 1;
        goto L_0x006f;
    L_0x0066:
        if (r4 != r0) goto L_0x006e;
    L_0x0068:
        r4 = r0 + -1;
        r4 = (float) r4;
        r10[r3] = r4;
        goto L_0x0064;
    L_0x006e:
        r4 = 0;
    L_0x006f:
        if (r8 != r7) goto L_0x0075;
    L_0x0071:
        r10[r5] = r6;
    L_0x0073:
        r4 = 1;
        goto L_0x007d;
    L_0x0075:
        if (r8 != r9) goto L_0x007d;
    L_0x0077:
        r4 = r9 + -1;
        r4 = (float) r4;
        r10[r5] = r4;
        goto L_0x0073;
    L_0x007d:
        r3 = r3 + -2;
        goto L_0x004c;
    L_0x0080:
        r9 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r9;
    L_0x0085:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.common.GridSampler.checkAndNudgePoints(com.google.zxing.common.BitMatrix, float[]):void");
    }
}
