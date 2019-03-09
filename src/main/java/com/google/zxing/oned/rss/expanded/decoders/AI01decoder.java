package com.google.zxing.oned.rss.expanded.decoders;

import com.google.zxing.common.BitArray;

abstract class AI01decoder extends AbstractExpandedDecoder {
    static final int GTIN_SIZE = 40;

    AI01decoder(BitArray bitArray) {
        super(bitArray);
    }

    /* Access modifiers changed, original: final */
    public final void encodeCompressedGtin(StringBuilder stringBuilder, int i) {
        stringBuilder.append("(01)");
        int length = stringBuilder.length();
        stringBuilder.append('9');
        encodeCompressedGtinWithoutAI(stringBuilder, i, length);
    }

    /* Access modifiers changed, original: final */
    public final void encodeCompressedGtinWithoutAI(StringBuilder stringBuilder, int i, int i2) {
        for (int i3 = 0; i3 < 4; i3++) {
            int extractNumericValueFromBitArray = getGeneralDecoder().extractNumericValueFromBitArray((i3 * 10) + i, 10);
            if (extractNumericValueFromBitArray / 100 == 0) {
                stringBuilder.append('0');
            }
            if (extractNumericValueFromBitArray / 10 == 0) {
                stringBuilder.append('0');
            }
            stringBuilder.append(extractNumericValueFromBitArray);
        }
        appendCheckDigit(stringBuilder, i2);
    }

    private static void appendCheckDigit(StringBuilder stringBuilder, int i) {
        int i2;
        int i3 = 0;
        int i4 = 0;
        for (i2 = 0; i2 < 13; i2++) {
            int charAt = stringBuilder.charAt(i2 + i) - 48;
            if ((i2 & 1) == 0) {
                charAt *= 3;
            }
            i4 += charAt;
        }
        i2 = 10 - (i4 % 10);
        if (i2 != 10) {
            i3 = i2;
        }
        stringBuilder.append(i3);
    }
}
