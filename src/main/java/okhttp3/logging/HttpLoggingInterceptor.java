package okhttp3.logging;

import com.bumptech.glide.load.Key;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;
import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Interceptor.Chain;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSource;

public final class HttpLoggingInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName(Key.STRING_CHARSET_NAME);
    private volatile Level level;
    private final Logger logger;

    public enum Level {
        NONE,
        BASIC,
        HEADERS,
        BODY
    }

    public interface Logger {
        public static final Logger DEFAULT = new Logger() {
            public void log(String str) {
                Platform.get().log(4, str, null);
            }
        };

        void log(String str);
    }

    public HttpLoggingInterceptor() {
        this(Logger.DEFAULT);
    }

    public HttpLoggingInterceptor(Logger logger) {
        this.level = Level.NONE;
        this.logger = logger;
    }

    public HttpLoggingInterceptor setLevel(Level level) {
        if (level != null) {
            this.level = level;
            return this;
        }
        throw new NullPointerException("level == null. Use Level.NONE instead.");
    }

    public Level getLevel() {
        return this.level;
    }

    public Response intercept(Chain chain) throws IOException {
        Chain chain2 = chain;
        Level level = this.level;
        Request request = chain.request();
        if (level == Level.NONE) {
            return chain2.proceed(request);
        }
        StringBuilder stringBuilder;
        Object obj = 1;
        Object obj2 = level == Level.BODY ? 1 : null;
        Object obj3 = (obj2 != null || level == Level.HEADERS) ? 1 : null;
        RequestBody body = request.body();
        if (body == null) {
            obj = null;
        }
        Connection connection = chain.connection();
        Object protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("--> ");
        stringBuilder2.append(request.method());
        stringBuilder2.append(' ');
        stringBuilder2.append(request.url());
        stringBuilder2.append(' ');
        stringBuilder2.append(protocol);
        String stringBuilder3 = stringBuilder2.toString();
        if (obj3 == null && obj != null) {
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append(stringBuilder3);
            stringBuilder2.append(" (");
            stringBuilder2.append(body.contentLength());
            stringBuilder2.append("-byte body)");
            stringBuilder3 = stringBuilder2.toString();
        }
        this.logger.log(stringBuilder3);
        if (obj3 != null) {
            if (obj != null) {
                Logger logger;
                StringBuilder stringBuilder4;
                if (body.contentType() != null) {
                    logger = this.logger;
                    stringBuilder4 = new StringBuilder();
                    stringBuilder4.append("Content-Type: ");
                    stringBuilder4.append(body.contentType());
                    logger.log(stringBuilder4.toString());
                }
                if (body.contentLength() != -1) {
                    logger = this.logger;
                    stringBuilder4 = new StringBuilder();
                    stringBuilder4.append("Content-Length: ");
                    stringBuilder4.append(body.contentLength());
                    logger.log(stringBuilder4.toString());
                }
            }
            Headers headers = request.headers();
            int size = headers.size();
            for (int i = 0; i < size; i++) {
                String name = headers.name(i);
                if (!("Content-Type".equalsIgnoreCase(name) || "Content-Length".equalsIgnoreCase(name))) {
                    Logger logger2 = this.logger;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(name);
                    stringBuilder.append(": ");
                    stringBuilder.append(headers.value(i));
                    logger2.log(stringBuilder.toString());
                }
            }
            Logger logger3;
            StringBuilder stringBuilder5;
            if (obj2 == null || obj == null) {
                logger3 = this.logger;
                stringBuilder5 = new StringBuilder();
                stringBuilder5.append("--> END ");
                stringBuilder5.append(request.method());
                logger3.log(stringBuilder5.toString());
            } else if (bodyEncoded(request.headers())) {
                logger3 = this.logger;
                stringBuilder5 = new StringBuilder();
                stringBuilder5.append("--> END ");
                stringBuilder5.append(request.method());
                stringBuilder5.append(" (encoded body omitted)");
                logger3.log(stringBuilder5.toString());
            } else {
                Buffer buffer = new Buffer();
                body.writeTo(buffer);
                Charset charset = UTF8;
                MediaType contentType = body.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                this.logger.log("");
                if (isPlaintext(buffer)) {
                    this.logger.log(buffer.readString(charset));
                    logger3 = this.logger;
                    stringBuilder5 = new StringBuilder();
                    stringBuilder5.append("--> END ");
                    stringBuilder5.append(request.method());
                    stringBuilder5.append(" (");
                    stringBuilder5.append(body.contentLength());
                    stringBuilder5.append("-byte body)");
                    logger3.log(stringBuilder5.toString());
                } else {
                    logger3 = this.logger;
                    stringBuilder5 = new StringBuilder();
                    stringBuilder5.append("--> END ");
                    stringBuilder5.append(request.method());
                    stringBuilder5.append(" (binary ");
                    stringBuilder5.append(body.contentLength());
                    stringBuilder5.append("-byte body omitted)");
                    logger3.log(stringBuilder5.toString());
                }
            }
        }
        long nanoTime = System.nanoTime();
        try {
            String stringBuilder6;
            String stringBuilder7;
            Response proceed = chain2.proceed(request);
            nanoTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - nanoTime);
            ResponseBody body2 = proceed.body();
            long contentLength = body2.contentLength();
            if (contentLength != -1) {
                StringBuilder stringBuilder8 = new StringBuilder();
                stringBuilder8.append(contentLength);
                stringBuilder8.append("-byte");
                stringBuilder6 = stringBuilder8.toString();
            } else {
                stringBuilder6 = "unknown-length";
            }
            Logger logger4 = this.logger;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("<-- ");
            stringBuilder2.append(proceed.code());
            stringBuilder2.append(' ');
            stringBuilder2.append(proceed.message());
            stringBuilder2.append(' ');
            stringBuilder2.append(proceed.request().url());
            stringBuilder2.append(" (");
            stringBuilder2.append(nanoTime);
            stringBuilder2.append("ms");
            if (obj3 == null) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(", ");
                stringBuilder.append(stringBuilder6);
                stringBuilder.append(" body");
                stringBuilder7 = stringBuilder.toString();
            } else {
                stringBuilder7 = "";
            }
            stringBuilder2.append(stringBuilder7);
            stringBuilder2.append(')');
            logger4.log(stringBuilder2.toString());
            if (obj3 != null) {
                Headers headers2 = proceed.headers();
                int size2 = headers2.size();
                for (int i2 = 0; i2 < size2; i2++) {
                    Logger logger5 = this.logger;
                    StringBuilder stringBuilder9 = new StringBuilder();
                    stringBuilder9.append(headers2.name(i2));
                    stringBuilder9.append(": ");
                    stringBuilder9.append(headers2.value(i2));
                    logger5.log(stringBuilder9.toString());
                }
                if (obj2 == null || !HttpHeaders.hasBody(proceed)) {
                    this.logger.log("<-- END HTTP");
                } else if (bodyEncoded(proceed.headers())) {
                    this.logger.log("<-- END HTTP (encoded body omitted)");
                } else {
                    BufferedSource source = body2.source();
                    source.request(Long.MAX_VALUE);
                    Buffer buffer2 = source.buffer();
                    Charset charset2 = UTF8;
                    MediaType contentType2 = body2.contentType();
                    if (contentType2 != null) {
                        try {
                            charset2 = contentType2.charset(UTF8);
                        } catch (UnsupportedCharsetException unused) {
                            this.logger.log("");
                            this.logger.log("Couldn't decode the response body; charset is likely malformed.");
                            this.logger.log("<-- END HTTP");
                            return proceed;
                        }
                    }
                    Logger logger6;
                    StringBuilder stringBuilder10;
                    if (isPlaintext(buffer2)) {
                        if (contentLength != 0) {
                            this.logger.log("");
                            this.logger.log(buffer2.clone().readString(charset2));
                        }
                        logger6 = this.logger;
                        stringBuilder10 = new StringBuilder();
                        stringBuilder10.append("<-- END HTTP (");
                        stringBuilder10.append(buffer2.size());
                        stringBuilder10.append("-byte body)");
                        logger6.log(stringBuilder10.toString());
                    } else {
                        this.logger.log("");
                        logger6 = this.logger;
                        stringBuilder10 = new StringBuilder();
                        stringBuilder10.append("<-- END HTTP (binary ");
                        stringBuilder10.append(buffer2.size());
                        stringBuilder10.append("-byte body omitted)");
                        logger6.log(stringBuilder10.toString());
                        return proceed;
                    }
                }
            }
            return proceed;
        } catch (Exception e) {
            Exception exception = e;
            Logger logger7 = this.logger;
            StringBuilder stringBuilder11 = new StringBuilder();
            stringBuilder11.append("<-- HTTP FAILED: ");
            stringBuilder11.append(exception);
            logger7.log(stringBuilder11.toString());
            throw exception;
        }
    }

    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer buffer2 = new Buffer();
            buffer.copyTo(buffer2, 0, buffer.size() < 64 ? buffer.size() : 64);
            for (int i = 0; i < 16; i++) {
                if (buffer2.exhausted()) {
                    break;
                }
                int readUtf8CodePoint = buffer2.readUtf8CodePoint();
                if (Character.isISOControl(readUtf8CodePoint) && !Character.isWhitespace(readUtf8CodePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException unused) {
            return false;
        }
    }

    private boolean bodyEncoded(Headers headers) {
        String str = headers.get("Content-Encoding");
        return (str == null || str.equalsIgnoreCase("identity")) ? false : true;
    }
}
