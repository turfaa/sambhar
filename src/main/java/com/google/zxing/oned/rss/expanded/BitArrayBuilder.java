package com.google.zxing.oned.rss.expanded;

import com.google.zxing.common.BitArray;
import java.util.List;

final class BitArrayBuilder {
    private BitArrayBuilder() {
    }

    static BitArray buildBitArray(List<ExpandedPair> list) {
        int size = (list.size() << 1) - 1;
        if (((ExpandedPair) list.get(list.size() - 1)).getRightChar() == null) {
            size--;
        }
        BitArray bitArray = new BitArray(size * 12);
        int value = ((ExpandedPair) list.get(0)).getRightChar().getValue();
        int i = 0;
        for (size = 11; size >= 0; size--) {
            if (((1 << size) & value) != 0) {
                bitArray.set(i);
            }
            i++;
        }
        for (size = 1; size < list.size(); size++) {
            ExpandedPair expandedPair = (ExpandedPair) list.get(size);
            int value2 = expandedPair.getLeftChar().getValue();
            int i2 = i;
            for (i = 11; i >= 0; i--) {
                if (((1 << i) & value2) != 0) {
                    bitArray.set(i2);
                }
                i2++;
            }
            if (expandedPair.getRightChar() != null) {
                value = expandedPair.getRightChar().getValue();
                for (i = 11; i >= 0; i--) {
                    if (((1 << i) & value) != 0) {
                        bitArray.set(i2);
                    }
                    i2++;
                }
            }
            i = i2;
        }
        return bitArray;
    }
}
