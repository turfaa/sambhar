package com.google.zxing.oned.rss.expanded;

import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import com.facebook.internal.FacebookRequestErrorClassification;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.detector.MathUtils;
import com.google.zxing.oned.OneDReader;
import com.google.zxing.oned.rss.AbstractRSSReader;
import com.google.zxing.oned.rss.DataCharacter;
import com.google.zxing.oned.rss.FinderPattern;
import com.google.zxing.oned.rss.RSSUtils;
import com.google.zxing.oned.rss.expanded.decoders.AbstractExpandedDecoder;
import com.twitter.sdk.android.core.internal.TwitterApiConstants.Errors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class RSSExpandedReader extends AbstractRSSReader {
    private static final int[] EVEN_TOTAL_SUBSET = new int[]{4, 20, 52, 104, 204};
    private static final int[][] FINDER_PATTERNS = new int[][]{new int[]{1, 8, 4, 1}, new int[]{3, 6, 4, 1}, new int[]{3, 4, 6, 1}, new int[]{3, 2, 8, 1}, new int[]{2, 6, 5, 1}, new int[]{2, 2, 9, 1}};
    private static final int[][] FINDER_PATTERN_SEQUENCES = new int[][]{new int[]{0, 0}, new int[]{0, 1, 1}, new int[]{0, 2, 1, 3}, new int[]{0, 4, 1, 3, 2}, new int[]{0, 4, 1, 3, 3, 5}, new int[]{0, 4, 1, 3, 4, 5, 5}, new int[]{0, 0, 1, 1, 2, 2, 3, 3}, new int[]{0, 0, 1, 1, 2, 2, 3, 4, 4}, new int[]{0, 0, 1, 1, 2, 2, 3, 4, 5, 5}, new int[]{0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5}};
    private static final int FINDER_PAT_A = 0;
    private static final int FINDER_PAT_B = 1;
    private static final int FINDER_PAT_C = 2;
    private static final int FINDER_PAT_D = 3;
    private static final int FINDER_PAT_E = 4;
    private static final int FINDER_PAT_F = 5;
    private static final int[] GSUM = new int[]{0, 348, 1388, 2948, 3988};
    private static final int MAX_PAIRS = 11;
    private static final int[] SYMBOL_WIDEST = new int[]{7, 5, 4, 3, 1};
    private static final int[][] WEIGHTS = new int[][]{new int[]{1, 3, 9, 27, 81, 32, 96, 77}, new int[]{20, 60, 180, 118, 143, 7, 21, 63}, new int[]{189, 145, 13, 39, 117, 140, 209, 205}, new int[]{193, 157, 49, 147, 19, 57, 171, 91}, new int[]{62, 186, 136, 197, 169, 85, 44, 132}, new int[]{185, 133, 188, 142, 4, 12, 36, 108}, new int[]{113, 128, 173, 97, 80, 29, 87, 50}, new int[]{150, 28, 84, 41, 123, 158, 52, 156}, new int[]{46, 138, 203, 187, Errors.ALREADY_FAVORITED, 206, 196, 166}, new int[]{76, 17, 51, 153, 37, 111, 122, 155}, new int[]{43, 129, 176, 106, 107, 110, 119, 146}, new int[]{16, 48, Errors.ALREADY_UNFAVORITED, 10, 30, 90, 59, 177}, new int[]{109, 116, 137, Callback.DEFAULT_DRAG_ANIMATION_DURATION, 178, 112, 125, 164}, new int[]{70, 210, 208, 202, 184, 130, 179, 115}, new int[]{134, 191, 151, 31, 93, 68, 204, FacebookRequestErrorClassification.EC_INVALID_TOKEN}, new int[]{148, 22, 66, 198, 172, 94, 71, 2}, new int[]{6, 18, 54, 162, 64, 192, 154, 40}, new int[]{120, 149, 25, 75, 14, 42, 126, 167}, new int[]{79, 26, 78, 23, 69, 207, 199, 175}, new int[]{103, 98, 83, 38, 114, 131, 182, 124}, new int[]{161, 61, 183, 127, 170, 88, 53, 159}, new int[]{55, 165, 73, 8, 24, 72, 5, 15}, new int[]{45, 135, 194, 160, 58, 174, 100, 89}};
    private final List<ExpandedPair> pairs = new ArrayList(11);
    private final List<ExpandedRow> rows = new ArrayList();
    private final int[] startEnd = new int[2];
    private boolean startFromEven;

    public Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws NotFoundException, FormatException {
        this.pairs.clear();
        this.startFromEven = false;
        try {
            return constructResult(decodeRow2pairs(i, bitArray));
        } catch (NotFoundException unused) {
            this.pairs.clear();
            this.startFromEven = true;
            return constructResult(decodeRow2pairs(i, bitArray));
        }
    }

    public void reset() {
        this.pairs.clear();
        this.rows.clear();
    }

    /* Access modifiers changed, original: 0000 */
    public List<ExpandedPair> decodeRow2pairs(int i, BitArray bitArray) throws NotFoundException {
        while (true) {
            try {
                this.pairs.add(retrieveNextPair(bitArray, this.pairs, i));
            } catch (NotFoundException e) {
                if (this.pairs.isEmpty()) {
                    throw e;
                } else if (checkChecksum()) {
                    return this.pairs;
                } else {
                    int isEmpty = this.rows.isEmpty() ^ 1;
                    storeRow(i, false);
                    if (isEmpty != 0) {
                        List checkRows = checkRows(false);
                        if (checkRows != null) {
                            return checkRows;
                        }
                        checkRows = checkRows(true);
                        if (checkRows != null) {
                            return checkRows;
                        }
                    }
                    throw NotFoundException.getNotFoundInstance();
                }
            }
        }
    }

    private List<ExpandedPair> checkRows(boolean z) {
        if (this.rows.size() > 25) {
            this.rows.clear();
            return null;
        }
        List<ExpandedPair> checkRows;
        this.pairs.clear();
        if (z) {
            Collections.reverse(this.rows);
        }
        try {
            checkRows = checkRows(new ArrayList(), 0);
        } catch (NotFoundException unused) {
            checkRows = null;
        }
        if (z) {
            Collections.reverse(this.rows);
        }
        return checkRows;
    }

    private List<ExpandedPair> checkRows(List<ExpandedRow> list, int i) throws NotFoundException {
        while (i < this.rows.size()) {
            ExpandedRow expandedRow = (ExpandedRow) this.rows.get(i);
            this.pairs.clear();
            for (ExpandedRow pairs : list) {
                this.pairs.addAll(pairs.getPairs());
            }
            this.pairs.addAll(expandedRow.getPairs());
            if (!isValidSequence(this.pairs)) {
                i++;
            } else if (checkChecksum()) {
                return this.pairs;
            } else {
                ArrayList arrayList = new ArrayList();
                arrayList.addAll(list);
                arrayList.add(expandedRow);
                try {
                    return checkRows(arrayList, i + 1);
                } catch (NotFoundException unused) {
                    continue;
                }
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static boolean isValidSequence(List<ExpandedPair> list) {
        for (int[] iArr : FINDER_PATTERN_SEQUENCES) {
            if (list.size() <= iArr.length) {
                Object obj;
                for (int i = 0; i < list.size(); i++) {
                    if (((ExpandedPair) list.get(i)).getFinderPattern().getValue() != iArr[i]) {
                        obj = null;
                        break;
                    }
                }
                obj = 1;
                if (obj != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:14:0x004d, code skipped:
            return;
     */
    private void storeRow(int r6, boolean r7) {
        /*
        r5 = this;
        r0 = 0;
        r1 = 0;
        r2 = 0;
    L_0x0003:
        r3 = r5.rows;
        r3 = r3.size();
        if (r1 >= r3) goto L_0x0029;
    L_0x000b:
        r3 = r5.rows;
        r3 = r3.get(r1);
        r3 = (com.google.zxing.oned.rss.expanded.ExpandedRow) r3;
        r4 = r3.getRowNumber();
        if (r4 <= r6) goto L_0x0020;
    L_0x0019:
        r0 = r5.pairs;
        r0 = r3.isEquivalent(r0);
        goto L_0x0029;
    L_0x0020:
        r2 = r5.pairs;
        r2 = r3.isEquivalent(r2);
        r1 = r1 + 1;
        goto L_0x0003;
    L_0x0029:
        if (r0 != 0) goto L_0x004d;
    L_0x002b:
        if (r2 == 0) goto L_0x002e;
    L_0x002d:
        goto L_0x004d;
    L_0x002e:
        r0 = r5.pairs;
        r2 = r5.rows;
        r0 = isPartialRow(r0, r2);
        if (r0 == 0) goto L_0x0039;
    L_0x0038:
        return;
    L_0x0039:
        r0 = r5.rows;
        r2 = new com.google.zxing.oned.rss.expanded.ExpandedRow;
        r3 = r5.pairs;
        r2.<init>(r3, r6, r7);
        r0.add(r1, r2);
        r6 = r5.pairs;
        r7 = r5.rows;
        removePartialRows(r6, r7);
        return;
    L_0x004d:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.oned.rss.expanded.RSSExpandedReader.storeRow(int, boolean):void");
    }

    private static void removePartialRows(List<ExpandedPair> list, List<ExpandedRow> list2) {
        Iterator it = list2.iterator();
        while (it.hasNext()) {
            ExpandedRow expandedRow = (ExpandedRow) it.next();
            if (expandedRow.getPairs().size() != list.size()) {
                Object obj;
                Iterator it2 = expandedRow.getPairs().iterator();
                Object obj2;
                do {
                    obj = null;
                    obj2 = 1;
                    if (!it2.hasNext()) {
                        obj = 1;
                        break;
                    }
                    ExpandedPair expandedPair = (ExpandedPair) it2.next();
                    for (ExpandedPair equals : list) {
                        if (expandedPair.equals(equals)) {
                            continue;
                            break;
                        }
                    }
                    obj2 = null;
                    continue;
                } while (obj2 != null);
                if (obj != null) {
                    it.remove();
                }
            }
        }
    }

    private static boolean isPartialRow(Iterable<ExpandedPair> iterable, Iterable<ExpandedRow> iterable2) {
        Iterator it = iterable2.iterator();
        boolean z;
        do {
            z = false;
            if (!it.hasNext()) {
                return false;
            }
            ExpandedRow expandedRow = (ExpandedRow) it.next();
            for (ExpandedPair expandedPair : iterable) {
                Object obj;
                for (ExpandedPair equals : expandedRow.getPairs()) {
                    if (expandedPair.equals(equals)) {
                        obj = 1;
                        continue;
                        break;
                    }
                }
                obj = null;
                continue;
                if (obj == null) {
                    continue;
                    break;
                }
            }
            z = true;
            continue;
        } while (!z);
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public List<ExpandedRow> getRows() {
        return this.rows;
    }

    static Result constructResult(List<ExpandedPair> list) throws NotFoundException, FormatException {
        String parseInformation = AbstractExpandedDecoder.createDecoder(BitArrayBuilder.buildBitArray(list)).parseInformation();
        ResultPoint[] resultPoints = ((ExpandedPair) list.get(0)).getFinderPattern().getResultPoints();
        ResultPoint[] resultPoints2 = ((ExpandedPair) list.get(list.size() - 1)).getFinderPattern().getResultPoints();
        return new Result(parseInformation, null, new ResultPoint[]{resultPoints[0], resultPoints[1], resultPoints2[0], resultPoints2[1]}, BarcodeFormat.RSS_EXPANDED);
    }

    private boolean checkChecksum() {
        ExpandedPair expandedPair = (ExpandedPair) this.pairs.get(0);
        DataCharacter leftChar = expandedPair.getLeftChar();
        DataCharacter rightChar = expandedPair.getRightChar();
        if (rightChar == null) {
            return false;
        }
        int checksumPortion = rightChar.getChecksumPortion();
        int i = 2;
        for (int i2 = 1; i2 < this.pairs.size(); i2++) {
            ExpandedPair expandedPair2 = (ExpandedPair) this.pairs.get(i2);
            checksumPortion += expandedPair2.getLeftChar().getChecksumPortion();
            i++;
            DataCharacter rightChar2 = expandedPair2.getRightChar();
            if (rightChar2 != null) {
                checksumPortion += rightChar2.getChecksumPortion();
                i++;
            }
        }
        if (((i - 4) * 211) + (checksumPortion % 211) == leftChar.getValue()) {
            return true;
        }
        return false;
    }

    private static int getNextSecondBar(BitArray bitArray, int i) {
        if (bitArray.get(i)) {
            return bitArray.getNextSet(bitArray.getNextUnset(i));
        }
        return bitArray.getNextUnset(bitArray.getNextSet(i));
    }

    /* Access modifiers changed, original: 0000 */
    public ExpandedPair retrieveNextPair(BitArray bitArray, List<ExpandedPair> list, int i) throws NotFoundException {
        FinderPattern parseFoundFinderPattern;
        boolean z = list.size() % 2 == 0;
        if (this.startFromEven) {
            z ^= 1;
        }
        int i2 = -1;
        Object obj = 1;
        do {
            findNextPair(bitArray, list, i2);
            parseFoundFinderPattern = parseFoundFinderPattern(bitArray, i, z);
            if (parseFoundFinderPattern == null) {
                i2 = getNextSecondBar(bitArray, this.startEnd[0]);
                continue;
            } else {
                obj = null;
                continue;
            }
        } while (obj != null);
        DataCharacter decodeDataCharacter = decodeDataCharacter(bitArray, parseFoundFinderPattern, z, true);
        if (list.isEmpty() || !((ExpandedPair) list.get(list.size() - 1)).mustBeLast()) {
            DataCharacter decodeDataCharacter2;
            try {
                decodeDataCharacter2 = decodeDataCharacter(bitArray, parseFoundFinderPattern, z, false);
            } catch (NotFoundException unused) {
                decodeDataCharacter2 = null;
            }
            return new ExpandedPair(decodeDataCharacter, decodeDataCharacter2, parseFoundFinderPattern, true);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private void findNextPair(BitArray bitArray, List<ExpandedPair> list, int i) throws NotFoundException {
        int[] decodeFinderCounters = getDecodeFinderCounters();
        decodeFinderCounters[0] = 0;
        decodeFinderCounters[1] = 0;
        decodeFinderCounters[2] = 0;
        decodeFinderCounters[3] = 0;
        int size = bitArray.getSize();
        if (i < 0) {
            if (list.isEmpty()) {
                i = 0;
            } else {
                i = ((ExpandedPair) list.get(list.size() - 1)).getFinderPattern().getStartEnd()[1];
            }
        }
        int i2 = list.size() % 2 != 0 ? 1 : 0;
        if (this.startFromEven) {
            i2 ^= 1;
        }
        int i3 = 0;
        while (i < size) {
            i3 = bitArray.get(i) ^ 1;
            if (i3 == 0) {
                break;
            }
            i++;
        }
        int i4 = i;
        int i5 = 0;
        while (i < size) {
            if ((bitArray.get(i) ^ i3) != 0) {
                decodeFinderCounters[i5] = decodeFinderCounters[i5] + 1;
            } else {
                if (i5 == 3) {
                    if (i2 != 0) {
                        reverseCounters(decodeFinderCounters);
                    }
                    if (AbstractRSSReader.isFinderPattern(decodeFinderCounters)) {
                        this.startEnd[0] = i4;
                        this.startEnd[1] = i;
                        return;
                    }
                    if (i2 != 0) {
                        reverseCounters(decodeFinderCounters);
                    }
                    i4 += decodeFinderCounters[0] + decodeFinderCounters[1];
                    decodeFinderCounters[0] = decodeFinderCounters[2];
                    decodeFinderCounters[1] = decodeFinderCounters[3];
                    decodeFinderCounters[2] = 0;
                    decodeFinderCounters[3] = 0;
                    i5--;
                } else {
                    i5++;
                }
                decodeFinderCounters[i5] = 1;
                i3 = i3 == 0 ? 1 : 0;
            }
            i++;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static void reverseCounters(int[] iArr) {
        int length = iArr.length;
        for (int i = 0; i < length / 2; i++) {
            int i2 = iArr[i];
            int i3 = (length - i) - 1;
            iArr[i] = iArr[i3];
            iArr[i3] = i2;
        }
    }

    private FinderPattern parseFoundFinderPattern(BitArray bitArray, int i, boolean z) {
        int i2;
        int i3;
        int i4;
        if (z) {
            i2 = this.startEnd[0] - 1;
            while (i2 >= 0 && !bitArray.get(i2)) {
                i2--;
            }
            i2++;
            i3 = this.startEnd[0] - i2;
            i4 = this.startEnd[1];
        } else {
            i2 = this.startEnd[0];
            i4 = bitArray.getNextUnset(this.startEnd[1] + 1);
            i3 = i4 - this.startEnd[1];
        }
        int i5 = i2;
        int i6 = i4;
        int[] decodeFinderCounters = getDecodeFinderCounters();
        System.arraycopy(decodeFinderCounters, 0, decodeFinderCounters, 1, decodeFinderCounters.length - 1);
        decodeFinderCounters[0] = i3;
        try {
            return new FinderPattern(AbstractRSSReader.parseFinderValue(decodeFinderCounters, FINDER_PATTERNS), new int[]{i5, i6}, i5, i6, i);
        } catch (NotFoundException unused) {
            return null;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public DataCharacter decodeDataCharacter(BitArray bitArray, FinderPattern finderPattern, boolean z, boolean z2) throws NotFoundException {
        int i;
        BitArray bitArray2 = bitArray;
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
            OneDReader.recordPattern(bitArray2, finderPattern.getStartEnd()[1], dataCharacterCounters);
            i = 0;
            for (int length = dataCharacterCounters.length - 1; i < length; length--) {
                int i2 = dataCharacterCounters[i];
                dataCharacterCounters[i] = dataCharacterCounters[length];
                dataCharacterCounters[length] = i2;
                i++;
            }
        }
        float sum = ((float) MathUtils.sum(dataCharacterCounters)) / 17.0f;
        float f = ((float) (finderPattern.getStartEnd()[1] - finderPattern.getStartEnd()[0])) / 15.0f;
        if (Math.abs(sum - f) / f <= 0.3f) {
            int i3;
            int i4;
            int length2;
            int[] oddCounts = getOddCounts();
            int[] evenCounts = getEvenCounts();
            float[] oddRoundingErrors = getOddRoundingErrors();
            float[] evenRoundingErrors = getEvenRoundingErrors();
            for (i3 = 0; i3 < dataCharacterCounters.length; i3++) {
                float f2 = (((float) dataCharacterCounters[i3]) * 1.0f) / sum;
                i4 = (int) (0.5f + f2);
                if (i4 <= 0) {
                    if (f2 >= 0.3f) {
                        i4 = 1;
                    } else {
                        throw NotFoundException.getNotFoundInstance();
                    }
                } else if (i4 > 8) {
                    if (f2 <= 8.7f) {
                        i4 = 8;
                    } else {
                        throw NotFoundException.getNotFoundInstance();
                    }
                }
                int i5 = i3 / 2;
                if ((i3 & 1) == 0) {
                    oddCounts[i5] = i4;
                    oddRoundingErrors[i5] = f2 - ((float) i4);
                } else {
                    evenCounts[i5] = i4;
                    evenRoundingErrors[i5] = f2 - ((float) i4);
                }
            }
            adjustOddEvenCounts(17);
            i = (((finderPattern.getValue() * 4) + (z ? 0 : 2)) + (z2 ^ 1)) - 1;
            int i6 = 0;
            i3 = 0;
            for (length2 = oddCounts.length - 1; length2 >= 0; length2--) {
                if (isNotA1left(finderPattern, z, z2)) {
                    i6 += oddCounts[length2] * WEIGHTS[i][length2 * 2];
                }
                i3 += oddCounts[length2];
            }
            i4 = 0;
            for (length2 = evenCounts.length - 1; length2 >= 0; length2--) {
                if (isNotA1left(finderPattern, z, z2)) {
                    i4 += evenCounts[length2] * WEIGHTS[i][(length2 * 2) + 1];
                }
            }
            i6 += i4;
            if ((i3 & 1) != 0 || i3 > 13 || i3 < 4) {
                throw NotFoundException.getNotFoundInstance();
            }
            i = (13 - i3) / 2;
            int i7 = SYMBOL_WIDEST[i];
            int i8 = 9 - i7;
            return new DataCharacter(((RSSUtils.getRSSvalue(oddCounts, i7, true) * EVEN_TOTAL_SUBSET[i]) + RSSUtils.getRSSvalue(evenCounts, i8, false)) + GSUM[i], i6);
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static boolean isNotA1left(FinderPattern finderPattern, boolean z, boolean z2) {
        return (finderPattern.getValue() == 0 && z && z2) ? false : true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0026  */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0023  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0035  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x003d  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00b4  */
    private void adjustOddEvenCounts(int r11) throws com.google.zxing.NotFoundException {
        /*
        r10 = this;
        r0 = r10.getOddCounts();
        r0 = com.google.zxing.common.detector.MathUtils.sum(r0);
        r1 = r10.getEvenCounts();
        r1 = com.google.zxing.common.detector.MathUtils.sum(r1);
        r2 = 4;
        r3 = 13;
        r4 = 0;
        r5 = 1;
        if (r0 <= r3) goto L_0x001a;
    L_0x0017:
        r6 = 1;
    L_0x0018:
        r7 = 0;
        goto L_0x0021;
    L_0x001a:
        if (r0 >= r2) goto L_0x001f;
    L_0x001c:
        r6 = 0;
        r7 = 1;
        goto L_0x0021;
    L_0x001f:
        r6 = 0;
        goto L_0x0018;
    L_0x0021:
        if (r1 <= r3) goto L_0x0026;
    L_0x0023:
        r2 = 0;
        r3 = 1;
        goto L_0x002c;
    L_0x0026:
        if (r1 >= r2) goto L_0x002a;
    L_0x0028:
        r2 = 1;
        goto L_0x002b;
    L_0x002a:
        r2 = 0;
    L_0x002b:
        r3 = 0;
    L_0x002c:
        r8 = r0 + r1;
        r8 = r8 - r11;
        r11 = r0 & 1;
        if (r11 != r5) goto L_0x0035;
    L_0x0033:
        r11 = 1;
        goto L_0x0036;
    L_0x0035:
        r11 = 0;
    L_0x0036:
        r9 = r1 & 1;
        if (r9 != 0) goto L_0x003b;
    L_0x003a:
        r4 = 1;
    L_0x003b:
        if (r8 != r5) goto L_0x0051;
    L_0x003d:
        if (r11 == 0) goto L_0x0048;
    L_0x003f:
        if (r4 != 0) goto L_0x0043;
    L_0x0041:
        r6 = 1;
        goto L_0x007b;
    L_0x0043:
        r11 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r11;
    L_0x0048:
        if (r4 == 0) goto L_0x004c;
    L_0x004a:
        r3 = 1;
        goto L_0x007b;
    L_0x004c:
        r11 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r11;
    L_0x0051:
        r9 = -1;
        if (r8 != r9) goto L_0x0068;
    L_0x0054:
        if (r11 == 0) goto L_0x005f;
    L_0x0056:
        if (r4 != 0) goto L_0x005a;
    L_0x0058:
        r7 = 1;
        goto L_0x007b;
    L_0x005a:
        r11 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r11;
    L_0x005f:
        if (r4 == 0) goto L_0x0063;
    L_0x0061:
        r2 = 1;
        goto L_0x007b;
    L_0x0063:
        r11 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r11;
    L_0x0068:
        if (r8 != 0) goto L_0x00c5;
    L_0x006a:
        if (r11 == 0) goto L_0x0079;
    L_0x006c:
        if (r4 == 0) goto L_0x0074;
    L_0x006e:
        if (r0 >= r1) goto L_0x0072;
    L_0x0070:
        r3 = 1;
        goto L_0x0058;
    L_0x0072:
        r2 = 1;
        goto L_0x0041;
    L_0x0074:
        r11 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r11;
    L_0x0079:
        if (r4 != 0) goto L_0x00c0;
    L_0x007b:
        if (r7 == 0) goto L_0x0090;
    L_0x007d:
        if (r6 != 0) goto L_0x008b;
    L_0x007f:
        r11 = r10.getOddCounts();
        r0 = r10.getOddRoundingErrors();
        com.google.zxing.oned.rss.AbstractRSSReader.increment(r11, r0);
        goto L_0x0090;
    L_0x008b:
        r11 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r11;
    L_0x0090:
        if (r6 == 0) goto L_0x009d;
    L_0x0092:
        r11 = r10.getOddCounts();
        r0 = r10.getOddRoundingErrors();
        com.google.zxing.oned.rss.AbstractRSSReader.decrement(r11, r0);
    L_0x009d:
        if (r2 == 0) goto L_0x00b2;
    L_0x009f:
        if (r3 != 0) goto L_0x00ad;
    L_0x00a1:
        r11 = r10.getEvenCounts();
        r0 = r10.getOddRoundingErrors();
        com.google.zxing.oned.rss.AbstractRSSReader.increment(r11, r0);
        goto L_0x00b2;
    L_0x00ad:
        r11 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r11;
    L_0x00b2:
        if (r3 == 0) goto L_0x00bf;
    L_0x00b4:
        r11 = r10.getEvenCounts();
        r0 = r10.getEvenRoundingErrors();
        com.google.zxing.oned.rss.AbstractRSSReader.decrement(r11, r0);
    L_0x00bf:
        return;
    L_0x00c0:
        r11 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r11;
    L_0x00c5:
        r11 = com.google.zxing.NotFoundException.getNotFoundInstance();
        throw r11;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.oned.rss.expanded.RSSExpandedReader.adjustOddEvenCounts(int):void");
    }
}
