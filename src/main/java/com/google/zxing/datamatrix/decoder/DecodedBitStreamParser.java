package com.google.zxing.datamatrix.decoder;

import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import com.google.zxing.FormatException;
import com.google.zxing.common.BitSource;
import com.twitter.sdk.android.core.internal.TwitterApiConstants.Errors;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

final class DecodedBitStreamParser {
    private static final char[] C40_BASIC_SET_CHARS = new char[]{'*', '*', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final char[] C40_SHIFT2_SET_CHARS = new char[]{'!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_'};
    private static final char[] TEXT_BASIC_SET_CHARS = new char[]{'*', '*', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final char[] TEXT_SHIFT2_SET_CHARS = C40_SHIFT2_SET_CHARS;
    private static final char[] TEXT_SHIFT3_SET_CHARS = new char[]{'`', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '{', '|', '}', '~', 127};

    private enum Mode {
        PAD_ENCODE,
        ASCII_ENCODE,
        C40_ENCODE,
        TEXT_ENCODE,
        ANSIX12_ENCODE,
        EDIFACT_ENCODE,
        BASE256_ENCODE
    }

    private DecodedBitStreamParser() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0068  */
    static com.google.zxing.common.DecoderResult decode(byte[] r6) throws com.google.zxing.FormatException {
        /*
        r0 = new com.google.zxing.common.BitSource;
        r0.<init>(r6);
        r1 = new java.lang.StringBuilder;
        r2 = 100;
        r1.<init>(r2);
        r2 = new java.lang.StringBuilder;
        r3 = 0;
        r2.<init>(r3);
        r3 = new java.util.ArrayList;
        r4 = 1;
        r3.<init>(r4);
        r4 = com.google.zxing.datamatrix.decoder.DecodedBitStreamParser.Mode.ASCII_ENCODE;
    L_0x001a:
        r5 = com.google.zxing.datamatrix.decoder.DecodedBitStreamParser.Mode.ASCII_ENCODE;
        if (r4 != r5) goto L_0x0023;
    L_0x001e:
        r4 = decodeAsciiSegment(r0, r1, r2);
        goto L_0x0048;
    L_0x0023:
        r5 = com.google.zxing.datamatrix.decoder.DecodedBitStreamParser.AnonymousClass1.$SwitchMap$com$google$zxing$datamatrix$decoder$DecodedBitStreamParser$Mode;
        r4 = r4.ordinal();
        r4 = r5[r4];
        switch(r4) {
            case 1: goto L_0x0043;
            case 2: goto L_0x003f;
            case 3: goto L_0x003b;
            case 4: goto L_0x0037;
            case 5: goto L_0x0033;
            default: goto L_0x002e;
        };
    L_0x002e:
        r6 = com.google.zxing.FormatException.getFormatInstance();
        throw r6;
    L_0x0033:
        decodeBase256Segment(r0, r1, r3);
        goto L_0x0046;
    L_0x0037:
        decodeEdifactSegment(r0, r1);
        goto L_0x0046;
    L_0x003b:
        decodeAnsiX12Segment(r0, r1);
        goto L_0x0046;
    L_0x003f:
        decodeTextSegment(r0, r1);
        goto L_0x0046;
    L_0x0043:
        decodeC40Segment(r0, r1);
    L_0x0046:
        r4 = com.google.zxing.datamatrix.decoder.DecodedBitStreamParser.Mode.ASCII_ENCODE;
    L_0x0048:
        r5 = com.google.zxing.datamatrix.decoder.DecodedBitStreamParser.Mode.PAD_ENCODE;
        if (r4 == r5) goto L_0x0052;
    L_0x004c:
        r5 = r0.available();
        if (r5 > 0) goto L_0x001a;
    L_0x0052:
        r0 = r2.length();
        if (r0 <= 0) goto L_0x005b;
    L_0x0058:
        r1.append(r2);
    L_0x005b:
        r0 = new com.google.zxing.common.DecoderResult;
        r1 = r1.toString();
        r2 = r3.isEmpty();
        r4 = 0;
        if (r2 == 0) goto L_0x0069;
    L_0x0068:
        r3 = r4;
    L_0x0069:
        r0.<init>(r6, r1, r3, r4);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.datamatrix.decoder.DecodedBitStreamParser.decode(byte[]):com.google.zxing.common.DecoderResult");
    }

    private static Mode decodeAsciiSegment(BitSource bitSource, StringBuilder stringBuilder, StringBuilder stringBuilder2) throws FormatException {
        Object obj = null;
        do {
            int readBits = bitSource.readBits(8);
            if (readBits == 0) {
                throw FormatException.getFormatInstance();
            } else if (readBits <= 128) {
                if (obj != null) {
                    readBits += 128;
                }
                stringBuilder.append((char) (readBits - 1));
                return Mode.ASCII_ENCODE;
            } else if (readBits == 129) {
                return Mode.PAD_ENCODE;
            } else {
                if (readBits <= 229) {
                    readBits -= 130;
                    if (readBits < 10) {
                        stringBuilder.append('0');
                    }
                    stringBuilder.append(readBits);
                } else if (readBits == 230) {
                    return Mode.C40_ENCODE;
                } else {
                    if (readBits == 231) {
                        return Mode.BASE256_ENCODE;
                    }
                    if (readBits == 232) {
                        stringBuilder.append(29);
                    } else if (!(readBits == 233 || readBits == 234)) {
                        if (readBits == 235) {
                            obj = 1;
                        } else if (readBits == 236) {
                            stringBuilder.append("[)>\u001e05\u001d");
                            stringBuilder2.insert(0, "\u001e\u0004");
                        } else if (readBits == 237) {
                            stringBuilder.append("[)>\u001e06\u001d");
                            stringBuilder2.insert(0, "\u001e\u0004");
                        } else if (readBits == 238) {
                            return Mode.ANSIX12_ENCODE;
                        } else {
                            if (readBits == Errors.GUEST_AUTH_ERROR_CODE) {
                                return Mode.TEXT_ENCODE;
                            }
                            if (readBits == 240) {
                                return Mode.EDIFACT_ENCODE;
                            }
                            if (!(readBits == 241 || readBits < 242 || (readBits == 254 && bitSource.available() == 0))) {
                                throw FormatException.getFormatInstance();
                            }
                        }
                    }
                }
            }
        } while (bitSource.available() > 0);
        return Mode.ASCII_ENCODE;
    }

    /* JADX WARNING: Missing block: B:15:0x0035, code skipped:
            r5 = null;
     */
    private static void decodeC40Segment(com.google.zxing.common.BitSource r8, java.lang.StringBuilder r9) throws com.google.zxing.FormatException {
        /*
        r0 = 3;
        r1 = new int[r0];
        r2 = 0;
        r3 = 0;
        r4 = 0;
    L_0x0006:
        r5 = r8.available();
        r6 = 8;
        if (r5 != r6) goto L_0x000f;
    L_0x000e:
        return;
    L_0x000f:
        r5 = r8.readBits(r6);
        r7 = 254; // 0xfe float:3.56E-43 double:1.255E-321;
        if (r5 != r7) goto L_0x0018;
    L_0x0017:
        return;
    L_0x0018:
        r6 = r8.readBits(r6);
        parseTwoBytes(r5, r6, r1);
        r5 = r3;
        r3 = 0;
    L_0x0021:
        if (r3 >= r0) goto L_0x009e;
    L_0x0023:
        r6 = r1[r3];
        switch(r4) {
            case 0: goto L_0x007b;
            case 1: goto L_0x006c;
            case 2: goto L_0x003e;
            case 3: goto L_0x002d;
            default: goto L_0x0028;
        };
    L_0x0028:
        r8 = com.google.zxing.FormatException.getFormatInstance();
        throw r8;
    L_0x002d:
        if (r5 == 0) goto L_0x0037;
    L_0x002f:
        r6 = r6 + 224;
        r4 = (char) r6;
        r9.append(r4);
    L_0x0035:
        r5 = 0;
        goto L_0x0079;
    L_0x0037:
        r6 = r6 + 96;
        r4 = (char) r6;
        r9.append(r4);
        goto L_0x0079;
    L_0x003e:
        r4 = C40_SHIFT2_SET_CHARS;
        r4 = r4.length;
        if (r6 >= r4) goto L_0x0055;
    L_0x0043:
        r4 = C40_SHIFT2_SET_CHARS;
        r4 = r4[r6];
        if (r5 == 0) goto L_0x0051;
    L_0x0049:
        r4 = r4 + 128;
        r4 = (char) r4;
        r9.append(r4);
        r4 = 0;
        goto L_0x0065;
    L_0x0051:
        r9.append(r4);
        goto L_0x005e;
    L_0x0055:
        r4 = 27;
        if (r6 != r4) goto L_0x0060;
    L_0x0059:
        r4 = 29;
        r9.append(r4);
    L_0x005e:
        r4 = r5;
        goto L_0x0065;
    L_0x0060:
        r4 = 30;
        if (r6 != r4) goto L_0x0067;
    L_0x0064:
        r4 = 1;
    L_0x0065:
        r5 = r4;
        goto L_0x0079;
    L_0x0067:
        r8 = com.google.zxing.FormatException.getFormatInstance();
        throw r8;
    L_0x006c:
        if (r5 == 0) goto L_0x0075;
    L_0x006e:
        r6 = r6 + 128;
        r4 = (char) r6;
        r9.append(r4);
        goto L_0x0035;
    L_0x0075:
        r4 = (char) r6;
        r9.append(r4);
    L_0x0079:
        r4 = 0;
        goto L_0x0096;
    L_0x007b:
        if (r6 >= r0) goto L_0x0080;
    L_0x007d:
        r4 = r6 + 1;
        goto L_0x0096;
    L_0x0080:
        r7 = C40_BASIC_SET_CHARS;
        r7 = r7.length;
        if (r6 >= r7) goto L_0x0099;
    L_0x0085:
        r7 = C40_BASIC_SET_CHARS;
        r6 = r7[r6];
        if (r5 == 0) goto L_0x0093;
    L_0x008b:
        r6 = r6 + 128;
        r5 = (char) r6;
        r9.append(r5);
        r5 = 0;
        goto L_0x0096;
    L_0x0093:
        r9.append(r6);
    L_0x0096:
        r3 = r3 + 1;
        goto L_0x0021;
    L_0x0099:
        r8 = com.google.zxing.FormatException.getFormatInstance();
        throw r8;
    L_0x009e:
        r3 = r8.available();
        if (r3 > 0) goto L_0x00a5;
    L_0x00a4:
        return;
    L_0x00a5:
        r3 = r5;
        goto L_0x0006;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.datamatrix.decoder.DecodedBitStreamParser.decodeC40Segment(com.google.zxing.common.BitSource, java.lang.StringBuilder):void");
    }

    /* JADX WARNING: Missing block: B:18:0x003e, code skipped:
            r5 = null;
     */
    private static void decodeTextSegment(com.google.zxing.common.BitSource r8, java.lang.StringBuilder r9) throws com.google.zxing.FormatException {
        /*
        r0 = 3;
        r1 = new int[r0];
        r2 = 0;
        r3 = 0;
        r4 = 0;
    L_0x0006:
        r5 = r8.available();
        r6 = 8;
        if (r5 != r6) goto L_0x000f;
    L_0x000e:
        return;
    L_0x000f:
        r5 = r8.readBits(r6);
        r7 = 254; // 0xfe float:3.56E-43 double:1.255E-321;
        if (r5 != r7) goto L_0x0018;
    L_0x0017:
        return;
    L_0x0018:
        r6 = r8.readBits(r6);
        parseTwoBytes(r5, r6, r1);
        r5 = r3;
        r3 = 0;
    L_0x0021:
        if (r3 >= r0) goto L_0x00aa;
    L_0x0023:
        r6 = r1[r3];
        switch(r4) {
            case 0: goto L_0x0086;
            case 1: goto L_0x0077;
            case 2: goto L_0x0049;
            case 3: goto L_0x002d;
            default: goto L_0x0028;
        };
    L_0x0028:
        r8 = com.google.zxing.FormatException.getFormatInstance();
        throw r8;
    L_0x002d:
        r4 = TEXT_SHIFT3_SET_CHARS;
        r4 = r4.length;
        if (r6 >= r4) goto L_0x0044;
    L_0x0032:
        r4 = TEXT_SHIFT3_SET_CHARS;
        r4 = r4[r6];
        if (r5 == 0) goto L_0x0040;
    L_0x0038:
        r4 = r4 + 128;
        r4 = (char) r4;
        r9.append(r4);
    L_0x003e:
        r5 = 0;
        goto L_0x0084;
    L_0x0040:
        r9.append(r4);
        goto L_0x0084;
    L_0x0044:
        r8 = com.google.zxing.FormatException.getFormatInstance();
        throw r8;
    L_0x0049:
        r4 = TEXT_SHIFT2_SET_CHARS;
        r4 = r4.length;
        if (r6 >= r4) goto L_0x0060;
    L_0x004e:
        r4 = TEXT_SHIFT2_SET_CHARS;
        r4 = r4[r6];
        if (r5 == 0) goto L_0x005c;
    L_0x0054:
        r4 = r4 + 128;
        r4 = (char) r4;
        r9.append(r4);
        r4 = 0;
        goto L_0x0070;
    L_0x005c:
        r9.append(r4);
        goto L_0x0069;
    L_0x0060:
        r4 = 27;
        if (r6 != r4) goto L_0x006b;
    L_0x0064:
        r4 = 29;
        r9.append(r4);
    L_0x0069:
        r4 = r5;
        goto L_0x0070;
    L_0x006b:
        r4 = 30;
        if (r6 != r4) goto L_0x0072;
    L_0x006f:
        r4 = 1;
    L_0x0070:
        r5 = r4;
        goto L_0x0084;
    L_0x0072:
        r8 = com.google.zxing.FormatException.getFormatInstance();
        throw r8;
    L_0x0077:
        if (r5 == 0) goto L_0x0080;
    L_0x0079:
        r6 = r6 + 128;
        r4 = (char) r6;
        r9.append(r4);
        goto L_0x003e;
    L_0x0080:
        r4 = (char) r6;
        r9.append(r4);
    L_0x0084:
        r4 = 0;
        goto L_0x00a1;
    L_0x0086:
        if (r6 >= r0) goto L_0x008b;
    L_0x0088:
        r4 = r6 + 1;
        goto L_0x00a1;
    L_0x008b:
        r7 = TEXT_BASIC_SET_CHARS;
        r7 = r7.length;
        if (r6 >= r7) goto L_0x00a5;
    L_0x0090:
        r7 = TEXT_BASIC_SET_CHARS;
        r6 = r7[r6];
        if (r5 == 0) goto L_0x009e;
    L_0x0096:
        r6 = r6 + 128;
        r5 = (char) r6;
        r9.append(r5);
        r5 = 0;
        goto L_0x00a1;
    L_0x009e:
        r9.append(r6);
    L_0x00a1:
        r3 = r3 + 1;
        goto L_0x0021;
    L_0x00a5:
        r8 = com.google.zxing.FormatException.getFormatInstance();
        throw r8;
    L_0x00aa:
        r3 = r8.available();
        if (r3 > 0) goto L_0x00b1;
    L_0x00b0:
        return;
    L_0x00b1:
        r3 = r5;
        goto L_0x0006;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.datamatrix.decoder.DecodedBitStreamParser.decodeTextSegment(com.google.zxing.common.BitSource, java.lang.StringBuilder):void");
    }

    private static void decodeAnsiX12Segment(BitSource bitSource, StringBuilder stringBuilder) throws FormatException {
        int[] iArr = new int[3];
        while (bitSource.available() != 8) {
            int readBits = bitSource.readBits(8);
            if (readBits != 254) {
                parseTwoBytes(readBits, bitSource.readBits(8), iArr);
                for (readBits = 0; readBits < 3; readBits++) {
                    int i = iArr[readBits];
                    if (i == 0) {
                        stringBuilder.append(13);
                    } else if (i == 1) {
                        stringBuilder.append('*');
                    } else if (i == 2) {
                        stringBuilder.append('>');
                    } else if (i == 3) {
                        stringBuilder.append(' ');
                    } else if (i < 14) {
                        stringBuilder.append((char) (i + 44));
                    } else if (i < 40) {
                        stringBuilder.append((char) (i + 51));
                    } else {
                        throw FormatException.getFormatInstance();
                    }
                }
                if (bitSource.available() <= 0) {
                    return;
                }
            }
            return;
        }
    }

    private static void parseTwoBytes(int i, int i2, int[] iArr) {
        i = ((i << 8) + i2) - 1;
        int i3 = i / 1600;
        iArr[0] = i3;
        i -= i3 * 1600;
        i3 = i / 40;
        iArr[1] = i3;
        iArr[2] = i - (i3 * 40);
    }

    private static void decodeEdifactSegment(BitSource bitSource, StringBuilder stringBuilder) {
        while (bitSource.available() > 16) {
            for (int i = 0; i < 4; i++) {
                int readBits = bitSource.readBits(6);
                if (readBits == 31) {
                    int bitOffset = 8 - bitSource.getBitOffset();
                    if (bitOffset != 8) {
                        bitSource.readBits(bitOffset);
                    }
                    return;
                }
                if ((readBits & 32) == 0) {
                    readBits |= 64;
                }
                stringBuilder.append((char) readBits);
            }
            if (bitSource.available() <= 0) {
                return;
            }
        }
    }

    private static void decodeBase256Segment(BitSource bitSource, StringBuilder stringBuilder, Collection<byte[]> collection) throws FormatException {
        int byteOffset = bitSource.getByteOffset() + 1;
        int i = byteOffset + 1;
        byteOffset = unrandomize255State(bitSource.readBits(8), byteOffset);
        if (byteOffset == 0) {
            byteOffset = bitSource.available() / 8;
        } else if (byteOffset >= Callback.DEFAULT_SWIPE_ANIMATION_DURATION) {
            byteOffset = ((byteOffset - 249) * Callback.DEFAULT_SWIPE_ANIMATION_DURATION) + unrandomize255State(bitSource.readBits(8), i);
            i++;
        }
        if (byteOffset >= 0) {
            byte[] bArr = new byte[byteOffset];
            int i2 = 0;
            while (i2 < byteOffset) {
                if (bitSource.available() >= 8) {
                    int i3 = i + 1;
                    bArr[i2] = (byte) unrandomize255State(bitSource.readBits(8), i);
                    i2++;
                    i = i3;
                } else {
                    throw FormatException.getFormatInstance();
                }
            }
            collection.add(bArr);
            try {
                stringBuilder.append(new String(bArr, "ISO8859_1"));
                return;
            } catch (UnsupportedEncodingException e) {
                StringBuilder stringBuilder2 = new StringBuilder("Platform does not support required encoding: ");
                stringBuilder2.append(e);
                throw new IllegalStateException(stringBuilder2.toString());
            }
        }
        throw FormatException.getFormatInstance();
    }

    private static int unrandomize255State(int i, int i2) {
        i -= ((i2 * 149) % 255) + 1;
        return i >= 0 ? i : i + 256;
    }
}
