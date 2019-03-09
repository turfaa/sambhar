package com.fasterxml.jackson.core.base;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.json.DupDetector;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

public abstract class GeneratorBase extends JsonGenerator {
    protected static final int DERIVED_FEATURES_MASK = ((Feature.WRITE_NUMBERS_AS_STRINGS.getMask() | Feature.ESCAPE_NON_ASCII.getMask()) | Feature.STRICT_DUPLICATE_DETECTION.getMask());
    protected static final int MAX_BIG_DECIMAL_SCALE = 9999;
    public static final int SURR1_FIRST = 55296;
    public static final int SURR1_LAST = 56319;
    public static final int SURR2_FIRST = 56320;
    public static final int SURR2_LAST = 57343;
    protected static final String WRITE_BINARY = "write a binary value";
    protected static final String WRITE_BOOLEAN = "write a boolean value";
    protected static final String WRITE_NULL = "write a null";
    protected static final String WRITE_NUMBER = "write a number";
    protected static final String WRITE_RAW = "write a raw (unencoded) value";
    protected static final String WRITE_STRING = "write a string";
    protected boolean _cfgNumbersAsStrings;
    protected boolean _closed;
    protected int _features;
    protected ObjectCodec _objectCodec;
    protected JsonWriteContext _writeContext;

    public abstract void _releaseBuffers();

    public abstract void _verifyValueWrite(String str) throws IOException;

    public abstract void flush() throws IOException;

    protected GeneratorBase(int i, ObjectCodec objectCodec) {
        this._features = i;
        this._objectCodec = objectCodec;
        this._writeContext = JsonWriteContext.createRootContext(Feature.STRICT_DUPLICATE_DETECTION.enabledIn(i) ? DupDetector.rootDetector((JsonGenerator) this) : null);
        this._cfgNumbersAsStrings = Feature.WRITE_NUMBERS_AS_STRINGS.enabledIn(i);
    }

    protected GeneratorBase(int i, ObjectCodec objectCodec, JsonWriteContext jsonWriteContext) {
        this._features = i;
        this._objectCodec = objectCodec;
        this._writeContext = jsonWriteContext;
        this._cfgNumbersAsStrings = Feature.WRITE_NUMBERS_AS_STRINGS.enabledIn(i);
    }

    public Version version() {
        return PackageVersion.VERSION;
    }

    public Object getCurrentValue() {
        return this._writeContext.getCurrentValue();
    }

    public void setCurrentValue(Object obj) {
        this._writeContext.setCurrentValue(obj);
    }

    public final boolean isEnabled(Feature feature) {
        return (feature.getMask() & this._features) != 0;
    }

    public int getFeatureMask() {
        return this._features;
    }

    public JsonGenerator enable(Feature feature) {
        int mask = feature.getMask();
        this._features |= mask;
        if ((mask & DERIVED_FEATURES_MASK) != 0) {
            if (feature == Feature.WRITE_NUMBERS_AS_STRINGS) {
                this._cfgNumbersAsStrings = true;
            } else if (feature == Feature.ESCAPE_NON_ASCII) {
                setHighestNonEscapedChar(127);
            } else if (feature == Feature.STRICT_DUPLICATE_DETECTION && this._writeContext.getDupDetector() == null) {
                this._writeContext = this._writeContext.withDupDetector(DupDetector.rootDetector((JsonGenerator) this));
            }
        }
        return this;
    }

    public JsonGenerator disable(Feature feature) {
        int mask = feature.getMask();
        this._features &= mask ^ -1;
        if ((mask & DERIVED_FEATURES_MASK) != 0) {
            if (feature == Feature.WRITE_NUMBERS_AS_STRINGS) {
                this._cfgNumbersAsStrings = false;
            } else if (feature == Feature.ESCAPE_NON_ASCII) {
                setHighestNonEscapedChar(0);
            } else if (feature == Feature.STRICT_DUPLICATE_DETECTION) {
                this._writeContext = this._writeContext.withDupDetector(null);
            }
        }
        return this;
    }

    @Deprecated
    public JsonGenerator setFeatureMask(int i) {
        int i2 = this._features ^ i;
        this._features = i;
        if (i2 != 0) {
            _checkStdFeatureChanges(i, i2);
        }
        return this;
    }

    public JsonGenerator overrideStdFeatures(int i, int i2) {
        int i3 = this._features;
        i = (i & i2) | ((i2 ^ -1) & i3);
        i2 = i3 ^ i;
        if (i2 != 0) {
            this._features = i;
            _checkStdFeatureChanges(i, i2);
        }
        return this;
    }

    /* Access modifiers changed, original: protected */
    public void _checkStdFeatureChanges(int i, int i2) {
        if ((DERIVED_FEATURES_MASK & i2) != 0) {
            this._cfgNumbersAsStrings = Feature.WRITE_NUMBERS_AS_STRINGS.enabledIn(i);
            if (Feature.ESCAPE_NON_ASCII.enabledIn(i2)) {
                if (Feature.ESCAPE_NON_ASCII.enabledIn(i)) {
                    setHighestNonEscapedChar(127);
                } else {
                    setHighestNonEscapedChar(0);
                }
            }
            if (Feature.STRICT_DUPLICATE_DETECTION.enabledIn(i2)) {
                if (!Feature.STRICT_DUPLICATE_DETECTION.enabledIn(i)) {
                    this._writeContext = this._writeContext.withDupDetector(null);
                } else if (this._writeContext.getDupDetector() == null) {
                    this._writeContext = this._writeContext.withDupDetector(DupDetector.rootDetector((JsonGenerator) this));
                }
            }
        }
    }

    public JsonGenerator useDefaultPrettyPrinter() {
        if (getPrettyPrinter() != null) {
            return this;
        }
        return setPrettyPrinter(_constructDefaultPrettyPrinter());
    }

    public JsonGenerator setCodec(ObjectCodec objectCodec) {
        this._objectCodec = objectCodec;
        return this;
    }

    public ObjectCodec getCodec() {
        return this._objectCodec;
    }

    public JsonStreamContext getOutputContext() {
        return this._writeContext;
    }

    public void writeStartObject(Object obj) throws IOException {
        writeStartObject();
        if (!(this._writeContext == null || obj == null)) {
            this._writeContext.setCurrentValue(obj);
        }
        setCurrentValue(obj);
    }

    public void writeFieldName(SerializableString serializableString) throws IOException {
        writeFieldName(serializableString.getValue());
    }

    public void writeString(SerializableString serializableString) throws IOException {
        writeString(serializableString.getValue());
    }

    public void writeRawValue(String str) throws IOException {
        _verifyValueWrite("write raw value");
        writeRaw(str);
    }

    public void writeRawValue(String str, int i, int i2) throws IOException {
        _verifyValueWrite("write raw value");
        writeRaw(str, i, i2);
    }

    public void writeRawValue(char[] cArr, int i, int i2) throws IOException {
        _verifyValueWrite("write raw value");
        writeRaw(cArr, i, i2);
    }

    public void writeRawValue(SerializableString serializableString) throws IOException {
        _verifyValueWrite("write raw value");
        writeRaw(serializableString);
    }

    public int writeBinary(Base64Variant base64Variant, InputStream inputStream, int i) throws IOException {
        _reportUnsupportedOperation();
        return 0;
    }

    public void writeObject(Object obj) throws IOException {
        if (obj == null) {
            writeNull();
        } else if (this._objectCodec != null) {
            this._objectCodec.writeValue(this, obj);
        } else {
            _writeSimpleObject(obj);
        }
    }

    public void writeTree(TreeNode treeNode) throws IOException {
        if (treeNode == null) {
            writeNull();
        } else if (this._objectCodec != null) {
            this._objectCodec.writeValue(this, treeNode);
        } else {
            throw new IllegalStateException("No ObjectCodec defined");
        }
    }

    public void close() throws IOException {
        this._closed = true;
    }

    public boolean isClosed() {
        return this._closed;
    }

    /* Access modifiers changed, original: protected */
    public PrettyPrinter _constructDefaultPrettyPrinter() {
        return new DefaultPrettyPrinter();
    }

    /* Access modifiers changed, original: protected */
    public String _asString(BigDecimal bigDecimal) throws IOException {
        if (!Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(this._features)) {
            return bigDecimal.toString();
        }
        int scale = bigDecimal.scale();
        if (scale < -9999 || scale > MAX_BIG_DECIMAL_SCALE) {
            _reportError(String.format("Attempt to write plain `java.math.BigDecimal` (see JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN) with illegal scale (%d): needs to be between [-%d, %d]", new Object[]{Integer.valueOf(scale), Integer.valueOf(MAX_BIG_DECIMAL_SCALE), Integer.valueOf(MAX_BIG_DECIMAL_SCALE)}));
        }
        return bigDecimal.toPlainString();
    }

    /* Access modifiers changed, original: protected|final */
    public final int _decodeSurrogate(int i, int i2) throws IOException {
        if (i2 < SURR2_FIRST || i2 > SURR2_LAST) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Incomplete surrogate pair: first char 0x");
            stringBuilder.append(Integer.toHexString(i));
            stringBuilder.append(", second 0x");
            stringBuilder.append(Integer.toHexString(i2));
            _reportError(stringBuilder.toString());
        }
        return (((i - SURR1_FIRST) << 10) + 65536) + (i2 - SURR2_FIRST);
    }
}
