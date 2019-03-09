package com.google.zxing.common;

import java.util.Arrays;

public final class BitArray implements Cloneable {
    private int[] bits;
    private int size;

    public BitArray() {
        this.size = 0;
        this.bits = new int[1];
    }

    public BitArray(int i) {
        this.size = i;
        this.bits = makeArray(i);
    }

    BitArray(int[] iArr, int i) {
        this.bits = iArr;
        this.size = i;
    }

    public int getSize() {
        return this.size;
    }

    public int getSizeInBytes() {
        return (this.size + 7) / 8;
    }

    private void ensureCapacity(int i) {
        if (i > (this.bits.length << 5)) {
            int[] makeArray = makeArray(i);
            System.arraycopy(this.bits, 0, makeArray, 0, this.bits.length);
            this.bits = makeArray;
        }
    }

    public boolean get(int i) {
        return ((1 << (i & 31)) & this.bits[i / 32]) != 0;
    }

    public void set(int i) {
        int[] iArr = this.bits;
        int i2 = i / 32;
        iArr[i2] = (1 << (i & 31)) | iArr[i2];
    }

    public void flip(int i) {
        int[] iArr = this.bits;
        int i2 = i / 32;
        iArr[i2] = (1 << (i & 31)) ^ iArr[i2];
    }

    public int getNextSet(int i) {
        if (i >= this.size) {
            return this.size;
        }
        int i2 = i / 32;
        i = (((1 << (i & 31)) - 1) ^ -1) & this.bits[i2];
        while (i == 0) {
            i2++;
            if (i2 == this.bits.length) {
                return this.size;
            }
            i = this.bits[i2];
        }
        i2 = (i2 << 5) + Integer.numberOfTrailingZeros(i);
        return i2 > this.size ? this.size : i2;
    }

    public int getNextUnset(int i) {
        if (i >= this.size) {
            return this.size;
        }
        int i2 = i / 32;
        i = (((1 << (i & 31)) - 1) ^ -1) & (this.bits[i2] ^ -1);
        while (i == 0) {
            i2++;
            if (i2 == this.bits.length) {
                return this.size;
            }
            i = this.bits[i2] ^ -1;
        }
        i2 = (i2 << 5) + Integer.numberOfTrailingZeros(i);
        return i2 > this.size ? this.size : i2;
    }

    public void setBulk(int i, int i2) {
        this.bits[i / 32] = i2;
    }

    public void setRange(int i, int i2) {
        if (i2 < i || i < 0 || i2 > this.size) {
            throw new IllegalArgumentException();
        } else if (i2 != i) {
            i2--;
            int i3 = i / 32;
            int i4 = i2 / 32;
            int i5 = i3;
            while (i5 <= i4) {
                int i6 = 31;
                int i7 = i5 > i3 ? 0 : i & 31;
                if (i5 >= i4) {
                    i6 = 31 & i2;
                }
                i6 = (2 << i6) - (1 << i7);
                int[] iArr = this.bits;
                iArr[i5] = i6 | iArr[i5];
                i5++;
            }
        }
    }

    public void clear() {
        int length = this.bits.length;
        for (int i = 0; i < length; i++) {
            this.bits[i] = 0;
        }
    }

    public boolean isRange(int i, int i2, boolean z) {
        if (i2 < i || i < 0 || i2 > this.size) {
            throw new IllegalArgumentException();
        } else if (i2 == i) {
            return true;
        } else {
            i2--;
            int i3 = i / 32;
            int i4 = i2 / 32;
            int i5 = i3;
            while (i5 <= i4) {
                int i6 = 31;
                int i7 = i5 > i3 ? 0 : i & 31;
                if (i5 >= i4) {
                    i6 = 31 & i2;
                }
                i6 = (2 << i6) - (1 << i7);
                i7 = this.bits[i5] & i6;
                if (!z) {
                    i6 = 0;
                }
                if (i7 != i6) {
                    return false;
                }
                i5++;
            }
            return true;
        }
    }

    public void appendBit(boolean z) {
        ensureCapacity(this.size + 1);
        if (z) {
            int[] iArr = this.bits;
            int i = this.size / 32;
            iArr[i] = iArr[i] | (1 << (this.size & 31));
        }
        this.size++;
    }

    public void appendBits(int i, int i2) {
        if (i2 < 0 || i2 > 32) {
            throw new IllegalArgumentException("Num bits must be between 0 and 32");
        }
        ensureCapacity(this.size + i2);
        while (i2 > 0) {
            boolean z = true;
            if (((i >> (i2 - 1)) & 1) != 1) {
                z = false;
            }
            appendBit(z);
            i2--;
        }
    }

    public void appendBitArray(BitArray bitArray) {
        int i = bitArray.size;
        ensureCapacity(this.size + i);
        for (int i2 = 0; i2 < i; i2++) {
            appendBit(bitArray.get(i2));
        }
    }

    public void xor(BitArray bitArray) {
        if (this.size == bitArray.size) {
            for (int i = 0; i < this.bits.length; i++) {
                int[] iArr = this.bits;
                iArr[i] = iArr[i] ^ bitArray.bits[i];
            }
            return;
        }
        throw new IllegalArgumentException("Sizes don't match");
    }

    public void toBytes(int i, byte[] bArr, int i2, int i3) {
        int i4 = i;
        i = 0;
        while (i < i3) {
            int i5 = i4;
            int i6 = 0;
            for (i4 = 0; i4 < 8; i4++) {
                if (get(i5)) {
                    i6 |= 1 << (7 - i4);
                }
                i5++;
            }
            bArr[i2 + i] = (byte) i6;
            i++;
            i4 = i5;
        }
    }

    public int[] getBitArray() {
        return this.bits;
    }

    public void reverse() {
        int i;
        int[] iArr = new int[this.bits.length];
        int i2 = (this.size - 1) / 32;
        int i3 = i2 + 1;
        for (i = 0; i < i3; i++) {
            long j = (long) this.bits[i];
            j = ((j & 1431655765) << 1) | ((j >> 1) & 1431655765);
            j = ((j & 858993459) << 2) | ((j >> 2) & 858993459);
            j = ((j & 252645135) << 4) | ((j >> 4) & 252645135);
            j = ((j & 16711935) << 8) | ((j >> 8) & 16711935);
            iArr[i2 - i] = (int) (((j & 65535) << 16) | ((j >> 16) & 65535));
        }
        i = i3 << 5;
        if (this.size != i) {
            i -= this.size;
            int i4 = iArr[0] >>> i;
            for (i2 = 1; i2 < i3; i2++) {
                int i5 = iArr[i2];
                iArr[i2 - 1] = i4 | (i5 << (32 - i));
                i4 = i5 >>> i;
            }
            iArr[i3 - 1] = i4;
        }
        this.bits = iArr;
    }

    private static int[] makeArray(int i) {
        return new int[((i + 31) / 32)];
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BitArray)) {
            return false;
        }
        BitArray bitArray = (BitArray) obj;
        if (this.size == bitArray.size && Arrays.equals(this.bits, bitArray.bits)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (this.size * 31) + Arrays.hashCode(this.bits);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(this.size);
        for (int i = 0; i < this.size; i++) {
            if ((i & 7) == 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(get(i) ? 'X' : '.');
        }
        return stringBuilder.toString();
    }

    public BitArray clone() {
        return new BitArray((int[]) this.bits.clone(), this.size);
    }
}
