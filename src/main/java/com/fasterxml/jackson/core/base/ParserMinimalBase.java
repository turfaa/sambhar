package com.fasterxml.jackson.core.base;

import com.facebook.internal.ServerProtocol;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.VersionUtil;
import java.io.IOException;

public abstract class ParserMinimalBase extends JsonParser {
    protected static final int INT_BACKSLASH = 92;
    protected static final int INT_COLON = 58;
    protected static final int INT_COMMA = 44;
    protected static final int INT_CR = 13;
    protected static final int INT_E = 69;
    protected static final int INT_HASH = 35;
    protected static final int INT_LBRACKET = 91;
    protected static final int INT_LCURLY = 123;
    protected static final int INT_LF = 10;
    protected static final int INT_PERIOD = 46;
    protected static final int INT_QUOTE = 34;
    protected static final int INT_RBRACKET = 93;
    protected static final int INT_RCURLY = 125;
    protected static final int INT_SLASH = 47;
    protected static final int INT_SPACE = 32;
    protected static final int INT_TAB = 9;
    protected static final int INT_e = 101;
    protected JsonToken _currToken;
    protected JsonToken _lastClearedToken;

    public abstract void _handleEOF() throws JsonParseException;

    public abstract void close() throws IOException;

    public abstract byte[] getBinaryValue(Base64Variant base64Variant) throws IOException;

    public abstract String getCurrentName() throws IOException;

    public abstract JsonStreamContext getParsingContext();

    public abstract String getText() throws IOException;

    public abstract char[] getTextCharacters() throws IOException;

    public abstract int getTextLength() throws IOException;

    public abstract int getTextOffset() throws IOException;

    public abstract boolean hasTextCharacters();

    public abstract boolean isClosed();

    public abstract JsonToken nextToken() throws IOException;

    public abstract void overrideCurrentName(String str);

    protected ParserMinimalBase() {
    }

    protected ParserMinimalBase(int i) {
        super(i);
    }

    public JsonToken currentToken() {
        return this._currToken;
    }

    public int currentTokenId() {
        JsonToken jsonToken = this._currToken;
        if (jsonToken == null) {
            return 0;
        }
        return jsonToken.id();
    }

    public JsonToken getCurrentToken() {
        return this._currToken;
    }

    public int getCurrentTokenId() {
        JsonToken jsonToken = this._currToken;
        if (jsonToken == null) {
            return 0;
        }
        return jsonToken.id();
    }

    public boolean hasCurrentToken() {
        return this._currToken != null;
    }

    public boolean hasTokenId(int i) {
        JsonToken jsonToken = this._currToken;
        boolean z = false;
        if (jsonToken == null) {
            if (i == 0) {
                z = true;
            }
            return z;
        }
        if (jsonToken.id() == i) {
            z = true;
        }
        return z;
    }

    public boolean hasToken(JsonToken jsonToken) {
        return this._currToken == jsonToken;
    }

    public boolean isExpectedStartArrayToken() {
        return this._currToken == JsonToken.START_ARRAY;
    }

    public boolean isExpectedStartObjectToken() {
        return this._currToken == JsonToken.START_OBJECT;
    }

    public JsonToken nextValue() throws IOException {
        JsonToken nextToken = nextToken();
        return nextToken == JsonToken.FIELD_NAME ? nextToken() : nextToken;
    }

    public JsonParser skipChildren() throws IOException {
        if (this._currToken != JsonToken.START_OBJECT && this._currToken != JsonToken.START_ARRAY) {
            return this;
        }
        int i = 1;
        while (true) {
            JsonToken nextToken = nextToken();
            if (nextToken == null) {
                _handleEOF();
                return this;
            } else if (nextToken.isStructStart()) {
                i++;
            } else if (nextToken.isStructEnd()) {
                i--;
                if (i == 0) {
                    return this;
                }
            } else {
                continue;
            }
        }
    }

    public void clearCurrentToken() {
        if (this._currToken != null) {
            this._lastClearedToken = this._currToken;
            this._currToken = null;
        }
    }

    public JsonToken getLastClearedToken() {
        return this._lastClearedToken;
    }

    public boolean getValueAsBoolean(boolean z) throws IOException {
        JsonToken jsonToken = this._currToken;
        if (jsonToken != null) {
            boolean z2 = true;
            switch (jsonToken.id()) {
                case 6:
                    String trim = getText().trim();
                    if (ServerProtocol.DIALOG_RETURN_SCOPES_TRUE.equals(trim)) {
                        return true;
                    }
                    if ("false".equals(trim) || _hasTextualNull(trim)) {
                        return false;
                    }
                    return z;
                case 7:
                    if (getIntValue() == 0) {
                        z2 = false;
                    }
                    return z2;
                case 9:
                    return true;
                case 10:
                case 11:
                    return false;
                case 12:
                    Object embeddedObject = getEmbeddedObject();
                    if (embeddedObject instanceof Boolean) {
                        return ((Boolean) embeddedObject).booleanValue();
                    }
                    break;
            }
        }
        return z;
    }

    public int getValueAsInt() throws IOException {
        JsonToken jsonToken = this._currToken;
        if (jsonToken == JsonToken.VALUE_NUMBER_INT || jsonToken == JsonToken.VALUE_NUMBER_FLOAT) {
            return getIntValue();
        }
        return getValueAsInt(0);
    }

    public int getValueAsInt(int i) throws IOException {
        JsonToken jsonToken = this._currToken;
        if (jsonToken == JsonToken.VALUE_NUMBER_INT || jsonToken == JsonToken.VALUE_NUMBER_FLOAT) {
            return getIntValue();
        }
        if (jsonToken != null) {
            int id = jsonToken.id();
            if (id != 6) {
                switch (id) {
                    case 9:
                        return 1;
                    case 10:
                        return 0;
                    case 11:
                        return 0;
                    case 12:
                        Object embeddedObject = getEmbeddedObject();
                        if (embeddedObject instanceof Number) {
                            return ((Number) embeddedObject).intValue();
                        }
                        break;
                }
            }
            String text = getText();
            if (_hasTextualNull(text)) {
                return 0;
            }
            return NumberInput.parseAsInt(text, i);
        }
        return i;
    }

    public long getValueAsLong() throws IOException {
        JsonToken jsonToken = this._currToken;
        if (jsonToken == JsonToken.VALUE_NUMBER_INT || jsonToken == JsonToken.VALUE_NUMBER_FLOAT) {
            return getLongValue();
        }
        return getValueAsLong(0);
    }

    public long getValueAsLong(long j) throws IOException {
        JsonToken jsonToken = this._currToken;
        if (jsonToken == JsonToken.VALUE_NUMBER_INT || jsonToken == JsonToken.VALUE_NUMBER_FLOAT) {
            return getLongValue();
        }
        if (jsonToken != null) {
            int id = jsonToken.id();
            if (id != 6) {
                switch (id) {
                    case 9:
                        return 1;
                    case 10:
                    case 11:
                        return 0;
                    case 12:
                        Object embeddedObject = getEmbeddedObject();
                        if (embeddedObject instanceof Number) {
                            return ((Number) embeddedObject).longValue();
                        }
                        break;
                }
            }
            String text = getText();
            if (_hasTextualNull(text)) {
                return 0;
            }
            return NumberInput.parseAsLong(text, j);
        }
        return j;
    }

    public double getValueAsDouble(double d) throws IOException {
        JsonToken jsonToken = this._currToken;
        if (jsonToken != null) {
            switch (jsonToken.id()) {
                case 6:
                    String text = getText();
                    if (_hasTextualNull(text)) {
                        return 0.0d;
                    }
                    return NumberInput.parseAsDouble(text, d);
                case 7:
                case 8:
                    return getDoubleValue();
                case 9:
                    return 1.0d;
                case 10:
                case 11:
                    return 0.0d;
                case 12:
                    Object embeddedObject = getEmbeddedObject();
                    if (embeddedObject instanceof Number) {
                        return ((Number) embeddedObject).doubleValue();
                    }
                    break;
            }
        }
        return d;
    }

    public String getValueAsString() throws IOException {
        if (this._currToken == JsonToken.VALUE_STRING) {
            return getText();
        }
        if (this._currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        return getValueAsString(null);
    }

    public String getValueAsString(String str) throws IOException {
        if (this._currToken == JsonToken.VALUE_STRING) {
            return getText();
        }
        if (this._currToken == JsonToken.FIELD_NAME) {
            return getCurrentName();
        }
        return (this._currToken == null || this._currToken == JsonToken.VALUE_NULL || !this._currToken.isScalarValue()) ? str : getText();
    }

    /* Access modifiers changed, original: protected */
    public void _decodeBase64(String str, ByteArrayBuilder byteArrayBuilder, Base64Variant base64Variant) throws IOException {
        try {
            base64Variant.decode(str, byteArrayBuilder);
        } catch (IllegalArgumentException e) {
            _reportError(e.getMessage());
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean _hasTextualNull(String str) {
        return "null".equals(str);
    }

    /* Access modifiers changed, original: protected */
    public void _reportUnexpectedChar(int i, String str) throws JsonParseException {
        if (i < 0) {
            _reportInvalidEOF();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected character (");
        stringBuilder.append(_getCharDesc(i));
        stringBuilder.append(")");
        String stringBuilder2 = stringBuilder.toString();
        if (str != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(stringBuilder2);
            stringBuilder.append(": ");
            stringBuilder.append(str);
            stringBuilder2 = stringBuilder.toString();
        }
        _reportError(stringBuilder2);
    }

    /* Access modifiers changed, original: protected */
    public void _reportInvalidEOF() throws JsonParseException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" in ");
        stringBuilder.append(this._currToken);
        _reportInvalidEOF(stringBuilder.toString(), this._currToken);
    }

    /* Access modifiers changed, original: protected */
    public void _reportInvalidEOFInValue(JsonToken jsonToken) throws JsonParseException {
        String str = jsonToken == JsonToken.VALUE_STRING ? " in a String value" : (jsonToken == JsonToken.VALUE_NUMBER_INT || jsonToken == JsonToken.VALUE_NUMBER_FLOAT) ? " in a Number value" : " in a value";
        _reportInvalidEOF(str, jsonToken);
    }

    /* Access modifiers changed, original: protected */
    public void _reportInvalidEOF(String str, JsonToken jsonToken) throws JsonParseException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected end-of-input");
        stringBuilder.append(str);
        throw new JsonEOFException(this, jsonToken, stringBuilder.toString());
    }

    /* Access modifiers changed, original: protected */
    @Deprecated
    public void _reportInvalidEOFInValue() throws JsonParseException {
        _reportInvalidEOF(" in a value");
    }

    /* Access modifiers changed, original: protected */
    @Deprecated
    public void _reportInvalidEOF(String str) throws JsonParseException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected end-of-input");
        stringBuilder.append(str);
        throw new JsonEOFException(this, null, stringBuilder.toString());
    }

    /* Access modifiers changed, original: protected */
    public void _reportMissingRootWS(int i) throws JsonParseException {
        _reportUnexpectedChar(i, "Expected space separating root-level values");
    }

    /* Access modifiers changed, original: protected */
    public void _throwInvalidSpace(int i) throws JsonParseException {
        char c = (char) i;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Illegal character (");
        stringBuilder.append(_getCharDesc(c));
        stringBuilder.append("): only regular white space (\\r, \\n, \\t) is allowed between tokens");
        _reportError(stringBuilder.toString());
    }

    /* Access modifiers changed, original: protected */
    public void _throwUnquotedSpace(int i, String str) throws JsonParseException {
        if (!isEnabled(Feature.ALLOW_UNQUOTED_CONTROL_CHARS) || i > 32) {
            char c = (char) i;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Illegal unquoted character (");
            stringBuilder.append(_getCharDesc(c));
            stringBuilder.append("): has to be escaped using backslash to be included in ");
            stringBuilder.append(str);
            _reportError(stringBuilder.toString());
        }
    }

    /* Access modifiers changed, original: protected */
    public char _handleUnrecognizedCharacterEscape(char c) throws JsonProcessingException {
        if (isEnabled(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)) {
            return c;
        }
        if (c == '\'' && isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
            return c;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unrecognized character escape ");
        stringBuilder.append(_getCharDesc(c));
        _reportError(stringBuilder.toString());
        return c;
    }

    protected static final String _getCharDesc(int i) {
        char c = (char) i;
        StringBuilder stringBuilder;
        if (Character.isISOControl(c)) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("(CTRL-CHAR, code ");
            stringBuilder2.append(i);
            stringBuilder2.append(")");
            return stringBuilder2.toString();
        } else if (i > 255) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("'");
            stringBuilder.append(c);
            stringBuilder.append("' (code ");
            stringBuilder.append(i);
            stringBuilder.append(" / 0x");
            stringBuilder.append(Integer.toHexString(i));
            stringBuilder.append(")");
            return stringBuilder.toString();
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append("'");
            stringBuilder.append(c);
            stringBuilder.append("' (code ");
            stringBuilder.append(i);
            stringBuilder.append(")");
            return stringBuilder.toString();
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final void _reportError(String str) throws JsonParseException {
        throw _constructError(str);
    }

    /* Access modifiers changed, original: protected|final */
    public final void _wrapError(String str, Throwable th) throws JsonParseException {
        throw _constructError(str, th);
    }

    /* Access modifiers changed, original: protected|final */
    public final void _throwInternal() {
        VersionUtil.throwInternal();
    }

    /* Access modifiers changed, original: protected|final */
    public final JsonParseException _constructError(String str, Throwable th) {
        return new JsonParseException((JsonParser) this, str, th);
    }

    protected static byte[] _asciiBytes(String str) {
        byte[] bArr = new byte[str.length()];
        int length = str.length();
        for (int i = 0; i < length; i++) {
            bArr[i] = (byte) str.charAt(i);
        }
        return bArr;
    }

    protected static String _ascii(byte[] bArr) {
        try {
            return new String(bArr, "US-ASCII");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
