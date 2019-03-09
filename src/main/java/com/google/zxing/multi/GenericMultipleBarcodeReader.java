package com.google.zxing.multi;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GenericMultipleBarcodeReader implements MultipleBarcodeReader {
    private static final int MAX_DEPTH = 4;
    private static final int MIN_DIMENSION_TO_RECUR = 100;
    private final Reader delegate;

    public GenericMultipleBarcodeReader(Reader reader) {
        this.delegate = reader;
    }

    public Result[] decodeMultiple(BinaryBitmap binaryBitmap) throws NotFoundException {
        return decodeMultiple(binaryBitmap, null);
    }

    public Result[] decodeMultiple(BinaryBitmap binaryBitmap, Map<DecodeHintType, ?> map) throws NotFoundException {
        ArrayList arrayList = new ArrayList();
        doDecodeMultiple(binaryBitmap, map, arrayList, 0, 0, 0);
        if (!arrayList.isEmpty()) {
            return (Result[]) arrayList.toArray(new Result[arrayList.size()]);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private void doDecodeMultiple(BinaryBitmap binaryBitmap, Map<DecodeHintType, ?> map, List<Result> list, int i, int i2, int i3) {
        BinaryBitmap binaryBitmap2 = binaryBitmap;
        int i4 = i;
        int i5 = i2;
        int i6 = i3;
        if (i6 <= 4) {
            try {
                Object obj;
                Result decode = this.delegate.decode(binaryBitmap2, map);
                for (Result text : list) {
                    if (text.getText().equals(decode.getText())) {
                        obj = 1;
                        break;
                    }
                }
                obj = null;
                if (obj == null) {
                    list.add(translateResultPoints(decode, i4, i5));
                } else {
                    List<Result> list2 = list;
                }
                ResultPoint[] resultPoints = decode.getResultPoints();
                if (resultPoints != null && resultPoints.length != 0) {
                    float f;
                    int i7;
                    int i8;
                    int i9;
                    int width = binaryBitmap.getWidth();
                    int height = binaryBitmap.getHeight();
                    float f2 = (float) width;
                    float f3 = (float) height;
                    float f4 = f3;
                    float f5 = 0.0f;
                    float f6 = 0.0f;
                    f3 = f2;
                    for (ResultPoint resultPoint : resultPoints) {
                        if (resultPoint != null) {
                            float x = resultPoint.getX();
                            float y = resultPoint.getY();
                            if (x < f3) {
                                f3 = x;
                            }
                            if (y < f4) {
                                f4 = y;
                            }
                            if (x > f5) {
                                f5 = x;
                            }
                            if (y > f6) {
                                f6 = y;
                            }
                        }
                    }
                    if (f3 > 100.0f) {
                        f = f4;
                        i7 = height;
                        i8 = width;
                        doDecodeMultiple(binaryBitmap2.crop(0, 0, (int) f3, height), map, list, i, i2, i6 + 1);
                    } else {
                        f = f4;
                        i7 = height;
                        i8 = width;
                    }
                    if (f > 100.0f) {
                        doDecodeMultiple(binaryBitmap2.crop(0, 0, i8, (int) f), map, list, i, i2, i6 + 1);
                    }
                    if (f5 < ((float) (i8 - 100))) {
                        i9 = (int) f5;
                        doDecodeMultiple(binaryBitmap2.crop(i9, 0, i8 - i9, i7), map, list, i4 + i9, i2, i6 + 1);
                    }
                    if (f6 < ((float) (i7 - 100))) {
                        i9 = (int) f6;
                        doDecodeMultiple(binaryBitmap2.crop(0, i9, i8, i7 - i9), map, list, i, i5 + i9, i6 + 1);
                    }
                }
            } catch (ReaderException unused) {
            }
        }
    }

    private static Result translateResultPoints(Result result, int i, int i2) {
        ResultPoint[] resultPoints = result.getResultPoints();
        if (resultPoints == null) {
            return result;
        }
        ResultPoint[] resultPointArr = new ResultPoint[resultPoints.length];
        for (int i3 = 0; i3 < resultPoints.length; i3++) {
            ResultPoint resultPoint = resultPoints[i3];
            if (resultPoint != null) {
                resultPointArr[i3] = new ResultPoint(resultPoint.getX() + ((float) i), resultPoint.getY() + ((float) i2));
            }
        }
        Result result2 = new Result(result.getText(), result.getRawBytes(), result.getNumBits(), resultPointArr, result.getBarcodeFormat(), result.getTimestamp());
        result2.putAllMetadata(result.getResultMetadata());
        return result2;
    }
}
