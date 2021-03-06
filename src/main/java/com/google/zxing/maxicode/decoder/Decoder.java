package com.google.zxing.maxicode.decoder;

import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;
import java.util.Map;

public final class Decoder {
    private static final int ALL = 0;
    private static final int EVEN = 1;
    private static final int ODD = 2;
    private final ReedSolomonDecoder rsDecoder = new ReedSolomonDecoder(GenericGF.MAXICODE_FIELD_64);

    public DecoderResult decode(BitMatrix bitMatrix) throws ChecksumException, FormatException {
        return decode(bitMatrix, null);
    }

    public DecoderResult decode(BitMatrix bitMatrix, Map<DecodeHintType, ?> map) throws FormatException, ChecksumException {
        Object obj;
        byte[] readCodewords = new BitMatrixParser(bitMatrix).readCodewords();
        correctErrors(readCodewords, 0, 10, 10, 0);
        int i = readCodewords[0] & 15;
        byte[] bArr;
        switch (i) {
            case 2:
            case 3:
            case 4:
                bArr = readCodewords;
                correctErrors(bArr, 20, 84, 40, 1);
                correctErrors(bArr, 20, 84, 40, 2);
                obj = new byte[94];
                break;
            case 5:
                bArr = readCodewords;
                correctErrors(bArr, 20, 68, 56, 1);
                correctErrors(bArr, 20, 68, 56, 2);
                obj = new byte[78];
                break;
            default:
                throw FormatException.getFormatInstance();
        }
        System.arraycopy(readCodewords, 0, obj, 0, 10);
        System.arraycopy(readCodewords, 20, obj, 10, obj.length - 10);
        return DecodedBitStreamParser.decode(obj, i);
    }

    private void correctErrors(byte[] bArr, int i, int i2, int i3, int i4) throws ChecksumException {
        int i5 = i2 + i3;
        int i6 = i4 == 0 ? 1 : 2;
        int[] iArr = new int[(i5 / i6)];
        int i7 = 0;
        int i8 = 0;
        while (i8 < i5) {
            if (i4 == 0 || i8 % 2 == i4 - 1) {
                iArr[i8 / i6] = bArr[i8 + i] & 255;
            }
            i8++;
        }
        try {
            this.rsDecoder.decode(iArr, i3 / i6);
            while (i7 < i2) {
                if (i4 == 0 || i7 % 2 == i4 - 1) {
                    bArr[i7 + i] = (byte) iArr[i7 / i6];
                }
                i7++;
            }
        } catch (ReedSolomonException unused) {
            throw ChecksumException.getChecksumInstance();
        }
    }
}
