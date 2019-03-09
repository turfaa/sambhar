package retrofit2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter.Factory;
import retrofit2.http.Streaming;

final class BuiltInConverters extends Factory {

    static final class BufferingResponseBodyConverter implements Converter<ResponseBody, ResponseBody> {
        static final BufferingResponseBodyConverter INSTANCE = new BufferingResponseBodyConverter();

        BufferingResponseBodyConverter() {
        }

        public ResponseBody convert(ResponseBody responseBody) throws IOException {
            try {
                ResponseBody buffer = Utils.buffer(responseBody);
                return buffer;
            } finally {
                responseBody.close();
            }
        }
    }

    static final class RequestBodyConverter implements Converter<RequestBody, RequestBody> {
        static final RequestBodyConverter INSTANCE = new RequestBodyConverter();

        public RequestBody convert(RequestBody requestBody) throws IOException {
            return requestBody;
        }

        RequestBodyConverter() {
        }
    }

    static final class StreamingResponseBodyConverter implements Converter<ResponseBody, ResponseBody> {
        static final StreamingResponseBodyConverter INSTANCE = new StreamingResponseBodyConverter();

        public ResponseBody convert(ResponseBody responseBody) throws IOException {
            return responseBody;
        }

        StreamingResponseBodyConverter() {
        }
    }

    static final class ToStringConverter implements Converter<Object, String> {
        static final ToStringConverter INSTANCE = new ToStringConverter();

        ToStringConverter() {
        }

        public String convert(Object obj) {
            return obj.toString();
        }
    }

    static final class VoidResponseBodyConverter implements Converter<ResponseBody, Void> {
        static final VoidResponseBodyConverter INSTANCE = new VoidResponseBodyConverter();

        VoidResponseBodyConverter() {
        }

        public Void convert(ResponseBody responseBody) throws IOException {
            responseBody.close();
            return null;
        }
    }

    BuiltInConverters() {
    }

    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotationArr, Retrofit retrofit) {
        if (type != ResponseBody.class) {
            return type == Void.class ? VoidResponseBodyConverter.INSTANCE : null;
        } else {
            Converter<ResponseBody, ?> converter;
            if (Utils.isAnnotationPresent(annotationArr, Streaming.class)) {
                converter = StreamingResponseBodyConverter.INSTANCE;
            } else {
                converter = BufferingResponseBodyConverter.INSTANCE;
            }
            return converter;
        }
    }

    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] annotationArr, Annotation[] annotationArr2, Retrofit retrofit) {
        return RequestBody.class.isAssignableFrom(Utils.getRawType(type)) ? RequestBodyConverter.INSTANCE : null;
    }
}
