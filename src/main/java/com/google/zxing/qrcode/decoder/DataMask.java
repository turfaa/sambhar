package com.google.zxing.qrcode.decoder;

import com.google.zxing.common.BitMatrix;

enum DataMask {
    DATA_MASK_000 {
        /* Access modifiers changed, original: 0000 */
        public boolean isMasked(int i, int i2) {
            return ((i + i2) & 1) == 0;
        }
    },
    DATA_MASK_001 {
        /* Access modifiers changed, original: 0000 */
        public boolean isMasked(int i, int i2) {
            return (i & 1) == 0;
        }
    },
    DATA_MASK_010 {
        /* Access modifiers changed, original: 0000 */
        public boolean isMasked(int i, int i2) {
            return i2 % 3 == 0;
        }
    },
    DATA_MASK_011 {
        /* Access modifiers changed, original: 0000 */
        public boolean isMasked(int i, int i2) {
            return (i + i2) % 3 == 0;
        }
    },
    DATA_MASK_100 {
        /* Access modifiers changed, original: 0000 */
        public boolean isMasked(int i, int i2) {
            return (((i / 2) + (i2 / 3)) & 1) == 0;
        }
    },
    DATA_MASK_101 {
        /* Access modifiers changed, original: 0000 */
        public boolean isMasked(int i, int i2) {
            return (i * i2) % 6 == 0;
        }
    },
    DATA_MASK_110 {
        /* Access modifiers changed, original: 0000 */
        public boolean isMasked(int i, int i2) {
            return (i * i2) % 6 < 3;
        }
    },
    DATA_MASK_111 {
        /* Access modifiers changed, original: 0000 */
        public boolean isMasked(int i, int i2) {
            return (((i + i2) + ((i * i2) % 3)) & 1) == 0;
        }
    };

    public abstract boolean isMasked(int i, int i2);

    /* Access modifiers changed, original: final */
    public final void unmaskBitMatrix(BitMatrix bitMatrix, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            for (int i3 = 0; i3 < i; i3++) {
                if (isMasked(i2, i3)) {
                    bitMatrix.flip(i3, i2);
                }
            }
        }
    }
}
