package com.google.zxing.pdf417.decoder.ec;

final class ModulusPoly {
    private final int[] coefficients;
    private final ModulusGF field;

    ModulusPoly(ModulusGF modulusGF, int[] iArr) {
        if (iArr.length != 0) {
            this.field = modulusGF;
            int length = iArr.length;
            if (length <= 1 || iArr[0] != 0) {
                this.coefficients = iArr;
                return;
            }
            int i = 1;
            while (i < length && iArr[i] == 0) {
                i++;
            }
            if (i == length) {
                this.coefficients = new int[]{0};
                return;
            }
            this.coefficients = new int[(length - i)];
            System.arraycopy(iArr, i, this.coefficients, 0, this.coefficients.length);
            return;
        }
        throw new IllegalArgumentException();
    }

    /* Access modifiers changed, original: 0000 */
    public int[] getCoefficients() {
        return this.coefficients;
    }

    /* Access modifiers changed, original: 0000 */
    public int getDegree() {
        return this.coefficients.length - 1;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isZero() {
        return this.coefficients[0] == 0;
    }

    /* Access modifiers changed, original: 0000 */
    public int getCoefficient(int i) {
        return this.coefficients[(this.coefficients.length - 1) - i];
    }

    /* Access modifiers changed, original: 0000 */
    public int evaluateAt(int i) {
        int i2 = 0;
        if (i == 0) {
            return getCoefficient(0);
        }
        int i3 = 1;
        int i4;
        if (i == 1) {
            int[] iArr = this.coefficients;
            i3 = iArr.length;
            i4 = 0;
            while (i2 < i3) {
                i4 = this.field.add(i4, iArr[i2]);
                i2++;
            }
            return i4;
        }
        i2 = this.coefficients[0];
        i4 = this.coefficients.length;
        while (i3 < i4) {
            i2 = this.field.add(this.field.multiply(i, i2), this.coefficients[i3]);
            i3++;
        }
        return i2;
    }

    /* Access modifiers changed, original: 0000 */
    public ModulusPoly add(ModulusPoly modulusPoly) {
        if (!this.field.equals(modulusPoly.field)) {
            throw new IllegalArgumentException("ModulusPolys do not have same ModulusGF field");
        } else if (isZero()) {
            return modulusPoly;
        } else {
            if (modulusPoly.isZero()) {
                return this;
            }
            int[] iArr = this.coefficients;
            Object obj = modulusPoly.coefficients;
            if (iArr.length > obj.length) {
                Object obj2 = iArr;
                iArr = obj;
                obj = obj2;
            }
            int[] iArr2 = new int[obj.length];
            int length = obj.length - iArr.length;
            System.arraycopy(obj, 0, iArr2, 0, length);
            for (int i = length; i < obj.length; i++) {
                iArr2[i] = this.field.add(iArr[i - length], obj[i]);
            }
            return new ModulusPoly(this.field, iArr2);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public ModulusPoly subtract(ModulusPoly modulusPoly) {
        if (!this.field.equals(modulusPoly.field)) {
            throw new IllegalArgumentException("ModulusPolys do not have same ModulusGF field");
        } else if (modulusPoly.isZero()) {
            return this;
        } else {
            return add(modulusPoly.negative());
        }
    }

    /* Access modifiers changed, original: 0000 */
    public ModulusPoly multiply(ModulusPoly modulusPoly) {
        if (!this.field.equals(modulusPoly.field)) {
            throw new IllegalArgumentException("ModulusPolys do not have same ModulusGF field");
        } else if (isZero() || modulusPoly.isZero()) {
            return this.field.getZero();
        } else {
            int[] iArr = this.coefficients;
            int length = iArr.length;
            int[] iArr2 = modulusPoly.coefficients;
            int length2 = iArr2.length;
            int[] iArr3 = new int[((length + length2) - 1)];
            for (int i = 0; i < length; i++) {
                int i2 = iArr[i];
                for (int i3 = 0; i3 < length2; i3++) {
                    int i4 = i + i3;
                    iArr3[i4] = this.field.add(iArr3[i4], this.field.multiply(i2, iArr2[i3]));
                }
            }
            return new ModulusPoly(this.field, iArr3);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public ModulusPoly negative() {
        int length = this.coefficients.length;
        int[] iArr = new int[length];
        for (int i = 0; i < length; i++) {
            iArr[i] = this.field.subtract(0, this.coefficients[i]);
        }
        return new ModulusPoly(this.field, iArr);
    }

    /* Access modifiers changed, original: 0000 */
    public ModulusPoly multiply(int i) {
        if (i == 0) {
            return this.field.getZero();
        }
        if (i == 1) {
            return this;
        }
        int length = this.coefficients.length;
        int[] iArr = new int[length];
        for (int i2 = 0; i2 < length; i2++) {
            iArr[i2] = this.field.multiply(this.coefficients[i2], i);
        }
        return new ModulusPoly(this.field, iArr);
    }

    /* Access modifiers changed, original: 0000 */
    public ModulusPoly multiplyByMonomial(int i, int i2) {
        if (i < 0) {
            throw new IllegalArgumentException();
        } else if (i2 == 0) {
            return this.field.getZero();
        } else {
            int length = this.coefficients.length;
            int[] iArr = new int[(i + length)];
            for (int i3 = 0; i3 < length; i3++) {
                iArr[i3] = this.field.multiply(this.coefficients[i3], i2);
            }
            return new ModulusPoly(this.field, iArr);
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(getDegree() * 8);
        for (int degree = getDegree(); degree >= 0; degree--) {
            int coefficient = getCoefficient(degree);
            if (coefficient != 0) {
                if (coefficient < 0) {
                    stringBuilder.append(" - ");
                    coefficient = -coefficient;
                } else if (stringBuilder.length() > 0) {
                    stringBuilder.append(" + ");
                }
                if (degree == 0 || coefficient != 1) {
                    stringBuilder.append(coefficient);
                }
                if (degree != 0) {
                    if (degree == 1) {
                        stringBuilder.append('x');
                    } else {
                        stringBuilder.append("x^");
                        stringBuilder.append(degree);
                    }
                }
            }
        }
        return stringBuilder.toString();
    }
}
