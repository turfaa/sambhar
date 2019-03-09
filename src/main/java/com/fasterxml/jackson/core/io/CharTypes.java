package com.fasterxml.jackson.core.io;

import java.util.Arrays;

public final class CharTypes {
    private static final byte[] HB;
    private static final char[] HC = "0123456789ABCDEF".toCharArray();
    private static final int[] sHexValues = new int[128];
    private static final int[] sInputCodes;
    private static final int[] sInputCodesComment;
    private static final int[] sInputCodesJsNames;
    private static final int[] sInputCodesUTF8;
    private static final int[] sInputCodesUtf8JsNames;
    private static final int[] sInputCodesWS;
    private static final int[] sOutputEscapes128;

    static {
        int i;
        int i2;
        int i3;
        int length = HC.length;
        HB = new byte[length];
        int i4 = 0;
        for (i = 0; i < length; i++) {
            HB[i] = (byte) HC[i];
        }
        int[] iArr = new int[256];
        for (i2 = 0; i2 < 32; i2++) {
            iArr[i2] = -1;
        }
        iArr[34] = 1;
        iArr[92] = 1;
        sInputCodes = iArr;
        iArr = new int[sInputCodes.length];
        System.arraycopy(sInputCodes, 0, iArr, 0, iArr.length);
        for (i3 = 128; i3 < 256; i3++) {
            int i5 = (i3 & 224) == 192 ? 2 : (i3 & 240) == 224 ? 3 : (i3 & 248) == 240 ? 4 : -1;
            iArr[i3] = i5;
        }
        sInputCodesUTF8 = iArr;
        iArr = new int[256];
        Arrays.fill(iArr, -1);
        for (i3 = 33; i3 < 256; i3++) {
            if (Character.isJavaIdentifierPart((char) i3)) {
                iArr[i3] = 0;
            }
        }
        iArr[64] = 0;
        iArr[35] = 0;
        iArr[42] = 0;
        iArr[45] = 0;
        iArr[43] = 0;
        sInputCodesJsNames = iArr;
        iArr = new int[256];
        System.arraycopy(sInputCodesJsNames, 0, iArr, 0, iArr.length);
        Arrays.fill(iArr, 128, 128, 0);
        sInputCodesUtf8JsNames = iArr;
        iArr = new int[256];
        System.arraycopy(sInputCodesUTF8, 128, iArr, 128, 128);
        Arrays.fill(iArr, 0, 32, -1);
        iArr[9] = 0;
        iArr[10] = 10;
        iArr[13] = 13;
        iArr[42] = 42;
        sInputCodesComment = iArr;
        int[] iArr2 = new int[256];
        System.arraycopy(sInputCodesUTF8, 128, iArr2, 128, 128);
        Arrays.fill(iArr2, 0, 32, -1);
        iArr2[32] = 1;
        iArr2[9] = 1;
        iArr2[10] = 10;
        iArr2[13] = 13;
        iArr2[47] = 47;
        iArr2[35] = 35;
        sInputCodesWS = iArr2;
        iArr2 = new int[128];
        for (i = 0; i < 32; i++) {
            iArr2[i] = -1;
        }
        iArr2[34] = 34;
        iArr2[92] = 92;
        iArr2[8] = 98;
        iArr2[9] = 116;
        iArr2[12] = 102;
        iArr2[10] = 110;
        iArr2[13] = 114;
        sOutputEscapes128 = iArr2;
        Arrays.fill(sHexValues, -1);
        for (length = 0; length < 10; length++) {
            sHexValues[length + 48] = length;
        }
        while (i4 < 6) {
            i2 = i4 + 10;
            sHexValues[i4 + 97] = i2;
            sHexValues[i4 + 65] = i2;
            i4++;
        }
    }

    public static int[] getInputCodeLatin1() {
        return sInputCodes;
    }

    public static int[] getInputCodeUtf8() {
        return sInputCodesUTF8;
    }

    public static int[] getInputCodeLatin1JsNames() {
        return sInputCodesJsNames;
    }

    public static int[] getInputCodeUtf8JsNames() {
        return sInputCodesUtf8JsNames;
    }

    public static int[] getInputCodeComment() {
        return sInputCodesComment;
    }

    public static int[] getInputCodeWS() {
        return sInputCodesWS;
    }

    public static int[] get7BitOutputEscapes() {
        return sOutputEscapes128;
    }

    public static int charToHex(int i) {
        return i > 127 ? -1 : sHexValues[i];
    }

    public static void appendQuoted(StringBuilder stringBuilder, String str) {
        int[] iArr = sOutputEscapes128;
        char length = iArr.length;
        int length2 = str.length();
        for (int i = 0; i < length2; i++) {
            char charAt = str.charAt(i);
            if (charAt >= length || iArr[charAt] == 0) {
                stringBuilder.append(charAt);
            } else {
                stringBuilder.append('\\');
                int i2 = iArr[charAt];
                if (i2 < 0) {
                    stringBuilder.append('u');
                    stringBuilder.append('0');
                    stringBuilder.append('0');
                    stringBuilder.append(HC[charAt >> 4]);
                    stringBuilder.append(HC[charAt & 15]);
                } else {
                    stringBuilder.append((char) i2);
                }
            }
        }
    }

    public static char[] copyHexChars() {
        return (char[]) HC.clone();
    }

    public static byte[] copyHexBytes() {
        return (byte[]) HB.clone();
    }
}
