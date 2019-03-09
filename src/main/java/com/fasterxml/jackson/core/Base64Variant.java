package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import java.io.Serializable;
import java.util.Arrays;

public final class Base64Variant implements Serializable {
    public static final int BASE64_VALUE_INVALID = -1;
    public static final int BASE64_VALUE_PADDING = -2;
    private static final int INT_SPACE = 32;
    static final char PADDING_CHAR_NONE = '\u0000';
    private static final long serialVersionUID = 1;
    private final transient int[] _asciiToBase64;
    private final transient byte[] _base64ToAsciiB;
    private final transient char[] _base64ToAsciiC;
    private final transient int _maxLineLength;
    final String _name;
    private final transient char _paddingChar;
    private final transient boolean _usesPadding;

    public boolean equals(Object obj) {
        return obj == this;
    }

    public Base64Variant(String str, String str2, boolean z, char c, int i) {
        this._asciiToBase64 = new int[128];
        this._base64ToAsciiC = new char[64];
        this._base64ToAsciiB = new byte[64];
        this._name = str;
        this._usesPadding = z;
        this._paddingChar = c;
        this._maxLineLength = i;
        int length = str2.length();
        if (length == 64) {
            int i2 = 0;
            str2.getChars(0, length, this._base64ToAsciiC, 0);
            Arrays.fill(this._asciiToBase64, -1);
            while (i2 < length) {
                char c2 = this._base64ToAsciiC[i2];
                this._base64ToAsciiB[i2] = (byte) c2;
                this._asciiToBase64[c2] = i2;
                i2++;
            }
            if (z) {
                this._asciiToBase64[c] = -2;
                return;
            }
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Base64Alphabet length must be exactly 64 (was ");
        stringBuilder.append(length);
        stringBuilder.append(")");
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public Base64Variant(Base64Variant base64Variant, String str, int i) {
        this(base64Variant, str, base64Variant._usesPadding, base64Variant._paddingChar, i);
    }

    public Base64Variant(Base64Variant base64Variant, String str, boolean z, char c, int i) {
        this._asciiToBase64 = new int[128];
        this._base64ToAsciiC = new char[64];
        this._base64ToAsciiB = new byte[64];
        this._name = str;
        byte[] bArr = base64Variant._base64ToAsciiB;
        System.arraycopy(bArr, 0, this._base64ToAsciiB, 0, bArr.length);
        char[] cArr = base64Variant._base64ToAsciiC;
        System.arraycopy(cArr, 0, this._base64ToAsciiC, 0, cArr.length);
        int[] iArr = base64Variant._asciiToBase64;
        System.arraycopy(iArr, 0, this._asciiToBase64, 0, iArr.length);
        this._usesPadding = z;
        this._paddingChar = c;
        this._maxLineLength = i;
    }

    /* Access modifiers changed, original: protected */
    public Object readResolve() {
        return Base64Variants.valueOf(this._name);
    }

    public String getName() {
        return this._name;
    }

    public boolean usesPadding() {
        return this._usesPadding;
    }

    public boolean usesPaddingChar(char c) {
        return c == this._paddingChar;
    }

    public boolean usesPaddingChar(int i) {
        return i == this._paddingChar;
    }

    public char getPaddingChar() {
        return this._paddingChar;
    }

    public byte getPaddingByte() {
        return (byte) this._paddingChar;
    }

    public int getMaxLineLength() {
        return this._maxLineLength;
    }

    public int decodeBase64Char(char c) {
        return c <= 127 ? this._asciiToBase64[c] : -1;
    }

    public int decodeBase64Char(int i) {
        return i <= 127 ? this._asciiToBase64[i] : -1;
    }

    public int decodeBase64Byte(byte b) {
        return b < (byte) 0 ? -1 : this._asciiToBase64[b];
    }

    public char encodeBase64BitsAsChar(int i) {
        return this._base64ToAsciiC[i];
    }

    public int encodeBase64Chunk(int i, char[] cArr, int i2) {
        int i3 = i2 + 1;
        cArr[i2] = this._base64ToAsciiC[(i >> 18) & 63];
        i2 = i3 + 1;
        cArr[i3] = this._base64ToAsciiC[(i >> 12) & 63];
        i3 = i2 + 1;
        cArr[i2] = this._base64ToAsciiC[(i >> 6) & 63];
        i2 = i3 + 1;
        cArr[i3] = this._base64ToAsciiC[i & 63];
        return i2;
    }

    public void encodeBase64Chunk(StringBuilder stringBuilder, int i) {
        stringBuilder.append(this._base64ToAsciiC[(i >> 18) & 63]);
        stringBuilder.append(this._base64ToAsciiC[(i >> 12) & 63]);
        stringBuilder.append(this._base64ToAsciiC[(i >> 6) & 63]);
        stringBuilder.append(this._base64ToAsciiC[i & 63]);
    }

    public int encodeBase64Partial(int i, int i2, char[] cArr, int i3) {
        int i4 = i3 + 1;
        cArr[i3] = this._base64ToAsciiC[(i >> 18) & 63];
        i3 = i4 + 1;
        cArr[i4] = this._base64ToAsciiC[(i >> 12) & 63];
        if (this._usesPadding) {
            i4 = i3 + 1;
            cArr[i3] = i2 == 2 ? this._base64ToAsciiC[(i >> 6) & 63] : this._paddingChar;
            i3 = i4 + 1;
            cArr[i4] = this._paddingChar;
            return i3;
        } else if (i2 != 2) {
            return i3;
        } else {
            i2 = i3 + 1;
            cArr[i3] = this._base64ToAsciiC[(i >> 6) & 63];
            return i2;
        }
    }

    public void encodeBase64Partial(StringBuilder stringBuilder, int i, int i2) {
        stringBuilder.append(this._base64ToAsciiC[(i >> 18) & 63]);
        stringBuilder.append(this._base64ToAsciiC[(i >> 12) & 63]);
        if (this._usesPadding) {
            stringBuilder.append(i2 == 2 ? this._base64ToAsciiC[(i >> 6) & 63] : this._paddingChar);
            stringBuilder.append(this._paddingChar);
        } else if (i2 == 2) {
            stringBuilder.append(this._base64ToAsciiC[(i >> 6) & 63]);
        }
    }

    public byte encodeBase64BitsAsByte(int i) {
        return this._base64ToAsciiB[i];
    }

    public int encodeBase64Chunk(int i, byte[] bArr, int i2) {
        int i3 = i2 + 1;
        bArr[i2] = this._base64ToAsciiB[(i >> 18) & 63];
        i2 = i3 + 1;
        bArr[i3] = this._base64ToAsciiB[(i >> 12) & 63];
        i3 = i2 + 1;
        bArr[i2] = this._base64ToAsciiB[(i >> 6) & 63];
        i2 = i3 + 1;
        bArr[i3] = this._base64ToAsciiB[i & 63];
        return i2;
    }

    public int encodeBase64Partial(int i, int i2, byte[] bArr, int i3) {
        int i4 = i3 + 1;
        bArr[i3] = this._base64ToAsciiB[(i >> 18) & 63];
        i3 = i4 + 1;
        bArr[i4] = this._base64ToAsciiB[(i >> 12) & 63];
        if (this._usesPadding) {
            byte b = (byte) this._paddingChar;
            int i5 = i3 + 1;
            bArr[i3] = i2 == 2 ? this._base64ToAsciiB[(i >> 6) & 63] : b;
            i3 = i5 + 1;
            bArr[i5] = b;
            return i3;
        } else if (i2 != 2) {
            return i3;
        } else {
            i2 = i3 + 1;
            bArr[i3] = this._base64ToAsciiB[(i >> 6) & 63];
            return i2;
        }
    }

    public String encode(byte[] bArr) {
        return encode(bArr, false);
    }

    public String encode(byte[] bArr, boolean z) {
        int length = bArr.length;
        StringBuilder stringBuilder = new StringBuilder(((length >> 2) + length) + (length >> 3));
        if (z) {
            stringBuilder.append('\"');
        }
        int maxLineLength = getMaxLineLength() >> 2;
        int i = 0;
        int i2 = length - 3;
        while (i <= i2) {
            int i3 = i + 1;
            int i4 = i3 + 1;
            i3 = i4 + 1;
            encodeBase64Chunk(stringBuilder, (((bArr[i] << 8) | (bArr[i3] & 255)) << 8) | (bArr[i4] & 255));
            maxLineLength--;
            if (maxLineLength <= 0) {
                stringBuilder.append('\\');
                stringBuilder.append('n');
                maxLineLength = getMaxLineLength() >> 2;
            }
            i = i3;
        }
        length -= i;
        if (length > 0) {
            maxLineLength = i + 1;
            i = bArr[i] << 16;
            if (length == 2) {
                i |= (bArr[maxLineLength] & 255) << 8;
            }
            encodeBase64Partial(stringBuilder, i, length);
        }
        if (z) {
            stringBuilder.append('\"');
        }
        return stringBuilder.toString();
    }

    public byte[] decode(String str) throws IllegalArgumentException {
        ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();
        decode(str, byteArrayBuilder);
        return byteArrayBuilder.toByteArray();
    }

    public void decode(String str, ByteArrayBuilder byteArrayBuilder) throws IllegalArgumentException {
        int length = str.length();
        int i = 0;
        while (i < length) {
            int i2;
            char charAt;
            int decodeBase64Char;
            while (true) {
                i2 = i + 1;
                charAt = str.charAt(i);
                if (i2 >= length || charAt > ' ') {
                    decodeBase64Char = decodeBase64Char(charAt);
                } else {
                    i = i2;
                }
            }
            decodeBase64Char = decodeBase64Char(charAt);
            if (decodeBase64Char < 0) {
                _reportInvalidBase64(charAt, 0, null);
            }
            if (i2 >= length) {
                _reportBase64EOF();
            }
            i = i2 + 1;
            char charAt2 = str.charAt(i2);
            int decodeBase64Char2 = decodeBase64Char(charAt2);
            if (decodeBase64Char2 < 0) {
                _reportInvalidBase64(charAt2, 1, null);
            }
            i2 = (decodeBase64Char << 6) | decodeBase64Char2;
            if (i >= length) {
                if (usesPadding()) {
                    _reportBase64EOF();
                } else {
                    byteArrayBuilder.append(i2 >> 4);
                    return;
                }
            }
            decodeBase64Char = i + 1;
            charAt = str.charAt(i);
            decodeBase64Char2 = decodeBase64Char(charAt);
            char charAt3;
            if (decodeBase64Char2 < 0) {
                if (decodeBase64Char2 != -2) {
                    _reportInvalidBase64(charAt, 2, null);
                }
                if (decodeBase64Char >= length) {
                    _reportBase64EOF();
                }
                i = decodeBase64Char + 1;
                charAt3 = str.charAt(decodeBase64Char);
                if (!usesPaddingChar(charAt3)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("expected padding character '");
                    stringBuilder.append(getPaddingChar());
                    stringBuilder.append("'");
                    _reportInvalidBase64(charAt3, 3, stringBuilder.toString());
                }
                byteArrayBuilder.append(i2 >> 4);
            } else {
                i = (i2 << 6) | decodeBase64Char2;
                if (decodeBase64Char >= length) {
                    if (usesPadding()) {
                        _reportBase64EOF();
                    } else {
                        byteArrayBuilder.appendTwoBytes(i >> 2);
                        return;
                    }
                }
                i2 = decodeBase64Char + 1;
                charAt3 = str.charAt(decodeBase64Char);
                decodeBase64Char2 = decodeBase64Char(charAt3);
                if (decodeBase64Char2 < 0) {
                    if (decodeBase64Char2 != -2) {
                        _reportInvalidBase64(charAt3, 3, null);
                    }
                    byteArrayBuilder.appendTwoBytes(i >> 2);
                } else {
                    byteArrayBuilder.appendThreeBytes((i << 6) | decodeBase64Char2);
                }
                i = i2;
            }
        }
    }

    public String toString() {
        return this._name;
    }

    public int hashCode() {
        return this._name.hashCode();
    }

    /* Access modifiers changed, original: protected */
    public void _reportInvalidBase64(char c, int i, String str) throws IllegalArgumentException {
        String stringBuilder;
        StringBuilder stringBuilder2;
        if (c <= ' ') {
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("Illegal white space character (code 0x");
            stringBuilder3.append(Integer.toHexString(c));
            stringBuilder3.append(") as character #");
            stringBuilder3.append(i + 1);
            stringBuilder3.append(" of 4-char base64 unit: can only used between units");
            stringBuilder = stringBuilder3.toString();
        } else if (usesPaddingChar(c)) {
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append("Unexpected padding character ('");
            stringBuilder4.append(getPaddingChar());
            stringBuilder4.append("') as character #");
            stringBuilder4.append(i + 1);
            stringBuilder4.append(" of 4-char base64 unit: padding only legal as 3rd or 4th character");
            stringBuilder = stringBuilder4.toString();
        } else if (!Character.isDefined(c) || Character.isISOControl(c)) {
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Illegal character (code 0x");
            stringBuilder2.append(Integer.toHexString(c));
            stringBuilder2.append(") in base64 content");
            stringBuilder = stringBuilder2.toString();
        } else {
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Illegal character '");
            stringBuilder2.append(c);
            stringBuilder2.append("' (code 0x");
            stringBuilder2.append(Integer.toHexString(c));
            stringBuilder2.append(") in base64 content");
            stringBuilder = stringBuilder2.toString();
        }
        if (str != null) {
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append(stringBuilder);
            stringBuilder2.append(": ");
            stringBuilder2.append(str);
            stringBuilder = stringBuilder2.toString();
        }
        throw new IllegalArgumentException(stringBuilder);
    }

    /* Access modifiers changed, original: protected */
    public void _reportBase64EOF() throws IllegalArgumentException {
        throw new IllegalArgumentException("Unexpected end-of-String in base64 content");
    }
}
