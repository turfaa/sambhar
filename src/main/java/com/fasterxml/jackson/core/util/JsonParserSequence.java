package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonParserSequence extends JsonParserDelegate {
    protected final boolean _checkForExistingToken;
    protected boolean _hasToken;
    protected int _nextParserIndex;
    protected final JsonParser[] _parsers;

    @Deprecated
    protected JsonParserSequence(JsonParser[] jsonParserArr) {
        this(false, jsonParserArr);
    }

    protected JsonParserSequence(boolean z, JsonParser[] jsonParserArr) {
        boolean z2 = false;
        super(jsonParserArr[0]);
        this._checkForExistingToken = z;
        if (z && this.delegate.hasCurrentToken()) {
            z2 = true;
        }
        this._hasToken = z2;
        this._parsers = jsonParserArr;
        this._nextParserIndex = 1;
    }

    public static JsonParserSequence createFlattened(boolean z, JsonParser jsonParser, JsonParser jsonParser2) {
        boolean z2 = jsonParser instanceof JsonParserSequence;
        if (z2 || (jsonParser2 instanceof JsonParserSequence)) {
            ArrayList arrayList = new ArrayList();
            if (z2) {
                ((JsonParserSequence) jsonParser).addFlattenedActiveParsers(arrayList);
            } else {
                arrayList.add(jsonParser);
            }
            if (jsonParser2 instanceof JsonParserSequence) {
                ((JsonParserSequence) jsonParser2).addFlattenedActiveParsers(arrayList);
            } else {
                arrayList.add(jsonParser2);
            }
            return new JsonParserSequence(z, (JsonParser[]) arrayList.toArray(new JsonParser[arrayList.size()]));
        }
        return new JsonParserSequence(z, new JsonParser[]{jsonParser, jsonParser2});
    }

    @Deprecated
    public static JsonParserSequence createFlattened(JsonParser jsonParser, JsonParser jsonParser2) {
        return createFlattened(false, jsonParser, jsonParser2);
    }

    /* Access modifiers changed, original: protected */
    public void addFlattenedActiveParsers(List<JsonParser> list) {
        int length = this._parsers.length;
        for (int i = this._nextParserIndex - 1; i < length; i++) {
            Object obj = this._parsers[i];
            if (obj instanceof JsonParserSequence) {
                ((JsonParserSequence) obj).addFlattenedActiveParsers(list);
            } else {
                list.add(obj);
            }
        }
    }

    public void close() throws IOException {
        do {
            this.delegate.close();
        } while (switchToNext());
    }

    public JsonToken nextToken() throws IOException {
        if (this.delegate == null) {
            return null;
        }
        if (this._hasToken) {
            this._hasToken = false;
            return this.delegate.currentToken();
        }
        JsonToken nextToken = this.delegate.nextToken();
        return nextToken == null ? switchAndReturnNext() : nextToken;
    }

    public int containedParsersCount() {
        return this._parsers.length;
    }

    /* Access modifiers changed, original: protected */
    public boolean switchToNext() {
        if (this._nextParserIndex >= this._parsers.length) {
            return false;
        }
        JsonParser[] jsonParserArr = this._parsers;
        int i = this._nextParserIndex;
        this._nextParserIndex = i + 1;
        this.delegate = jsonParserArr[i];
        return true;
    }

    /* Access modifiers changed, original: protected */
    public JsonToken switchAndReturnNext() throws IOException {
        while (this._nextParserIndex < this._parsers.length) {
            JsonParser[] jsonParserArr = this._parsers;
            int i = this._nextParserIndex;
            this._nextParserIndex = i + 1;
            this.delegate = jsonParserArr[i];
            if (this._checkForExistingToken && this.delegate.hasCurrentToken()) {
                return this.delegate.getCurrentToken();
            }
            JsonToken nextToken = this.delegate.nextToken();
            if (nextToken != null) {
                return nextToken;
            }
        }
        return null;
    }
}
