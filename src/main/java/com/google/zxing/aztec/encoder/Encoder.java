package com.google.zxing.aztec.encoder;

import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonEncoder;

public final class Encoder {
    public static final int DEFAULT_AZTEC_LAYERS = 0;
    public static final int DEFAULT_EC_PERCENT = 33;
    private static final int MAX_NB_BITS = 32;
    private static final int MAX_NB_BITS_COMPACT = 4;
    private static final int[] WORD_SIZE = new int[]{4, 6, 6, 8, 8, 8, 8, 8, 8, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12};

    private static int totalBitsInLayer(int i, boolean z) {
        return ((z ? 88 : 112) + (i << 4)) * i;
    }

    private Encoder() {
    }

    public static AztecCode encode(byte[] bArr) {
        return encode(bArr, 33, 0);
    }

    public static AztecCode encode(byte[] bArr, int i, int i2) {
        boolean z;
        int abs;
        int totalBitsInLayer;
        int i3;
        int i4;
        int totalBitsInLayer2;
        int i5;
        int i6;
        int i7;
        BitArray encode = new HighLevelEncoder(bArr).encode();
        int i8 = 11;
        int size = ((encode.getSize() * i) / 100) + 11;
        int size2 = encode.getSize() + size;
        int i9 = 32;
        int i10 = 0;
        if (i2 != 0) {
            z = i2 < 0;
            abs = Math.abs(i2);
            if (z) {
                i9 = 4;
            }
            if (abs <= i9) {
                totalBitsInLayer = totalBitsInLayer(abs, z);
                i9 = WORD_SIZE[abs];
                i3 = totalBitsInLayer - (totalBitsInLayer % i9);
                encode = stuffBits(encode, i9);
                if (encode.getSize() + size > i3) {
                    throw new IllegalArgumentException("Data to large for user specified layer");
                } else if (z && encode.getSize() > (i9 << 6)) {
                    throw new IllegalArgumentException("Data to large for user specified layer");
                }
            }
            throw new IllegalArgumentException(String.format("Illegal value %s for layers", new Object[]{Integer.valueOf(i2)}));
        }
        BitArray bitArray = null;
        totalBitsInLayer = 0;
        abs = 0;
        while (totalBitsInLayer <= 32) {
            boolean z2 = totalBitsInLayer <= 3;
            i4 = z2 ? totalBitsInLayer + 1 : totalBitsInLayer;
            totalBitsInLayer2 = totalBitsInLayer(i4, z2);
            if (size2 <= totalBitsInLayer2) {
                if (abs != WORD_SIZE[i4]) {
                    abs = WORD_SIZE[i4];
                    bitArray = stuffBits(encode, abs);
                }
                i5 = totalBitsInLayer2 - (totalBitsInLayer2 % abs);
                if ((!z2 || bitArray.getSize() <= (abs << 6)) && bitArray.getSize() + size <= i5) {
                    i9 = abs;
                    encode = bitArray;
                    z = z2;
                    abs = i4;
                    totalBitsInLayer = totalBitsInLayer2;
                }
            }
            totalBitsInLayer++;
            i10 = 0;
        }
        throw new IllegalArgumentException("Data too large for an Aztec code");
        BitArray generateCheckWords = generateCheckWords(encode, totalBitsInLayer, i9);
        int size3 = encode.getSize() / i9;
        BitArray generateModeMessage = generateModeMessage(z, abs, size3);
        if (!z) {
            i8 = 14;
        }
        i8 += abs << 2;
        int[] iArr = new int[i8];
        i3 = 2;
        if (z) {
            for (i6 = 0; i6 < iArr.length; i6++) {
                iArr[i6] = i6;
            }
            i6 = i8;
        } else {
            i4 = i8 / 2;
            i6 = (i8 + 1) + (((i4 - 1) / 15) * 2);
            totalBitsInLayer2 = i6 / 2;
            for (i5 = 0; i5 < i4; i5++) {
                i7 = (i5 / 15) + i5;
                iArr[(i4 - i5) - 1] = (totalBitsInLayer2 - i7) - 1;
                iArr[i4 + i5] = (i7 + totalBitsInLayer2) + 1;
            }
        }
        BitMatrix bitMatrix = new BitMatrix(i6);
        totalBitsInLayer2 = 0;
        i5 = 0;
        while (totalBitsInLayer2 < abs) {
            i7 = ((abs - totalBitsInLayer2) << i3) + (z ? 9 : 12);
            int i11 = 0;
            while (i11 < i7) {
                int i12 = i11 << 1;
                for (i3 = 
/*
Method generation error in method: com.google.zxing.aztec.encoder.Encoder.encode(byte[], int, int):com.google.zxing.aztec.encoder.AztecCode, dex: classes.dex
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r9_10 'i3' int) = (r9_9 'i3' int), (r9_22 'i3' int) binds: {(r9_9 'i3' int)=B:60:0x0115, (r9_22 'i3' int)=B:77:0x018a} in method: com.google.zxing.aztec.encoder.Encoder.encode(byte[], int, int):com.google.zxing.aztec.encoder.AztecCode, dex: classes.dex
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:185)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:89)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:95)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:220)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:89)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:95)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:220)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:89)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:183)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:321)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:259)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:221)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 25 more

*/

    private static void drawBullsEye(BitMatrix bitMatrix, int i, int i2) {
        int i3;
        int i4;
        for (i3 = 0; i3 < i2; i3 += 2) {
            i4 = i - i3;
            int i5 = i4;
            while (true) {
                int i6 = i + i3;
                if (i5 > i6) {
                    break;
                }
                bitMatrix.set(i5, i4);
                bitMatrix.set(i5, i6);
                bitMatrix.set(i4, i5);
                bitMatrix.set(i6, i5);
                i5++;
            }
        }
        i3 = i - i2;
        bitMatrix.set(i3, i3);
        i4 = i3 + 1;
        bitMatrix.set(i4, i3);
        bitMatrix.set(i3, i4);
        i += i2;
        bitMatrix.set(i, i3);
        bitMatrix.set(i, i4);
        bitMatrix.set(i, i - 1);
    }

    static BitArray generateModeMessage(boolean z, int i, int i2) {
        BitArray bitArray = new BitArray();
        if (z) {
            bitArray.appendBits(i - 1, 2);
            bitArray.appendBits(i2 - 1, 6);
            return generateCheckWords(bitArray, 28, 4);
        }
        bitArray.appendBits(i - 1, 5);
        bitArray.appendBits(i2 - 1, 11);
        return generateCheckWords(bitArray, 40, 4);
    }

    private static void drawModeMessage(BitMatrix bitMatrix, boolean z, int i, BitArray bitArray) {
        i /= 2;
        int i2 = 0;
        int i3;
        if (z) {
            while (i2 < 7) {
                i3 = (i - 3) + i2;
                if (bitArray.get(i2)) {
                    bitMatrix.set(i3, i - 5);
                }
                if (bitArray.get(i2 + 7)) {
                    bitMatrix.set(i + 5, i3);
                }
                if (bitArray.get(20 - i2)) {
                    bitMatrix.set(i3, i + 5);
                }
                if (bitArray.get(27 - i2)) {
                    bitMatrix.set(i - 5, i3);
                }
                i2++;
            }
            return;
        }
        while (i2 < 10) {
            i3 = ((i - 5) + i2) + (i2 / 5);
            if (bitArray.get(i2)) {
                bitMatrix.set(i3, i - 7);
            }
            if (bitArray.get(i2 + 10)) {
                bitMatrix.set(i + 7, i3);
            }
            if (bitArray.get(29 - i2)) {
                bitMatrix.set(i3, i + 7);
            }
            if (bitArray.get(39 - i2)) {
                bitMatrix.set(i - 7, i3);
            }
            i2++;
        }
    }

    private static BitArray generateCheckWords(BitArray bitArray, int i, int i2) {
        int size = bitArray.getSize() / i2;
        ReedSolomonEncoder reedSolomonEncoder = new ReedSolomonEncoder(getGF(i2));
        int i3 = i / i2;
        int[] bitsToWords = bitsToWords(bitArray, i2, i3);
        reedSolomonEncoder.encode(bitsToWords, i3 - size);
        i %= i2;
        BitArray bitArray2 = new BitArray();
        int i4 = 0;
        bitArray2.appendBits(0, i);
        i = bitsToWords.length;
        while (i4 < i) {
            bitArray2.appendBits(bitsToWords[i4], i2);
            i4++;
        }
        return bitArray2;
    }

    private static int[] bitsToWords(BitArray bitArray, int i, int i2) {
        int[] iArr = new int[i2];
        int size = bitArray.getSize() / i;
        for (int i3 = 0; i3 < size; i3++) {
            int i4 = 0;
            for (int i5 = 0; i5 < i; i5++) {
                i4 |= bitArray.get((i3 * i) + i5) ? 1 << ((i - i5) - 1) : 0;
            }
            iArr[i3] = i4;
        }
        return iArr;
    }

    private static GenericGF getGF(int i) {
        if (i == 4) {
            return GenericGF.AZTEC_PARAM;
        }
        if (i == 6) {
            return GenericGF.AZTEC_DATA_6;
        }
        if (i == 8) {
            return GenericGF.AZTEC_DATA_8;
        }
        if (i == 10) {
            return GenericGF.AZTEC_DATA_10;
        }
        if (i == 12) {
            return GenericGF.AZTEC_DATA_12;
        }
        StringBuilder stringBuilder = new StringBuilder("Unsupported word size ");
        stringBuilder.append(i);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    static BitArray stuffBits(BitArray bitArray, int i) {
        BitArray bitArray2 = new BitArray();
        int size = bitArray.getSize();
        int i2 = (1 << i) - 2;
        int i3 = 0;
        while (i3 < size) {
            int i4;
            int i5 = 0;
            for (i4 = 0; i4 < i; i4++) {
                int i6 = i3 + i4;
                if (i6 >= size || bitArray.get(i6)) {
                    i5 |= 1 << ((i - 1) - i4);
                }
            }
            i4 = i5 & i2;
            if (i4 == i2) {
                bitArray2.appendBits(i4, i);
                i3--;
            } else if (i4 == 0) {
                bitArray2.appendBits(i5 | 1, i);
                i3--;
            } else {
                bitArray2.appendBits(i5, i);
            }
            i3 += i;
        }
        return bitArray2;
    }
}
