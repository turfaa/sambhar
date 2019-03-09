package com.google.zxing.pdf417.detector;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class Detector {
    private static final int BARCODE_MIN_HEIGHT = 10;
    private static final int[] INDEXES_START_PATTERN = new int[]{0, 4, 1, 5};
    private static final int[] INDEXES_STOP_PATTERN = new int[]{6, 2, 7, 3};
    private static final float MAX_AVG_VARIANCE = 0.42f;
    private static final float MAX_INDIVIDUAL_VARIANCE = 0.8f;
    private static final int MAX_PATTERN_DRIFT = 5;
    private static final int MAX_PIXEL_DRIFT = 3;
    private static final int ROW_STEP = 5;
    private static final int SKIPPED_ROW_COUNT_MAX = 25;
    private static final int[] START_PATTERN = new int[]{8, 1, 1, 1, 1, 1, 1, 3};
    private static final int[] STOP_PATTERN = new int[]{7, 1, 1, 3, 1, 1, 1, 2, 1};

    private Detector() {
    }

    public static PDF417DetectorResult detect(BinaryBitmap binaryBitmap, Map<DecodeHintType, ?> map, boolean z) throws NotFoundException {
        BitMatrix blackMatrix = binaryBitmap.getBlackMatrix();
        List detect = detect(z, blackMatrix);
        if (detect.isEmpty()) {
            blackMatrix = blackMatrix.clone();
            blackMatrix.rotate180();
            detect = detect(z, blackMatrix);
        }
        return new PDF417DetectorResult(blackMatrix, detect);
    }

    private static List<ResultPoint[]> detect(boolean z, BitMatrix bitMatrix) {
        ArrayList<ResultPoint[]> arrayList = new ArrayList();
        int i = 0;
        loop0:
        while (true) {
            int i2 = 0;
            Object obj = null;
            while (i < bitMatrix.getHeight()) {
                ResultPoint[] findVertices = findVertices(bitMatrix, i, i2);
                if (findVertices[0] == null && findVertices[3] == null) {
                    if (obj == null) {
                        break;
                    }
                    for (ResultPoint[] resultPointArr : arrayList) {
                        if (resultPointArr[1] != null) {
                            i = (int) Math.max((float) i, resultPointArr[1].getY());
                        }
                        if (resultPointArr[3] != null) {
                            i = Math.max(i, (int) resultPointArr[3].getY());
                        }
                    }
                    i += 5;
                } else {
                    arrayList.add(findVertices);
                    if (!z) {
                        break loop0;
                    }
                    int x;
                    float y;
                    if (findVertices[2] != null) {
                        x = (int) findVertices[2].getX();
                        y = findVertices[2].getY();
                    } else {
                        x = (int) findVertices[4].getX();
                        y = findVertices[4].getY();
                    }
                    i = (int) y;
                    i2 = x;
                    obj = 1;
                }
            }
            break loop0;
        }
        return arrayList;
    }

    private static ResultPoint[] findVertices(BitMatrix bitMatrix, int i, int i2) {
        int height = bitMatrix.getHeight();
        int width = bitMatrix.getWidth();
        ResultPoint[] resultPointArr = new ResultPoint[8];
        copyToResult(resultPointArr, findRowsWithPattern(bitMatrix, height, width, i, i2, START_PATTERN), INDEXES_START_PATTERN);
        if (resultPointArr[4] != null) {
            i2 = (int) resultPointArr[4].getX();
            i = (int) resultPointArr[4].getY();
        }
        copyToResult(resultPointArr, findRowsWithPattern(bitMatrix, height, width, i, i2, STOP_PATTERN), INDEXES_STOP_PATTERN);
        return resultPointArr;
    }

    private static void copyToResult(ResultPoint[] resultPointArr, ResultPoint[] resultPointArr2, int[] iArr) {
        for (int i = 0; i < iArr.length; i++) {
            resultPointArr[iArr[i]] = resultPointArr2[i];
        }
    }

    private static ResultPoint[] findRowsWithPattern(BitMatrix bitMatrix, int i, int i2, int i3, int i4, int[] iArr) {
        int i5;
        Object obj;
        int[] findGuardPattern;
        int i6 = i;
        ResultPoint[] resultPointArr = new ResultPoint[4];
        int[] iArr2 = new int[iArr.length];
        int i7 = i3;
        while (true) {
            i5 = 0;
            if (i7 >= i6) {
                obj = null;
                break;
            }
            findGuardPattern = findGuardPattern(bitMatrix, i4, i7, i2, false, iArr, iArr2);
            if (findGuardPattern != null) {
                int[] iArr3;
                while (true) {
                    iArr3 = findGuardPattern;
                    if (i7 <= 0) {
                        break;
                    }
                    i7--;
                    findGuardPattern = findGuardPattern(bitMatrix, i4, i7, i2, false, iArr, iArr2);
                    if (findGuardPattern == null) {
                        i7++;
                        break;
                    }
                }
                float f = (float) i7;
                resultPointArr[0] = new ResultPoint((float) iArr3[0], f);
                resultPointArr[1] = new ResultPoint((float) iArr3[1], f);
                obj = 1;
            } else {
                i7 += 5;
            }
        }
        int i8 = i7 + 1;
        if (obj != null) {
            int i9;
            int i10;
            int[] iArr4 = new int[]{(int) resultPointArr[0].getX(), (int) resultPointArr[1].getX()};
            int i11 = i8;
            int i12 = 0;
            while (i11 < i6) {
                i9 = i12;
                i10 = i11;
                findGuardPattern = findGuardPattern(bitMatrix, iArr4[0], i11, i2, false, iArr, iArr2);
                if (findGuardPattern == null || Math.abs(iArr4[0] - findGuardPattern[0]) >= 5 || Math.abs(iArr4[1] - findGuardPattern[1]) >= 5) {
                    if (i9 > 25) {
                        break;
                    }
                    i12 = i9 + 1;
                } else {
                    iArr4 = findGuardPattern;
                    i12 = 0;
                }
                i11 = i10 + 1;
            }
            i9 = i12;
            i10 = i11;
            i8 = i10 - (i9 + 1);
            float f2 = (float) i8;
            resultPointArr[2] = new ResultPoint((float) iArr4[0], f2);
            resultPointArr[3] = new ResultPoint((float) iArr4[1], f2);
        }
        if (i8 - i7 < 10) {
            while (i5 < 4) {
                resultPointArr[i5] = null;
                i5++;
            }
        }
        return resultPointArr;
    }

    private static int[] findGuardPattern(BitMatrix bitMatrix, int i, int i2, int i3, boolean z, int[] iArr, int[] iArr2) {
        int i4;
        Arrays.fill(iArr2, 0, iArr2.length, 0);
        int i5 = 0;
        while (bitMatrix.get(i, i2) && i > 0) {
            i4 = i5 + 1;
            if (i5 >= 3) {
                break;
            }
            i--;
            i5 = i4;
        }
        i5 = iArr.length;
        int i6 = i;
        i4 = 0;
        while (true) {
            int i7 = 1;
            if (i < i3) {
                int z2;
                if ((bitMatrix.get(i, i2) ^ z2) != 0) {
                    iArr2[i4] = iArr2[i4] + 1;
                } else {
                    int i8 = i5 - 1;
                    if (i4 != i8) {
                        i4++;
                    } else if (patternMatchVariance(iArr2, iArr, MAX_INDIVIDUAL_VARIANCE) < MAX_AVG_VARIANCE) {
                        return new int[]{i6, i};
                    } else {
                        i6 += iArr2[0] + iArr2[1];
                        int i9 = i5 - 2;
                        System.arraycopy(iArr2, 2, iArr2, 0, i9);
                        iArr2[i9] = 0;
                        iArr2[i8] = 0;
                        i4--;
                    }
                    iArr2[i4] = 1;
                    if (z2 != 0) {
                        i7 = 0;
                    }
                    z2 = i7;
                }
                i++;
            } else if (i4 != i5 - 1 || patternMatchVariance(iArr2, iArr, MAX_INDIVIDUAL_VARIANCE) >= MAX_AVG_VARIANCE) {
                return null;
            } else {
                return new int[]{i6, i - 1};
            }
        }
    }

    private static float patternMatchVariance(int[] iArr, int[] iArr2, float f) {
        int length = iArr.length;
        int i = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < length; i3++) {
            i += iArr[i3];
            i2 += iArr2[i3];
        }
        if (i < i2) {
            return Float.POSITIVE_INFINITY;
        }
        float f2 = (float) i;
        float f3 = f2 / ((float) i2);
        f *= f3;
        float f4 = 0.0f;
        for (int i4 = 0; i4 < length; i4++) {
            float f5 = ((float) iArr2[i4]) * f3;
            float f6 = (float) iArr[i4];
            f6 = f6 > f5 ? f6 - f5 : f5 - f6;
            if (f6 > f) {
                return Float.POSITIVE_INFINITY;
            }
            f4 += f6;
        }
        return f4 / f2;
    }
}
