package com.google.zxing.oned.rss;

import com.google.zxing.ResultPoint;

public final class FinderPattern {
    private final ResultPoint[] resultPoints;
    private final int[] startEnd;
    private final int value;

    public FinderPattern(int i, int[] iArr, int i2, int i3, int i4) {
        this.value = i;
        this.startEnd = iArr;
        r1 = new ResultPoint[2];
        float f = (float) i4;
        r1[0] = new ResultPoint((float) i2, f);
        r1[1] = new ResultPoint((float) i3, f);
        this.resultPoints = r1;
    }

    public int getValue() {
        return this.value;
    }

    public int[] getStartEnd() {
        return this.startEnd;
    }

    public ResultPoint[] getResultPoints() {
        return this.resultPoints;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof FinderPattern)) {
            return false;
        }
        if (this.value == ((FinderPattern) obj).value) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.value;
    }
}
