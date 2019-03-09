package com.google.zxing.datamatrix.decoder;

final class DataBlock {
    private final byte[] codewords;
    private final int numDataCodewords;

    private DataBlock(int i, byte[] bArr) {
        this.numDataCodewords = i;
        this.codewords = bArr;
    }

    static DataBlock[] getDataBlocks(byte[] bArr, Version version) {
        int i;
        int dataCodewords;
        int i2;
        ECBlocks eCBlocks = version.getECBlocks();
        ECB[] eCBlocks2 = eCBlocks.getECBlocks();
        int i3 = 0;
        for (ECB count : eCBlocks2) {
            i3 += count.getCount();
        }
        DataBlock[] dataBlockArr = new DataBlock[i3];
        int length = eCBlocks2.length;
        i3 = 0;
        int i4 = 0;
        while (i3 < length) {
            ECB ecb = eCBlocks2[i3];
            i = i4;
            i4 = 0;
            while (i4 < ecb.getCount()) {
                dataCodewords = ecb.getDataCodewords();
                int i5 = i + 1;
                dataBlockArr[i] = new DataBlock(dataCodewords, new byte[(eCBlocks.getECCodewords() + dataCodewords)]);
                i4++;
                i = i5;
            }
            i3++;
            i4 = i;
        }
        int length2 = dataBlockArr[0].codewords.length - eCBlocks.getECCodewords();
        int i6 = length2 - 1;
        length = 0;
        i3 = 0;
        while (length < i6) {
            i2 = i3;
            i3 = 0;
            while (i3 < i4) {
                dataCodewords = i2 + 1;
                dataBlockArr[i3].codewords[length] = bArr[i2];
                i3++;
                i2 = dataCodewords;
            }
            length++;
            i3 = i2;
        }
        Object obj = version.getVersionNumber() == 24 ? 1 : null;
        length = obj != null ? 8 : i4;
        i2 = i3;
        i3 = 0;
        while (i3 < length) {
            dataCodewords = i2 + 1;
            dataBlockArr[i3].codewords[i6] = bArr[i2];
            i3++;
            i2 = dataCodewords;
        }
        i6 = dataBlockArr[0].codewords.length;
        while (length2 < i6) {
            length = 0;
            while (length < i4) {
                i3 = obj != null ? (length + 8) % i4 : length;
                i = (obj == null || i3 <= 7) ? length2 : length2 - 1;
                dataCodewords = i2 + 1;
                dataBlockArr[i3].codewords[i] = bArr[i2];
                length++;
                i2 = dataCodewords;
            }
            length2++;
        }
        if (i2 == bArr.length) {
            return dataBlockArr;
        }
        throw new IllegalArgumentException();
    }

    /* Access modifiers changed, original: 0000 */
    public int getNumDataCodewords() {
        return this.numDataCodewords;
    }

    /* Access modifiers changed, original: 0000 */
    public byte[] getCodewords() {
        return this.codewords;
    }
}
