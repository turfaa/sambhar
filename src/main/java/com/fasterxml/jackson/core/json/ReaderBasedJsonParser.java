package com.fasterxml.jackson.core.json;

import com.facebook.internal.ServerProtocol;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.base.ParserBase;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.TextBuffer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class ReaderBasedJsonParser extends ParserBase {
    protected static final int[] _icLatin1 = CharTypes.getInputCodeLatin1();
    protected boolean _bufferRecyclable;
    protected final int _hashSeed;
    protected char[] _inputBuffer;
    protected int _nameStartCol;
    protected long _nameStartOffset;
    protected int _nameStartRow;
    protected ObjectCodec _objectCodec;
    protected Reader _reader;
    protected final CharsToNameCanonicalizer _symbols;
    protected boolean _tokenIncomplete;

    public ReaderBasedJsonParser(IOContext iOContext, int i, Reader reader, ObjectCodec objectCodec, CharsToNameCanonicalizer charsToNameCanonicalizer, char[] cArr, int i2, int i3, boolean z) {
        super(iOContext, i);
        this._reader = reader;
        this._inputBuffer = cArr;
        this._inputPtr = i2;
        this._inputEnd = i3;
        this._objectCodec = objectCodec;
        this._symbols = charsToNameCanonicalizer;
        this._hashSeed = charsToNameCanonicalizer.hashSeed();
        this._bufferRecyclable = z;
    }

    public ReaderBasedJsonParser(IOContext iOContext, int i, Reader reader, ObjectCodec objectCodec, CharsToNameCanonicalizer charsToNameCanonicalizer) {
        super(iOContext, i);
        this._reader = reader;
        this._inputBuffer = iOContext.allocTokenBuffer();
        this._inputPtr = 0;
        this._inputEnd = 0;
        this._objectCodec = objectCodec;
        this._symbols = charsToNameCanonicalizer;
        this._hashSeed = charsToNameCanonicalizer.hashSeed();
        this._bufferRecyclable = true;
    }

    public ObjectCodec getCodec() {
        return this._objectCodec;
    }

    public void setCodec(ObjectCodec objectCodec) {
        this._objectCodec = objectCodec;
    }

    public int releaseBuffered(Writer writer) throws IOException {
        int i = this._inputEnd - this._inputPtr;
        if (i < 1) {
            return 0;
        }
        writer.write(this._inputBuffer, this._inputPtr, i);
        return i;
    }

    public Object getInputSource() {
        return this._reader;
    }

    /* Access modifiers changed, original: protected */
    @Deprecated
    public char getNextChar(String str) throws IOException {
        return getNextChar(str, null);
    }

    /* Access modifiers changed, original: protected */
    public char getNextChar(String str, JsonToken jsonToken) throws IOException {
        if (this._inputPtr >= this._inputEnd && !_loadMore()) {
            _reportInvalidEOF(str, jsonToken);
        }
        char[] cArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        return cArr[i];
    }

    /* Access modifiers changed, original: protected */
    public void _closeInput() throws IOException {
        if (this._reader != null) {
            if (this._ioContext.isResourceManaged() || isEnabled(Feature.AUTO_CLOSE_SOURCE)) {
                this._reader.close();
            }
            this._reader = null;
        }
    }

    /* Access modifiers changed, original: protected */
    public void _releaseBuffers() throws IOException {
        super._releaseBuffers();
        this._symbols.release();
        if (this._bufferRecyclable) {
            char[] cArr = this._inputBuffer;
            if (cArr != null) {
                this._inputBuffer = null;
                this._ioContext.releaseTokenBuffer(cArr);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void _loadMoreGuaranteed() throws IOException {
        if (!_loadMore()) {
            _reportInvalidEOF();
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean _loadMore() throws IOException {
        int i = this._inputEnd;
        long j = (long) i;
        this._currInputProcessed += j;
        this._currInputRowStart -= i;
        this._nameStartOffset -= j;
        if (this._reader != null) {
            i = this._reader.read(this._inputBuffer, 0, this._inputBuffer.length);
            if (i > 0) {
                this._inputPtr = 0;
                this._inputEnd = i;
                return true;
            }
            _closeInput();
            if (i == 0) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Reader returned 0 characters when trying to read ");
                stringBuilder.append(this._inputEnd);
                throw new IOException(stringBuilder.toString());
            }
        }
        return false;
    }

    public final String getText() throws IOException {
        JsonToken jsonToken = this._currToken;
        if (jsonToken != JsonToken.VALUE_STRING) {
            return _getText2(jsonToken);
        }
        if (this._tokenIncomplete) {
            this._tokenIncomplete = false;
            _finishString();
        }
        return this._textBuffer.contentsAsString();
    }

    public int getText(Writer writer) throws IOException {
        JsonToken jsonToken = this._currToken;
        if (jsonToken == JsonToken.VALUE_STRING) {
            if (this._tokenIncomplete) {
                this._tokenIncomplete = false;
                _finishString();
            }
            return this._textBuffer.contentsToWriter(writer);
        } else if (jsonToken == JsonToken.FIELD_NAME) {
            String currentName = this._parsingContext.getCurrentName();
            writer.write(currentName);
            return currentName.length();
        } else if (jsonToken == null) {
            return 0;
        } else {
            if (jsonToken.isNumeric()) {
                return this._textBuffer.contentsToWriter(writer);
            }
            char[] asCharArray = jsonToken.asCharArray();
            writer.write(asCharArray);
            return asCharArray.length;
        }
    }

    public final String getValueAsString() throws IOException {
        if (this._currToken == JsonToken.VALUE_STRING) {
            if (this._tokenIncomplete) {
                this._tokenIncomplete = false;
                _finishString();
            }
            return this._textBuffer.contentsAsString();
        } else if (this._currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        } else {
            return super.getValueAsString(null);
        }
    }

    public final String getValueAsString(String str) throws IOException {
        if (this._currToken == JsonToken.VALUE_STRING) {
            if (this._tokenIncomplete) {
                this._tokenIncomplete = false;
                _finishString();
            }
            return this._textBuffer.contentsAsString();
        } else if (this._currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        } else {
            return super.getValueAsString(str);
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final String _getText2(JsonToken jsonToken) {
        if (jsonToken == null) {
            return null;
        }
        switch (jsonToken.id()) {
            case 5:
                return this._parsingContext.getCurrentName();
            case 6:
            case 7:
            case 8:
                return this._textBuffer.contentsAsString();
            default:
                return jsonToken.asString();
        }
    }

    public final char[] getTextCharacters() throws IOException {
        if (this._currToken == null) {
            return null;
        }
        switch (this._currToken.id()) {
            case 5:
                if (!this._nameCopied) {
                    String currentName = this._parsingContext.getCurrentName();
                    int length = currentName.length();
                    if (this._nameCopyBuffer == null) {
                        this._nameCopyBuffer = this._ioContext.allocNameCopyBuffer(length);
                    } else if (this._nameCopyBuffer.length < length) {
                        this._nameCopyBuffer = new char[length];
                    }
                    currentName.getChars(0, length, this._nameCopyBuffer, 0);
                    this._nameCopied = true;
                }
                return this._nameCopyBuffer;
            case 6:
                if (this._tokenIncomplete) {
                    this._tokenIncomplete = false;
                    _finishString();
                    break;
                }
                break;
            case 7:
            case 8:
                break;
            default:
                return this._currToken.asCharArray();
        }
        return this._textBuffer.getTextBuffer();
    }

    public final int getTextLength() throws IOException {
        if (this._currToken == null) {
            return 0;
        }
        switch (this._currToken.id()) {
            case 5:
                return this._parsingContext.getCurrentName().length();
            case 6:
                if (this._tokenIncomplete) {
                    this._tokenIncomplete = false;
                    _finishString();
                    break;
                }
                break;
            case 7:
            case 8:
                break;
            default:
                return this._currToken.asCharArray().length;
        }
        return this._textBuffer.size();
    }

    /* JADX WARNING: Missing block: B:8:0x001e, code skipped:
            return r2._textBuffer.getTextOffset();
     */
    public final int getTextOffset() throws java.io.IOException {
        /*
        r2 = this;
        r0 = r2._currToken;
        r1 = 0;
        if (r0 == 0) goto L_0x0020;
    L_0x0005:
        r0 = r2._currToken;
        r0 = r0.id();
        switch(r0) {
            case 5: goto L_0x001f;
            case 6: goto L_0x000f;
            case 7: goto L_0x0018;
            case 8: goto L_0x0018;
            default: goto L_0x000e;
        };
    L_0x000e:
        goto L_0x0020;
    L_0x000f:
        r0 = r2._tokenIncomplete;
        if (r0 == 0) goto L_0x0018;
    L_0x0013:
        r2._tokenIncomplete = r1;
        r2._finishString();
    L_0x0018:
        r0 = r2._textBuffer;
        r0 = r0.getTextOffset();
        return r0;
    L_0x001f:
        return r1;
    L_0x0020:
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.ReaderBasedJsonParser.getTextOffset():int");
    }

    public byte[] getBinaryValue(Base64Variant base64Variant) throws IOException {
        if (this._currToken != JsonToken.VALUE_STRING && (this._currToken != JsonToken.VALUE_EMBEDDED_OBJECT || this._binaryValue == null)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Current token (");
            stringBuilder.append(this._currToken);
            stringBuilder.append(") not VALUE_STRING or VALUE_EMBEDDED_OBJECT, can not access as binary");
            _reportError(stringBuilder.toString());
        }
        if (this._tokenIncomplete) {
            try {
                this._binaryValue = _decodeBase64(base64Variant);
                this._tokenIncomplete = false;
            } catch (IllegalArgumentException e) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Failed to decode VALUE_STRING as base64 (");
                stringBuilder2.append(base64Variant);
                stringBuilder2.append("): ");
                stringBuilder2.append(e.getMessage());
                throw _constructError(stringBuilder2.toString());
            }
        } else if (this._binaryValue == null) {
            ByteArrayBuilder _getByteArrayBuilder = _getByteArrayBuilder();
            _decodeBase64(getText(), _getByteArrayBuilder, base64Variant);
            this._binaryValue = _getByteArrayBuilder.toByteArray();
        }
        return this._binaryValue;
    }

    public int readBinaryValue(Base64Variant base64Variant, OutputStream outputStream) throws IOException {
        if (this._tokenIncomplete && this._currToken == JsonToken.VALUE_STRING) {
            byte[] allocBase64Buffer = this._ioContext.allocBase64Buffer();
            try {
                int _readBinary = _readBinary(base64Variant, outputStream, allocBase64Buffer);
                return _readBinary;
            } finally {
                this._ioContext.releaseBase64Buffer(allocBase64Buffer);
            }
        } else {
            byte[] binaryValue = getBinaryValue(base64Variant);
            outputStream.write(binaryValue);
            return binaryValue.length;
        }
    }

    /* Access modifiers changed, original: protected */
    public int _readBinary(Base64Variant base64Variant, OutputStream outputStream, byte[] bArr) throws IOException {
        int length = bArr.length - 3;
        int i = 0;
        int i2 = 0;
        while (true) {
            if (this._inputPtr >= this._inputEnd) {
                _loadMoreGuaranteed();
            }
            char[] cArr = this._inputBuffer;
            int i3 = this._inputPtr;
            this._inputPtr = i3 + 1;
            char c = cArr[i3];
            if (c > ' ') {
                i3 = base64Variant.decodeBase64Char(c);
                if (i3 < 0) {
                    if (c == '\"') {
                        break;
                    }
                    i3 = _decodeBase64Escape(base64Variant, c, 0);
                    if (i3 < 0) {
                    }
                }
                if (i > length) {
                    i2 += i;
                    outputStream.write(bArr, 0, i);
                    i = 0;
                }
                if (this._inputPtr >= this._inputEnd) {
                    _loadMoreGuaranteed();
                }
                cArr = this._inputBuffer;
                int i4 = this._inputPtr;
                this._inputPtr = i4 + 1;
                c = cArr[i4];
                i4 = base64Variant.decodeBase64Char(c);
                if (i4 < 0) {
                    i4 = _decodeBase64Escape(base64Variant, c, 1);
                }
                int i5 = (i3 << 6) | i4;
                if (this._inputPtr >= this._inputEnd) {
                    _loadMoreGuaranteed();
                }
                char[] cArr2 = this._inputBuffer;
                i4 = this._inputPtr;
                this._inputPtr = i4 + 1;
                char c2 = cArr2[i4];
                i4 = base64Variant.decodeBase64Char(c2);
                if (i4 < 0) {
                    if (i4 != -2) {
                        if (c2 == '\"' && !base64Variant.usesPadding()) {
                            length = i + 1;
                            bArr[i] = (byte) (i5 >> 4);
                            i = length;
                            break;
                        }
                        i4 = _decodeBase64Escape(base64Variant, c2, 2);
                    }
                    if (i4 == -2) {
                        if (this._inputPtr >= this._inputEnd) {
                            _loadMoreGuaranteed();
                        }
                        cArr2 = this._inputBuffer;
                        int i6 = this._inputPtr;
                        this._inputPtr = i6 + 1;
                        c2 = cArr2[i6];
                        if (base64Variant.usesPaddingChar(c2)) {
                            i3 = i + 1;
                            bArr[i] = (byte) (i5 >> 4);
                            i = i3;
                        } else {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("expected padding character '");
                            stringBuilder.append(base64Variant.getPaddingChar());
                            stringBuilder.append("'");
                            throw reportInvalidBase64Char(base64Variant, c2, 3, stringBuilder.toString());
                        }
                    }
                }
                i5 = (i5 << 6) | i4;
                if (this._inputPtr >= this._inputEnd) {
                    _loadMoreGuaranteed();
                }
                cArr2 = this._inputBuffer;
                i4 = this._inputPtr;
                this._inputPtr = i4 + 1;
                c2 = cArr2[i4];
                i4 = base64Variant.decodeBase64Char(c2);
                if (i4 < 0) {
                    if (i4 != -2) {
                        if (c2 == '\"' && !base64Variant.usesPadding()) {
                            int i7 = i5 >> 2;
                            length = i + 1;
                            bArr[i] = (byte) (i7 >> 8);
                            i = length + 1;
                            bArr[length] = (byte) i7;
                            break;
                        }
                        i4 = _decodeBase64Escape(base64Variant, c2, 3);
                    }
                    if (i4 == -2) {
                        i5 >>= 2;
                        i3 = i + 1;
                        bArr[i] = (byte) (i5 >> 8);
                        i = i3 + 1;
                        bArr[i3] = (byte) i5;
                    }
                }
                i5 = (i5 << 6) | i4;
                i3 = i + 1;
                bArr[i] = (byte) (i5 >> 16);
                i = i3 + 1;
                bArr[i3] = (byte) (i5 >> 8);
                i3 = i + 1;
                bArr[i] = (byte) i5;
                i = i3;
            }
        }
        this._tokenIncomplete = false;
        if (i <= 0) {
            return i2;
        }
        i2 += i;
        outputStream.write(bArr, 0, i);
        return i2;
    }

    public final JsonToken nextToken() throws IOException {
        if (this._currToken == JsonToken.FIELD_NAME) {
            return _nextAfterName();
        }
        this._numTypesValid = 0;
        if (this._tokenIncomplete) {
            _skipString();
        }
        int _skipWSOrEnd = _skipWSOrEnd();
        if (_skipWSOrEnd < 0) {
            close();
            this._currToken = null;
            return null;
        }
        this._binaryValue = null;
        JsonToken jsonToken;
        if (_skipWSOrEnd == 93) {
            _updateLocation();
            if (!this._parsingContext.inArray()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, '}');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            jsonToken = JsonToken.END_ARRAY;
            this._currToken = jsonToken;
            return jsonToken;
        } else if (_skipWSOrEnd == 125) {
            _updateLocation();
            if (!this._parsingContext.inObject()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, ']');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            jsonToken = JsonToken.END_OBJECT;
            this._currToken = jsonToken;
            return jsonToken;
        } else {
            if (this._parsingContext.expectComma()) {
                _skipWSOrEnd = _skipComma(_skipWSOrEnd);
            }
            boolean inObject = this._parsingContext.inObject();
            if (inObject) {
                _updateNameLocation();
                this._parsingContext.setCurrentName(_skipWSOrEnd == 34 ? _parseName() : _handleOddName(_skipWSOrEnd));
                this._currToken = JsonToken.FIELD_NAME;
                _skipWSOrEnd = _skipColon();
            }
            _updateLocation();
            if (_skipWSOrEnd == 34) {
                this._tokenIncomplete = true;
                jsonToken = JsonToken.VALUE_STRING;
            } else if (_skipWSOrEnd == 45) {
                jsonToken = _parseNegNumber();
            } else if (_skipWSOrEnd == 91) {
                if (!inObject) {
                    this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
                }
                jsonToken = JsonToken.START_ARRAY;
            } else if (_skipWSOrEnd == 102) {
                _matchFalse();
                jsonToken = JsonToken.VALUE_FALSE;
            } else if (_skipWSOrEnd != 110) {
                if (_skipWSOrEnd != 116) {
                    if (_skipWSOrEnd == 123) {
                        if (!inObject) {
                            this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
                        }
                        jsonToken = JsonToken.START_OBJECT;
                    } else if (_skipWSOrEnd != 125) {
                        switch (_skipWSOrEnd) {
                            case 48:
                            case 49:
                            case 50:
                            case 51:
                            case 52:
                            case 53:
                            case 54:
                            case 55:
                            case 56:
                            case 57:
                                jsonToken = _parsePosNumber(_skipWSOrEnd);
                                break;
                            default:
                                jsonToken = _handleOddValue(_skipWSOrEnd);
                                break;
                        }
                    } else {
                        _reportUnexpectedChar(_skipWSOrEnd, "expected a value");
                    }
                }
                _matchTrue();
                jsonToken = JsonToken.VALUE_TRUE;
            } else {
                _matchNull();
                jsonToken = JsonToken.VALUE_NULL;
            }
            if (inObject) {
                this._nextToken = jsonToken;
                return this._currToken;
            }
            this._currToken = jsonToken;
            return jsonToken;
        }
    }

    private final JsonToken _nextAfterName() {
        this._nameCopied = false;
        JsonToken jsonToken = this._nextToken;
        this._nextToken = null;
        if (jsonToken == JsonToken.START_ARRAY) {
            this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
        } else if (jsonToken == JsonToken.START_OBJECT) {
            this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
        }
        this._currToken = jsonToken;
        return jsonToken;
    }

    public void finishToken() throws IOException {
        if (this._tokenIncomplete) {
            this._tokenIncomplete = false;
            _finishString();
        }
    }

    public boolean nextFieldName(SerializableString serializableString) throws IOException {
        int i = 0;
        this._numTypesValid = 0;
        if (this._currToken == JsonToken.FIELD_NAME) {
            _nextAfterName();
            return false;
        }
        if (this._tokenIncomplete) {
            _skipString();
        }
        int _skipWSOrEnd = _skipWSOrEnd();
        if (_skipWSOrEnd < 0) {
            close();
            this._currToken = null;
            return false;
        }
        this._binaryValue = null;
        if (_skipWSOrEnd == 93) {
            _updateLocation();
            if (!this._parsingContext.inArray()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, '}');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            this._currToken = JsonToken.END_ARRAY;
            return false;
        } else if (_skipWSOrEnd == 125) {
            _updateLocation();
            if (!this._parsingContext.inObject()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, ']');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            this._currToken = JsonToken.END_OBJECT;
            return false;
        } else {
            if (this._parsingContext.expectComma()) {
                _skipWSOrEnd = _skipComma(_skipWSOrEnd);
            }
            if (this._parsingContext.inObject()) {
                _updateNameLocation();
                if (_skipWSOrEnd == 34) {
                    char[] asQuotedChars = serializableString.asQuotedChars();
                    int length = asQuotedChars.length;
                    if ((this._inputPtr + length) + 4 < this._inputEnd) {
                        int i2 = this._inputPtr + length;
                        if (this._inputBuffer[i2] == '\"') {
                            int i3 = this._inputPtr;
                            while (i3 != i2) {
                                if (asQuotedChars[i] == this._inputBuffer[i3]) {
                                    i++;
                                    i3++;
                                }
                            }
                            this._parsingContext.setCurrentName(serializableString.getValue());
                            _isNextTokenNameYes(_skipColonFast(i3 + 1));
                            return true;
                        }
                    }
                }
                return _isNextTokenNameMaybe(_skipWSOrEnd, serializableString.getValue());
            }
            _updateLocation();
            _nextTokenNotInObject(_skipWSOrEnd);
            return false;
        }
    }

    public String nextFieldName() throws IOException {
        this._numTypesValid = 0;
        if (this._currToken == JsonToken.FIELD_NAME) {
            _nextAfterName();
            return null;
        }
        if (this._tokenIncomplete) {
            _skipString();
        }
        int _skipWSOrEnd = _skipWSOrEnd();
        if (_skipWSOrEnd < 0) {
            close();
            this._currToken = null;
            return null;
        }
        this._binaryValue = null;
        if (_skipWSOrEnd == 93) {
            _updateLocation();
            if (!this._parsingContext.inArray()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, '}');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            this._currToken = JsonToken.END_ARRAY;
            return null;
        } else if (_skipWSOrEnd == 125) {
            _updateLocation();
            if (!this._parsingContext.inObject()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, ']');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            this._currToken = JsonToken.END_OBJECT;
            return null;
        } else {
            if (this._parsingContext.expectComma()) {
                _skipWSOrEnd = _skipComma(_skipWSOrEnd);
            }
            if (this._parsingContext.inObject()) {
                _updateNameLocation();
                String _parseName = _skipWSOrEnd == 34 ? _parseName() : _handleOddName(_skipWSOrEnd);
                this._parsingContext.setCurrentName(_parseName);
                this._currToken = JsonToken.FIELD_NAME;
                int _skipColon = _skipColon();
                _updateLocation();
                if (_skipColon == 34) {
                    this._tokenIncomplete = true;
                    this._nextToken = JsonToken.VALUE_STRING;
                    return _parseName;
                }
                JsonToken _parseNegNumber;
                if (_skipColon == 45) {
                    _parseNegNumber = _parseNegNumber();
                } else if (_skipColon == 91) {
                    _parseNegNumber = JsonToken.START_ARRAY;
                } else if (_skipColon == 102) {
                    _matchFalse();
                    _parseNegNumber = JsonToken.VALUE_FALSE;
                } else if (_skipColon == 110) {
                    _matchNull();
                    _parseNegNumber = JsonToken.VALUE_NULL;
                } else if (_skipColon == 116) {
                    _matchTrue();
                    _parseNegNumber = JsonToken.VALUE_TRUE;
                } else if (_skipColon != 123) {
                    switch (_skipColon) {
                        case 48:
                        case 49:
                        case 50:
                        case 51:
                        case 52:
                        case 53:
                        case 54:
                        case 55:
                        case 56:
                        case 57:
                            _parseNegNumber = _parsePosNumber(_skipColon);
                            break;
                        default:
                            _parseNegNumber = _handleOddValue(_skipColon);
                            break;
                    }
                } else {
                    _parseNegNumber = JsonToken.START_OBJECT;
                }
                this._nextToken = _parseNegNumber;
                return _parseName;
            }
            _updateLocation();
            _nextTokenNotInObject(_skipWSOrEnd);
            return null;
        }
    }

    private final void _isNextTokenNameYes(int i) throws IOException {
        this._currToken = JsonToken.FIELD_NAME;
        _updateLocation();
        if (i == 34) {
            this._tokenIncomplete = true;
            this._nextToken = JsonToken.VALUE_STRING;
        } else if (i == 45) {
            this._nextToken = _parseNegNumber();
        } else if (i == 91) {
            this._nextToken = JsonToken.START_ARRAY;
        } else if (i == 102) {
            _matchToken("false", 1);
            this._nextToken = JsonToken.VALUE_FALSE;
        } else if (i == 110) {
            _matchToken("null", 1);
            this._nextToken = JsonToken.VALUE_NULL;
        } else if (i == 116) {
            _matchToken(ServerProtocol.DIALOG_RETURN_SCOPES_TRUE, 1);
            this._nextToken = JsonToken.VALUE_TRUE;
        } else if (i != 123) {
            switch (i) {
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                    this._nextToken = _parsePosNumber(i);
                    return;
                default:
                    this._nextToken = _handleOddValue(i);
                    return;
            }
        } else {
            this._nextToken = JsonToken.START_OBJECT;
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean _isNextTokenNameMaybe(int i, String str) throws IOException {
        String _parseName = i == 34 ? _parseName() : _handleOddName(i);
        this._parsingContext.setCurrentName(_parseName);
        this._currToken = JsonToken.FIELD_NAME;
        int _skipColon = _skipColon();
        _updateLocation();
        if (_skipColon == 34) {
            this._tokenIncomplete = true;
            this._nextToken = JsonToken.VALUE_STRING;
            return str.equals(_parseName);
        }
        JsonToken _parseNegNumber;
        if (_skipColon == 45) {
            _parseNegNumber = _parseNegNumber();
        } else if (_skipColon == 91) {
            _parseNegNumber = JsonToken.START_ARRAY;
        } else if (_skipColon == 102) {
            _matchFalse();
            _parseNegNumber = JsonToken.VALUE_FALSE;
        } else if (_skipColon == 110) {
            _matchNull();
            _parseNegNumber = JsonToken.VALUE_NULL;
        } else if (_skipColon == 116) {
            _matchTrue();
            _parseNegNumber = JsonToken.VALUE_TRUE;
        } else if (_skipColon != 123) {
            switch (_skipColon) {
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                    _parseNegNumber = _parsePosNumber(_skipColon);
                    break;
                default:
                    _parseNegNumber = _handleOddValue(_skipColon);
                    break;
            }
        } else {
            _parseNegNumber = JsonToken.START_OBJECT;
        }
        this._nextToken = _parseNegNumber;
        return str.equals(_parseName);
    }

    private final JsonToken _nextTokenNotInObject(int i) throws IOException {
        JsonToken jsonToken;
        if (i == 34) {
            this._tokenIncomplete = true;
            jsonToken = JsonToken.VALUE_STRING;
            this._currToken = jsonToken;
            return jsonToken;
        } else if (i != 91) {
            if (i != 93) {
                if (i == 102) {
                    _matchToken("false", 1);
                    jsonToken = JsonToken.VALUE_FALSE;
                    this._currToken = jsonToken;
                    return jsonToken;
                } else if (i == 110) {
                    _matchToken("null", 1);
                    jsonToken = JsonToken.VALUE_NULL;
                    this._currToken = jsonToken;
                    return jsonToken;
                } else if (i == 116) {
                    _matchToken(ServerProtocol.DIALOG_RETURN_SCOPES_TRUE, 1);
                    jsonToken = JsonToken.VALUE_TRUE;
                    this._currToken = jsonToken;
                    return jsonToken;
                } else if (i != 123) {
                    switch (i) {
                        case 44:
                            break;
                        case 45:
                            jsonToken = _parseNegNumber();
                            this._currToken = jsonToken;
                            return jsonToken;
                        default:
                            switch (i) {
                                case 48:
                                case 49:
                                case 50:
                                case 51:
                                case 52:
                                case 53:
                                case 54:
                                case 55:
                                case 56:
                                case 57:
                                    jsonToken = _parsePosNumber(i);
                                    this._currToken = jsonToken;
                                    return jsonToken;
                            }
                            break;
                    }
                } else {
                    this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
                    jsonToken = JsonToken.START_OBJECT;
                    this._currToken = jsonToken;
                    return jsonToken;
                }
            }
            if (isEnabled(Feature.ALLOW_MISSING_VALUES)) {
                this._inputPtr--;
                jsonToken = JsonToken.VALUE_NULL;
                this._currToken = jsonToken;
                return jsonToken;
            }
            jsonToken = _handleOddValue(i);
            this._currToken = jsonToken;
            return jsonToken;
        } else {
            this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
            jsonToken = JsonToken.START_ARRAY;
            this._currToken = jsonToken;
            return jsonToken;
        }
    }

    public final String nextTextValue() throws IOException {
        String str = null;
        if (this._currToken == JsonToken.FIELD_NAME) {
            this._nameCopied = false;
            JsonToken jsonToken = this._nextToken;
            this._nextToken = null;
            this._currToken = jsonToken;
            if (jsonToken == JsonToken.VALUE_STRING) {
                if (this._tokenIncomplete) {
                    this._tokenIncomplete = false;
                    _finishString();
                }
                return this._textBuffer.contentsAsString();
            }
            if (jsonToken == JsonToken.START_ARRAY) {
                this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
            } else if (jsonToken == JsonToken.START_OBJECT) {
                this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
            }
            return null;
        }
        if (nextToken() == JsonToken.VALUE_STRING) {
            str = getText();
        }
        return str;
    }

    public final int nextIntValue(int i) throws IOException {
        if (this._currToken == JsonToken.FIELD_NAME) {
            this._nameCopied = false;
            JsonToken jsonToken = this._nextToken;
            this._nextToken = null;
            this._currToken = jsonToken;
            if (jsonToken == JsonToken.VALUE_NUMBER_INT) {
                return getIntValue();
            }
            if (jsonToken == JsonToken.START_ARRAY) {
                this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
            } else if (jsonToken == JsonToken.START_OBJECT) {
                this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
            }
            return i;
        }
        if (nextToken() == JsonToken.VALUE_NUMBER_INT) {
            i = getIntValue();
        }
        return i;
    }

    public final long nextLongValue(long j) throws IOException {
        if (this._currToken == JsonToken.FIELD_NAME) {
            this._nameCopied = false;
            JsonToken jsonToken = this._nextToken;
            this._nextToken = null;
            this._currToken = jsonToken;
            if (jsonToken == JsonToken.VALUE_NUMBER_INT) {
                return getLongValue();
            }
            if (jsonToken == JsonToken.START_ARRAY) {
                this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
            } else if (jsonToken == JsonToken.START_OBJECT) {
                this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
            }
            return j;
        }
        if (nextToken() == JsonToken.VALUE_NUMBER_INT) {
            j = getLongValue();
        }
        return j;
    }

    public final Boolean nextBooleanValue() throws IOException {
        JsonToken jsonToken;
        if (this._currToken == JsonToken.FIELD_NAME) {
            this._nameCopied = false;
            jsonToken = this._nextToken;
            this._nextToken = null;
            this._currToken = jsonToken;
            if (jsonToken == JsonToken.VALUE_TRUE) {
                return Boolean.TRUE;
            }
            if (jsonToken == JsonToken.VALUE_FALSE) {
                return Boolean.FALSE;
            }
            if (jsonToken == JsonToken.START_ARRAY) {
                this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
            } else if (jsonToken == JsonToken.START_OBJECT) {
                this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
            }
            return null;
        }
        jsonToken = nextToken();
        if (jsonToken != null) {
            int id = jsonToken.id();
            if (id == 9) {
                return Boolean.TRUE;
            }
            if (id == 10) {
                return Boolean.FALSE;
            }
        }
        return null;
    }

    /* Access modifiers changed, original: protected|final */
    public final JsonToken _parsePosNumber(int i) throws IOException {
        int i2 = this._inputPtr;
        int i3 = i2 - 1;
        int i4 = this._inputEnd;
        if (i == 48) {
            return _parseNumber2(false, i3);
        }
        int i5 = 1;
        while (i2 < i4) {
            int i6 = i2 + 1;
            char c = this._inputBuffer[i2];
            if (c >= '0' && c <= '9') {
                i5++;
                i2 = i6;
            } else if (c == '.' || c == 'e' || c == 'E') {
                this._inputPtr = i6;
                return _parseFloat(c, i3, i6, false, i5);
            } else {
                i6--;
                this._inputPtr = i6;
                if (this._parsingContext.inRoot()) {
                    _verifyRootSpace(c);
                }
                this._textBuffer.resetWithShared(this._inputBuffer, i3, i6 - i3);
                return resetInt(false, i5);
            }
        }
        this._inputPtr = i3;
        return _parseNumber2(false, i3);
    }

    /* JADX WARNING: Removed duplicated region for block: B:38:0x0077  */
    private final com.fasterxml.jackson.core.JsonToken _parseFloat(int r7, int r8, int r9, boolean r10, int r11) throws java.io.IOException {
        /*
        r6 = this;
        r0 = r6._inputEnd;
        r1 = 57;
        r2 = 48;
        r3 = 0;
        r4 = 46;
        if (r7 != r4) goto L_0x002d;
    L_0x000b:
        r7 = 0;
    L_0x000c:
        if (r9 < r0) goto L_0x0013;
    L_0x000e:
        r7 = r6._parseNumber2(r10, r8);
        return r7;
    L_0x0013:
        r4 = r6._inputBuffer;
        r5 = r9 + 1;
        r9 = r4[r9];
        if (r9 < r2) goto L_0x0022;
    L_0x001b:
        if (r9 <= r1) goto L_0x001e;
    L_0x001d:
        goto L_0x0022;
    L_0x001e:
        r7 = r7 + 1;
        r9 = r5;
        goto L_0x000c;
    L_0x0022:
        if (r7 != 0) goto L_0x0029;
    L_0x0024:
        r4 = "Decimal point not followed by a digit";
        r6.reportUnexpectedNumberChar(r9, r4);
    L_0x0029:
        r4 = r7;
        r7 = r9;
        r9 = r5;
        goto L_0x002e;
    L_0x002d:
        r4 = 0;
    L_0x002e:
        r5 = 101; // 0x65 float:1.42E-43 double:5.0E-322;
        if (r7 == r5) goto L_0x0036;
    L_0x0032:
        r5 = 69;
        if (r7 != r5) goto L_0x007c;
    L_0x0036:
        if (r9 < r0) goto L_0x003f;
    L_0x0038:
        r6._inputPtr = r8;
        r7 = r6._parseNumber2(r10, r8);
        return r7;
    L_0x003f:
        r7 = r6._inputBuffer;
        r5 = r9 + 1;
        r7 = r7[r9];
        r9 = 45;
        if (r7 == r9) goto L_0x0050;
    L_0x0049:
        r9 = 43;
        if (r7 != r9) goto L_0x004e;
    L_0x004d:
        goto L_0x0050;
    L_0x004e:
        r9 = r5;
        goto L_0x005f;
    L_0x0050:
        if (r5 < r0) goto L_0x0059;
    L_0x0052:
        r6._inputPtr = r8;
        r7 = r6._parseNumber2(r10, r8);
        return r7;
    L_0x0059:
        r7 = r6._inputBuffer;
        r9 = r5 + 1;
        r7 = r7[r5];
    L_0x005f:
        if (r7 > r1) goto L_0x0075;
    L_0x0061:
        if (r7 < r2) goto L_0x0075;
    L_0x0063:
        r3 = r3 + 1;
        if (r9 < r0) goto L_0x006e;
    L_0x0067:
        r6._inputPtr = r8;
        r7 = r6._parseNumber2(r10, r8);
        return r7;
    L_0x006e:
        r7 = r6._inputBuffer;
        r5 = r9 + 1;
        r7 = r7[r9];
        goto L_0x004e;
    L_0x0075:
        if (r3 != 0) goto L_0x007c;
    L_0x0077:
        r0 = "Exponent indicator not followed by a digit";
        r6.reportUnexpectedNumberChar(r7, r0);
    L_0x007c:
        r9 = r9 + -1;
        r6._inputPtr = r9;
        r0 = r6._parsingContext;
        r0 = r0.inRoot();
        if (r0 == 0) goto L_0x008b;
    L_0x0088:
        r6._verifyRootSpace(r7);
    L_0x008b:
        r9 = r9 - r8;
        r7 = r6._textBuffer;
        r0 = r6._inputBuffer;
        r7.resetWithShared(r0, r8, r9);
        r7 = r6.resetFloat(r10, r11, r4, r3);
        return r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.ReaderBasedJsonParser._parseFloat(int, int, int, boolean, int):com.fasterxml.jackson.core.JsonToken");
    }

    /* Access modifiers changed, original: protected|final */
    public final JsonToken _parseNegNumber() throws IOException {
        int i = this._inputPtr;
        int i2 = i - 1;
        int i3 = this._inputEnd;
        if (i >= i3) {
            return _parseNumber2(true, i2);
        }
        int i4 = i + 1;
        char c = this._inputBuffer[i];
        if (c > '9' || c < '0') {
            this._inputPtr = i4;
            return _handleInvalidNumberStart(c, true);
        } else if (c == '0') {
            return _parseNumber2(true, i2);
        } else {
            i = 1;
            while (i4 < i3) {
                int i5 = i4 + 1;
                char c2 = this._inputBuffer[i4];
                if (c2 >= '0' && c2 <= '9') {
                    i++;
                    i4 = i5;
                } else if (c2 == '.' || c2 == 'e' || c2 == 'E') {
                    this._inputPtr = i5;
                    return _parseFloat(c2, i2, i5, true, i);
                } else {
                    i5--;
                    this._inputPtr = i5;
                    if (this._parsingContext.inRoot()) {
                        _verifyRootSpace(c2);
                    }
                    this._textBuffer.resetWithShared(this._inputBuffer, i2, i5 - i2);
                    return resetInt(true, i);
                }
            }
            return _parseNumber2(true, i2);
        }
    }

    private final JsonToken _parseNumber2(boolean z, int i) throws IOException {
        int i2;
        char[] cArr;
        int i3;
        Object obj;
        char[] cArr2;
        if (z) {
            i++;
        }
        this._inputPtr = i;
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int i4 = 0;
        if (z) {
            emptyAndGetCurrentSegment[0] = '-';
            i2 = 1;
        } else {
            i2 = 0;
        }
        if (this._inputPtr < this._inputEnd) {
            cArr = this._inputBuffer;
            int i5 = this._inputPtr;
            this._inputPtr = i5 + 1;
            i3 = cArr[i5];
        } else {
            i3 = getNextChar("No digit following minus sign", JsonToken.VALUE_NUMBER_INT);
        }
        if (i3 == '0') {
            i3 = _verifyNoLeadingZeroes();
        }
        char[] cArr3 = emptyAndGetCurrentSegment;
        i = 0;
        while (i3 >= '0' && i3 <= '9') {
            i++;
            if (i2 >= cArr3.length) {
                cArr3 = this._textBuffer.finishCurrentSegment();
                i2 = 0;
            }
            int i6 = i2 + 1;
            cArr3[i2] = i3;
            if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                i2 = i6;
                i3 = 0;
                obj = 1;
                break;
            }
            cArr2 = this._inputBuffer;
            i3 = this._inputPtr;
            this._inputPtr = i3 + 1;
            i3 = cArr2[i3];
            i2 = i6;
        }
        obj = null;
        if (i == 0) {
            return _handleInvalidNumberStart(i3, z);
        }
        int i7;
        int i8;
        if (i3 == '.') {
            if (i2 >= cArr3.length) {
                cArr3 = this._textBuffer.finishCurrentSegment();
                i2 = 0;
            }
            i7 = i2 + 1;
            cArr3[i2] = i3;
            i2 = 0;
            while (true) {
                if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                    obj = 1;
                    break;
                }
                cArr = this._inputBuffer;
                i8 = this._inputPtr;
                this._inputPtr = i8 + 1;
                i3 = cArr[i8];
                if (i3 < '0' || i3 > '9') {
                    break;
                }
                i2++;
                if (i7 >= cArr3.length) {
                    cArr3 = this._textBuffer.finishCurrentSegment();
                    i7 = 0;
                }
                i8 = i7 + 1;
                cArr3[i7] = i3;
                i7 = i8;
            }
            if (i2 == 0) {
                reportUnexpectedNumberChar(i3, "Decimal point not followed by a digit");
            }
            int i9 = i7;
            i7 = i2;
            i2 = i9;
        } else {
            i7 = 0;
        }
        if (i3 == 101 || i3 == 69) {
            char c;
            int i10;
            if (i2 >= cArr3.length) {
                cArr3 = this._textBuffer.finishCurrentSegment();
                i2 = 0;
            }
            i8 = i2 + 1;
            cArr3[i2] = i3;
            if (this._inputPtr < this._inputEnd) {
                cArr2 = this._inputBuffer;
                i3 = this._inputPtr;
                this._inputPtr = i3 + 1;
                c = cArr2[i3];
            } else {
                c = getNextChar("expected a digit for number exponent");
            }
            if (c == '-' || c == '+') {
                if (i8 >= cArr3.length) {
                    cArr3 = this._textBuffer.finishCurrentSegment();
                    i8 = 0;
                }
                i10 = i8 + 1;
                cArr3[i8] = c;
                if (this._inputPtr < this._inputEnd) {
                    cArr2 = this._inputBuffer;
                    i3 = this._inputPtr;
                    this._inputPtr = i3 + 1;
                    c = cArr2[i3];
                } else {
                    c = getNextChar("expected a digit for number exponent");
                }
                i8 = i10;
            }
            i3 = c;
            i10 = 0;
            while (i3 <= 57 && i3 >= 48) {
                i10++;
                if (i8 >= cArr3.length) {
                    cArr3 = this._textBuffer.finishCurrentSegment();
                    i8 = 0;
                }
                i2 = i8 + 1;
                cArr3[i8] = i3;
                if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                    i4 = i10;
                    obj = 1;
                    break;
                }
                cArr = this._inputBuffer;
                i8 = this._inputPtr;
                this._inputPtr = i8 + 1;
                i3 = cArr[i8];
                i8 = i2;
            }
            i4 = i10;
            i2 = i8;
            if (i4 == 0) {
                reportUnexpectedNumberChar(i3, "Exponent indicator not followed by a digit");
            }
        }
        if (obj == null) {
            this._inputPtr--;
            if (this._parsingContext.inRoot()) {
                _verifyRootSpace(i3);
            }
        }
        this._textBuffer.setCurrentLength(i2);
        return reset(z, i, i7, i4);
    }

    private final char _verifyNoLeadingZeroes() throws IOException {
        if (this._inputPtr < this._inputEnd) {
            char c = this._inputBuffer[this._inputPtr];
            if (c < '0' || c > '9') {
                return '0';
            }
        }
        return _verifyNLZ2();
    }

    private char _verifyNLZ2() throws IOException {
        if (this._inputPtr >= this._inputEnd && !_loadMore()) {
            return '0';
        }
        char c = this._inputBuffer[this._inputPtr];
        if (c < '0' || c > '9') {
            return '0';
        }
        if (!isEnabled(Feature.ALLOW_NUMERIC_LEADING_ZEROS)) {
            reportInvalidNumber("Leading zeroes not allowed");
        }
        this._inputPtr++;
        if (c == '0') {
            do {
                if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                    break;
                }
                c = this._inputBuffer[this._inputPtr];
                if (c < '0' || c > '9') {
                    return '0';
                }
                this._inputPtr++;
            } while (c == '0');
        }
        return c;
    }

    /* Access modifiers changed, original: protected */
    public JsonToken _handleInvalidNumberStart(int i, boolean z) throws IOException {
        if (i == 73) {
            if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                _reportInvalidEOFInValue(JsonToken.VALUE_NUMBER_INT);
            }
            char[] cArr = this._inputBuffer;
            int i2 = this._inputPtr;
            this._inputPtr = i2 + 1;
            i = cArr[i2];
            double d = Double.POSITIVE_INFINITY;
            String str;
            StringBuilder stringBuilder;
            if (i == 78) {
                str = z ? "-INF" : "+INF";
                _matchToken(str, 3);
                if (isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                    if (z) {
                        d = Double.NEGATIVE_INFINITY;
                    }
                    return resetAsNaN(str, d);
                }
                stringBuilder = new StringBuilder();
                stringBuilder.append("Non-standard token '");
                stringBuilder.append(str);
                stringBuilder.append("': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                _reportError(stringBuilder.toString());
            } else if (i == 110) {
                str = z ? "-Infinity" : "+Infinity";
                _matchToken(str, 3);
                if (isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                    if (z) {
                        d = Double.NEGATIVE_INFINITY;
                    }
                    return resetAsNaN(str, d);
                }
                stringBuilder = new StringBuilder();
                stringBuilder.append("Non-standard token '");
                stringBuilder.append(str);
                stringBuilder.append("': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                _reportError(stringBuilder.toString());
            }
        }
        reportUnexpectedNumberChar(i, "expected digit (0-9) to follow minus sign, for valid numeric value");
        return null;
    }

    private final void _verifyRootSpace(int i) throws IOException {
        this._inputPtr++;
        if (i != 13) {
            if (i != 32) {
                switch (i) {
                    case 9:
                        break;
                    case 10:
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                        return;
                    default:
                        _reportMissingRootWS(i);
                        return;
                }
            }
            return;
        }
        _skipCR();
    }

    /* Access modifiers changed, original: protected|final */
    public final String _parseName() throws IOException {
        int i;
        int i2 = this._inputPtr;
        int i3 = this._hashSeed;
        int[] iArr = _icLatin1;
        while (i2 < this._inputEnd) {
            char c = this._inputBuffer[i2];
            if (c >= iArr.length || iArr[c] == 0) {
                i3 = (i3 * 33) + c;
                i2++;
            } else {
                if (c == '\"') {
                    i = this._inputPtr;
                    this._inputPtr = i2 + 1;
                    return this._symbols.findSymbol(this._inputBuffer, i, i2 - i, i3);
                }
                i = this._inputPtr;
                this._inputPtr = i2;
                return _parseName2(i, i3, 34);
            }
        }
        i = this._inputPtr;
        this._inputPtr = i2;
        return _parseName2(i, i3, 34);
    }

    private String _parseName2(int i, int i2, int i3) throws IOException {
        this._textBuffer.resetWithShared(this._inputBuffer, i, this._inputPtr - i);
        char[] currentSegment = this._textBuffer.getCurrentSegment();
        int currentSegmentSize = this._textBuffer.getCurrentSegmentSize();
        while (true) {
            if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                _reportInvalidEOF(" in field name", JsonToken.FIELD_NAME);
            }
            char[] cArr = this._inputBuffer;
            int i4 = this._inputPtr;
            this._inputPtr = i4 + 1;
            int i5 = cArr[i4];
            if (i5 <= 92) {
                if (i5 == 92) {
                    i5 = _decodeEscaped();
                } else if (i5 <= i3) {
                    if (i5 == i3) {
                        this._textBuffer.setCurrentLength(currentSegmentSize);
                        TextBuffer textBuffer = this._textBuffer;
                        return this._symbols.findSymbol(textBuffer.getTextBuffer(), textBuffer.getTextOffset(), textBuffer.size(), i2);
                    } else if (i5 < 32) {
                        _throwUnquotedSpace(i5, "name");
                    }
                }
            }
            i2 = (i2 * 33) + i5;
            i4 = currentSegmentSize + 1;
            currentSegment[currentSegmentSize] = i5;
            if (i4 >= currentSegment.length) {
                currentSegment = this._textBuffer.finishCurrentSegment();
                currentSegmentSize = 0;
            } else {
                currentSegmentSize = i4;
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public String _handleOddName(int i) throws IOException {
        if (i == 39 && isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
            return _parseAposName();
        }
        if (!isEnabled(Feature.ALLOW_UNQUOTED_FIELD_NAMES)) {
            _reportUnexpectedChar(i, "was expecting double-quote to start field name");
        }
        int[] inputCodeLatin1JsNames = CharTypes.getInputCodeLatin1JsNames();
        char length = inputCodeLatin1JsNames.length;
        boolean isJavaIdentifierPart = i < length ? inputCodeLatin1JsNames[i] == 0 : Character.isJavaIdentifierPart((char) i);
        if (!isJavaIdentifierPart) {
            _reportUnexpectedChar(i, "was expecting either valid name character (for unquoted name) or double-quote (for quoted) to start field name");
        }
        i = this._inputPtr;
        int i2 = this._hashSeed;
        int i3 = this._inputEnd;
        if (i < i3) {
            do {
                char c = this._inputBuffer[i];
                int i4;
                if (c < length) {
                    if (inputCodeLatin1JsNames[c] != 0) {
                        i4 = this._inputPtr - 1;
                        this._inputPtr = i;
                        return this._symbols.findSymbol(this._inputBuffer, i4, i - i4, i2);
                    }
                } else if (!Character.isJavaIdentifierPart((char) c)) {
                    i4 = this._inputPtr - 1;
                    this._inputPtr = i;
                    return this._symbols.findSymbol(this._inputBuffer, i4, i - i4, i2);
                }
                i2 = (i2 * 33) + c;
                i++;
            } while (i < i3);
        }
        int i5 = this._inputPtr - 1;
        this._inputPtr = i;
        return _handleOddName2(i5, i2, inputCodeLatin1JsNames);
    }

    /* Access modifiers changed, original: protected */
    public String _parseAposName() throws IOException {
        int i = this._inputPtr;
        int i2 = this._hashSeed;
        int i3 = this._inputEnd;
        if (i < i3) {
            int[] iArr = _icLatin1;
            char length = iArr.length;
            do {
                char c = this._inputBuffer[i];
                if (c != '\'') {
                    if (c < length && iArr[c] != 0) {
                        break;
                    }
                    i2 = (i2 * 33) + c;
                    i++;
                } else {
                    i3 = this._inputPtr;
                    this._inputPtr = i + 1;
                    return this._symbols.findSymbol(this._inputBuffer, i3, i - i3, i2);
                }
            } while (i < i3);
        }
        i3 = this._inputPtr;
        this._inputPtr = i;
        return _parseName2(i3, i2, 39);
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Missing block: B:17:0x003d, code skipped:
            if (r2._parsingContext.inArray() == false) goto L_0x0095;
     */
    public com.fasterxml.jackson.core.JsonToken _handleOddValue(int r3) throws java.io.IOException {
        /*
        r2 = this;
        r0 = 39;
        if (r3 == r0) goto L_0x0088;
    L_0x0004:
        r0 = 73;
        r1 = 1;
        if (r3 == r0) goto L_0x006c;
    L_0x0009:
        r0 = 78;
        if (r3 == r0) goto L_0x0050;
    L_0x000d:
        r0 = 93;
        if (r3 == r0) goto L_0x0037;
    L_0x0011:
        switch(r3) {
            case 43: goto L_0x0016;
            case 44: goto L_0x0040;
            default: goto L_0x0014;
        };
    L_0x0014:
        goto L_0x0095;
    L_0x0016:
        r3 = r2._inputPtr;
        r0 = r2._inputEnd;
        if (r3 < r0) goto L_0x0027;
    L_0x001c:
        r3 = r2._loadMore();
        if (r3 != 0) goto L_0x0027;
    L_0x0022:
        r3 = com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT;
        r2._reportInvalidEOFInValue(r3);
    L_0x0027:
        r3 = r2._inputBuffer;
        r0 = r2._inputPtr;
        r1 = r0 + 1;
        r2._inputPtr = r1;
        r3 = r3[r0];
        r0 = 0;
        r3 = r2._handleInvalidNumberStart(r3, r0);
        return r3;
    L_0x0037:
        r0 = r2._parsingContext;
        r0 = r0.inArray();
        if (r0 != 0) goto L_0x0040;
    L_0x003f:
        goto L_0x0095;
    L_0x0040:
        r0 = com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_MISSING_VALUES;
        r0 = r2.isEnabled(r0);
        if (r0 == 0) goto L_0x0095;
    L_0x0048:
        r3 = r2._inputPtr;
        r3 = r3 - r1;
        r2._inputPtr = r3;
        r3 = com.fasterxml.jackson.core.JsonToken.VALUE_NULL;
        return r3;
    L_0x0050:
        r0 = "NaN";
        r2._matchToken(r0, r1);
        r0 = com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS;
        r0 = r2.isEnabled(r0);
        if (r0 == 0) goto L_0x0066;
    L_0x005d:
        r3 = "NaN";
        r0 = 9221120237041090560; // 0x7ff8000000000000 float:0.0 double:NaN;
        r3 = r2.resetAsNaN(r3, r0);
        return r3;
    L_0x0066:
        r0 = "Non-standard token 'NaN': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow";
        r2._reportError(r0);
        goto L_0x0095;
    L_0x006c:
        r0 = "Infinity";
        r2._matchToken(r0, r1);
        r0 = com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS;
        r0 = r2.isEnabled(r0);
        if (r0 == 0) goto L_0x0082;
    L_0x0079:
        r3 = "Infinity";
        r0 = 9218868437227405312; // 0x7ff0000000000000 float:0.0 double:Infinity;
        r3 = r2.resetAsNaN(r3, r0);
        return r3;
    L_0x0082:
        r0 = "Non-standard token 'Infinity': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow";
        r2._reportError(r0);
        goto L_0x0095;
    L_0x0088:
        r0 = com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
        r0 = r2.isEnabled(r0);
        if (r0 == 0) goto L_0x0095;
    L_0x0090:
        r3 = r2._handleApos();
        return r3;
    L_0x0095:
        r0 = java.lang.Character.isJavaIdentifierStart(r3);
        if (r0 == 0) goto L_0x00b2;
    L_0x009b:
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r1 = "";
        r0.append(r1);
        r1 = (char) r3;
        r0.append(r1);
        r0 = r0.toString();
        r1 = "('true', 'false' or 'null')";
        r2._reportInvalidToken(r0, r1);
    L_0x00b2:
        r0 = "expected a valid value (number, String, array, object, 'true', 'false' or 'null')";
        r2._reportUnexpectedChar(r3, r0);
        r3 = 0;
        return r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.ReaderBasedJsonParser._handleOddValue(int):com.fasterxml.jackson.core.JsonToken");
    }

    /* Access modifiers changed, original: protected */
    public JsonToken _handleApos() throws IOException {
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int currentSegmentSize = this._textBuffer.getCurrentSegmentSize();
        while (true) {
            if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                _reportInvalidEOF(": was expecting closing quote for a string value", JsonToken.VALUE_STRING);
            }
            char[] cArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            char c = cArr[i];
            if (c <= '\\') {
                if (c == '\\') {
                    c = _decodeEscaped();
                } else if (c <= '\'') {
                    if (c == '\'') {
                        this._textBuffer.setCurrentLength(currentSegmentSize);
                        return JsonToken.VALUE_STRING;
                    } else if (c < ' ') {
                        _throwUnquotedSpace(c, "string value");
                    }
                }
            }
            if (currentSegmentSize >= emptyAndGetCurrentSegment.length) {
                emptyAndGetCurrentSegment = this._textBuffer.finishCurrentSegment();
                currentSegmentSize = 0;
            }
            i = currentSegmentSize + 1;
            emptyAndGetCurrentSegment[currentSegmentSize] = c;
            currentSegmentSize = i;
        }
    }

    private String _handleOddName2(int i, int i2, int[] iArr) throws IOException {
        this._textBuffer.resetWithShared(this._inputBuffer, i, this._inputPtr - i);
        char[] currentSegment = this._textBuffer.getCurrentSegment();
        int currentSegmentSize = this._textBuffer.getCurrentSegmentSize();
        char length = iArr.length;
        while (true) {
            if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                break;
            }
            char c = this._inputBuffer[this._inputPtr];
            if (c > length) {
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
            } else if (iArr[c] != 0) {
                break;
            }
            this._inputPtr++;
            i2 = (i2 * 33) + c;
            int i3 = currentSegmentSize + 1;
            currentSegment[currentSegmentSize] = c;
            if (i3 >= currentSegment.length) {
                currentSegment = this._textBuffer.finishCurrentSegment();
                currentSegmentSize = 0;
            } else {
                currentSegmentSize = i3;
            }
        }
        this._textBuffer.setCurrentLength(currentSegmentSize);
        TextBuffer textBuffer = this._textBuffer;
        return this._symbols.findSymbol(textBuffer.getTextBuffer(), textBuffer.getTextOffset(), textBuffer.size(), i2);
    }

    /* Access modifiers changed, original: protected|final */
    public final void _finishString() throws IOException {
        int i = this._inputPtr;
        int i2 = this._inputEnd;
        if (i < i2) {
            int[] iArr = _icLatin1;
            char length = iArr.length;
            do {
                char c = this._inputBuffer[i];
                if (c >= length || iArr[c] == 0) {
                    i++;
                } else if (c == '\"') {
                    this._textBuffer.resetWithShared(this._inputBuffer, this._inputPtr, i - this._inputPtr);
                    this._inputPtr = i + 1;
                    return;
                }
            } while (i < i2);
        }
        this._textBuffer.resetWithCopy(this._inputBuffer, this._inputPtr, i - this._inputPtr);
        this._inputPtr = i;
        _finishString2();
    }

    /* Access modifiers changed, original: protected */
    public void _finishString2() throws IOException {
        char[] currentSegment = this._textBuffer.getCurrentSegment();
        int currentSegmentSize = this._textBuffer.getCurrentSegmentSize();
        int[] iArr = _icLatin1;
        char length = iArr.length;
        while (true) {
            if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                _reportInvalidEOF(": was expecting closing quote for a string value", JsonToken.VALUE_STRING);
            }
            char[] cArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            char c = cArr[i];
            if (c < length && iArr[c] != 0) {
                if (c == '\"') {
                    this._textBuffer.setCurrentLength(currentSegmentSize);
                    return;
                } else if (c == '\\') {
                    c = _decodeEscaped();
                } else if (c < ' ') {
                    _throwUnquotedSpace(c, "string value");
                }
            }
            if (currentSegmentSize >= currentSegment.length) {
                currentSegment = this._textBuffer.finishCurrentSegment();
                currentSegmentSize = 0;
            }
            i = currentSegmentSize + 1;
            currentSegment[currentSegmentSize] = c;
            currentSegmentSize = i;
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final void _skipString() throws IOException {
        this._tokenIncomplete = false;
        int i = this._inputPtr;
        int i2 = this._inputEnd;
        char[] cArr = this._inputBuffer;
        while (true) {
            if (i >= i2) {
                this._inputPtr = i;
                if (!_loadMore()) {
                    _reportInvalidEOF(": was expecting closing quote for a string value", JsonToken.VALUE_STRING);
                }
                i = this._inputPtr;
                i2 = this._inputEnd;
            }
            int i3 = i + 1;
            char c = cArr[i];
            if (c <= '\\') {
                if (c == '\\') {
                    this._inputPtr = i3;
                    _decodeEscaped();
                    i = this._inputPtr;
                    i2 = this._inputEnd;
                } else if (c <= '\"') {
                    if (c == '\"') {
                        this._inputPtr = i3;
                        return;
                    } else if (c < ' ') {
                        this._inputPtr = i3;
                        _throwUnquotedSpace(c, "string value");
                    }
                }
            }
            i = i3;
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final void _skipCR() throws IOException {
        if ((this._inputPtr < this._inputEnd || _loadMore()) && this._inputBuffer[this._inputPtr] == 10) {
            this._inputPtr++;
        }
        this._currInputRow++;
        this._currInputRowStart = this._inputPtr;
    }

    private final int _skipColon() throws IOException {
        if (this._inputPtr + 4 >= this._inputEnd) {
            return _skipColon2(false);
        }
        char c = this._inputBuffer[this._inputPtr];
        char[] cArr;
        int i;
        if (c == ':') {
            cArr = this._inputBuffer;
            i = this._inputPtr + 1;
            this._inputPtr = i;
            c = cArr[i];
            if (c <= ' ') {
                if (c == ' ' || c == 9) {
                    cArr = this._inputBuffer;
                    i = this._inputPtr + 1;
                    this._inputPtr = i;
                    c = cArr[i];
                    if (c > ' ') {
                        if (c == '/' || c == '#') {
                            return _skipColon2(true);
                        }
                        this._inputPtr++;
                        return c;
                    }
                }
                return _skipColon2(true);
            } else if (c == '/' || c == '#') {
                return _skipColon2(true);
            } else {
                this._inputPtr++;
                return c;
            }
        }
        if (c == ' ' || c == 9) {
            cArr = this._inputBuffer;
            int i2 = this._inputPtr + 1;
            this._inputPtr = i2;
            c = cArr[i2];
        }
        if (c != ':') {
            return _skipColon2(false);
        }
        cArr = this._inputBuffer;
        i = this._inputPtr + 1;
        this._inputPtr = i;
        c = cArr[i];
        if (c <= ' ') {
            if (c == ' ' || c == 9) {
                cArr = this._inputBuffer;
                i = this._inputPtr + 1;
                this._inputPtr = i;
                c = cArr[i];
                if (c > ' ') {
                    if (c == '/' || c == '#') {
                        return _skipColon2(true);
                    }
                    this._inputPtr++;
                    return c;
                }
            }
            return _skipColon2(true);
        } else if (c == '/' || c == '#') {
            return _skipColon2(true);
        } else {
            this._inputPtr++;
            return c;
        }
    }

    private final int _skipColon2(boolean z) throws IOException {
        while (true) {
            if (this._inputPtr < this._inputEnd || _loadMore()) {
                char[] cArr = this._inputBuffer;
                int i = this._inputPtr;
                this._inputPtr = i + 1;
                char c = cArr[i];
                if (c > ' ') {
                    if (c == '/') {
                        _skipComment();
                    } else if (c != '#' || !_skipYAMLComment()) {
                        if (z) {
                            return c;
                        }
                        if (c != ':') {
                            _reportUnexpectedChar(c, "was expecting a colon to separate field name and value");
                        }
                        z = true;
                    }
                } else if (c < ' ') {
                    if (c == 10) {
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                    } else if (c == 13) {
                        _skipCR();
                    } else if (c != 9) {
                        _throwInvalidSpace(c);
                    }
                }
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(" within/between ");
                stringBuilder.append(this._parsingContext.typeDesc());
                stringBuilder.append(" entries");
                _reportInvalidEOF(stringBuilder.toString(), null);
                return -1;
            }
        }
    }

    private final int _skipColonFast(int i) throws IOException {
        int i2 = i + 1;
        char c = this._inputBuffer[i];
        if (c == ':') {
            int i3 = i2 + 1;
            c = this._inputBuffer[i2];
            if (c > ' ') {
                if (!(c == '/' || c == '#')) {
                    this._inputPtr = i3;
                    return c;
                }
            } else if (c == ' ' || c == 9) {
                i2 = i3 + 1;
                c = this._inputBuffer[i3];
                if (c <= ' ' || c == '/' || c == '#') {
                    i3 = i2;
                } else {
                    this._inputPtr = i2;
                    return c;
                }
            }
            this._inputPtr = i3 - 1;
            return _skipColon2(true);
        }
        int i4;
        if (c == ' ' || c == 9) {
            i4 = i2 + 1;
            c = this._inputBuffer[i2];
            i2 = i4;
        }
        boolean z = c == ':';
        if (z) {
            i4 = i2 + 1;
            char c2 = this._inputBuffer[i2];
            if (c2 > ' ') {
                if (!(c2 == '/' || c2 == '#')) {
                    this._inputPtr = i4;
                    return c2;
                }
            } else if (c2 == ' ' || c2 == 9) {
                i2 = i4 + 1;
                c2 = this._inputBuffer[i4];
                if (!(c2 <= ' ' || c2 == '/' || c2 == '#')) {
                    this._inputPtr = i2;
                    return c2;
                }
            }
            i2 = i4;
        }
        this._inputPtr = i2 - 1;
        return _skipColon2(z);
    }

    private final int _skipComma(int i) throws IOException {
        if (i != 44) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("was expecting comma to separate ");
            stringBuilder.append(this._parsingContext.typeDesc());
            stringBuilder.append(" entries");
            _reportUnexpectedChar(i, stringBuilder.toString());
        }
        while (this._inputPtr < this._inputEnd) {
            char[] cArr = this._inputBuffer;
            int i2 = this._inputPtr;
            this._inputPtr = i2 + 1;
            char c = cArr[i2];
            if (c > ' ') {
                if (c != '/' && c != '#') {
                    return c;
                }
                this._inputPtr--;
                return _skipAfterComma2();
            } else if (c < ' ') {
                if (c == 10) {
                    this._currInputRow++;
                    this._currInputRowStart = this._inputPtr;
                } else if (c == 13) {
                    _skipCR();
                } else if (c != 9) {
                    _throwInvalidSpace(c);
                }
            }
        }
        return _skipAfterComma2();
    }

    private final int _skipAfterComma2() throws IOException {
        char c;
        while (true) {
            if (this._inputPtr < this._inputEnd || _loadMore()) {
                char[] cArr = this._inputBuffer;
                int i = this._inputPtr;
                this._inputPtr = i + 1;
                c = cArr[i];
                if (c > ' ') {
                    if (c == '/') {
                        _skipComment();
                    } else if (c != '#' || !_skipYAMLComment()) {
                        return c;
                    }
                } else if (c < ' ') {
                    if (c == 10) {
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                    } else if (c == 13) {
                        _skipCR();
                    } else if (c != 9) {
                        _throwInvalidSpace(c);
                    }
                }
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unexpected end-of-input within/between ");
                stringBuilder.append(this._parsingContext.typeDesc());
                stringBuilder.append(" entries");
                throw _constructError(stringBuilder.toString());
            }
        }
        return c;
    }

    private final int _skipWSOrEnd() throws IOException {
        if (this._inputPtr >= this._inputEnd && !_loadMore()) {
            return _eofAsNextChar();
        }
        char[] cArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        char c = cArr[i];
        if (c <= ' ') {
            if (c != ' ') {
                if (c == 10) {
                    this._currInputRow++;
                    this._currInputRowStart = this._inputPtr;
                } else if (c == 13) {
                    _skipCR();
                } else if (c != 9) {
                    _throwInvalidSpace(c);
                }
            }
            while (this._inputPtr < this._inputEnd) {
                cArr = this._inputBuffer;
                int i2 = this._inputPtr;
                this._inputPtr = i2 + 1;
                c = cArr[i2];
                if (c > ' ') {
                    if (c != '/' && c != '#') {
                        return c;
                    }
                    this._inputPtr--;
                    return _skipWSOrEnd2();
                } else if (c != ' ') {
                    if (c == 10) {
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                    } else if (c == 13) {
                        _skipCR();
                    } else if (c != 9) {
                        _throwInvalidSpace(c);
                    }
                }
            }
            return _skipWSOrEnd2();
        } else if (c != '/' && c != '#') {
            return c;
        } else {
            this._inputPtr--;
            return _skipWSOrEnd2();
        }
    }

    private int _skipWSOrEnd2() throws IOException {
        char c;
        while (true) {
            if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                return _eofAsNextChar();
            }
            char[] cArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            c = cArr[i];
            if (c > ' ') {
                if (c == '/') {
                    _skipComment();
                } else if (c != '#' || !_skipYAMLComment()) {
                    return c;
                }
            } else if (c != ' ') {
                if (c == 10) {
                    this._currInputRow++;
                    this._currInputRowStart = this._inputPtr;
                } else if (c == 13) {
                    _skipCR();
                } else if (c != 9) {
                    _throwInvalidSpace(c);
                }
            }
        }
        return c;
    }

    private void _skipComment() throws IOException {
        if (!isEnabled(Feature.ALLOW_COMMENTS)) {
            _reportUnexpectedChar(47, "maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)");
        }
        if (this._inputPtr >= this._inputEnd && !_loadMore()) {
            _reportInvalidEOF(" in a comment", null);
        }
        char[] cArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        char c = cArr[i];
        if (c == '/') {
            _skipLine();
        } else if (c == '*') {
            _skipCComment();
        } else {
            _reportUnexpectedChar(c, "was expecting either '*' or '/' for a comment");
        }
    }

    private void _skipCComment() throws IOException {
        while (true) {
            if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                break;
            }
            char[] cArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            char c = cArr[i];
            if (c <= '*') {
                if (c == '*') {
                    if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                        break;
                    } else if (this._inputBuffer[this._inputPtr] == '/') {
                        this._inputPtr++;
                        return;
                    }
                } else if (c < ' ') {
                    if (c == 10) {
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                    } else if (c == 13) {
                        _skipCR();
                    } else if (c != 9) {
                        _throwInvalidSpace(c);
                    }
                }
            }
        }
        _reportInvalidEOF(" in a comment", null);
    }

    private boolean _skipYAMLComment() throws IOException {
        if (!isEnabled(Feature.ALLOW_YAML_COMMENTS)) {
            return false;
        }
        _skipLine();
        return true;
    }

    private void _skipLine() throws IOException {
        while (true) {
            if (this._inputPtr < this._inputEnd || _loadMore()) {
                char[] cArr = this._inputBuffer;
                int i = this._inputPtr;
                this._inputPtr = i + 1;
                char c = cArr[i];
                if (c < ' ') {
                    if (c == 10) {
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                        return;
                    } else if (c == 13) {
                        _skipCR();
                        return;
                    } else if (c != 9) {
                        _throwInvalidSpace(c);
                    }
                }
            } else {
                return;
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public char _decodeEscaped() throws IOException {
        if (this._inputPtr >= this._inputEnd && !_loadMore()) {
            _reportInvalidEOF(" in character escape sequence", JsonToken.VALUE_STRING);
        }
        char[] cArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        char c = cArr[i];
        if (c == '\"' || c == '/' || c == '\\') {
            return c;
        }
        if (c == 'b') {
            return 8;
        }
        if (c == 'f') {
            return 12;
        }
        if (c == 'n') {
            return 10;
        }
        if (c == 'r') {
            return 13;
        }
        switch (c) {
            case 't':
                return 9;
            case 'u':
                i = 0;
                for (int i2 = 0; i2 < 4; i2++) {
                    if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                        _reportInvalidEOF(" in character escape sequence", JsonToken.VALUE_STRING);
                    }
                    char[] cArr2 = this._inputBuffer;
                    int i3 = this._inputPtr;
                    this._inputPtr = i3 + 1;
                    char c2 = cArr2[i3];
                    i3 = CharTypes.charToHex(c2);
                    if (i3 < 0) {
                        _reportUnexpectedChar(c2, "expected a hex-digit for character escape sequence");
                    }
                    i = (i << 4) | i3;
                }
                return (char) i;
            default:
                return _handleUnrecognizedCharacterEscape(c);
        }
    }

    private final void _matchTrue() throws IOException {
        int i = this._inputPtr;
        if (i + 3 < this._inputEnd) {
            char[] cArr = this._inputBuffer;
            if (cArr[i] == 'r') {
                i++;
                if (cArr[i] == 'u') {
                    i++;
                    if (cArr[i] == 'e') {
                        i++;
                        char c = cArr[i];
                        if (c < '0' || c == ']' || c == '}') {
                            this._inputPtr = i;
                            return;
                        }
                    }
                }
            }
        }
        _matchToken(ServerProtocol.DIALOG_RETURN_SCOPES_TRUE, 1);
    }

    private final void _matchFalse() throws IOException {
        int i = this._inputPtr;
        if (i + 4 < this._inputEnd) {
            char[] cArr = this._inputBuffer;
            if (cArr[i] == 'a') {
                i++;
                if (cArr[i] == 'l') {
                    i++;
                    if (cArr[i] == 's') {
                        i++;
                        if (cArr[i] == 'e') {
                            i++;
                            char c = cArr[i];
                            if (c < '0' || c == ']' || c == '}') {
                                this._inputPtr = i;
                                return;
                            }
                        }
                    }
                }
            }
        }
        _matchToken("false", 1);
    }

    private final void _matchNull() throws IOException {
        int i = this._inputPtr;
        if (i + 3 < this._inputEnd) {
            char[] cArr = this._inputBuffer;
            if (cArr[i] == 'u') {
                i++;
                if (cArr[i] == 'l') {
                    i++;
                    if (cArr[i] == 'l') {
                        i++;
                        char c = cArr[i];
                        if (c < '0' || c == ']' || c == '}') {
                            this._inputPtr = i;
                            return;
                        }
                    }
                }
            }
        }
        _matchToken("null", 1);
    }

    /* Access modifiers changed, original: protected|final */
    /* JADX WARNING: Missing block: B:26:0x0063, code skipped:
            return;
     */
    public final void _matchToken(java.lang.String r5, int r6) throws java.io.IOException {
        /*
        r4 = this;
        r0 = r5.length();
    L_0x0004:
        r1 = r4._inputPtr;
        r2 = r4._inputEnd;
        r3 = 0;
        if (r1 < r2) goto L_0x0018;
    L_0x000b:
        r1 = r4._loadMore();
        if (r1 != 0) goto L_0x0018;
    L_0x0011:
        r1 = r5.substring(r3, r6);
        r4._reportInvalidToken(r1);
    L_0x0018:
        r1 = r4._inputBuffer;
        r2 = r4._inputPtr;
        r1 = r1[r2];
        r2 = r5.charAt(r6);
        if (r1 == r2) goto L_0x002b;
    L_0x0024:
        r1 = r5.substring(r3, r6);
        r4._reportInvalidToken(r1);
    L_0x002b:
        r1 = r4._inputPtr;
        r1 = r1 + 1;
        r4._inputPtr = r1;
        r6 = r6 + 1;
        if (r6 < r0) goto L_0x0004;
    L_0x0035:
        r0 = r4._inputPtr;
        r1 = r4._inputEnd;
        if (r0 < r1) goto L_0x0042;
    L_0x003b:
        r0 = r4._loadMore();
        if (r0 != 0) goto L_0x0042;
    L_0x0041:
        return;
    L_0x0042:
        r0 = r4._inputBuffer;
        r1 = r4._inputPtr;
        r0 = r0[r1];
        r1 = 48;
        if (r0 < r1) goto L_0x0063;
    L_0x004c:
        r1 = 93;
        if (r0 == r1) goto L_0x0063;
    L_0x0050:
        r1 = 125; // 0x7d float:1.75E-43 double:6.2E-322;
        if (r0 != r1) goto L_0x0055;
    L_0x0054:
        goto L_0x0063;
    L_0x0055:
        r0 = java.lang.Character.isJavaIdentifierPart(r0);
        if (r0 == 0) goto L_0x0062;
    L_0x005b:
        r5 = r5.substring(r3, r6);
        r4._reportInvalidToken(r5);
    L_0x0062:
        return;
    L_0x0063:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.ReaderBasedJsonParser._matchToken(java.lang.String, int):void");
    }

    /* Access modifiers changed, original: protected */
    public byte[] _decodeBase64(Base64Variant base64Variant) throws IOException {
        ByteArrayBuilder _getByteArrayBuilder = _getByteArrayBuilder();
        while (true) {
            if (this._inputPtr >= this._inputEnd) {
                _loadMoreGuaranteed();
            }
            char[] cArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            char c = cArr[i];
            if (c > ' ') {
                i = base64Variant.decodeBase64Char(c);
                if (i < 0) {
                    if (c == '\"') {
                        return _getByteArrayBuilder.toByteArray();
                    }
                    i = _decodeBase64Escape(base64Variant, c, 0);
                    if (i < 0) {
                    }
                }
                if (this._inputPtr >= this._inputEnd) {
                    _loadMoreGuaranteed();
                }
                cArr = this._inputBuffer;
                int i2 = this._inputPtr;
                this._inputPtr = i2 + 1;
                c = cArr[i2];
                i2 = base64Variant.decodeBase64Char(c);
                if (i2 < 0) {
                    i2 = _decodeBase64Escape(base64Variant, c, 1);
                }
                int i3 = (i << 6) | i2;
                if (this._inputPtr >= this._inputEnd) {
                    _loadMoreGuaranteed();
                }
                char[] cArr2 = this._inputBuffer;
                i2 = this._inputPtr;
                this._inputPtr = i2 + 1;
                char c2 = cArr2[i2];
                i2 = base64Variant.decodeBase64Char(c2);
                if (i2 < 0) {
                    if (i2 != -2) {
                        if (c2 != '\"' || base64Variant.usesPadding()) {
                            i2 = _decodeBase64Escape(base64Variant, c2, 2);
                        } else {
                            _getByteArrayBuilder.append(i3 >> 4);
                            return _getByteArrayBuilder.toByteArray();
                        }
                    }
                    if (i2 == -2) {
                        if (this._inputPtr >= this._inputEnd) {
                            _loadMoreGuaranteed();
                        }
                        cArr2 = this._inputBuffer;
                        int i4 = this._inputPtr;
                        this._inputPtr = i4 + 1;
                        c2 = cArr2[i4];
                        if (base64Variant.usesPaddingChar(c2)) {
                            _getByteArrayBuilder.append(i3 >> 4);
                        } else {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("expected padding character '");
                            stringBuilder.append(base64Variant.getPaddingChar());
                            stringBuilder.append("'");
                            throw reportInvalidBase64Char(base64Variant, c2, 3, stringBuilder.toString());
                        }
                    }
                }
                i3 = (i3 << 6) | i2;
                if (this._inputPtr >= this._inputEnd) {
                    _loadMoreGuaranteed();
                }
                cArr2 = this._inputBuffer;
                i2 = this._inputPtr;
                this._inputPtr = i2 + 1;
                c2 = cArr2[i2];
                i2 = base64Variant.decodeBase64Char(c2);
                if (i2 < 0) {
                    if (i2 != -2) {
                        if (c2 != '\"' || base64Variant.usesPadding()) {
                            i2 = _decodeBase64Escape(base64Variant, c2, 3);
                        } else {
                            _getByteArrayBuilder.appendTwoBytes(i3 >> 2);
                            return _getByteArrayBuilder.toByteArray();
                        }
                    }
                    if (i2 == -2) {
                        _getByteArrayBuilder.appendTwoBytes(i3 >> 2);
                    }
                }
                _getByteArrayBuilder.appendThreeBytes((i3 << 6) | i2);
            }
        }
    }

    public JsonLocation getTokenLocation() {
        Object sourceReference = this._ioContext.getSourceReference();
        if (this._currToken == JsonToken.FIELD_NAME) {
            return new JsonLocation(sourceReference, -1, (this._nameStartOffset - 1) + this._currInputProcessed, this._nameStartRow, this._nameStartCol);
        }
        return new JsonLocation(sourceReference, -1, this._tokenInputTotal - 1, this._tokenInputRow, this._tokenInputCol);
    }

    public JsonLocation getCurrentLocation() {
        int i = (this._inputPtr - this._currInputRowStart) + 1;
        return new JsonLocation(this._ioContext.getSourceReference(), -1, ((long) this._inputPtr) + this._currInputProcessed, this._currInputRow, i);
    }

    private final void _updateLocation() {
        int i = this._inputPtr;
        this._tokenInputTotal = this._currInputProcessed + ((long) i);
        this._tokenInputRow = this._currInputRow;
        this._tokenInputCol = i - this._currInputRowStart;
    }

    private final void _updateNameLocation() {
        int i = this._inputPtr;
        this._nameStartOffset = (long) i;
        this._nameStartRow = this._currInputRow;
        this._nameStartCol = i - this._currInputRowStart;
    }

    /* Access modifiers changed, original: protected */
    public void _reportInvalidToken(String str) throws IOException {
        _reportInvalidToken(str, "'null', 'true', 'false' or NaN");
    }

    /* Access modifiers changed, original: protected */
    public void _reportInvalidToken(String str, String str2) throws IOException {
        StringBuilder stringBuilder = new StringBuilder(str);
        while (stringBuilder.length() < 256 && (this._inputPtr < this._inputEnd || _loadMore())) {
            char c = this._inputBuffer[this._inputPtr];
            if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
            this._inputPtr++;
            stringBuilder.append(c);
        }
        if (stringBuilder.length() == 256) {
            stringBuilder.append("...");
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Unrecognized token '");
        stringBuilder2.append(stringBuilder.toString());
        stringBuilder2.append("': was expecting ");
        stringBuilder2.append(str2);
        _reportError(stringBuilder2.toString());
    }
}
