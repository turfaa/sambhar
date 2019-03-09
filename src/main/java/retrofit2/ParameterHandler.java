package retrofit2;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import okhttp3.Headers;
import okhttp3.RequestBody;

abstract class ParameterHandler<T> {

    static final class Body<T> extends ParameterHandler<T> {
        private final Converter<T, RequestBody> converter;

        Body(Converter<T, RequestBody> converter) {
            this.converter = converter;
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(RequestBuilder requestBuilder, @Nullable T t) {
            if (t != null) {
                try {
                    requestBuilder.setBody((RequestBody) this.converter.convert(t));
                    return;
                } catch (IOException e) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unable to convert ");
                    stringBuilder.append(t);
                    stringBuilder.append(" to RequestBody");
                    throw new RuntimeException(stringBuilder.toString(), e);
                }
            }
            throw new IllegalArgumentException("Body parameter value must not be null.");
        }
    }

    static final class Field<T> extends ParameterHandler<T> {
        private final boolean encoded;
        private final String name;
        private final Converter<T, String> valueConverter;

        Field(String str, Converter<T, String> converter, boolean z) {
            this.name = (String) Utils.checkNotNull(str, "name == null");
            this.valueConverter = converter;
            this.encoded = z;
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(RequestBuilder requestBuilder, @Nullable T t) throws IOException {
            if (t != null) {
                String str = (String) this.valueConverter.convert(t);
                if (str != null) {
                    requestBuilder.addFormField(this.name, str, this.encoded);
                }
            }
        }
    }

    static final class FieldMap<T> extends ParameterHandler<Map<String, T>> {
        private final boolean encoded;
        private final Converter<T, String> valueConverter;

        FieldMap(Converter<T, String> converter, boolean z) {
            this.valueConverter = converter;
            this.encoded = z;
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(RequestBuilder requestBuilder, @Nullable Map<String, T> map) throws IOException {
            if (map != null) {
                for (Entry entry : map.entrySet()) {
                    String str = (String) entry.getKey();
                    if (str != null) {
                        Object value = entry.getValue();
                        StringBuilder stringBuilder;
                        if (value != null) {
                            String str2 = (String) this.valueConverter.convert(value);
                            if (str2 != null) {
                                requestBuilder.addFormField(str, str2, this.encoded);
                            } else {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("Field map value '");
                                stringBuilder.append(value);
                                stringBuilder.append("' converted to null by ");
                                stringBuilder.append(this.valueConverter.getClass().getName());
                                stringBuilder.append(" for key '");
                                stringBuilder.append(str);
                                stringBuilder.append("'.");
                                throw new IllegalArgumentException(stringBuilder.toString());
                            }
                        }
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Field map contained null value for key '");
                        stringBuilder.append(str);
                        stringBuilder.append("'.");
                        throw new IllegalArgumentException(stringBuilder.toString());
                    }
                    throw new IllegalArgumentException("Field map contained null key.");
                }
                return;
            }
            throw new IllegalArgumentException("Field map was null.");
        }
    }

    static final class Header<T> extends ParameterHandler<T> {
        private final String name;
        private final Converter<T, String> valueConverter;

        Header(String str, Converter<T, String> converter) {
            this.name = (String) Utils.checkNotNull(str, "name == null");
            this.valueConverter = converter;
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(RequestBuilder requestBuilder, @Nullable T t) throws IOException {
            if (t != null) {
                String str = (String) this.valueConverter.convert(t);
                if (str != null) {
                    requestBuilder.addHeader(this.name, str);
                }
            }
        }
    }

    static final class HeaderMap<T> extends ParameterHandler<Map<String, T>> {
        private final Converter<T, String> valueConverter;

        HeaderMap(Converter<T, String> converter) {
            this.valueConverter = converter;
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(RequestBuilder requestBuilder, @Nullable Map<String, T> map) throws IOException {
            if (map != null) {
                for (Entry entry : map.entrySet()) {
                    String str = (String) entry.getKey();
                    if (str != null) {
                        Object value = entry.getValue();
                        if (value != null) {
                            requestBuilder.addHeader(str, (String) this.valueConverter.convert(value));
                        } else {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Header map contained null value for key '");
                            stringBuilder.append(str);
                            stringBuilder.append("'.");
                            throw new IllegalArgumentException(stringBuilder.toString());
                        }
                    }
                    throw new IllegalArgumentException("Header map contained null key.");
                }
                return;
            }
            throw new IllegalArgumentException("Header map was null.");
        }
    }

    static final class Part<T> extends ParameterHandler<T> {
        private final Converter<T, RequestBody> converter;
        private final Headers headers;

        Part(Headers headers, Converter<T, RequestBody> converter) {
            this.headers = headers;
            this.converter = converter;
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(RequestBuilder requestBuilder, @Nullable T t) {
            if (t != null) {
                try {
                    requestBuilder.addPart(this.headers, (RequestBody) this.converter.convert(t));
                } catch (IOException e) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unable to convert ");
                    stringBuilder.append(t);
                    stringBuilder.append(" to RequestBody");
                    throw new RuntimeException(stringBuilder.toString(), e);
                }
            }
        }
    }

    static final class PartMap<T> extends ParameterHandler<Map<String, T>> {
        private final String transferEncoding;
        private final Converter<T, RequestBody> valueConverter;

        PartMap(Converter<T, RequestBody> converter, String str) {
            this.valueConverter = converter;
            this.transferEncoding = str;
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(RequestBuilder requestBuilder, @Nullable Map<String, T> map) throws IOException {
            if (map != null) {
                for (Entry entry : map.entrySet()) {
                    String str = (String) entry.getKey();
                    if (str != null) {
                        Object value = entry.getValue();
                        if (value != null) {
                            String[] strArr = new String[4];
                            strArr[0] = "Content-Disposition";
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("form-data; name=\"");
                            stringBuilder.append(str);
                            stringBuilder.append("\"");
                            strArr[1] = stringBuilder.toString();
                            strArr[2] = "Content-Transfer-Encoding";
                            strArr[3] = this.transferEncoding;
                            requestBuilder.addPart(Headers.of(strArr), (RequestBody) this.valueConverter.convert(value));
                        } else {
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("Part map contained null value for key '");
                            stringBuilder2.append(str);
                            stringBuilder2.append("'.");
                            throw new IllegalArgumentException(stringBuilder2.toString());
                        }
                    }
                    throw new IllegalArgumentException("Part map contained null key.");
                }
                return;
            }
            throw new IllegalArgumentException("Part map was null.");
        }
    }

    static final class Path<T> extends ParameterHandler<T> {
        private final boolean encoded;
        private final String name;
        private final Converter<T, String> valueConverter;

        Path(String str, Converter<T, String> converter, boolean z) {
            this.name = (String) Utils.checkNotNull(str, "name == null");
            this.valueConverter = converter;
            this.encoded = z;
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(RequestBuilder requestBuilder, @Nullable T t) throws IOException {
            if (t != null) {
                requestBuilder.addPathParam(this.name, (String) this.valueConverter.convert(t), this.encoded);
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Path parameter \"");
            stringBuilder.append(this.name);
            stringBuilder.append("\" value must not be null.");
            throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    static final class Query<T> extends ParameterHandler<T> {
        private final boolean encoded;
        private final String name;
        private final Converter<T, String> valueConverter;

        Query(String str, Converter<T, String> converter, boolean z) {
            this.name = (String) Utils.checkNotNull(str, "name == null");
            this.valueConverter = converter;
            this.encoded = z;
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(RequestBuilder requestBuilder, @Nullable T t) throws IOException {
            if (t != null) {
                String str = (String) this.valueConverter.convert(t);
                if (str != null) {
                    requestBuilder.addQueryParam(this.name, str, this.encoded);
                }
            }
        }
    }

    static final class QueryMap<T> extends ParameterHandler<Map<String, T>> {
        private final boolean encoded;
        private final Converter<T, String> valueConverter;

        QueryMap(Converter<T, String> converter, boolean z) {
            this.valueConverter = converter;
            this.encoded = z;
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(RequestBuilder requestBuilder, @Nullable Map<String, T> map) throws IOException {
            if (map != null) {
                for (Entry entry : map.entrySet()) {
                    String str = (String) entry.getKey();
                    if (str != null) {
                        Object value = entry.getValue();
                        StringBuilder stringBuilder;
                        if (value != null) {
                            String str2 = (String) this.valueConverter.convert(value);
                            if (str2 != null) {
                                requestBuilder.addQueryParam(str, str2, this.encoded);
                            } else {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("Query map value '");
                                stringBuilder.append(value);
                                stringBuilder.append("' converted to null by ");
                                stringBuilder.append(this.valueConverter.getClass().getName());
                                stringBuilder.append(" for key '");
                                stringBuilder.append(str);
                                stringBuilder.append("'.");
                                throw new IllegalArgumentException(stringBuilder.toString());
                            }
                        }
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Query map contained null value for key '");
                        stringBuilder.append(str);
                        stringBuilder.append("'.");
                        throw new IllegalArgumentException(stringBuilder.toString());
                    }
                    throw new IllegalArgumentException("Query map contained null key.");
                }
                return;
            }
            throw new IllegalArgumentException("Query map was null.");
        }
    }

    static final class QueryName<T> extends ParameterHandler<T> {
        private final boolean encoded;
        private final Converter<T, String> nameConverter;

        QueryName(Converter<T, String> converter, boolean z) {
            this.nameConverter = converter;
            this.encoded = z;
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(RequestBuilder requestBuilder, @Nullable T t) throws IOException {
            if (t != null) {
                requestBuilder.addQueryParam((String) this.nameConverter.convert(t), null, this.encoded);
            }
        }
    }

    static final class RawPart extends ParameterHandler<okhttp3.MultipartBody.Part> {
        static final RawPart INSTANCE = new RawPart();

        private RawPart() {
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(RequestBuilder requestBuilder, @Nullable okhttp3.MultipartBody.Part part) throws IOException {
            if (part != null) {
                requestBuilder.addPart(part);
            }
        }
    }

    static final class RelativeUrl extends ParameterHandler<Object> {
        RelativeUrl() {
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(RequestBuilder requestBuilder, @Nullable Object obj) {
            Utils.checkNotNull(obj, "@Url parameter is null.");
            requestBuilder.setRelativeUrl(obj);
        }
    }

    public abstract void apply(RequestBuilder requestBuilder, @Nullable T t) throws IOException;

    ParameterHandler() {
    }

    /* Access modifiers changed, original: final */
    public final ParameterHandler<Iterable<T>> iterable() {
        return new ParameterHandler<Iterable<T>>() {
            /* Access modifiers changed, original: 0000 */
            public void apply(RequestBuilder requestBuilder, @Nullable Iterable<T> iterable) throws IOException {
                if (iterable != null) {
                    for (T apply : iterable) {
                        ParameterHandler.this.apply(requestBuilder, apply);
                    }
                }
            }
        };
    }

    /* Access modifiers changed, original: final */
    public final ParameterHandler<Object> array() {
        return new ParameterHandler<Object>() {
            /* Access modifiers changed, original: 0000 */
            public void apply(RequestBuilder requestBuilder, @Nullable Object obj) throws IOException {
                if (obj != null) {
                    int length = Array.getLength(obj);
                    for (int i = 0; i < length; i++) {
                        ParameterHandler.this.apply(requestBuilder, Array.get(obj, i));
                    }
                }
            }
        };
    }
}
