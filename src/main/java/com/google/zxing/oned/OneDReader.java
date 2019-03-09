package com.google.zxing.oned;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public abstract class OneDReader implements Reader {
    public abstract Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws NotFoundException, ChecksumException, FormatException;

    public void reset() {
    }

    public Result decode(BinaryBitmap binaryBitmap) throws NotFoundException, FormatException {
        return decode(binaryBitmap, null);
    }

    public Result decode(BinaryBitmap binaryBitmap, Map<DecodeHintType, ?> map) throws NotFoundException, FormatException {
        try {
            return doDecode(binaryBitmap, map);
        } catch (NotFoundException e) {
            Object obj = (map == null || !map.containsKey(DecodeHintType.TRY_HARDER)) ? null : 1;
            if (obj == null || !binaryBitmap.isRotateSupported()) {
                throw e;
            }
            binaryBitmap = binaryBitmap.rotateCounterClockwise();
            Result doDecode = doDecode(binaryBitmap, map);
            Map resultMetadata = doDecode.getResultMetadata();
            int i = 270;
            if (resultMetadata != null && resultMetadata.containsKey(ResultMetadataType.ORIENTATION)) {
                i = (((Integer) resultMetadata.get(ResultMetadataType.ORIENTATION)).intValue() + 270) % 360;
            }
            doDecode.putMetadata(ResultMetadataType.ORIENTATION, Integer.valueOf(i));
            ResultPoint[] resultPoints = doDecode.getResultPoints();
            if (resultPoints != null) {
                int height = binaryBitmap.getHeight();
                for (int i2 = 0; i2 < resultPoints.length; i2++) {
                    resultPoints[i2] = new ResultPoint((((float) height) - resultPoints[i2].getY()) - 1.0f, resultPoints[i2].getX());
                }
            }
            return doDecode;
        }
    }

    private Result doDecode(BinaryBitmap binaryBitmap, Map<DecodeHintType, ?> map) throws NotFoundException {
        int i;
        BinaryBitmap binaryBitmap2;
        Map<DecodeHintType, ?> map2 = map;
        int width = binaryBitmap.getWidth();
        int height = binaryBitmap.getHeight();
        BitArray bitArray = new BitArray(width);
        int i2 = height >> 1;
        int i3 = 0;
        int i4 = 1;
        Object obj = (map2 == null || !map2.containsKey(DecodeHintType.TRY_HARDER)) ? null : 1;
        int max = Math.max(1, height >> (obj != null ? 8 : 5));
        int i5 = obj != null ? height : 15;
        Map map3 = map2;
        int i6 = 0;
        while (i6 < i5) {
            int i7 = i6 + 1;
            int i8 = i7 / 2;
            if (((i6 & 1) == 0 ? 1 : null) == null) {
                i8 = -i8;
            }
            i8 = (i8 * max) + i2;
            if (i8 < 0 || i8 >= height) {
                break;
            }
            try {
                BitArray blackRow = binaryBitmap.getBlackRow(i8, bitArray);
                int i9 = 0;
                while (i9 < 2) {
                    if (i9 == i4) {
                        blackRow.reverse();
                        if (map3 != null && map3.containsKey(DecodeHintType.NEED_RESULT_POINT_CALLBACK)) {
                            EnumMap enumMap = new EnumMap(DecodeHintType.class);
                            enumMap.putAll(map3);
                            enumMap.remove(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
                            map3 = enumMap;
                        }
                    }
                    try {
                        Result decodeRow = decodeRow(i8, blackRow, map3);
                        if (i9 == i4) {
                            decodeRow.putMetadata(ResultMetadataType.ORIENTATION, Integer.valueOf(180));
                            ResultPoint[] resultPoints = decodeRow.getResultPoints();
                            if (resultPoints != null) {
                                float f = (float) width;
                                i = width;
                                try {
                                    resultPoints[0] = new ResultPoint((f - resultPoints[i3].getX()) - 1.0f, resultPoints[i3].getY());
                                    try {
                                        resultPoints[1] = new ResultPoint((f - resultPoints[1].getX()) - 1.0f, resultPoints[1].getY());
                                    } catch (ReaderException unused) {
                                        continue;
                                    }
                                } catch (ReaderException unused2) {
                                    i9++;
                                    width = i;
                                    binaryBitmap2 = binaryBitmap;
                                    i3 = 0;
                                    i4 = 1;
                                }
                            }
                        }
                        return decodeRow;
                    } catch (ReaderException unused3) {
                        i = width;
                        i9++;
                        width = i;
                        binaryBitmap2 = binaryBitmap;
                        i3 = 0;
                        i4 = 1;
                    }
                }
                i = width;
                bitArray = blackRow;
            } catch (NotFoundException unused4) {
                i = width;
            }
            i6 = i7;
            width = i;
            i3 = 0;
            i4 = 1;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    protected static void recordPattern(BitArray bitArray, int i, int[] iArr) throws NotFoundException {
        int length = iArr.length;
        Arrays.fill(iArr, 0, length, 0);
        int size = bitArray.getSize();
        if (i < size) {
            int i2 = bitArray.get(i) ^ 1;
            int i3 = 0;
            while (i < size) {
                if ((bitArray.get(i) ^ i2) == 0) {
                    i3++;
                    if (i3 == length) {
                        break;
                    }
                    iArr[i3] = 1;
                    i2 = i2 == 0 ? 1 : 0;
                } else {
                    iArr[i3] = iArr[i3] + 1;
                }
                i++;
            }
            if (i3 == length) {
                return;
            }
            if (i3 != length - 1 || i != size) {
                throw NotFoundException.getNotFoundInstance();
            }
            return;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    protected static void recordPatternInReverse(BitArray bitArray, int i, int[] iArr) throws NotFoundException {
        int length = iArr.length;
        boolean z = bitArray.get(i);
        while (i > 0 && length >= 0) {
            i--;
            if (bitArray.get(i) != z) {
                length--;
                z = !z;
            }
        }
        if (length < 0) {
            recordPattern(bitArray, i + 1, iArr);
            return;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    protected static float patternMatchVariance(int[] iArr, int[] iArr2, float f) {
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
