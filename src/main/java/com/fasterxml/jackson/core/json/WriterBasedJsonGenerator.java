package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.NumberOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class WriterBasedJsonGenerator extends JsonGeneratorImpl {
    protected static final char[] HEX_CHARS = CharTypes.copyHexChars();
    protected static final int SHORT_WRITE = 32;
    protected SerializableString _currentEscape;
    protected char[] _entityBuffer;
    protected char[] _outputBuffer;
    protected int _outputEnd;
    protected int _outputHead;
    protected int _outputTail;
    protected char _quoteChar = '\"';
    protected final Writer _writer;

    public boolean canWriteFormattedNumbers() {
        return true;
    }

    public WriterBasedJsonGenerator(IOContext iOContext, int i, ObjectCodec objectCodec, Writer writer) {
        super(iOContext, i, objectCodec);
        this._writer = writer;
        this._outputBuffer = iOContext.allocConcatBuffer();
        this._outputEnd = this._outputBuffer.length;
    }

    public Object getOutputTarget() {
        return this._writer;
    }

    public int getOutputBuffered() {
        return Math.max(0, this._outputTail - this._outputHead);
    }

    public void writeFieldName(String str) throws IOException {
        int writeFieldName = this._writeContext.writeFieldName(str);
        if (writeFieldName == 4) {
            _reportError("Can not write a field name, expecting a value");
        }
        boolean z = true;
        if (writeFieldName != 1) {
            z = false;
        }
        _writeFieldName(str, z);
    }

    public void writeFieldName(SerializableString serializableString) throws IOException {
        int writeFieldName = this._writeContext.writeFieldName(serializableString.getValue());
        if (writeFieldName == 4) {
            _reportError("Can not write a field name, expecting a value");
        }
        boolean z = true;
        if (writeFieldName != 1) {
            z = false;
        }
        _writeFieldName(serializableString, z);
    }

    /* Access modifiers changed, original: protected */
    public void _writeFieldName(String str, boolean z) throws IOException {
        if (this._cfgPrettyPrinter != null) {
            _writePPFieldName(str, z);
            return;
        }
        char[] cArr;
        int i;
        if (this._outputTail + 1 >= this._outputEnd) {
            _flushBuffer();
        }
        if (z) {
            cArr = this._outputBuffer;
            i = this._outputTail;
            this._outputTail = i + 1;
            cArr[i] = ',';
        }
        if (this._cfgUnqNames) {
            _writeString(str);
            return;
        }
        cArr = this._outputBuffer;
        i = this._outputTail;
        this._outputTail = i + 1;
        cArr[i] = this._quoteChar;
        _writeString(str);
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr2 = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        cArr2[i2] = this._quoteChar;
    }

    /* Access modifiers changed, original: protected */
    public void _writeFieldName(SerializableString serializableString, boolean z) throws IOException {
        if (this._cfgPrettyPrinter != null) {
            _writePPFieldName(serializableString, z);
            return;
        }
        char[] cArr;
        if (this._outputTail + 1 >= this._outputEnd) {
            _flushBuffer();
        }
        if (z) {
            cArr = this._outputBuffer;
            int i = this._outputTail;
            this._outputTail = i + 1;
            cArr[i] = ',';
        }
        char[] asQuotedChars = serializableString.asQuotedChars();
        if (this._cfgUnqNames) {
            writeRaw(asQuotedChars, 0, asQuotedChars.length);
            return;
        }
        cArr = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        cArr[i2] = this._quoteChar;
        int length = asQuotedChars.length;
        if ((this._outputTail + length) + 1 >= this._outputEnd) {
            writeRaw(asQuotedChars, 0, length);
            if (this._outputTail >= this._outputEnd) {
                _flushBuffer();
            }
            asQuotedChars = this._outputBuffer;
            length = this._outputTail;
            this._outputTail = length + 1;
            asQuotedChars[length] = this._quoteChar;
        } else {
            System.arraycopy(asQuotedChars, 0, this._outputBuffer, this._outputTail, length);
            this._outputTail += length;
            asQuotedChars = this._outputBuffer;
            length = this._outputTail;
            this._outputTail = length + 1;
            asQuotedChars[length] = this._quoteChar;
        }
    }

    public void writeStartArray() throws IOException {
        _verifyValueWrite("start an array");
        this._writeContext = this._writeContext.createChildArrayContext();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartArray(this);
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        cArr[i] = '[';
    }

    public void writeEndArray() throws IOException {
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
            char[] cArr = this._outputBuffer;
            int i = this._outputTail;
            this._outputTail = i + 1;
            cArr[i] = ']';
        }
        this._writeContext = this._writeContext.clearAndGetParent();
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
        char[] cArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        cArr[i] = '{';
    }

    public void writeStartObject() throws IOException {
        _verifyValueWrite("start an object");
        this._writeContext = this._writeContext.createChildObjectContext();
        if (this._cfgPrettyPrinter != null) {
            this._cfgPrettyPrinter.writeStartObject(this);
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        cArr[i] = '{';
    }

    public void writeEndObject() throws IOException {
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
            char[] cArr = this._outputBuffer;
            int i = this._outputTail;
            this._outputTail = i + 1;
            cArr[i] = '}';
        }
        this._writeContext = this._writeContext.clearAndGetParent();
    }

    /* Access modifiers changed, original: protected */
    public void _writePPFieldName(String str, boolean z) throws IOException {
        if (z) {
            this._cfgPrettyPrinter.writeObjectEntrySeparator(this);
        } else {
            this._cfgPrettyPrinter.beforeObjectEntries(this);
        }
        if (this._cfgUnqNames) {
            _writeString(str);
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        cArr[i] = this._quoteChar;
        _writeString(str);
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr2 = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        cArr2[i2] = this._quoteChar;
    }

    /* Access modifiers changed, original: protected */
    public void _writePPFieldName(SerializableString serializableString, boolean z) throws IOException {
        if (z) {
            this._cfgPrettyPrinter.writeObjectEntrySeparator(this);
        } else {
            this._cfgPrettyPrinter.beforeObjectEntries(this);
        }
        char[] asQuotedChars = serializableString.asQuotedChars();
        if (this._cfgUnqNames) {
            writeRaw(asQuotedChars, 0, asQuotedChars.length);
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        cArr[i] = this._quoteChar;
        writeRaw(asQuotedChars, 0, asQuotedChars.length);
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        asQuotedChars = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        asQuotedChars[i2] = this._quoteChar;
    }

    public void writeString(String str) throws IOException {
        _verifyValueWrite("write a string");
        if (str == null) {
            _writeNull();
            return;
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        cArr[i] = this._quoteChar;
        _writeString(str);
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr2 = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        cArr2[i2] = this._quoteChar;
    }

    public void writeString(char[] cArr, int i, int i2) throws IOException {
        _verifyValueWrite("write a string");
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr2 = this._outputBuffer;
        int i3 = this._outputTail;
        this._outputTail = i3 + 1;
        cArr2[i3] = this._quoteChar;
        _writeString(cArr, i, i2);
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        cArr = this._outputBuffer;
        i = this._outputTail;
        this._outputTail = i + 1;
        cArr[i] = this._quoteChar;
    }

    public void writeString(SerializableString serializableString) throws IOException {
        _verifyValueWrite("write a string");
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        cArr[i] = this._quoteChar;
        char[] asQuotedChars = serializableString.asQuotedChars();
        int length = asQuotedChars.length;
        if (length < 32) {
            if (length > this._outputEnd - this._outputTail) {
                _flushBuffer();
            }
            System.arraycopy(asQuotedChars, 0, this._outputBuffer, this._outputTail, length);
            this._outputTail += length;
        } else {
            _flushBuffer();
            this._writer.write(asQuotedChars, 0, length);
        }
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        asQuotedChars = this._outputBuffer;
        length = this._outputTail;
        this._outputTail = length + 1;
        asQuotedChars[length] = this._quoteChar;
    }

    public void writeRawUTF8String(byte[] bArr, int i, int i2) throws IOException {
        _reportUnsupportedOperation();
    }

    public void writeUTF8String(byte[] bArr, int i, int i2) throws IOException {
        _reportUnsupportedOperation();
    }

    public void writeRaw(String str) throws IOException {
        int length = str.length();
        int i = this._outputEnd - this._outputTail;
        if (i == 0) {
            _flushBuffer();
            i = this._outputEnd - this._outputTail;
        }
        if (i >= length) {
            str.getChars(0, length, this._outputBuffer, this._outputTail);
            this._outputTail += length;
            return;
        }
        writeRawLong(str);
    }

    public void writeRaw(String str, int i, int i2) throws IOException {
        int i3 = this._outputEnd - this._outputTail;
        if (i3 < i2) {
            _flushBuffer();
            i3 = this._outputEnd - this._outputTail;
        }
        if (i3 >= i2) {
            str.getChars(i, i + i2, this._outputBuffer, this._outputTail);
            this._outputTail += i2;
            return;
        }
        writeRawLong(str.substring(i, i2 + i));
    }

    public void writeRaw(SerializableString serializableString) throws IOException {
        writeRaw(serializableString.getValue());
    }

    public void writeRaw(char[] cArr, int i, int i2) throws IOException {
        if (i2 < 32) {
            if (i2 > this._outputEnd - this._outputTail) {
                _flushBuffer();
            }
            System.arraycopy(cArr, i, this._outputBuffer, this._outputTail, i2);
            this._outputTail += i2;
            return;
        }
        _flushBuffer();
        this._writer.write(cArr, i, i2);
    }

    public void writeRaw(char c) throws IOException {
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        cArr[i] = c;
    }

    private void writeRawLong(String str) throws IOException {
        int i = this._outputEnd - this._outputTail;
        str.getChars(0, i, this._outputBuffer, this._outputTail);
        this._outputTail += i;
        _flushBuffer();
        int length = str.length() - i;
        while (length > this._outputEnd) {
            int i2 = this._outputEnd;
            int i3 = i + i2;
            str.getChars(i, i3, this._outputBuffer, 0);
            this._outputHead = 0;
            this._outputTail = i2;
            _flushBuffer();
            length -= i2;
            i = i3;
        }
        str.getChars(i, i + length, this._outputBuffer, 0);
        this._outputHead = 0;
        this._outputTail = length;
    }

    public void writeBinary(Base64Variant base64Variant, byte[] bArr, int i, int i2) throws IOException, JsonGenerationException {
        _verifyValueWrite("write a binary value");
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr = this._outputBuffer;
        int i3 = this._outputTail;
        this._outputTail = i3 + 1;
        cArr[i3] = this._quoteChar;
        _writeBinary(base64Variant, bArr, i, i2 + i);
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr2 = this._outputBuffer;
        int i4 = this._outputTail;
        this._outputTail = i4 + 1;
        cArr2[i4] = this._quoteChar;
    }

    public int writeBinary(Base64Variant base64Variant, InputStream inputStream, int i) throws IOException, JsonGenerationException {
        _verifyValueWrite("write a binary value");
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        cArr[i2] = this._quoteChar;
        byte[] allocBase64Buffer = this._ioContext.allocBase64Buffer();
        if (i < 0) {
            try {
                i = _writeBinary(base64Variant, inputStream, allocBase64Buffer);
            } catch (Throwable th) {
                this._ioContext.releaseBase64Buffer(allocBase64Buffer);
            }
        } else {
            int _writeBinary = _writeBinary(base64Variant, inputStream, allocBase64Buffer, i);
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
        this._ioContext.releaseBase64Buffer(allocBase64Buffer);
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr2 = this._outputBuffer;
        int i3 = this._outputTail;
        this._outputTail = i3 + 1;
        cArr2[i3] = this._quoteChar;
        return i;
    }

    public void writeNumber(short s) throws IOException {
        _verifyValueWrite("write a number");
        if (this._cfgNumbersAsStrings) {
            _writeQuotedShort(s);
            return;
        }
        if (this._outputTail + 6 >= this._outputEnd) {
            _flushBuffer();
        }
        this._outputTail = NumberOutput.outputInt((int) s, this._outputBuffer, this._outputTail);
    }

    private void _writeQuotedShort(short s) throws IOException {
        if (this._outputTail + 8 >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        cArr[i] = this._quoteChar;
        this._outputTail = NumberOutput.outputInt((int) s, this._outputBuffer, this._outputTail);
        char[] cArr2 = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        cArr2[i2] = this._quoteChar;
    }

    public void writeNumber(int i) throws IOException {
        _verifyValueWrite("write a number");
        if (this._cfgNumbersAsStrings) {
            _writeQuotedInt(i);
            return;
        }
        if (this._outputTail + 11 >= this._outputEnd) {
            _flushBuffer();
        }
        this._outputTail = NumberOutput.outputInt(i, this._outputBuffer, this._outputTail);
    }

    private void _writeQuotedInt(int i) throws IOException {
        if (this._outputTail + 13 >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        cArr[i2] = this._quoteChar;
        this._outputTail = NumberOutput.outputInt(i, this._outputBuffer, this._outputTail);
        char[] cArr2 = this._outputBuffer;
        int i3 = this._outputTail;
        this._outputTail = i3 + 1;
        cArr2[i3] = this._quoteChar;
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

    private void _writeQuotedLong(long j) throws IOException {
        if (this._outputTail + 23 >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        cArr[i] = this._quoteChar;
        this._outputTail = NumberOutput.outputLong(j, this._outputBuffer, this._outputTail);
        char[] cArr2 = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        cArr2[i2] = this._quoteChar;
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
        if (this._cfgNumbersAsStrings || (isEnabled(Feature.QUOTE_NON_NUMERIC_NUMBERS) && (Double.isNaN(d) || Double.isInfinite(d)))) {
            writeString(String.valueOf(d));
            return;
        }
        _verifyValueWrite("write a number");
        writeRaw(String.valueOf(d));
    }

    public void writeNumber(float f) throws IOException {
        if (this._cfgNumbersAsStrings || (isEnabled(Feature.QUOTE_NON_NUMERIC_NUMBERS) && (Float.isNaN(f) || Float.isInfinite(f)))) {
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

    private void _writeQuotedRaw(String str) throws IOException {
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr = this._outputBuffer;
        int i = this._outputTail;
        this._outputTail = i + 1;
        cArr[i] = this._quoteChar;
        writeRaw(str);
        if (this._outputTail >= this._outputEnd) {
            _flushBuffer();
        }
        char[] cArr2 = this._outputBuffer;
        int i2 = this._outputTail;
        this._outputTail = i2 + 1;
        cArr2[i2] = this._quoteChar;
    }

    public void writeBoolean(boolean z) throws IOException {
        _verifyValueWrite("write a boolean value");
        if (this._outputTail + 5 >= this._outputEnd) {
            _flushBuffer();
        }
        int i = this._outputTail;
        char[] cArr = this._outputBuffer;
        if (z) {
            cArr[i] = 't';
            i++;
            cArr[i] = 'r';
            i++;
            cArr[i] = 'u';
            i++;
            cArr[i] = 'e';
        } else {
            cArr[i] = 'f';
            i++;
            cArr[i] = 'a';
            i++;
            cArr[i] = 'l';
            i++;
            cArr[i] = 's';
            i++;
            cArr[i] = 'e';
        }
        this._outputTail = i + 1;
    }

    public void writeNull() throws IOException {
        _verifyValueWrite("write a null");
        _writeNull();
    }

    /* Access modifiers changed, original: protected */
    public void _verifyValueWrite(String str) throws IOException {
        int writeValue = this._writeContext.writeValue();
        if (this._cfgPrettyPrinter != null) {
            _verifyPrettyValueWrite(str, writeValue);
        } else if (writeValue != 5) {
            char c;
            switch (writeValue) {
                case 1:
                    c = ',';
                    break;
                case 2:
                    c = ':';
                    break;
                case 3:
                    if (this._rootValueSeparator != null) {
                        writeRaw(this._rootValueSeparator.getValue());
                    }
                    return;
                default:
                    return;
            }
            if (this._outputTail >= this._outputEnd) {
                _flushBuffer();
            }
            char[] cArr = this._outputBuffer;
            int i = this._outputTail;
            this._outputTail = i + 1;
            cArr[i] = c;
        } else {
            _reportCantWriteValueExpectName(str);
        }
    }

    public void flush() throws IOException {
        _flushBuffer();
        if (this._writer != null && isEnabled(Feature.FLUSH_PASSED_TO_STREAM)) {
            this._writer.flush();
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
        this._outputHead = 0;
        this._outputTail = 0;
        if (this._writer != null) {
            if (this._ioContext.isResourceManaged() || isEnabled(Feature.AUTO_CLOSE_TARGET)) {
                this._writer.close();
            } else if (isEnabled(Feature.FLUSH_PASSED_TO_STREAM)) {
                this._writer.flush();
            }
        }
        _releaseBuffers();
    }

    /* Access modifiers changed, original: protected */
    public void _releaseBuffers() {
        char[] cArr = this._outputBuffer;
        if (cArr != null) {
            this._outputBuffer = null;
            this._ioContext.releaseConcatBuffer(cArr);
        }
    }

    private void _writeString(String str) throws IOException {
        int length = str.length();
        if (length > this._outputEnd) {
            _writeLongString(str);
            return;
        }
        if (this._outputTail + length > this._outputEnd) {
            _flushBuffer();
        }
        str.getChars(0, length, this._outputBuffer, this._outputTail);
        if (this._characterEscapes != null) {
            _writeStringCustom(length);
        } else if (this._maximumNonEscapedChar != 0) {
            _writeStringASCII(length, this._maximumNonEscapedChar);
        } else {
            _writeString2(length);
        }
    }

    /* JADX WARNING: Missing block: B:7:0x0016, code skipped:
            r2 = r6._outputTail - r6._outputHead;
     */
    /* JADX WARNING: Missing block: B:8:0x001b, code skipped:
            if (r2 <= 0) goto L_0x0026;
     */
    /* JADX WARNING: Missing block: B:9:0x001d, code skipped:
            r6._writer.write(r6._outputBuffer, r6._outputHead, r2);
     */
    /* JADX WARNING: Missing block: B:10:0x0026, code skipped:
            r2 = r6._outputBuffer;
            r3 = r6._outputTail;
            r6._outputTail = r3 + 1;
            r2 = r2[r3];
            _prependOrWriteCharacterEscape(r2, r7[r2]);
     */
    private void _writeString2(int r7) throws java.io.IOException {
        /*
        r6 = this;
        r0 = r6._outputTail;
        r0 = r0 + r7;
        r7 = r6._outputEscapes;
        r1 = r7.length;
    L_0x0006:
        r2 = r6._outputTail;
        if (r2 >= r0) goto L_0x003e;
    L_0x000a:
        r2 = r6._outputBuffer;
        r3 = r6._outputTail;
        r2 = r2[r3];
        if (r2 >= r1) goto L_0x0036;
    L_0x0012:
        r2 = r7[r2];
        if (r2 == 0) goto L_0x0036;
    L_0x0016:
        r2 = r6._outputTail;
        r3 = r6._outputHead;
        r2 = r2 - r3;
        if (r2 <= 0) goto L_0x0026;
    L_0x001d:
        r3 = r6._writer;
        r4 = r6._outputBuffer;
        r5 = r6._outputHead;
        r3.write(r4, r5, r2);
    L_0x0026:
        r2 = r6._outputBuffer;
        r3 = r6._outputTail;
        r4 = r3 + 1;
        r6._outputTail = r4;
        r2 = r2[r3];
        r3 = r7[r2];
        r6._prependOrWriteCharacterEscape(r2, r3);
        goto L_0x0006;
    L_0x0036:
        r2 = r6._outputTail;
        r2 = r2 + 1;
        r6._outputTail = r2;
        if (r2 < r0) goto L_0x000a;
    L_0x003e:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.WriterBasedJsonGenerator._writeString2(int):void");
    }

    private void _writeLongString(String str) throws IOException {
        _flushBuffer();
        int length = str.length();
        int i = 0;
        while (true) {
            int i2 = this._outputEnd;
            if (i + i2 > length) {
                i2 = length - i;
            }
            int i3 = i + i2;
            str.getChars(i, i3, this._outputBuffer, 0);
            if (this._characterEscapes != null) {
                _writeSegmentCustom(i2);
            } else if (this._maximumNonEscapedChar != 0) {
                _writeSegmentASCII(i2, this._maximumNonEscapedChar);
            } else {
                _writeSegment(i2);
            }
            if (i3 < length) {
                i = i3;
            } else {
                return;
            }
        }
    }

    private void _writeSegment(int i) throws IOException {
        int[] iArr = this._outputEscapes;
        char length = iArr.length;
        int i2 = 0;
        int i3 = 0;
        while (i2 < i) {
            char c;
            do {
                c = this._outputBuffer[i2];
                if (c < length && iArr[c] != 0) {
                    break;
                }
                i2++;
            } while (i2 < i);
            int i4 = i2 - i3;
            if (i4 > 0) {
                this._writer.write(this._outputBuffer, i3, i4);
                if (i2 >= i) {
                    return;
                }
            }
            i2++;
            i3 = _prependOrWriteCharacterEscape(this._outputBuffer, i2, i, c, iArr[c]);
        }
    }

    private void _writeString(char[] cArr, int i, int i2) throws IOException {
        if (this._characterEscapes != null) {
            _writeStringCustom(cArr, i, i2);
        } else if (this._maximumNonEscapedChar != 0) {
            _writeStringASCII(cArr, i, i2, this._maximumNonEscapedChar);
        } else {
            i2 += i;
            int[] iArr = this._outputEscapes;
            char length = iArr.length;
            while (i < i2) {
                int i3 = i;
                do {
                    char c = cArr[i3];
                    if (c < length && iArr[c] != 0) {
                        break;
                    }
                    i3++;
                } while (i3 < i2);
                int i4 = i3 - i;
                if (i4 < 32) {
                    if (this._outputTail + i4 > this._outputEnd) {
                        _flushBuffer();
                    }
                    if (i4 > 0) {
                        System.arraycopy(cArr, i, this._outputBuffer, this._outputTail, i4);
                        this._outputTail += i4;
                    }
                } else {
                    _flushBuffer();
                    this._writer.write(cArr, i, i4);
                }
                if (i3 >= i2) {
                    break;
                }
                i = i3 + 1;
                char c2 = cArr[i3];
                _appendCharacterEscape(c2, iArr[c2]);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0042 A:{SYNTHETIC} */
    private void _writeStringASCII(int r9, int r10) throws java.io.IOException, com.fasterxml.jackson.core.JsonGenerationException {
        /*
        r8 = this;
        r0 = r8._outputTail;
        r0 = r0 + r9;
        r9 = r8._outputEscapes;
        r1 = r9.length;
        r2 = r10 + 1;
        r1 = java.lang.Math.min(r1, r2);
    L_0x000c:
        r2 = r8._outputTail;
        if (r2 >= r0) goto L_0x0042;
    L_0x0010:
        r2 = r8._outputBuffer;
        r3 = r8._outputTail;
        r2 = r2[r3];
        if (r2 >= r1) goto L_0x001d;
    L_0x0018:
        r3 = r9[r2];
        if (r3 == 0) goto L_0x003a;
    L_0x001c:
        goto L_0x0020;
    L_0x001d:
        if (r2 <= r10) goto L_0x003a;
    L_0x001f:
        r3 = -1;
    L_0x0020:
        r4 = r8._outputTail;
        r5 = r8._outputHead;
        r4 = r4 - r5;
        if (r4 <= 0) goto L_0x0030;
    L_0x0027:
        r5 = r8._writer;
        r6 = r8._outputBuffer;
        r7 = r8._outputHead;
        r5.write(r6, r7, r4);
    L_0x0030:
        r4 = r8._outputTail;
        r4 = r4 + 1;
        r8._outputTail = r4;
        r8._prependOrWriteCharacterEscape(r2, r3);
        goto L_0x000c;
    L_0x003a:
        r2 = r8._outputTail;
        r2 = r2 + 1;
        r8._outputTail = r2;
        if (r2 < r0) goto L_0x0010;
    L_0x0042:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.WriterBasedJsonGenerator._writeStringASCII(int, int):void");
    }

    private void _writeSegmentASCII(int i, int i2) throws IOException, JsonGenerationException {
        int[] iArr = this._outputEscapes;
        char min = Math.min(iArr.length, i2 + 1);
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        while (i3 < i) {
            char c;
            do {
                c = this._outputBuffer[i3];
                if (c >= min) {
                    if (c > i2) {
                        i5 = -1;
                        break;
                    }
                }
                i5 = iArr[c];
                if (i5 != 0) {
                    break;
                }
                i3++;
            } while (i3 < i);
            int i6 = i3 - i4;
            if (i6 > 0) {
                this._writer.write(this._outputBuffer, i4, i6);
                if (i3 >= i) {
                    return;
                }
            }
            i3++;
            i4 = _prependOrWriteCharacterEscape(this._outputBuffer, i3, i, c, i5);
        }
    }

    private void _writeStringASCII(char[] cArr, int i, int i2, int i3) throws IOException, JsonGenerationException {
        i2 += i;
        int[] iArr = this._outputEscapes;
        char min = Math.min(iArr.length, i3 + 1);
        int i4 = 0;
        while (i < i2) {
            char c;
            int i5 = i4;
            i4 = i;
            do {
                c = cArr[i4];
                if (c >= min) {
                    if (c > i3) {
                        i5 = -1;
                        break;
                    }
                }
                i5 = iArr[c];
                if (i5 != 0) {
                    break;
                }
                i4++;
            } while (i4 < i2);
            int i6 = i4 - i;
            if (i6 < 32) {
                if (this._outputTail + i6 > this._outputEnd) {
                    _flushBuffer();
                }
                if (i6 > 0) {
                    System.arraycopy(cArr, i, this._outputBuffer, this._outputTail, i6);
                    this._outputTail += i6;
                }
            } else {
                _flushBuffer();
                this._writer.write(cArr, i, i6);
            }
            if (i4 < i2) {
                i = i4 + 1;
                _appendCharacterEscape(c, i5);
                i4 = i5;
            } else {
                return;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0057 A:{SYNTHETIC} */
    private void _writeStringCustom(int r12) throws java.io.IOException, com.fasterxml.jackson.core.JsonGenerationException {
        /*
        r11 = this;
        r0 = r11._outputTail;
        r0 = r0 + r12;
        r12 = r11._outputEscapes;
        r1 = r11._maximumNonEscapedChar;
        r2 = 1;
        if (r1 >= r2) goto L_0x000e;
    L_0x000a:
        r1 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;
        goto L_0x0010;
    L_0x000e:
        r1 = r11._maximumNonEscapedChar;
    L_0x0010:
        r3 = r12.length;
        r4 = r1 + 1;
        r3 = java.lang.Math.min(r3, r4);
        r4 = r11._characterEscapes;
    L_0x0019:
        r5 = r11._outputTail;
        if (r5 >= r0) goto L_0x0057;
    L_0x001d:
        r5 = r11._outputBuffer;
        r6 = r11._outputTail;
        r5 = r5[r6];
        if (r5 >= r3) goto L_0x002a;
    L_0x0025:
        r6 = r12[r5];
        if (r6 == 0) goto L_0x0050;
    L_0x0029:
        goto L_0x0037;
    L_0x002a:
        if (r5 <= r1) goto L_0x002e;
    L_0x002c:
        r6 = -1;
        goto L_0x0037;
    L_0x002e:
        r6 = r4.getEscapeSequence(r5);
        r11._currentEscape = r6;
        if (r6 == 0) goto L_0x0050;
    L_0x0036:
        r6 = -2;
    L_0x0037:
        r7 = r11._outputTail;
        r8 = r11._outputHead;
        r7 = r7 - r8;
        if (r7 <= 0) goto L_0x0047;
    L_0x003e:
        r8 = r11._writer;
        r9 = r11._outputBuffer;
        r10 = r11._outputHead;
        r8.write(r9, r10, r7);
    L_0x0047:
        r7 = r11._outputTail;
        r7 = r7 + r2;
        r11._outputTail = r7;
        r11._prependOrWriteCharacterEscape(r5, r6);
        goto L_0x0019;
    L_0x0050:
        r5 = r11._outputTail;
        r5 = r5 + r2;
        r11._outputTail = r5;
        if (r5 < r0) goto L_0x001d;
    L_0x0057:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.WriterBasedJsonGenerator._writeStringCustom(int):void");
    }

    private void _writeSegmentCustom(int i) throws IOException, JsonGenerationException {
        int[] iArr = this._outputEscapes;
        char c = this._maximumNonEscapedChar < 1 ? 65535 : this._maximumNonEscapedChar;
        char min = Math.min(iArr.length, c + 1);
        CharacterEscapes characterEscapes = this._characterEscapes;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        while (i2 < i) {
            char c2;
            do {
                c2 = this._outputBuffer[i2];
                if (c2 >= min) {
                    if (c2 <= c) {
                        SerializableString escapeSequence = characterEscapes.getEscapeSequence(c2);
                        this._currentEscape = escapeSequence;
                        if (escapeSequence != null) {
                            i4 = -2;
                            break;
                        }
                    }
                    i4 = -1;
                    break;
                }
                i4 = iArr[c2];
                if (i4 != 0) {
                    break;
                }
                i2++;
            } while (i2 < i);
            int i5 = i2 - i3;
            if (i5 > 0) {
                this._writer.write(this._outputBuffer, i3, i5);
                if (i2 >= i) {
                    return;
                }
            }
            i2++;
            i3 = _prependOrWriteCharacterEscape(this._outputBuffer, i2, i, c2, i4);
        }
    }

    private void _writeStringCustom(char[] cArr, int i, int i2) throws IOException, JsonGenerationException {
        i2 += i;
        int[] iArr = this._outputEscapes;
        char c = this._maximumNonEscapedChar < 1 ? 65535 : this._maximumNonEscapedChar;
        char min = Math.min(iArr.length, c + 1);
        CharacterEscapes characterEscapes = this._characterEscapes;
        int i3 = 0;
        while (i < i2) {
            char c2;
            int i4 = i3;
            i3 = i;
            do {
                c2 = cArr[i3];
                if (c2 >= min) {
                    if (c2 <= c) {
                        SerializableString escapeSequence = characterEscapes.getEscapeSequence(c2);
                        this._currentEscape = escapeSequence;
                        if (escapeSequence != null) {
                            i4 = -2;
                            break;
                        }
                    }
                    i4 = -1;
                    break;
                }
                i4 = iArr[c2];
                if (i4 != 0) {
                    break;
                }
                i3++;
            } while (i3 < i2);
            int i5 = i3 - i;
            if (i5 < 32) {
                if (this._outputTail + i5 > this._outputEnd) {
                    _flushBuffer();
                }
                if (i5 > 0) {
                    System.arraycopy(cArr, i, this._outputBuffer, this._outputTail, i5);
                    this._outputTail += i5;
                }
            } else {
                _flushBuffer();
                this._writer.write(cArr, i, i5);
            }
            if (i3 < i2) {
                i = i3 + 1;
                _appendCharacterEscape(c2, i4);
                i3 = i4;
            } else {
                return;
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void _writeBinary(Base64Variant base64Variant, byte[] bArr, int i, int i2) throws IOException, JsonGenerationException {
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
                char[] cArr = this._outputBuffer;
                maxLineLength = this._outputTail;
                this._outputTail = maxLineLength + 1;
                cArr[maxLineLength] = '\\';
                cArr = this._outputBuffer;
                maxLineLength = this._outputTail;
                this._outputTail = maxLineLength + 1;
                cArr[maxLineLength] = 'n';
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

    /* Access modifiers changed, original: protected */
    public int _writeBinary(Base64Variant base64Variant, InputStream inputStream, byte[] bArr, int i) throws IOException, JsonGenerationException {
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
                char[] cArr = this._outputBuffer;
                i6 = this._outputTail;
                this._outputTail = i6 + 1;
                cArr[i6] = '\\';
                cArr = this._outputBuffer;
                i6 = this._outputTail;
                this._outputTail = i6 + 1;
                cArr[i6] = 'n';
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

    /* Access modifiers changed, original: protected */
    public int _writeBinary(Base64Variant base64Variant, InputStream inputStream, byte[] bArr) throws IOException, JsonGenerationException {
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
                char[] cArr = this._outputBuffer;
                i7 = this._outputTail;
                this._outputTail = i7 + 1;
                cArr[i7] = '\\';
                cArr = this._outputBuffer;
                i7 = this._outputTail;
                this._outputTail = i7 + 1;
                cArr[i7] = 'n';
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

    private int _readMore(InputStream inputStream, byte[] bArr, int i, int i2, int i3) throws IOException {
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

    private final void _writeNull() throws IOException {
        if (this._outputTail + 4 >= this._outputEnd) {
            _flushBuffer();
        }
        int i = this._outputTail;
        char[] cArr = this._outputBuffer;
        cArr[i] = 'n';
        i++;
        cArr[i] = 'u';
        i++;
        cArr[i] = 'l';
        i++;
        cArr[i] = 'l';
        this._outputTail = i + 1;
    }

    private void _prependOrWriteCharacterEscape(char c, int i) throws IOException, JsonGenerationException {
        int i2;
        int i3;
        int i4;
        char[] cArr;
        if (i >= 0) {
            if (this._outputTail >= 2) {
                i2 = this._outputTail - 2;
                this._outputHead = i2;
                i3 = i2 + 1;
                this._outputBuffer[i2] = '\\';
                this._outputBuffer[i3] = (char) i;
                return;
            }
            char[] cArr2 = this._entityBuffer;
            if (cArr2 == null) {
                cArr2 = _allocateEntityBuffer();
            }
            this._outputHead = this._outputTail;
            cArr2[1] = (char) i;
            this._writer.write(cArr2, 0, 2);
        } else if (i == -2) {
            String value;
            if (this._currentEscape == null) {
                value = this._characterEscapes.getEscapeSequence(c).getValue();
            } else {
                value = this._currentEscape.getValue();
                this._currentEscape = null;
            }
            i = value.length();
            if (this._outputTail >= i) {
                i4 = this._outputTail - i;
                this._outputHead = i4;
                value.getChars(0, i, this._outputBuffer, i4);
                return;
            }
            this._outputHead = this._outputTail;
            this._writer.write(value);
        } else if (this._outputTail >= 6) {
            cArr = this._outputBuffer;
            i3 = this._outputTail - 6;
            this._outputHead = i3;
            cArr[i3] = '\\';
            i3++;
            cArr[i3] = 'u';
            if (c > 255) {
                int i5 = (c >> 8) & 255;
                i3++;
                cArr[i3] = HEX_CHARS[i5 >> 4];
                i3++;
                cArr[i3] = HEX_CHARS[i5 & 15];
                i2 = (char) (c & 255);
            } else {
                i3++;
                cArr[i3] = '0';
                i3++;
                cArr[i3] = '0';
            }
            i3++;
            cArr[i3] = HEX_CHARS[i2 >> 4];
            cArr[i3 + 1] = HEX_CHARS[i2 & 15];
        } else {
            cArr = this._entityBuffer;
            if (cArr == null) {
                cArr = _allocateEntityBuffer();
            }
            this._outputHead = this._outputTail;
            if (c > 255) {
                i4 = (c >> 8) & 255;
                i2 = c & 255;
                cArr[10] = HEX_CHARS[i4 >> 4];
                cArr[11] = HEX_CHARS[i4 & 15];
                cArr[12] = HEX_CHARS[i2 >> 4];
                cArr[13] = HEX_CHARS[i2 & 15];
                this._writer.write(cArr, 8, 6);
            } else {
                cArr[6] = HEX_CHARS[c >> 4];
                cArr[7] = HEX_CHARS[c & 15];
                this._writer.write(cArr, 2, 6);
            }
        }
    }

    private int _prependOrWriteCharacterEscape(char[] cArr, int i, int i2, char c, int i3) throws IOException, JsonGenerationException {
        if (i3 >= 0) {
            if (i <= 1 || i >= i2) {
                cArr = this._entityBuffer;
                if (cArr == null) {
                    cArr = _allocateEntityBuffer();
                }
                cArr[1] = (char) i3;
                this._writer.write(cArr, 0, 2);
            } else {
                i -= 2;
                cArr[i] = '\\';
                cArr[i + 1] = (char) i3;
            }
            return i;
        } else if (i3 != -2) {
            int i4;
            if (i <= 5 || i >= i2) {
                cArr = this._entityBuffer;
                if (cArr == null) {
                    cArr = _allocateEntityBuffer();
                }
                this._outputHead = this._outputTail;
                if (c > 255) {
                    i3 = (c >> 8) & 255;
                    i4 = c & 255;
                    cArr[10] = HEX_CHARS[i3 >> 4];
                    cArr[11] = HEX_CHARS[i3 & 15];
                    cArr[12] = HEX_CHARS[i4 >> 4];
                    cArr[13] = HEX_CHARS[i4 & 15];
                    this._writer.write(cArr, 8, 6);
                } else {
                    cArr[6] = HEX_CHARS[c >> 4];
                    cArr[7] = HEX_CHARS[c & 15];
                    this._writer.write(cArr, 2, 6);
                }
            } else {
                i -= 6;
                i2 = i + 1;
                cArr[i] = '\\';
                i = i2 + 1;
                cArr[i2] = 'u';
                if (c > 255) {
                    i2 = (c >> 8) & 255;
                    i3 = i + 1;
                    cArr[i] = HEX_CHARS[i2 >> 4];
                    i = i3 + 1;
                    cArr[i3] = HEX_CHARS[i2 & 15];
                    i4 = (char) (c & 255);
                } else {
                    i2 = i + 1;
                    cArr[i] = '0';
                    i = i2 + 1;
                    cArr[i2] = '0';
                }
                i2 = i + 1;
                cArr[i] = HEX_CHARS[i4 >> 4];
                cArr[i2] = HEX_CHARS[i4 & 15];
                i = i2 - 5;
            }
            return i;
        } else {
            String value;
            if (this._currentEscape == null) {
                value = this._characterEscapes.getEscapeSequence(c).getValue();
            } else {
                value = this._currentEscape.getValue();
                this._currentEscape = null;
            }
            i3 = value.length();
            if (i < i3 || i >= i2) {
                this._writer.write(value);
            } else {
                i -= i3;
                value.getChars(0, i3, cArr, i);
            }
            return i;
        }
    }

    private void _appendCharacterEscape(char c, int i) throws IOException, JsonGenerationException {
        int i2;
        if (i >= 0) {
            if (this._outputTail + 2 > this._outputEnd) {
                _flushBuffer();
            }
            char[] cArr = this._outputBuffer;
            int i3 = this._outputTail;
            this._outputTail = i3 + 1;
            cArr[i3] = '\\';
            cArr = this._outputBuffer;
            i2 = this._outputTail;
            this._outputTail = i2 + 1;
            cArr[i2] = (char) i;
        } else if (i != -2) {
            int c2;
            if (this._outputTail + 5 >= this._outputEnd) {
                _flushBuffer();
            }
            i = this._outputTail;
            char[] cArr2 = this._outputBuffer;
            int i4 = i + 1;
            cArr2[i] = '\\';
            i = i4 + 1;
            cArr2[i4] = 'u';
            if (c2 > 255) {
                i2 = 255 & (c2 >> 8);
                i4 = i + 1;
                cArr2[i] = HEX_CHARS[i2 >> 4];
                i = i4 + 1;
                cArr2[i4] = HEX_CHARS[i2 & 15];
                c2 = (char) (c2 & 255);
            } else {
                i2 = i + 1;
                cArr2[i] = '0';
                i = i2 + 1;
                cArr2[i2] = '0';
            }
            i2 = i + 1;
            cArr2[i] = HEX_CHARS[c2 >> 4];
            i = i2 + 1;
            cArr2[i2] = HEX_CHARS[c2 & 15];
            this._outputTail = i;
        } else {
            String value;
            if (this._currentEscape == null) {
                value = this._characterEscapes.getEscapeSequence(c2).getValue();
            } else {
                value = this._currentEscape.getValue();
                this._currentEscape = null;
            }
            i = value.length();
            if (this._outputTail + i > this._outputEnd) {
                _flushBuffer();
                if (i > this._outputEnd) {
                    this._writer.write(value);
                    return;
                }
            }
            value.getChars(0, i, this._outputBuffer, this._outputTail);
            this._outputTail += i;
        }
    }

    private char[] _allocateEntityBuffer() {
        char[] cArr = new char[14];
        cArr[0] = '\\';
        cArr[2] = '\\';
        cArr[3] = 'u';
        cArr[4] = '0';
        cArr[5] = '0';
        cArr[8] = '\\';
        cArr[9] = 'u';
        this._entityBuffer = cArr;
        return cArr;
    }

    /* Access modifiers changed, original: protected */
    public void _flushBuffer() throws IOException {
        int i = this._outputTail - this._outputHead;
        if (i > 0) {
            int i2 = this._outputHead;
            this._outputHead = 0;
            this._outputTail = 0;
            this._writer.write(this._outputBuffer, i2, i);
        }
    }
}
