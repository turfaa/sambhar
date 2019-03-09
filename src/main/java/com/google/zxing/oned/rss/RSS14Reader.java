package com.google.zxing.oned.rss;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.detector.MathUtils;
import com.google.zxing.oned.OneDReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class RSS14Reader extends AbstractRSSReader {
    private static final int[][] FINDER_PATTERNS = new int[][]{new int[]{3, 8, 2, 1}, new int[]{3, 5, 5, 1}, new int[]{3, 3, 7, 1}, new int[]{3, 1, 9, 1}, new int[]{2, 7, 4, 1}, new int[]{2, 5, 6, 1}, new int[]{2, 3, 8, 1}, new int[]{1, 5, 7, 1}, new int[]{1, 3, 9, 1}};
    private static final int[] INSIDE_GSUM = new int[]{0, 336, 1036, 1516};
    private static final int[] INSIDE_ODD_TOTAL_SUBSET = new int[]{4, 20, 48, 81};
    private static final int[] INSIDE_ODD_WIDEST = new int[]{2, 4, 6, 8};
    private static final int[] OUTSIDE_EVEN_TOTAL_SUBSET = new int[]{1, 10, 34, 70, 126};
    private static final int[] OUTSIDE_GSUM = new int[]{0, 161, 961, 2015, 2715};
    private static final int[] OUTSIDE_ODD_WIDEST = new int[]{8, 6, 4, 3, 1};
    private final List<Pair> possibleLeftPairs = new ArrayList();
    private final List<Pair> possibleRightPairs = new ArrayList();

    public Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws NotFoundException {
        addOrTally(this.possibleLeftPairs, decodePair(bitArray, false, i, map));
        bitArray.reverse();
        addOrTally(this.possibleRightPairs, decodePair(bitArray, true, i, map));
        bitArray.reverse();
        for (Pair pair : this.possibleLeftPairs) {
            if (pair.getCount() > 1) {
                for (Pair pair2 : this.possibleRightPairs) {
                    if (pair2.getCount() > 1 && checkChecksum(pair, pair2)) {
                        return constructResult(pair, pair2);
                    }
                }
                continue;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static void addOrTally(Collection<Pair> collection, Pair pair) {
        if (pair != null) {
            Object obj = null;
            for (Pair pair2 : collection) {
                if (pair2.getValue() == pair.getValue()) {
                    pair2.incrementCount();
                    obj = 1;
                    break;
                }
            }
            if (obj == null) {
                collection.add(pair);
            }
        }
    }

    public void reset() {
        this.possibleLeftPairs.clear();
        this.possibleRightPairs.clear();
    }

    private static Result constructResult(Pair pair, Pair pair2) {
        int length;
        String valueOf = String.valueOf((((long) pair.getValue()) * 4537077) + ((long) pair2.getValue()));
        StringBuilder stringBuilder = new StringBuilder(14);
        for (length = 13 - valueOf.length(); length > 0; length--) {
            stringBuilder.append('0');
        }
        stringBuilder.append(valueOf);
        int i = 0;
        for (length = 0; length < 13; length++) {
            int charAt = stringBuilder.charAt(length) - 48;
            if ((length & 1) == 0) {
                charAt *= 3;
            }
            i += charAt;
        }
        int i2 = 10 - (i % 10);
        if (i2 == 10) {
            i2 = 0;
        }
        stringBuilder.append(i2);
        ResultPoint[] resultPoints = pair.getFinderPattern().getResultPoints();
        ResultPoint[] resultPoints2 = pair2.getFinderPattern().getResultPoints();
        return new Result(String.valueOf(stringBuilder.toString()), null, new ResultPoint[]{resultPoints[0], resultPoints[1], resultPoints2[0], resultPoints2[1]}, BarcodeFormat.RSS_14);
    }

    private static boolean checkChecksum(Pair pair, Pair pair2) {
        int checksumPortion = (pair.getChecksumPortion() + (pair2.getChecksumPortion() * 16)) % 79;
        int value = (pair.getFinderPattern().getValue() * 9) + pair2.getFinderPattern().getValue();
        if (value > 72) {
            value--;
        }
        if (value > 8) {
            value--;
        }
        return checksumPortion == value;
    }

    private Pair decodePair(BitArray bitArray, boolean z, int i, Map<DecodeHintType, ?> map) {
        try {
            ResultPointCallback resultPointCallback;
            int[] findFinderPattern = findFinderPattern(bitArray, 0, z);
            FinderPattern parseFoundFinderPattern = parseFoundFinderPattern(bitArray, i, z, findFinderPattern);
            if (map == null) {
                resultPointCallback = null;
            } else {
                resultPointCallback = (ResultPointCallback) map.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
            }
            if (resultPointCallback != null) {
                float f = ((float) (findFinderPattern[0] + findFinderPattern[1])) / 2.0f;
                if (z) {
                    f = ((float) (bitArray.getSize() - 1)) - f;
                }
                resultPointCallback.foundPossibleResultPoint(new ResultPoint(f, (float) i));
            }
            DataCharacter decodeDataCharacter = decodeDataCharacter(bitArray, parseFoundFinderPattern, true);
            DataCharacter decodeDataCharacter2 = decodeDataCharacter(bitArray, parseFoundFinderPattern, false);
            return new Pair((decodeDataCharacter.getValue() * 1597) + decodeDataCharacter2.getValue(), decodeDataCharacter.getChecksumPortion() + (decodeDataCharacter2.getChecksumPortion() * 4), parseFoundFinderPattern);
        } catch (NotFoundException unused) {
            return null;
        }
    }

    private DataCharacter decodeDataCharacter(BitArray bitArray, FinderPattern finderPattern, boolean z) throws NotFoundException {
        int i;
        int length;
        BitArray bitArray2 = bitArray;
        boolean z2 = z;
        int[] dataCharacterCounters = getDataCharacterCounters();
        dataCharacterCounters[0] = 0;
        dataCharacterCounters[1] = 0;
        dataCharacterCounters[2] = 0;
        dataCharacterCounters[3] = 0;
        dataCharacterCounters[4] = 0;
        dataCharacterCounters[5] = 0;
        dataCharacterCounters[6] = 0;
        dataCharacterCounters[7] = 0;
        if (z2) {
            OneDReader.recordPatternInReverse(bitArray2, finderPattern.getStartEnd()[0], dataCharacterCounters);
        } else {
            OneDReader.recordPattern(bitArray2, finderPattern.getStartEnd()[1] + 1, dataCharacterCounters);
            i = 0;
            for (length = dataCharacterCounters.length - 1; i < length; length--) {
                int i2 = dataCharacterCounters[i];
                dataCharacterCounters[i] = dataCharacterCounters[length];
                dataCharacterCounters[length] = i2;
                i++;
            }
        }
        i = z2 ? 16 : 15;
        float sum = ((float) MathUtils.sum(dataCharacterCounters)) / ((float) i);
        int[] oddCounts = getOddCounts();
        int[] evenCounts = getEvenCounts();
        float[] oddRoundingErrors = getOddRoundingErrors();
        float[] evenRoundingErrors = getEvenRoundingErrors();
        for (int i3 = 0; i3 < dataCharacterCounters.length; i3++) {
            float f = ((float) dataCharacterCounters[i3]) / sum;
            int i4 = (int) (0.5f + f);
            if (i4 <= 0) {
                i4 = 1;
            } else if (i4 > 8) {
                i4 = 8;
            }
            int i5 = i3 / 2;
            if ((i3 & 1) == 0) {
                oddCounts[i5] = i4;
                oddRoundingErrors[i5] = f - ((float) i4);
            } else {
                evenCounts[i5] = i4;
                evenRoundingErrors[i5] = f - ((float) i4);
            }
        }
        adjustOddEvenCounts(z2, i);
        int i6 = 0;
        length = 0;
        for (i = oddCounts.length - 1; i >= 0; i--) {
            i6 = (i6 * 9) + oddCounts[i];
            length += oddCounts[i];
        }
        int i7 = 0;
        int i8 = 0;
        for (i = evenCounts.length - 1; i >= 0; i--) {
            i7 = (i7 * 9) + evenCounts[i];
            i8 += evenCounts[i];
        }
        i6 += i7 * 3;
        int i9;
        if (z2) {
            if ((length & 1) != 0 || length > 12 || length < 4) {
                throw NotFoundException.getNotFoundInstance();
            }
            i = (12 - length) / 2;
            i9 = OUTSIDE_ODD_WIDEST[i];
            int i10 = 9 - i9;
            return new DataCharacter(((RSSUtils.getRSSvalue(oddCounts, i9, false) * OUTSIDE_EVEN_TOTAL_SUBSET[i]) + RSSUtils.getRSSvalue(evenCounts, i10, true)) + OUTSIDE_GSUM[i], i6);
        } else if ((i8 & 1) != 0 || i8 > 10 || i8 < 4) {
            throw NotFoundException.getNotFoundInstance();
        } else {
            i = (10 - i8) / 2;
            i9 = INSIDE_ODD_WIDEST[i];
            return new DataCharacter(((RSSUtils.getRSSvalue(evenCounts, 9 - i9, false) * INSIDE_ODD_TOTAL_SUBSET[i]) + RSSUtils.getRSSvalue(oddCounts, i9, true)) + INSIDE_GSUM[i], i6);
        }
    }

    private int[] findFinderPattern(BitArray bitArray, int i, boolean z) throws NotFoundException {
        int[] decodeFinderCounters = getDecodeFinderCounters();
        decodeFinderCounters[0] = 0;
        decodeFinderCounters[1] = 0;
        decodeFinderCounters[2] = 0;
        decodeFinderCounters[3] = 0;
        int size = bitArray.getSize();
        int i2 = 0;
        while (i < size) {
            i2 = bitArray.get(i) ^ 1;
            if (z == i2) {
                break;
            }
            i++;
        }
        int i3 = i;
        int i4 = 0;
        while (i < size) {
            if ((bitArray.get(i) ^ i2) != 0) {
                decodeFinderCounters[i4] = decodeFinderCounters[i4] + 1;
            } else {
                if (i4 != 3) {
                    i4++;
                } else if (AbstractRSSReader.isFinderPattern(decodeFinderCounters)) {
                    return new int[]{i3, i};
                } else {
                    i3 += decodeFinderCounters[0] + decodeFinderCounters[1];
                    decodeFinderCounters[0] = decodeFinderCounters[2];
                    decodeFinderCounters[1] = decodeFinderCounters[3];
                    decodeFinderCounters[2] = 0;
                    decodeFinderCounters[3] = 0;
                    i4--;
                }
                decodeFinderCounters[i4] = 1;
                i2 = i2 == 0 ? 1 : 0;
            }
            i++;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private FinderPattern parseFoundFinderPattern(BitArray bitArray, int i, boolean z, int[] iArr) throws NotFoundException {
        int size;
        int size2;
        boolean z2 = bitArray.get(iArr[0]);
        int i2 = iArr[0] - 1;
        while (i2 >= 0 && (bitArray.get(i2) ^ z2) != 0) {
            i2--;
        }
        i2++;
        int i3 = iArr[0] - i2;
        int[] decodeFinderCounters = getDecodeFinderCounters();
        System.arraycopy(decodeFinderCounters, 0, decodeFinderCounters, 1, decodeFinderCounters.length - 1);
        decodeFinderCounters[0] = i3;
        int parseFinderValue = AbstractRSSReader.parseFinderValue(decodeFinderCounters, FINDER_PATTERNS);
        i3 = iArr[1];
        if (z) {
            size = (bitArray.getSize() - 1) - i3;
            size2 = (bitArray.getSize() - 1) - i2;
        } else {
            size = i3;
            size2 = i2;
        }
        return new FinderPattern(parseFinderValue, new int[]{i2, iArr[1]}, size2, size, i);
    }

    /* JADX WARNING: Removed duplicated region for block: B:67:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x00bd  */
    /* JADX WARNING: Removed duplicated region for block: B:85:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x00bd  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:85:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x006f  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x005b  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x00bd  */
    /* JADX WARNING: Removed duplicated region for block: B:85:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x005b  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x006f  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x00bd  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:85:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Missing block: B:28:0x0047, code skipped:
            if (r1 < 4) goto L_0x002d;
     */
    private void adjustOddEvenCounts(boolean r10, int r11) throws com.google.zxing.NotFoundException {
        /*
        r9 = this;
        r0 = r9.getOddCounts();
        r0 = com.google.zxing.common.detector.MathUtils.sum(r0);
        r1 = r9.getEvenCounts();
        r1 = com.google.zxing.common.detector.MathUtils.sum(r1);
        r2 = 4;
        r3 = 0;
        r4 = 1;
        if (r10 == 0) goto L_0x0034;
    L_0x0015:
        r5 = 12;
        if (r0 <= r5) goto L_0x001c;
    L_0x0019:
        r6 = 0;
        r7 = 1;
        goto L_0x0022;
    L_0x001c:
        if (r0 >= r2) goto L_0x0020;
    L_0x001e:
        r6 = 1;
        goto L_0x0021;
    L_0x0020:
        r6 = 0;
    L_0x0021:
        r7 = 0;
    L_0x0022:
        if (r1 <= r5) goto L_0x0029;
    L_0x0024:
        r5 = r6;
        r6 = r7;
    L_0x0026:
        r2 = 0;
        r7 = 1;
        goto L_0x004a;
    L_0x0029:
        if (r1 >= r2) goto L_0x002f;
    L_0x002b:
        r5 = r6;
        r6 = r7;
    L_0x002d:
        r2 = 1;
        goto L_0x0032;
    L_0x002f:
        r5 = r6;
        r6 = r7;
    L_0x0031:
        r2 = 0;
    L_0x0032:
        r7 = 0;
        goto L_0x004a;
    L_0x0034:
        r5 = 11;
        if (r0 <= r5) goto L_0x003b;
    L_0x0038:
        r5 = 0;
        r6 = 1;
        goto L_0x0042;
    L_0x003b:
        r5 = 5;
        if (r0 >= r5) goto L_0x0040;
    L_0x003e:
        r5 = 1;
        goto L_0x0041;
    L_0x0040:
        r5 = 0;
    L_0x0041:
        r6 = 0;
    L_0x0042:
        r7 = 10;
        if (r1 <= r7) goto L_0x0047;
    L_0x0046:
        goto L_0x0026;
    L_0x0047:
        if (r1 >= r2) goto L_0x0031;
    L_0x0049:
        goto L_0x002d;
    L_0x004a:
        r8 = r0 + r1;
        r8 = r8 - r11;
        r11 = r0 & 1;
        if (r11 != r10) goto L_0x0053;
    L_0x0051:
        r10 = 1;
        goto L_0x0054;
    L_0x0053:
        r10 = 0;
    L_0x0054:
        r11 = r1 & 1;
        if (r11 != r4) goto L_0x0059;
    L_0x0058:
        r3 = 1;
    L_0x0059:
        if (r8 != r4) goto L_0x006f;
    L_0x005b:
        if (r10 == 0) goto L_0x0066;
    L_0x005d:
        if (r3 != 0) goto L_0x0061;
    L_0x005f:
        r6 = 1;
        goto L_0x0099;
    L_0x0061:
        r10 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r10;
    L_0x0066:
        if (r3 == 0) goto L_0x006a;
    L_0x0068:
        r7 = 1;
        goto L_0x0099;
    L_0x006a:
        r10 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r10;
    L_0x006f:
        r11 = -1;
        if (r8 != r11) goto L_0x0086;
    L_0x0072:
        if (r10 == 0) goto L_0x007d;
    L_0x0074:
        if (r3 != 0) goto L_0x0078;
    L_0x0076:
        r5 = 1;
        goto L_0x0099;
    L_0x0078:
        r10 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r10;
    L_0x007d:
        if (r3 == 0) goto L_0x0081;
    L_0x007f:
        r2 = 1;
        goto L_0x0099;
    L_0x0081:
        r10 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r10;
    L_0x0086:
        if (r8 != 0) goto L_0x00e3;
    L_0x0088:
        if (r10 == 0) goto L_0x0097;
    L_0x008a:
        if (r3 == 0) goto L_0x0092;
    L_0x008c:
        if (r0 >= r1) goto L_0x0090;
    L_0x008e:
        r5 = 1;
        goto L_0x0068;
    L_0x0090:
        r2 = 1;
        goto L_0x005f;
    L_0x0092:
        r10 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r10;
    L_0x0097:
        if (r3 != 0) goto L_0x00de;
    L_0x0099:
        if (r5 == 0) goto L_0x00ae;
    L_0x009b:
        if (r6 != 0) goto L_0x00a9;
    L_0x009d:
        r10 = r9.getOddCounts();
        r11 = r9.getOddRoundingErrors();
        com.google.zxing.oned.rss.AbstractRSSReader.increment(r10, r11);
        goto L_0x00ae;
    L_0x00a9:
        r10 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r10;
    L_0x00ae:
        if (r6 == 0) goto L_0x00bb;
    L_0x00b0:
        r10 = r9.getOddCounts();
        r11 = r9.getOddRoundingErrors();
        com.google.zxing.oned.rss.AbstractRSSReader.decrement(r10, r11);
    L_0x00bb:
        if (r2 == 0) goto L_0x00d0;
    L_0x00bd:
        if (r7 != 0) goto L_0x00cb;
    L_0x00bf:
        r10 = r9.getEvenCounts();
        r11 = r9.getOddRoundingErrors();
        com.google.zxing.oned.rss.AbstractRSSReader.increment(r10, r11);
        goto L_0x00d0;
    L_0x00cb:
        r10 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r10;
    L_0x00d0:
        if (r7 == 0) goto L_0x00dd;
    L_0x00d2:
        r10 = r9.getEvenCounts();
        r11 = r9.getEvenRoundingErrors();
        com.google.zxing.oned.rss.AbstractRSSReader.decrement(r10, r11);
    L_0x00dd:
        return;
    L_0x00de:
        r10 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r10;
    L_0x00e3:
        r10 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r10;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.oned.rss.RSS14Reader.adjustOddEvenCounts(boolean, int):void");
    }
}
