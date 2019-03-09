package com.fasterxml.jackson.core.json;

import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory.Feature;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.format.InputAccessor;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.MergedStream;
import com.fasterxml.jackson.core.io.UTF32Reader;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.twitter.sdk.android.core.internal.TwitterApiConstants.Errors;
import java.io.ByteArrayInputStream;
import java.io.CharConversionException;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public final class ByteSourceJsonBootstrapper {
    static final byte UTF8_BOM_1 = (byte) -17;
    static final byte UTF8_BOM_2 = (byte) -69;
    static final byte UTF8_BOM_3 = (byte) -65;
    private boolean _bigEndian = true;
    private final boolean _bufferRecyclable;
    private int _bytesPerChar;
    private final IOContext _context;
    private final InputStream _in;
    private final byte[] _inputBuffer;
    private int _inputEnd;
    private int _inputPtr;

    public ByteSourceJsonBootstrapper(IOContext iOContext, InputStream inputStream) {
        this._context = iOContext;
        this._in = inputStream;
        this._inputBuffer = iOContext.allocReadIOBuffer();
        this._inputPtr = 0;
        this._inputEnd = 0;
        this._bufferRecyclable = true;
    }

    public ByteSourceJsonBootstrapper(IOContext iOContext, byte[] bArr, int i, int i2) {
        this._context = iOContext;
        this._in = null;
        this._inputBuffer = bArr;
        this._inputPtr = i;
        this._inputEnd = i + i2;
        this._bufferRecyclable = false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0073  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0070  */
    /* JADX WARNING: Missing block: B:7:0x0049, code skipped:
            if (checkUTF16(r1 >>> 16) != false) goto L_0x006e;
     */
    /* JADX WARNING: Missing block: B:11:0x006a, code skipped:
            if (checkUTF16(((r7._inputBuffer[r7._inputPtr] & 255) << 8) | (r7._inputBuffer[r7._inputPtr + 1] & 255)) != false) goto L_0x006e;
     */
    public com.fasterxml.jackson.core.JsonEncoding detectEncoding() throws java.io.IOException {
        /*
        r7 = this;
        r0 = 4;
        r1 = r7.ensureLoaded(r0);
        r2 = 2;
        r3 = 1;
        r4 = 0;
        if (r1 == 0) goto L_0x004c;
    L_0x000a:
        r1 = r7._inputBuffer;
        r5 = r7._inputPtr;
        r1 = r1[r5];
        r1 = r1 << 24;
        r5 = r7._inputBuffer;
        r6 = r7._inputPtr;
        r6 = r6 + r3;
        r5 = r5[r6];
        r5 = r5 & 255;
        r5 = r5 << 16;
        r1 = r1 | r5;
        r5 = r7._inputBuffer;
        r6 = r7._inputPtr;
        r6 = r6 + r2;
        r2 = r5[r6];
        r2 = r2 & 255;
        r2 = r2 << 8;
        r1 = r1 | r2;
        r2 = r7._inputBuffer;
        r5 = r7._inputPtr;
        r5 = r5 + 3;
        r2 = r2[r5];
        r2 = r2 & 255;
        r1 = r1 | r2;
        r2 = r7.handleBOM(r1);
        if (r2 == 0) goto L_0x003c;
    L_0x003b:
        goto L_0x006e;
    L_0x003c:
        r2 = r7.checkUTF32(r1);
        if (r2 == 0) goto L_0x0043;
    L_0x0042:
        goto L_0x006e;
    L_0x0043:
        r1 = r1 >>> 16;
        r1 = r7.checkUTF16(r1);
        if (r1 == 0) goto L_0x006d;
    L_0x004b:
        goto L_0x006e;
    L_0x004c:
        r1 = r7.ensureLoaded(r2);
        if (r1 == 0) goto L_0x006d;
    L_0x0052:
        r1 = r7._inputBuffer;
        r2 = r7._inputPtr;
        r1 = r1[r2];
        r1 = r1 & 255;
        r1 = r1 << 8;
        r2 = r7._inputBuffer;
        r5 = r7._inputPtr;
        r5 = r5 + r3;
        r2 = r2[r5];
        r2 = r2 & 255;
        r1 = r1 | r2;
        r1 = r7.checkUTF16(r1);
        if (r1 == 0) goto L_0x006d;
    L_0x006c:
        goto L_0x006e;
    L_0x006d:
        r3 = 0;
    L_0x006e:
        if (r3 != 0) goto L_0x0073;
    L_0x0070:
        r0 = com.fasterxml.jackson.core.JsonEncoding.UTF8;
        goto L_0x0098;
    L_0x0073:
        r1 = r7._bytesPerChar;
        if (r1 == r0) goto L_0x008f;
    L_0x0077:
        switch(r1) {
            case 1: goto L_0x008c;
            case 2: goto L_0x0082;
            default: goto L_0x007a;
        };
    L_0x007a:
        r0 = new java.lang.RuntimeException;
        r1 = "Internal error";
        r0.<init>(r1);
        throw r0;
    L_0x0082:
        r0 = r7._bigEndian;
        if (r0 == 0) goto L_0x0089;
    L_0x0086:
        r0 = com.fasterxml.jackson.core.JsonEncoding.UTF16_BE;
        goto L_0x0098;
    L_0x0089:
        r0 = com.fasterxml.jackson.core.JsonEncoding.UTF16_LE;
        goto L_0x0098;
    L_0x008c:
        r0 = com.fasterxml.jackson.core.JsonEncoding.UTF8;
        goto L_0x0098;
    L_0x008f:
        r0 = r7._bigEndian;
        if (r0 == 0) goto L_0x0096;
    L_0x0093:
        r0 = com.fasterxml.jackson.core.JsonEncoding.UTF32_BE;
        goto L_0x0098;
    L_0x0096:
        r0 = com.fasterxml.jackson.core.JsonEncoding.UTF32_LE;
    L_0x0098:
        r1 = r7._context;
        r1.setEncoding(r0);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.ByteSourceJsonBootstrapper.detectEncoding():com.fasterxml.jackson.core.JsonEncoding");
    }

    public static int skipUTF8BOM(DataInput dataInput) throws IOException {
        int readUnsignedByte = dataInput.readUnsignedByte();
        if (readUnsignedByte != Errors.GUEST_AUTH_ERROR_CODE) {
            return readUnsignedByte;
        }
        readUnsignedByte = dataInput.readUnsignedByte();
        StringBuilder stringBuilder;
        if (readUnsignedByte == 187) {
            readUnsignedByte = dataInput.readUnsignedByte();
            if (readUnsignedByte == 191) {
                return dataInput.readUnsignedByte();
            }
            stringBuilder = new StringBuilder();
            stringBuilder.append("Unexpected byte 0x");
            stringBuilder.append(Integer.toHexString(readUnsignedByte));
            stringBuilder.append(" following 0xEF 0xBB; should get 0xBF as part of UTF-8 BOM");
            throw new IOException(stringBuilder.toString());
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected byte 0x");
        stringBuilder.append(Integer.toHexString(readUnsignedByte));
        stringBuilder.append(" following 0xEF; should get 0xBB as part of UTF-8 BOM");
        throw new IOException(stringBuilder.toString());
    }

    public Reader constructReader() throws IOException {
        JsonEncoding encoding = this._context.getEncoding();
        int bits = encoding.bits();
        if (bits == 8 || bits == 16) {
            InputStream inputStream = this._in;
            if (inputStream == null) {
                inputStream = new ByteArrayInputStream(this._inputBuffer, this._inputPtr, this._inputEnd);
            } else if (this._inputPtr < this._inputEnd) {
                inputStream = new MergedStream(this._context, inputStream, this._inputBuffer, this._inputPtr, this._inputEnd);
            }
            return new InputStreamReader(inputStream, encoding.getJavaName());
        } else if (bits == 32) {
            return new UTF32Reader(this._context, this._in, this._inputBuffer, this._inputPtr, this._inputEnd, this._context.getEncoding().isBigEndian());
        } else {
            throw new RuntimeException("Internal error");
        }
    }

    public JsonParser constructParser(int i, ObjectCodec objectCodec, ByteQuadsCanonicalizer byteQuadsCanonicalizer, CharsToNameCanonicalizer charsToNameCanonicalizer, int i2) throws IOException {
        int i3 = i2;
        if (detectEncoding() == JsonEncoding.UTF8 && Feature.CANONICALIZE_FIELD_NAMES.enabledIn(i3)) {
            return new UTF8StreamJsonParser(this._context, i, this._in, objectCodec, byteQuadsCanonicalizer.makeChild(i3), this._inputBuffer, this._inputPtr, this._inputEnd, this._bufferRecyclable);
        }
        return new ReaderBasedJsonParser(this._context, i, constructReader(), objectCodec, charsToNameCanonicalizer.makeChild(i2));
    }

    public static MatchStrength hasJSONFormat(InputAccessor inputAccessor) throws IOException {
        if (!inputAccessor.hasMoreBytes()) {
            return MatchStrength.INCONCLUSIVE;
        }
        byte nextByte = inputAccessor.nextByte();
        if (nextByte == UTF8_BOM_1) {
            if (!inputAccessor.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (inputAccessor.nextByte() != UTF8_BOM_2) {
                return MatchStrength.NO_MATCH;
            }
            if (!inputAccessor.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (inputAccessor.nextByte() != UTF8_BOM_3) {
                return MatchStrength.NO_MATCH;
            }
            if (!inputAccessor.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            nextByte = inputAccessor.nextByte();
        }
        int skipSpace = skipSpace(inputAccessor, nextByte);
        if (skipSpace < 0) {
            return MatchStrength.INCONCLUSIVE;
        }
        int skipSpace2;
        if (skipSpace == 123) {
            skipSpace2 = skipSpace(inputAccessor);
            if (skipSpace2 < 0) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (skipSpace2 == 34 || skipSpace2 == 125) {
                return MatchStrength.SOLID_MATCH;
            }
            return MatchStrength.NO_MATCH;
        } else if (skipSpace == 91) {
            skipSpace2 = skipSpace(inputAccessor);
            if (skipSpace2 < 0) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (skipSpace2 == 93 || skipSpace2 == 91) {
                return MatchStrength.SOLID_MATCH;
            }
            return MatchStrength.SOLID_MATCH;
        } else {
            MatchStrength matchStrength = MatchStrength.WEAK_MATCH;
            if (skipSpace == 34) {
                return matchStrength;
            }
            if (skipSpace <= 57 && skipSpace >= 48) {
                return matchStrength;
            }
            if (skipSpace == 45) {
                skipSpace2 = skipSpace(inputAccessor);
                if (skipSpace2 < 0) {
                    return MatchStrength.INCONCLUSIVE;
                }
                if (skipSpace2 > 57 || skipSpace2 < 48) {
                    matchStrength = MatchStrength.NO_MATCH;
                }
                return matchStrength;
            } else if (skipSpace == 110) {
                return tryMatch(inputAccessor, "ull", matchStrength);
            } else {
                if (skipSpace == 116) {
                    return tryMatch(inputAccessor, "rue", matchStrength);
                }
                if (skipSpace == 102) {
                    return tryMatch(inputAccessor, "alse", matchStrength);
                }
                return MatchStrength.NO_MATCH;
            }
        }
    }

    private static MatchStrength tryMatch(InputAccessor inputAccessor, String str, MatchStrength matchStrength) throws IOException {
        int length = str.length();
        for (int i = 0; i < length; i++) {
            if (!inputAccessor.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (inputAccessor.nextByte() != str.charAt(i)) {
                return MatchStrength.NO_MATCH;
            }
        }
        return matchStrength;
    }

    private static int skipSpace(InputAccessor inputAccessor) throws IOException {
        if (inputAccessor.hasMoreBytes()) {
            return skipSpace(inputAccessor, inputAccessor.nextByte());
        }
        return -1;
    }

    private static int skipSpace(InputAccessor inputAccessor, byte b) throws IOException {
        while (true) {
            int i &= 255;
            if (i != 32 && i != 13 && i != 10 && i != 9) {
                return i;
            }
            if (!inputAccessor.hasMoreBytes()) {
                return -1;
            }
            i = inputAccessor.nextByte();
        }
    }

    private boolean handleBOM(int i) throws IOException {
        if (i == -16842752) {
            reportWeirdUCS4("3412");
        } else if (i == -131072) {
            this._inputPtr += 4;
            this._bytesPerChar = 4;
            this._bigEndian = false;
            return true;
        } else if (i == 65279) {
            this._bigEndian = true;
            this._inputPtr += 4;
            this._bytesPerChar = 4;
            return true;
        } else if (i == 65534) {
            reportWeirdUCS4("2143");
        }
        int i2 = i >>> 16;
        if (i2 == 65279) {
            this._inputPtr += 2;
            this._bytesPerChar = 2;
            this._bigEndian = true;
            return true;
        } else if (i2 == 65534) {
            this._inputPtr += 2;
            this._bytesPerChar = 2;
            this._bigEndian = false;
            return true;
        } else if ((i >>> 8) != 15711167) {
            return false;
        } else {
            this._inputPtr += 3;
            this._bytesPerChar = 1;
            this._bigEndian = true;
            return true;
        }
    }

    private boolean checkUTF32(int i) throws IOException {
        if ((i >> 8) == 0) {
            this._bigEndian = true;
        } else if ((ViewCompat.MEASURED_SIZE_MASK & i) == 0) {
            this._bigEndian = false;
        } else if ((-16711681 & i) == 0) {
            reportWeirdUCS4("3412");
        } else if ((i & -65281) != 0) {
            return false;
        } else {
            reportWeirdUCS4("2143");
        }
        this._bytesPerChar = 4;
        return true;
    }

    private boolean checkUTF16(int i) {
        if ((MotionEventCompat.ACTION_POINTER_INDEX_MASK & i) == 0) {
            this._bigEndian = true;
        } else if ((i & 255) != 0) {
            return false;
        } else {
            this._bigEndian = false;
        }
        this._bytesPerChar = 2;
        return true;
    }

    private void reportWeirdUCS4(String str) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unsupported UCS-4 endianness (");
        stringBuilder.append(str);
        stringBuilder.append(") detected");
        throw new CharConversionException(stringBuilder.toString());
    }

    /* Access modifiers changed, original: protected */
    public boolean ensureLoaded(int i) throws IOException {
        int i2 = this._inputEnd - this._inputPtr;
        while (i2 < i) {
            int i3;
            if (this._in == null) {
                i3 = -1;
            } else {
                i3 = this._in.read(this._inputBuffer, this._inputEnd, this._inputBuffer.length - this._inputEnd);
            }
            if (i3 < 1) {
                return false;
            }
            this._inputEnd += i3;
            i2 += i3;
        }
        return true;
    }
}
