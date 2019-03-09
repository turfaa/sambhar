package com.google.zxing.qrcode.detector;

import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.common.GridSampler;
import com.google.zxing.common.PerspectiveTransform;
import com.google.zxing.common.detector.MathUtils;
import com.google.zxing.qrcode.decoder.Version;
import java.util.Map;

public class Detector {
    private final BitMatrix image;
    private ResultPointCallback resultPointCallback;

    public Detector(BitMatrix bitMatrix) {
        this.image = bitMatrix;
    }

    /* Access modifiers changed, original: protected|final */
    public final BitMatrix getImage() {
        return this.image;
    }

    /* Access modifiers changed, original: protected|final */
    public final ResultPointCallback getResultPointCallback() {
        return this.resultPointCallback;
    }

    public DetectorResult detect() throws NotFoundException, FormatException {
        return detect(null);
    }

    public final DetectorResult detect(Map<DecodeHintType, ?> map) throws NotFoundException, FormatException {
        ResultPointCallback resultPointCallback;
        if (map == null) {
            resultPointCallback = null;
        } else {
            resultPointCallback = (ResultPointCallback) map.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
        }
        this.resultPointCallback = resultPointCallback;
        return processFinderPatternInfo(new FinderPatternFinder(this.image, this.resultPointCallback).find(map));
    }

    /* Access modifiers changed, original: protected|final */
    public final DetectorResult processFinderPatternInfo(FinderPatternInfo finderPatternInfo) throws NotFoundException, FormatException {
        FinderPattern topLeft = finderPatternInfo.getTopLeft();
        FinderPattern topRight = finderPatternInfo.getTopRight();
        FinderPattern bottomLeft = finderPatternInfo.getBottomLeft();
        float calculateModuleSize = calculateModuleSize(topLeft, topRight, bottomLeft);
        if (calculateModuleSize >= 1.0f) {
            ResultPoint[] resultPointArr;
            int computeDimension = computeDimension(topLeft, topRight, bottomLeft, calculateModuleSize);
            Version provisionalVersionForDimension = Version.getProvisionalVersionForDimension(computeDimension);
            int dimensionForVersion = provisionalVersionForDimension.getDimensionForVersion() - 7;
            ResultPoint resultPoint = null;
            if (provisionalVersionForDimension.getAlignmentPatternCenters().length > 0) {
                float f = 1.0f - (3.0f / ((float) dimensionForVersion));
                int x = (int) (topLeft.getX() + ((((topRight.getX() - topLeft.getX()) + bottomLeft.getX()) - topLeft.getX()) * f));
                int y = (int) (topLeft.getY() + (f * (((topRight.getY() - topLeft.getY()) + bottomLeft.getY()) - topLeft.getY())));
                dimensionForVersion = 4;
                while (dimensionForVersion <= 16) {
                    try {
                        resultPoint = findAlignmentInRegion(calculateModuleSize, x, y, (float) dimensionForVersion);
                        break;
                    } catch (NotFoundException unused) {
                        dimensionForVersion <<= 1;
                    }
                }
            }
            BitMatrix sampleGrid = sampleGrid(this.image, createTransform(topLeft, topRight, bottomLeft, resultPoint, computeDimension), computeDimension);
            if (resultPoint == null) {
                resultPointArr = new ResultPoint[]{bottomLeft, topLeft, topRight};
            } else {
                resultPointArr = new ResultPoint[]{bottomLeft, topLeft, topRight, resultPoint};
            }
            return new DetectorResult(sampleGrid, resultPointArr);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static PerspectiveTransform createTransform(ResultPoint resultPoint, ResultPoint resultPoint2, ResultPoint resultPoint3, ResultPoint resultPoint4, int i) {
        float y;
        float f;
        float f2;
        float f3 = ((float) i) - 3.5f;
        if (resultPoint4 != null) {
            float x = resultPoint4.getX();
            y = resultPoint4.getY();
            f = x;
            f2 = f3 - 3.0f;
        } else {
            f = (resultPoint2.getX() - resultPoint.getX()) + resultPoint3.getX();
            y = (resultPoint2.getY() - resultPoint.getY()) + resultPoint3.getY();
            f2 = f3;
        }
        return PerspectiveTransform.quadrilateralToQuadrilateral(3.5f, 3.5f, f3, 3.5f, f2, f2, 3.5f, f3, resultPoint.getX(), resultPoint.getY(), resultPoint2.getX(), resultPoint2.getY(), f, y, resultPoint3.getX(), resultPoint3.getY());
    }

    private static BitMatrix sampleGrid(BitMatrix bitMatrix, PerspectiveTransform perspectiveTransform, int i) throws NotFoundException {
        return GridSampler.getInstance().sampleGrid(bitMatrix, i, i, perspectiveTransform);
    }

    private static int computeDimension(ResultPoint resultPoint, ResultPoint resultPoint2, ResultPoint resultPoint3, float f) throws NotFoundException {
        int round = ((MathUtils.round(ResultPoint.distance(resultPoint, resultPoint2) / f) + MathUtils.round(ResultPoint.distance(resultPoint, resultPoint3) / f)) / 2) + 7;
        int i = round & 3;
        if (i == 0) {
            return round + 1;
        }
        switch (i) {
            case 2:
                return round - 1;
            case 3:
                throw NotFoundException.getNotFoundInstance();
            default:
                return round;
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final float calculateModuleSize(ResultPoint resultPoint, ResultPoint resultPoint2, ResultPoint resultPoint3) {
        return (calculateModuleSizeOneWay(resultPoint, resultPoint2) + calculateModuleSizeOneWay(resultPoint, resultPoint3)) / 2.0f;
    }

    private float calculateModuleSizeOneWay(ResultPoint resultPoint, ResultPoint resultPoint2) {
        float sizeOfBlackWhiteBlackRunBothWays = sizeOfBlackWhiteBlackRunBothWays((int) resultPoint.getX(), (int) resultPoint.getY(), (int) resultPoint2.getX(), (int) resultPoint2.getY());
        float sizeOfBlackWhiteBlackRunBothWays2 = sizeOfBlackWhiteBlackRunBothWays((int) resultPoint2.getX(), (int) resultPoint2.getY(), (int) resultPoint.getX(), (int) resultPoint.getY());
        if (Float.isNaN(sizeOfBlackWhiteBlackRunBothWays)) {
            return sizeOfBlackWhiteBlackRunBothWays2 / 7.0f;
        }
        return Float.isNaN(sizeOfBlackWhiteBlackRunBothWays2) ? sizeOfBlackWhiteBlackRunBothWays / 7.0f : (sizeOfBlackWhiteBlackRunBothWays + sizeOfBlackWhiteBlackRunBothWays2) / 14.0f;
    }

    private float sizeOfBlackWhiteBlackRunBothWays(int i, int i2, int i3, int i4) {
        float f;
        int i5;
        float sizeOfBlackWhiteBlackRun = sizeOfBlackWhiteBlackRun(i, i2, i3, i4);
        i3 = i - (i3 - i);
        int i6 = 0;
        if (i3 < 0) {
            f = ((float) i) / ((float) (i - i3));
            i5 = 0;
        } else if (i3 >= this.image.getWidth()) {
            f = ((float) ((this.image.getWidth() - 1) - i)) / ((float) (i3 - i));
            i5 = this.image.getWidth() - 1;
        } else {
            i5 = i3;
            f = 1.0f;
        }
        float f2 = (float) i2;
        i3 = (int) (f2 - (((float) (i4 - i2)) * f));
        if (i3 < 0) {
            f = f2 / ((float) (i2 - i3));
        } else if (i3 >= this.image.getHeight()) {
            f = ((float) ((this.image.getHeight() - 1) - i2)) / ((float) (i3 - i2));
            i6 = this.image.getHeight() - 1;
        } else {
            i6 = i3;
            f = 1.0f;
        }
        return (sizeOfBlackWhiteBlackRun + sizeOfBlackWhiteBlackRun(i, i2, (int) (((float) i) + (((float) (i5 - i)) * f)), i6)) - 1.0f;
    }

    private float sizeOfBlackWhiteBlackRun(int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10 = 1;
        Object obj = Math.abs(i4 - i2) > Math.abs(i3 - i) ? 1 : null;
        if (obj != null) {
            i5 = i;
            i6 = i2;
            i7 = i3;
            i8 = i4;
        } else {
            i6 = i;
            i5 = i2;
            i8 = i3;
            i7 = i4;
        }
        int abs = Math.abs(i8 - i6);
        int abs2 = Math.abs(i7 - i5);
        int i11 = (-abs) / 2;
        int i12 = -1;
        int i13 = i6 < i8 ? 1 : -1;
        if (i5 < i7) {
            i12 = 1;
        }
        i8 += i13;
        int i14 = i5;
        int i15 = i11;
        int i16 = 0;
        i11 = i6;
        while (i11 != i8) {
            Detector detector;
            Object obj2;
            boolean z;
            int i17 = obj != null ? i14 : i11;
            int i18 = obj != null ? i11 : i14;
            if (i16 == i10) {
                detector = this;
                i9 = i8;
                obj2 = obj;
                z = true;
            } else {
                detector = this;
                i9 = i8;
                obj2 = obj;
                z = false;
            }
            if (z == detector.image.get(i17, i18)) {
                if (i16 == 2) {
                    return MathUtils.distance(i11, i14, i6, i5);
                }
                i16++;
            }
            i15 += abs2;
            if (i15 > 0) {
                if (i14 == i7) {
                    break;
                }
                i14 += i12;
                i15 -= abs;
            }
            i11 += i13;
            obj = obj2;
            i8 = i9;
            i10 = 1;
        }
        i9 = i8;
        return i16 == 2 ? MathUtils.distance(i9, i7, i6, i5) : Float.NaN;
    }

    /* Access modifiers changed, original: protected|final */
    public final AlignmentPattern findAlignmentInRegion(float f, int i, int i2, float f2) throws NotFoundException {
        int i3 = (int) (f2 * f);
        int max = Math.max(0, i - i3);
        int min = Math.min(this.image.getWidth() - 1, i + i3) - max;
        float f3 = 3.0f * f;
        if (((float) min) >= f3) {
            int max2 = Math.max(0, i2 - i3);
            int min2 = Math.min(this.image.getHeight() - 1, i2 + i3) - max2;
            if (((float) min2) >= f3) {
                return new AlignmentPatternFinder(this.image, max, max2, min, min2, f, this.resultPointCallback).find();
            }
            throw NotFoundException.getNotFoundInstance();
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
