package com.google.zxing.pdf417.decoder;

import com.google.zxing.FormatException;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.pdf417.PDF417ResultMetadata;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

final class DecodedBitStreamParser {
    private static final int AL = 28;
    private static final int AS = 27;
    private static final int BEGIN_MACRO_PDF417_CONTROL_BLOCK = 928;
    private static final int BEGIN_MACRO_PDF417_OPTIONAL_FIELD = 923;
    private static final int BYTE_COMPACTION_MODE_LATCH = 901;
    private static final int BYTE_COMPACTION_MODE_LATCH_6 = 924;
    private static final Charset DEFAULT_ENCODING = Charset.forName("ISO-8859-1");
    private static final int ECI_CHARSET = 927;
    private static final int ECI_GENERAL_PURPOSE = 926;
    private static final int ECI_USER_DEFINED = 925;
    private static final BigInteger[] EXP900;
    private static final int LL = 27;
    private static final int MACRO_PDF417_TERMINATOR = 922;
    private static final int MAX_NUMERIC_CODEWORDS = 15;
    private static final char[] MIXED_CHARS = "0123456789&\r\t,:#-.$/+%*=^".toCharArray();
    private static final int ML = 28;
    private static final int MODE_SHIFT_TO_BYTE_COMPACTION_MODE = 913;
    private static final int NUMBER_OF_SEQUENCE_CODEWORDS = 2;
    private static final int NUMERIC_COMPACTION_MODE_LATCH = 902;
    private static final int PAL = 29;
    private static final int PL = 25;
    private static final int PS = 29;
    private static final char[] PUNCT_CHARS = ";<>@[\\]_`~!\r\t,:\n-.$/\"|*()?{}'".toCharArray();
    private static final int TEXT_COMPACTION_MODE_LATCH = 900;

    private enum Mode {
        ALPHA,
        LOWER,
        MIXED,
        PUNCT,
        ALPHA_SHIFT,
        PUNCT_SHIFT
    }

    static {
        BigInteger[] bigIntegerArr = new BigInteger[16];
        EXP900 = bigIntegerArr;
        bigIntegerArr[0] = BigInteger.ONE;
        BigInteger valueOf = BigInteger.valueOf(900);
        EXP900[1] = valueOf;
        for (int i = 2; i < EXP900.length; i++) {
            EXP900[i] = EXP900[i - 1].multiply(valueOf);
        }
    }

    private DecodedBitStreamParser() {
    }

    static DecoderResult decode(int[] iArr, String str) throws FormatException {
        StringBuilder stringBuilder = new StringBuilder(iArr.length << 1);
        Charset charset = DEFAULT_ENCODING;
        int i = iArr[1];
        PDF417ResultMetadata pDF417ResultMetadata = new PDF417ResultMetadata();
        int i2 = 2;
        while (i2 < iArr[0]) {
            if (i != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                switch (i) {
                    case TEXT_COMPACTION_MODE_LATCH /*900*/:
                        i = textCompaction(iArr, i2, stringBuilder);
                        break;
                    case BYTE_COMPACTION_MODE_LATCH /*901*/:
                        i = byteCompaction(i, iArr, charset, i2, stringBuilder);
                        break;
                    case NUMERIC_COMPACTION_MODE_LATCH /*902*/:
                        i = numericCompaction(iArr, i2, stringBuilder);
                        break;
                    default:
                        switch (i) {
                            case MACRO_PDF417_TERMINATOR /*922*/:
                            case BEGIN_MACRO_PDF417_OPTIONAL_FIELD /*923*/:
                                throw FormatException.getFormatInstance();
                            case BYTE_COMPACTION_MODE_LATCH_6 /*924*/:
                                break;
                            case ECI_USER_DEFINED /*925*/:
                                i = i2 + 1;
                                break;
                            case ECI_GENERAL_PURPOSE /*926*/:
                                i = i2 + 2;
                                break;
                            case ECI_CHARSET /*927*/:
                                i = i2 + 1;
                                charset = Charset.forName(CharacterSetECI.getCharacterSetECIByValue(iArr[i2]).name());
                                break;
                            case 928:
                                i = decodeMacroBlock(iArr, i2, pDF417ResultMetadata);
                                break;
                            default:
                                i = textCompaction(iArr, i2 - 1, stringBuilder);
                                break;
                        }
                        i = byteCompaction(i, iArr, charset, i2, stringBuilder);
                        break;
                }
            }
            i = i2 + 1;
            stringBuilder.append((char) iArr[i2]);
            if (i < iArr.length) {
                i2 = i + 1;
                i = iArr[i];
            } else {
                throw FormatException.getFormatInstance();
            }
        }
        if (stringBuilder.length() != 0) {
            DecoderResult decoderResult = new DecoderResult(null, stringBuilder.toString(), null, str);
            decoderResult.setOther(pDF417ResultMetadata);
            return decoderResult;
        }
        throw FormatException.getFormatInstance();
    }

    private static int decodeMacroBlock(int[] iArr, int i, PDF417ResultMetadata pDF417ResultMetadata) throws FormatException {
        if (i + 2 <= iArr[0]) {
            int[] iArr2 = new int[2];
            int i2 = i;
            i = 0;
            while (i < 2) {
                iArr2[i] = iArr[i2];
                i++;
                i2++;
            }
            pDF417ResultMetadata.setSegmentIndex(Integer.parseInt(decodeBase900toBase10(iArr2, 2)));
            StringBuilder stringBuilder = new StringBuilder();
            int textCompaction = textCompaction(iArr, i2, stringBuilder);
            pDF417ResultMetadata.setFileId(stringBuilder.toString());
            if (iArr[textCompaction] == BEGIN_MACRO_PDF417_OPTIONAL_FIELD) {
                textCompaction++;
                int[] iArr3 = new int[(iArr[0] - textCompaction)];
                Object obj = null;
                int i3 = 0;
                while (textCompaction < iArr[0] && obj == null) {
                    int i4 = textCompaction + 1;
                    textCompaction = iArr[textCompaction];
                    if (textCompaction < TEXT_COMPACTION_MODE_LATCH) {
                        int i5 = i3 + 1;
                        iArr3[i3] = textCompaction;
                        textCompaction = i4;
                        i3 = i5;
                    } else if (textCompaction == MACRO_PDF417_TERMINATOR) {
                        pDF417ResultMetadata.setLastSegment(true);
                        textCompaction = i4 + 1;
                        obj = 1;
                    } else {
                        throw FormatException.getFormatInstance();
                    }
                }
                pDF417ResultMetadata.setOptionalData(Arrays.copyOf(iArr3, i3));
                return textCompaction;
            } else if (iArr[textCompaction] != MACRO_PDF417_TERMINATOR) {
                return textCompaction;
            } else {
                pDF417ResultMetadata.setLastSegment(true);
                return textCompaction + 1;
            }
        }
        throw FormatException.getFormatInstance();
    }

    private static int textCompaction(int[] iArr, int i, StringBuilder stringBuilder) {
        int[] iArr2 = new int[((iArr[0] - i) << 1)];
        int[] iArr3 = new int[((iArr[0] - i) << 1)];
        Object obj = null;
        int i2 = 0;
        while (i < iArr[0] && obj == null) {
            int i3 = i + 1;
            i = iArr[i];
            if (i < TEXT_COMPACTION_MODE_LATCH) {
                iArr2[i2] = i / 30;
                iArr2[i2 + 1] = i % 30;
                i2 += 2;
            } else if (i != MODE_SHIFT_TO_BYTE_COMPACTION_MODE) {
                if (i != 928) {
                    switch (i) {
                        case TEXT_COMPACTION_MODE_LATCH /*900*/:
                            i = i2 + 1;
                            iArr2[i2] = TEXT_COMPACTION_MODE_LATCH;
                            i2 = i;
                            break;
                        case BYTE_COMPACTION_MODE_LATCH /*901*/:
                        case NUMERIC_COMPACTION_MODE_LATCH /*902*/:
                            break;
                        default:
                            switch (i) {
                                case MACRO_PDF417_TERMINATOR /*922*/:
                                case BEGIN_MACRO_PDF417_OPTIONAL_FIELD /*923*/:
                                case BYTE_COMPACTION_MODE_LATCH_6 /*924*/:
                                    break;
                            }
                            break;
                    }
                }
                i = i3 - 1;
                obj = 1;
            } else {
                iArr2[i2] = MODE_SHIFT_TO_BYTE_COMPACTION_MODE;
                i = i3 + 1;
                iArr3[i2] = iArr[i3];
                i2++;
            }
            i = i3;
        }
        decodeTextCompaction(iArr2, iArr3, i2, stringBuilder);
        return i;
    }

    /* JADX WARNING: Missing block: B:14:0x0049, code skipped:
            r4 = r5;
     */
    /* JADX WARNING: Missing block: B:19:0x0056, code skipped:
            r4 = r5;
     */
    /* JADX WARNING: Missing block: B:33:0x0083, code skipped:
            r4 = r3;
     */
    /* JADX WARNING: Missing block: B:50:0x00b4, code skipped:
            r5 = r4;
     */
    /* JADX WARNING: Missing block: B:62:0x00d6, code skipped:
            r3 = ' ';
     */
    /* JADX WARNING: Missing block: B:73:0x00f6, code skipped:
            r3 = 0;
     */
    /* JADX WARNING: Missing block: B:74:0x00f7, code skipped:
            if (r3 == 0) goto L_0x00fc;
     */
    /* JADX WARNING: Missing block: B:75:0x00f9, code skipped:
            r0.append(r3);
     */
    /* JADX WARNING: Missing block: B:76:0x00fc, code skipped:
            r2 = r2 + 1;
     */
    private static void decodeTextCompaction(int[] r14, int[] r15, int r16, java.lang.StringBuilder r17) {
        /*
        r0 = r17;
        r1 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.ALPHA;
        r2 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.ALPHA;
        r4 = r1;
        r5 = r2;
        r2 = 0;
        r1 = r16;
    L_0x000b:
        if (r2 >= r1) goto L_0x0100;
    L_0x000d:
        r6 = r14[r2];
        r7 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.AnonymousClass1.$SwitchMap$com$google$zxing$pdf417$decoder$DecodedBitStreamParser$Mode;
        r8 = r4.ordinal();
        r7 = r7[r8];
        r8 = 28;
        r9 = 27;
        r10 = 32;
        r11 = 913; // 0x391 float:1.28E-42 double:4.51E-321;
        r12 = 900; // 0x384 float:1.261E-42 double:4.447E-321;
        r13 = 29;
        r3 = 26;
        switch(r7) {
            case 1: goto L_0x00ce;
            case 2: goto L_0x00a7;
            case 3: goto L_0x0075;
            case 4: goto L_0x0059;
            case 5: goto L_0x0044;
            case 6: goto L_0x002a;
            default: goto L_0x0028;
        };
    L_0x0028:
        goto L_0x00f6;
    L_0x002a:
        if (r6 >= r13) goto L_0x0031;
    L_0x002c:
        r3 = PUNCT_CHARS;
        r3 = r3[r6];
        goto L_0x0049;
    L_0x0031:
        if (r6 != r13) goto L_0x0036;
    L_0x0033:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.ALPHA;
        goto L_0x0083;
    L_0x0036:
        if (r6 != r11) goto L_0x003f;
    L_0x0038:
        r3 = r15[r2];
        r3 = (char) r3;
        r0.append(r3);
        goto L_0x0056;
    L_0x003f:
        if (r6 != r12) goto L_0x0056;
    L_0x0041:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.ALPHA;
        goto L_0x0083;
    L_0x0044:
        if (r6 >= r3) goto L_0x004c;
    L_0x0046:
        r6 = r6 + 65;
        r3 = (char) r6;
    L_0x0049:
        r4 = r5;
        goto L_0x00f7;
    L_0x004c:
        if (r6 != r3) goto L_0x0051;
    L_0x004e:
        r4 = r5;
        goto L_0x00d6;
    L_0x0051:
        if (r6 != r12) goto L_0x0056;
    L_0x0053:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.ALPHA;
        goto L_0x0083;
    L_0x0056:
        r4 = r5;
        goto L_0x00f6;
    L_0x0059:
        if (r6 >= r13) goto L_0x0061;
    L_0x005b:
        r3 = PUNCT_CHARS;
        r3 = r3[r6];
        goto L_0x00f7;
    L_0x0061:
        if (r6 != r13) goto L_0x0066;
    L_0x0063:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.ALPHA;
        goto L_0x0083;
    L_0x0066:
        if (r6 != r11) goto L_0x0070;
    L_0x0068:
        r3 = r15[r2];
        r3 = (char) r3;
        r0.append(r3);
        goto L_0x00f6;
    L_0x0070:
        if (r6 != r12) goto L_0x00f6;
    L_0x0072:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.ALPHA;
        goto L_0x0083;
    L_0x0075:
        r7 = 25;
        if (r6 >= r7) goto L_0x007f;
    L_0x0079:
        r3 = MIXED_CHARS;
        r3 = r3[r6];
        goto L_0x00f7;
    L_0x007f:
        if (r6 != r7) goto L_0x0086;
    L_0x0081:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.PUNCT;
    L_0x0083:
        r4 = r3;
        goto L_0x00f6;
    L_0x0086:
        if (r6 != r3) goto L_0x0089;
    L_0x0088:
        goto L_0x00d6;
    L_0x0089:
        if (r6 != r9) goto L_0x008e;
    L_0x008b:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.LOWER;
        goto L_0x0083;
    L_0x008e:
        if (r6 != r8) goto L_0x0093;
    L_0x0090:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.ALPHA;
        goto L_0x0083;
    L_0x0093:
        if (r6 != r13) goto L_0x0098;
    L_0x0095:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.PUNCT_SHIFT;
        goto L_0x00b4;
    L_0x0098:
        if (r6 != r11) goto L_0x00a2;
    L_0x009a:
        r3 = r15[r2];
        r3 = (char) r3;
        r0.append(r3);
        goto L_0x00f6;
    L_0x00a2:
        if (r6 != r12) goto L_0x00f6;
    L_0x00a4:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.ALPHA;
        goto L_0x0083;
    L_0x00a7:
        if (r6 >= r3) goto L_0x00ad;
    L_0x00a9:
        r6 = r6 + 97;
        r3 = (char) r6;
        goto L_0x00f7;
    L_0x00ad:
        if (r6 != r3) goto L_0x00b0;
    L_0x00af:
        goto L_0x00d6;
    L_0x00b0:
        if (r6 != r9) goto L_0x00b6;
    L_0x00b2:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.ALPHA_SHIFT;
    L_0x00b4:
        r5 = r4;
        goto L_0x0083;
    L_0x00b6:
        if (r6 != r8) goto L_0x00bb;
    L_0x00b8:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.MIXED;
        goto L_0x0083;
    L_0x00bb:
        if (r6 != r13) goto L_0x00c0;
    L_0x00bd:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.PUNCT_SHIFT;
        goto L_0x00b4;
    L_0x00c0:
        if (r6 != r11) goto L_0x00c9;
    L_0x00c2:
        r3 = r15[r2];
        r3 = (char) r3;
        r0.append(r3);
        goto L_0x00f6;
    L_0x00c9:
        if (r6 != r12) goto L_0x00f6;
    L_0x00cb:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.ALPHA;
        goto L_0x0083;
    L_0x00ce:
        if (r6 >= r3) goto L_0x00d4;
    L_0x00d0:
        r6 = r6 + 65;
        r3 = (char) r6;
        goto L_0x00f7;
    L_0x00d4:
        if (r6 != r3) goto L_0x00d9;
    L_0x00d6:
        r3 = 32;
        goto L_0x00f7;
    L_0x00d9:
        if (r6 != r9) goto L_0x00de;
    L_0x00db:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.LOWER;
        goto L_0x0083;
    L_0x00de:
        if (r6 != r8) goto L_0x00e3;
    L_0x00e0:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.MIXED;
        goto L_0x0083;
    L_0x00e3:
        if (r6 != r13) goto L_0x00e8;
    L_0x00e5:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.PUNCT_SHIFT;
        goto L_0x00b4;
    L_0x00e8:
        if (r6 != r11) goto L_0x00f1;
    L_0x00ea:
        r3 = r15[r2];
        r3 = (char) r3;
        r0.append(r3);
        goto L_0x00f6;
    L_0x00f1:
        if (r6 != r12) goto L_0x00f6;
    L_0x00f3:
        r3 = com.google.zxing.pdf417.decoder.DecodedBitStreamParser.Mode.ALPHA;
        goto L_0x0083;
    L_0x00f6:
        r3 = 0;
    L_0x00f7:
        if (r3 == 0) goto L_0x00fc;
    L_0x00f9:
        r0.append(r3);
    L_0x00fc:
        r2 = r2 + 1;
        goto L_0x000b;
    L_0x0100:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.pdf417.decoder.DecodedBitStreamParser.decodeTextCompaction(int[], int[], int, java.lang.StringBuilder):void");
    }

    private static int byteCompaction(int i, int[] iArr, Charset charset, int i2, StringBuilder stringBuilder) {
        int i3;
        int i4 = i;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i5 = MACRO_PDF417_TERMINATOR;
        int i6 = BEGIN_MACRO_PDF417_OPTIONAL_FIELD;
        int i7 = 928;
        int i8 = NUMERIC_COMPACTION_MODE_LATCH;
        long j = 900;
        if (i4 == BYTE_COMPACTION_MODE_LATCH) {
            int i9;
            int i10;
            int[] iArr2 = new int[6];
            int i11 = i2 + 1;
            int i12 = iArr[i2];
            i3 = i11;
            Object obj = null;
            loop0:
            while (true) {
                i9 = 0;
                long j2 = 0;
                while (i3 < iArr[0] && obj == null) {
                    int i13 = i9 + 1;
                    iArr2[i9] = i12;
                    j2 = (j2 * j) + ((long) i12);
                    int i14 = i3 + 1;
                    i12 = iArr[i3];
                    if (i12 == TEXT_COMPACTION_MODE_LATCH || i12 == BYTE_COMPACTION_MODE_LATCH || i12 == NUMERIC_COMPACTION_MODE_LATCH || i12 == BYTE_COMPACTION_MODE_LATCH_6 || i12 == 928 || i12 == i6 || i12 == i5) {
                        i3 = i14 - 1;
                        i9 = i13;
                        i5 = MACRO_PDF417_TERMINATOR;
                        i6 = BEGIN_MACRO_PDF417_OPTIONAL_FIELD;
                        j = 900;
                        obj = 1;
                    } else if (i13 % 5 != 0 || i13 <= 0) {
                        i3 = i14;
                        i9 = i13;
                        i5 = MACRO_PDF417_TERMINATOR;
                        i6 = BEGIN_MACRO_PDF417_OPTIONAL_FIELD;
                        j = 900;
                    } else {
                        i3 = 0;
                        while (i3 < 6) {
                            byteArrayOutputStream.write((byte) ((int) (j2 >> ((5 - i3) * 8))));
                            i3++;
                            i5 = MACRO_PDF417_TERMINATOR;
                            i6 = BEGIN_MACRO_PDF417_OPTIONAL_FIELD;
                        }
                        i3 = i14;
                        j = 900;
                    }
                }
            }
            if (i3 != iArr[0] || i12 >= TEXT_COMPACTION_MODE_LATCH) {
                i10 = i9;
            } else {
                i10 = i9 + 1;
                iArr2[i9] = i12;
            }
            for (i5 = 0; i5 < i10; i5++) {
                byteArrayOutputStream.write((byte) iArr2[i5]);
            }
        } else if (i4 == BYTE_COMPACTION_MODE_LATCH_6) {
            i3 = i2;
            Object obj2 = null;
            i5 = 0;
            j = 0;
            while (i3 < iArr[0] && obj2 == null) {
                i6 = i3 + 1;
                i3 = iArr[i3];
                if (i3 < TEXT_COMPACTION_MODE_LATCH) {
                    i5++;
                    j = (j * 900) + ((long) i3);
                    i3 = i6;
                } else {
                    if (i3 != TEXT_COMPACTION_MODE_LATCH && i3 != BYTE_COMPACTION_MODE_LATCH && i3 != i8 && i3 != BYTE_COMPACTION_MODE_LATCH_6 && i3 != i7) {
                        if (i3 != BEGIN_MACRO_PDF417_OPTIONAL_FIELD) {
                            if (i3 != MACRO_PDF417_TERMINATOR) {
                                i3 = i6;
                            }
                            i3 = i6 - 1;
                            obj2 = 1;
                        }
                    }
                    i3 = i6 - 1;
                    obj2 = 1;
                }
                if (i5 % 5 == 0 && i5 > 0) {
                    for (i5 = 0; i5 < 6; i5++) {
                        byteArrayOutputStream.write((byte) ((int) (j >> ((5 - i5) * 8))));
                    }
                    i5 = 0;
                    j = 0;
                }
                i7 = 928;
                i8 = NUMERIC_COMPACTION_MODE_LATCH;
            }
        } else {
            i3 = i2;
        }
        stringBuilder.append(new String(byteArrayOutputStream.toByteArray(), charset));
        return i3;
    }

    private static int numericCompaction(int[] iArr, int i, StringBuilder stringBuilder) throws FormatException {
        int[] iArr2 = new int[15];
        Object obj = null;
        int i2 = 0;
        while (i < iArr[0] && r2 == null) {
            int i3 = i + 1;
            i = iArr[i];
            if (i3 == iArr[0]) {
                obj = 1;
            }
            if (i < TEXT_COMPACTION_MODE_LATCH) {
                iArr2[i2] = i;
                i2++;
            } else if (i == TEXT_COMPACTION_MODE_LATCH || i == BYTE_COMPACTION_MODE_LATCH || i == BYTE_COMPACTION_MODE_LATCH_6 || i == 928 || i == BEGIN_MACRO_PDF417_OPTIONAL_FIELD || i == MACRO_PDF417_TERMINATOR) {
                i3--;
                obj = 1;
            }
            if ((i2 % 15 == 0 || i == NUMERIC_COMPACTION_MODE_LATCH || obj != null) && i2 > 0) {
                stringBuilder.append(decodeBase900toBase10(iArr2, i2));
                i2 = 0;
            }
            i = i3;
        }
        return i;
    }

    private static String decodeBase900toBase10(int[] iArr, int i) throws FormatException {
        BigInteger bigInteger = BigInteger.ZERO;
        for (int i2 = 0; i2 < i; i2++) {
            bigInteger = bigInteger.add(EXP900[(i - i2) - 1].multiply(BigInteger.valueOf((long) iArr[i2])));
        }
        String bigInteger2 = bigInteger.toString();
        if (bigInteger2.charAt(0) == '1') {
            return bigInteger2.substring(1);
        }
        throw FormatException.getFormatInstance();
    }
}
