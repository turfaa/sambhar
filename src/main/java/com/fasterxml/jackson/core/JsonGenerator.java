package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.util.VersionUtil;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class JsonGenerator implements Closeable, Flushable, Versioned {
    protected PrettyPrinter _cfgPrettyPrinter;

    public enum Feature {
        AUTO_CLOSE_TARGET(true),
        AUTO_CLOSE_JSON_CONTENT(true),
        FLUSH_PASSED_TO_STREAM(true),
        QUOTE_FIELD_NAMES(true),
        QUOTE_NON_NUMERIC_NUMBERS(true),
        WRITE_NUMBERS_AS_STRINGS(false),
        WRITE_BIGDECIMAL_AS_PLAIN(false),
        ESCAPE_NON_ASCII(false),
        STRICT_DUPLICATE_DETECTION(false),
        IGNORE_UNKNOWN(false);
        
        private final boolean _defaultState;
        private final int _mask;

        public static int collectDefaults() {
            int i = 0;
            for (Feature feature : values()) {
                if (feature.enabledByDefault()) {
                    i |= feature.getMask();
                }
            }
            return i;
        }

        private Feature(boolean z) {
            this._defaultState = z;
            this._mask = 1 << ordinal();
        }

        public boolean enabledByDefault() {
            return this._defaultState;
        }

        public boolean enabledIn(int i) {
            return (i & this._mask) != 0;
        }

        public int getMask() {
            return this._mask;
        }
    }

    public boolean canOmitFields() {
        return true;
    }

    public boolean canUseSchema(FormatSchema formatSchema) {
        return false;
    }

    public boolean canWriteBinaryNatively() {
        return false;
    }

    public boolean canWriteFormattedNumbers() {
        return false;
    }

    public boolean canWriteObjectId() {
        return false;
    }

    public boolean canWriteTypeId() {
        return false;
    }

    public abstract void close() throws IOException;

    public abstract JsonGenerator disable(Feature feature);

    public abstract JsonGenerator enable(Feature feature);

    public abstract void flush() throws IOException;

    public CharacterEscapes getCharacterEscapes() {
        return null;
    }

    public abstract ObjectCodec getCodec();

    public abstract int getFeatureMask();

    public int getFormatFeatures() {
        return 0;
    }

    public int getHighestEscapedChar() {
        return 0;
    }

    public int getOutputBuffered() {
        return -1;
    }

    public abstract JsonStreamContext getOutputContext();

    public Object getOutputTarget() {
        return null;
    }

    public FormatSchema getSchema() {
        return null;
    }

    public abstract boolean isClosed();

    public abstract boolean isEnabled(Feature feature);

    public JsonGenerator setCharacterEscapes(CharacterEscapes characterEscapes) {
        return this;
    }

    public abstract JsonGenerator setCodec(ObjectCodec objectCodec);

    @Deprecated
    public abstract JsonGenerator setFeatureMask(int i);

    public JsonGenerator setHighestNonEscapedChar(int i) {
        return this;
    }

    public abstract JsonGenerator useDefaultPrettyPrinter();

    public abstract Version version();

    public abstract int writeBinary(Base64Variant base64Variant, InputStream inputStream, int i) throws IOException;

    public abstract void writeBinary(Base64Variant base64Variant, byte[] bArr, int i, int i2) throws IOException;

    public abstract void writeBoolean(boolean z) throws IOException;

    public abstract void writeEndArray() throws IOException;

    public abstract void writeEndObject() throws IOException;

    public abstract void writeFieldName(SerializableString serializableString) throws IOException;

    public abstract void writeFieldName(String str) throws IOException;

    public abstract void writeNull() throws IOException;

    public abstract void writeNumber(double d) throws IOException;

    public abstract void writeNumber(float f) throws IOException;

    public abstract void writeNumber(int i) throws IOException;

    public abstract void writeNumber(long j) throws IOException;

    public abstract void writeNumber(String str) throws IOException;

    public abstract void writeNumber(BigDecimal bigDecimal) throws IOException;

    public abstract void writeNumber(BigInteger bigInteger) throws IOException;

    public abstract void writeObject(Object obj) throws IOException;

    public void writeOmittedField(String str) throws IOException {
    }

    public abstract void writeRaw(char c) throws IOException;

    public abstract void writeRaw(String str) throws IOException;

    public abstract void writeRaw(String str, int i, int i2) throws IOException;

    public abstract void writeRaw(char[] cArr, int i, int i2) throws IOException;

    public abstract void writeRawUTF8String(byte[] bArr, int i, int i2) throws IOException;

    public abstract void writeRawValue(String str) throws IOException;

    public abstract void writeRawValue(String str, int i, int i2) throws IOException;

    public abstract void writeRawValue(char[] cArr, int i, int i2) throws IOException;

    public abstract void writeStartArray() throws IOException;

    public abstract void writeStartObject() throws IOException;

    public abstract void writeString(SerializableString serializableString) throws IOException;

    public abstract void writeString(String str) throws IOException;

    public abstract void writeString(char[] cArr, int i, int i2) throws IOException;

    public abstract void writeTree(TreeNode treeNode) throws IOException;

    public abstract void writeUTF8String(byte[] bArr, int i, int i2) throws IOException;

    protected JsonGenerator() {
    }

    public final JsonGenerator configure(Feature feature, boolean z) {
        if (z) {
            enable(feature);
        } else {
            disable(feature);
        }
        return this;
    }

    public JsonGenerator overrideStdFeatures(int i, int i2) {
        return setFeatureMask((i & i2) | (getFeatureMask() & (i2 ^ -1)));
    }

    public JsonGenerator overrideFormatFeatures(int i, int i2) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("No FormatFeatures defined for generator of type ");
        stringBuilder.append(getClass().getName());
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public void setSchema(FormatSchema formatSchema) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Generator of type ");
        stringBuilder.append(getClass().getName());
        stringBuilder.append(" does not support schema of type '");
        stringBuilder.append(formatSchema.getSchemaType());
        stringBuilder.append("'");
        throw new UnsupportedOperationException(stringBuilder.toString());
    }

    public JsonGenerator setPrettyPrinter(PrettyPrinter prettyPrinter) {
        this._cfgPrettyPrinter = prettyPrinter;
        return this;
    }

    public PrettyPrinter getPrettyPrinter() {
        return this._cfgPrettyPrinter;
    }

    public JsonGenerator setRootValueSeparator(SerializableString serializableString) {
        throw new UnsupportedOperationException();
    }

    public Object getCurrentValue() {
        JsonStreamContext outputContext = getOutputContext();
        if (outputContext == null) {
            return null;
        }
        return outputContext.getCurrentValue();
    }

    public void setCurrentValue(Object obj) {
        JsonStreamContext outputContext = getOutputContext();
        if (outputContext != null) {
            outputContext.setCurrentValue(obj);
        }
    }

    public void writeStartArray(int i) throws IOException {
        writeStartArray();
    }

    public void writeStartObject(Object obj) throws IOException {
        writeStartObject();
        setCurrentValue(obj);
    }

    public void writeFieldId(long j) throws IOException {
        writeFieldName(Long.toString(j));
    }

    public void writeArray(int[] iArr, int i, int i2) throws IOException {
        if (iArr != null) {
            _verifyOffsets(iArr.length, i, i2);
            writeStartArray();
            i2 += i;
            while (i < i2) {
                writeNumber(iArr[i]);
                i++;
            }
            writeEndArray();
            return;
        }
        throw new IllegalArgumentException("null array");
    }

    public void writeArray(long[] jArr, int i, int i2) throws IOException {
        if (jArr != null) {
            _verifyOffsets(jArr.length, i, i2);
            writeStartArray();
            i2 += i;
            while (i < i2) {
                writeNumber(jArr[i]);
                i++;
            }
            writeEndArray();
            return;
        }
        throw new IllegalArgumentException("null array");
    }

    public void writeArray(double[] dArr, int i, int i2) throws IOException {
        if (dArr != null) {
            _verifyOffsets(dArr.length, i, i2);
            writeStartArray();
            i2 += i;
            while (i < i2) {
                writeNumber(dArr[i]);
                i++;
            }
            writeEndArray();
            return;
        }
        throw new IllegalArgumentException("null array");
    }

    public void writeRaw(SerializableString serializableString) throws IOException {
        writeRaw(serializableString.getValue());
    }

    public void writeRawValue(SerializableString serializableString) throws IOException {
        writeRawValue(serializableString.getValue());
    }

    public void writeBinary(byte[] bArr, int i, int i2) throws IOException {
        writeBinary(Base64Variants.getDefaultVariant(), bArr, i, i2);
    }

    public void writeBinary(byte[] bArr) throws IOException {
        writeBinary(Base64Variants.getDefaultVariant(), bArr, 0, bArr.length);
    }

    public int writeBinary(InputStream inputStream, int i) throws IOException {
        return writeBinary(Base64Variants.getDefaultVariant(), inputStream, i);
    }

    public void writeNumber(short s) throws IOException {
        writeNumber((int) s);
    }

    public void writeEmbeddedObject(Object obj) throws IOException {
        if (obj == null) {
            writeNull();
        } else if (obj instanceof byte[]) {
            writeBinary((byte[]) obj);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No native support for writing embedded objects of type ");
            stringBuilder.append(obj.getClass().getName());
            throw new JsonGenerationException(stringBuilder.toString(), this);
        }
    }

    public void writeObjectId(Object obj) throws IOException {
        throw new JsonGenerationException("No native support for writing Object Ids", this);
    }

    public void writeObjectRef(Object obj) throws IOException {
        throw new JsonGenerationException("No native support for writing Object Ids", this);
    }

    public void writeTypeId(Object obj) throws IOException {
        throw new JsonGenerationException("No native support for writing Type Ids", this);
    }

    public void writeStringField(String str, String str2) throws IOException {
        writeFieldName(str);
        writeString(str2);
    }

    public final void writeBooleanField(String str, boolean z) throws IOException {
        writeFieldName(str);
        writeBoolean(z);
    }

    public final void writeNullField(String str) throws IOException {
        writeFieldName(str);
        writeNull();
    }

    public final void writeNumberField(String str, int i) throws IOException {
        writeFieldName(str);
        writeNumber(i);
    }

    public final void writeNumberField(String str, long j) throws IOException {
        writeFieldName(str);
        writeNumber(j);
    }

    public final void writeNumberField(String str, double d) throws IOException {
        writeFieldName(str);
        writeNumber(d);
    }

    public final void writeNumberField(String str, float f) throws IOException {
        writeFieldName(str);
        writeNumber(f);
    }

    public final void writeNumberField(String str, BigDecimal bigDecimal) throws IOException {
        writeFieldName(str);
        writeNumber(bigDecimal);
    }

    public final void writeBinaryField(String str, byte[] bArr) throws IOException {
        writeFieldName(str);
        writeBinary(bArr);
    }

    public final void writeArrayFieldStart(String str) throws IOException {
        writeFieldName(str);
        writeStartArray();
    }

    public final void writeObjectFieldStart(String str) throws IOException {
        writeFieldName(str);
        writeStartObject();
    }

    public final void writeObjectField(String str, Object obj) throws IOException {
        writeFieldName(str);
        writeObject(obj);
    }

    public void copyCurrentEvent(JsonParser jsonParser) throws IOException {
        JsonToken currentToken = jsonParser.currentToken();
        if (currentToken == null) {
            _reportError("No current event to copy");
        }
        int id = currentToken.id();
        if (id != -1) {
            NumberType numberType;
            switch (id) {
                case 1:
                    writeStartObject();
                    return;
                case 2:
                    writeEndObject();
                    return;
                case 3:
                    writeStartArray();
                    return;
                case 4:
                    writeEndArray();
                    return;
                case 5:
                    writeFieldName(jsonParser.getCurrentName());
                    return;
                case 6:
                    if (jsonParser.hasTextCharacters()) {
                        writeString(jsonParser.getTextCharacters(), jsonParser.getTextOffset(), jsonParser.getTextLength());
                        return;
                    } else {
                        writeString(jsonParser.getText());
                        return;
                    }
                case 7:
                    numberType = jsonParser.getNumberType();
                    if (numberType == NumberType.INT) {
                        writeNumber(jsonParser.getIntValue());
                        return;
                    } else if (numberType == NumberType.BIG_INTEGER) {
                        writeNumber(jsonParser.getBigIntegerValue());
                        return;
                    } else {
                        writeNumber(jsonParser.getLongValue());
                        return;
                    }
                case 8:
                    numberType = jsonParser.getNumberType();
                    if (numberType == NumberType.BIG_DECIMAL) {
                        writeNumber(jsonParser.getDecimalValue());
                        return;
                    } else if (numberType == NumberType.FLOAT) {
                        writeNumber(jsonParser.getFloatValue());
                        return;
                    } else {
                        writeNumber(jsonParser.getDoubleValue());
                        return;
                    }
                case 9:
                    writeBoolean(true);
                    return;
                case 10:
                    writeBoolean(false);
                    return;
                case 11:
                    writeNull();
                    return;
                case 12:
                    writeObject(jsonParser.getEmbeddedObject());
                    return;
                default:
                    _throwInternal();
                    return;
            }
        }
        _reportError("No current event to copy");
    }

    public void copyCurrentStructure(JsonParser jsonParser) throws IOException {
        JsonToken currentToken = jsonParser.currentToken();
        if (currentToken == null) {
            _reportError("No current event to copy");
        }
        int id = currentToken.id();
        if (id == 5) {
            writeFieldName(jsonParser.getCurrentName());
            id = jsonParser.nextToken().id();
        }
        if (id == 1) {
            writeStartObject();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                copyCurrentStructure(jsonParser);
            }
            writeEndObject();
        } else if (id != 3) {
            copyCurrentEvent(jsonParser);
        } else {
            writeStartArray();
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                copyCurrentStructure(jsonParser);
            }
            writeEndArray();
        }
    }

    /* Access modifiers changed, original: protected */
    public void _reportError(String str) throws JsonGenerationException {
        throw new JsonGenerationException(str, this);
    }

    /* Access modifiers changed, original: protected|final */
    public final void _throwInternal() {
        VersionUtil.throwInternal();
    }

    /* Access modifiers changed, original: protected */
    public void _reportUnsupportedOperation() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Operation not supported by generator of type ");
        stringBuilder.append(getClass().getName());
        throw new UnsupportedOperationException(stringBuilder.toString());
    }

    /* Access modifiers changed, original: protected|final */
    public final void _verifyOffsets(int i, int i2, int i3) {
        if (i2 < 0 || i2 + i3 > i) {
            throw new IllegalArgumentException(String.format("invalid argument(s) (offset=%d, length=%d) for input array of %d element", new Object[]{Integer.valueOf(i2), Integer.valueOf(i3), Integer.valueOf(i)}));
        }
    }

    /* Access modifiers changed, original: protected */
    public void _writeSimpleObject(Object obj) throws IOException {
        if (obj == null) {
            writeNull();
        } else if (obj instanceof String) {
            writeString((String) obj);
        } else {
            if (obj instanceof Number) {
                Number number = (Number) obj;
                if (number instanceof Integer) {
                    writeNumber(number.intValue());
                    return;
                } else if (number instanceof Long) {
                    writeNumber(number.longValue());
                    return;
                } else if (number instanceof Double) {
                    writeNumber(number.doubleValue());
                    return;
                } else if (number instanceof Float) {
                    writeNumber(number.floatValue());
                    return;
                } else if (number instanceof Short) {
                    writeNumber(number.shortValue());
                    return;
                } else if (number instanceof Byte) {
                    writeNumber((short) number.byteValue());
                    return;
                } else if (number instanceof BigInteger) {
                    writeNumber((BigInteger) number);
                    return;
                } else if (number instanceof BigDecimal) {
                    writeNumber((BigDecimal) number);
                    return;
                } else if (number instanceof AtomicInteger) {
                    writeNumber(((AtomicInteger) number).get());
                    return;
                } else if (number instanceof AtomicLong) {
                    writeNumber(((AtomicLong) number).get());
                    return;
                }
            } else if (obj instanceof byte[]) {
                writeBinary((byte[]) obj);
                return;
            } else if (obj instanceof Boolean) {
                writeBoolean(((Boolean) obj).booleanValue());
                return;
            } else if (obj instanceof AtomicBoolean) {
                writeBoolean(((AtomicBoolean) obj).get());
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No ObjectCodec defined for the generator, can only serialize simple wrapper types (type passed ");
            stringBuilder.append(obj.getClass().getName());
            stringBuilder.append(")");
            throw new IllegalStateException(stringBuilder.toString());
        }
    }
}
