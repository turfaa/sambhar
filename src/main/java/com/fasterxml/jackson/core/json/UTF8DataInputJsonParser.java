package com.fasterxml.jackson.core.json;

import com.facebook.internal.ServerProtocol;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.base.GeneratorBase;
import com.fasterxml.jackson.core.base.ParserBase;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import java.io.DataInput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;

public class UTF8DataInputJsonParser extends ParserBase {
    static final byte BYTE_LF = (byte) 10;
    protected static final int[] _icLatin1 = CharTypes.getInputCodeLatin1();
    private static final int[] _icUTF8 = CharTypes.getInputCodeUtf8();
    protected DataInput _inputData;
    protected int _nextByte = -1;
    protected ObjectCodec _objectCodec;
    private int _quad1;
    protected int[] _quadBuffer = new int[16];
    protected final ByteQuadsCanonicalizer _symbols;
    protected boolean _tokenIncomplete;

    private static final int pad(int i, int i2) {
        return i2 == 4 ? i : i | (-1 << (i2 << 3));
    }

    /* Access modifiers changed, original: protected */
    public void _closeInput() throws IOException {
    }

    public int releaseBuffered(OutputStream outputStream) throws IOException {
        return 0;
    }

    public UTF8DataInputJsonParser(IOContext iOContext, int i, DataInput dataInput, ObjectCodec objectCodec, ByteQuadsCanonicalizer byteQuadsCanonicalizer, int i2) {
        super(iOContext, i);
        this._objectCodec = objectCodec;
        this._symbols = byteQuadsCanonicalizer;
        this._inputData = dataInput;
        this._nextByte = i2;
    }

    public ObjectCodec getCodec() {
        return this._objectCodec;
    }

    public void setCodec(ObjectCodec objectCodec) {
        this._objectCodec = objectCodec;
    }

    public Object getInputSource() {
        return this._inputData;
    }

    /* Access modifiers changed, original: protected */
    public void _releaseBuffers() throws IOException {
        super._releaseBuffers();
        this._symbols.release();
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
        if (this._currToken == JsonToken.VALUE_STRING) {
            if (this._tokenIncomplete) {
                this._tokenIncomplete = false;
                _finishString();
            }
            return this._textBuffer.size();
        } else if (this._currToken == JsonToken.FIELD_NAME) {
            return this._parsingContext.getCurrentName().length();
        } else {
            if (this._currToken == null) {
                return 0;
            }
            if (this._currToken.isNumeric()) {
                return this._textBuffer.size();
            }
            return this._currToken.asCharArray().length;
        }
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
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8DataInputJsonParser.getTextOffset():int");
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
            int readUnsignedByte = this._inputData.readUnsignedByte();
            if (readUnsignedByte > 32) {
                int decodeBase64Char = base64Variant.decodeBase64Char(readUnsignedByte);
                if (decodeBase64Char < 0) {
                    if (readUnsignedByte == 34) {
                        break;
                    }
                    decodeBase64Char = _decodeBase64Escape(base64Variant, readUnsignedByte, 0);
                    if (decodeBase64Char < 0) {
                    }
                }
                if (i > length) {
                    i2 += i;
                    outputStream.write(bArr, 0, i);
                    i = 0;
                }
                readUnsignedByte = this._inputData.readUnsignedByte();
                int decodeBase64Char2 = base64Variant.decodeBase64Char(readUnsignedByte);
                if (decodeBase64Char2 < 0) {
                    decodeBase64Char2 = _decodeBase64Escape(base64Variant, readUnsignedByte, 1);
                }
                readUnsignedByte = (decodeBase64Char << 6) | decodeBase64Char2;
                decodeBase64Char = this._inputData.readUnsignedByte();
                decodeBase64Char2 = base64Variant.decodeBase64Char(decodeBase64Char);
                if (decodeBase64Char2 < 0) {
                    if (decodeBase64Char2 != -2) {
                        if (decodeBase64Char == 34 && !base64Variant.usesPadding()) {
                            length = i + 1;
                            bArr[i] = (byte) (readUnsignedByte >> 4);
                            i = length;
                            break;
                        }
                        decodeBase64Char2 = _decodeBase64Escape(base64Variant, decodeBase64Char, 2);
                    }
                    if (decodeBase64Char2 == -2) {
                        decodeBase64Char = this._inputData.readUnsignedByte();
                        if (base64Variant.usesPaddingChar(decodeBase64Char)) {
                            decodeBase64Char = i + 1;
                            bArr[i] = (byte) (readUnsignedByte >> 4);
                            i = decodeBase64Char;
                        } else {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("expected padding character '");
                            stringBuilder.append(base64Variant.getPaddingChar());
                            stringBuilder.append("'");
                            throw reportInvalidBase64Char(base64Variant, decodeBase64Char, 3, stringBuilder.toString());
                        }
                    }
                }
                readUnsignedByte = (readUnsignedByte << 6) | decodeBase64Char2;
                decodeBase64Char = this._inputData.readUnsignedByte();
                decodeBase64Char2 = base64Variant.decodeBase64Char(decodeBase64Char);
                if (decodeBase64Char2 < 0) {
                    if (decodeBase64Char2 != -2) {
                        if (decodeBase64Char == 34 && !base64Variant.usesPadding()) {
                            int i3 = readUnsignedByte >> 2;
                            length = i + 1;
                            bArr[i] = (byte) (i3 >> 8);
                            i = length + 1;
                            bArr[length] = (byte) i3;
                            break;
                        }
                        decodeBase64Char2 = _decodeBase64Escape(base64Variant, decodeBase64Char, 3);
                    }
                    if (decodeBase64Char2 == -2) {
                        readUnsignedByte >>= 2;
                        decodeBase64Char = i + 1;
                        bArr[i] = (byte) (readUnsignedByte >> 8);
                        i = decodeBase64Char + 1;
                        bArr[decodeBase64Char] = (byte) readUnsignedByte;
                    }
                }
                readUnsignedByte = (readUnsignedByte << 6) | decodeBase64Char2;
                decodeBase64Char = i + 1;
                bArr[i] = (byte) (readUnsignedByte >> 16);
                i = decodeBase64Char + 1;
                bArr[decodeBase64Char] = (byte) (readUnsignedByte >> 8);
                decodeBase64Char = i + 1;
                bArr[i] = (byte) readUnsignedByte;
                i = decodeBase64Char;
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
        int _skipWS = _skipWS();
        this._binaryValue = null;
        this._tokenInputRow = this._currInputRow;
        JsonToken jsonToken;
        if (_skipWS == 93) {
            if (!this._parsingContext.inArray()) {
                _reportMismatchedEndMarker(_skipWS, '}');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            jsonToken = JsonToken.END_ARRAY;
            this._currToken = jsonToken;
            return jsonToken;
        } else if (_skipWS == 125) {
            if (!this._parsingContext.inObject()) {
                _reportMismatchedEndMarker(_skipWS, ']');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            jsonToken = JsonToken.END_OBJECT;
            this._currToken = jsonToken;
            return jsonToken;
        } else {
            if (this._parsingContext.expectComma()) {
                if (_skipWS != 44) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("was expecting comma to separate ");
                    stringBuilder.append(this._parsingContext.typeDesc());
                    stringBuilder.append(" entries");
                    _reportUnexpectedChar(_skipWS, stringBuilder.toString());
                }
                _skipWS = _skipWS();
            }
            if (!this._parsingContext.inObject()) {
                return _nextTokenNotInObject(_skipWS);
            }
            this._parsingContext.setCurrentName(_parseName(_skipWS));
            this._currToken = JsonToken.FIELD_NAME;
            _skipWS = _skipColon();
            if (_skipWS == 34) {
                this._tokenIncomplete = true;
                this._nextToken = JsonToken.VALUE_STRING;
                return this._currToken;
            }
            if (_skipWS == 45) {
                jsonToken = _parseNegNumber();
            } else if (_skipWS == 91) {
                jsonToken = JsonToken.START_ARRAY;
            } else if (_skipWS == 102) {
                _matchToken("false", 1);
                jsonToken = JsonToken.VALUE_FALSE;
            } else if (_skipWS == 110) {
                _matchToken("null", 1);
                jsonToken = JsonToken.VALUE_NULL;
            } else if (_skipWS == 116) {
                _matchToken(ServerProtocol.DIALOG_RETURN_SCOPES_TRUE, 1);
                jsonToken = JsonToken.VALUE_TRUE;
            } else if (_skipWS != 123) {
                switch (_skipWS) {
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
                        jsonToken = _parsePosNumber(_skipWS);
                        break;
                    default:
                        jsonToken = _handleUnexpectedValue(_skipWS);
                        break;
                }
            } else {
                jsonToken = JsonToken.START_OBJECT;
            }
            this._nextToken = jsonToken;
            return this._currToken;
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

    public String nextFieldName() throws IOException {
        this._numTypesValid = 0;
        if (this._currToken == JsonToken.FIELD_NAME) {
            _nextAfterName();
            return null;
        }
        if (this._tokenIncomplete) {
            _skipString();
        }
        int _skipWS = _skipWS();
        this._binaryValue = null;
        this._tokenInputRow = this._currInputRow;
        if (_skipWS == 93) {
            if (!this._parsingContext.inArray()) {
                _reportMismatchedEndMarker(_skipWS, '}');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            this._currToken = JsonToken.END_ARRAY;
            return null;
        } else if (_skipWS == 125) {
            if (!this._parsingContext.inObject()) {
                _reportMismatchedEndMarker(_skipWS, ']');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            this._currToken = JsonToken.END_OBJECT;
            return null;
        } else {
            if (this._parsingContext.expectComma()) {
                if (_skipWS != 44) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("was expecting comma to separate ");
                    stringBuilder.append(this._parsingContext.typeDesc());
                    stringBuilder.append(" entries");
                    _reportUnexpectedChar(_skipWS, stringBuilder.toString());
                }
                _skipWS = _skipWS();
            }
            if (this._parsingContext.inObject()) {
                String _parseName = _parseName(_skipWS);
                this._parsingContext.setCurrentName(_parseName);
                this._currToken = JsonToken.FIELD_NAME;
                int _skipColon = _skipColon();
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
            _nextTokenNotInObject(_skipWS);
            return null;
        }
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
        int i2 = 1;
        if (i == 48) {
            i = _handleLeadingZeroes();
            if (i > 57 || i < 48) {
                emptyAndGetCurrentSegment[0] = '0';
            } else {
                i2 = 0;
            }
        } else {
            emptyAndGetCurrentSegment[0] = (char) i;
            i = this._inputData.readUnsignedByte();
        }
        int i3 = i2;
        int i4 = i3;
        while (i <= 57 && i >= 48) {
            i4++;
            i2 = i3 + 1;
            emptyAndGetCurrentSegment[i3] = (char) i;
            i = this._inputData.readUnsignedByte();
            i3 = i2;
        }
        if (i == 46 || i == 101 || i == 69) {
            return _parseFloat(emptyAndGetCurrentSegment, i3, i, false, i4);
        }
        this._textBuffer.setCurrentLength(i3);
        if (this._parsingContext.inRoot()) {
            _verifyRootSpace();
        } else {
            this._nextByte = i;
        }
        return resetInt(false, i4);
    }

    /* Access modifiers changed, original: protected */
    public JsonToken _parseNegNumber() throws IOException {
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        emptyAndGetCurrentSegment[0] = '-';
        int readUnsignedByte = this._inputData.readUnsignedByte();
        emptyAndGetCurrentSegment[1] = (char) readUnsignedByte;
        if (readUnsignedByte <= 48) {
            if (readUnsignedByte != 48) {
                return _handleInvalidNumberStart(readUnsignedByte, true);
            }
            readUnsignedByte = _handleLeadingZeroes();
        } else if (readUnsignedByte > 57) {
            return _handleInvalidNumberStart(readUnsignedByte, true);
        } else {
            readUnsignedByte = this._inputData.readUnsignedByte();
        }
        int i = 2;
        int i2 = 1;
        while (readUnsignedByte <= 57 && readUnsignedByte >= 48) {
            i2++;
            int i3 = i + 1;
            emptyAndGetCurrentSegment[i] = (char) readUnsignedByte;
            readUnsignedByte = this._inputData.readUnsignedByte();
            i = i3;
        }
        if (readUnsignedByte == 46 || readUnsignedByte == 101 || readUnsignedByte == 69) {
            return _parseFloat(emptyAndGetCurrentSegment, i, readUnsignedByte, true, i2);
        }
        this._textBuffer.setCurrentLength(i);
        this._nextByte = readUnsignedByte;
        if (this._parsingContext.inRoot()) {
            _verifyRootSpace();
        }
        return resetInt(true, i2);
    }

    private final int _handleLeadingZeroes() throws IOException {
        int readUnsignedByte = this._inputData.readUnsignedByte();
        if (readUnsignedByte < 48 || readUnsignedByte > 57) {
            return readUnsignedByte;
        }
        if (!isEnabled(Feature.ALLOW_NUMERIC_LEADING_ZEROS)) {
            reportInvalidNumber("Leading zeroes not allowed");
        }
        while (readUnsignedByte == 48) {
            readUnsignedByte = this._inputData.readUnsignedByte();
        }
        return readUnsignedByte;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0031  */
    private final com.fasterxml.jackson.core.JsonToken _parseFloat(char[] r8, int r9, int r10, boolean r11, int r12) throws java.io.IOException {
        /*
        r7 = this;
        r0 = 57;
        r1 = 48;
        r2 = 0;
        r3 = 46;
        if (r10 != r3) goto L_0x003b;
    L_0x0009:
        r3 = r9 + 1;
        r10 = (char) r10;
        r8[r9] = r10;
        r10 = r8;
        r9 = r3;
        r8 = 0;
    L_0x0011:
        r3 = r7._inputData;
        r3 = r3.readUnsignedByte();
        if (r3 < r1) goto L_0x002f;
    L_0x0019:
        if (r3 <= r0) goto L_0x001c;
    L_0x001b:
        goto L_0x002f;
    L_0x001c:
        r8 = r8 + 1;
        r4 = r10.length;
        if (r9 < r4) goto L_0x0028;
    L_0x0021:
        r9 = r7._textBuffer;
        r10 = r9.finishCurrentSegment();
        r9 = 0;
    L_0x0028:
        r4 = r9 + 1;
        r3 = (char) r3;
        r10[r9] = r3;
        r9 = r4;
        goto L_0x0011;
    L_0x002f:
        if (r8 != 0) goto L_0x0036;
    L_0x0031:
        r4 = "Decimal point not followed by a digit";
        r7.reportUnexpectedNumberChar(r3, r4);
    L_0x0036:
        r6 = r3;
        r3 = r8;
        r8 = r10;
        r10 = r6;
        goto L_0x003c;
    L_0x003b:
        r3 = 0;
    L_0x003c:
        r4 = 101; // 0x65 float:1.42E-43 double:5.0E-322;
        if (r10 == r4) goto L_0x0047;
    L_0x0040:
        r4 = 69;
        if (r10 != r4) goto L_0x0045;
    L_0x0044:
        goto L_0x0047;
    L_0x0045:
        r8 = 0;
        goto L_0x00a5;
    L_0x0047:
        r4 = r8.length;
        if (r9 < r4) goto L_0x0051;
    L_0x004a:
        r8 = r7._textBuffer;
        r8 = r8.finishCurrentSegment();
        r9 = 0;
    L_0x0051:
        r4 = r9 + 1;
        r10 = (char) r10;
        r8[r9] = r10;
        r9 = r7._inputData;
        r9 = r9.readUnsignedByte();
        r10 = 45;
        if (r9 == r10) goto L_0x0069;
    L_0x0060:
        r10 = 43;
        if (r9 != r10) goto L_0x0065;
    L_0x0064:
        goto L_0x0069;
    L_0x0065:
        r10 = r9;
        r9 = r8;
        r8 = 0;
        goto L_0x0080;
    L_0x0069:
        r10 = r8.length;
        if (r4 < r10) goto L_0x0073;
    L_0x006c:
        r8 = r7._textBuffer;
        r8 = r8.finishCurrentSegment();
        r4 = 0;
    L_0x0073:
        r10 = r4 + 1;
        r9 = (char) r9;
        r8[r4] = r9;
        r9 = r7._inputData;
        r9 = r9.readUnsignedByte();
        r4 = r10;
        goto L_0x0065;
    L_0x0080:
        if (r10 > r0) goto L_0x009d;
    L_0x0082:
        if (r10 < r1) goto L_0x009d;
    L_0x0084:
        r8 = r8 + 1;
        r5 = r9.length;
        if (r4 < r5) goto L_0x0090;
    L_0x0089:
        r9 = r7._textBuffer;
        r9 = r9.finishCurrentSegment();
        r4 = 0;
    L_0x0090:
        r5 = r4 + 1;
        r10 = (char) r10;
        r9[r4] = r10;
        r10 = r7._inputData;
        r10 = r10.readUnsignedByte();
        r4 = r5;
        goto L_0x0080;
    L_0x009d:
        if (r8 != 0) goto L_0x00a4;
    L_0x009f:
        r9 = "Exponent indicator not followed by a digit";
        r7.reportUnexpectedNumberChar(r10, r9);
    L_0x00a4:
        r9 = r4;
    L_0x00a5:
        r7._nextByte = r10;
        r10 = r7._parsingContext;
        r10 = r10.inRoot();
        if (r10 == 0) goto L_0x00b2;
    L_0x00af:
        r7._verifyRootSpace();
    L_0x00b2:
        r10 = r7._textBuffer;
        r10.setCurrentLength(r9);
        r8 = r7.resetFloat(r11, r12, r3, r8);
        return r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8DataInputJsonParser._parseFloat(char[], int, int, boolean, int):com.fasterxml.jackson.core.JsonToken");
    }

    private final void _verifyRootSpace() throws IOException {
        int i = this._nextByte;
        if (i <= 32) {
            this._nextByte = -1;
            if (i == 13 || i == 10) {
                this._currInputRow++;
            }
            return;
        }
        _reportMissingRootWS(i);
    }

    /* Access modifiers changed, original: protected|final */
    public final String _parseName(int i) throws IOException {
        if (i != 34) {
            return _handleOddName(i);
        }
        int[] iArr = _icLatin1;
        int readUnsignedByte = this._inputData.readUnsignedByte();
        if (iArr[readUnsignedByte] == 0) {
            int readUnsignedByte2 = this._inputData.readUnsignedByte();
            if (iArr[readUnsignedByte2] == 0) {
                readUnsignedByte = (readUnsignedByte << 8) | readUnsignedByte2;
                readUnsignedByte2 = this._inputData.readUnsignedByte();
                if (iArr[readUnsignedByte2] == 0) {
                    readUnsignedByte = (readUnsignedByte << 8) | readUnsignedByte2;
                    readUnsignedByte2 = this._inputData.readUnsignedByte();
                    if (iArr[readUnsignedByte2] == 0) {
                        readUnsignedByte = (readUnsignedByte << 8) | readUnsignedByte2;
                        readUnsignedByte2 = this._inputData.readUnsignedByte();
                        if (iArr[readUnsignedByte2] == 0) {
                            this._quad1 = readUnsignedByte;
                            return _parseMediumName(readUnsignedByte2);
                        } else if (readUnsignedByte2 == 34) {
                            return findName(readUnsignedByte, 4);
                        } else {
                            return parseName(readUnsignedByte, readUnsignedByte2, 4);
                        }
                    } else if (readUnsignedByte2 == 34) {
                        return findName(readUnsignedByte, 3);
                    } else {
                        return parseName(readUnsignedByte, readUnsignedByte2, 3);
                    }
                } else if (readUnsignedByte2 == 34) {
                    return findName(readUnsignedByte, 2);
                } else {
                    return parseName(readUnsignedByte, readUnsignedByte2, 2);
                }
            } else if (readUnsignedByte2 == 34) {
                return findName(readUnsignedByte, 1);
            } else {
                return parseName(readUnsignedByte, readUnsignedByte2, 1);
            }
        } else if (readUnsignedByte == 34) {
            return "";
        } else {
            return parseName(0, readUnsignedByte, 0);
        }
    }

    private final String _parseMediumName(int i) throws IOException {
        int[] iArr = _icLatin1;
        int readUnsignedByte = this._inputData.readUnsignedByte();
        if (iArr[readUnsignedByte] == 0) {
            i = (i << 8) | readUnsignedByte;
            readUnsignedByte = this._inputData.readUnsignedByte();
            if (iArr[readUnsignedByte] == 0) {
                i = (i << 8) | readUnsignedByte;
                readUnsignedByte = this._inputData.readUnsignedByte();
                if (iArr[readUnsignedByte] == 0) {
                    i = (i << 8) | readUnsignedByte;
                    readUnsignedByte = this._inputData.readUnsignedByte();
                    if (iArr[readUnsignedByte] == 0) {
                        return _parseMediumName2(readUnsignedByte, i);
                    }
                    if (readUnsignedByte == 34) {
                        return findName(this._quad1, i, 4);
                    }
                    return parseName(this._quad1, i, readUnsignedByte, 4);
                } else if (readUnsignedByte == 34) {
                    return findName(this._quad1, i, 3);
                } else {
                    return parseName(this._quad1, i, readUnsignedByte, 3);
                }
            } else if (readUnsignedByte == 34) {
                return findName(this._quad1, i, 2);
            } else {
                return parseName(this._quad1, i, readUnsignedByte, 2);
            }
        } else if (readUnsignedByte == 34) {
            return findName(this._quad1, i, 1);
        } else {
            return parseName(this._quad1, i, readUnsignedByte, 1);
        }
    }

    private final String _parseMediumName2(int i, int i2) throws IOException {
        int[] iArr = _icLatin1;
        int readUnsignedByte = this._inputData.readUnsignedByte();
        if (iArr[readUnsignedByte] == 0) {
            int i3 = (i << 8) | readUnsignedByte;
            readUnsignedByte = this._inputData.readUnsignedByte();
            if (iArr[readUnsignedByte] == 0) {
                i3 = (i3 << 8) | readUnsignedByte;
                readUnsignedByte = this._inputData.readUnsignedByte();
                if (iArr[readUnsignedByte] == 0) {
                    i3 = (i3 << 8) | readUnsignedByte;
                    readUnsignedByte = this._inputData.readUnsignedByte();
                    if (iArr[readUnsignedByte] == 0) {
                        return _parseLongName(readUnsignedByte, i2, i3);
                    }
                    if (readUnsignedByte == 34) {
                        return findName(this._quad1, i2, i3, 4);
                    }
                    return parseName(this._quad1, i2, i3, readUnsignedByte, 4);
                } else if (readUnsignedByte == 34) {
                    return findName(this._quad1, i2, i3, 3);
                } else {
                    return parseName(this._quad1, i2, i3, readUnsignedByte, 3);
                }
            } else if (readUnsignedByte == 34) {
                return findName(this._quad1, i2, i3, 2);
            } else {
                return parseName(this._quad1, i2, i3, readUnsignedByte, 2);
            }
        } else if (readUnsignedByte == 34) {
            return findName(this._quad1, i2, i, 1);
        } else {
            return parseName(this._quad1, i2, i, readUnsignedByte, 1);
        }
    }

    private final String _parseLongName(int i, int i2, int i3) throws IOException {
        this._quadBuffer[0] = this._quad1;
        this._quadBuffer[1] = i2;
        this._quadBuffer[2] = i3;
        int[] iArr = _icLatin1;
        int i4 = i;
        int i5 = 3;
        while (true) {
            int readUnsignedByte = this._inputData.readUnsignedByte();
            if (iArr[readUnsignedByte] == 0) {
                int i6 = (i4 << 8) | readUnsignedByte;
                int readUnsignedByte2 = this._inputData.readUnsignedByte();
                if (iArr[readUnsignedByte2] == 0) {
                    i6 = (i6 << 8) | readUnsignedByte2;
                    readUnsignedByte2 = this._inputData.readUnsignedByte();
                    if (iArr[readUnsignedByte2] == 0) {
                        i6 = (i6 << 8) | readUnsignedByte2;
                        readUnsignedByte2 = this._inputData.readUnsignedByte();
                        if (iArr[readUnsignedByte2] == 0) {
                            if (i5 >= this._quadBuffer.length) {
                                this._quadBuffer = _growArrayBy(this._quadBuffer, i5);
                            }
                            int i7 = i5 + 1;
                            this._quadBuffer[i5] = i6;
                            i5 = i7;
                            i4 = readUnsignedByte2;
                        } else if (readUnsignedByte2 == 34) {
                            return findName(this._quadBuffer, i5, i6, 4);
                        } else {
                            return parseEscapedName(this._quadBuffer, i5, i6, readUnsignedByte2, 4);
                        }
                    } else if (readUnsignedByte2 == 34) {
                        return findName(this._quadBuffer, i5, i6, 3);
                    } else {
                        return parseEscapedName(this._quadBuffer, i5, i6, readUnsignedByte2, 3);
                    }
                } else if (readUnsignedByte2 == 34) {
                    return findName(this._quadBuffer, i5, i6, 2);
                } else {
                    return parseEscapedName(this._quadBuffer, i5, i6, readUnsignedByte2, 2);
                }
            } else if (readUnsignedByte == 34) {
                return findName(this._quadBuffer, i5, i4, 1);
            } else {
                return parseEscapedName(this._quadBuffer, i5, i4, readUnsignedByte, 1);
            }
        }
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
                            iArr = _growArrayBy(iArr, iArr.length);
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
                                iArr = _growArrayBy(iArr, iArr.length);
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
                    iArr = _growArrayBy(iArr, iArr.length);
                    this._quadBuffer = iArr;
                }
                i4 = i + 1;
                iArr[i] = i2;
                i2 = i3;
                i = i4;
                i4 = 1;
            }
            i3 = this._inputData.readUnsignedByte();
        }
        if (i4 > 0) {
            if (i >= iArr.length) {
                iArr = _growArrayBy(iArr, iArr.length);
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
        do {
            if (i3 < 4) {
                i3++;
                i = (i << 8) | i5;
            } else {
                if (i4 >= iArr.length) {
                    iArr = _growArrayBy(iArr, iArr.length);
                    this._quadBuffer = iArr;
                }
                i3 = i4 + 1;
                iArr[i4] = i;
                i = i5;
                i4 = i3;
                i3 = 1;
            }
            i5 = this._inputData.readUnsignedByte();
        } while (inputCodeUtf8JsNames[i5] == 0);
        this._nextByte = i5;
        if (i3 > 0) {
            if (i4 >= iArr.length) {
                iArr = _growArrayBy(iArr, iArr.length);
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
        int readUnsignedByte = this._inputData.readUnsignedByte();
        if (readUnsignedByte == 39) {
            return "";
        }
        int[] iArr = this._quadBuffer;
        int[] iArr2 = _icLatin1;
        int[] iArr3 = iArr;
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        while (readUnsignedByte != 39) {
            if (!(readUnsignedByte == 34 || iArr2[readUnsignedByte] == 0)) {
                if (readUnsignedByte != 92) {
                    _throwUnquotedSpace(readUnsignedByte, "name");
                } else {
                    readUnsignedByte = _decodeEscaped();
                }
                if (readUnsignedByte > 127) {
                    if (i >= 4) {
                        if (i2 >= iArr3.length) {
                            iArr3 = _growArrayBy(iArr3, iArr3.length);
                            this._quadBuffer = iArr3;
                        }
                        i = i2 + 1;
                        iArr3[i2] = i3;
                        i2 = i;
                        i = 0;
                        i3 = 0;
                    }
                    if (readUnsignedByte < 2048) {
                        i3 = (i3 << 8) | ((readUnsignedByte >> 6) | 192);
                        i++;
                    } else {
                        i3 = (i3 << 8) | ((readUnsignedByte >> 12) | 224);
                        i++;
                        if (i >= 4) {
                            if (i2 >= iArr3.length) {
                                iArr3 = _growArrayBy(iArr3, iArr3.length);
                                this._quadBuffer = iArr3;
                            }
                            i = i2 + 1;
                            iArr3[i2] = i3;
                            i2 = i;
                            i = 0;
                            i3 = 0;
                        }
                        i3 = (i3 << 8) | (((readUnsignedByte >> 6) & 63) | 128);
                        i++;
                    }
                    readUnsignedByte = (readUnsignedByte & 63) | 128;
                }
            }
            if (i < 4) {
                i++;
                i3 = readUnsignedByte | (i3 << 8);
            } else {
                if (i2 >= iArr3.length) {
                    iArr3 = _growArrayBy(iArr3, iArr3.length);
                    this._quadBuffer = iArr3;
                }
                i = i2 + 1;
                iArr3[i2] = i3;
                i3 = readUnsignedByte;
                i2 = i;
                i = 1;
            }
            readUnsignedByte = this._inputData.readUnsignedByte();
        }
        if (i > 0) {
            if (i2 >= iArr3.length) {
                iArr3 = _growArrayBy(iArr3, iArr3.length);
                this._quadBuffer = iArr3;
            }
            readUnsignedByte = i2 + 1;
            iArr3[i2] = pad(i3, i);
        } else {
            readUnsignedByte = i2;
        }
        String findName = this._symbols.findName(iArr3, readUnsignedByte);
        if (findName == null) {
            findName = addName(iArr3, readUnsignedByte, i);
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
            iArr = _growArrayBy(iArr, iArr.length);
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
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8DataInputJsonParser.addName(int[], int, int):java.lang.String");
    }

    /* Access modifiers changed, original: protected */
    public void _finishString() throws IOException {
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int[] iArr = _icUTF8;
        int length = emptyAndGetCurrentSegment.length;
        int i = 0;
        while (true) {
            int readUnsignedByte = this._inputData.readUnsignedByte();
            if (iArr[readUnsignedByte] == 0) {
                int i2 = i + 1;
                emptyAndGetCurrentSegment[i] = (char) readUnsignedByte;
                if (i2 >= length) {
                    _finishString2(emptyAndGetCurrentSegment, i2, this._inputData.readUnsignedByte());
                    return;
                }
                i = i2;
            } else if (readUnsignedByte == 34) {
                this._textBuffer.setCurrentLength(i);
                return;
            } else {
                _finishString2(emptyAndGetCurrentSegment, i, readUnsignedByte);
                return;
            }
        }
    }

    private String _finishAndReturnString() throws IOException {
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int[] iArr = _icUTF8;
        int length = emptyAndGetCurrentSegment.length;
        int i = 0;
        while (true) {
            int readUnsignedByte = this._inputData.readUnsignedByte();
            if (iArr[readUnsignedByte] == 0) {
                int i2 = i + 1;
                emptyAndGetCurrentSegment[i] = (char) readUnsignedByte;
                if (i2 >= length) {
                    _finishString2(emptyAndGetCurrentSegment, i2, this._inputData.readUnsignedByte());
                    return this._textBuffer.contentsAsString();
                }
                i = i2;
            } else if (readUnsignedByte == 34) {
                return this._textBuffer.setCurrentAndReturn(i);
            } else {
                _finishString2(emptyAndGetCurrentSegment, i, readUnsignedByte);
                return this._textBuffer.contentsAsString();
            }
        }
    }

    private final void _finishString2(char[] cArr, int i, int i2) throws IOException {
        int[] iArr = _icUTF8;
        int length = cArr.length;
        while (true) {
            int i3;
            if (iArr[i2] == 0) {
                if (i >= length) {
                    cArr = this._textBuffer.finishCurrentSegment();
                    length = cArr.length;
                    i = 0;
                }
                i3 = i + 1;
                cArr[i] = (char) i2;
                i2 = this._inputData.readUnsignedByte();
            } else if (i2 == 34) {
                this._textBuffer.setCurrentLength(i);
                return;
            } else {
                switch (iArr[i2]) {
                    case 1:
                        i2 = _decodeEscaped();
                        break;
                    case 2:
                        i2 = _decodeUtf8_2(i2);
                        break;
                    case 3:
                        i2 = _decodeUtf8_3(i2);
                        break;
                    case 4:
                        i2 = _decodeUtf8_4(i2);
                        i3 = i + 1;
                        cArr[i] = (char) (GeneratorBase.SURR1_FIRST | (i2 >> 10));
                        if (i3 >= cArr.length) {
                            cArr = this._textBuffer.finishCurrentSegment();
                            length = cArr.length;
                            i = 0;
                        } else {
                            i = i3;
                        }
                        i2 = (i2 & 1023) | GeneratorBase.SURR2_FIRST;
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
                    length = cArr.length;
                    i = 0;
                }
                i3 = i + 1;
                cArr[i] = (char) i2;
                i2 = this._inputData.readUnsignedByte();
            }
            i = i3;
        }
    }

    /* Access modifiers changed, original: protected */
    public void _skipString() throws IOException {
        this._tokenIncomplete = false;
        int[] iArr = _icUTF8;
        while (true) {
            int readUnsignedByte = this._inputData.readUnsignedByte();
            if (iArr[readUnsignedByte] != 0) {
                if (readUnsignedByte != 34) {
                    switch (iArr[readUnsignedByte]) {
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
                            _skipUtf8_4();
                            break;
                        default:
                            if (readUnsignedByte >= 32) {
                                _reportInvalidChar(readUnsignedByte);
                                break;
                            } else {
                                _throwUnquotedSpace(readUnsignedByte, "string value");
                                break;
                            }
                    }
                }
                return;
            }
        }
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x008c  */
    /* JADX WARNING: Missing block: B:14:0x002b, code skipped:
            if (r2._parsingContext.inArray() == false) goto L_0x0086;
     */
    public com.fasterxml.jackson.core.JsonToken _handleUnexpectedValue(int r3) throws java.io.IOException {
        /*
        r2 = this;
        r0 = 39;
        if (r3 == r0) goto L_0x0079;
    L_0x0004:
        r0 = 73;
        r1 = 1;
        if (r3 == r0) goto L_0x005d;
    L_0x0009:
        r0 = 78;
        if (r3 == r0) goto L_0x0041;
    L_0x000d:
        r0 = 93;
        if (r3 == r0) goto L_0x0025;
    L_0x0011:
        r0 = 125; // 0x7d float:1.75E-43 double:6.2E-322;
        if (r3 == r0) goto L_0x003b;
    L_0x0015:
        switch(r3) {
            case 43: goto L_0x0019;
            case 44: goto L_0x002e;
            default: goto L_0x0018;
        };
    L_0x0018:
        goto L_0x0086;
    L_0x0019:
        r3 = r2._inputData;
        r3 = r3.readUnsignedByte();
        r0 = 0;
        r3 = r2._handleInvalidNumberStart(r3, r0);
        return r3;
    L_0x0025:
        r0 = r2._parsingContext;
        r0 = r0.inArray();
        if (r0 != 0) goto L_0x002e;
    L_0x002d:
        goto L_0x0086;
    L_0x002e:
        r0 = com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_MISSING_VALUES;
        r0 = r2.isEnabled(r0);
        if (r0 == 0) goto L_0x003b;
    L_0x0036:
        r2._nextByte = r3;
        r3 = com.fasterxml.jackson.core.JsonToken.VALUE_NULL;
        return r3;
    L_0x003b:
        r0 = "expected a value";
        r2._reportUnexpectedChar(r3, r0);
        goto L_0x0079;
    L_0x0041:
        r0 = "NaN";
        r2._matchToken(r0, r1);
        r0 = com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS;
        r0 = r2.isEnabled(r0);
        if (r0 == 0) goto L_0x0057;
    L_0x004e:
        r3 = "NaN";
        r0 = 9221120237041090560; // 0x7ff8000000000000 float:0.0 double:NaN;
        r3 = r2.resetAsNaN(r3, r0);
        return r3;
    L_0x0057:
        r0 = "Non-standard token 'NaN': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow";
        r2._reportError(r0);
        goto L_0x0086;
    L_0x005d:
        r0 = "Infinity";
        r2._matchToken(r0, r1);
        r0 = com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS;
        r0 = r2.isEnabled(r0);
        if (r0 == 0) goto L_0x0073;
    L_0x006a:
        r3 = "Infinity";
        r0 = 9218868437227405312; // 0x7ff0000000000000 float:0.0 double:Infinity;
        r3 = r2.resetAsNaN(r3, r0);
        return r3;
    L_0x0073:
        r0 = "Non-standard token 'Infinity': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow";
        r2._reportError(r0);
        goto L_0x0086;
    L_0x0079:
        r0 = com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
        r0 = r2.isEnabled(r0);
        if (r0 == 0) goto L_0x0086;
    L_0x0081:
        r3 = r2._handleApos();
        return r3;
    L_0x0086:
        r0 = java.lang.Character.isJavaIdentifierStart(r3);
        if (r0 == 0) goto L_0x00a3;
    L_0x008c:
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r1 = "";
        r0.append(r1);
        r1 = (char) r3;
        r0.append(r1);
        r0 = r0.toString();
        r1 = "('true', 'false' or 'null')";
        r2._reportInvalidToken(r3, r0, r1);
    L_0x00a3:
        r0 = "expected a valid value (number, String, array, object, 'true', 'false' or 'null')";
        r2._reportUnexpectedChar(r3, r0);
        r3 = 0;
        return r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8DataInputJsonParser._handleUnexpectedValue(int):com.fasterxml.jackson.core.JsonToken");
    }

    /* Access modifiers changed, original: protected */
    public JsonToken _handleApos() throws IOException {
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int[] iArr = _icUTF8;
        int i = 0;
        while (true) {
            int length = emptyAndGetCurrentSegment.length;
            if (i >= emptyAndGetCurrentSegment.length) {
                emptyAndGetCurrentSegment = this._textBuffer.finishCurrentSegment();
                length = emptyAndGetCurrentSegment.length;
                i = 0;
            }
            while (true) {
                int readUnsignedByte = this._inputData.readUnsignedByte();
                if (readUnsignedByte == 39) {
                    this._textBuffer.setCurrentLength(i);
                    return JsonToken.VALUE_STRING;
                } else if (iArr[readUnsignedByte] != 0) {
                    switch (iArr[readUnsignedByte]) {
                        case 1:
                            readUnsignedByte = _decodeEscaped();
                            break;
                        case 2:
                            readUnsignedByte = _decodeUtf8_2(readUnsignedByte);
                            break;
                        case 3:
                            readUnsignedByte = _decodeUtf8_3(readUnsignedByte);
                            break;
                        case 4:
                            length = _decodeUtf8_4(readUnsignedByte);
                            readUnsignedByte = i + 1;
                            emptyAndGetCurrentSegment[i] = (char) (GeneratorBase.SURR1_FIRST | (length >> 10));
                            if (readUnsignedByte >= emptyAndGetCurrentSegment.length) {
                                emptyAndGetCurrentSegment = this._textBuffer.finishCurrentSegment();
                                readUnsignedByte = 0;
                            }
                            int i2 = readUnsignedByte;
                            readUnsignedByte = GeneratorBase.SURR2_FIRST | (length & 1023);
                            i = i2;
                            break;
                        default:
                            if (readUnsignedByte < 32) {
                                _throwUnquotedSpace(readUnsignedByte, "string value");
                            }
                            _reportInvalidChar(readUnsignedByte);
                            break;
                    }
                    if (i >= emptyAndGetCurrentSegment.length) {
                        emptyAndGetCurrentSegment = this._textBuffer.finishCurrentSegment();
                        i = 0;
                    }
                    length = i + 1;
                    emptyAndGetCurrentSegment[i] = (char) readUnsignedByte;
                    i = length;
                } else {
                    int i3 = i + 1;
                    emptyAndGetCurrentSegment[i] = (char) readUnsignedByte;
                    i = i3 >= length ? i3 : i3;
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public JsonToken _handleInvalidNumberStart(int i, boolean z) throws IOException {
        while (i == 73) {
            String str;
            i = this._inputData.readUnsignedByte();
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
        do {
            char readUnsignedByte = this._inputData.readUnsignedByte();
            if (readUnsignedByte != str.charAt(i)) {
                _reportInvalidToken(readUnsignedByte, str.substring(0, i));
            }
            i++;
        } while (i < length);
        length = this._inputData.readUnsignedByte();
        if (!(length < 48 || length == 93 || length == 125)) {
            _checkMatchEnd(str, i, length);
        }
        this._nextByte = length;
    }

    private final void _checkMatchEnd(String str, int i, int i2) throws IOException {
        char _decodeCharForError = (char) _decodeCharForError(i2);
        if (Character.isJavaIdentifierPart(_decodeCharForError)) {
            _reportInvalidToken(_decodeCharForError, str.substring(0, i));
        }
    }

    private final int _skipWS() throws IOException {
        int i = this._nextByte;
        if (i < 0) {
            i = this._inputData.readUnsignedByte();
        } else {
            this._nextByte = -1;
        }
        while (i <= 32) {
            if (i == 13 || i == 10) {
                this._currInputRow++;
            }
            i = this._inputData.readUnsignedByte();
        }
        if (i == 47 || i == 35) {
            return _skipWSComment(i);
        }
        return i;
    }

    private final int _skipWSComment(int i) throws IOException {
        while (true) {
            if (i > 32) {
                if (i == 47) {
                    _skipComment();
                } else if (i != 35 || !_skipYAMLComment()) {
                    return i;
                }
            } else if (i == 13 || i == 10) {
                this._currInputRow++;
            }
            i = this._inputData.readUnsignedByte();
        }
    }

    private final int _skipColon() throws IOException {
        int i = this._nextByte;
        if (i < 0) {
            i = this._inputData.readUnsignedByte();
        } else {
            this._nextByte = -1;
        }
        if (i == 58) {
            i = this._inputData.readUnsignedByte();
            if (i <= 32) {
                if (i == 32 || i == 9) {
                    i = this._inputData.readUnsignedByte();
                    if (i > 32) {
                        if (i == 47 || i == 35) {
                            return _skipColon2(i, true);
                        }
                        return i;
                    }
                }
                return _skipColon2(i, true);
            } else if (i == 47 || i == 35) {
                return _skipColon2(i, true);
            } else {
                return i;
            }
        }
        if (i == 32 || i == 9) {
            i = this._inputData.readUnsignedByte();
        }
        if (i != 58) {
            return _skipColon2(i, false);
        }
        i = this._inputData.readUnsignedByte();
        if (i <= 32) {
            if (i == 32 || i == 9) {
                i = this._inputData.readUnsignedByte();
                if (i > 32) {
                    if (i == 47 || i == 35) {
                        return _skipColon2(i, true);
                    }
                    return i;
                }
            }
            return _skipColon2(i, true);
        } else if (i == 47 || i == 35) {
            return _skipColon2(i, true);
        } else {
            return i;
        }
    }

    private final int _skipColon2(int i, boolean z) throws IOException {
        while (true) {
            if (i > 32) {
                if (i == 47) {
                    _skipComment();
                } else if (i != 35 || !_skipYAMLComment()) {
                    if (z) {
                        return i;
                    }
                    if (i != 58) {
                        _reportUnexpectedChar(i, "was expecting a colon to separate field name and value");
                    }
                    z = true;
                }
            } else if (i == 13 || i == 10) {
                this._currInputRow++;
            }
            i = this._inputData.readUnsignedByte();
        }
    }

    private final void _skipComment() throws IOException {
        if (!isEnabled(Feature.ALLOW_COMMENTS)) {
            _reportUnexpectedChar(47, "maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)");
        }
        int readUnsignedByte = this._inputData.readUnsignedByte();
        if (readUnsignedByte == 47) {
            _skipLine();
        } else if (readUnsignedByte == 42) {
            _skipCComment();
        } else {
            _reportUnexpectedChar(readUnsignedByte, "was expecting either '*' or '/' for a comment");
        }
    }

    private final void _skipCComment() throws IOException {
        int[] inputCodeComment = CharTypes.getInputCodeComment();
        int readUnsignedByte = this._inputData.readUnsignedByte();
        while (true) {
            int i = inputCodeComment[readUnsignedByte];
            if (i != 0) {
                if (i == 10 || i == 13) {
                    this._currInputRow++;
                } else if (i != 42) {
                    switch (i) {
                        case 2:
                            _skipUtf8_2();
                            break;
                        case 3:
                            _skipUtf8_3();
                            break;
                        case 4:
                            _skipUtf8_4();
                            break;
                        default:
                            _reportInvalidChar(readUnsignedByte);
                            break;
                    }
                } else {
                    readUnsignedByte = this._inputData.readUnsignedByte();
                    if (readUnsignedByte == 47) {
                        return;
                    }
                }
            }
            readUnsignedByte = this._inputData.readUnsignedByte();
        }
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
            int readUnsignedByte = this._inputData.readUnsignedByte();
            int i = inputCodeComment[readUnsignedByte];
            if (i != 0) {
                if (i == 10 || i == 13) {
                    this._currInputRow++;
                } else if (i != 42) {
                    switch (i) {
                        case 2:
                            _skipUtf8_2();
                            break;
                        case 3:
                            _skipUtf8_3();
                            break;
                        case 4:
                            _skipUtf8_4();
                            break;
                        default:
                            if (i >= 0) {
                                break;
                            }
                            _reportInvalidChar(readUnsignedByte);
                            break;
                    }
                }
            }
        }
        this._currInputRow++;
    }

    /* Access modifiers changed, original: protected */
    public char _decodeEscaped() throws IOException {
        int readUnsignedByte = this._inputData.readUnsignedByte();
        if (readUnsignedByte == 34 || readUnsignedByte == 47 || readUnsignedByte == 92) {
            return (char) readUnsignedByte;
        }
        if (readUnsignedByte == 98) {
            return 8;
        }
        if (readUnsignedByte == 102) {
            return 12;
        }
        if (readUnsignedByte == 110) {
            return 10;
        }
        if (readUnsignedByte == 114) {
            return 13;
        }
        switch (readUnsignedByte) {
            case 116:
                return 9;
            case 117:
                int i = 0;
                for (readUnsignedByte = 0; readUnsignedByte < 4; readUnsignedByte++) {
                    int readUnsignedByte2 = this._inputData.readUnsignedByte();
                    int charToHex = CharTypes.charToHex(readUnsignedByte2);
                    if (charToHex < 0) {
                        _reportUnexpectedChar(readUnsignedByte2, "expected a hex-digit for character escape sequence");
                    }
                    i = (i << 4) | charToHex;
                }
                return (char) i;
            default:
                return _handleUnrecognizedCharacterEscape((char) _decodeCharForError(readUnsignedByte));
        }
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:29:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0044  */
    public int _decodeCharForError(int r7) throws java.io.IOException {
        /*
        r6 = this;
        r7 = r7 & 255;
        r0 = 127; // 0x7f float:1.78E-43 double:6.27E-322;
        if (r7 <= r0) goto L_0x006e;
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
        r3 = r6._inputData;
        r3 = r3.readUnsignedByte();
        r4 = r3 & 192;
        r5 = 128; // 0x80 float:1.794E-43 double:6.32E-322;
        if (r4 == r5) goto L_0x003d;
    L_0x0038:
        r4 = r3 & 255;
        r6._reportInvalidOther(r4);
    L_0x003d:
        r7 = r7 << 6;
        r3 = r3 & 63;
        r7 = r7 | r3;
        if (r0 <= r2) goto L_0x006e;
    L_0x0044:
        r2 = r6._inputData;
        r2 = r2.readUnsignedByte();
        r3 = r2 & 192;
        if (r3 == r5) goto L_0x0053;
    L_0x004e:
        r3 = r2 & 255;
        r6._reportInvalidOther(r3);
    L_0x0053:
        r7 = r7 << 6;
        r2 = r2 & 63;
        r7 = r7 | r2;
        if (r0 <= r1) goto L_0x006e;
    L_0x005a:
        r0 = r6._inputData;
        r0 = r0.readUnsignedByte();
        r1 = r0 & 192;
        if (r1 == r5) goto L_0x0069;
    L_0x0064:
        r1 = r0 & 255;
        r6._reportInvalidOther(r1);
    L_0x0069:
        r7 = r7 << 6;
        r0 = r0 & 63;
        r7 = r7 | r0;
    L_0x006e:
        return r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8DataInputJsonParser._decodeCharForError(int):int");
    }

    private final int _decodeUtf8_2(int i) throws IOException {
        int readUnsignedByte = this._inputData.readUnsignedByte();
        if ((readUnsignedByte & 192) != 128) {
            _reportInvalidOther(readUnsignedByte & 255);
        }
        return ((i & 31) << 6) | (readUnsignedByte & 63);
    }

    private final int _decodeUtf8_3(int i) throws IOException {
        i &= 15;
        int readUnsignedByte = this._inputData.readUnsignedByte();
        if ((readUnsignedByte & 192) != 128) {
            _reportInvalidOther(readUnsignedByte & 255);
        }
        i = (i << 6) | (readUnsignedByte & 63);
        readUnsignedByte = this._inputData.readUnsignedByte();
        if ((readUnsignedByte & 192) != 128) {
            _reportInvalidOther(readUnsignedByte & 255);
        }
        return (i << 6) | (readUnsignedByte & 63);
    }

    private final int _decodeUtf8_4(int i) throws IOException {
        int readUnsignedByte = this._inputData.readUnsignedByte();
        if ((readUnsignedByte & 192) != 128) {
            _reportInvalidOther(readUnsignedByte & 255);
        }
        i = ((i & 7) << 6) | (readUnsignedByte & 63);
        readUnsignedByte = this._inputData.readUnsignedByte();
        if ((readUnsignedByte & 192) != 128) {
            _reportInvalidOther(readUnsignedByte & 255);
        }
        i = (i << 6) | (readUnsignedByte & 63);
        readUnsignedByte = this._inputData.readUnsignedByte();
        if ((readUnsignedByte & 192) != 128) {
            _reportInvalidOther(readUnsignedByte & 255);
        }
        return ((i << 6) | (readUnsignedByte & 63)) - 65536;
    }

    private final void _skipUtf8_2() throws IOException {
        int readUnsignedByte = this._inputData.readUnsignedByte();
        if ((readUnsignedByte & 192) != 128) {
            _reportInvalidOther(readUnsignedByte & 255);
        }
    }

    private final void _skipUtf8_3() throws IOException {
        int readUnsignedByte = this._inputData.readUnsignedByte();
        if ((readUnsignedByte & 192) != 128) {
            _reportInvalidOther(readUnsignedByte & 255);
        }
        readUnsignedByte = this._inputData.readUnsignedByte();
        if ((readUnsignedByte & 192) != 128) {
            _reportInvalidOther(readUnsignedByte & 255);
        }
    }

    private final void _skipUtf8_4() throws IOException {
        int readUnsignedByte = this._inputData.readUnsignedByte();
        if ((readUnsignedByte & 192) != 128) {
            _reportInvalidOther(readUnsignedByte & 255);
        }
        readUnsignedByte = this._inputData.readUnsignedByte();
        if ((readUnsignedByte & 192) != 128) {
            _reportInvalidOther(readUnsignedByte & 255);
        }
        readUnsignedByte = this._inputData.readUnsignedByte();
        if ((readUnsignedByte & 192) != 128) {
            _reportInvalidOther(readUnsignedByte & 255);
        }
    }

    /* Access modifiers changed, original: protected */
    public void _reportInvalidToken(int i, String str) throws IOException {
        _reportInvalidToken(i, str, "'null', 'true', 'false' or NaN");
    }

    /* Access modifiers changed, original: protected */
    public void _reportInvalidToken(int i, String str, String str2) throws IOException {
        StringBuilder stringBuilder = new StringBuilder(str);
        while (true) {
            char _decodeCharForError = (char) _decodeCharForError(i);
            if (Character.isJavaIdentifierPart(_decodeCharForError)) {
                stringBuilder.append(_decodeCharForError);
                i = this._inputData.readUnsignedByte();
            } else {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Unrecognized token '");
                stringBuilder2.append(stringBuilder.toString());
                stringBuilder2.append("': was expecting ");
                stringBuilder2.append(str2);
                _reportError(stringBuilder2.toString());
                return;
            }
        }
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

    private void _reportInvalidOther(int i) throws JsonParseException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid UTF-8 middle byte 0x");
        stringBuilder.append(Integer.toHexString(i));
        _reportError(stringBuilder.toString());
    }

    private static int[] _growArrayBy(int[] iArr, int i) {
        if (iArr == null) {
            return new int[i];
        }
        return Arrays.copyOf(iArr, iArr.length + i);
    }

    /* Access modifiers changed, original: protected|final */
    public final byte[] _decodeBase64(Base64Variant base64Variant) throws IOException {
        ByteArrayBuilder _getByteArrayBuilder = _getByteArrayBuilder();
        while (true) {
            int readUnsignedByte = this._inputData.readUnsignedByte();
            if (readUnsignedByte > 32) {
                int decodeBase64Char = base64Variant.decodeBase64Char(readUnsignedByte);
                if (decodeBase64Char < 0) {
                    if (readUnsignedByte == 34) {
                        return _getByteArrayBuilder.toByteArray();
                    }
                    decodeBase64Char = _decodeBase64Escape(base64Variant, readUnsignedByte, 0);
                    if (decodeBase64Char < 0) {
                    }
                }
                readUnsignedByte = this._inputData.readUnsignedByte();
                int decodeBase64Char2 = base64Variant.decodeBase64Char(readUnsignedByte);
                if (decodeBase64Char2 < 0) {
                    decodeBase64Char2 = _decodeBase64Escape(base64Variant, readUnsignedByte, 1);
                }
                readUnsignedByte = (decodeBase64Char << 6) | decodeBase64Char2;
                decodeBase64Char = this._inputData.readUnsignedByte();
                decodeBase64Char2 = base64Variant.decodeBase64Char(decodeBase64Char);
                if (decodeBase64Char2 < 0) {
                    if (decodeBase64Char2 != -2) {
                        if (decodeBase64Char != 34 || base64Variant.usesPadding()) {
                            decodeBase64Char2 = _decodeBase64Escape(base64Variant, decodeBase64Char, 2);
                        } else {
                            _getByteArrayBuilder.append(readUnsignedByte >> 4);
                            return _getByteArrayBuilder.toByteArray();
                        }
                    }
                    if (decodeBase64Char2 == -2) {
                        decodeBase64Char = this._inputData.readUnsignedByte();
                        if (base64Variant.usesPaddingChar(decodeBase64Char)) {
                            _getByteArrayBuilder.append(readUnsignedByte >> 4);
                        } else {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("expected padding character '");
                            stringBuilder.append(base64Variant.getPaddingChar());
                            stringBuilder.append("'");
                            throw reportInvalidBase64Char(base64Variant, decodeBase64Char, 3, stringBuilder.toString());
                        }
                    }
                }
                readUnsignedByte = (readUnsignedByte << 6) | decodeBase64Char2;
                decodeBase64Char = this._inputData.readUnsignedByte();
                decodeBase64Char2 = base64Variant.decodeBase64Char(decodeBase64Char);
                if (decodeBase64Char2 < 0) {
                    if (decodeBase64Char2 != -2) {
                        if (decodeBase64Char != 34 || base64Variant.usesPadding()) {
                            decodeBase64Char2 = _decodeBase64Escape(base64Variant, decodeBase64Char, 3);
                        } else {
                            _getByteArrayBuilder.appendTwoBytes(readUnsignedByte >> 2);
                            return _getByteArrayBuilder.toByteArray();
                        }
                    }
                    if (decodeBase64Char2 == -2) {
                        _getByteArrayBuilder.appendTwoBytes(readUnsignedByte >> 2);
                    }
                }
                _getByteArrayBuilder.appendThreeBytes((readUnsignedByte << 6) | decodeBase64Char2);
            }
        }
    }

    public JsonLocation getTokenLocation() {
        return new JsonLocation(this._ioContext.getSourceReference(), -1, -1, this._tokenInputRow, -1);
    }

    public JsonLocation getCurrentLocation() {
        return new JsonLocation(this._ioContext.getSourceReference(), -1, -1, this._currInputRow, -1);
    }
}
