package com.google.zxing.common.detector;

import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;

public final class WhiteRectangleDetector {
    private static final int CORR = 1;
    private static final int INIT_SIZE = 10;
    private final int downInit;
    private final int height;
    private final BitMatrix image;
    private final int leftInit;
    private final int rightInit;
    private final int upInit;
    private final int width;

    public WhiteRectangleDetector(BitMatrix bitMatrix) throws NotFoundException {
        this(bitMatrix, 10, bitMatrix.getWidth() / 2, bitMatrix.getHeight() / 2);
    }

    public WhiteRectangleDetector(BitMatrix bitMatrix, int i, int i2, int i3) throws NotFoundException {
        this.image = bitMatrix;
        this.height = bitMatrix.getHeight();
        this.width = bitMatrix.getWidth();
        i /= 2;
        this.leftInit = i2 - i;
        this.rightInit = i2 + i;
        this.upInit = i3 - i;
        this.downInit = i3 + i;
        if (this.upInit < 0 || this.leftInit < 0 || this.downInit >= this.height || this.rightInit >= this.width) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    public ResultPoint[] detect() throws NotFoundException {
        int i = this.leftInit;
        int i2 = this.rightInit;
        int i3 = this.upInit;
        int i4 = this.downInit;
        boolean z = false;
        int i5 = 1;
        int i6 = i;
        Object obj = 1;
        Object obj2 = null;
        Object obj3 = null;
        Object obj4 = null;
        Object obj5 = null;
        Object obj6 = null;
        while (obj != null) {
            boolean z2 = true;
            Object obj7 = null;
            while (true) {
                if ((z2 || obj2 == null) && i2 < this.width) {
                    z2 = containsBlackPoint(i3, i4, i2, false);
                    if (z2) {
                        i2++;
                        obj2 = 1;
                        obj7 = 1;
                    } else if (obj2 == null) {
                        i2++;
                    }
                }
            }
            if (i2 < this.width) {
                z2 = true;
                while (true) {
                    if ((z2 || obj3 == null) && i4 < this.height) {
                        z2 = containsBlackPoint(i6, i2, i4, true);
                        if (z2) {
                            i4++;
                            obj3 = 1;
                            obj7 = 1;
                        } else if (obj3 == null) {
                            i4++;
                        }
                    }
                }
                if (i4 < this.height) {
                    z2 = true;
                    while (true) {
                        if ((z2 || obj4 == null) && i6 >= 0) {
                            z2 = containsBlackPoint(i3, i4, i6, false);
                            if (z2) {
                                i6--;
                                obj4 = 1;
                                obj7 = 1;
                            } else if (obj4 == null) {
                                i6--;
                            }
                        }
                    }
                    if (i6 >= 0) {
                        z2 = true;
                        while (true) {
                            if ((z2 || obj6 == null) && i3 >= 0) {
                                z2 = containsBlackPoint(i6, i2, i3, true);
                                if (z2) {
                                    i3--;
                                    obj6 = 1;
                                    obj7 = 1;
                                } else if (obj6 == null) {
                                    i3--;
                                }
                            }
                        }
                        if (i3 >= 0) {
                            if (obj7 != null) {
                                obj5 = 1;
                            }
                            obj = obj7;
                        }
                    }
                }
            }
            z = true;
            break;
        }
        if (z || obj5 == null) {
            throw NotFoundException.getNotFoundInstance();
        }
        i = i2 - i6;
        ResultPoint resultPoint = null;
        ResultPoint resultPoint2 = null;
        int i7 = 1;
        while (resultPoint2 == null && i7 < i) {
            resultPoint2 = getBlackPointOnSegment((float) i6, (float) (i4 - i7), (float) (i6 + i7), (float) i4);
            i7++;
        }
        if (resultPoint2 != null) {
            ResultPoint resultPoint3 = null;
            int i8 = 1;
            while (resultPoint3 == null && i8 < i) {
                resultPoint3 = getBlackPointOnSegment((float) i6, (float) (i3 + i8), (float) (i6 + i8), (float) i3);
                i8++;
            }
            if (resultPoint3 != null) {
                ResultPoint resultPoint4 = null;
                i8 = 1;
                while (resultPoint4 == null && i8 < i) {
                    resultPoint4 = getBlackPointOnSegment((float) i2, (float) (i3 + i8), (float) (i2 - i8), (float) i3);
                    i8++;
                }
                if (resultPoint4 != null) {
                    while (resultPoint == null && i5 < i) {
                        resultPoint = getBlackPointOnSegment((float) i2, (float) (i4 - i5), (float) (i2 - i5), (float) i4);
                        i5++;
                    }
                    if (resultPoint != null) {
                        return centerEdges(resultPoint, resultPoint2, resultPoint4, resultPoint3);
                    }
                    throw NotFoundException.getNotFoundInstance();
                }
                throw NotFoundException.getNotFoundInstance();
            }
            throw NotFoundException.getNotFoundInstance();
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private ResultPoint getBlackPointOnSegment(float f, float f2, float f3, float f4) {
        int round = MathUtils.round(MathUtils.distance(f, f2, f3, f4));
        float f5 = (float) round;
        f3 = (f3 - f) / f5;
        f4 = (f4 - f2) / f5;
        for (int i = 0; i < round; i++) {
            float f6 = (float) i;
            int round2 = MathUtils.round((f6 * f3) + f);
            int round3 = MathUtils.round((f6 * f4) + f2);
            if (this.image.get(round2, round3)) {
                return new ResultPoint((float) round2, (float) round3);
            }
        }
        return null;
    }

    private ResultPoint[] centerEdges(ResultPoint resultPoint, ResultPoint resultPoint2, ResultPoint resultPoint3, ResultPoint resultPoint4) {
        float x = resultPoint.getX();
        float y = resultPoint.getY();
        float x2 = resultPoint2.getX();
        float y2 = resultPoint2.getY();
        float x3 = resultPoint3.getX();
        float y3 = resultPoint3.getY();
        float x4 = resultPoint4.getX();
        float y4 = resultPoint4.getY();
        if (x < ((float) this.width) / 2.0f) {
            return new ResultPoint[]{new ResultPoint(x4 - 1.0f, y4 + 1.0f), new ResultPoint(x2 + 1.0f, y2 + 1.0f), new ResultPoint(x3 - 1.0f, y3 - 1.0f), new ResultPoint(x + 1.0f, y - 1.0f)};
        }
        return new ResultPoint[]{new ResultPoint(x4 + 1.0f, y4 + 1.0f), new ResultPoint(x2 + 1.0f, y2 - 1.0f), new ResultPoint(x3 - 1.0f, y3 + 1.0f), new ResultPoint(x - 1.0f, y - 1.0f)};
    }

    private boolean containsBlackPoint(int i, int i2, int i3, boolean z) {
        if (z) {
            while (i <= i2) {
                if (this.image.get(i, i3)) {
                    return true;
                }
                i++;
            }
        } else {
            while (i <= i2) {
                if (this.image.get(i3, i)) {
                    return true;
                }
                i++;
            }
        }
        return false;
    }
}
