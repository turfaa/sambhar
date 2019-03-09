package com.fasterxml.jackson.core.format;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.format.InputAccessor.Std;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class DataFormatDetector {
    public static final int DEFAULT_MAX_INPUT_LOOKAHEAD = 64;
    protected final JsonFactory[] _detectors;
    protected final int _maxInputLookahead;
    protected final MatchStrength _minimalMatch;
    protected final MatchStrength _optimalMatch;

    public DataFormatDetector(JsonFactory... jsonFactoryArr) {
        this(jsonFactoryArr, MatchStrength.SOLID_MATCH, MatchStrength.WEAK_MATCH, 64);
    }

    public DataFormatDetector(Collection<JsonFactory> collection) {
        this((JsonFactory[]) collection.toArray(new JsonFactory[collection.size()]));
    }

    public DataFormatDetector withOptimalMatch(MatchStrength matchStrength) {
        if (matchStrength == this._optimalMatch) {
            return this;
        }
        return new DataFormatDetector(this._detectors, matchStrength, this._minimalMatch, this._maxInputLookahead);
    }

    public DataFormatDetector withMinimalMatch(MatchStrength matchStrength) {
        if (matchStrength == this._minimalMatch) {
            return this;
        }
        return new DataFormatDetector(this._detectors, this._optimalMatch, matchStrength, this._maxInputLookahead);
    }

    public DataFormatDetector withMaxInputLookahead(int i) {
        if (i == this._maxInputLookahead) {
            return this;
        }
        return new DataFormatDetector(this._detectors, this._optimalMatch, this._minimalMatch, i);
    }

    private DataFormatDetector(JsonFactory[] jsonFactoryArr, MatchStrength matchStrength, MatchStrength matchStrength2, int i) {
        this._detectors = jsonFactoryArr;
        this._optimalMatch = matchStrength;
        this._minimalMatch = matchStrength2;
        this._maxInputLookahead = i;
    }

    public DataFormatMatcher findFormat(InputStream inputStream) throws IOException {
        return _findFormat(new Std(inputStream, new byte[this._maxInputLookahead]));
    }

    public DataFormatMatcher findFormat(byte[] bArr) throws IOException {
        return _findFormat(new Std(bArr));
    }

    public DataFormatMatcher findFormat(byte[] bArr, int i, int i2) throws IOException {
        return _findFormat(new Std(bArr, i, i2));
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        int length = this._detectors.length;
        if (length > 0) {
            stringBuilder.append(this._detectors[0].getFormatName());
            for (int i = 1; i < length; i++) {
                stringBuilder.append(", ");
                stringBuilder.append(this._detectors[i].getFormatName());
            }
        }
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    private DataFormatMatcher _findFormat(Std std) throws IOException {
        JsonFactory jsonFactory = null;
        MatchStrength matchStrength = null;
        for (JsonFactory jsonFactory2 : this._detectors) {
            std.reset();
            MatchStrength hasFormat = jsonFactory2.hasFormat(std);
            if (hasFormat != null && hasFormat.ordinal() >= this._minimalMatch.ordinal() && (r2 == null || r4.ordinal() < hasFormat.ordinal())) {
                if (hasFormat.ordinal() >= this._optimalMatch.ordinal()) {
                    jsonFactory = jsonFactory2;
                    matchStrength = hasFormat;
                    break;
                }
                jsonFactory = jsonFactory2;
                matchStrength = hasFormat;
            }
        }
        return std.createMatcher(jsonFactory, matchStrength);
    }
}
