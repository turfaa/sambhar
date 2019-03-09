package com.google.zxing.multi.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.google.zxing.multi.qrcode.detector.MultiDetector;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.QRCodeDecoderMetaData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class QRCodeMultiReader extends QRCodeReader implements MultipleBarcodeReader {
    private static final Result[] EMPTY_RESULT_ARRAY = new Result[0];
    private static final ResultPoint[] NO_POINTS = new ResultPoint[0];

    private static final class SAComparator implements Serializable, Comparator<Result> {
        private SAComparator() {
        }

        public int compare(Result result, Result result2) {
            int intValue = ((Integer) result.getResultMetadata().get(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE)).intValue();
            int intValue2 = ((Integer) result2.getResultMetadata().get(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE)).intValue();
            if (intValue < intValue2) {
                return -1;
            }
            return intValue > intValue2 ? 1 : 0;
        }
    }

    public Result[] decodeMultiple(BinaryBitmap binaryBitmap) throws NotFoundException {
        return decodeMultiple(binaryBitmap, null);
    }

    public Result[] decodeMultiple(BinaryBitmap binaryBitmap, Map<DecodeHintType, ?> map) throws NotFoundException {
        ArrayList arrayList = new ArrayList();
        for (DetectorResult detectorResult : new MultiDetector(binaryBitmap.getBlackMatrix()).detectMulti(map)) {
            try {
                DecoderResult decode = getDecoder().decode(detectorResult.getBits(), (Map) map);
                ResultPoint[] points = detectorResult.getPoints();
                if (decode.getOther() instanceof QRCodeDecoderMetaData) {
                    ((QRCodeDecoderMetaData) decode.getOther()).applyMirroredCorrection(points);
                }
                Result result = new Result(decode.getText(), decode.getRawBytes(), points, BarcodeFormat.QR_CODE);
                List byteSegments = decode.getByteSegments();
                if (byteSegments != null) {
                    result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
                }
                String eCLevel = decode.getECLevel();
                if (eCLevel != null) {
                    result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, eCLevel);
                }
                if (decode.hasStructuredAppend()) {
                    result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE, Integer.valueOf(decode.getStructuredAppendSequenceNumber()));
                    result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_PARITY, Integer.valueOf(decode.getStructuredAppendParity()));
                }
                arrayList.add(result);
            } catch (ReaderException unused) {
            }
        }
        if (arrayList.isEmpty()) {
            return EMPTY_RESULT_ARRAY;
        }
        List processStructuredAppend = processStructuredAppend(arrayList);
        return (Result[]) processStructuredAppend.toArray(new Result[processStructuredAppend.size()]);
    }

    private static List<Result> processStructuredAppend(List<Result> list) {
        Result result;
        Object obj;
        for (Result result2 : list) {
            if (result2.getResultMetadata().containsKey(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE)) {
                obj = 1;
                break;
            }
        }
        obj = null;
        if (obj == null) {
            return list;
        }
        ArrayList arrayList = new ArrayList();
        ArrayList<Result> arrayList2 = new ArrayList();
        for (Result result3 : list) {
            arrayList.add(result3);
            if (result3.getResultMetadata().containsKey(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE)) {
                arrayList2.add(result3);
            }
        }
        Collections.sort(arrayList2, new SAComparator());
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        int i2 = 0;
        for (Result result4 : arrayList2) {
            stringBuilder.append(result4.getText());
            i += result4.getRawBytes().length;
            if (result4.getResultMetadata().containsKey(ResultMetadataType.BYTE_SEGMENTS)) {
                for (byte[] length : (Iterable) result4.getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)) {
                    i2 += length.length;
                }
            }
        }
        byte[] bArr = new byte[i];
        byte[] bArr2 = new byte[i2];
        int i3 = 0;
        int i4 = 0;
        for (Result result5 : arrayList2) {
            System.arraycopy(result5.getRawBytes(), 0, bArr, i3, result5.getRawBytes().length);
            i3 += result5.getRawBytes().length;
            if (result5.getResultMetadata().containsKey(ResultMetadataType.BYTE_SEGMENTS)) {
                for (byte[] bArr3 : (Iterable) result5.getResultMetadata().get(ResultMetadataType.BYTE_SEGMENTS)) {
                    System.arraycopy(bArr3, 0, bArr2, i4, bArr3.length);
                    i4 += bArr3.length;
                }
            }
        }
        result2 = new Result(stringBuilder.toString(), bArr, NO_POINTS, BarcodeFormat.QR_CODE);
        if (i2 > 0) {
            ArrayList arrayList3 = new ArrayList();
            arrayList3.add(bArr2);
            result2.putMetadata(ResultMetadataType.BYTE_SEGMENTS, arrayList3);
        }
        arrayList.add(result2);
        return arrayList;
    }
}
