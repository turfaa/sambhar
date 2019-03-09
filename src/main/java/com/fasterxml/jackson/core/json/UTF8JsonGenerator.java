package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.base.GeneratorBase;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.NumberOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

public class UTF8JsonGenerator extends JsonGeneratorImpl {
    private static final byte BYTE_0 = (byte) 48;
    private static final byte BYTE_BACKSLASH = (byte) 92;
    private static final byte BYTE_COLON = (byte) 58;
    private static final byte BYTE_COMMA = (byte) 44;
    private static final byte BYTE_LBRACKET = (byte) 91;
    private static final byte BYTE_LCURLY = (byte) 123;
    private static final byte BYTE_RBRACKET = (byte) 93;
    private static final byte BYTE_RCURLY = (byte) 125;
    private static final byte BYTE_u = (byte) 117;
    private static final byte[] FALSE_BYTES = new byte[]{(byte) 102, (byte) 97, (byte) 108, (byte) 115, (byte) 101};
    private static final byte[] HEX_CHARS = CharTypes.copyHexBytes();
    private static final int MAX_BYTES_TO_BUFFER = 512;
    private static final byte[] NULL_BYTES = new byte[]{(byte) 110, BYTE_u, (byte) 108, (byte) 108};
    private static final byte[] TRUE_BYTES = new byte[]{(byte) 116, (byte) 114, BYTE_u, (byte) 101};
    protected boolean _bufferRecyclable;
    protected char[] _charBuffer;
    protected final int _charBufferLength;
    protected byte[] _entityBuffer;
    protected byte[] _outputBuffer;
    protected final int _outputEnd;
    protected final int _outputMaxContiguous;
    protected final OutputStream _outputStream;
    protected int _outputTail;
    protected byte _quoteChar = (byte) 34;

    public UTF8JsonGenerator(IOContext iOContext, int i, ObjectCodec objectCodec, OutputStream outputStream) {
        super(iOContext, i, objectCodec);
        this._outputStream = outputStream;
        this._bufferRecyclable = true;
        this._outputBuffer = iOContext.allocWriteEncodingBuffer();
        this._outputEnd = this._outputBuffer.length;
        this._outputMaxContiguous = this._outputEnd >> 3;
        this._charBuffer = iOContext.allocConcatBuffer();
        this._charBufferLength = this._charBuffer.length;
        if (isEnabled(Feature.ESCAPE_NON_ASCII)) {
            setHighestNonEscapedChar(127);
        }
    }

    public UTF8JsonGenerator(IOContext iOContext, int i, ObjectCodec objectCodec, OutputStream outputStream, byte[] bArr, int i2, boolean z) {
        super(iOContext, i, objectCodec);
        this._outputStream = outputStream;
        this._bufferRecyclable = z;
        this._outputTail = i2;
        this._outputBuffer = bArr;
        this._outputEnd = this._outputBuffer.length;
        this._outputMaxContiguous = this._outputEnd >> 3;
        this._charBuffer = iOContext.allocConcatBuffer();
        this._charBufferLength = this._charBuffer.length;
    }

    public Object getOutputTarget() {
        return this._outputStream;
    }

    public int getOutputBuffered() {
        return this._outputTail;
    }

    public void writeFieldName(String str) throws IOException {
        if (this._cfgPrettyPrinter != null) {
            _writePPFieldName(str);
            return;
        }
        int writeFieldName = this._writeContext.writeFieldName(str);
        if (writeFieldName == 4) {
            _reportError("Can not write a field name, expecting a value");
        }
        if (writeFieldName == 1) {
            if (this._outputTail >= this._outputEnd) {
                _flushBuffer();
            }
            byte[] bArr = this._outputBuffer;
            int i = this._outputTail;
            this._outputTail = i + 1;
            bArr[i] = BYTE_COMMA;
        }
        if (this._cfgUnqNames) {
            _writeStringSegments(str, false);
            return;
        }
        writeFieldName = str.length();
        if (writeFieldName > this._charBufferLength) {
            _writeStringSegments(str, true);
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr2 = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        bArr2[i2] = this._quoteChar;
        if (writeFieldName <= this._outputMaxContiguous) {
            if (this._outputTail + writeFieldName > this._outputEnd) {
                _flushBuffer();
            }
            _writeStringSegment(str, 0, writeFieldName);
        } else {
            _writeStringSegments(str, 0, writeFieldName);
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr3 = this._outputBuffer;
        writeFieldName = this._outputTail;
        this._outputTail = writeFieldName + 1;
        bArr3[writeFieldName] = this._quoteChar;
    }

    public void writeFieldName(SerializableString serializableString) throws IOException {
        if (this._cfgPrettyPrinter != null) {
            _writePPFieldName(serializableString);
            return;
        }
        byte[] bArr;
        int i;
        int writeFieldName = this._writeContext.writeFieldName(serializableString.getValue());
        if (writeFieldName == 4) {
            _reportError("Can not write a field name, expecting a value");
        }
        if (writeFieldName == 1) {
            if (this._outputTail >= this._outputEnd) {
                _flushBuffer();
            }
            bArr = this._outputBuffer;
            i = this._outputTail;
            this._outputTail = i + 1;
            bArr[i] = BYTE_COMMA;
        }
        if (this._cfgUnqNames) {
            _writeUnq(serializableString);
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        bArr = this._outputBuffer;
        i = this._outputTail;
        this._outputTail = i + 1;
        bArr[i] = this._quoteChar;
        writeFieldName = serializableString.appendQuotedUTF8(this._outputBuffer, this._outputTail);
        if (writeFieldName < 0) {
            _writeBytes(serializableString.asQuotedUTF8());
        } else {
            this._outputTail += writeFieldName;
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr2 = this._outputBuffer;
        writeFieldName = this._outputTail;
        this._outputTail = writeFieldName + 1;
        bArr2[writeFieldName] = this._quoteChar;
    }

    private final void _writeUnq(SerializableString serializableString) throws IOException {
        int appendQuotedUTF8 = serializableString.appendQuotedUTF8(this._outputBuffer, this._outputTail);
        if (appendQuotedUTF8 < 0) {
            _writeBytes(serializableString.asQuotedUTF8());
        } else {
            this._outputTail += appendQuotedUTF8;
        }
    }

    public final void writeStartArray() throws IOException {
        _verifyValueWrite("start an array");
        this._writeContext = this._writeContext.createChildArrayContext();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartArray(this);
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        bArr[i] = BYTE_LBRACKET;
    }

    public final void writeEndArray() throws IOException {
        if (!this._writeContext.inArray()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Current context not Array but ");
            stringBuilder.append(this._writeContext.typeDesc());
            _reportError(stringBuilder.toString());
        }
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeEndArray(this, this._writeContext.getEntryCount());
        } else {
            if (this._outputTail >= this._outputEnd) {
                _flushBuffer();
            }
            byte[] bArr = this._outputBuffer;
            int i = this._outputTail;
            this._outputTail = i + 1;
            bArr[i] = BYTE_RBRACKET;
        }
        this._writeContext = this._writeContext.clearAndGetParent();
    }

    public final void writeStartObject() throws IOException {
        _verifyValueWrite("start an object");
        this._writeContext = this._writeContext.createChildObjectContext();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartObject(this);
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        bArr[i] = BYTE_LCURLY;
    }

    public void writeStartObject(Object obj) throws IOException {
        _verifyValueWrite("start an object");
        JsonWriteContext createChildObjectContext = this._writeContext.createChildObjectContext();
        this._writeContext = createChildObjectContext;
        if (obj != null) {
            createChildObjectContext.setCurrentValue(obj);
        }
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartObject(this);
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        bArr[i] = BYTE_LCURLY;
    }

    public final void writeEndObject() throws IOException {
        if (!this._writeContext.inObject()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Current context not Object but ");
            stringBuilder.append(this._writeContext.typeDesc());
            _reportError(stringBuilder.toString());
        }
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeEndObject(this, this._writeContext.getEntryCount());
        } else {
            if (this._outputTail >= this._outputEnd) {
                _flushBuffer();
            }
            byte[] bArr = this._outputBuffer;
            int i = this._outputTail;
            this._outputTail = i + 1;
            bArr[i] = BYTE_RCURLY;
        }
        this._writeContext = this._writeContext.clearAndGetParent();
    }

    /* Access modifiers changed, original: protected|final */
    public final void _writePPFieldName(String str) throws IOException {
        int writeFieldName = this._writeContext.writeFieldName(str);
        if (writeFieldName == 4) {
            _reportError("Can not write a field name, expecting a value");
        }
        if (writeFieldName == 1) {
            this._cfgPrettyPrinter.writeObjectEntrySeparator(this);
        } else {
            this._cfgPrettyPrinter.beforeObjectEntries(this);
        }
        if (this._cfgUnqNames) {
            _writeStringSegments(str, false);
            return;
        }
        writeFieldName = str.length();
        if (writeFieldName > this._charBufferLength) {
            _writeStringSegments(str, true);
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        bArr[i] = this._quoteChar;
        str.getChars(0, writeFieldName, this._charBuffer, 0);
        if (writeFieldName <= this._outputMaxContiguous) {
            if (this._outputTail + writeFieldName > this._outputEnd) {
                _flushBuffer();
            }
            _writeStringSegment(this._charBuffer, 0, writeFieldName);
        } else {
            _writeStringSegments(this._charBuffer, 0, writeFieldName);
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr2 = this._outputBuffer;
        writeFieldName = this._outputTail;
        this._outputTail = writeFieldName + 1;
        bArr2[writeFieldName] = this._quoteChar;
    }

    /* Access modifiers changed, original: protected|final */
    public final void _writePPFieldName(SerializableString serializableString) throws IOException {
        int writeFieldName = this._writeContext.writeFieldName(serializableString.getValue());
        if (writeFieldName == 4) {
            _reportError("Can not write a field name, expecting a value");
        }
        if (writeFieldName == 1) {
            this._cfgPrettyPrinter.writeObjectEntrySeparator(this);
        } else {
            this._cfgPrettyPrinter.beforeObjectEntries(this);
        }
        writeFieldName = this._cfgUnqNames ^ 1;
        if (writeFieldName != 0) {
            if (this._outputTail >= this._outputEnd) {
                _flushBuffer();
            }
            byte[] bArr = this._outputBuffer;
            int i = this._outputTail;
            this._outputTail = i + 1;
            bArr[i] = this._quoteChar;
        }
        _writeBytes(serializableString.asQuotedUTF8());
        if (writeFieldName != 0) {
            if (this._outputTail >= this._outputEnd) {
                _flushBuffer();
            }
            byte[] bArr2 = this._outputBuffer;
            writeFieldName = this._outputTail;
            this._outputTail = writeFieldName + 1;
            bArr2[writeFieldName] = this._quoteChar;
        }
    }

    public void writeString(String str) throws IOException {
        _verifyValueWrite("write a string");
        if (str == null) {
            _writeNull();
            return;
        }
        int length = str.length();
        if (length > this._outputMaxContiguous) {
            _writeStringSegments(str, true);
            return;
        }
        if (this._outputTail + length >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        bArr[i] = this._quoteChar;
        _writeStringSegment(str, 0, length);
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr2 = this._outputBuffer;
        length = this._outputTail;
        this._outputTail = length + 1;
        bArr2[length] = this._quoteChar;
    }

    public void writeString(char[] cArr, int i, int i2) throws IOException {
        _verifyValueWrite("write a string");
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i3 = this._outputTail;
        this._outputTail = i3 + 1;
        bArr[i3] = this._quoteChar;
        if (i2 <= this._outputMaxContiguous) {
            if (this._outputTail + i2 > this._outputEnd) {
                _flushBuffer();
            }
            _writeStringSegment(cArr, i, i2);
        } else {
            _writeStringSegments(cArr, i, i2);
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr2 = this._outputBuffer;
        i = this._outputTail;
        this._outputTail = i + 1;
        bArr2[i] = this._quoteChar;
    }

    public final void writeString(SerializableString serializableString) throws IOException {
        _verifyValueWrite("write a string");
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        bArr[i] = this._quoteChar;
        int appendQuotedUTF8 = serializableString.appendQuotedUTF8(this._outputBuffer, this._outputTail);
        if (appendQuotedUTF8 < 0) {
            _writeBytes(serializableString.asQuotedUTF8());
        } else {
            this._outputTail += appendQuotedUTF8;
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr2 = this._outputBuffer;
        appendQuotedUTF8 = this._outputTail;
        this._outputTail = appendQuotedUTF8 + 1;
        bArr2[appendQuotedUTF8] = this._quoteChar;
    }

    public void writeRawUTF8String(byte[] bArr, int i, int i2) throws IOException {
        _verifyValueWrite("write a string");
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr2 = this._outputBuffer;
        int i3 = this._outputTail;
        this._outputTail = i3 + 1;
        bArr2[i3] = this._quoteChar;
        _writeBytes(bArr, i, i2);
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        bArr = this._outputBuffer;
        i = this._outputTail;
        this._outputTail = i + 1;
        bArr[i] = this._quoteChar;
    }

    public void writeUTF8String(byte[] bArr, int i, int i2) throws IOException {
        _verifyValueWrite("write a string");
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr2 = this._outputBuffer;
        int i3 = this._outputTail;
        this._outputTail = i3 + 1;
        bArr2[i3] = this._quoteChar;
        if (i2 <= this._outputMaxContiguous) {
            _writeUTF8Segment(bArr, i, i2);
        } else {
            _writeUTF8Segments(bArr, i, i2);
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        bArr = this._outputBuffer;
        i = this._outputTail;
        this._outputTail = i + 1;
        bArr[i] = this._quoteChar;
    }

    public void writeRaw(String str) throws IOException {
        int length = str.length();
        char[] cArr = this._charBuffer;
        if (length <= cArr.length) {
            str.getChars(0, length, cArr, 0);
            writeRaw(cArr, 0, length);
            return;
        }
        writeRaw(str, 0, length);
    }

    public void writeRaw(String str, int i, int i2) throws IOException {
        char[] cArr = this._charBuffer;
        int length = cArr.length;
        if (i2 <= length) {
            str.getChars(i, i + i2, cArr, 0);
            writeRaw(cArr, 0, i2);
            return;
        }
        length = Math.min(length, (this._outputEnd >> 2) + (this._outputEnd >> 4));
        int i3 = length * 3;
        while (i2 > 0) {
            int min = Math.min(length, i2);
            str.getChars(i, i + min, cArr, 0);
            if (this._outputTail + i3 > this._outputEnd) {
                _flushBuffer();
            }
            if (i2 > 0) {
                char c = cArr[min - 1];
                if (c >= 55296 && c <= 56319) {
                    min--;
                }
            }
            _writeRawSegment(cArr, 0, min);
            i += min;
            i2 -= min;
        }
    }

    public void writeRaw(SerializableString serializableString) throws IOException {
        byte[] asUnquotedUTF8 = serializableString.asUnquotedUTF8();
        if (asUnquotedUTF8.length > 0) {
            _writeBytes(asUnquotedUTF8);
        }
    }

    public void writeRawValue(SerializableString serializableString) throws IOException {
        _verifyValueWrite("write a raw (unencoded) value");
        byte[] asUnquotedUTF8 = serializableString.asUnquotedUTF8();
        if (asUnquotedUTF8.length > 0) {
            _writeBytes(asUnquotedUTF8);
        }
    }

    /* JADX WARNING: Missing block: B:11:0x001e, code skipped:
            r0 = r6 + 1;
            r6 = r5[r6];
     */
    /* JADX WARNING: Missing block: B:12:0x0024, code skipped:
            if (r6 >= 2048) goto L_0x0046;
     */
    /* JADX WARNING: Missing block: B:13:0x0026, code skipped:
            r1 = r4._outputBuffer;
            r2 = r4._outputTail;
            r4._outputTail = r2 + 1;
            r1[r2] = (byte) ((r6 >> 6) | 192);
            r1 = r4._outputBuffer;
            r2 = r4._outputTail;
            r4._outputTail = r2 + 1;
            r1[r2] = (byte) ((r6 & 63) | 128);
            r6 = r0;
     */
    /* JADX WARNING: Missing block: B:14:0x0046, code skipped:
            r6 = _outputRawMultiByteChar(r6, r5, r0, r7);
     */
    public final void writeRaw(char[] r5, int r6, int r7) throws java.io.IOException {
        /*
        r4 = this;
        r0 = r7 + r7;
        r0 = r0 + r7;
        r1 = r4._outputTail;
        r1 = r1 + r0;
        r2 = r4._outputEnd;
        if (r1 <= r2) goto L_0x0015;
    L_0x000a:
        r1 = r4._outputEnd;
        if (r1 >= r0) goto L_0x0012;
    L_0x000e:
        r4._writeSegmentedRaw(r5, r6, r7);
        return;
    L_0x0012:
        r4._flushBuffer();
    L_0x0015:
        r7 = r7 + r6;
    L_0x0016:
        if (r6 >= r7) goto L_0x005a;
    L_0x0018:
        r0 = r5[r6];
        r1 = 127; // 0x7f float:1.78E-43 double:6.27E-322;
        if (r0 <= r1) goto L_0x004b;
    L_0x001e:
        r0 = r6 + 1;
        r6 = r5[r6];
        r1 = 2048; // 0x800 float:2.87E-42 double:1.0118E-320;
        if (r6 >= r1) goto L_0x0046;
    L_0x0026:
        r1 = r4._outputBuffer;
        r2 = r4._outputTail;
        r3 = r2 + 1;
        r4._outputTail = r3;
        r3 = r6 >> 6;
        r3 = r3 | 192;
        r3 = (byte) r3;
        r1[r2] = r3;
        r1 = r4._outputBuffer;
        r2 = r4._outputTail;
        r3 = r2 + 1;
        r4._outputTail = r3;
        r6 = r6 & 63;
        r6 = r6 | 128;
        r6 = (byte) r6;
        r1[r2] = r6;
        r6 = r0;
        goto L_0x0016;
    L_0x0046:
        r6 = r4._outputRawMultiByteChar(r6, r5, r0, r7);
        goto L_0x0016;
    L_0x004b:
        r1 = r4._outputBuffer;
        r2 = r4._outputTail;
        r3 = r2 + 1;
        r4._outputTail = r3;
        r0 = (byte) r0;
        r1[r2] = r0;
        r6 = r6 + 1;
        if (r6 < r7) goto L_0x0018;
    L_0x005a:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8JsonGenerator.writeRaw(char[], int, int):void");
    }

    public void writeRaw(char c) throws IOException {
        if (this._outputTail + 3 >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i;
        if (c <= 127) {
            i = this._outputTail;
            this._outputTail = i + 1;
            bArr[i] = (byte) c;
        } else if (c < 2048) {
            i = this._outputTail;
            this._outputTail = i + 1;
            bArr[i] = (byte) ((c >> 6) | 192);
            i = this._outputTail;
            this._outputTail = i + 1;
            bArr[i] = (byte) ((c & 63) | 128);
        } else {
            _outputRawMultiByteChar(c, null, 0, 0);
        }
    }

    /* JADX WARNING: Missing block: B:5:0x0013, code skipped:
            if ((r6._outputTail + 3) < r6._outputEnd) goto L_0x0018;
     */
    /* JADX WARNING: Missing block: B:6:0x0015, code skipped:
            _flushBuffer();
     */
    /* JADX WARNING: Missing block: B:7:0x0018, code skipped:
            r2 = r8 + 1;
            r8 = r7[r8];
     */
    /* JADX WARNING: Missing block: B:8:0x001e, code skipped:
            if (r8 >= 2048) goto L_0x003b;
     */
    /* JADX WARNING: Missing block: B:9:0x0020, code skipped:
            r4 = r6._outputTail;
            r6._outputTail = r4 + 1;
            r1[r4] = (byte) ((r8 >> 6) | 192);
            r4 = r6._outputTail;
            r6._outputTail = r4 + 1;
            r1[r4] = (byte) ((r8 & 63) | 128);
            r8 = r2;
     */
    /* JADX WARNING: Missing block: B:10:0x003b, code skipped:
            r8 = _outputRawMultiByteChar(r8, r7, r2, r9);
     */
    private final void _writeSegmentedRaw(char[] r7, int r8, int r9) throws java.io.IOException {
        /*
        r6 = this;
        r0 = r6._outputEnd;
        r1 = r6._outputBuffer;
        r9 = r9 + r8;
    L_0x0005:
        if (r8 >= r9) goto L_0x0054;
    L_0x0007:
        r2 = r7[r8];
        r3 = 128; // 0x80 float:1.794E-43 double:6.32E-322;
        if (r2 < r3) goto L_0x0040;
    L_0x000d:
        r2 = r6._outputTail;
        r2 = r2 + 3;
        r4 = r6._outputEnd;
        if (r2 < r4) goto L_0x0018;
    L_0x0015:
        r6._flushBuffer();
    L_0x0018:
        r2 = r8 + 1;
        r8 = r7[r8];
        r4 = 2048; // 0x800 float:2.87E-42 double:1.0118E-320;
        if (r8 >= r4) goto L_0x003b;
    L_0x0020:
        r4 = r6._outputTail;
        r5 = r4 + 1;
        r6._outputTail = r5;
        r5 = r8 >> 6;
        r5 = r5 | 192;
        r5 = (byte) r5;
        r1[r4] = r5;
        r4 = r6._outputTail;
        r5 = r4 + 1;
        r6._outputTail = r5;
        r8 = r8 & 63;
        r8 = r8 | r3;
        r8 = (byte) r8;
        r1[r4] = r8;
        r8 = r2;
        goto L_0x0005;
    L_0x003b:
        r8 = r6._outputRawMultiByteChar(r8, r7, r2, r9);
        goto L_0x0005;
    L_0x0040:
        r3 = r6._outputTail;
        if (r3 < r0) goto L_0x0047;
    L_0x0044:
        r6._flushBuffer();
    L_0x0047:
        r3 = r6._outputTail;
        r4 = r3 + 1;
        r6._outputTail = r4;
        r2 = (byte) r2;
        r1[r3] = r2;
        r8 = r8 + 1;
        if (r8 < r9) goto L_0x0007;
    L_0x0054:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8JsonGenerator._writeSegmentedRaw(char[], int, int):void");
    }

    /* JADX WARNING: Missing block: B:3:0x0008, code skipped:
            r0 = r6 + 1;
            r6 = r5[r6];
     */
    /* JADX WARNING: Missing block: B:4:0x000e, code skipped:
            if (r6 >= 2048) goto L_0x0030;
     */
    /* JADX WARNING: Missing block: B:5:0x0010, code skipped:
            r1 = r4._outputBuffer;
            r2 = r4._outputTail;
            r4._outputTail = r2 + 1;
            r1[r2] = (byte) ((r6 >> 6) | 192);
            r1 = r4._outputBuffer;
            r2 = r4._outputTail;
            r4._outputTail = r2 + 1;
            r1[r2] = (byte) ((r6 & 63) | 128);
            r6 = r0;
     */
    /* JADX WARNING: Missing block: B:6:0x0030, code skipped:
            r6 = _outputRawMultiByteChar(r6, r5, r0, r7);
     */
    private void _writeRawSegment(char[] r5, int r6, int r7) throws java.io.IOException {
        /*
        r4 = this;
    L_0x0000:
        if (r6 >= r7) goto L_0x0044;
    L_0x0002:
        r0 = r5[r6];
        r1 = 127; // 0x7f float:1.78E-43 double:6.27E-322;
        if (r0 <= r1) goto L_0x0035;
    L_0x0008:
        r0 = r6 + 1;
        r6 = r5[r6];
        r1 = 2048; // 0x800 float:2.87E-42 double:1.0118E-320;
        if (r6 >= r1) goto L_0x0030;
    L_0x0010:
        r1 = r4._outputBuffer;
        r2 = r4._outputTail;
        r3 = r2 + 1;
        r4._outputTail = r3;
        r3 = r6 >> 6;
        r3 = r3 | 192;
        r3 = (byte) r3;
        r1[r2] = r3;
        r1 = r4._outputBuffer;
        r2 = r4._outputTail;
        r3 = r2 + 1;
        r4._outputTail = r3;
        r6 = r6 & 63;
        r6 = r6 | 128;
        r6 = (byte) r6;
        r1[r2] = r6;
        r6 = r0;
        goto L_0x0000;
    L_0x0030:
        r6 = r4._outputRawMultiByteChar(r6, r5, r0, r7);
        goto L_0x0000;
    L_0x0035:
        r1 = r4._outputBuffer;
        r2 = r4._outputTail;
        r3 = r2 + 1;
        r4._outputTail = r3;
        r0 = (byte) r0;
        r1[r2] = r0;
        r6 = r6 + 1;
        if (r6 < r7) goto L_0x0002;
    L_0x0044:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8JsonGenerator._writeRawSegment(char[], int, int):void");
    }

    public void writeBinary(Base64Variant base64Variant, byte[] bArr, int i, int i2) throws IOException, JsonGenerationException {
        _verifyValueWrite("write a binary value");
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr2 = this._outputBuffer;
        int i3 = this._outputTail;
        this._outputTail = i3 + 1;
        bArr2[i3] = this._quoteChar;
        _writeBinary(base64Variant, bArr, i, i2 + i);
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr3 = this._outputBuffer;
        int i4 = this._outputTail;
        this._outputTail = i4 + 1;
        bArr3[i4] = this._quoteChar;
    }

    public int writeBinary(Base64Variant base64Variant, InputStream inputStream, int i) throws IOException, JsonGenerationException {
        _verifyValueWrite("write a binary value");
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        bArr[i2] = this._quoteChar;
        bArr = this._ioContext.allocBase64Buffer();
        if (i < 0) {
            try {
                i = _writeBinary(base64Variant, inputStream, bArr);
            } catch (Throwable th) {
                this._ioContext.releaseBase64Buffer(bArr);
            }
        } else {
            int _writeBinary = _writeBinary(base64Variant, inputStream, bArr, i);
            if (_writeBinary > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Too few bytes available: missing ");
                stringBuilder.append(_writeBinary);
                stringBuilder.append(" bytes (out of ");
                stringBuilder.append(i);
                stringBuilder.append(")");
                _reportError(stringBuilder.toString());
            }
        }
        this._ioContext.releaseBase64Buffer(bArr);
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr2 = this._outputBuffer;
        int i3 = this._outputTail;
        this._outputTail = i3 + 1;
        bArr2[i3] = this._quoteChar;
        return i;
    }

    public void writeNumber(short s) throws IOException {
        _verifyValueWrite("write a number");
        if (this._outputTail + 6 >= this._outputEnd) {
            _flushBuffer();
        }
        if (this._cfgNumbersAsStrings) {
            _writeQuotedShort(s);
        } else {
            this._outputTail = NumberOutput.outputInt((int) s, this._outputBuffer, this._outputTail);
        }
    }

    private final void _writeQuotedShort(short s) throws IOException {
        if (this._outputTail + 8 >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        bArr[i] = this._quoteChar;
        this._outputTail = NumberOutput.outputInt((int) s, this._outputBuffer, this._outputTail);
        byte[] bArr2 = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        bArr2[i2] = this._quoteChar;
    }

    public void writeNumber(int i) throws IOException {
        _verifyValueWrite("write a number");
        if (this._outputTail + 11 >= this._outputEnd) {
            _flushBuffer();
        }
        if (this._cfgNumbersAsStrings) {
            _writeQuotedInt(i);
        } else {
            this._outputTail = NumberOutput.outputInt(i, this._outputBuffer, this._outputTail);
        }
    }

    private final void _writeQuotedInt(int i) throws IOException {
        if (this._outputTail + 13 >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        bArr[i2] = this._quoteChar;
        this._outputTail = NumberOutput.outputInt(i, this._outputBuffer, this._outputTail);
        byte[] bArr2 = this._outputBuffer;
        int i3 = this._outputTail;
        this._outputTail = i3 + 1;
        bArr2[i3] = this._quoteChar;
    }

    public void writeNumber(long j) throws IOException {
        _verifyValueWrite("write a number");
        if (this._cfgNumbersAsStrings) {
            _writeQuotedLong(j);
            return;
        }
        if (this._outputTail + 21 >= this._outputEnd) {
            _flushBuffer();
        }
        this._outputTail = NumberOutput.outputLong(j, this._outputBuffer, this._outputTail);
    }

    private final void _writeQuotedLong(long j) throws IOException {
        if (this._outputTail + 23 >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        bArr[i] = this._quoteChar;
        this._outputTail = NumberOutput.outputLong(j, this._outputBuffer, this._outputTail);
        byte[] bArr2 = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        bArr2[i2] = this._quoteChar;
    }

    public void writeNumber(BigInteger bigInteger) throws IOException {
        _verifyValueWrite("write a number");
        if (bigInteger == null) {
            _writeNull();
        } else if (this._cfgNumbersAsStrings) {
            _writeQuotedRaw(bigInteger.toString());
        } else {
            writeRaw(bigInteger.toString());
        }
    }

    public void writeNumber(double d) throws IOException {
        if (this._cfgNumbersAsStrings || ((Double.isNaN(d) || Double.isInfinite(d)) && Feature.QUOTE_NON_NUMERIC_NUMBERS.enabledIn(this._features))) {
            writeString(String.valueOf(d));
            return;
        }
        _verifyValueWrite("write a number");
        writeRaw(String.valueOf(d));
    }

    public void writeNumber(float f) throws IOException {
        if (this._cfgNumbersAsStrings || ((Float.isNaN(f) || Float.isInfinite(f)) && Feature.QUOTE_NON_NUMERIC_NUMBERS.enabledIn(this._features))) {
            writeString(String.valueOf(f));
            return;
        }
        _verifyValueWrite("write a number");
        writeRaw(String.valueOf(f));
    }

    public void writeNumber(BigDecimal bigDecimal) throws IOException {
        _verifyValueWrite("write a number");
        if (bigDecimal == null) {
            _writeNull();
        } else if (this._cfgNumbersAsStrings) {
            _writeQuotedRaw(_asString(bigDecimal));
        } else {
            writeRaw(_asString(bigDecimal));
        }
    }

    public void writeNumber(String str) throws IOException {
        _verifyValueWrite("write a number");
        if (this._cfgNumbersAsStrings) {
            _writeQuotedRaw(str);
        } else {
            writeRaw(str);
        }
    }

    private final void _writeQuotedRaw(String str) throws IOException {
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        bArr[i] = this._quoteChar;
        writeRaw(str);
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr2 = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        bArr2[i2] = this._quoteChar;
    }

    public void writeBoolean(boolean z) throws IOException {
        _verifyValueWrite("write a boolean value");
        if (this._outputTail + 5 >= this._outputEnd) {
            _flushBuffer();
        }
        Object obj = z ? TRUE_BYTES : FALSE_BYTES;
        int length = obj.length;
        System.arraycopy(obj, 0, this._outputBuffer, this._outputTail, length);
        this._outputTail += length;
    }

    public void writeNull() throws IOException {
        _verifyValueWrite("write a null");
        _writeNull();
    }

    /* Access modifiers changed, original: protected|final */
    public final void _verifyValueWrite(String str) throws IOException {
        int writeValue = this._writeContext.writeValue();
        if (this._cfgPrettyPrinter != null) {
            _verifyPrettyValueWrite(str, writeValue);
        } else if (writeValue != 5) {
            byte b;
            switch (writeValue) {
                case 1:
                    b = BYTE_COMMA;
                    break;
                case 2:
                    b = BYTE_COLON;
                    break;
                case 3:
                    if (this._rootValueSeparator != null) {
                        byte[] asUnquotedUTF8 = this._rootValueSeparator.asUnquotedUTF8();
                        if (asUnquotedUTF8.length > 0) {
                            _writeBytes(asUnquotedUTF8);
                        }
                    }
                    return;
                default:
                    return;
            }
            if (this._outputTail >= this._outputEnd) {
                _flushBuffer();
            }
            byte[] bArr = this._outputBuffer;
            int i = this._outputTail;
            this._outputTail = i + 1;
            bArr[i] = b;
        } else {
            _reportCantWriteValueExpectName(str);
        }
    }

    public void flush() throws IOException {
        _flushBuffer();
        if (this._outputStream != null && isEnabled(Feature.FLUSH_PASSED_TO_STREAM)) {
            this._outputStream.flush();
        }
    }

    public void close() throws IOException {
        super.close();
        if (this._outputBuffer != null && isEnabled(Feature.AUTO_CLOSE_JSON_CONTENT)) {
            while (true) {
                JsonStreamContext outputContext = getOutputContext();
                if (!outputContext.inArray()) {
                    if (!outputContext.inObject()) {
                        break;
                    }
                    writeEndObject();
                } else {
                    writeEndArray();
                }
            }
        }
        _flushBuffer();
        this._outputTail = 0;
        if (this._outputStream != null) {
            if (this._ioContext.isResourceManaged() || isEnabled(Feature.AUTO_CLOSE_TARGET)) {
                this._outputStream.close();
            } else if (isEnabled(Feature.FLUSH_PASSED_TO_STREAM)) {
                this._outputStream.flush();
            }
        }
        _releaseBuffers();
    }

    /* Access modifiers changed, original: protected */
    public void _releaseBuffers() {
        byte[] bArr = this._outputBuffer;
        if (bArr != null && this._bufferRecyclable) {
            this._outputBuffer = null;
            this._ioContext.releaseWriteEncodingBuffer(bArr);
        }
        char[] cArr = this._charBuffer;
        if (cArr != null) {
            this._charBuffer = null;
            this._ioContext.releaseConcatBuffer(cArr);
        }
    }

    private final void _writeBytes(byte[] bArr) throws IOException {
        int length = bArr.length;
        if (this._outputTail + length > this._outputEnd) {
            _flushBuffer();
            if (length > 512) {
                this._outputStream.write(bArr, 0, length);
                return;
            }
        }
        System.arraycopy(bArr, 0, this._outputBuffer, this._outputTail, length);
        this._outputTail += length;
    }

    private final void _writeBytes(byte[] bArr, int i, int i2) throws IOException {
        if (this._outputTail + i2 > this._outputEnd) {
            _flushBuffer();
            if (i2 > 512) {
                this._outputStream.write(bArr, i, i2);
                return;
            }
        }
        System.arraycopy(bArr, i, this._outputBuffer, this._outputTail, i2);
        this._outputTail += i2;
    }

    private final void _writeStringSegments(String str, boolean z) throws IOException {
        int i;
        if (z) {
            if (this._outputTail >= this._outputEnd) {
                _flushBuffer();
            }
            byte[] bArr = this._outputBuffer;
            i = this._outputTail;
            this._outputTail = i + 1;
            bArr[i] = this._quoteChar;
        }
        int length = str.length();
        i = 0;
        while (length > 0) {
            int min = Math.min(this._outputMaxContiguous, length);
            if (this._outputTail + min > this._outputEnd) {
                _flushBuffer();
            }
            _writeStringSegment(str, i, min);
            i += min;
            length -= min;
        }
        if (z) {
            if (this._outputTail >= this._outputEnd) {
                _flushBuffer();
            }
            byte[] bArr2 = this._outputBuffer;
            int i2 = this._outputTail;
            this._outputTail = i2 + 1;
            bArr2[i2] = this._quoteChar;
        }
    }

    private final void _writeStringSegments(char[] cArr, int i, int i2) throws IOException {
        do {
            int min = Math.min(this._outputMaxContiguous, i2);
            if (this._outputTail + min > this._outputEnd) {
                _flushBuffer();
            }
            _writeStringSegment(cArr, i, min);
            i += min;
            i2 -= min;
        } while (i2 > 0);
    }

    private final void _writeStringSegments(String str, int i, int i2) throws IOException {
        do {
            int min = Math.min(this._outputMaxContiguous, i2);
            if (this._outputTail + min > this._outputEnd) {
                _flushBuffer();
            }
            _writeStringSegment(str, i, min);
            i += min;
            i2 -= min;
        } while (i2 > 0);
    }

    private final void _writeStringSegment(char[] cArr, int i, int i2) throws IOException {
        i2 += i;
        int i3 = this._outputTail;
        byte[] bArr = this._outputBuffer;
        int[] iArr = this._outputEscapes;
        while (i < i2) {
            char c = cArr[i];
            if (c > 127 || iArr[c] != 0) {
                break;
            }
            int i4 = i3 + 1;
            bArr[i3] = (byte) c;
            i++;
            i3 = i4;
        }
        this._outputTail = i3;
        if (i >= i2) {
            return;
        }
        if (this._characterEscapes != null) {
            _writeCustomStringSegment2(cArr, i, i2);
        } else if (this._maximumNonEscapedChar == 0) {
            _writeStringSegment2(cArr, i, i2);
        } else {
            _writeStringSegmentASCII2(cArr, i, i2);
        }
    }

    private final void _writeStringSegment(String str, int i, int i2) throws IOException {
        i2 += i;
        int i3 = this._outputTail;
        byte[] bArr = this._outputBuffer;
        int[] iArr = this._outputEscapes;
        while (i < i2) {
            char charAt = str.charAt(i);
            if (charAt > 127 || iArr[charAt] != 0) {
                break;
            }
            int i4 = i3 + 1;
            bArr[i3] = (byte) charAt;
            i++;
            i3 = i4;
        }
        this._outputTail = i3;
        if (i >= i2) {
            return;
        }
        if (this._characterEscapes != null) {
            _writeCustomStringSegment2(str, i, i2);
        } else if (this._maximumNonEscapedChar == 0) {
            _writeStringSegment2(str, i, i2);
        } else {
            _writeStringSegmentASCII2(str, i, i2);
        }
    }

    private final void _writeStringSegment2(char[] cArr, int i, int i2) throws IOException {
        if (this._outputTail + ((i2 - i) * 6) > this._outputEnd) {
            _flushBuffer();
        }
        int i3 = this._outputTail;
        byte[] bArr = this._outputBuffer;
        int[] iArr = this._outputEscapes;
        while (i < i2) {
            int i4 = i + 1;
            char c = cArr[i];
            int i5;
            if (c <= 127) {
                if (iArr[c] == 0) {
                    i5 = i3 + 1;
                    bArr[i3] = (byte) c;
                    i = i4;
                    i3 = i5;
                } else {
                    i5 = iArr[c];
                    if (i5 > 0) {
                        i = i3 + 1;
                        bArr[i3] = BYTE_BACKSLASH;
                        i3 = i + 1;
                        bArr[i] = (byte) i5;
                    } else {
                        i3 = _writeGenericEscape(c, i3);
                    }
                }
            } else if (c <= 2047) {
                i5 = i3 + 1;
                bArr[i3] = (byte) ((c >> 6) | 192);
                i3 = i5 + 1;
                bArr[i5] = (byte) ((c & 63) | 128);
            } else {
                i3 = _outputMultiByteChar(c, i3);
            }
            i = i4;
        }
        this._outputTail = i3;
    }

    private final void _writeStringSegment2(String str, int i, int i2) throws IOException {
        if (this._outputTail + ((i2 - i) * 6) > this._outputEnd) {
            _flushBuffer();
        }
        int i3 = this._outputTail;
        byte[] bArr = this._outputBuffer;
        int[] iArr = this._outputEscapes;
        while (i < i2) {
            int i4 = i + 1;
            char charAt = str.charAt(i);
            int i5;
            if (charAt <= 127) {
                if (iArr[charAt] == 0) {
                    i5 = i3 + 1;
                    bArr[i3] = (byte) charAt;
                    i = i4;
                    i3 = i5;
                } else {
                    i5 = iArr[charAt];
                    if (i5 > 0) {
                        i = i3 + 1;
                        bArr[i3] = BYTE_BACKSLASH;
                        i3 = i + 1;
                        bArr[i] = (byte) i5;
                    } else {
                        i3 = _writeGenericEscape(charAt, i3);
                    }
                }
            } else if (charAt <= 2047) {
                i5 = i3 + 1;
                bArr[i3] = (byte) ((charAt >> 6) | 192);
                i3 = i5 + 1;
                bArr[i5] = (byte) ((charAt & 63) | 128);
            } else {
                i3 = _outputMultiByteChar(charAt, i3);
            }
            i = i4;
        }
        this._outputTail = i3;
    }

    private final void _writeStringSegmentASCII2(char[] cArr, int i, int i2) throws IOException {
        if (this._outputTail + ((i2 - i) * 6) > this._outputEnd) {
            _flushBuffer();
        }
        int i3 = this._outputTail;
        byte[] bArr = this._outputBuffer;
        int[] iArr = this._outputEscapes;
        char c = this._maximumNonEscapedChar;
        while (i < i2) {
            int i4 = i + 1;
            char c2 = cArr[i];
            int i5;
            if (c2 <= 127) {
                if (iArr[c2] == 0) {
                    i5 = i3 + 1;
                    bArr[i3] = (byte) c2;
                    i = i4;
                    i3 = i5;
                } else {
                    i5 = iArr[c2];
                    if (i5 > 0) {
                        i = i3 + 1;
                        bArr[i3] = BYTE_BACKSLASH;
                        i3 = i + 1;
                        bArr[i] = (byte) i5;
                    } else {
                        i3 = _writeGenericEscape(c2, i3);
                    }
                }
            } else if (c2 > c) {
                i3 = _writeGenericEscape(c2, i3);
            } else if (c2 <= 2047) {
                i5 = i3 + 1;
                bArr[i3] = (byte) ((c2 >> 6) | 192);
                i3 = i5 + 1;
                bArr[i5] = (byte) ((c2 & 63) | 128);
            } else {
                i3 = _outputMultiByteChar(c2, i3);
            }
            i = i4;
        }
        this._outputTail = i3;
    }

    private final void _writeStringSegmentASCII2(String str, int i, int i2) throws IOException {
        if (this._outputTail + ((i2 - i) * 6) > this._outputEnd) {
            _flushBuffer();
        }
        int i3 = this._outputTail;
        byte[] bArr = this._outputBuffer;
        int[] iArr = this._outputEscapes;
        char c = this._maximumNonEscapedChar;
        while (i < i2) {
            int i4 = i + 1;
            char charAt = str.charAt(i);
            int i5;
            if (charAt <= 127) {
                if (iArr[charAt] == 0) {
                    i5 = i3 + 1;
                    bArr[i3] = (byte) charAt;
                    i = i4;
                    i3 = i5;
                } else {
                    i5 = iArr[charAt];
                    if (i5 > 0) {
                        i = i3 + 1;
                        bArr[i3] = BYTE_BACKSLASH;
                        i3 = i + 1;
                        bArr[i] = (byte) i5;
                    } else {
                        i3 = _writeGenericEscape(charAt, i3);
                    }
                }
            } else if (charAt > c) {
                i3 = _writeGenericEscape(charAt, i3);
            } else if (charAt <= 2047) {
                i5 = i3 + 1;
                bArr[i3] = (byte) ((charAt >> 6) | 192);
                i3 = i5 + 1;
                bArr[i5] = (byte) ((charAt & 63) | 128);
            } else {
                i3 = _outputMultiByteChar(charAt, i3);
            }
            i = i4;
        }
        this._outputTail = i3;
    }

    private final void _writeCustomStringSegment2(char[] cArr, int i, int i2) throws IOException {
        if (this._outputTail + ((i2 - i) * 6) > this._outputEnd) {
            _flushBuffer();
        }
        int i3 = this._outputTail;
        byte[] bArr = this._outputBuffer;
        int[] iArr = this._outputEscapes;
        char c = this._maximumNonEscapedChar <= 0 ? 65535 : this._maximumNonEscapedChar;
        CharacterEscapes characterEscapes = this._characterEscapes;
        while (i < i2) {
            int i4 = i + 1;
            char c2 = cArr[i];
            int i5;
            SerializableString escapeSequence;
            if (c2 <= 127) {
                if (iArr[c2] == 0) {
                    i5 = i3 + 1;
                    bArr[i3] = (byte) c2;
                    i = i4;
                    i3 = i5;
                } else {
                    i5 = iArr[c2];
                    if (i5 > 0) {
                        i = i3 + 1;
                        bArr[i3] = BYTE_BACKSLASH;
                        i3 = i + 1;
                        bArr[i] = (byte) i5;
                    } else if (i5 == -2) {
                        escapeSequence = characterEscapes.getEscapeSequence(c2);
                        if (escapeSequence == null) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Invalid custom escape definitions; custom escape not found for character code 0x");
                            stringBuilder.append(Integer.toHexString(c2));
                            stringBuilder.append(", although was supposed to have one");
                            _reportError(stringBuilder.toString());
                        }
                        i3 = _writeCustomEscape(bArr, i3, escapeSequence, i2 - i4);
                    } else {
                        i3 = _writeGenericEscape(c2, i3);
                    }
                }
            } else if (c2 > c) {
                i3 = _writeGenericEscape(c2, i3);
            } else {
                escapeSequence = characterEscapes.getEscapeSequence(c2);
                if (escapeSequence != null) {
                    i3 = _writeCustomEscape(bArr, i3, escapeSequence, i2 - i4);
                } else if (c2 <= 2047) {
                    i5 = i3 + 1;
                    bArr[i3] = (byte) ((c2 >> 6) | 192);
                    i3 = i5 + 1;
                    bArr[i5] = (byte) ((c2 & 63) | 128);
                } else {
                    i3 = _outputMultiByteChar(c2, i3);
                }
            }
            i = i4;
        }
        this._outputTail = i3;
    }

    private final void _writeCustomStringSegment2(String str, int i, int i2) throws IOException {
        if (this._outputTail + ((i2 - i) * 6) > this._outputEnd) {
            _flushBuffer();
        }
        int i3 = this._outputTail;
        byte[] bArr = this._outputBuffer;
        int[] iArr = this._outputEscapes;
        char c = this._maximumNonEscapedChar <= 0 ? 65535 : this._maximumNonEscapedChar;
        CharacterEscapes characterEscapes = this._characterEscapes;
        while (i < i2) {
            int i4 = i + 1;
            char charAt = str.charAt(i);
            int i5;
            SerializableString escapeSequence;
            if (charAt <= 127) {
                if (iArr[charAt] == 0) {
                    i5 = i3 + 1;
                    bArr[i3] = (byte) charAt;
                    i = i4;
                    i3 = i5;
                } else {
                    i5 = iArr[charAt];
                    if (i5 > 0) {
                        i = i3 + 1;
                        bArr[i3] = BYTE_BACKSLASH;
                        i3 = i + 1;
                        bArr[i] = (byte) i5;
                    } else if (i5 == -2) {
                        escapeSequence = characterEscapes.getEscapeSequence(charAt);
                        if (escapeSequence == null) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Invalid custom escape definitions; custom escape not found for character code 0x");
                            stringBuilder.append(Integer.toHexString(charAt));
                            stringBuilder.append(", although was supposed to have one");
                            _reportError(stringBuilder.toString());
                        }
                        i3 = _writeCustomEscape(bArr, i3, escapeSequence, i2 - i4);
                    } else {
                        i3 = _writeGenericEscape(charAt, i3);
                    }
                }
            } else if (charAt > c) {
                i3 = _writeGenericEscape(charAt, i3);
            } else {
                escapeSequence = characterEscapes.getEscapeSequence(charAt);
                if (escapeSequence != null) {
                    i3 = _writeCustomEscape(bArr, i3, escapeSequence, i2 - i4);
                } else if (charAt <= 2047) {
                    i5 = i3 + 1;
                    bArr[i3] = (byte) ((charAt >> 6) | 192);
                    i3 = i5 + 1;
                    bArr[i5] = (byte) ((charAt & 63) | 128);
                } else {
                    i3 = _outputMultiByteChar(charAt, i3);
                }
            }
            i = i4;
        }
        this._outputTail = i3;
    }

    private final int _writeCustomEscape(byte[] bArr, int i, SerializableString serializableString, int i2) throws IOException, JsonGenerationException {
        byte[] asUnquotedUTF8 = serializableString.asUnquotedUTF8();
        int length = asUnquotedUTF8.length;
        if (length > 6) {
            return _handleLongCustomEscape(bArr, i, this._outputEnd, asUnquotedUTF8, i2);
        }
        System.arraycopy(asUnquotedUTF8, 0, bArr, i, length);
        return i + length;
    }

    private final int _handleLongCustomEscape(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws IOException, JsonGenerationException {
        int length = bArr2.length;
        if (i + length > i2) {
            this._outputTail = i;
            _flushBuffer();
            i = this._outputTail;
            if (length > bArr.length) {
                this._outputStream.write(bArr2, 0, length);
                return i;
            }
            System.arraycopy(bArr2, 0, bArr, i, length);
            i += length;
        }
        if ((i3 * 6) + i <= i2) {
            return i;
        }
        _flushBuffer();
        return this._outputTail;
    }

    private final void _writeUTF8Segments(byte[] bArr, int i, int i2) throws IOException, JsonGenerationException {
        do {
            int min = Math.min(this._outputMaxContiguous, i2);
            _writeUTF8Segment(bArr, i, min);
            i += min;
            i2 -= min;
        } while (i2 > 0);
    }

    private final void _writeUTF8Segment(byte[] bArr, int i, int i2) throws IOException, JsonGenerationException {
        int[] iArr = this._outputEscapes;
        int i3 = i + i2;
        int i4 = i;
        while (i4 < i3) {
            int i5 = i4 + 1;
            byte b = bArr[i4];
            if (b < (byte) 0 || iArr[b] == 0) {
                i4 = i5;
            } else {
                _writeUTF8Segment2(bArr, i, i2);
                return;
            }
        }
        if (this._outputTail + i2 > this._outputEnd) {
            _flushBuffer();
        }
        System.arraycopy(bArr, i, this._outputBuffer, this._outputTail, i2);
        this._outputTail += i2;
    }

    private final void _writeUTF8Segment2(byte[] bArr, int i, int i2) throws IOException, JsonGenerationException {
        int i3 = this._outputTail;
        if ((i2 * 6) + i3 > this._outputEnd) {
            _flushBuffer();
            i3 = this._outputTail;
        }
        byte[] bArr2 = this._outputBuffer;
        int[] iArr = this._outputEscapes;
        i2 += i;
        while (i < i2) {
            int i4 = i + 1;
            byte b = bArr[i];
            int i5;
            if (b < (byte) 0 || iArr[b] == 0) {
                i5 = i3 + 1;
                bArr2[i3] = b;
                i = i4;
                i3 = i5;
            } else {
                i5 = iArr[b];
                if (i5 > 0) {
                    i = i3 + 1;
                    bArr2[i3] = BYTE_BACKSLASH;
                    i3 = i + 1;
                    bArr2[i] = (byte) i5;
                } else {
                    i3 = _writeGenericEscape(b, i3);
                }
                i = i4;
            }
        }
        this._outputTail = i3;
    }

    /* Access modifiers changed, original: protected|final */
    public final void _writeBinary(Base64Variant base64Variant, byte[] bArr, int i, int i2) throws IOException, JsonGenerationException {
        int i3 = i2 - 3;
        int i4 = this._outputEnd - 6;
        int maxLineLength = base64Variant.getMaxLineLength() >> 2;
        while (i <= i3) {
            if (this._outputTail > i4) {
                _flushBuffer();
            }
            int i5 = i + 1;
            int i6 = i5 + 1;
            i5 = i6 + 1;
            this._outputTail = base64Variant.encodeBase64Chunk((((bArr[i] << 8) | (bArr[i5] & 255)) << 8) | (bArr[i6] & 255), this._outputBuffer, this._outputTail);
            maxLineLength--;
            if (maxLineLength <= 0) {
                byte[] bArr2 = this._outputBuffer;
                maxLineLength = this._outputTail;
                this._outputTail = maxLineLength + 1;
                bArr2[maxLineLength] = BYTE_BACKSLASH;
                bArr2 = this._outputBuffer;
                maxLineLength = this._outputTail;
                this._outputTail = maxLineLength + 1;
                bArr2[maxLineLength] = (byte) 110;
                maxLineLength = base64Variant.getMaxLineLength() >> 2;
            }
            i = i5;
        }
        i2 -= i;
        if (i2 > 0) {
            if (this._outputTail > i4) {
                _flushBuffer();
            }
            i3 = i + 1;
            i = bArr[i] << 16;
            if (i2 == 2) {
                i |= (bArr[i3] & 255) << 8;
            }
            this._outputTail = base64Variant.encodeBase64Partial(i, i2, this._outputBuffer, this._outputTail);
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final int _writeBinary(Base64Variant base64Variant, InputStream inputStream, byte[] bArr, int i) throws IOException, JsonGenerationException {
        int i2 = this._outputEnd - 6;
        int maxLineLength = base64Variant.getMaxLineLength() >> 2;
        int i3 = -3;
        int i4 = 0;
        int i5 = 0;
        while (i > 2) {
            if (i4 > i3) {
                i3 = _readMore(inputStream, bArr, i4, i5, i);
                if (i3 < 3) {
                    i5 = i3;
                    i4 = 0;
                    break;
                }
                i5 = i3;
                i3 -= 3;
                i4 = 0;
            }
            if (this._outputTail > i2) {
                _flushBuffer();
            }
            int i6 = i4 + 1;
            int i7 = bArr[i4] << 8;
            int i8 = i6 + 1;
            i4 = i8 + 1;
            i -= 3;
            this._outputTail = base64Variant.encodeBase64Chunk((((bArr[i6] & 255) | i7) << 8) | (bArr[i8] & 255), this._outputBuffer, this._outputTail);
            maxLineLength--;
            if (maxLineLength <= 0) {
                byte[] bArr2 = this._outputBuffer;
                i6 = this._outputTail;
                this._outputTail = i6 + 1;
                bArr2[i6] = BYTE_BACKSLASH;
                bArr2 = this._outputBuffer;
                i6 = this._outputTail;
                this._outputTail = i6 + 1;
                bArr2[i6] = (byte) 110;
                maxLineLength = base64Variant.getMaxLineLength() >> 2;
            }
        }
        if (i <= 0) {
            return i;
        }
        int _readMore = _readMore(inputStream, bArr, i4, i5, i);
        if (_readMore <= 0) {
            return i;
        }
        if (this._outputTail > i2) {
            _flushBuffer();
        }
        i2 = bArr[0] << 16;
        maxLineLength = 1;
        if (1 < _readMore) {
            i2 |= (bArr[1] & 255) << 8;
            maxLineLength = 2;
        }
        this._outputTail = base64Variant.encodeBase64Partial(i2, maxLineLength, this._outputBuffer, this._outputTail);
        return i - maxLineLength;
    }

    /* Access modifiers changed, original: protected|final */
    public final int _writeBinary(Base64Variant base64Variant, InputStream inputStream, byte[] bArr) throws IOException, JsonGenerationException {
        int i = this._outputEnd - 6;
        int i2 = -3;
        int maxLineLength = base64Variant.getMaxLineLength() >> 2;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        while (true) {
            if (i4 > i2) {
                i2 = _readMore(inputStream, bArr, i4, i5, bArr.length);
                if (i2 < 3) {
                    break;
                }
                i5 = i2;
                i2 -= 3;
                i4 = 0;
            }
            if (this._outputTail > i) {
                _flushBuffer();
            }
            int i6 = i4 + 1;
            int i7 = bArr[i4] << 8;
            int i8 = i6 + 1;
            i4 = i8 + 1;
            i3 += 3;
            this._outputTail = base64Variant.encodeBase64Chunk((((bArr[i6] & 255) | i7) << 8) | (bArr[i8] & 255), this._outputBuffer, this._outputTail);
            maxLineLength--;
            if (maxLineLength <= 0) {
                byte[] bArr2 = this._outputBuffer;
                i7 = this._outputTail;
                this._outputTail = i7 + 1;
                bArr2[i7] = BYTE_BACKSLASH;
                bArr2 = this._outputBuffer;
                i7 = this._outputTail;
                this._outputTail = i7 + 1;
                bArr2[i7] = (byte) 110;
                maxLineLength = base64Variant.getMaxLineLength() >> 2;
            }
        }
        if (i2 <= 0) {
            return i3;
        }
        if (this._outputTail > i) {
            _flushBuffer();
        }
        int i9 = bArr[0] << 16;
        i = 1;
        if (1 < i2) {
            i9 |= (bArr[1] & 255) << 8;
            i = 2;
        }
        i3 += i;
        this._outputTail = base64Variant.encodeBase64Partial(i9, i, this._outputBuffer, this._outputTail);
        return i3;
    }

    private final int _readMore(InputStream inputStream, byte[] bArr, int i, int i2, int i3) throws IOException {
        int i4 = 0;
        while (i < i2) {
            int i5 = i4 + 1;
            int i6 = i + 1;
            bArr[i4] = bArr[i];
            i4 = i5;
            i = i6;
        }
        i = Math.min(i3, bArr.length);
        do {
            i2 = i - i4;
            if (i2 == 0) {
                break;
            }
            i2 = inputStream.read(bArr, i4, i2);
            if (i2 < 0) {
                return i4;
            }
            i4 += i2;
        } while (i4 < 3);
        return i4;
    }

    private final int _outputRawMultiByteChar(int i, char[] cArr, int i2, int i3) throws IOException {
        if (i < GeneratorBase.SURR1_FIRST || i > GeneratorBase.SURR2_LAST) {
            byte[] bArr = this._outputBuffer;
            i3 = this._outputTail;
            this._outputTail = i3 + 1;
            bArr[i3] = (byte) ((i >> 12) | 224);
            i3 = this._outputTail;
            this._outputTail = i3 + 1;
            bArr[i3] = (byte) (((i >> 6) & 63) | 128);
            i3 = this._outputTail;
            this._outputTail = i3 + 1;
            bArr[i3] = (byte) ((i & 63) | 128);
            return i2;
        }
        if (i2 >= i3 || cArr == null) {
            _reportError(String.format("Split surrogate on writeRaw() input (last character): first character 0x%4x", new Object[]{Integer.valueOf(i)}));
        }
        _outputSurrogates(i, cArr[i2]);
        return i2 + 1;
    }

    /* Access modifiers changed, original: protected|final */
    public final void _outputSurrogates(int i, int i2) throws IOException {
        i = _decodeSurrogate(i, i2);
        if (this._outputTail + 4 > this._outputEnd) {
            _flushBuffer();
        }
        byte[] bArr = this._outputBuffer;
        int i3 = this._outputTail;
        this._outputTail = i3 + 1;
        bArr[i3] = (byte) ((i >> 18) | 240);
        i3 = this._outputTail;
        this._outputTail = i3 + 1;
        bArr[i3] = (byte) (((i >> 12) & 63) | 128);
        i3 = this._outputTail;
        this._outputTail = i3 + 1;
        bArr[i3] = (byte) (((i >> 6) & 63) | 128);
        i3 = this._outputTail;
        this._outputTail = i3 + 1;
        bArr[i3] = (byte) ((i & 63) | 128);
    }

    private final int _outputMultiByteChar(int i, int i2) throws IOException {
        byte[] bArr = this._outputBuffer;
        int i3;
        if (i < GeneratorBase.SURR1_FIRST || i > GeneratorBase.SURR2_LAST) {
            i3 = i2 + 1;
            bArr[i2] = (byte) ((i >> 12) | 224);
            i2 = i3 + 1;
            bArr[i3] = (byte) (((i >> 6) & 63) | 128);
            i3 = i2 + 1;
            bArr[i2] = (byte) ((i & 63) | 128);
            return i3;
        }
        i3 = i2 + 1;
        bArr[i2] = BYTE_BACKSLASH;
        i2 = i3 + 1;
        bArr[i3] = BYTE_u;
        i3 = i2 + 1;
        bArr[i2] = HEX_CHARS[(i >> 12) & 15];
        i2 = i3 + 1;
        bArr[i3] = HEX_CHARS[(i >> 8) & 15];
        i3 = i2 + 1;
        bArr[i2] = HEX_CHARS[(i >> 4) & 15];
        i2 = i3 + 1;
        bArr[i3] = HEX_CHARS[i & 15];
        return i2;
    }

    private final void _writeNull() throws IOException {
        if (this._outputTail + 4 >= this._outputEnd) {
            _flushBuffer();
        }
        System.arraycopy(NULL_BYTES, 0, this._outputBuffer, this._outputTail, 4);
        this._outputTail += 4;
    }

    private int _writeGenericEscape(int i, int i2) throws IOException {
        byte[] bArr = this._outputBuffer;
        int i3 = i2 + 1;
        bArr[i2] = BYTE_BACKSLASH;
        i2 = i3 + 1;
        bArr[i3] = BYTE_u;
        if (i > 255) {
            i3 = 255 & (i >> 8);
            int i4 = i2 + 1;
            bArr[i2] = HEX_CHARS[i3 >> 4];
            i2 = i4 + 1;
            bArr[i4] = HEX_CHARS[i3 & 15];
            i &= 255;
        } else {
            i3 = i2 + 1;
            bArr[i2] = BYTE_0;
            i2 = i3 + 1;
            bArr[i3] = BYTE_0;
        }
        i3 = i2 + 1;
        bArr[i2] = HEX_CHARS[i >> 4];
        i2 = i3 + 1;
        bArr[i3] = HEX_CHARS[i & 15];
        return i2;
    }

    /* Access modifiers changed, original: protected|final */
    public final void _flushBuffer() throws IOException {
        int i = this._outputTail;
        if (i > 0) {
            this._outputTail = 0;
            this._outputStream.write(this._outputBuffer, 0, i);
        }
    }
}
