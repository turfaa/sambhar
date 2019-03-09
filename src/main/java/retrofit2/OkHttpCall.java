package retrofit2;

import android.support.v7.widget.helper.ItemTouchHelper;
import java.io.IOException;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

final class OkHttpCall<T> implements Call<T> {
    @Nullable
    private final Object[] args;
    private volatile boolean canceled;
    @GuardedBy("this")
    @Nullable
    private Throwable creationFailure;
    @GuardedBy("this")
    private boolean executed;
    @GuardedBy("this")
    @Nullable
    private Call rawCall;
    private final ServiceMethod<T, ?> serviceMethod;

    static final class ExceptionCatchingRequestBody extends ResponseBody {
        private final ResponseBody delegate;
        IOException thrownException;

        ExceptionCatchingRequestBody(ResponseBody responseBody) {
            this.delegate = responseBody;
        }

        public MediaType contentType() {
            return this.delegate.contentType();
        }

        public long contentLength() {
            return this.delegate.contentLength();
        }

        public BufferedSource source() {
            return Okio.buffer(new ForwardingSource(this.delegate.source()) {
                public long read(Buffer buffer, long j) throws IOException {
                    try {
                        return super.read(buffer, j);
                    } catch (IOException e) {
                        ExceptionCatchingRequestBody.this.thrownException = e;
                        throw e;
                    }
                }
            });
        }

        public void close() {
            this.delegate.close();
        }

        /* Access modifiers changed, original: 0000 */
        public void throwIfCaught() throws IOException {
            if (this.thrownException != null) {
                throw this.thrownException;
            }
        }
    }

    static final class NoContentResponseBody extends ResponseBody {
        private final long contentLength;
        private final MediaType contentType;

        NoContentResponseBody(MediaType mediaType, long j) {
            this.contentType = mediaType;
            this.contentLength = j;
        }

        public MediaType contentType() {
            return this.contentType;
        }

        public long contentLength() {
            return this.contentLength;
        }

        public BufferedSource source() {
            throw new IllegalStateException("Cannot read raw response body of a converted body.");
        }
    }

    OkHttpCall(ServiceMethod<T, ?> serviceMethod, @Nullable Object[] objArr) {
        this.serviceMethod = serviceMethod;
        this.args = objArr;
    }

    public OkHttpCall<T> clone() {
        return new OkHttpCall(this.serviceMethod, this.args);
    }

    public synchronized Request request() {
        Call call = this.rawCall;
        if (call != null) {
            return call.request();
        } else if (this.creationFailure == null) {
            try {
                call = createRawCall();
                this.rawCall = call;
                return call.request();
            } catch (RuntimeException e) {
                this.creationFailure = e;
                throw e;
            } catch (IOException e2) {
                this.creationFailure = e2;
                throw new RuntimeException("Unable to create request.", e2);
            }
        } else if (this.creationFailure instanceof IOException) {
            throw new RuntimeException("Unable to create request.", this.creationFailure);
        } else {
            throw ((RuntimeException) this.creationFailure);
        }
    }

    public void enqueue(final Callback<T> callback) {
        Call call;
        Throwable th;
        Utils.checkNotNull(callback, "callback == null");
        synchronized (this) {
            if (this.executed) {
                throw new IllegalStateException("Already executed.");
            }
            this.executed = true;
            call = this.rawCall;
            th = this.creationFailure;
            if (call == null && th == null) {
                try {
                    Call createRawCall = createRawCall();
                    this.rawCall = createRawCall;
                    call = createRawCall;
                } catch (Throwable th2) {
                    th = th2;
                    this.creationFailure = th;
                }
            }
        }
        if (th != null) {
            callback.onFailure(this, th);
            return;
        }
        if (this.canceled) {
            call.cancel();
        }
        call.enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    callSuccess(OkHttpCall.this.parseResponse(response));
                } catch (Throwable th) {
                    callFailure(th);
                }
            }

            public void onFailure(Call call, IOException iOException) {
                try {
                    callback.onFailure(OkHttpCall.this, iOException);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }

            private void callFailure(Throwable th) {
                try {
                    callback.onFailure(OkHttpCall.this, th);
                } catch (Throwable th2) {
                    th2.printStackTrace();
                }
            }

            private void callSuccess(Response<T> response) {
                try {
                    callback.onResponse(OkHttpCall.this, response);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        });
    }

    public synchronized boolean isExecuted() {
        return this.executed;
    }

    public Response<T> execute() throws IOException {
        Call call;
        synchronized (this) {
            if (this.executed) {
                throw new IllegalStateException("Already executed.");
            }
            this.executed = true;
            if (this.creationFailure == null) {
                call = this.rawCall;
                if (call == null) {
                    try {
                        call = createRawCall();
                        this.rawCall = call;
                    } catch (IOException | RuntimeException e) {
                        this.creationFailure = e;
                        throw e;
                    }
                }
            } else if (this.creationFailure instanceof IOException) {
                throw ((IOException) this.creationFailure);
            } else {
                throw ((RuntimeException) this.creationFailure);
            }
        }
        if (this.canceled) {
            call.cancel();
        }
        return parseResponse(call.execute());
    }

    private Call createRawCall() throws IOException {
        Call newCall = this.serviceMethod.callFactory.newCall(this.serviceMethod.toRequest(this.args));
        if (newCall != null) {
            return newCall;
        }
        throw new NullPointerException("Call.Factory returned null.");
    }

    /* Access modifiers changed, original: 0000 */
    public Response<T> parseResponse(Response response) throws IOException {
        ResponseBody body = response.body();
        response = response.newBuilder().body(new NoContentResponseBody(body.contentType(), body.contentLength())).build();
        int code = response.code();
        if (code < ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION || code >= 300) {
            try {
                Response<T> error = Response.error(Utils.buffer(body), response);
                return error;
            } finally {
                body.close();
            }
        } else if (code == 204 || code == 205) {
            body.close();
            return Response.success(null, response);
        } else {
            ExceptionCatchingRequestBody exceptionCatchingRequestBody = new ExceptionCatchingRequestBody(body);
            try {
                return Response.success(this.serviceMethod.toResponse(exceptionCatchingRequestBody), response);
            } catch (RuntimeException e) {
                exceptionCatchingRequestBody.throwIfCaught();
                throw e;
            }
        }
    }

    public void cancel() {
        Call call;
        this.canceled = true;
        synchronized (this) {
            call = this.rawCall;
        }
        if (call != null) {
            call.cancel();
        }
    }

    public boolean isCanceled() {
        boolean z = true;
        if (this.canceled) {
            return true;
        }
        synchronized (this) {
            if (this.rawCall == null || !this.rawCall.isCanceled()) {
                z = false;
            }
        }
        return z;
    }
}
