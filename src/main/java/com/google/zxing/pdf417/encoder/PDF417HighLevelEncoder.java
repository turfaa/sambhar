package com.google.zxing.pdf417.encoder;

import com.facebook.appevents.AppEventsConstants;
import com.google.zxing.WriterException;
import com.google.zxing.common.CharacterSetECI;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;

final class PDF417HighLevelEncoder {
    private static final int BYTE_COMPACTION = 1;
    private static final Charset DEFAULT_ENCODING = Charset.forName("ISO-8859-1");
    private static final int ECI_CHARSET = 927;
    private static final int ECI_GENERAL_PURPOSE = 926;
    private static final int ECI_USER_DEFINED = 925;
    private static final int LATCH_TO_BYTE = 924;
    private static final int LATCH_TO_BYTE_PADDED = 901;
    private static final int LATCH_TO_NUMERIC = 902;
    private static final int LATCH_TO_TEXT = 900;
    private static final byte[] MIXED = new byte[128];
    private static final int NUMERIC_COMPACTION = 2;
    private static final byte[] PUNCTUATION = new byte[128];
    private static final int SHIFT_TO_BYTE = 913;
    private static final int SUBMODE_ALPHA = 0;
    private static final int SUBMODE_LOWER = 1;
    private static final int SUBMODE_MIXED = 2;
    private static final int SUBMODE_PUNCTUATION = 3;
    private static final int TEXT_COMPACTION = 0;
    private static final byte[] TEXT_MIXED_RAW = new byte[]{(byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 38, (byte) 13, (byte) 9, (byte) 44, (byte) 58, (byte) 35, (byte) 45, (byte) 46, (byte) 36, (byte) 47, (byte) 43, (byte) 37, (byte) 42, (byte) 61, (byte) 94, (byte) 0, (byte) 32, (byte) 0, (byte) 0, (byte) 0};
    private static final byte[] TEXT_PUNCTUATION_RAW = new byte[]{(byte) 59, (byte) 60, (byte) 62, (byte) 64, (byte) 91, (byte) 92, (byte) 93, (byte) 95, (byte) 96, (byte) 126, (byte) 33, (byte) 13, (byte) 9, (byte) 44, (byte) 58, (byte) 10, (byte) 45, (byte) 46, (byte) 36, (byte) 47, (byte) 34, (byte) 124, (byte) 42, (byte) 40, (byte) 41, (byte) 63, (byte) 123, (byte) 125, (byte) 39, (byte) 0};

    private static boolean isAlphaLower(char c) {
        return c == ' ' || (c >= 'a' && c <= 'z');
    }

    private static boolean isAlphaUpper(char c) {
        return c == ' ' || (c >= 'A' && c <= 'Z');
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isText(char c) {
        return c == 9 || c == 10 || c == 13 || (c >= ' ' && c <= '~');
    }

    static {
        Arrays.fill(MIXED, (byte) -1);
        for (int i = 0; i < TEXT_MIXED_RAW.length; i++) {
            byte b = TEXT_MIXED_RAW[i];
            if (b > (byte) 0) {
                MIXED[b] = (byte) i;
            }
        }
        Arrays.fill(PUNCTUATION, (byte) -1);
        for (int i2 = 0; i2 < TEXT_PUNCTUATION_RAW.length; i2++) {
            byte b2 = TEXT_PUNCTUATION_RAW[i2];
            if (b2 > (byte) 0) {
                PUNCTUATION[b2] = (byte) i2;
            }
        }
    }

    private PDF417HighLevelEncoder() {
    }

    static String encodeHighLevel(String str, Compaction compaction, Charset charset) throws WriterException {
        StringBuilder stringBuilder = new StringBuilder(str.length());
        if (charset == null) {
            charset = DEFAULT_ENCODING;
        } else if (!DEFAULT_ENCODING.equals(charset)) {
            CharacterSetECI characterSetECIByName = CharacterSetECI.getCharacterSetECIByName(charset.name());
            if (characterSetECIByName != null) {
                encodingECI(characterSetECIByName.getValue(), stringBuilder);
            }
        }
        int length = str.length();
        if (compaction != Compaction.TEXT) {
            if (compaction != Compaction.BYTE) {
                if (compaction != Compaction.NUMERIC) {
                    int i = 0;
                    int i2 = 0;
                    loop0:
                    while (true) {
                        int i3 = 0;
                        while (i < length) {
                            int determineConsecutiveDigitCount = determineConsecutiveDigitCount(str, i);
                            if (determineConsecutiveDigitCount >= 13) {
                                stringBuilder.append(902);
                                i2 = 2;
                                encodeNumeric(str, i, determineConsecutiveDigitCount, stringBuilder);
                                i += determineConsecutiveDigitCount;
                            } else {
                                int determineConsecutiveTextCount = determineConsecutiveTextCount(str, i);
                                if (determineConsecutiveTextCount >= 5 || determineConsecutiveDigitCount == length) {
                                    if (i2 != 0) {
                                        stringBuilder.append(900);
                                        i2 = 0;
                                        i3 = 0;
                                    }
                                    i3 = encodeText(str, i, determineConsecutiveTextCount, stringBuilder, i3);
                                    i += determineConsecutiveTextCount;
                                } else {
                                    determineConsecutiveDigitCount = determineConsecutiveBinaryCount(str, i, charset);
                                    if (determineConsecutiveDigitCount == 0) {
                                        determineConsecutiveDigitCount = 1;
                                    }
                                    determineConsecutiveDigitCount += i;
                                    byte[] bytes = str.substring(i, determineConsecutiveDigitCount).getBytes(charset);
                                    if (bytes.length == 1 && i2 == 0) {
                                        encodeBinary(bytes, 0, 1, 0, stringBuilder);
                                    } else {
                                        encodeBinary(bytes, 0, bytes.length, i2, stringBuilder);
                                        i2 = 1;
                                        i3 = 0;
                                    }
                                    i = determineConsecutiveDigitCount;
                                }
                            }
                        }
                        break loop0;
                    }
                }
                stringBuilder.append(902);
                encodeNumeric(str, 0, length, stringBuilder);
            } else {
                byte[] bytes2 = str.getBytes(charset);
                encodeBinary(bytes2, 0, bytes2.length, 1, stringBuilder);
            }
        } else {
            encodeText(str, 0, length, stringBuilder, 0);
        }
        return stringBuilder.toString();
    }

    /* JADX WARNING: Missing block: B:41:0x00d0, code skipped:
            r9 = 1;
     */
    /* JADX WARNING: Missing block: B:45:0x00dc, code skipped:
            r9 = 2;
     */
    /* JADX WARNING: Missing block: B:47:0x00ea, code skipped:
            r8 = r8 + 1;
     */
    /* JADX WARNING: Missing block: B:48:0x00ec, code skipped:
            if (r8 < r1) goto L_0x0010;
     */
    /* JADX WARNING: Missing block: B:49:0x00ee, code skipped:
            r0 = r3.length();
            r1 = 0;
            r7 = 0;
     */
    /* JADX WARNING: Missing block: B:50:0x00f4, code skipped:
            if (r1 >= r0) goto L_0x0112;
     */
    /* JADX WARNING: Missing block: B:52:0x00f8, code skipped:
            if ((r1 % 2) == 0) goto L_0x00fc;
     */
    /* JADX WARNING: Missing block: B:53:0x00fa, code skipped:
            r8 = 1;
     */
    /* JADX WARNING: Missing block: B:54:0x00fc, code skipped:
            r8 = null;
     */
    /* JADX WARNING: Missing block: B:55:0x00fd, code skipped:
            if (r8 == null) goto L_0x010b;
     */
    /* JADX WARNING: Missing block: B:56:0x00ff, code skipped:
            r7 = (char) ((r7 * 30) + r3.charAt(r1));
            r2.append(r7);
     */
    /* JADX WARNING: Missing block: B:57:0x010b, code skipped:
            r7 = r3.charAt(r1);
     */
    /* JADX WARNING: Missing block: B:58:0x010f, code skipped:
            r1 = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:60:0x0113, code skipped:
            if ((r0 % 2) == 0) goto L_0x011c;
     */
    /* JADX WARNING: Missing block: B:61:0x0115, code skipped:
            r2.append((char) ((r7 * 30) + 29));
     */
    /* JADX WARNING: Missing block: B:62:0x011c, code skipped:
            return r9;
     */
    /* JADX WARNING: Missing block: B:64:0x0120, code skipped:
            r9 = 0;
     */
    private static int encodeText(java.lang.CharSequence r16, int r17, int r18, java.lang.StringBuilder r19, int r20) {
        /*
        r0 = r16;
        r1 = r18;
        r2 = r19;
        r3 = new java.lang.StringBuilder;
        r3.<init>(r1);
        r4 = 2;
        r6 = 0;
        r9 = r20;
        r8 = 0;
    L_0x0010:
        r10 = r17 + r8;
        r11 = r0.charAt(r10);
        r12 = 26;
        r13 = 32;
        r14 = 28;
        r15 = 27;
        r5 = 29;
        switch(r9) {
            case 0: goto L_0x00b4;
            case 1: goto L_0x007b;
            case 2: goto L_0x0033;
            default: goto L_0x0023;
        };
    L_0x0023:
        r10 = isPunctuation(r11);
        if (r10 == 0) goto L_0x011d;
    L_0x0029:
        r10 = PUNCTUATION;
        r10 = r10[r11];
        r10 = (char) r10;
        r3.append(r10);
        goto L_0x00ea;
    L_0x0033:
        r12 = isMixed(r11);
        if (r12 == 0) goto L_0x0043;
    L_0x0039:
        r10 = MIXED;
        r10 = r10[r11];
        r10 = (char) r10;
        r3.append(r10);
        goto L_0x00ea;
    L_0x0043:
        r12 = isAlphaUpper(r11);
        if (r12 == 0) goto L_0x004e;
    L_0x0049:
        r3.append(r14);
        goto L_0x0120;
    L_0x004e:
        r12 = isAlphaLower(r11);
        if (r12 == 0) goto L_0x0059;
    L_0x0054:
        r3.append(r15);
        goto L_0x00d0;
    L_0x0059:
        r10 = r10 + 1;
        if (r10 >= r1) goto L_0x006e;
    L_0x005d:
        r10 = r0.charAt(r10);
        r10 = isPunctuation(r10);
        if (r10 == 0) goto L_0x006e;
    L_0x0067:
        r9 = 3;
        r5 = 25;
        r3.append(r5);
        goto L_0x0010;
    L_0x006e:
        r3.append(r5);
        r10 = PUNCTUATION;
        r10 = r10[r11];
        r10 = (char) r10;
        r3.append(r10);
        goto L_0x00ea;
    L_0x007b:
        r10 = isAlphaLower(r11);
        if (r10 == 0) goto L_0x008e;
    L_0x0081:
        if (r11 != r13) goto L_0x0087;
    L_0x0083:
        r3.append(r12);
        goto L_0x00ea;
    L_0x0087:
        r11 = r11 + -97;
        r10 = (char) r11;
        r3.append(r10);
        goto L_0x00ea;
    L_0x008e:
        r10 = isAlphaUpper(r11);
        if (r10 == 0) goto L_0x009e;
    L_0x0094:
        r3.append(r15);
        r11 = r11 + -65;
        r10 = (char) r11;
        r3.append(r10);
        goto L_0x00ea;
    L_0x009e:
        r10 = isMixed(r11);
        if (r10 == 0) goto L_0x00a8;
    L_0x00a4:
        r3.append(r14);
        goto L_0x00dc;
    L_0x00a8:
        r3.append(r5);
        r10 = PUNCTUATION;
        r10 = r10[r11];
        r10 = (char) r10;
        r3.append(r10);
        goto L_0x00ea;
    L_0x00b4:
        r10 = isAlphaUpper(r11);
        if (r10 == 0) goto L_0x00c7;
    L_0x00ba:
        if (r11 != r13) goto L_0x00c0;
    L_0x00bc:
        r3.append(r12);
        goto L_0x00ea;
    L_0x00c0:
        r11 = r11 + -65;
        r10 = (char) r11;
        r3.append(r10);
        goto L_0x00ea;
    L_0x00c7:
        r10 = isAlphaLower(r11);
        if (r10 == 0) goto L_0x00d3;
    L_0x00cd:
        r3.append(r15);
    L_0x00d0:
        r9 = 1;
        goto L_0x0010;
    L_0x00d3:
        r10 = isMixed(r11);
        if (r10 == 0) goto L_0x00df;
    L_0x00d9:
        r3.append(r14);
    L_0x00dc:
        r9 = 2;
        goto L_0x0010;
    L_0x00df:
        r3.append(r5);
        r10 = PUNCTUATION;
        r10 = r10[r11];
        r10 = (char) r10;
        r3.append(r10);
    L_0x00ea:
        r8 = r8 + 1;
        if (r8 < r1) goto L_0x0010;
    L_0x00ee:
        r0 = r3.length();
        r1 = 0;
        r7 = 0;
    L_0x00f4:
        if (r1 >= r0) goto L_0x0112;
    L_0x00f6:
        r8 = r1 % 2;
        if (r8 == 0) goto L_0x00fc;
    L_0x00fa:
        r8 = 1;
        goto L_0x00fd;
    L_0x00fc:
        r8 = 0;
    L_0x00fd:
        if (r8 == 0) goto L_0x010b;
    L_0x00ff:
        r7 = r7 * 30;
        r8 = r3.charAt(r1);
        r7 = r7 + r8;
        r7 = (char) r7;
        r2.append(r7);
        goto L_0x010f;
    L_0x010b:
        r7 = r3.charAt(r1);
    L_0x010f:
        r1 = r1 + 1;
        goto L_0x00f4;
    L_0x0112:
        r0 = r0 % r4;
        if (r0 == 0) goto L_0x011c;
    L_0x0115:
        r7 = r7 * 30;
        r7 = r7 + r5;
        r0 = (char) r7;
        r2.append(r0);
    L_0x011c:
        return r9;
    L_0x011d:
        r3.append(r5);
    L_0x0120:
        r9 = 0;
        goto L_0x0010;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.pdf417.encoder.PDF417HighLevelEncoder.encodeText(java.lang.CharSequence, int, int, java.lang.StringBuilder, int):int");
    }

    private static void encodeBinary(byte[] bArr, int i, int i2, int i3, StringBuilder stringBuilder) {
        int i4;
        if (i2 == 1 && i3 == 0) {
            stringBuilder.append(913);
        } else if (i2 % 6 == 0) {
            stringBuilder.append(924);
        } else {
            stringBuilder.append(901);
        }
        if (i2 >= 6) {
            char[] cArr = new char[5];
            i4 = i;
            while ((i + i2) - i4 >= 6) {
                int i5;
                long j = 0;
                for (i5 = 0; i5 < 6; i5++) {
                    j = (j << 8) + ((long) (bArr[i4 + i5] & 255));
                }
                for (int i6 = 0; i6 < 5; i6++) {
                    cArr[i6] = (char) ((int) (j % 900));
                    j /= 900;
                }
                for (i5 = 4; i5 >= 0; i5--) {
                    stringBuilder.append(cArr[i5]);
                }
                i4 += 6;
            }
        } else {
            i4 = i;
        }
        while (i4 < i + i2) {
            stringBuilder.append((char) (bArr[i4] & 255));
            i4++;
        }
    }

    private static void encodeNumeric(String str, int i, int i2, StringBuilder stringBuilder) {
        StringBuilder stringBuilder2 = new StringBuilder((i2 / 3) + 1);
        BigInteger valueOf = BigInteger.valueOf(900);
        BigInteger valueOf2 = BigInteger.valueOf(0);
        int i3 = 0;
        while (i3 < i2) {
            stringBuilder2.setLength(0);
            int min = Math.min(44, i2 - i3);
            StringBuilder stringBuilder3 = new StringBuilder(AppEventsConstants.EVENT_PARAM_VALUE_YES);
            int i4 = i + i3;
            stringBuilder3.append(str.substring(i4, i4 + min));
            BigInteger bigInteger = new BigInteger(stringBuilder3.toString());
            do {
                stringBuilder2.append((char) bigInteger.mod(valueOf).intValue());
                bigInteger = bigInteger.divide(valueOf);
            } while (!bigInteger.equals(valueOf2));
            for (int length = stringBuilder2.length() - 1; length >= 0; length--) {
                stringBuilder.append(stringBuilder2.charAt(length));
            }
            i3 += min;
        }
    }

    private static boolean isMixed(char c) {
        return MIXED[c] != (byte) -1;
    }

    private static boolean isPunctuation(char c) {
        return PUNCTUATION[c] != (byte) -1;
    }

    private static int determineConsecutiveDigitCount(CharSequence charSequence, int i) {
        int length = charSequence.length();
        int i2 = 0;
        if (i < length) {
            char charAt = charSequence.charAt(i);
            while (isDigit(charAt) && i < length) {
                i2++;
                i++;
                if (i < length) {
                    charAt = charSequence.charAt(i);
                }
            }
        }
        return i2;
    }

    private static int determineConsecutiveTextCount(CharSequence charSequence, int i) {
        int length = charSequence.length();
        int i2 = i;
        while (i2 < length) {
            char charAt = charSequence.charAt(i2);
            int i3 = 0;
            while (i3 < 13 && isDigit(charAt) && i2 < length) {
                i3++;
                i2++;
                if (i2 < length) {
                    charAt = charSequence.charAt(i2);
                }
            }
            if (i3 < 13) {
                if (i3 <= 0) {
                    if (!isText(charSequence.charAt(i2))) {
                        break;
                    }
                    i2++;
                }
            } else {
                return (i2 - i) - i3;
            }
        }
        return i2 - i;
    }

    private static int determineConsecutiveBinaryCount(String str, int i, Charset charset) throws WriterException {
        CharsetEncoder newEncoder = charset.newEncoder();
        int length = str.length();
        int i2 = i;
        while (i2 < length) {
            char charAt = str.charAt(i2);
            int i3 = 0;
            while (i3 < 13 && isDigit(charAt)) {
                i3++;
                int i4 = i2 + i3;
                if (i4 >= length) {
                    break;
                }
                charAt = str.charAt(i4);
            }
            if (i3 >= 13) {
                return i2 - i;
            }
            charAt = str.charAt(i2);
            if (newEncoder.canEncode(charAt)) {
                i2++;
            } else {
                StringBuilder stringBuilder = new StringBuilder("Non-encodable character detected: ");
                stringBuilder.append(charAt);
                stringBuilder.append(" (Unicode: ");
                stringBuilder.append(charAt);
                stringBuilder.append(')');
                throw new WriterException(stringBuilder.toString());
            }
        }
        return i2 - i;
    }

    private static void encodingECI(int i, StringBuilder stringBuilder) throws WriterException {
        if (i >= 0 && i < LATCH_TO_TEXT) {
            stringBuilder.append(927);
            stringBuilder.append((char) i);
        } else if (i < 810900) {
            stringBuilder.append(926);
            stringBuilder.append((char) ((i / LATCH_TO_TEXT) - 1));
            stringBuilder.append((char) (i % LATCH_TO_TEXT));
        } else if (i < 811800) {
            stringBuilder.append(925);
            stringBuilder.append((char) (810900 - i));
        } else {
            StringBuilder stringBuilder2 = new StringBuilder("ECI number not in valid range from 0..811799, but was ");
            stringBuilder2.append(i);
            throw new WriterException(stringBuilder2.toString());
        }
    }
}
