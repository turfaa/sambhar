package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.io.NumberInput;

public class JsonPointer {
    protected static final JsonPointer EMPTY = new JsonPointer();
    protected final String _asString;
    protected volatile JsonPointer _head;
    protected final int _matchingElementIndex;
    protected final String _matchingPropertyName;
    protected final JsonPointer _nextSegment;

    protected JsonPointer() {
        this._nextSegment = null;
        this._matchingPropertyName = "";
        this._matchingElementIndex = -1;
        this._asString = "";
    }

    protected JsonPointer(String str, String str2, JsonPointer jsonPointer) {
        this._asString = str;
        this._nextSegment = jsonPointer;
        this._matchingPropertyName = str2;
        this._matchingElementIndex = _parseIndex(str2);
    }

    protected JsonPointer(String str, String str2, int i, JsonPointer jsonPointer) {
        this._asString = str;
        this._nextSegment = jsonPointer;
        this._matchingPropertyName = str2;
        this._matchingElementIndex = i;
    }

    public static JsonPointer compile(String str) throws IllegalArgumentException {
        if (str == null || str.length() == 0) {
            return EMPTY;
        }
        if (str.charAt(0) == '/') {
            return _parseTail(str);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid input: JSON Pointer expression must start with '/': \"");
        stringBuilder.append(str);
        stringBuilder.append("\"");
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public static JsonPointer valueOf(String str) {
        return compile(str);
    }

    public boolean matches() {
        return this._nextSegment == null;
    }

    public String getMatchingProperty() {
        return this._matchingPropertyName;
    }

    public int getMatchingIndex() {
        return this._matchingElementIndex;
    }

    public boolean mayMatchProperty() {
        return this._matchingPropertyName != null;
    }

    public boolean mayMatchElement() {
        return this._matchingElementIndex >= 0;
    }

    public JsonPointer last() {
        if (this == EMPTY) {
            return null;
        }
        JsonPointer jsonPointer = this;
        while (true) {
            JsonPointer jsonPointer2 = jsonPointer._nextSegment;
            if (jsonPointer2 == EMPTY) {
                return jsonPointer;
            }
            jsonPointer = jsonPointer2;
        }
    }

    public JsonPointer append(JsonPointer jsonPointer) {
        if (this == EMPTY) {
            return jsonPointer;
        }
        if (jsonPointer == EMPTY) {
            return this;
        }
        String str = this._asString;
        if (str.endsWith("/")) {
            str = str.substring(0, str.length() - 1);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(str);
        stringBuilder.append(jsonPointer._asString);
        return compile(stringBuilder.toString());
    }

    public boolean matchesProperty(String str) {
        return this._nextSegment != null && this._matchingPropertyName.equals(str);
    }

    public JsonPointer matchProperty(String str) {
        return (this._nextSegment == null || !this._matchingPropertyName.equals(str)) ? null : this._nextSegment;
    }

    public boolean matchesElement(int i) {
        return i == this._matchingElementIndex && i >= 0;
    }

    public JsonPointer matchElement(int i) {
        return (i != this._matchingElementIndex || i < 0) ? null : this._nextSegment;
    }

    public JsonPointer tail() {
        return this._nextSegment;
    }

    public JsonPointer head() {
        JsonPointer jsonPointer = this._head;
        if (jsonPointer == null) {
            if (this != EMPTY) {
                jsonPointer = _constructHead();
            }
            this._head = jsonPointer;
        }
        return jsonPointer;
    }

    public String toString() {
        return this._asString;
    }

    public int hashCode() {
        return this._asString.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && (obj instanceof JsonPointer)) {
            return this._asString.equals(((JsonPointer) obj)._asString);
        }
        return false;
    }

    private static final int _parseIndex(String str) {
        int length = str.length();
        int i = -1;
        if (length == 0 || length > 10) {
            return -1;
        }
        char charAt = str.charAt(0);
        int i2 = 1;
        if (charAt <= '0') {
            if (length == 1 && charAt == '0') {
                i = 0;
            }
            return i;
        } else if (charAt > '9') {
            return -1;
        } else {
            while (i2 < length) {
                charAt = str.charAt(i2);
                if (charAt > '9' || charAt < '0') {
                    return -1;
                }
                i2++;
            }
            if (length != 10 || NumberInput.parseLong(str) <= 2147483647L) {
                return NumberInput.parseInt(str);
            }
            return -1;
        }
    }

    protected static JsonPointer _parseTail(String str) {
        int length = str.length();
        int i = 1;
        while (i < length) {
            char charAt = str.charAt(i);
            if (charAt == '/') {
                return new JsonPointer(str, str.substring(1, i), _parseTail(str.substring(i)));
            }
            i++;
            if (charAt == '~' && i < length) {
                return _parseQuotedTail(str, i);
            }
        }
        return new JsonPointer(str, str.substring(1), EMPTY);
    }

    protected static JsonPointer _parseQuotedTail(String str, int i) {
        int length = str.length();
        StringBuilder stringBuilder = new StringBuilder(Math.max(16, length));
        if (i > 2) {
            stringBuilder.append(str, 1, i - 1);
        }
        int i2 = i + 1;
        _appendEscape(stringBuilder, str.charAt(i));
        while (i2 < length) {
            char charAt = str.charAt(i2);
            if (charAt == '/') {
                return new JsonPointer(str, stringBuilder.toString(), _parseTail(str.substring(i2)));
            }
            i2++;
            if (charAt != '~' || i2 >= length) {
                stringBuilder.append(charAt);
            } else {
                i = i2 + 1;
                _appendEscape(stringBuilder, str.charAt(i2));
                i2 = i;
            }
        }
        return new JsonPointer(str, stringBuilder.toString(), EMPTY);
    }

    /* Access modifiers changed, original: protected */
    public JsonPointer _constructHead() {
        JsonPointer last = last();
        if (last == this) {
            return EMPTY;
        }
        int length = last._asString.length();
        return new JsonPointer(this._asString.substring(0, this._asString.length() - length), this._matchingPropertyName, this._matchingElementIndex, this._nextSegment._constructHead(length, last));
    }

    /* Access modifiers changed, original: protected */
    public JsonPointer _constructHead(int i, JsonPointer jsonPointer) {
        if (this == jsonPointer) {
            return EMPTY;
        }
        JsonPointer jsonPointer2 = this._nextSegment;
        String str = this._asString;
        return new JsonPointer(str.substring(0, str.length() - i), this._matchingPropertyName, this._matchingElementIndex, jsonPointer2._constructHead(i, jsonPointer));
    }

    private static void _appendEscape(StringBuilder stringBuilder, char c) {
        if (c == '0') {
            c = '~';
        } else if (c == '1') {
            c = '/';
        } else {
            stringBuilder.append('~');
        }
        stringBuilder.append(c);
    }
}
