package com.google.zxing.aztec.decoder;

import com.facebook.appevents.AppEventsConstants;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.zxing.FormatException;
import com.google.zxing.aztec.AztecDetectorResult;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;
import com.twitter.sdk.android.core.internal.scribe.EventsFilesManager;
import io.sentry.connection.AbstractConnection;
import java.util.Arrays;
import org.slf4j.Marker;

public final class Decoder {
    private static final String[] DIGIT_TABLE = new String[]{"CTRL_PS", MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR, AppEventsConstants.EVENT_PARAM_VALUE_NO, AppEventsConstants.EVENT_PARAM_VALUE_YES, "2", "3", "4", "5", AbstractConnection.SENTRY_PROTOCOL_VERSION, "7", "8", "9", ",", ".", "CTRL_UL", "CTRL_US"};
    private static final String[] LOWER_TABLE = new String[]{"CTRL_PS", MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR, "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "CTRL_US", "CTRL_ML", "CTRL_DL", "CTRL_BS"};
    private static final String[] MIXED_TABLE = new String[]{"CTRL_PS", MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR, "\u0001", "\u0002", "\u0003", "\u0004", "\u0005", "\u0006", "\u0007", "\b", "\t", "\n", "\u000b", "\f", "\r", "\u001b", "\u001c", "\u001d", "\u001e", "\u001f", "@", "\\", "^", EventsFilesManager.ROLL_OVER_FILE_NAME_SEPARATOR, "`", "|", "~", "", "CTRL_LL", "CTRL_UL", "CTRL_PL", "CTRL_BS"};
    private static final String[] PUNCT_TABLE = new String[]{"", "\r", "\r\n", ". ", ", ", ": ", "!", "\"", "#", "$", "%", "&", "'", "(", ")", Marker.ANY_MARKER, Marker.ANY_NON_NULL_MARKER, ",", "-", ".", "/", ":", ";", "<", "=", ">", "?", "[", "]", "{", "}", "CTRL_UL"};
    private static final String[] UPPER_TABLE = new String[]{"CTRL_PS", MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CTRL_LL", "CTRL_ML", "CTRL_DL", "CTRL_BS"};
    private AztecDetectorResult ddata;

    private enum Table {
        UPPER,
        LOWER,
        MIXED,
        DIGIT,
        PUNCT,
        BINARY
    }

    private static int totalBitsInLayer(int i, boolean z) {
        return ((z ? 88 : 112) + (i << 4)) * i;
    }

    public DecoderResult decode(AztecDetectorResult aztecDetectorResult) throws FormatException {
        this.ddata = aztecDetectorResult;
        boolean[] correctBits = correctBits(extractBits(aztecDetectorResult.getBits()));
        DecoderResult decoderResult = new DecoderResult(convertBoolArrayToByteArray(correctBits), getEncodedData(correctBits), null, null);
        decoderResult.setNumBits(correctBits.length);
        return decoderResult;
    }

    public static String highLevelDecode(boolean[] zArr) {
        return getEncodedData(zArr);
    }

    private static String getEncodedData(boolean[] zArr) {
        int length = zArr.length;
        Table table = Table.UPPER;
        Table table2 = Table.UPPER;
        StringBuilder stringBuilder = new StringBuilder(20);
        Table table3 = table;
        int i = 0;
        while (i < length) {
            int i2;
            if (table2 == Table.BINARY) {
                if (length - i < 5) {
                    break;
                }
                int readCode = readCode(zArr, i, 5);
                i += 5;
                if (readCode == 0) {
                    if (length - i < 11) {
                        break;
                    }
                    readCode = readCode(zArr, i, 11) + 31;
                    i += 11;
                }
                i2 = i;
                for (i = 0; i < readCode; i++) {
                    if (length - i2 < 8) {
                        i = length;
                        break;
                    }
                    stringBuilder.append((char) readCode(zArr, i2, 8));
                    i2 += 8;
                }
                i = i2;
            } else {
                i2 = table2 == Table.DIGIT ? 4 : 5;
                if (length - i < i2) {
                    break;
                }
                int readCode2 = readCode(zArr, i, i2);
                i += i2;
                String character = getCharacter(table2, readCode2);
                if (character.startsWith("CTRL_")) {
                    table3 = getTable(character.charAt(5));
                    if (character.charAt(6) != 'L') {
                        Table table4 = table3;
                        table3 = table2;
                        table2 = table4;
                    }
                } else {
                    stringBuilder.append(character);
                }
            }
            table2 = table3;
        }
        return stringBuilder.toString();
    }

    private static Table getTable(char c) {
        if (c == 'B') {
            return Table.BINARY;
        }
        if (c == 'D') {
            return Table.DIGIT;
        }
        if (c == 'P') {
            return Table.PUNCT;
        }
        switch (c) {
            case 'L':
                return Table.LOWER;
            case 'M':
                return Table.MIXED;
            default:
                return Table.UPPER;
        }
    }

    private static String getCharacter(Table table, int i) {
        switch (table) {
            case UPPER:
                return UPPER_TABLE[i];
            case LOWER:
                return LOWER_TABLE[i];
            case MIXED:
                return MIXED_TABLE[i];
            case PUNCT:
                return PUNCT_TABLE[i];
            case DIGIT:
                return DIGIT_TABLE[i];
            default:
                throw new IllegalStateException("Bad table");
        }
    }

    private boolean[] correctBits(boolean[] zArr) throws FormatException {
        GenericGF genericGF;
        int i = 8;
        if (this.ddata.getNbLayers() <= 2) {
            i = 6;
            genericGF = GenericGF.AZTEC_DATA_6;
        } else if (this.ddata.getNbLayers() <= 8) {
            genericGF = GenericGF.AZTEC_DATA_8;
        } else if (this.ddata.getNbLayers() <= 22) {
            i = 10;
            genericGF = GenericGF.AZTEC_DATA_10;
        } else {
            i = 12;
            genericGF = GenericGF.AZTEC_DATA_12;
        }
        int nbDatablocks = this.ddata.getNbDatablocks();
        int length = zArr.length / i;
        if (length >= nbDatablocks) {
            int[] iArr = new int[length];
            int length2 = zArr.length % i;
            int i2 = 0;
            while (i2 < length) {
                iArr[i2] = readCode(zArr, length2, i);
                i2++;
                length2 += i;
            }
            try {
                new ReedSolomonDecoder(genericGF).decode(iArr, length - nbDatablocks);
                int i3 = (1 << i) - 1;
                i2 = 0;
                for (length = 0; length < nbDatablocks; length++) {
                    length2 = iArr[length];
                    if (length2 == 0 || length2 == i3) {
                        throw FormatException.getFormatInstance();
                    }
                    if (length2 == 1 || length2 == i3 - 1) {
                        i2++;
                    }
                }
                boolean[] zArr2 = new boolean[((nbDatablocks * i) - i2)];
                length2 = 0;
                for (i2 = 0; i2 < nbDatablocks; i2++) {
                    int i4 = iArr[i2];
                    if (i4 == 1 || i4 == i3 - 1) {
                        Arrays.fill(zArr2, length2, (length2 + i) - 1, i4 > 1);
                        length2 += i - 1;
                    } else {
                        int i5 = i - 1;
                        while (i5 >= 0) {
                            int i6 = length2 + 1;
                            zArr2[length2] = ((1 << i5) & i4) != 0;
                            i5--;
                            length2 = i6;
                        }
                    }
                }
                return zArr2;
            } catch (ReedSolomonException e) {
                throw FormatException.getFormatInstance(e);
            }
        }
        throw FormatException.getFormatInstance();
    }

    private boolean[] extractBits(BitMatrix bitMatrix) {
        int i;
        int i2;
        int i3;
        int i4;
        BitMatrix bitMatrix2 = bitMatrix;
        boolean isCompact = this.ddata.isCompact();
        int nbLayers = this.ddata.getNbLayers();
        int i5 = (isCompact ? 11 : 14) + (nbLayers << 2);
        int[] iArr = new int[i5];
        boolean[] zArr = new boolean[totalBitsInLayer(nbLayers, isCompact)];
        int i6 = 2;
        if (isCompact) {
            for (i = 0; i < iArr.length; i++) {
                iArr[i] = i;
            }
        } else {
            i2 = i5 / 2;
            i = ((i5 + 1) + (((i2 - 1) / 15) * 2)) / 2;
            for (i3 = 0; i3 < i2; i3++) {
                i4 = (i3 / 15) + i3;
                iArr[(i2 - i3) - 1] = (i - i4) - 1;
                iArr[i2 + i3] = (i4 + i) + 1;
            }
        }
        i = 0;
        i2 = 0;
        while (i < nbLayers) {
            boolean z;
            int i7;
            i3 = ((nbLayers - i) << i6) + (isCompact ? 9 : 12);
            i4 = i << 1;
            int i8 = (i5 - 1) - i4;
            int i9 = 0;
            while (i9 < i3) {
                int i10 = i9 << 1;
                int i11 = 0;
                for (i6 = 
/*
Method generation error in method: com.google.zxing.aztec.decoder.Decoder.extractBits(com.google.zxing.common.BitMatrix):boolean[], dex: classes.dex
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r8_2 'i6' int) = (r8_1 'i6' int), (r8_8 'i6' int) binds: {(r8_1 'i6' int)=B:19:0x0062, (r8_8 'i6' int)=B:24:0x00c1} in method: com.google.zxing.aztec.decoder.Decoder.extractBits(com.google.zxing.common.BitMatrix):boolean[], dex: classes.dex
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

    private static int readCode(boolean[] zArr, int i, int i2) {
        int i3 = 0;
        for (int i4 = i; i4 < i + i2; i4++) {
            i3 <<= 1;
            if (zArr[i4]) {
                i3 |= 1;
            }
        }
        return i3;
    }

    private static byte readByte(boolean[] zArr, int i) {
        int length = zArr.length - i;
        if (length >= 8) {
            return (byte) readCode(zArr, i, 8);
        }
        return (byte) (readCode(zArr, i, length) << (8 - length));
    }

    static byte[] convertBoolArrayToByteArray(boolean[] zArr) {
        byte[] bArr = new byte[((zArr.length + 7) / 8)];
        for (int i = 0; i < bArr.length; i++) {
            bArr[i] = readByte(zArr, i << 3);
        }
        return bArr;
    }
}
