package com.google.zxing.qrcode.decoder;

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
    private final ReedSolomonDecoder rsDecoder = new ReedSolomonDecoder(GenericGF.QR_CODE_FIELD_256);

    public DecoderResult decode(boolean[][] zArr) throws ChecksumException, FormatException {
        return decode(zArr, null);
    }

    public DecoderResult decode(boolean[][] zArr, Map<DecodeHintType, ?> map) throws ChecksumException, FormatException {
        int length = zArr.length;
        BitMatrix bitMatrix = new BitMatrix(length);
        for (int i = 0; i < length; i++) {
            for (int i2 = 0; i2 < length; i2++) {
                if (zArr[i][i2]) {
                    bitMatrix.set(i2, i);
                }
            }
        }
        return decode(bitMatrix, (Map) map);
    }

    public DecoderResult decode(BitMatrix bitMatrix) throws ChecksumException, FormatException {
        return decode(bitMatrix, null);
    }

    public DecoderResult decode(BitMatrix bitMatrix, Map<DecodeHintType, ?> map) throws FormatException, ChecksumException {
        ChecksumException e;
        DecoderResult decode;
        BitMatrixParser bitMatrixParser = new BitMatrixParser(bitMatrix);
        FormatException formatException = null;
        try {
            return decode(bitMatrixParser, (Map) map);
        } catch (FormatException e2) {
            FormatException formatException2 = e2;
            e = null;
            formatException = formatException2;
            try {
                bitMatrixParser.remask();
                bitMatrixParser.setMirror(true);
                bitMatrixParser.readVersion();
                bitMatrixParser.readFormatInformation();
                bitMatrixParser.mirror();
                decode = decode(bitMatrixParser, (Map) map);
                decode.setOther(new QRCodeDecoderMetaData(true));
                return decode;
            } catch (ChecksumException | FormatException e3) {
                if (formatException != null) {
                    throw formatException;
                } else if (e != null) {
                    throw e;
                } else {
                    throw e3;
                }
            }
        } catch (ChecksumException e4) {
            e = e4;
            bitMatrixParser.remask();
            bitMatrixParser.setMirror(true);
            bitMatrixParser.readVersion();
            bitMatrixParser.readFormatInformation();
            bitMatrixParser.mirror();
            decode = decode(bitMatrixParser, (Map) map);
            decode.setOther(new QRCodeDecoderMetaData(true));
            return decode;
        }
    }

    private DecoderResult decode(BitMatrixParser bitMatrixParser, Map<DecodeHintType, ?> map) throws FormatException, ChecksumException {
        Version readVersion = bitMatrixParser.readVersion();
        ErrorCorrectionLevel errorCorrectionLevel = bitMatrixParser.readFormatInformation().getErrorCorrectionLevel();
        DataBlock[] dataBlocks = DataBlock.getDataBlocks(bitMatrixParser.readCodewords(), readVersion, errorCorrectionLevel);
        int i = 0;
        for (DataBlock numDataCodewords : dataBlocks) {
            i += numDataCodewords.getNumDataCodewords();
        }
        byte[] bArr = new byte[i];
        int length = dataBlocks.length;
        i = 0;
        int i2 = 0;
        while (i < length) {
            DataBlock dataBlock = dataBlocks[i];
            byte[] codewords = dataBlock.getCodewords();
            int numDataCodewords2 = dataBlock.getNumDataCodewords();
            correctErrors(codewords, numDataCodewords2);
            int i3 = i2;
            i2 = 0;
            while (i2 < numDataCodewords2) {
                int i4 = i3 + 1;
                bArr[i3] = codewords[i2];
                i2++;
                i3 = i4;
            }
            i++;
            i2 = i3;
        }
        return DecodedBitStreamParser.decode(bArr, readVersion, errorCorrectionLevel, map);
    }

    private void correctErrors(byte[] bArr, int i) throws ChecksumException {
        int length = bArr.length;
        int[] iArr = new int[length];
        for (int i2 = 0; i2 < length; i2++) {
            iArr[i2] = bArr[i2] & 255;
        }
        try {
            this.rsDecoder.decode(iArr, bArr.length - i);
            for (int i3 = 0; i3 < i; i3++) {
                bArr[i3] = (byte) iArr[i3];
            }
        } catch (ReedSolomonException unused) {
            throw ChecksumException.getChecksumInstance();
        }
    }
}
