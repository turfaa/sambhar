package com.google.zxing.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import java.util.Map;

public final class QRCodeWriter implements Writer {
    private static final int QUIET_ZONE_SIZE = 4;

    public BitMatrix encode(String str, BarcodeFormat barcodeFormat, int i, int i2) throws WriterException {
        return encode(str, barcodeFormat, i, i2, null);
    }

    public BitMatrix encode(String str, BarcodeFormat barcodeFormat, int i, int i2, Map<EncodeHintType, ?> map) throws WriterException {
        if (str.isEmpty()) {
            throw new IllegalArgumentException("Found empty contents");
        } else if (barcodeFormat != BarcodeFormat.QR_CODE) {
            StringBuilder stringBuilder = new StringBuilder("Can only encode QR_CODE, but got ");
            stringBuilder.append(barcodeFormat);
            throw new IllegalArgumentException(stringBuilder.toString());
        } else if (i < 0 || i2 < 0) {
            StringBuilder stringBuilder2 = new StringBuilder("Requested dimensions are too small: ");
            stringBuilder2.append(i);
            stringBuilder2.append('x');
            stringBuilder2.append(i2);
            throw new IllegalArgumentException(stringBuilder2.toString());
        } else {
            ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.L;
            int i3 = 4;
            if (map != null) {
                if (map.containsKey(EncodeHintType.ERROR_CORRECTION)) {
                    errorCorrectionLevel = ErrorCorrectionLevel.valueOf(map.get(EncodeHintType.ERROR_CORRECTION).toString());
                }
                if (map.containsKey(EncodeHintType.MARGIN)) {
                    i3 = Integer.parseInt(map.get(EncodeHintType.MARGIN).toString());
                }
            }
            return renderResult(Encoder.encode(str, errorCorrectionLevel, map), i, i2, i3);
        }
    }

    private static BitMatrix renderResult(QRCode qRCode, int i, int i2, int i3) {
        ByteMatrix matrix = qRCode.getMatrix();
        if (matrix != null) {
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            i3 <<= 1;
            int i4 = width + i3;
            i3 += height;
            i = Math.max(i, i4);
            i2 = Math.max(i2, i3);
            i3 = Math.min(i / i4, i2 / i3);
            i4 = (i - (width * i3)) / 2;
            int i5 = (i2 - (height * i3)) / 2;
            BitMatrix bitMatrix = new BitMatrix(i, i2);
            i2 = 0;
            while (i2 < height) {
                int i6 = i4;
                int i7 = 0;
                while (i7 < width) {
                    if (matrix.get(i7, i2) == (byte) 1) {
                        bitMatrix.setRegion(i6, i5, i3, i3);
                    }
                    i7++;
                    i6 += i3;
                }
                i2++;
                i5 += i3;
            }
            return bitMatrix;
        }
        throw new IllegalStateException();
    }
}
