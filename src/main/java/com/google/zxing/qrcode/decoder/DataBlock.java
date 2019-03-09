package com.google.zxing.qrcode.decoder;

import com.google.zxing.qrcode.decoder.Version.ECB;
import com.google.zxing.qrcode.decoder.Version.ECBlocks;

final class DataBlock {
    private final byte[] codewords;
    private final int numDataCodewords;

    private DataBlock(int i, byte[] bArr) {
        this.numDataCodewords = i;
        this.codewords = bArr;
    }

    static DataBlock[] getDataBlocks(byte[] bArr, Version version, ErrorCorrectionLevel errorCorrectionLevel) {
        if (bArr.length == version.getTotalCodewords()) {
            int i;
            int dataCodewords;
            int i2;
            ECBlocks eCBlocksForLevel = version.getECBlocksForLevel(errorCorrectionLevel);
            ECB[] eCBlocks = eCBlocksForLevel.getECBlocks();
            int i3 = 0;
            for (ECB count : eCBlocks) {
                i3 += count.getCount();
            }
            DataBlock[] dataBlockArr = new DataBlock[i3];
            int length = eCBlocks.length;
            i3 = 0;
            int i4 = 0;
            while (i3 < length) {
                ECB ecb = eCBlocks[i3];
                i = i4;
                i4 = 0;
                while (i4 < ecb.getCount()) {
                    dataCodewords = ecb.getDataCodewords();
                    int i5 = i + 1;
                    dataBlockArr[i] = new DataBlock(dataCodewords, new byte[(eCBlocksForLevel.getECCodewordsPerBlock() + dataCodewords)]);
                    i4++;
                    i = i5;
                }
                i3++;
                i4 = i;
            }
            int length2 = dataBlockArr[0].codewords.length;
            length = dataBlockArr.length - 1;
            while (length >= 0 && dataBlockArr[length].codewords.length != length2) {
                length--;
            }
            length++;
            length2 -= eCBlocksForLevel.getECCodewordsPerBlock();
            int i6 = 0;
            i3 = 0;
            while (i6 < length2) {
                i2 = i3;
                i3 = 0;
                while (i3 < i4) {
                    dataCodewords = i2 + 1;
                    dataBlockArr[i3].codewords[i6] = bArr[i2];
                    i3++;
                    i2 = dataCodewords;
                }
                i6++;
                i3 = i2;
            }
            i6 = length;
            while (i6 < i4) {
                i = i3 + 1;
                dataBlockArr[i6].codewords[length2] = bArr[i3];
                i6++;
                i3 = i;
            }
            i6 = dataBlockArr[0].codewords.length;
            while (length2 < i6) {
                i2 = i3;
                i3 = 0;
                while (i3 < i4) {
                    int i7 = i2 + 1;
                    dataBlockArr[i3].codewords[i3 < length ? length2 : length2 + 1] = bArr[i2];
                    i3++;
                    i2 = i7;
                }
                length2++;
                i3 = i2;
            }
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
