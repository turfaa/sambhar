package com.fasterxml.jackson.core.json;

import com.facebook.internal.ServerProtocol;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.base.GeneratorBase;
import com.fasterxml.jackson.core.base.ParserBase;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;

public class UTF8StreamJsonParser extends ParserBase {
    static final byte BYTE_LF = (byte) 10;
    protected static final int[] _icLatin1 = CharTypes.getInputCodeLatin1();
    private static final int[] _icUTF8 = CharTypes.getInputCodeUtf8();
    protected boolean _bufferRecyclable;
    protected byte[] _inputBuffer;
    protected InputStream _inputStream;
    protected int _nameStartCol;
    protected int _nameStartOffset;
    protected int _nameStartRow;
    protected ObjectCodec _objectCodec;
    private int _quad1;
    protected int[] _quadBuffer = new int[16];
    protected final ByteQuadsCanonicalizer _symbols;
    protected boolean _tokenIncomplete;

    private static final int pad(int i, int i2) {
        return i2 == 4 ? i : i | (-1 << (i2 << 3));
    }

    public UTF8StreamJsonParser(IOContext iOContext, int i, InputStream inputStream, ObjectCodec objectCodec, ByteQuadsCanonicalizer byteQuadsCanonicalizer, byte[] bArr, int i2, int i3, boolean z) {
        super(iOContext, i);
        this._inputStream = inputStream;
        this._objectCodec = objectCodec;
        this._symbols = byteQuadsCanonicalizer;
        this._inputBuffer = bArr;
        this._inputPtr = i2;
        this._inputEnd = i3;
        this._currInputRowStart = i2;
        this._currInputProcessed = (long) (-i2);
        this._bufferRecyclable = z;
    }

    public ObjectCodec getCodec() {
        return this._objectCodec;
    }

    public void setCodec(ObjectCodec objectCodec) {
        this._objectCodec = objectCodec;
    }

    public int releaseBuffered(OutputStream outputStream) throws IOException {
        int i = this._inputEnd - this._inputPtr;
        if (i < 1) {
            return 0;
        }
        outputStream.write(this._inputBuffer, this._inputPtr, i);
        return i;
    }

    public Object getInputSource() {
        return this._inputStream;
    }

    /* Access modifiers changed, original: protected|final */
    public final boolean _loadMore() throws IOException {
        int i = this._inputEnd;
        this._currInputProcessed += (long) this._inputEnd;
        this._currInputRowStart -= this._inputEnd;
        this._nameStartOffset -= i;
        if (this._inputStream != null) {
            i = this._inputBuffer.length;
            if (i == 0) {
                return false;
            }
            i = this._inputStream.read(this._inputBuffer, 0, i);
            if (i > 0) {
                this._inputPtr = 0;
                this._inputEnd = i;
                return true;
            }
            _closeInput();
            if (i == 0) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("InputStream.read() returned 0 characters when trying to read ");
                stringBuilder.append(this._inputBuffer.length);
                stringBuilder.append(" bytes");
                throw new IOException(stringBuilder.toString());
            }
        }
        return false;
    }

    /* Access modifiers changed, original: protected|final */
    public final boolean _loadToHaveAtLeast(int i) throws IOException {
        if (this._inputStream == null) {
            return false;
        }
        int i2;
        int i3 = this._inputEnd - this._inputPtr;
        if (i3 <= 0 || this._inputPtr <= 0) {
            this._inputEnd = 0;
        } else {
            i2 = this._inputPtr;
            this._currInputProcessed += (long) i2;
            this._currInputRowStart -= i2;
            this._nameStartOffset -= i2;
            System.arraycopy(this._inputBuffer, i2, this._inputBuffer, 0, i3);
            this._inputEnd = i3;
        }
        this._inputPtr = 0;
        while (this._inputEnd < i) {
            i2 = this._inputStream.read(this._inputBuffer, this._inputEnd, this._inputBuffer.length - this._inputEnd);
            if (i2 < 1) {
                _closeInput();
                if (i2 != 0) {
                    return false;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("InputStream.read() returned 0 characters when trying to read ");
                stringBuilder.append(i3);
                stringBuilder.append(" bytes");
                throw new IOException(stringBuilder.toString());
            }
            this._inputEnd += i2;
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void _closeInput() throws IOException {
        if (this._inputStream != null) {
            if (this._ioContext.isResourceManaged() || isEnabled(Feature.AUTO_CLOSE_SOURCE)) {
                this._inputStream.close();
            }
            this._inputStream = null;
        }
    }

    /* Access modifiers changed, original: protected */
    public void _releaseBuffers() throws IOException {
        super._releaseBuffers();
        this._symbols.release();
        if (this._bufferRecyclable) {
            byte[] bArr = this._inputBuffer;
            if (bArr != null) {
                this._inputBuffer = ByteArrayBuilder.NO_BYTES;
                this._ioContext.releaseReadIOBuffer(bArr);
            }
        }
    }

    public String getText() throws IOException {
        if (this._currToken != JsonToken.VALUE_STRING) {
            return _getText2(this._currToken);
        }
        if (!this._tokenIncomplete) {
            return this._textBuffer.contentsAsString();
        }
        this._tokenIncomplete = false;
        return _finishAndReturnString();
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

    public String getValueAsString() throws IOException {
        if (this._currToken == JsonToken.VALUE_STRING) {
            if (!this._tokenIncomplete) {
                return this._textBuffer.contentsAsString();
            }
            this._tokenIncomplete = false;
            return _finishAndReturnString();
        } else if (this._currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        } else {
            return super.getValueAsString(null);
        }
    }

    public String getValueAsString(String str) throws IOException {
        if (this._currToken == JsonToken.VALUE_STRING) {
            if (!this._tokenIncomplete) {
                return this._textBuffer.contentsAsString();
            }
            this._tokenIncomplete = false;
            return _finishAndReturnString();
        } else if (this._currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        } else {
            return super.getValueAsString(str);
        }
    }

    public int getValueAsInt() throws IOException {
        JsonToken jsonToken = this._currToken;
        if (jsonToken != JsonToken.VALUE_NUMBER_INT && jsonToken != JsonToken.VALUE_NUMBER_FLOAT) {
            return super.getValueAsInt(0);
        }
        if ((this._numTypesValid & 1) == 0) {
            if (this._numTypesValid == 0) {
                return _parseIntValue();
            }
            if ((this._numTypesValid & 1) == 0) {
                convertNumberToInt();
            }
        }
        return this._numberInt;
    }

    public int getValueAsInt(int i) throws IOException {
        JsonToken jsonToken = this._currToken;
        if (jsonToken != JsonToken.VALUE_NUMBER_INT && jsonToken != JsonToken.VALUE_NUMBER_FLOAT) {
            return super.getValueAsInt(i);
        }
        if ((this._numTypesValid & 1) == 0) {
            if (this._numTypesValid == 0) {
                return _parseIntValue();
            }
            if ((this._numTypesValid & 1) == 0) {
                convertNumberToInt();
            }
        }
        return this._numberInt;
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

    public char[] getTextCharacters() throws IOException {
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

    public int getTextLength() throws IOException {
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
    public int getTextOffset() throws java.io.IOException {
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
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8StreamJsonParser.getTextOffset():int");
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
            byte[] bArr2 = this._inputBuffer;
            int i3 = this._inputPtr;
            this._inputPtr = i3 + 1;
            int i4 = bArr2[i3] & 255;
            if (i4 > 32) {
                i3 = base64Variant.decodeBase64Char(i4);
                if (i3 < 0) {
                    if (i4 == 34) {
                        break;
                    }
                    i3 = _decodeBase64Escape(base64Variant, i4, 0);
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
                bArr2 = this._inputBuffer;
                int i5 = this._inputPtr;
                this._inputPtr = i5 + 1;
                i4 = bArr2[i5] & 255;
                i5 = base64Variant.decodeBase64Char(i4);
                if (i5 < 0) {
                    i5 = _decodeBase64Escape(base64Variant, i4, 1);
                }
                i4 = (i3 << 6) | i5;
                if (this._inputPtr >= this._inputEnd) {
                    _loadMoreGuaranteed();
                }
                byte[] bArr3 = this._inputBuffer;
                i5 = this._inputPtr;
                this._inputPtr = i5 + 1;
                i3 = bArr3[i5] & 255;
                i5 = base64Variant.decodeBase64Char(i3);
                if (i5 < 0) {
                    if (i5 != -2) {
                        if (i3 == 34 && !base64Variant.usesPadding()) {
                            length = i + 1;
                            bArr[i] = (byte) (i4 >> 4);
                            i = length;
                            break;
                        }
                        i5 = _decodeBase64Escape(base64Variant, i3, 2);
                    }
                    if (i5 == -2) {
                        if (this._inputPtr >= this._inputEnd) {
                            _loadMoreGuaranteed();
                        }
                        bArr3 = this._inputBuffer;
                        int i6 = this._inputPtr;
                        this._inputPtr = i6 + 1;
                        i3 = bArr3[i6] & 255;
                        if (base64Variant.usesPaddingChar(i3)) {
                            i3 = i + 1;
                            bArr[i] = (byte) (i4 >> 4);
                            i = i3;
                        } else {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("expected padding character '");
                            stringBuilder.append(base64Variant.getPaddingChar());
                            stringBuilder.append("'");
                            throw reportInvalidBase64Char(base64Variant, i3, 3, stringBuilder.toString());
                        }
                    }
                }
                i4 = (i4 << 6) | i5;
                if (this._inputPtr >= this._inputEnd) {
                    _loadMoreGuaranteed();
                }
                bArr3 = this._inputBuffer;
                i5 = this._inputPtr;
                this._inputPtr = i5 + 1;
                i3 = bArr3[i5] & 255;
                i5 = base64Variant.decodeBase64Char(i3);
                if (i5 < 0) {
                    if (i5 != -2) {
                        if (i3 == 34 && !base64Variant.usesPadding()) {
                            int i7 = i4 >> 2;
                            length = i + 1;
                            bArr[i] = (byte) (i7 >> 8);
                            i = length + 1;
                            bArr[length] = (byte) i7;
                            break;
                        }
                        i5 = _decodeBase64Escape(base64Variant, i3, 3);
                    }
                    if (i5 == -2) {
                        i4 >>= 2;
                        i3 = i + 1;
                        bArr[i] = (byte) (i4 >> 8);
                        i = i3 + 1;
                        bArr[i3] = (byte) i4;
                    }
                }
                i4 = (i4 << 6) | i5;
                i3 = i + 1;
                bArr[i] = (byte) (i4 >> 16);
                i = i3 + 1;
                bArr[i3] = (byte) (i4 >> 8);
                i3 = i + 1;
                bArr[i] = (byte) i4;
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

    public JsonToken nextToken() throws IOException {
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
                if (_skipWSOrEnd != 44) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("was expecting comma to separate ");
                    stringBuilder.append(this._parsingContext.typeDesc());
                    stringBuilder.append(" entries");
                    _reportUnexpectedChar(_skipWSOrEnd, stringBuilder.toString());
                }
                _skipWSOrEnd = _skipWS();
            }
            if (this._parsingContext.inObject()) {
                _updateNameLocation();
                this._parsingContext.setCurrentName(_parseName(_skipWSOrEnd));
                this._currToken = JsonToken.FIELD_NAME;
                _skipWSOrEnd = _skipColon();
                _updateLocation();
                if (_skipWSOrEnd == 34) {
                    this._tokenIncomplete = true;
                    this._nextToken = JsonToken.VALUE_STRING;
                    return this._currToken;
                }
                if (_skipWSOrEnd == 45) {
                    jsonToken = _parseNegNumber();
                } else if (_skipWSOrEnd == 91) {
                    jsonToken = JsonToken.START_ARRAY;
                } else if (_skipWSOrEnd == 102) {
                    _matchToken("false", 1);
                    jsonToken = JsonToken.VALUE_FALSE;
                } else if (_skipWSOrEnd == 110) {
                    _matchToken("null", 1);
                    jsonToken = JsonToken.VALUE_NULL;
                } else if (_skipWSOrEnd == 116) {
                    _matchToken(ServerProtocol.DIALOG_RETURN_SCOPES_TRUE, 1);
                    jsonToken = JsonToken.VALUE_TRUE;
                } else if (_skipWSOrEnd != 123) {
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
                            jsonToken = _handleUnexpectedValue(_skipWSOrEnd);
                            break;
                    }
                } else {
                    jsonToken = JsonToken.START_OBJECT;
                }
                this._nextToken = jsonToken;
                return this._currToken;
            }
            _updateLocation();
            return _nextTokenNotInObject(_skipWSOrEnd);
        }
    }

    private final JsonToken _nextTokenNotInObject(int i) throws IOException {
        JsonToken jsonToken;
        if (i == 34) {
            this._tokenIncomplete = true;
            jsonToken = JsonToken.VALUE_STRING;
            this._currToken = jsonToken;
            return jsonToken;
        } else if (i == 45) {
            jsonToken = _parseNegNumber();
            this._currToken = jsonToken;
            return jsonToken;
        } else if (i == 91) {
            this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
            jsonToken = JsonToken.START_ARRAY;
            this._currToken = jsonToken;
            return jsonToken;
        } else if (i == 102) {
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
                default:
                    jsonToken = _handleUnexpectedValue(i);
                    this._currToken = jsonToken;
                    return jsonToken;
            }
        } else {
            this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
            jsonToken = JsonToken.START_OBJECT;
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
                if (_skipWSOrEnd != 44) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("was expecting comma to separate ");
                    stringBuilder.append(this._parsingContext.typeDesc());
                    stringBuilder.append(" entries");
                    _reportUnexpectedChar(_skipWSOrEnd, stringBuilder.toString());
                }
                _skipWSOrEnd = _skipWS();
            }
            if (this._parsingContext.inObject()) {
                _updateNameLocation();
                if (_skipWSOrEnd == 34) {
                    byte[] asQuotedUTF8 = serializableString.asQuotedUTF8();
                    int length = asQuotedUTF8.length;
                    if ((this._inputPtr + length) + 4 < this._inputEnd) {
                        int i2 = this._inputPtr + length;
                        if (this._inputBuffer[i2] == (byte) 34) {
                            int i3 = this._inputPtr;
                            while (i3 != i2) {
                                if (asQuotedUTF8[i] == this._inputBuffer[i3]) {
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
                return _isNextTokenNameMaybe(_skipWSOrEnd, serializableString);
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
                if (_skipWSOrEnd != 44) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("was expecting comma to separate ");
                    stringBuilder.append(this._parsingContext.typeDesc());
                    stringBuilder.append(" entries");
                    _reportUnexpectedChar(_skipWSOrEnd, stringBuilder.toString());
                }
                _skipWSOrEnd = _skipWS();
            }
            if (this._parsingContext.inObject()) {
                _updateNameLocation();
                String _parseName = _parseName(_skipWSOrEnd);
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
                    _matchToken("false", 1);
                    _parseNegNumber = JsonToken.VALUE_FALSE;
                } else if (_skipColon == 110) {
                    _matchToken("null", 1);
                    _parseNegNumber = JsonToken.VALUE_NULL;
                } else if (_skipColon == 116) {
                    _matchToken(ServerProtocol.DIALOG_RETURN_SCOPES_TRUE, 1);
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
                            _parseNegNumber = _handleUnexpectedValue(_skipColon);
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

    private final int _skipColonFast(int i) throws IOException {
        int i2 = i + 1;
        byte b = this._inputBuffer[i];
        int i3;
        if (b == (byte) 58) {
            i3 = i2 + 1;
            b = this._inputBuffer[i2];
            if (b > (byte) 32) {
                if (!(b == (byte) 47 || b == (byte) 35)) {
                    this._inputPtr = i3;
                    return b;
                }
            } else if (b == (byte) 32 || b == (byte) 9) {
                i2 = i3 + 1;
                b = this._inputBuffer[i3];
                if (b <= (byte) 32 || b == (byte) 47 || b == (byte) 35) {
                    i3 = i2;
                } else {
                    this._inputPtr = i2;
                    return b;
                }
            }
            this._inputPtr = i3 - 1;
            return _skipColon2(true);
        }
        if (b == (byte) 32 || b == (byte) 9) {
            int i4 = i2 + 1;
            b = this._inputBuffer[i2];
            i2 = i4;
        }
        if (b == (byte) 58) {
            i3 = i2 + 1;
            b = this._inputBuffer[i2];
            if (b > (byte) 32) {
                if (!(b == (byte) 47 || b == (byte) 35)) {
                    this._inputPtr = i3;
                    return b;
                }
            } else if (b == (byte) 32 || b == (byte) 9) {
                i2 = i3 + 1;
                b = this._inputBuffer[i3];
                if (b <= (byte) 32 || b == (byte) 47 || b == (byte) 35) {
                    i3 = i2;
                } else {
                    this._inputPtr = i2;
                    return b;
                }
            }
            this._inputPtr = i3 - 1;
            return _skipColon2(true);
        }
        this._inputPtr = i2 - 1;
        return _skipColon2(false);
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
                    this._nextToken = _handleUnexpectedValue(i);
                    return;
            }
        } else {
            this._nextToken = JsonToken.START_OBJECT;
        }
    }

    private final boolean _isNextTokenNameMaybe(int i, SerializableString serializableString) throws IOException {
        String _parseName = _parseName(i);
        this._parsingContext.setCurrentName(_parseName);
        boolean equals = _parseName.equals(serializableString.getValue());
        this._currToken = JsonToken.FIELD_NAME;
        int _skipColon = _skipColon();
        _updateLocation();
        if (_skipColon == 34) {
            this._tokenIncomplete = true;
            this._nextToken = JsonToken.VALUE_STRING;
            return equals;
        }
        JsonToken _parseNegNumber;
        if (_skipColon == 45) {
            _parseNegNumber = _parseNegNumber();
        } else if (_skipColon == 91) {
            _parseNegNumber = JsonToken.START_ARRAY;
        } else if (_skipColon == 102) {
            _matchToken("false", 1);
            _parseNegNumber = JsonToken.VALUE_FALSE;
        } else if (_skipColon == 110) {
            _matchToken("null", 1);
            _parseNegNumber = JsonToken.VALUE_NULL;
        } else if (_skipColon == 116) {
            _matchToken(ServerProtocol.DIALOG_RETURN_SCOPES_TRUE, 1);
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
                    _parseNegNumber = _handleUnexpectedValue(_skipColon);
                    break;
            }
        } else {
            _parseNegNumber = JsonToken.START_OBJECT;
        }
        this._nextToken = _parseNegNumber;
        return equals;
    }

    public String nextTextValue() throws IOException {
        String str = null;
        if (this._currToken == JsonToken.FIELD_NAME) {
            this._nameCopied = false;
            JsonToken jsonToken = this._nextToken;
            this._nextToken = null;
            this._currToken = jsonToken;
            if (jsonToken != JsonToken.VALUE_STRING) {
                if (jsonToken == JsonToken.START_ARRAY) {
                    this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
                } else if (jsonToken == JsonToken.START_OBJECT) {
                    this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
                }
                return null;
            } else if (!this._tokenIncomplete) {
                return this._textBuffer.contentsAsString();
            } else {
                this._tokenIncomplete = false;
                return _finishAndReturnString();
            }
        }
        if (nextToken() == JsonToken.VALUE_STRING) {
            str = getText();
        }
        return str;
    }

    public int nextIntValue(int i) throws IOException {
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

    public long nextLongValue(long j) throws IOException {
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

    public Boolean nextBooleanValue() throws IOException {
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
        if (jsonToken == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE;
        }
        if (jsonToken == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public JsonToken _parsePosNumber(int i) throws IOException {
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        if (i == 48) {
            i = _verifyNoLeadingZeroes();
        }
        emptyAndGetCurrentSegment[0] = (char) i;
        i = (this._inputPtr + emptyAndGetCurrentSegment.length) - 1;
        if (i > this._inputEnd) {
            i = this._inputEnd;
        }
        int i2 = 1;
        int i3 = 1;
        while (this._inputPtr < i) {
            byte[] bArr = this._inputBuffer;
            int i4 = this._inputPtr;
            this._inputPtr = i4 + 1;
            int i5 = bArr[i4] & 255;
            if (i5 >= 48 && i5 <= 57) {
                i3++;
                i4 = i2 + 1;
                emptyAndGetCurrentSegment[i2] = (char) i5;
                i2 = i4;
            } else if (i5 == 46 || i5 == 101 || i5 == 69) {
                return _parseFloat(emptyAndGetCurrentSegment, i2, i5, false, i3);
            } else {
                this._inputPtr--;
                this._textBuffer.setCurrentLength(i2);
                if (this._parsingContext.inRoot()) {
                    _verifyRootSpace(i5);
                }
                return resetInt(false, i3);
            }
        }
        return _parseNumber2(emptyAndGetCurrentSegment, i2, false, i3);
    }

    /* Access modifiers changed, original: protected */
    public JsonToken _parseNegNumber() throws IOException {
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        emptyAndGetCurrentSegment[0] = '-';
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        int i2 = bArr[i] & 255;
        if (i2 < 48 || i2 > 57) {
            return _handleInvalidNumberStart(i2, true);
        }
        if (i2 == 48) {
            i2 = _verifyNoLeadingZeroes();
        }
        int i3 = 2;
        emptyAndGetCurrentSegment[1] = (char) i2;
        i2 = (this._inputPtr + emptyAndGetCurrentSegment.length) - 2;
        if (i2 > this._inputEnd) {
            i2 = this._inputEnd;
        }
        int i4 = 1;
        while (this._inputPtr < i2) {
            byte[] bArr2 = this._inputBuffer;
            int i5 = this._inputPtr;
            this._inputPtr = i5 + 1;
            int i6 = bArr2[i5] & 255;
            if (i6 >= 48 && i6 <= 57) {
                i4++;
                i5 = i3 + 1;
                emptyAndGetCurrentSegment[i3] = (char) i6;
                i3 = i5;
            } else if (i6 == 46 || i6 == 101 || i6 == 69) {
                return _parseFloat(emptyAndGetCurrentSegment, i3, i6, true, i4);
            } else {
                this._inputPtr--;
                this._textBuffer.setCurrentLength(i3);
                if (this._parsingContext.inRoot()) {
                    _verifyRootSpace(i6);
                }
                return resetInt(true, i4);
            }
        }
        return _parseNumber2(emptyAndGetCurrentSegment, i3, true, i4);
    }

    private final JsonToken _parseNumber2(char[] cArr, int i, boolean z, int i2) throws IOException {
        byte[] bArr;
        int i3;
        char[] cArr2 = cArr;
        int i4 = i;
        int i5 = i2;
        while (true) {
            if (this._inputPtr < this._inputEnd || _loadMore()) {
                bArr = this._inputBuffer;
                i = this._inputPtr;
                this._inputPtr = i + 1;
                i3 = bArr[i] & 255;
                if (i3 <= 57 && i3 >= 48) {
                    if (i4 >= cArr2.length) {
                        i4 = 0;
                        cArr2 = this._textBuffer.finishCurrentSegment();
                    }
                    int i6 = i4 + 1;
                    cArr2[i4] = (char) i3;
                    i5++;
                    i4 = i6;
                }
            } else {
                this._textBuffer.setCurrentLength(i4);
                return resetInt(z, i5);
            }
        }
        if (i3 == 46 || i3 == 101 || i3 == 69) {
            return _parseFloat(cArr2, i4, i3, z, i5);
        }
        this._inputPtr--;
        this._textBuffer.setCurrentLength(i4);
        if (this._parsingContext.inRoot()) {
            bArr = this._inputBuffer;
            i = this._inputPtr;
            this._inputPtr = i + 1;
            _verifyRootSpace(bArr[i] & 255);
        }
        return resetInt(z, i5);
    }

    private final int _verifyNoLeadingZeroes() throws IOException {
        if (this._inputPtr >= this._inputEnd && !_loadMore()) {
            return 48;
        }
        int i = this._inputBuffer[this._inputPtr] & 255;
        if (i < 48 || i > 57) {
            return 48;
        }
        if (!isEnabled(Feature.ALLOW_NUMERIC_LEADING_ZEROS)) {
            reportInvalidNumber("Leading zeroes not allowed");
        }
        this._inputPtr++;
        if (i == 48) {
            do {
                if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                    break;
                }
                i = this._inputBuffer[this._inputPtr] & 255;
                if (i < 48 || i > 57) {
                    return 48;
                }
                this._inputPtr++;
            } while (i == 48);
        }
        return i;
    }

    private final JsonToken _parseFloat(char[] cArr, int i, int i2, boolean z, int i3) throws IOException {
        int i4;
        int i5;
        Object obj;
        int i6 = 0;
        if (i2 == 46) {
            if (i >= cArr.length) {
                cArr = this._textBuffer.finishCurrentSegment();
                i = 0;
            }
            i4 = i + 1;
            cArr[i] = (char) i2;
            i = i4;
            i4 = i2;
            char[] cArr2 = cArr;
            i5 = 0;
            while (true) {
                if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                    obj = 1;
                    break;
                }
                byte[] bArr = this._inputBuffer;
                int i7 = this._inputPtr;
                this._inputPtr = i7 + 1;
                i4 = bArr[i7] & 255;
                if (i4 < 48 || i4 > 57) {
                    obj = null;
                } else {
                    i5++;
                    if (i >= cArr2.length) {
                        cArr2 = this._textBuffer.finishCurrentSegment();
                        i = 0;
                    }
                    i7 = i + 1;
                    cArr2[i] = (char) i4;
                    i = i7;
                }
            }
            obj = null;
            if (i5 == 0) {
                reportUnexpectedNumberChar(i4, "Decimal point not followed by a digit");
            }
            int i8 = i4;
            i4 = i5;
            cArr = cArr2;
            i2 = i8;
        } else {
            i4 = 0;
            obj = null;
        }
        if (i2 == 101 || i2 == 69) {
            if (i >= cArr.length) {
                cArr = this._textBuffer.finishCurrentSegment();
                i = 0;
            }
            int i9 = i + 1;
            cArr[i] = (char) i2;
            if (this._inputPtr >= this._inputEnd) {
                _loadMoreGuaranteed();
            }
            byte[] bArr2 = this._inputBuffer;
            i2 = this._inputPtr;
            this._inputPtr = i2 + 1;
            i = bArr2[i2] & 255;
            if (i == 45 || i == 43) {
                if (i9 >= cArr.length) {
                    cArr = this._textBuffer.finishCurrentSegment();
                    i9 = 0;
                }
                i2 = i9 + 1;
                cArr[i9] = (char) i;
                if (this._inputPtr >= this._inputEnd) {
                    _loadMoreGuaranteed();
                }
                bArr2 = this._inputBuffer;
                i9 = this._inputPtr;
                this._inputPtr = i9 + 1;
                i = bArr2[i9] & 255;
                i9 = i2;
            }
            i2 = i;
            char[] cArr3 = cArr;
            i5 = 0;
            while (i2 <= 57 && i2 >= 48) {
                i5++;
                if (i9 >= cArr3.length) {
                    cArr3 = this._textBuffer.finishCurrentSegment();
                    i9 = 0;
                }
                int i10 = i9 + 1;
                cArr3[i9] = (char) i2;
                if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                    i6 = i5;
                    i = i10;
                    obj = 1;
                    break;
                }
                byte[] bArr3 = this._inputBuffer;
                i9 = this._inputPtr;
                this._inputPtr = i9 + 1;
                i2 = bArr3[i9] & 255;
                i9 = i10;
            }
            i6 = i5;
            i = i9;
            if (i6 == 0) {
                reportUnexpectedNumberChar(i2, "Exponent indicator not followed by a digit");
            }
        }
        if (obj == null) {
            this._inputPtr--;
            if (this._parsingContext.inRoot()) {
                _verifyRootSpace(i2);
            }
        }
        this._textBuffer.setCurrentLength(i);
        return resetFloat(z, i3, i4, i6);
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
    public final String _parseName(int i) throws IOException {
        if (i != 34) {
            return _handleOddName(i);
        }
        if (this._inputPtr + 13 > this._inputEnd) {
            return slowParseName();
        }
        byte[] bArr = this._inputBuffer;
        int[] iArr = _icLatin1;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        i2 = bArr[i2] & 255;
        if (iArr[i2] == 0) {
            int i3 = this._inputPtr;
            this._inputPtr = i3 + 1;
            i3 = bArr[i3] & 255;
            if (iArr[i3] == 0) {
                i2 = (i2 << 8) | i3;
                i3 = this._inputPtr;
                this._inputPtr = i3 + 1;
                i3 = bArr[i3] & 255;
                if (iArr[i3] == 0) {
                    i2 = (i2 << 8) | i3;
                    i3 = this._inputPtr;
                    this._inputPtr = i3 + 1;
                    i3 = bArr[i3] & 255;
                    if (iArr[i3] == 0) {
                        i2 = (i2 << 8) | i3;
                        i3 = this._inputPtr;
                        this._inputPtr = i3 + 1;
                        i = bArr[i3] & 255;
                        if (iArr[i] == 0) {
                            this._quad1 = i2;
                            return parseMediumName(i);
                        } else if (i == 34) {
                            return findName(i2, 4);
                        } else {
                            return parseName(i2, i, 4);
                        }
                    } else if (i3 == 34) {
                        return findName(i2, 3);
                    } else {
                        return parseName(i2, i3, 3);
                    }
                } else if (i3 == 34) {
                    return findName(i2, 2);
                } else {
                    return parseName(i2, i3, 2);
                }
            } else if (i3 == 34) {
                return findName(i2, 1);
            } else {
                return parseName(i2, i3, 1);
            }
        } else if (i2 == 34) {
            return "";
        } else {
            return parseName(0, i2, 0);
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final String parseMediumName(int i) throws IOException {
        byte[] bArr = this._inputBuffer;
        int[] iArr = _icLatin1;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        i2 = bArr[i2] & 255;
        if (iArr[i2] == 0) {
            i = (i << 8) | i2;
            i2 = this._inputPtr;
            this._inputPtr = i2 + 1;
            i2 = bArr[i2] & 255;
            if (iArr[i2] == 0) {
                i = (i << 8) | i2;
                i2 = this._inputPtr;
                this._inputPtr = i2 + 1;
                i2 = bArr[i2] & 255;
                if (iArr[i2] == 0) {
                    i = (i << 8) | i2;
                    i2 = this._inputPtr;
                    this._inputPtr = i2 + 1;
                    int i3 = bArr[i2] & 255;
                    if (iArr[i3] == 0) {
                        return parseMediumName2(i3, i);
                    }
                    if (i3 == 34) {
                        return findName(this._quad1, i, 4);
                    }
                    return parseName(this._quad1, i, i3, 4);
                } else if (i2 == 34) {
                    return findName(this._quad1, i, 3);
                } else {
                    return parseName(this._quad1, i, i2, 3);
                }
            } else if (i2 == 34) {
                return findName(this._quad1, i, 2);
            } else {
                return parseName(this._quad1, i, i2, 2);
            }
        } else if (i2 == 34) {
            return findName(this._quad1, i, 1);
        } else {
            return parseName(this._quad1, i, i2, 1);
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final String parseMediumName2(int i, int i2) throws IOException {
        byte[] bArr = this._inputBuffer;
        int[] iArr = _icLatin1;
        int i3 = this._inputPtr;
        this._inputPtr = i3 + 1;
        i3 = bArr[i3] & 255;
        if (iArr[i3] == 0) {
            int i4 = (i << 8) | i3;
            i3 = this._inputPtr;
            this._inputPtr = i3 + 1;
            i3 = bArr[i3] & 255;
            if (iArr[i3] == 0) {
                i4 = (i4 << 8) | i3;
                i3 = this._inputPtr;
                this._inputPtr = i3 + 1;
                i3 = bArr[i3] & 255;
                if (iArr[i3] == 0) {
                    i4 = (i4 << 8) | i3;
                    i3 = this._inputPtr;
                    this._inputPtr = i3 + 1;
                    i3 = bArr[i3] & 255;
                    if (iArr[i3] == 0) {
                        return parseLongName(i3, i2, i4);
                    }
                    if (i3 == 34) {
                        return findName(this._quad1, i2, i4, 4);
                    }
                    return parseName(this._quad1, i2, i4, i3, 4);
                } else if (i3 == 34) {
                    return findName(this._quad1, i2, i4, 3);
                } else {
                    return parseName(this._quad1, i2, i4, i3, 3);
                }
            } else if (i3 == 34) {
                return findName(this._quad1, i2, i4, 2);
            } else {
                return parseName(this._quad1, i2, i4, i3, 2);
            }
        } else if (i3 == 34) {
            return findName(this._quad1, i2, i, 1);
        } else {
            return parseName(this._quad1, i2, i, i3, 1);
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final String parseLongName(int i, int i2, int i3) throws IOException {
        this._quadBuffer[0] = this._quad1;
        this._quadBuffer[1] = i2;
        this._quadBuffer[2] = i3;
        byte[] bArr = this._inputBuffer;
        int[] iArr = _icLatin1;
        int i4 = i;
        int i5 = 3;
        while (this._inputPtr + 4 <= this._inputEnd) {
            i = this._inputPtr;
            this._inputPtr = i + 1;
            i = bArr[i] & 255;
            if (iArr[i] == 0) {
                i |= i4 << 8;
                int i6 = this._inputPtr;
                this._inputPtr = i6 + 1;
                i4 = bArr[i6] & 255;
                if (iArr[i4] == 0) {
                    i = (i << 8) | i4;
                    i6 = this._inputPtr;
                    this._inputPtr = i6 + 1;
                    i4 = bArr[i6] & 255;
                    if (iArr[i4] == 0) {
                        i = (i << 8) | i4;
                        i6 = this._inputPtr;
                        this._inputPtr = i6 + 1;
                        i4 = bArr[i6] & 255;
                        if (iArr[i4] == 0) {
                            if (i5 >= this._quadBuffer.length) {
                                this._quadBuffer = growArrayBy(this._quadBuffer, i5);
                            }
                            i6 = i5 + 1;
                            this._quadBuffer[i5] = i;
                            i5 = i6;
                        } else if (i4 == 34) {
                            return findName(this._quadBuffer, i5, i, 4);
                        } else {
                            return parseEscapedName(this._quadBuffer, i5, i, i4, 4);
                        }
                    } else if (i4 == 34) {
                        return findName(this._quadBuffer, i5, i, 3);
                    } else {
                        return parseEscapedName(this._quadBuffer, i5, i, i4, 3);
                    }
                } else if (i4 == 34) {
                    return findName(this._quadBuffer, i5, i, 2);
                } else {
                    return parseEscapedName(this._quadBuffer, i5, i, i4, 2);
                }
            } else if (i == 34) {
                return findName(this._quadBuffer, i5, i4, 1);
            } else {
                return parseEscapedName(this._quadBuffer, i5, i4, i, 1);
            }
        }
        return parseEscapedName(this._quadBuffer, i5, 0, i4, 0);
    }

    /* Access modifiers changed, original: protected */
    public String slowParseName() throws IOException {
        if (this._inputPtr >= this._inputEnd && !_loadMore()) {
            _reportInvalidEOF(": was expecting closing '\"' for name", JsonToken.FIELD_NAME);
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        int i2 = bArr[i] & 255;
        if (i2 == 34) {
            return "";
        }
        return parseEscapedName(this._quadBuffer, 0, 0, i2, 0);
    }

    private final String parseName(int i, int i2, int i3) throws IOException {
        return parseEscapedName(this._quadBuffer, 0, i, i2, i3);
    }

    private final String parseName(int i, int i2, int i3, int i4) throws IOException {
        this._quadBuffer[0] = i;
        return parseEscapedName(this._quadBuffer, 1, i2, i3, i4);
    }

    private final String parseName(int i, int i2, int i3, int i4, int i5) throws IOException {
        this._quadBuffer[0] = i;
        this._quadBuffer[1] = i2;
        return parseEscapedName(this._quadBuffer, 2, i3, i4, i5);
    }

    /* Access modifiers changed, original: protected|final */
    public final String parseEscapedName(int[] iArr, int i, int i2, int i3, int i4) throws IOException {
        int[] iArr2 = _icLatin1;
        while (true) {
            if (iArr2[i3] != 0) {
                if (i3 == 34) {
                    break;
                }
                if (i3 != 92) {
                    _throwUnquotedSpace(i3, "name");
                } else {
                    i3 = _decodeEscaped();
                }
                if (i3 > 127) {
                    if (i4 >= 4) {
                        if (i >= iArr.length) {
                            iArr = growArrayBy(iArr, iArr.length);
                            this._quadBuffer = iArr;
                        }
                        i4 = i + 1;
                        iArr[i] = i2;
                        i = i4;
                        i2 = 0;
                        i4 = 0;
                    }
                    if (i3 < 2048) {
                        i2 = (i2 << 8) | ((i3 >> 6) | 192);
                        i4++;
                    } else {
                        i2 = (i2 << 8) | ((i3 >> 12) | 224);
                        i4++;
                        if (i4 >= 4) {
                            if (i >= iArr.length) {
                                iArr = growArrayBy(iArr, iArr.length);
                                this._quadBuffer = iArr;
                            }
                            i4 = i + 1;
                            iArr[i] = i2;
                            i = i4;
                            i2 = 0;
                            i4 = 0;
                        }
                        i2 = (i2 << 8) | (((i3 >> 6) & 63) | 128);
                        i4++;
                    }
                    i3 = (i3 & 63) | 128;
                }
            }
            if (i4 < 4) {
                i4++;
                i2 = (i2 << 8) | i3;
            } else {
                if (i >= iArr.length) {
                    iArr = growArrayBy(iArr, iArr.length);
                    this._quadBuffer = iArr;
                }
                i4 = i + 1;
                iArr[i] = i2;
                i2 = i3;
                i = i4;
                i4 = 1;
            }
            if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                _reportInvalidEOF(" in field name", JsonToken.FIELD_NAME);
            }
            byte[] bArr = this._inputBuffer;
            int i5 = this._inputPtr;
            this._inputPtr = i5 + 1;
            i3 = bArr[i5] & 255;
        }
        if (i4 > 0) {
            if (i >= iArr.length) {
                iArr = growArrayBy(iArr, iArr.length);
                this._quadBuffer = iArr;
            }
            i3 = i + 1;
            iArr[i] = pad(i2, i4);
            i = i3;
        }
        String findName = this._symbols.findName(iArr, i);
        if (findName == null) {
            return addName(iArr, i, i4);
        }
        return findName;
    }

    /* Access modifiers changed, original: protected */
    public String _handleOddName(int i) throws IOException {
        if (i == 39 && isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
            return _parseAposName();
        }
        int i2;
        if (!isEnabled(Feature.ALLOW_UNQUOTED_FIELD_NAMES)) {
            _reportUnexpectedChar((char) _decodeCharForError(i), "was expecting double-quote to start field name");
        }
        int[] inputCodeUtf8JsNames = CharTypes.getInputCodeUtf8JsNames();
        if (inputCodeUtf8JsNames[i] != 0) {
            _reportUnexpectedChar(i, "was expecting either valid name character (for unquoted name) or double-quote (for quoted) to start field name");
        }
        int i3 = 0;
        int[] iArr = this._quadBuffer;
        int i4 = 0;
        int i5 = i;
        i = 0;
        while (true) {
            if (i3 < 4) {
                i3++;
                i = (i << 8) | i5;
            } else {
                if (i4 >= iArr.length) {
                    iArr = growArrayBy(iArr, iArr.length);
                    this._quadBuffer = iArr;
                }
                i3 = i4 + 1;
                iArr[i4] = i;
                i = i5;
                i4 = i3;
                i3 = 1;
            }
            if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                _reportInvalidEOF(" in field name", JsonToken.FIELD_NAME);
            }
            i5 = this._inputBuffer[this._inputPtr] & 255;
            if (inputCodeUtf8JsNames[i5] != 0) {
                break;
            }
            this._inputPtr++;
        }
        if (i3 > 0) {
            if (i4 >= iArr.length) {
                iArr = growArrayBy(iArr, iArr.length);
                this._quadBuffer = iArr;
            }
            i2 = i4 + 1;
            iArr[i4] = i;
        } else {
            i2 = i4;
        }
        String findName = this._symbols.findName(iArr, i2);
        if (findName == null) {
            findName = addName(iArr, i2, i3);
        }
        return findName;
    }

    /* Access modifiers changed, original: protected */
    public String _parseAposName() throws IOException {
        if (this._inputPtr >= this._inputEnd && !_loadMore()) {
            _reportInvalidEOF(": was expecting closing ''' for field name", JsonToken.FIELD_NAME);
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        int i2 = bArr[i] & 255;
        if (i2 == 39) {
            return "";
        }
        int[] iArr = this._quadBuffer;
        int[] iArr2 = _icLatin1;
        int[] iArr3 = iArr;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        while (i2 != 39) {
            if (!(i2 == 34 || iArr2[i2] == 0)) {
                if (i2 != 92) {
                    _throwUnquotedSpace(i2, "name");
                } else {
                    i2 = _decodeEscaped();
                }
                if (i2 > 127) {
                    if (i3 >= 4) {
                        if (i4 >= iArr3.length) {
                            iArr3 = growArrayBy(iArr3, iArr3.length);
                            this._quadBuffer = iArr3;
                        }
                        i3 = i4 + 1;
                        iArr3[i4] = i5;
                        i4 = i3;
                        i3 = 0;
                        i5 = 0;
                    }
                    if (i2 < 2048) {
                        i5 = (i5 << 8) | ((i2 >> 6) | 192);
                        i3++;
                    } else {
                        i5 = (i5 << 8) | ((i2 >> 12) | 224);
                        i3++;
                        if (i3 >= 4) {
                            if (i4 >= iArr3.length) {
                                iArr3 = growArrayBy(iArr3, iArr3.length);
                                this._quadBuffer = iArr3;
                            }
                            i3 = i4 + 1;
                            iArr3[i4] = i5;
                            i4 = i3;
                            i3 = 0;
                            i5 = 0;
                        }
                        i5 = (i5 << 8) | (((i2 >> 6) & 63) | 128);
                        i3++;
                    }
                    i2 = (i2 & 63) | 128;
                }
            }
            if (i3 < 4) {
                i3++;
                i5 = i2 | (i5 << 8);
            } else {
                if (i4 >= iArr3.length) {
                    iArr3 = growArrayBy(iArr3, iArr3.length);
                    this._quadBuffer = iArr3;
                }
                i3 = i4 + 1;
                iArr3[i4] = i5;
                i5 = i2;
                i4 = i3;
                i3 = 1;
            }
            if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                _reportInvalidEOF(" in field name", JsonToken.FIELD_NAME);
            }
            bArr = this._inputBuffer;
            int i6 = this._inputPtr;
            this._inputPtr = i6 + 1;
            i2 = bArr[i6] & 255;
        }
        if (i3 > 0) {
            if (i4 >= iArr3.length) {
                iArr3 = growArrayBy(iArr3, iArr3.length);
                this._quadBuffer = iArr3;
            }
            i2 = i4 + 1;
            iArr3[i4] = pad(i5, i3);
        } else {
            i2 = i4;
        }
        String findName = this._symbols.findName(iArr3, i2);
        if (findName == null) {
            findName = addName(iArr3, i2, i3);
        }
        return findName;
    }

    private final String findName(int i, int i2) throws JsonParseException {
        i = pad(i, i2);
        String findName = this._symbols.findName(i);
        if (findName != null) {
            return findName;
        }
        this._quadBuffer[0] = i;
        return addName(this._quadBuffer, 1, i2);
    }

    private final String findName(int i, int i2, int i3) throws JsonParseException {
        i2 = pad(i2, i3);
        String findName = this._symbols.findName(i, i2);
        if (findName != null) {
            return findName;
        }
        this._quadBuffer[0] = i;
        this._quadBuffer[1] = i2;
        return addName(this._quadBuffer, 2, i3);
    }

    private final String findName(int i, int i2, int i3, int i4) throws JsonParseException {
        i3 = pad(i3, i4);
        String findName = this._symbols.findName(i, i2, i3);
        if (findName != null) {
            return findName;
        }
        int[] iArr = this._quadBuffer;
        iArr[0] = i;
        iArr[1] = i2;
        iArr[2] = pad(i3, i4);
        return addName(iArr, 3, i4);
    }

    private final String findName(int[] iArr, int i, int i2, int i3) throws JsonParseException {
        if (i >= iArr.length) {
            iArr = growArrayBy(iArr, iArr.length);
            this._quadBuffer = iArr;
        }
        int i4 = i + 1;
        iArr[i] = pad(i2, i3);
        String findName = this._symbols.findName(iArr, i4);
        return findName == null ? addName(iArr, i4, i3) : findName;
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00e3  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00c2  */
    private final java.lang.String addName(int[] r17, int r18, int r19) throws com.fasterxml.jackson.core.JsonParseException {
        /*
        r16 = this;
        r0 = r16;
        r1 = r17;
        r2 = r18;
        r3 = r19;
        r4 = r2 << 2;
        r5 = 4;
        r4 = r4 - r5;
        r4 = r4 + r3;
        r7 = 3;
        if (r3 >= r5) goto L_0x001c;
    L_0x0010:
        r8 = r2 + -1;
        r9 = r1[r8];
        r10 = 4 - r3;
        r10 = r10 << r7;
        r10 = r9 << r10;
        r1[r8] = r10;
        goto L_0x001d;
    L_0x001c:
        r9 = 0;
    L_0x001d:
        r8 = r0._textBuffer;
        r8 = r8.emptyAndGetCurrentSegment();
        r10 = r8;
        r8 = 0;
        r11 = 0;
    L_0x0026:
        if (r8 >= r4) goto L_0x00f7;
    L_0x0028:
        r12 = r8 >> 2;
        r12 = r1[r12];
        r13 = r8 & 3;
        r13 = 3 - r13;
        r13 = r13 << r7;
        r12 = r12 >> r13;
        r12 = r12 & 255;
        r8 = r8 + 1;
        r13 = 127; // 0x7f float:1.78E-43 double:6.27E-322;
        if (r12 <= r13) goto L_0x00e4;
    L_0x003a:
        r13 = r12 & 224;
        r14 = 192; // 0xc0 float:2.69E-43 double:9.5E-322;
        r5 = 1;
        if (r13 != r14) goto L_0x0046;
    L_0x0041:
        r12 = r12 & 31;
        r13 = r12;
        r12 = 1;
        goto L_0x0061;
    L_0x0046:
        r13 = r12 & 240;
        r14 = 224; // 0xe0 float:3.14E-43 double:1.107E-321;
        if (r13 != r14) goto L_0x0051;
    L_0x004c:
        r12 = r12 & 15;
        r13 = r12;
        r12 = 2;
        goto L_0x0061;
    L_0x0051:
        r13 = r12 & 248;
        r14 = 240; // 0xf0 float:3.36E-43 double:1.186E-321;
        if (r13 != r14) goto L_0x005c;
    L_0x0057:
        r12 = r12 & 7;
        r13 = r12;
        r12 = 3;
        goto L_0x0061;
    L_0x005c:
        r0._reportInvalidInitial(r12);
        r12 = 1;
        r13 = 1;
    L_0x0061:
        r14 = r8 + r12;
        if (r14 <= r4) goto L_0x006c;
    L_0x0065:
        r14 = " in field name";
        r6 = com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
        r0._reportInvalidEOF(r14, r6);
    L_0x006c:
        r6 = r8 >> 2;
        r6 = r1[r6];
        r14 = r8 & 3;
        r14 = 3 - r14;
        r14 = r14 << r7;
        r6 = r6 >> r14;
        r8 = r8 + 1;
        r14 = r6 & 192;
        r15 = 128; // 0x80 float:1.794E-43 double:6.32E-322;
        if (r14 == r15) goto L_0x0081;
    L_0x007e:
        r0._reportInvalidOther(r6);
    L_0x0081:
        r13 = r13 << 6;
        r6 = r6 & 63;
        r6 = r6 | r13;
        if (r12 <= r5) goto L_0x00be;
    L_0x0088:
        r5 = r8 >> 2;
        r5 = r1[r5];
        r13 = r8 & 3;
        r13 = 3 - r13;
        r13 = r13 << r7;
        r5 = r5 >> r13;
        r8 = r8 + 1;
        r13 = r5 & 192;
        if (r13 == r15) goto L_0x009b;
    L_0x0098:
        r0._reportInvalidOther(r5);
    L_0x009b:
        r6 = r6 << 6;
        r5 = r5 & 63;
        r5 = r5 | r6;
        r6 = 2;
        if (r12 <= r6) goto L_0x00c0;
    L_0x00a3:
        r6 = r8 >> 2;
        r6 = r1[r6];
        r13 = r8 & 3;
        r13 = 3 - r13;
        r13 = r13 << r7;
        r6 = r6 >> r13;
        r8 = r8 + 1;
        r13 = r6 & 192;
        if (r13 == r15) goto L_0x00b8;
    L_0x00b3:
        r13 = r6 & 255;
        r0._reportInvalidOther(r13);
    L_0x00b8:
        r5 = r5 << 6;
        r6 = r6 & 63;
        r5 = r5 | r6;
        goto L_0x00bf;
    L_0x00be:
        r5 = r6;
    L_0x00bf:
        r6 = 2;
    L_0x00c0:
        if (r12 <= r6) goto L_0x00e3;
    L_0x00c2:
        r6 = 65536; // 0x10000 float:9.18355E-41 double:3.2379E-319;
        r5 = r5 - r6;
        r6 = r10.length;
        if (r11 < r6) goto L_0x00cf;
    L_0x00c8:
        r6 = r0._textBuffer;
        r6 = r6.expandCurrentSegment();
        r10 = r6;
    L_0x00cf:
        r6 = r11 + 1;
        r12 = 55296; // 0xd800 float:7.7486E-41 double:2.732E-319;
        r13 = r5 >> 10;
        r13 = r13 + r12;
        r12 = (char) r13;
        r10[r11] = r12;
        r11 = 56320; // 0xdc00 float:7.8921E-41 double:2.7826E-319;
        r5 = r5 & 1023;
        r12 = r5 | r11;
        r11 = r6;
        goto L_0x00e4;
    L_0x00e3:
        r12 = r5;
    L_0x00e4:
        r5 = r10.length;
        if (r11 < r5) goto L_0x00ee;
    L_0x00e7:
        r5 = r0._textBuffer;
        r5 = r5.expandCurrentSegment();
        r10 = r5;
    L_0x00ee:
        r5 = r11 + 1;
        r6 = (char) r12;
        r10[r11] = r6;
        r11 = r5;
        r5 = 4;
        goto L_0x0026;
    L_0x00f7:
        r4 = new java.lang.String;
        r5 = 0;
        r4.<init>(r10, r5, r11);
        r5 = 4;
        if (r3 >= r5) goto L_0x0104;
    L_0x0100:
        r3 = r2 + -1;
        r1[r3] = r9;
    L_0x0104:
        r3 = r0._symbols;
        r1 = r3.addName(r4, r1, r2);
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8StreamJsonParser.addName(int[], int, int):java.lang.String");
    }

    /* Access modifiers changed, original: protected */
    public void _loadMoreGuaranteed() throws IOException {
        if (!_loadMore()) {
            _reportInvalidEOF();
        }
    }

    /* Access modifiers changed, original: protected */
    public void _finishString() throws IOException {
        int i = this._inputPtr;
        if (i >= this._inputEnd) {
            _loadMoreGuaranteed();
            i = this._inputPtr;
        }
        int i2 = 0;
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int[] iArr = _icUTF8;
        int min = Math.min(this._inputEnd, emptyAndGetCurrentSegment.length + i);
        byte[] bArr = this._inputBuffer;
        while (i < min) {
            int i3 = bArr[i] & 255;
            if (iArr[i3] != 0) {
                if (i3 == 34) {
                    this._inputPtr = i + 1;
                    this._textBuffer.setCurrentLength(i2);
                    return;
                }
                this._inputPtr = i;
                _finishString2(emptyAndGetCurrentSegment, i2);
            }
            i++;
            int i4 = i2 + 1;
            emptyAndGetCurrentSegment[i2] = (char) i3;
            i2 = i4;
        }
        this._inputPtr = i;
        _finishString2(emptyAndGetCurrentSegment, i2);
    }

    /* Access modifiers changed, original: protected */
    public String _finishAndReturnString() throws IOException {
        int i = this._inputPtr;
        if (i >= this._inputEnd) {
            _loadMoreGuaranteed();
            i = this._inputPtr;
        }
        int i2 = 0;
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int[] iArr = _icUTF8;
        int min = Math.min(this._inputEnd, emptyAndGetCurrentSegment.length + i);
        byte[] bArr = this._inputBuffer;
        while (i < min) {
            int i3 = bArr[i] & 255;
            if (iArr[i3] != 0) {
                if (i3 == 34) {
                    this._inputPtr = i + 1;
                    return this._textBuffer.setCurrentAndReturn(i2);
                }
                this._inputPtr = i;
                _finishString2(emptyAndGetCurrentSegment, i2);
                return this._textBuffer.contentsAsString();
            }
            i++;
            int i4 = i2 + 1;
            emptyAndGetCurrentSegment[i2] = (char) i3;
            i2 = i4;
        }
        this._inputPtr = i;
        _finishString2(emptyAndGetCurrentSegment, i2);
        return this._textBuffer.contentsAsString();
    }

    private final void _finishString2(char[] cArr, int i) throws IOException {
        int[] iArr = _icUTF8;
        byte[] bArr = this._inputBuffer;
        while (true) {
            int i2 = this._inputPtr;
            if (i2 >= this._inputEnd) {
                _loadMoreGuaranteed();
                i2 = this._inputPtr;
            }
            if (i >= cArr.length) {
                cArr = this._textBuffer.finishCurrentSegment();
                i = 0;
            }
            int min = Math.min(this._inputEnd, (cArr.length - i) + i2);
            while (i2 < min) {
                int i3 = i2 + 1;
                i2 = bArr[i2] & 255;
                if (iArr[i2] != 0) {
                    this._inputPtr = i3;
                    if (i2 == 34) {
                        this._textBuffer.setCurrentLength(i);
                        return;
                    }
                    switch (iArr[i2]) {
                        case 1:
                            i2 = _decodeEscaped();
                            break;
                        case 2:
                            i2 = _decodeUtf8_2(i2);
                            break;
                        case 3:
                            if (this._inputEnd - this._inputPtr < 2) {
                                i2 = _decodeUtf8_3(i2);
                                break;
                            } else {
                                i2 = _decodeUtf8_3fast(i2);
                                break;
                            }
                        case 4:
                            i2 = _decodeUtf8_4(i2);
                            min = i + 1;
                            cArr[i] = (char) (GeneratorBase.SURR1_FIRST | (i2 >> 10));
                            if (min >= cArr.length) {
                                cArr = this._textBuffer.finishCurrentSegment();
                                min = 0;
                            }
                            i2 = (i2 & 1023) | GeneratorBase.SURR2_FIRST;
                            i = min;
                            break;
                        default:
                            if (i2 >= 32) {
                                _reportInvalidChar(i2);
                                break;
                            } else {
                                _throwUnquotedSpace(i2, "string value");
                                break;
                            }
                    }
                    if (i >= cArr.length) {
                        cArr = this._textBuffer.finishCurrentSegment();
                        i = 0;
                    }
                    min = i + 1;
                    cArr[i] = (char) i2;
                    i = min;
                } else {
                    int i4 = i + 1;
                    cArr[i] = (char) i2;
                    i2 = i3;
                    i = i4;
                }
            }
            this._inputPtr = i2;
        }
    }

    /* Access modifiers changed, original: protected */
    public void _skipString() throws IOException {
        this._tokenIncomplete = false;
        int[] iArr = _icUTF8;
        byte[] bArr = this._inputBuffer;
        while (true) {
            int i = this._inputPtr;
            int i2 = this._inputEnd;
            if (i >= i2) {
                _loadMoreGuaranteed();
                i = this._inputPtr;
                i2 = this._inputEnd;
            }
            while (i < i2) {
                int i3 = i + 1;
                i = bArr[i] & 255;
                if (iArr[i] != 0) {
                    this._inputPtr = i3;
                    if (i != 34) {
                        switch (iArr[i]) {
                            case 1:
                                _decodeEscaped();
                                break;
                            case 2:
                                _skipUtf8_2();
                                break;
                            case 3:
                                _skipUtf8_3();
                                break;
                            case 4:
                                _skipUtf8_4(i);
                                break;
                            default:
                                if (i >= 32) {
                                    _reportInvalidChar(i);
                                    break;
                                } else {
                                    _throwUnquotedSpace(i, "string value");
                                    break;
                                }
                        }
                    }
                    return;
                }
                i = i3;
            }
            this._inputPtr = i;
        }
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00a7  */
    /* JADX WARNING: Missing block: B:19:0x0043, code skipped:
            if (r2._parsingContext.inArray() == false) goto L_0x00a1;
     */
    public com.fasterxml.jackson.core.JsonToken _handleUnexpectedValue(int r3) throws java.io.IOException {
        /*
        r2 = this;
        r0 = 39;
        if (r3 == r0) goto L_0x0094;
    L_0x0004:
        r0 = 73;
        r1 = 1;
        if (r3 == r0) goto L_0x0078;
    L_0x0009:
        r0 = 78;
        if (r3 == r0) goto L_0x005c;
    L_0x000d:
        r0 = 93;
        if (r3 == r0) goto L_0x003d;
    L_0x0011:
        r0 = 125; // 0x7d float:1.75E-43 double:6.2E-322;
        if (r3 == r0) goto L_0x0056;
    L_0x0015:
        switch(r3) {
            case 43: goto L_0x001a;
            case 44: goto L_0x0046;
            default: goto L_0x0018;
        };
    L_0x0018:
        goto L_0x00a1;
    L_0x001a:
        r3 = r2._inputPtr;
        r0 = r2._inputEnd;
        if (r3 < r0) goto L_0x002b;
    L_0x0020:
        r3 = r2._loadMore();
        if (r3 != 0) goto L_0x002b;
    L_0x0026:
        r3 = com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT;
        r2._reportInvalidEOFInValue(r3);
    L_0x002b:
        r3 = r2._inputBuffer;
        r0 = r2._inputPtr;
        r1 = r0 + 1;
        r2._inputPtr = r1;
        r3 = r3[r0];
        r3 = r3 & 255;
        r0 = 0;
        r3 = r2._handleInvalidNumberStart(r3, r0);
        return r3;
    L_0x003d:
        r0 = r2._parsingContext;
        r0 = r0.inArray();
        if (r0 != 0) goto L_0x0046;
    L_0x0045:
        goto L_0x00a1;
    L_0x0046:
        r0 = com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_MISSING_VALUES;
        r0 = r2.isEnabled(r0);
        if (r0 == 0) goto L_0x0056;
    L_0x004e:
        r3 = r2._inputPtr;
        r3 = r3 - r1;
        r2._inputPtr = r3;
        r3 = com.fasterxml.jackson.core.JsonToken.VALUE_NULL;
        return r3;
    L_0x0056:
        r0 = "expected a value";
        r2._reportUnexpectedChar(r3, r0);
        goto L_0x0094;
    L_0x005c:
        r0 = "NaN";
        r2._matchToken(r0, r1);
        r0 = com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS;
        r0 = r2.isEnabled(r0);
        if (r0 == 0) goto L_0x0072;
    L_0x0069:
        r3 = "NaN";
        r0 = 9221120237041090560; // 0x7ff8000000000000 float:0.0 double:NaN;
        r3 = r2.resetAsNaN(r3, r0);
        return r3;
    L_0x0072:
        r0 = "Non-standard token 'NaN': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow";
        r2._reportError(r0);
        goto L_0x00a1;
    L_0x0078:
        r0 = "Infinity";
        r2._matchToken(r0, r1);
        r0 = com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS;
        r0 = r2.isEnabled(r0);
        if (r0 == 0) goto L_0x008e;
    L_0x0085:
        r3 = "Infinity";
        r0 = 9218868437227405312; // 0x7ff0000000000000 float:0.0 double:Infinity;
        r3 = r2.resetAsNaN(r3, r0);
        return r3;
    L_0x008e:
        r0 = "Non-standard token 'Infinity': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow";
        r2._reportError(r0);
        goto L_0x00a1;
    L_0x0094:
        r0 = com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
        r0 = r2.isEnabled(r0);
        if (r0 == 0) goto L_0x00a1;
    L_0x009c:
        r3 = r2._handleApos();
        return r3;
    L_0x00a1:
        r0 = java.lang.Character.isJavaIdentifierStart(r3);
        if (r0 == 0) goto L_0x00be;
    L_0x00a7:
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r1 = "";
        r0.append(r1);
        r1 = (char) r3;
        r0.append(r1);
        r0 = r0.toString();
        r1 = "('true', 'false' or 'null')";
        r2._reportInvalidToken(r0, r1);
    L_0x00be:
        r0 = "expected a valid value (number, String, array, object, 'true', 'false' or 'null')";
        r2._reportUnexpectedChar(r3, r0);
        r3 = 0;
        return r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8StreamJsonParser._handleUnexpectedValue(int):com.fasterxml.jackson.core.JsonToken");
    }

    /* Access modifiers changed, original: protected */
    public JsonToken _handleApos() throws IOException {
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int[] iArr = _icUTF8;
        byte[] bArr = this._inputBuffer;
        char[] cArr = emptyAndGetCurrentSegment;
        int i = 0;
        while (true) {
            if (this._inputPtr >= this._inputEnd) {
                _loadMoreGuaranteed();
            }
            if (i >= cArr.length) {
                cArr = this._textBuffer.finishCurrentSegment();
                i = 0;
            }
            int i2 = this._inputEnd;
            int length = this._inputPtr + (cArr.length - i);
            if (length < i2) {
                i2 = length;
            }
            while (this._inputPtr < i2) {
                length = this._inputPtr;
                this._inputPtr = length + 1;
                length = bArr[length] & 255;
                if (length != 39 && iArr[length] == 0) {
                    int i3 = i + 1;
                    cArr[i] = (char) length;
                    i = i3;
                } else if (length == 39) {
                    this._textBuffer.setCurrentLength(i);
                    return JsonToken.VALUE_STRING;
                } else {
                    switch (iArr[length]) {
                        case 1:
                            length = _decodeEscaped();
                            break;
                        case 2:
                            length = _decodeUtf8_2(length);
                            break;
                        case 3:
                            if (this._inputEnd - this._inputPtr < 2) {
                                length = _decodeUtf8_3(length);
                                break;
                            }
                            length = _decodeUtf8_3fast(length);
                            break;
                        case 4:
                            i2 = _decodeUtf8_4(length);
                            length = i + 1;
                            cArr[i] = (char) (GeneratorBase.SURR1_FIRST | (i2 >> 10));
                            if (length >= cArr.length) {
                                cArr = this._textBuffer.finishCurrentSegment();
                                length = 0;
                            }
                            int i4 = length;
                            length = GeneratorBase.SURR2_FIRST | (i2 & 1023);
                            i = i4;
                            break;
                        default:
                            if (length < 32) {
                                _throwUnquotedSpace(length, "string value");
                            }
                            _reportInvalidChar(length);
                            break;
                    }
                    if (i >= cArr.length) {
                        cArr = this._textBuffer.finishCurrentSegment();
                        i = 0;
                    }
                    i2 = i + 1;
                    cArr[i] = (char) length;
                    i = i2;
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public JsonToken _handleInvalidNumberStart(int i, boolean z) throws IOException {
        while (i == 73) {
            String str;
            if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                _reportInvalidEOFInValue(JsonToken.VALUE_NUMBER_FLOAT);
            }
            byte[] bArr = this._inputBuffer;
            int i2 = this._inputPtr;
            this._inputPtr = i2 + 1;
            i = bArr[i2];
            if (i != 78) {
                if (i != 110) {
                    break;
                }
                str = z ? "-Infinity" : "+Infinity";
            } else {
                str = z ? "-INF" : "+INF";
            }
            _matchToken(str, 3);
            if (isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                return resetAsNaN(str, z ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Non-standard token '");
            stringBuilder.append(str);
            stringBuilder.append("': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            _reportError(stringBuilder.toString());
        }
        reportUnexpectedNumberChar(i, "expected digit (0-9) to follow minus sign, for valid numeric value");
        return null;
    }

    /* Access modifiers changed, original: protected|final */
    public final void _matchToken(String str, int i) throws IOException {
        int length = str.length();
        if (this._inputPtr + length >= this._inputEnd) {
            _matchToken2(str, i);
            return;
        }
        do {
            if (this._inputBuffer[this._inputPtr] != str.charAt(i)) {
                _reportInvalidToken(str.substring(0, i));
            }
            this._inputPtr++;
            i++;
        } while (i < length);
        length = this._inputBuffer[this._inputPtr] & 255;
        if (!(length < 48 || length == 93 || length == 125)) {
            _checkMatchEnd(str, i, length);
        }
    }

    private final void _matchToken2(String str, int i) throws IOException {
        int length = str.length();
        do {
            if ((this._inputPtr >= this._inputEnd && !_loadMore()) || this._inputBuffer[this._inputPtr] != str.charAt(i)) {
                _reportInvalidToken(str.substring(0, i));
            }
            this._inputPtr++;
            i++;
        } while (i < length);
        if (this._inputPtr < this._inputEnd || _loadMore()) {
            length = this._inputBuffer[this._inputPtr] & 255;
            if (!(length < 48 || length == 93 || length == 125)) {
                _checkMatchEnd(str, i, length);
            }
        }
    }

    private final void _checkMatchEnd(String str, int i, int i2) throws IOException {
        if (Character.isJavaIdentifierPart((char) _decodeCharForError(i2))) {
            _reportInvalidToken(str.substring(0, i));
        }
    }

    private final int _skipWS() throws IOException {
        while (this._inputPtr < this._inputEnd) {
            byte[] bArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            int i2 = bArr[i] & 255;
            if (i2 > 32) {
                if (i2 != 47 && i2 != 35) {
                    return i2;
                }
                this._inputPtr--;
                return _skipWS2();
            } else if (i2 != 32) {
                if (i2 == 10) {
                    this._currInputRow++;
                    this._currInputRowStart = this._inputPtr;
                } else if (i2 == 13) {
                    _skipCR();
                } else if (i2 != 9) {
                    _throwInvalidSpace(i2);
                }
            }
        }
        return _skipWS2();
    }

    private final int _skipWS2() throws IOException {
        int i;
        while (true) {
            if (this._inputPtr < this._inputEnd || _loadMore()) {
                byte[] bArr = this._inputBuffer;
                int i2 = this._inputPtr;
                this._inputPtr = i2 + 1;
                i = bArr[i2] & 255;
                if (i > 32) {
                    if (i == 47) {
                        _skipComment();
                    } else if (i != 35 || !_skipYAMLComment()) {
                        return i;
                    }
                } else if (i != 32) {
                    if (i == 10) {
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                    } else if (i == 13) {
                        _skipCR();
                    } else if (i != 9) {
                        _throwInvalidSpace(i);
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
        return i;
    }

    private final int _skipWSOrEnd() throws IOException {
        if (this._inputPtr >= this._inputEnd && !_loadMore()) {
            return _eofAsNextChar();
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        int i2 = bArr[i] & 255;
        if (i2 <= 32) {
            if (i2 != 32) {
                if (i2 == 10) {
                    this._currInputRow++;
                    this._currInputRowStart = this._inputPtr;
                } else if (i2 == 13) {
                    _skipCR();
                } else if (i2 != 9) {
                    _throwInvalidSpace(i2);
                }
            }
            while (this._inputPtr < this._inputEnd) {
                bArr = this._inputBuffer;
                int i3 = this._inputPtr;
                this._inputPtr = i3 + 1;
                i2 = bArr[i3] & 255;
                if (i2 > 32) {
                    if (i2 != 47 && i2 != 35) {
                        return i2;
                    }
                    this._inputPtr--;
                    return _skipWSOrEnd2();
                } else if (i2 != 32) {
                    if (i2 == 10) {
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                    } else if (i2 == 13) {
                        _skipCR();
                    } else if (i2 != 9) {
                        _throwInvalidSpace(i2);
                    }
                }
            }
            return _skipWSOrEnd2();
        } else if (i2 != 47 && i2 != 35) {
            return i2;
        } else {
            this._inputPtr--;
            return _skipWSOrEnd2();
        }
    }

    private final int _skipWSOrEnd2() throws IOException {
        int i;
        while (true) {
            if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                return _eofAsNextChar();
            }
            byte[] bArr = this._inputBuffer;
            int i2 = this._inputPtr;
            this._inputPtr = i2 + 1;
            i = bArr[i2] & 255;
            if (i > 32) {
                if (i == 47) {
                    _skipComment();
                } else if (i != 35 || !_skipYAMLComment()) {
                    return i;
                }
            } else if (i != 32) {
                if (i == 10) {
                    this._currInputRow++;
                    this._currInputRowStart = this._inputPtr;
                } else if (i == 13) {
                    _skipCR();
                } else if (i != 9) {
                    _throwInvalidSpace(i);
                }
            }
        }
        return i;
    }

    private final int _skipColon() throws IOException {
        if (this._inputPtr + 4 >= this._inputEnd) {
            return _skipColon2(false);
        }
        byte b = this._inputBuffer[this._inputPtr];
        byte[] bArr;
        int i;
        if (b == (byte) 58) {
            bArr = this._inputBuffer;
            i = this._inputPtr + 1;
            this._inputPtr = i;
            b = bArr[i];
            if (b <= (byte) 32) {
                if (b == (byte) 32 || b == (byte) 9) {
                    bArr = this._inputBuffer;
                    i = this._inputPtr + 1;
                    this._inputPtr = i;
                    b = bArr[i];
                    if (b > (byte) 32) {
                        if (b == (byte) 47 || b == (byte) 35) {
                            return _skipColon2(true);
                        }
                        this._inputPtr++;
                        return b;
                    }
                }
                return _skipColon2(true);
            } else if (b == (byte) 47 || b == (byte) 35) {
                return _skipColon2(true);
            } else {
                this._inputPtr++;
                return b;
            }
        }
        if (b == (byte) 32 || b == (byte) 9) {
            bArr = this._inputBuffer;
            int i2 = this._inputPtr + 1;
            this._inputPtr = i2;
            b = bArr[i2];
        }
        if (b != (byte) 58) {
            return _skipColon2(false);
        }
        bArr = this._inputBuffer;
        i = this._inputPtr + 1;
        this._inputPtr = i;
        b = bArr[i];
        if (b <= (byte) 32) {
            if (b == (byte) 32 || b == (byte) 9) {
                bArr = this._inputBuffer;
                i = this._inputPtr + 1;
                this._inputPtr = i;
                b = bArr[i];
                if (b > (byte) 32) {
                    if (b == (byte) 47 || b == (byte) 35) {
                        return _skipColon2(true);
                    }
                    this._inputPtr++;
                    return b;
                }
            }
            return _skipColon2(true);
        } else if (b == (byte) 47 || b == (byte) 35) {
            return _skipColon2(true);
        } else {
            this._inputPtr++;
            return b;
        }
    }

    private final int _skipColon2(boolean z) throws IOException {
        while (true) {
            if (this._inputPtr < this._inputEnd || _loadMore()) {
                byte[] bArr = this._inputBuffer;
                int i = this._inputPtr;
                this._inputPtr = i + 1;
                int i2 = bArr[i] & 255;
                if (i2 > 32) {
                    if (i2 == 47) {
                        _skipComment();
                    } else if (i2 != 35 || !_skipYAMLComment()) {
                        if (z) {
                            return i2;
                        }
                        if (i2 != 58) {
                            _reportUnexpectedChar(i2, "was expecting a colon to separate field name and value");
                        }
                        z = true;
                    }
                } else if (i2 != 32) {
                    if (i2 == 10) {
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                    } else if (i2 == 13) {
                        _skipCR();
                    } else if (i2 != 9) {
                        _throwInvalidSpace(i2);
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

    private final void _skipComment() throws IOException {
        if (!isEnabled(Feature.ALLOW_COMMENTS)) {
            _reportUnexpectedChar(47, "maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)");
        }
        if (this._inputPtr >= this._inputEnd && !_loadMore()) {
            _reportInvalidEOF(" in a comment", null);
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        int i2 = bArr[i] & 255;
        if (i2 == 47) {
            _skipLine();
        } else if (i2 == 42) {
            _skipCComment();
        } else {
            _reportUnexpectedChar(i2, "was expecting either '*' or '/' for a comment");
        }
    }

    private final void _skipCComment() throws IOException {
        int[] inputCodeComment = CharTypes.getInputCodeComment();
        while (true) {
            if (this._inputPtr < this._inputEnd || _loadMore()) {
                byte[] bArr = this._inputBuffer;
                int i = this._inputPtr;
                this._inputPtr = i + 1;
                int i2 = bArr[i] & 255;
                i = inputCodeComment[i2];
                if (i != 0) {
                    if (i == 10) {
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                    } else if (i == 13) {
                        _skipCR();
                    } else if (i != 42) {
                        switch (i) {
                            case 2:
                                _skipUtf8_2();
                                break;
                            case 3:
                                _skipUtf8_3();
                                break;
                            case 4:
                                _skipUtf8_4(i2);
                                break;
                            default:
                                _reportInvalidChar(i2);
                                break;
                        }
                    } else if (this._inputPtr < this._inputEnd || _loadMore()) {
                        if (this._inputBuffer[this._inputPtr] == (byte) 47) {
                            this._inputPtr++;
                            return;
                        }
                    }
                }
            }
        }
        _reportInvalidEOF(" in a comment", null);
    }

    private final boolean _skipYAMLComment() throws IOException {
        if (!isEnabled(Feature.ALLOW_YAML_COMMENTS)) {
            return false;
        }
        _skipLine();
        return true;
    }

    private final void _skipLine() throws IOException {
        int[] inputCodeComment = CharTypes.getInputCodeComment();
        while (true) {
            if (this._inputPtr < this._inputEnd || _loadMore()) {
                byte[] bArr = this._inputBuffer;
                int i = this._inputPtr;
                this._inputPtr = i + 1;
                int i2 = bArr[i] & 255;
                i = inputCodeComment[i2];
                if (i != 0) {
                    if (i == 10) {
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                        return;
                    } else if (i == 13) {
                        _skipCR();
                        return;
                    } else if (i != 42) {
                        switch (i) {
                            case 2:
                                _skipUtf8_2();
                                break;
                            case 3:
                                _skipUtf8_3();
                                break;
                            case 4:
                                _skipUtf8_4(i2);
                                break;
                            default:
                                if (i >= 0) {
                                    break;
                                }
                                _reportInvalidChar(i2);
                                break;
                        }
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
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        byte b = bArr[i];
        if (b == (byte) 34 || b == (byte) 47 || b == (byte) 92) {
            return (char) b;
        }
        if (b == (byte) 98) {
            return 8;
        }
        if (b == (byte) 102) {
            return 12;
        }
        if (b == (byte) 110) {
            return 10;
        }
        if (b == (byte) 114) {
            return 13;
        }
        switch (b) {
            case (byte) 116:
                return 9;
            case (byte) 117:
                i = 0;
                for (int i2 = 0; i2 < 4; i2++) {
                    if (this._inputPtr >= this._inputEnd && !_loadMore()) {
                        _reportInvalidEOF(" in character escape sequence", JsonToken.VALUE_STRING);
                    }
                    byte[] bArr2 = this._inputBuffer;
                    int i3 = this._inputPtr;
                    this._inputPtr = i3 + 1;
                    byte b2 = bArr2[i3];
                    i3 = CharTypes.charToHex(b2);
                    if (i3 < 0) {
                        _reportUnexpectedChar(b2, "expected a hex-digit for character escape sequence");
                    }
                    i = (i << 4) | i3;
                }
                return (char) i;
            default:
                return _handleUnrecognizedCharacterEscape((char) _decodeCharForError(b));
        }
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0036  */
    /* JADX WARNING: Removed duplicated region for block: B:29:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0042  */
    public int _decodeCharForError(int r7) throws java.io.IOException {
        /*
        r6 = this;
        r7 = r7 & 255;
        r0 = 127; // 0x7f float:1.78E-43 double:6.27E-322;
        if (r7 <= r0) goto L_0x0068;
    L_0x0006:
        r0 = r7 & 224;
        r1 = 2;
        r2 = 1;
        r3 = 192; // 0xc0 float:2.69E-43 double:9.5E-322;
        if (r0 != r3) goto L_0x0012;
    L_0x000e:
        r7 = r7 & 31;
    L_0x0010:
        r0 = 1;
        goto L_0x002c;
    L_0x0012:
        r0 = r7 & 240;
        r3 = 224; // 0xe0 float:3.14E-43 double:1.107E-321;
        if (r0 != r3) goto L_0x001c;
    L_0x0018:
        r7 = r7 & 15;
        r0 = 2;
        goto L_0x002c;
    L_0x001c:
        r0 = r7 & 248;
        r3 = 240; // 0xf0 float:3.36E-43 double:1.186E-321;
        if (r0 != r3) goto L_0x0026;
    L_0x0022:
        r7 = r7 & 7;
        r0 = 3;
        goto L_0x002c;
    L_0x0026:
        r0 = r7 & 255;
        r6._reportInvalidInitial(r0);
        goto L_0x0010;
    L_0x002c:
        r3 = r6.nextByte();
        r4 = r3 & 192;
        r5 = 128; // 0x80 float:1.794E-43 double:6.32E-322;
        if (r4 == r5) goto L_0x003b;
    L_0x0036:
        r4 = r3 & 255;
        r6._reportInvalidOther(r4);
    L_0x003b:
        r7 = r7 << 6;
        r3 = r3 & 63;
        r7 = r7 | r3;
        if (r0 <= r2) goto L_0x0068;
    L_0x0042:
        r2 = r6.nextByte();
        r3 = r2 & 192;
        if (r3 == r5) goto L_0x004f;
    L_0x004a:
        r3 = r2 & 255;
        r6._reportInvalidOther(r3);
    L_0x004f:
        r7 = r7 << 6;
        r2 = r2 & 63;
        r7 = r7 | r2;
        if (r0 <= r1) goto L_0x0068;
    L_0x0056:
        r0 = r6.nextByte();
        r1 = r0 & 192;
        if (r1 == r5) goto L_0x0063;
    L_0x005e:
        r1 = r0 & 255;
        r6._reportInvalidOther(r1);
    L_0x0063:
        r7 = r7 << 6;
        r0 = r0 & 63;
        r7 = r7 | r0;
    L_0x0068:
        return r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8StreamJsonParser._decodeCharForError(int):int");
    }

    private final int _decodeUtf8_2(int i) throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        return ((i & 31) << 6) | (b & 63);
    }

    private final int _decodeUtf8_3(int i) throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        i &= 15;
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        i = (i << 6) | (b & 63);
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        bArr = this._inputBuffer;
        i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        return (i << 6) | (b & 63);
    }

    private final int _decodeUtf8_3fast(int i) throws IOException {
        i &= 15;
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        i = (i << 6) | (b & 63);
        bArr = this._inputBuffer;
        i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        return (i << 6) | (b & 63);
    }

    private final int _decodeUtf8_4(int i) throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        i = ((i & 7) << 6) | (b & 63);
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        bArr = this._inputBuffer;
        i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        i = (i << 6) | (b & 63);
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        bArr = this._inputBuffer;
        i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        return ((i << 6) | (b & 63)) - 65536;
    }

    private final void _skipUtf8_2() throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        byte b = bArr[i];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
    }

    private final void _skipUtf8_3() throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        byte b = bArr[i];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        bArr = this._inputBuffer;
        i = this._inputPtr;
        this._inputPtr = i + 1;
        b = bArr[i];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
    }

    private final void _skipUtf8_4(int i) throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        bArr = this._inputBuffer;
        i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        bArr = this._inputBuffer;
        i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final void _skipCR() throws IOException {
        if ((this._inputPtr < this._inputEnd || _loadMore()) && this._inputBuffer[this._inputPtr] == BYTE_LF) {
            this._inputPtr++;
        }
        this._currInputRow++;
        this._currInputRowStart = this._inputPtr;
    }

    private int nextByte() throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            _loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        return bArr[i] & 255;
    }

    /* Access modifiers changed, original: protected */
    public void _reportInvalidToken(String str) throws IOException {
        _reportInvalidToken(str, "'null', 'true', 'false' or NaN");
    }

    /* Access modifiers changed, original: protected */
    public void _reportInvalidToken(String str, String str2) throws IOException {
        StringBuilder stringBuilder = new StringBuilder(str);
        while (stringBuilder.length() < 256 && (this._inputPtr < this._inputEnd || _loadMore())) {
            byte[] bArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            char _decodeCharForError = (char) _decodeCharForError(bArr[i]);
            if (!Character.isJavaIdentifierPart(_decodeCharForError)) {
                break;
            }
            stringBuilder.append(_decodeCharForError);
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

    /* Access modifiers changed, original: protected */
    public void _reportInvalidChar(int i) throws JsonParseException {
        if (i < 32) {
            _throwInvalidSpace(i);
        }
        _reportInvalidInitial(i);
    }

    /* Access modifiers changed, original: protected */
    public void _reportInvalidInitial(int i) throws JsonParseException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid UTF-8 start byte 0x");
        stringBuilder.append(Integer.toHexString(i));
        _reportError(stringBuilder.toString());
    }

    /* Access modifiers changed, original: protected */
    public void _reportInvalidOther(int i) throws JsonParseException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid UTF-8 middle byte 0x");
        stringBuilder.append(Integer.toHexString(i));
        _reportError(stringBuilder.toString());
    }

    /* Access modifiers changed, original: protected */
    public void _reportInvalidOther(int i, int i2) throws JsonParseException {
        this._inputPtr = i2;
        _reportInvalidOther(i);
    }

    public static int[] growArrayBy(int[] iArr, int i) {
        if (iArr == null) {
            return new int[i];
        }
        return Arrays.copyOf(iArr, iArr.length + i);
    }

    /* Access modifiers changed, original: protected|final */
    public final byte[] _decodeBase64(Base64Variant base64Variant) throws IOException {
        ByteArrayBuilder _getByteArrayBuilder = _getByteArrayBuilder();
        while (true) {
            if (this._inputPtr >= this._inputEnd) {
                _loadMoreGuaranteed();
            }
            byte[] bArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            int i2 = bArr[i] & 255;
            if (i2 > 32) {
                i = base64Variant.decodeBase64Char(i2);
                if (i < 0) {
                    if (i2 == 34) {
                        return _getByteArrayBuilder.toByteArray();
                    }
                    i = _decodeBase64Escape(base64Variant, i2, 0);
                    if (i < 0) {
                    }
                }
                if (this._inputPtr >= this._inputEnd) {
                    _loadMoreGuaranteed();
                }
                bArr = this._inputBuffer;
                int i3 = this._inputPtr;
                this._inputPtr = i3 + 1;
                i2 = bArr[i3] & 255;
                i3 = base64Variant.decodeBase64Char(i2);
                if (i3 < 0) {
                    i3 = _decodeBase64Escape(base64Variant, i2, 1);
                }
                i2 = (i << 6) | i3;
                if (this._inputPtr >= this._inputEnd) {
                    _loadMoreGuaranteed();
                }
                byte[] bArr2 = this._inputBuffer;
                i3 = this._inputPtr;
                this._inputPtr = i3 + 1;
                i = bArr2[i3] & 255;
                i3 = base64Variant.decodeBase64Char(i);
                if (i3 < 0) {
                    if (i3 != -2) {
                        if (i != 34 || base64Variant.usesPadding()) {
                            i3 = _decodeBase64Escape(base64Variant, i, 2);
                        } else {
                            _getByteArrayBuilder.append(i2 >> 4);
                            return _getByteArrayBuilder.toByteArray();
                        }
                    }
                    if (i3 == -2) {
                        if (this._inputPtr >= this._inputEnd) {
                            _loadMoreGuaranteed();
                        }
                        bArr2 = this._inputBuffer;
                        int i4 = this._inputPtr;
                        this._inputPtr = i4 + 1;
                        i = bArr2[i4] & 255;
                        if (base64Variant.usesPaddingChar(i)) {
                            _getByteArrayBuilder.append(i2 >> 4);
                        } else {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("expected padding character '");
                            stringBuilder.append(base64Variant.getPaddingChar());
                            stringBuilder.append("'");
                            throw reportInvalidBase64Char(base64Variant, i, 3, stringBuilder.toString());
                        }
                    }
                }
                i2 = (i2 << 6) | i3;
                if (this._inputPtr >= this._inputEnd) {
                    _loadMoreGuaranteed();
                }
                bArr2 = this._inputBuffer;
                i3 = this._inputPtr;
                this._inputPtr = i3 + 1;
                i = bArr2[i3] & 255;
                i3 = base64Variant.decodeBase64Char(i);
                if (i3 < 0) {
                    if (i3 != -2) {
                        if (i != 34 || base64Variant.usesPadding()) {
                            i3 = _decodeBase64Escape(base64Variant, i, 3);
                        } else {
                            _getByteArrayBuilder.appendTwoBytes(i2 >> 2);
                            return _getByteArrayBuilder.toByteArray();
                        }
                    }
                    if (i3 == -2) {
                        _getByteArrayBuilder.appendTwoBytes(i2 >> 2);
                    }
                }
                _getByteArrayBuilder.appendThreeBytes((i2 << 6) | i3);
            }
        }
    }

    public JsonLocation getTokenLocation() {
        Object sourceReference = this._ioContext.getSourceReference();
        if (this._currToken != JsonToken.FIELD_NAME) {
            return new JsonLocation(sourceReference, this._tokenInputTotal - 1, -1, this._tokenInputRow, this._tokenInputCol);
        }
        return new JsonLocation(sourceReference, ((long) (this._nameStartOffset - 1)) + this._currInputProcessed, -1, this._nameStartRow, this._nameStartCol);
    }

    public JsonLocation getCurrentLocation() {
        return new JsonLocation(this._ioContext.getSourceReference(), this._currInputProcessed + ((long) this._inputPtr), -1, this._currInputRow, (this._inputPtr - this._currInputRowStart) + 1);
    }

    private final void _updateLocation() {
        this._tokenInputRow = this._currInputRow;
        int i = this._inputPtr;
        this._tokenInputTotal = this._currInputProcessed + ((long) i);
        this._tokenInputCol = i - this._currInputRowStart;
    }

    private final void _updateNameLocation() {
        this._nameStartRow = this._currInputRow;
        int i = this._inputPtr;
        this._nameStartOffset = i;
        this._nameStartCol = i - this._currInputRowStart;
    }
}
